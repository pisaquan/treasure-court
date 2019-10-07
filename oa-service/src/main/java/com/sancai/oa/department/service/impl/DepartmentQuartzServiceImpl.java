package com.sancai.oa.department.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.department.mapper.TDepartmentMapper;
import com.sancai.oa.department.service.IDepartmentQuartzService;
import com.sancai.oa.department.service.ITDepartmentService;
import com.sancai.oa.department.threadpool.DepartmentTask;
import com.sancai.oa.department.threadpool.UserTask;
import com.sancai.oa.dingding.department.DingDingDepartmentService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.mapper.UserDepartmentMapper;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.ITUserDepartmentService;
import com.sancai.oa.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 任务调度类
 *
 * @Author wangyl
 * @create 2019/7/30 09:14
 */
@Slf4j
@Service
public class DepartmentQuartzServiceImpl implements IDepartmentQuartzService {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DingDingDepartmentService dingDingDepartmentService;
    @Autowired
    private DingDingUserService dingDingUserService;
    @Autowired
    private ICompanyService companyService;
    @Autowired
    private IUserService userService;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ThreadPoolTool<Map<String, List<String>>> threadPoolTool;
    @Autowired
    private ITaskInstanceService iTaskInstanceService;
    @Autowired
    private ITUserDepartmentService iTUserDepartmentService;
    @Autowired
    private UserDepartmentMapper userDepartmentMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ITDepartmentService iTDepartmentService;
    @Autowired
    private TDepartmentMapper tDepartmentMapper;
    @Autowired
    private IExamineService examineService;
    /**
     * quartz 获取公司下的部门信息
     */
    @Override
    public void departmentList(String companyid, String taskInstanceId) throws Exception{
        // 获取钉钉里departmentList
        List<OapiDepartmentListResponse.Department> departmentList = dingDingDepartmentService.departmentList(companyid);
        if (null != departmentList && departmentList.size() > 0) {
            Department departmentCompnaynode = new Department();
            departmentCompnaynode.setId("1");
            Company company = companyService.getById(companyid);
            if (null != company) {
                departmentCompnaynode.setName(company.getName());
            }

            departmentCompnaynode.setLevel(0L);
            departmentCompnaynode.setParentid("-1");
            // 清除redis中老的部门信息
            redisUtil.del(companyid);
            //存储在缓存中的set中 24h过期  24*60*60
            redisUtil.sSetAndTime(companyid, 24 * 60 * 60, departmentCompnaynode);

            // 组装树形结构，完成后存储在redis中
            findChildrenTreebyList(taskInstanceId, companyid, departmentCompnaynode, departmentList);
            // 存储部门信息
            Set<Object> departmentSet = redisUtil.sGet(companyid);
            // 查询公司下的部门信息，然后比对处理  deptid存在则更新，不存在则新增
            QueryWrapper<TDepartment> wrapperTDepartment = new QueryWrapper();
            wrapperTDepartment.lambda().eq(TDepartment::getDeleted, 0L);
            wrapperTDepartment.lambda().eq(TDepartment::getCompanyId, companyid);
            List<TDepartment> tDepartmentList = iTDepartmentService.list(wrapperTDepartment);

            this.updateDepartmentInfo(departmentSet,tDepartmentList,companyid,taskInstanceId);

            // 根据部门id获取部门下用户信息，并持久化到数据库中
            updateUserinfoByDepartmentId(companyid, departmentList, taskInstanceId);

            TaskMessage.addMessage(taskInstanceId, "部门抓取-redis 完成");
        }
    }

