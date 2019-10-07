package com.sancai.oa.clockin.threadpool;

import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.entity.ClockinRecordGrapDTO;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadResult;
import com.sancai.oa.core.threadpool.ThreadTaskCall;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.user.entity.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 抓取打卡的任务
 * @Author chenm
 * @create 2019/8/27 14:43
 */
@Slf4j
public class ClockinTask extends ThreadTaskCall {

    private String companyId;
    private  List<List<UserDTO>> userList;
    private String taskInstanceId;
    private ClockinService clockinService;
    private long start;
    private long end;
    private int batch;
    private long startTime;

    public ClockinTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.userList = (List<List<UserDTO>>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.start = (long) getParam("start");
        this.end = (long) getParam("end");
        this.clockinService = (ClockinService) getParam("clockinService");
        this.startTime = (long) getParam("startTime");

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

            List<ClockinRecordGrapDTO> list = new ArrayList<>();
            for(List<UserDTO> users : userList){
                group ++;
                Map<String, UserDTO> userMap = users.stream().collect(Collectors.toMap(UserDTO::getUserId, a -> a,(k1, k2)->k1));
                System.out.println("--第"+batch+"批,第"+group+"组 用户（"+userMap.size()+"人）开始抓取考勤");
                TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批,第"+group+"组 用户（"+userMap.size()+"人）开始抓取考勤");
                Map<String, Map<Long,List<ClockinPoint>>> clockinMap = clockinService.graspClockin(taskInstanceId,companyId,start,end,userMap);

                ClockinRecordGrapDTO crg = new ClockinRecordGrapDTO();
                crg.setUserMap(userMap);
                crg.setClockinMap(clockinMap);
                list.add(crg);

                long endTime = System.currentTimeMillis();
                int sec = (int) ((endTime-startTime)/1000);
                System.out.println("--第"+batch+"批,第"+group+"组 用户抓取考勤完成,第"+sec+"秒");
                TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批,第"+group+"组 用户抓取考勤完成,第"+sec+"秒");
            }
            threadResult.setData(list);
            threadResult.setFlag(true);
        }catch (Exception e){
            for(StackTraceElement st : e.getStackTrace()){
                log.error(st.toString());
            }
            TaskMessage.addMessage(taskInstanceId,"--第"+batch+"批用户抓取时异常："+e.getMessage());
            threadResult.setFlag(false);
            threadResult.setE(e);
        }
        return threadResult;
    }


}