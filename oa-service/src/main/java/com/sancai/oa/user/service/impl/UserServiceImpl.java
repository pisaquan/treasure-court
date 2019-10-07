package com.sancai.oa.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.request.OapiSmartworkHrmEmployeeQuerydimissionRequest;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiSmartworkHrmEmployeeListdimissionResponse;
import com.dingtalk.api.response.OapiSmartworkHrmEmployeeQuerydimissionResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.department.mapper.DepartmentMapper;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.department.service.ITDepartmentService;
import com.sancai.oa.department.threadpool.DepartmentTask;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.typestatus.enums.UserCheckEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.entity.UserExcelDTO;
import com.sancai.oa.user.mapper.UserDepartmentMapper;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.ITUserDepartmentService;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.user.thread.InitUserTask;
import com.sancai.oa.user.thread.UserChkTask;
import com.sancai.oa.user.thread.UserOffTask;
import com.sancai.oa.utils.FileUtils;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.UUIDS;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
/**
 * @Author chenm
 * @create 2019/7/22 13:14
 */
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DingDingBase dingDingBase;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private DingDingUserService dingDingUserService;
    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    IUserService iUserService;

    @Autowired
    private ThreadPoolTool<List<String>> threadPoolTool;
    @Autowired
    private DataSourceTransactionManager transactionManager;


    @Autowired
    private ITaskInstanceService iTaskInstanceService;
    @Autowired
    private UserDepartmentMapper userDepartmentMapper;
    @Autowired
    private ITUserDepartmentService itUserDepartmentService;
    @Autowired
    private DingDingReportService dingDingReportService;

    @Autowired
    private ITDepartmentService itDepartmentService;

    @Autowired
    private IExamineService examineService;

    @Override
    public UserDTO getUserByUserId(String userId, String companyId) {
        if(StringUtils.isBlank(userId)){
            log.info("用户id为空，userId=" + userId);
            return null;
        }
        if(StringUtils.isBlank(companyId)){
            log.info("公司id为空，companyId=" + companyId);
            return null;
        }

        UserDTO user = userMapper.getUserByUserId(userId,companyId);
        return user;
    }

    @Override
    public List<User> listUser(int page, int capacity, String companyId) {
        List<User> list = new ArrayList<User>();
        PageHelper.startPage(page, capacity);
        QueryWrapper<User> wrapper = new QueryWrapper<User>().eq("company_id",companyId).notIn("status",1);
        list = this.list(wrapper);
        return list;
    }

    @Override
    public List<User> listUser(int page, String companyId) {
        return this.listUser(page,100,companyId);
    }

    @Override
    public List<UserDTO> listUserByCompany( String companyId,Integer status ) {
        return userMapper.listUserByCompany(companyId,status);
    }
    @Override
    public List<UserDTO> listUser(String companyId, int status, Long startTime, Long endTime) {
        //status 0：在职，1：离职
        // 查询出 1) 所有在职用户 2)所有离职且没有离职时间的用户  3) 所有离职且离职时间在起止时间范围内的用户
        List<UserDTO> userList = userMapper.listUserByWorkDay(companyId,status,startTime,endTime);

        return userList;
    }


    /**
     * 根据钉钉登录授权码取用户id
     * @param type  SIGNIN_CONFIRM、ATTENDANCE_RECORD,EXAMINE_LEAVE 三种
     * @param code 钉钉登录授权码
     * @param id 任务id
     * @return 返回userid
     */
    @Override
    public String getUserIdByCode(String type, String code, String id) throws ApiException {
        String companyId = null;
        if ("SIGNIN_CONFIRM".equals(type)) {
            companyId = userMapper.getCompanyIdBySId(id);
        }
        if ("ATTENDANCE_RECORD".equals(type)) {
            companyId = userMapper.getCompanyIdByAId(id);
        }
        if("EXAMINE_LEAVE".equals(type)){
            companyId = userMapper.getCompanyIdByEId(id);
        }
        //调用DingDingUserService的getUsetId
        String userId = dingDingUserService.getUsetId(companyId, code);
        return userId;
    }

    @Override
    public void updateOffUserinfoStatus(String companyId, String taskInstanceId) throws Exception{

        List<User> updateUserList = new ArrayList<User>();
        long startTime = System.currentTimeMillis();
        //获取钉钉目前公司下所有离职状态用户信息   map集合  userId （key） status （value）
        List<Future<ThreadResult>> userOffListRes = graspOffUserId(taskInstanceId,companyId);
        long endTime = System.currentTimeMillis();
        int sec = (int) ((endTime-startTime)/1000);
        System.out.println("多线程抓取离职用户id完成，共耗时"+sec+"秒");
        List<String> userOffList = new ArrayList<>();
        for(Future<ThreadResult> fString:userOffListRes){
            ThreadResult res =  fString.get();
            List<String> strList =res.getData();
            userOffList.addAll(strList);
        }
//        List<String> userOffListRes1 = dingDingUserService.getOfflineUserByCompanyId(companyId,1,20L,true);
//         sec = (int) ((System.currentTimeMillis()-endTime)/1000);
//        System.out.println("单线程线程抓取离职用户id完成，共耗时"+sec+"秒");
        endTime = System.currentTimeMillis();
        sec = (int) ((endTime-startTime)/1000);
        System.out.println("更新离职用户中抓取离职用户id完成，共耗时"+sec+"秒");
        TaskMessage.addMessage(taskInstanceId,"更新离职用户中抓取离职用户id完成，共耗时"+sec+"秒");


        //获取数据库中所有用户信息中在职，但是钉钉中已经离职的数据，并修改状态为离职
        QueryWrapper<User> wrapperUser = new QueryWrapper();
        wrapperUser.lambda().eq(User::getDeleted, 0L).eq(User::getStatus,0).in(User::getUserId,userOffList).eq(User::getCompanyId,companyId);
        List<User> userListDB = null;
        userListDB = list(wrapperUser);
        TaskMessage.addMessage(taskInstanceId,"本次更新离职用户数量为："+updateUserList.size()+" 个");

        if(userListDB == null || userListDB.size() == 0){
            return;
        }

        int threadCount = 5;

        if(userListDB.size()<=5){
            threadCount = 1;
        }

        startTime = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<>();
        params.put("taskInstanceId",taskInstanceId);
        params.put("companyId",companyId);
        params.put("userService",iUserService);
        params.put("dingDingUserService",dingDingUserService);
        params.put("isFinish",true);
        params.put("startTime",startTime);
        params.put("tDepartmentService", itDepartmentService);
        params.put("redisUtil", redisUtil);
        params.put("dingDingReportService",dingDingReportService);
        params.put("examineService",examineService);

        ThreadResult resultUser = threadPoolTool.excuteTaskFuture(transactionManager, taskInstanceId, userListDB, threadCount, params, true, UserOffTask.class);
        //离职人员的部门信息存入t_department（不重复的）中
        List<Future<ThreadResult>> resList = new ArrayList<Future<ThreadResult>>();
        resList = resultUser.getData();
        Set<String> set = new HashSet<>();
        Map<String, String> map = new HashMap<>();
        for (Future<ThreadResult> future : resList) {
            ThreadResult threadResult = future.get();
            List<String> data = (List<String>) threadResult.getData();
            if(null==data){
                continue;
            }
            for (String s : data) {
                String[] split = s.split("-");
                set.add(split[0]);
                map.put(split[0], split[1]);
            }
        }
        //获取redis中部门集合
        Set<Object> objects = redisUtil.sGet(companyId);
        Set<Department> departmentSet = new HashSet<Department>();
        for (Object o : objects) {
            Map m = (Map) o;
            Department department = new Department();
            department.setName(m.get("name") + "");
            department.setId(m.get("id") + "");
            department.setParentid(m.get("parentid")+"");
            departmentSet.add(department);
        }

        for (String s : set) {
            QueryWrapper<TDepartment> wrapperUser1 = new QueryWrapper();
            wrapperUser1.lambda().eq(TDepartment::getCompanyId, companyId).eq(TDepartment::getDeleted, 0);
            List<TDepartment> tDepartmentlist = itDepartmentService.list(wrapperUser1);
            if (!CollectionUtils.isEmpty(tDepartmentlist)) {
                TDepartment tdepartment = tDepartmentlist.stream().filter(TDepartment -> TDepartment.getDeptId().equals(s)).findAny().orElse(null);
                if (tdepartment != null) {
                    continue;
                }
            }
            TDepartment tDepartment = new TDepartment();
            //查询父部门Ids
            Department department = departmentSet.stream().filter(Department -> Department.getId().equals(s)).findAny().orElse(null);
            if(department != null){
                tDepartment.setParentId(department.getParentid());
            }else {
                tDepartment.setParentId("0");
            }
            tDepartment.setCompanyId(companyId);
            tDepartment.setDeleted(0);
            tDepartment.setDeptId(s);
            tDepartment.setDeptName(map.get(s));
            tDepartment.setCreateTime(System.currentTimeMillis());
            tDepartment.setTaskInstanceId(taskInstanceId);
            tDepartment.setId(UUIDS.getID());
            tDepartment.setLevel(5);
            itDepartmentService.save(tDepartment);
        }

        if (!resultUser.getFlag()) {
            throw resultUser.getE();
        }
    }

    @Override
    public List<Map> getUserInfo(String companyId,String name, int type) {
        List<Map> res = new ArrayList<Map>();

        List<Department> result = departmentService.listDepartment(companyId);
        List<String> deptlist = new ArrayList<String>();
        Map deptmap = new HashMap();
        for (Department d : result) {
            deptmap.put(d.getId(), d.getName());
        }

        List<UserDTO> userList = userMapper.listUserByCompanyAndName(name,type,companyId);

        if(null!=userList&&userList.size()>0){
            for(UserDTO user:userList){
                Map map = new HashMap();

                StringBuffer deptName = new StringBuffer();
                String dName = "";

                List<UserDepartment> userDepartmentList = user.getUserDepartments();
                if(null!=userDepartmentList&&userDepartmentList.size()>0){
                    for(UserDepartment userDepartment:userDepartmentList){
                        Object dptName = deptmap.get(userDepartment.getDeptId());
                        if(null!=dptName){
                            deptName.append(dptName.toString()+",");
                        }
                    }
                }

                if(!StringUtils.isEmpty(deptName.toString())){
                    dName =deptName.substring(0,deptName.toString().length()-1);
                }

                map.put("user_id",user.getUserId());
                map.put("status",user.getStatus());
                map.put("dept_name",dName);
                map.put("mobile",user.getMobile());
                map.put("name",user.getName());
                res.add(map);
            }
        }

        return res;
    }





    /**
     * 抓取公司下离职员工user_id
     */
    public List<Future<ThreadResult>> graspOffUserId(String taskInstanceId, String companyId) throws Exception{
        TaskMessage.addMessage(taskInstanceId, "离职用户id抓取   companyId：" + companyId);
        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            return null;
        }
        int threadCount = 5;
        long page = 20;
        long startTime = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<>();
        params.put("taskInstanceId", taskInstanceId);
        params.put("companyId", companyId);
        params.put("dingDingUserService", dingDingUserService);
        params.put("page", page);
        params.put("startTime", startTime);
        // 分成五段抓取数据
        List<String> data = new ArrayList<String>();
        for(int i=0;i<6;i++){
            data.add(i+"");
        }

        ThreadResult result = threadPoolTool.excuteTaskFuture(transactionManager, taskInstanceId, data, threadCount, params, true, UserChkTask.class);
        List<Future<ThreadResult>> resList = new ArrayList<Future<ThreadResult>>();
        if(result.getFlag()){
            resList = result.getData();
        }else{
            throw result.getE();
        }

        return resList;
    }
    /**
     * 导入离职员工信息
     * @param file excel文件
     * @param companyId 公司id
     */
    @Override

    public void initUserInfoByExcel(MultipartFile file , String companyId){
        //解析excel离职用户数据集合
        List<UserExcelDTO> excelModelList1 = FileUtils.importExcel(file,0,1,UserExcelDTO.class);
        //查询数据库中已存在的离职用户数据集合，进行数据重复预处理,提高执行效率
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getCompanyId, companyId);
        queryWrapper.lambda().eq(User::getStatus, 1);
        queryWrapper.lambda().eq(User::getDeleted, 0);
        queryWrapper.lambda().eq(User::getCompanyId, companyId);
        List<User> userList =  userMapper.selectList(queryWrapper);

        List<String> userIdList = userList.stream().map(User -> User.getUserId()).collect(Collectors.toList());

        List<String> userId = new ArrayList<>();
        List<UserExcelDTO> excelModelList = excelModelList1.stream().filter(v -> {
                    if(StringUtils.isBlank(v.getUserId())||StringUtils.isBlank(v.getName())){
                        return  false;
                    }
                    v.setName(v.getName().replaceAll(" ","").replaceAll(" ","").trim());
                    boolean flag = !userId.contains(v.getUserId()) && !userIdList.contains(v.getUserId());
                    userId.add(v.getUserId());
                    return flag;
                }
        ).collect(Collectors.toList());

        if(excelModelList == null || excelModelList.size() == 0){
            return;
        }

        //缓存查询公司所有部门信息
        List<Department> departmentRedis = departmentService.listDepartment(companyId);

        List<List<UserExcelDTO>> openEmpSimples = ListUtils.fixedGrouping(excelModelList,50);
        int threadCount = 5;
        long startTime = System.currentTimeMillis();
        String taskInstanceId = UUIDS.getID();
        Map<String,Object> params = new HashMap<>();
        params.put("taskInstanceId",taskInstanceId);
        params.put("companyId",companyId);
        params.put("UserService",iUserService);
        params.put("isFinish",false);
        params.put("startTime",startTime);
        params.put("departmentRedis",departmentRedis);
        threadPoolTool.excuteTask(transactionManager,taskInstanceId,openEmpSimples,threadCount,params,false, InitUserTask.class);
    }



    /**
     * 导入离职员工信息
     * @param companyId 公司id
     */
    @Override
    public void initUserInfo(List<UserExcelDTO> userExcelDTOList ,String companyId ,String taskInstanceId  ,List<Department> departmentRedis){
        String  userIds = getUserListString(userExcelDTOList);
        List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDimissionInfoVo> empDimissionInfoVos =  dingDingUserService.userOfflineById(userIds,companyId);
        List<String> deptIdList = new ArrayList<>();
        for (UserExcelDTO userExcelDTO :userExcelDTOList){

            try {
                OapiSmartworkHrmEmployeeListdimissionResponse.EmpDimissionInfoVo empDimissionInfoVo  = empDimissionInfoVos.stream().filter(EmpDimissionInfoVo -> EmpDimissionInfoVo.getUserid().equals(userExcelDTO.getUserId())).findAny().orElse(null);
                User user = new User();
                List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO> vos = null;
                if(empDimissionInfoVo != null){
                    if(empDimissionInfoVo.getLastWorkDay() != null){
                        user.setLastWorkDay(empDimissionInfoVo.getLastWorkDay());
                    }
                    if(empDimissionInfoVo.getDeptList() != null && empDimissionInfoVo.getDeptList().size()>0){
                        vos = empDimissionInfoVo.getDeptList();
                    }
                }
                String id = UUIDS.getID();
                user.setId(id);
                user.setStatus(1);
                user.setCompanyId(companyId);
                user.setCreateTime(System.currentTimeMillis());
                user.setDeleted(0);
                user.setMobile(userExcelDTO.getPhone());
                user.setUserId(userExcelDTO.getUserId());
                user.setName(userExcelDTO.getName());
                user.setTaskInstanceId(taskInstanceId);
                userMapper.insert(user);

                if(vos == null || vos.size() == 0){
                    continue;
                }
                for (OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO empDeptVO :vos ) {
                    if(empDeptVO == null){
                        continue;
                    }
                    if(empDeptVO.getDeptId() == null){
                        continue;
                    }
                    String deptId = String.valueOf(empDeptVO.getDeptId());
                    deptIdList.add(deptId);
                    UserDepartment userDepartment = new UserDepartment();
                    userDepartment.setCreateTime(System.currentTimeMillis());
                    userDepartment.setDeleted(0);
                    userDepartment.setId(UUIDS.getID());
                    userDepartment.setDeptId(deptId);
                    userDepartment.setTaskInstanceId(taskInstanceId);
                    userDepartment.setUId(id);
                    userDepartmentMapper.insert(userDepartment);
                    //查询部门是否已存在
                    if(StringUtils.isNotBlank(empDeptVO.getDeptPath())){
                        Department deptRedis = departmentRedis.stream().filter(Department -> Department.getId().equals(deptId)).findAny().orElse(null);
                        //不存在进行缓存
                        if(deptRedis == null){
                            String  deptName = empDeptVO.getDeptPath().substring(empDeptVO.getDeptPath().lastIndexOf("-")+1);
                            OapiDepartmentGetResponse oapiDepartmentGetResponse = null;
                            try {
                                oapiDepartmentGetResponse = dingDingReportService.deptInfoById(deptId,companyId);
                            } catch (Exception e) {
                            }
                            //无法获取父级部门默认0
                            String pid = "0";
                            if(oapiDepartmentGetResponse != null && oapiDepartmentGetResponse.getParentid() != null){
                                pid = String.valueOf(oapiDepartmentGetResponse.getParentid());
                            }
                            Department tmp = new Department();
                            tmp.setId(deptId);
                            tmp.setName(deptName);
                            tmp.setLevel(5L);
                            tmp.setParentid(pid);

                            Map m = new ConcurrentHashMap();
                            m.put("id", deptId);
                            m.put("parentid", pid);
                            m.put("name", deptName);
                            m.put("level", 5L);
                            //存储在list缓存中
                            redisUtil.sSetAndTime(companyId, 24 * 60 * 60, tmp);
                            //存储部门信息
                            redisUtil.hmset(deptId , m, 24 * 60 * 60);
                        }

                    }
                }

            } catch (Exception e) {
            }
        }
        if(deptIdList!= null && deptIdList.size() >0){
            String deptListString = (String) redisUtil.get(companyId + "LeavingUserDept");
            List<String> deptList = new ArrayList<>();
            if(StringUtils.isNotBlank(deptListString)){
                deptList = Arrays.asList(deptListString.split(",")).stream().map(s -> String.format(s.trim())).collect(Collectors.toList());
            }
            if(deptList!=null && deptList.size()>0){
                deptList.addAll(deptIdList);
            }else{
                deptList = deptIdList;
            }
            String stringDept = String.join(",",deptList);
            redisUtil.set(companyId + "LeavingUserDept" ,stringDept , 1 * 60 * 60);
        }
    }

    /**
     * 离职用户部门信息持久化
     * @param companyId
     */
    @Override
    public void  insertDeptId(String companyId){
        String taskInstanceId = UUIDS.getID();
        //数据库查询公司所有部门信息
        QueryWrapper<TDepartment> tDepartmentQueryWrapper = new QueryWrapper<>();
        tDepartmentQueryWrapper.lambda().ge(TDepartment::getDeleted, 0);
        tDepartmentQueryWrapper.lambda().ge(TDepartment::getCompanyId, companyId);
        List <TDepartment> departmentList = itDepartmentService.list(tDepartmentQueryWrapper);
        String deptListString = (String) redisUtil.get(companyId + "LeavingUserDept");
        if(StringUtils.isNotBlank(deptListString)){
            List<String> deptLists = Arrays.asList(deptListString.split(",")).stream().map(s -> String.format(s.trim())).collect(Collectors.toList());
            //缓存离职用户部门自身去重
            List<String> deptList = deptLists.stream().distinct().collect(Collectors.toList());
            //数据库部门按部门id分组
            List<String> deptListData = departmentList.stream().map(TDepartment::getDeptId).collect(Collectors.toList());
            //缓存离职用户部门 和 数据库部门按部门 取差集
            List<String> reduceDeptList = deptList.stream().filter(item -> !deptListData.contains(item)).collect(Collectors.toList());
            //得到需要插入的部门
            for(String deptId : reduceDeptList){
                Department department = departmentService.getDepartment(deptId);
                if(department!=null){
                    //持久化
                    TDepartment tDepartment = new TDepartment();
                    tDepartment.setDeptId(deptId);
                    tDepartment.setDeptName(department.getName());
                    tDepartment.setTaskInstanceId(taskInstanceId);
                    tDepartment.setCompanyId(companyId);
                    tDepartment.setCreateTime(System.currentTimeMillis());
                    tDepartment.setDeleted(0);
                    tDepartment.setLevel(5);
                    tDepartment.setParentId(department.getParentid());
                    tDepartment.setId(UUIDS.getID());
                    itDepartmentService.save(tDepartment);
                }
            }
            redisUtil.del(companyId + "LeavingUserDept");
        }
    }
    /**
     * 获取离职用户字符串集合
     * @param
     * @return 用户idString
     */
    private String getUserListString(List<UserExcelDTO> userExcelDTOList){
        StringBuilder userIds = new StringBuilder();
        for (UserExcelDTO userExcelDTO : userExcelDTOList ){
            if(StringUtils.isNotBlank(userExcelDTO.getUserId())){
                if(userIds.length()==0){
                    userIds.append(userExcelDTO.getUserId());
                }else{
                    userIds.append(","+userExcelDTO.getUserId());
                }
            }
        }
        return userIds.toString();
    }
    /**
     * 获取离职用户部门id字符串集合
     * @param
     * @return 用户idString
     */
    private String getDeptIdListString(List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO> empDeptVOList){
        if(empDeptVOList==null||empDeptVOList.size()==0){
            return null;
        }
        StringBuilder deptIds = new StringBuilder();
        for (OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO empDeptVO : empDeptVOList ){
            if(deptIds.length()==0){
                deptIds.append(empDeptVO.getDeptId());
            }else{
                deptIds.append(","+empDeptVO.getDeptId());
            }
        }
        return deptIds.toString();
    }
}