    private void updateDepartmentInfo( Set<Object> departmentSet ,List<TDepartment> tDepartmentList ,String companyId,String taskInstanceId){
        List<TDepartment> tDepartmentListADD = new ArrayList<TDepartment>();
        List<TDepartment> tDepartmentListUPD = new ArrayList<TDepartment>();

        // 将数据库中的值改为map结构
        Map<String, TDepartment> tDepartmentMapDB = tDepartmentList.stream().collect(Collectors.toMap(TDepartment::getDeptId, a -> a, (k1, k2) -> k1));
        //遍历set 存在则update 不存在则insert
        if(null==departmentSet||departmentSet.size()==0){
            return;
        }

        for(Object o:departmentSet){
            HashMap oMap = (HashMap)o;

            TDepartment tDepartment = new TDepartment();

            Object name = oMap.get("name");
            Object deptId = oMap.get("id");
            Object parentId = oMap.get("parentid");
            Object level = oMap.get("level");

            if(null!=name){
                tDepartment.setDeptName(name.toString());
            }
            if(null!=deptId){
                tDepartment.setDeptId(deptId.toString());
            }
            if(null!=parentId){
                tDepartment.setParentId(parentId.toString());
            }
            if(null!=level){
                tDepartment.setLevel(Integer.parseInt(level.toString()));
            }

            if(null==tDepartment){
                continue;
            }
            TDepartment tDepartment1DB = tDepartmentMapDB.get(tDepartment.getDeptId());

            if(null==tDepartment1DB){
                TDepartment tDepartment1 = new TDepartment();
                tDepartment1.setId(UUIDS.getID());
                tDepartment1.setCompanyId(companyId);
                tDepartment1.setTaskInstanceId(taskInstanceId);
                tDepartment1.setDeleted(0);
                tDepartment1.setCreateTime(System.currentTimeMillis());
                tDepartment1.setDeptId(tDepartment.getDeptId());
                tDepartment1.setDeptName(tDepartment.getDeptName());
                tDepartment1.setLevel(tDepartment.getLevel());
                tDepartment1.setParentId(tDepartment.getParentId());

                // 数据库中不存在，新增
                tDepartmentListADD.add(tDepartment1);
                continue;
            }else{
                // 数据库中存在，更新
                TDepartment tDepartment1 = new TDepartment();
                tDepartment1DB.setDeptName(tDepartment.getDeptName());
                tDepartment1DB.setLevel(tDepartment.getLevel());
                tDepartment1DB.setDeptId(tDepartment.getDeptId());
                tDepartment1DB.setParentId(tDepartment.getParentId());
                tDepartment1DB.setTaskInstanceId(taskInstanceId);
                tDepartment1DB.setModifyTime(System.currentTimeMillis());

                tDepartmentListUPD.add(tDepartment1DB);
            }

        }


        //一次性批量执行
        if(null!=tDepartmentListUPD && tDepartmentListUPD.size()>0){
            iTDepartmentService.updateBatchById(tDepartmentListUPD);
        }
        if(null!=tDepartmentListADD && tDepartmentListADD.size()>0){
            tDepartmentMapper.batchSave(tDepartmentListADD);
        }

    }
    /**
     * 获取子部门信息
     *
     * @param departmentCompnaynode
     * @param departmentList
     * @return
     */
    private Department findChildrenTreebyList(String taskInstanceId, String companyid, Department departmentCompnaynode, List<OapiDepartmentListResponse.Department> departmentList) {
        for (OapiDepartmentListResponse.Department d : departmentList) {
            if (departmentCompnaynode.getId().equals(d.getParentid().longValue() + "")) {
//                System.out.println("组装部门 tree:" + d.getName());
                TaskMessage.addMessage(taskInstanceId, d.getName() + " 存入 redis");
                Department tmp = new Department();
                Map m = new HashMap<>();
                tmp.setId(d.getId() + "");
                m.put("id", d.getId());
                tmp.setParentid(d.getParentid() + "");
                m.put("parentid", d.getParentid());
                tmp.setName(d.getName());
                m.put("name", d.getName());
                tmp.setLevel(departmentCompnaynode.getLevel() + 1);
                m.put("level", departmentCompnaynode.getLevel() + 1);
                //存储在list缓存中
                redisUtil.sSetAndTime(companyid, 24 * 60 * 60, tmp);
                //存储部门信息
                redisUtil.hmset(m.get("id") + "", m, 24 * 60 * 60);
                Department dtree = findChildrenTreebyList(taskInstanceId, companyid, tmp, departmentList);

            }
        }
        return departmentCompnaynode;
    }

    /**
     * 抓取公司下的部门用户id
     */
    public List<Future<ThreadResult>> graspDepartment(String taskInstanceId, String companyId, List<OapiDepartmentListResponse.Department> departmentList, boolean isFinish) throws Exception{
        TaskMessage.addMessage(taskInstanceId, "部门抓取  companyId：" + companyId);
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            return null;
        }
        int threadCount = 5;

        long startTime = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("taskInstanceId", taskInstanceId);
        params.put("companyId", companyId);
        params.put("dingDingUserService", dingDingUserService);
        params.put("startTime", startTime);
        params.put("isFinish", isFinish);

        ThreadResult result = threadPoolTool.excuteTaskFuture(transactionManager, taskInstanceId, departmentList, threadCount, params, isFinish, DepartmentTask.class);
        List<Future<ThreadResult>> resList = result.getData();

