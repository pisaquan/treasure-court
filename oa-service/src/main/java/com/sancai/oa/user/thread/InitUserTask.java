package com.sancai.oa.user.thread;

import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.UserExcelDTO;
import com.sancai.oa.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

/**
 * 初始化离职用户的任务
 * @Author fans
 * @create 2019/9/02
 */
@Slf4j
public class InitUserTask extends ThreadTask {

    private String companyId;
    private  List<List<UserExcelDTO>> userList;
    private String taskInstanceId;
    private IUserService UserService;
    private int batch;
    private long startTime;
    private List<Department> departmentRedis;
    public InitUserTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.userList = (List<List<UserExcelDTO>>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.UserService = (IUserService) getParam("UserService");
        this.startTime = (long) getParam("startTime");
        this.departmentRedis = ( List <Department>) getParam("departmentRedis");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     * @return
     */
    @Override
    public boolean processTask(){
        try{
            int group = 0;
            for(List<UserExcelDTO> users : userList){
                group ++;
                if(users == null || users.size() ==0){
                   continue;
                }
                System.out.println("--第"+batch+"批,第"+group+"组");
                TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批,第"+group+"组");
                UserService.initUserInfo(users,companyId,taskInstanceId ,departmentRedis);
                long endTime = System.currentTimeMillis();
                int sec = (int) ((endTime-startTime)/1000);
                System.out.println("--第"+batch+"批,第"+group+"组 用户初始化完成,第"+sec+"秒");
                TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批,第"+group+"组初始化完成,第"+sec+"秒");
            }
            return true;
        }catch (Exception e){
            System.out.println("--第"+batch+"批用户初始化异常："+e.getMessage());
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批用户初始化异常："+e.getMessage());
            return false;
        }
    }


}