package com.sancai.oa.department.threadpool;

import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.core.threadpool.ThreadTaskCall;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.mapper.UserDepartmentMapper;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.ITUserDepartmentService;
import com.sancai.oa.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 用户新增任务
 * @Author chenm
 * @create 2019/8/27 14:43
 */
@Slf4j
public class UserTask extends ThreadTaskCall {

    private String companyId;
    private  List<String> useridList;
    private String taskInstanceId;
    private int batch;
    private long startTime;
    private DingDingUserService dingDingUserService;
    private Map<String,String> userDeptBelong;
    private IUserService userService;
    private ITUserDepartmentService iTUserDepartmentService;
    private UserDepartmentMapper userDepartmentMapper;
    private UserMapper userMapper;

    public UserTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.useridList = (List<String>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.dingDingUserService = (DingDingUserService) getParam("dingDingUserService");
        this.userDepartmentMapper = (UserDepartmentMapper) getParam("userDepartmentMapper");
        this.iTUserDepartmentService = (ITUserDepartmentService) getParam("iTUserDepartmentService");
        this.userMapper = (UserMapper) getParam("userMapper");
        this.userService = (IUserService) getParam("userService");
        this.startTime = (long) getParam("startTime");
        this.userDeptBelong = (Map<String,String>) getParam("userDeptBelong");
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
            if(this.useridList == null){
                threadResult.setFlag(true);
                return threadResult;
            }

            List<User> addUserList = new ArrayList<User>();
            List<UserDepartment> addUserDepartmentList = new ArrayList<UserDepartment>();

            for(String userId:useridList){
                User u = dingDingUserService.getUser(companyId, userId);
                u.setTaskInstanceId(taskInstanceId);
                u.setStatus(0);
                addUserList.add(u);
//                userService.save(u);
                String tmpDept = userDeptBelong.get(userId)+"";
                if(StringUtils.isEmpty(tmpDept)){
                    continue;
                }
                String[] tmpDpt = tmpDept.split(",");
                if(null==tmpDpt || tmpDpt.length==0){
                    continue;
                }
                for(String tmpD: tmpDpt){
                    UserDepartment userDepartment = new UserDepartment();
                    userDepartment.setUId(u.getId());
                    userDepartment.setDeptId(tmpD);
                    userDepartment.setId(UUIDS.getID());
                    userDepartment.setDeleted(0);
                    userDepartment.setCreateTime(System.currentTimeMillis());
                    userDepartment.setTaskInstanceId(taskInstanceId);
//                    iTUserDepartmentService.save(userDepartment);
                    addUserDepartmentList.add(userDepartment);
                }
            }
            //批量新增

            if(null!=addUserList&&addUserList.size()>0){
                userMapper.batchSave(addUserList);
                addUserList.clear();
            }

            if(null!=addUserDepartmentList&&addUserDepartmentList.size()>0){
                userDepartmentMapper.batchSave(addUserDepartmentList);
                addUserDepartmentList.clear();
            }
            addUserList.clear();
            System.out.println("--第"+batch+"批用户（"+useridList.size()+"人）开始取用户信息");

            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批用户（"+useridList.size()+"人）开始存入数据库");

            long endTime = System.currentTimeMillis();
            int sec = (int) ((endTime-startTime)/1000);
            System.out.println("--第"+batch+"批用户抓取完成,第"+sec+"秒");
            this.batch++;
            threadResult.setFlag(true);
        }catch (Exception e){
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批用户新增时异常："+e.getMessage());
            threadResult.setFlag(false);
            threadResult.setE(e);
        }
        return threadResult;
    }


}