        if(result.getFlag()){
            resList = result.getData();
        }else{
            throw result.getE();
        }
        return resList;
    }


    /**
     * 抓取用户信息
     */
    public void graspUser(String taskInstanceId, String companyId, List<String> userIdList, Map<String, String> userDeptBelong, boolean isFinish) throws Exception{
        TaskMessage.addMessage(taskInstanceId, "部门抓取  companyId：" + companyId);
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            return;
        }
        int threadCount = 5;
        if (userIdList.size() < 5) {
            threadCount = 1;
        }
        long startTime = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("taskInstanceId", taskInstanceId);
        params.put("companyId", companyId);
        params.put("dingDingUserService", dingDingUserService);
        params.put("iTUserDepartmentService", iTUserDepartmentService);
        params.put("userService", userService);
        params.put("userDeptBelong", userDeptBelong);
        params.put("startTime", startTime);
        params.put("isFinish", isFinish);
        params.put("userDepartmentMapper", userDepartmentMapper);
        params.put("userMapper", userMapper);

        ThreadResult result = threadPoolTool.excuteTaskFuture(transactionManager, taskInstanceId, userIdList, threadCount, params, isFinish, UserTask.class);
        if(!result.getFlag()){
            throw result.getE();
        }
    }

    /**
     * 获取公司下所有部门里的员工信息，并持久化到数据库中
     *
     * @param companyId
     * @param departmentList
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void updateUserinfoByDepartmentId(String companyId, List<OapiDepartmentListResponse.Department> departmentList, String taskInstanceId) throws Exception {
        OapiDepartmentListResponse.Department dCompany = new OapiDepartmentListResponse.Department();
        dCompany.setId(1L);
        departmentList.add(dCompany);
        // 获取现有数据库中公司下所有用户信息deleted = 0   userId(key)   user(value)
        List<UserDTO> userListDB = userService.listUserByCompany(companyId,0);
        Map<String, UserDTO> userMapDB = userListDB.stream().collect(Collectors.toMap(UserDTO::getUserId, a -> a, (k1, k2) -> k1));

        Set<String> setAdd = new HashSet<String>();
        List<User> updateUserList = new ArrayList<User>();

        Map<String, String> userDeptBelong = new HashMap<String, String>();

        long startTime = System.currentTimeMillis();

        List<Map<String, List<String>>> deptList = new ArrayList<Map<String, List<String>>>();

        List<Future<ThreadResult>> userOffListRes = graspDepartment(taskInstanceId, companyId, departmentList, true);

        for(Future<ThreadResult> fString:userOffListRes){
            ThreadResult res =  fString.get();
            List<Map<String, List<String>>> strList =res.getData();
            deptList.addAll(strList);
        }


        for (Map<String, List<String>> furDeptMap : deptList) {
            try {
                Map<String, List<String>> deptMap = furDeptMap;
                Set<String> keySet = furDeptMap.keySet();
                for (String deptId : keySet) {
                    // 获取部门Id 和 对应的用户id

                    //用户id集合
                    List<String> userList = deptMap.get(deptId);
                    if(null!=userList&&userList.size()>0){
                        // 遍历用户，存储部门set，并更新map表
                        for (String userId : userList) {
                            UserDTO db = userMapDB.get(userId);

                            //id不在数据库中，则新增
                            if (null == db) {
                                setAdd.add(userId);

                            }

                            //id在用户表中，检测是否发生变化
                            // 存储用户最新部门信息
                            String dptmp = deptId;
                            if (null != userDeptBelong.get(userId)) {

                                // 判断是否存在，不存在则新加
                                if (!ifExists(userDeptBelong.get(userId), deptId)) {
                                    dptmp = userDeptBelong.get(userId) + "," + deptId;
                                }
                            }
                            userDeptBelong.put(userId, dptmp);

                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        int i = 0;
        int updateUserCount = 0;

        long endTime = System.currentTimeMillis();
        int sec = (int) ((endTime - startTime) / 1000);
        System.out.println("部门下用户遍历完成，共耗时" + sec + "秒");
        TaskMessage.addMessage(taskInstanceId, "部门下用户遍历完成，共耗时" + sec + "秒");

        startTime = System.currentTimeMillis();
        // 遍历数据库中用户信息，比对部门是否变化，有则update
        List<UserDepartment> userDepartmentListAdd = new ArrayList<UserDepartment>();
        if(null!=userListDB&&userListDB.size()>0){
            for (UserDTO user : userListDB) {
                //钉钉中最新的部门
                String deptIdnew = userDeptBelong.get(user.getUserId());
                Map<String, UserDepartment> userDepartmentMapDB = user.getUserDepartments().stream().collect(Collectors.toMap(UserDepartment::getDeptId, a -> a, (k1, k2) -> k1));

                // 比对是否一致
                if (StringUtils.isEmpty(deptIdnew)) {
                    // 最新部门为空删除原有所有部门，逻辑删除，用户更新为离职状态 ，然后continue
                    List<UserDepartment> listTmp = user.getUserDepartments();
                    if(null!=listTmp&&listTmp.size()>0){
                        for(UserDepartment ud :listTmp){
                            ud.setDeleted(1);
                            iTUserDepartmentService.updateById(ud);
                        }
                    }
                    User user1 = new User();
                    user1.setId(user.getId());
                    user1.setTaskInstanceId(user.getTaskInstanceId());
                    user1.setModifyTime(System.currentTimeMillis());
                    user1.setStatus(1);
                    updateUserList.add(user1);
                    continue;
                }

                String[] dptNew = deptIdnew.split(",");
                if(null==dptNew|| dptNew.length==0){
                    continue;
                }

                //遍历新部门，比对老部门，如果存在不处理，不存在则新增（每次操作必须remove）
                for (String dbtmp : dptNew) {
                    UserDepartment dptTmp = (UserDepartment)userDepartmentMapDB.get(dbtmp);
                    if(null==dptTmp){
                        //新增部门
                        UserDepartment userDepartment = new UserDepartment();
                        userDepartment.setId(UUIDS.getID());
                        userDepartment.setDeleted(0);
                        userDepartment.setCreateTime(System.currentTimeMillis());
                        userDepartment.setDeptId(dbtmp);
                        userDepartment.setUId(user.getId());
                        userDepartment.setTaskInstanceId(taskInstanceId);

//                        iTUserDepartmentService.save(userDepartment);
                        userDepartmentListAdd.add(userDepartment);
                    }
                    userDepartmentMapDB.remove(dbtmp);
                }
                //剩余未处理的
                Iterator<Map.Entry<String, UserDepartment>> iterator = userDepartmentMapDB.entrySet().iterator();

                while(iterator.hasNext()){
                    Map.Entry entry=iterator.next();
                    UserDepartment userDepartment = (UserDepartment)entry.getValue();
                    userDepartment.setDeleted(1);
                    iTUserDepartmentService.updateById(userDepartment);
                }

            }
        }
        // 批量insert
        if(null!=userDepartmentListAdd&&userDepartmentListAdd.size()>0){
            userDepartmentMapper.batchSave(userDepartmentListAdd);
            userDepartmentListAdd.clear();
        }

        endTime = System.currentTimeMillis();
        sec = (int) ((endTime - startTime) / 1000);
        if (updateUserList.size() > 0) {
            userService.updateBatchById(updateUserList);
            updateUserCount += updateUserList.size();
        }

        System.out.println("比对用户是否变化完成，共耗时" + sec + "秒");
        TaskMessage.addMessage(taskInstanceId, "比对用户是否变化完成，共耗时" + sec + "秒,共更新用户" + updateUserCount + "个");
        startTime = System.currentTimeMillis();
        //Todo 更新新增用户的考勤组
//        updateUserList.stream().forEach(User->examineService.saveUserAttendance(companyId,User.getUserId()));

        if(null!=setAdd&&setAdd.size()>0){
            List<String> listAdd = new ArrayList<>(setAdd);

            graspUser(taskInstanceId, companyId, listAdd, userDeptBelong, true);

            endTime = System.currentTimeMillis();
            sec = (int) ((endTime - startTime) / 1000);
            System.out.println("用户批量插入完成，共耗时" + sec + "秒");
            TaskMessage.addMessage(taskInstanceId, "用户批量插入完成，共耗时" + sec + "秒");
        }

        // 批量执行用户插入操作  end
    }

    private boolean ifExists(String dptmp, String buffer) {
        boolean flag = false;
        if (StringUtils.isEmpty(buffer)) {
            return false;
        }
        for (String str : buffer.split(",")) {
            if (str.equals(dptmp)) {
                return true;
            }
        }
        return flag;
    }

    private boolean ifAllSame(List<String> db, List<String> dd) {
        boolean flag = true;
        if (db.size() != dd.size()) {
            return false;
        }
        for (String dbtmp : db) {
            if (!dd.contains(dbtmp)) {
                return false;
            }
        }
        for (String ddtmp : dd) {
            if (!db.contains(ddtmp)) {
                return false;
            }
        }
        return flag;
    }
}
