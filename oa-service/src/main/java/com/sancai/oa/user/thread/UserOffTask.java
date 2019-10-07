package com.sancai.oa.user.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.dingtalk.api.response.OapiSmartworkHrmEmployeeListdimissionResponse;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.core.threadpool.ThreadTaskCall;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.department.mapper.TDepartmentMapper;
import com.sancai.oa.department.service.ITDepartmentService;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.UUIDS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 离职用户更新任务
 * @Author wangyl
 * @create 2019/8/27 14:43
 */
@Slf4j
public class UserOffTask extends ThreadTaskCall {

    private String companyId;
    private List<User> usersList;
    private String taskInstanceId;
    private int batch;
    private long startTime;
    private DingDingUserService dingDingUserService;
    private IUserService userService;
    private ITDepartmentService tDepartmentService;
    private RedisUtil redisUtil;
    private DingDingReportService dingDingReportService;
    private IExamineService examineService;

    public UserOffTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.usersList = (List<User>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.dingDingUserService = (DingDingUserService) getParam("dingDingUserService");
        this.userService = (IUserService) getParam("userService");
        this.startTime = (long) getParam("startTime");
        this.tDepartmentService = (ITDepartmentService) getParam("tDepartmentService");
        this.redisUtil = (RedisUtil) getParam("redisUtil");
        this.dingDingReportService = (DingDingReportService)getParam("dingDingReportService");
        this.examineService = (IExamineService)getParam("examineService");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     * @return
     */
    @Override
    public ThreadResult processTask(){
        ThreadResult threadResult = new ThreadResult();
        try{
            int group = 0;
            if(this.usersList == null||usersList.size()==0){
                threadResult.setFlag(true);
                return threadResult;
            }

            //存储不重复的离职部门信息
            Set<String> deptSet = new HashSet<>();
            Set<String> deptIdAndName = new HashSet<>();
            List<List<User>> userListBatch  = ListUtils.fixedGrouping(usersList,50);
            for(List<User> userListTmp : userListBatch){
                List<User> updateUserList = new ArrayList<User>();
                Map<String,Object> map = new HashMap<String,Object>();
                StringBuffer userIds = new StringBuffer();
                for(User user:userListTmp){
                    userIds.append(user.getUserId()+",");
                }
                if(StringUtils.isEmpty(userIds.toString())){
                    threadResult.setFlag(false);
                }
                //批量提取离职用户信息
                List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDimissionInfoVo> userInfoList = dingDingUserService.userOfflineById(userIds.toString(),companyId);
                // 批量处理数据，获取离职日期信息，封装为map键值对
                for(OapiSmartworkHrmEmployeeListdimissionResponse.EmpDimissionInfoVo userTmp :userInfoList){
                    map.put(userTmp.getUserid(),userTmp.getLastWorkDay());
                    List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO> deptList = userTmp.getDeptList();
                    for (OapiSmartworkHrmEmployeeListdimissionResponse.EmpDeptVO empDeptVO : deptList) {
                        //获取部门路径，截取最后一个部门名称
                        int length = empDeptVO.getDeptPath().split("-").length;
                        String deptName = empDeptVO.getDeptPath().split("-")[length - 1];
                        Long deptId = empDeptVO.getDeptId();
                        //map中存储 离职前主部门id--离职前主部门名称
                        map.put(deptId+"",deptName);
                        //添加不重复的部门id
                        deptSet.add(deptId+"");
                        deptIdAndName.add(deptId+"-"+deptName);
                    }
                }

                //离职部门信息插入redis 中
                //判断redis中是否有该部门，如果没有，将部门Id,name插入
                Set<Object> objects = redisUtil.sGet(companyId);
                Set<Department> departmentSet = new HashSet<Department>();
                for (Object o : objects) {
                    Map m = (Map) o;
                    Department department = new Department();
                    department.setName(m.get("name") + "");
                    department.setId(m.get("id") + "");
                    departmentSet.add(department);
                }
                for (String deptId : deptSet) {
                    Department department = departmentSet.stream().filter(Department -> Department.getId().equals(deptId)).findAny().orElse(null);
                    if(department == null){
                        department.setId(deptId+"");
                        department.setName(map.get(deptId).toString());
                        //查询父部门信息
                        OapiDepartmentGetResponse oapiDepartmentGetResponse = dingDingReportService.deptInfoById(deptId, companyId);
                        Long parentid = oapiDepartmentGetResponse.getParentid();
                        if(parentid != null){
                            department.setParentid(parentid+"");
                        }else {
                            department.setParentid("0");
                        }
                        department.setLevel(5L);
                        //将不重复的部门存入redis中
                        redisUtil.sSetAndTime(companyId, 24 * 60 * 60, department);
                    }
                }

                //批量更新
                for(User userUpdate:userListTmp){
                    //Todo 更新新增用户的考勤组
//                    examineService.saveUserAttendance(userUpdate.getUserId(),companyId);
                    //查询user表中是否已存在离职信息，如存在，删除离职信息，设置修改时间
                    QueryWrapper<User> wrapperUser1 = new QueryWrapper();
                    wrapperUser1.lambda().eq(User::getDeleted, 0L).eq(User::getStatus,1).eq(User::getUserId,userUpdate.getUserId()).eq(User::getCompanyId,companyId);
                    List<User> list = userService.list(wrapperUser1);
                    if(!CollectionUtils.isEmpty(list)){
                        User user = list.get(0);
                        if(user != null){
                            user.setDeleted(1);
                            user.setModifyTime(System.currentTimeMillis());
                            userService.updateById(user);
                        }
                    }
                    //更新在职信息，修改为离职状态
                    userUpdate.setStatus(1);
                    userUpdate.setLastWorkDay((Long)map.get(userUpdate.getUserId()));
                    userUpdate.setTaskInstanceId(taskInstanceId);
                    userUpdate.setModifyTime(System.currentTimeMillis());
//                    updateUserList.add(userUpdate);
                    userService.updateById(userUpdate);

                }
//                userService.updateBatchById(updateUserList);
                updateUserList.clear();
            }

            System.out.println("--第"+batch+"批用户（"+usersList.size()+"人）开始更新离职用户信息");

            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批用户（"+usersList.size()+"人）开始存入数据库");

            long endTime = System.currentTimeMillis();
            int sec = (int) ((endTime-startTime)/1000);
            System.out.println("--第"+batch+"批更新离职完成,第"+sec+"秒");
            this.batch++;
            threadResult.setFlag(true);
            threadResult.setData(new ArrayList<>(deptIdAndName));

        }catch (Exception e){
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批更新离职用户时异常："+e.getMessage());
            threadResult.setFlag(false);
            threadResult.setE(e);
        }
        return threadResult;
    }

}