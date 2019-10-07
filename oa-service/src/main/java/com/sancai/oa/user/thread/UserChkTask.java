package com.sancai.oa.user.thread;

import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.core.threadpool.ThreadTaskCall;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.quartz.util.TaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

/**
 * 抓取离职用户id任务
 * @Author wangyal
 * @create 2019/8/27 14:43
 */
@Slf4j
public class UserChkTask extends ThreadTaskCall<List<String>> {

    private String companyId;
    private String taskInstanceId;
    private DingDingUserService dingDingUserService;
    private int batch;
    private long startTime;
    private long page;

    public UserChkTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<ThreadResult> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {

        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.dingDingUserService = (DingDingUserService) getParam("dingDingUserService");
        this.page = (long)getParam("page");
        this.startTime = (long) getParam("startTime");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚 <?>
     * @return
     */
    @Override
    public ThreadResult processTask(){
        ThreadResult threadResult = new ThreadResult();
        List<String> res = new ArrayList<>();
        try{
            boolean isEnd = false;
            if(batch==5){
                isEnd = true;
            }
            res = dingDingUserService.getOfflineUserByCompanyId(companyId,batch,page,isEnd);
            long endTime = System.currentTimeMillis();
            int sec = (int) ((endTime-startTime)/1000);
            System.out.println("--第"+batch+"批离职用户id抓取完成,第"+sec+"秒 ");
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批离职用户id抓取完成,第"+sec+"秒");
            threadResult.setData(res);
            threadResult.setFlag(true);
        }catch (Exception e){
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批离职用户id抓取时异常："+e.getMessage());
            threadResult.setFlag(false);
            threadResult.setE(e);
        }

        return threadResult;
    }


}