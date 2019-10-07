package com.sancai.oa.core.threadpool;

import com.sancai.oa.clockin.exception.EnumClockinError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池
 * @Author chenm
 * @create 2019/8/27 14:06
 */
@Slf4j
@Service
public class ThreadPoolTool<T> {
    @Autowired
    private ITaskInstanceService taskInstanceService;

    /**
     * 多线程任务
     * @param transactionManager
     * @param taskInstanceId
     * @param data
     * @param threadCount
     * @param params
     * @param lastTask 最后一个任务
     * @param clazz
     */
    public void excuteTask(DataSourceTransactionManager transactionManager, String taskInstanceId, List data, int threadCount, Map<String,Object> params, boolean lastTask, Class clazz){
        if(data == null || data.size() == 0){
            return;
        }
        int batch = 0;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        //监控子线程的任务执行
        CountDownLatch childMonitor = new CountDownLatch(threadCount);
        //监控主线程，是否需要回滚
        CountDownLatch mainMonitor = new CountDownLatch(1);
        //存储任务的返回结果，返回true表示不需要回滚，反之，则回滚
        BlockingDeque<Boolean> results = new LinkedBlockingDeque<Boolean>(threadCount);
        RollBack rollback = new RollBack(false);

        try {
            LinkedBlockingQueue<List> queue = splitQueue(data,threadCount);
            while(true){
                List list =  queue.poll();
                if(list == null){
                    break;
                }
                batch++;
                params.put("batch",batch);
                Constructor constructor = clazz.getConstructor(new Class[]{CountDownLatch.class,CountDownLatch.class, BlockingDeque.class,RollBack.class, DataSourceTransactionManager.class,Object.class,Map.class});

                ThreadTask task = (ThreadTask) constructor.newInstance(childMonitor, mainMonitor, results, rollback,transactionManager,list,params);
                executor.execute(task);
            }

            //监测子线程的执行
            childMonitor.await();
            System.out.println("主线程开始执行任务");

            //根据返回结果来确定是否回滚
            for (int i = 0; i < threadCount; i++) {
                Boolean result = results.take();
                if (!result) {
                    rollback.setNeedRoolBack(true);

                }
            }

            mainMonitor.countDown();

        } catch (Exception e) {
            log.error(e.getMessage());
        }finally {
            executor.shutdown();
        }

        if(rollback.isNeedRoolBack()){
            System.out.println("Fail");
            TaskMessage.addMessage(taskInstanceId,"--任务异常，需要回滚!!!!!!!");

            //最后一个任务执行完
            TaskMessage.finishMessage(taskInstanceId);
            TaskInstance failedInstance = taskInstanceService.getById(taskInstanceId);

            failedInstance.setLastExcuteTime(System.currentTimeMillis());
            failedInstance.setStatus(TimedTaskStatusEnum.FAILURE.getKey());
            failedInstance.setFailReason("--任务异常，需要回滚!!!!!!!");

            taskInstanceService.updateById(failedInstance);
            throw new OaClockinlException(EnumClockinError.TASK_ERROR);
        }else{
            System.out.println("Success");
            if(lastTask){
                //最后一个任务执行完
                TaskMessage.finishMessage(taskInstanceId);

                TaskInstance successInstance = taskInstanceService.getById(taskInstanceId);
                successInstance.setLastExcuteTime(System.currentTimeMillis());
                successInstance.setStatus(TimedTaskStatusEnum.SUCCESS.getKey());
                successInstance.setSuccessTime(System.currentTimeMillis());
                // 自动重试后成功，需要清空失败原因
                successInstance.setFailReason(" ");
                taskInstanceService.updateById(successInstance);
            }
        }
    }

    /**
     * 多线程任务
     * @param transactionManager
     * @param taskInstanceId
     * @param data
     * @param threadCount
     * @param params
     * @param lastTask 最后一个任务
     * @param clazz
     */
    public ThreadResult excuteTaskFuture(DataSourceTransactionManager transactionManager, String taskInstanceId, List data, int threadCount, Map<String,Object> params, boolean lastTask, Class clazz){
        ThreadResult threadResult = new ThreadResult();
        threadResult.setFlag(true);
        if(data == null || data.size() == 0){
            return threadResult;
        }
        int batch = 0;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future <T>> res = new ArrayList<Future <T>>();

        //监控子线程的任务执行
        CountDownLatch childMonitor = new CountDownLatch(threadCount);
        //监控主线程，是否需要回滚
        CountDownLatch mainMonitor = new CountDownLatch(1);
        //存储任务的返回结果，返回true表示不需要回滚，反之，则回滚
        BlockingDeque<ThreadResult> results = new LinkedBlockingDeque<ThreadResult>(threadCount);
        RollBack rollback = new RollBack(false);

        try {
            LinkedBlockingQueue<List> queue = splitQueue(data,threadCount);
            while(true){
                List list =  queue.poll();
                if(list == null){
                    break;
                }
                batch++;
                params.put("batch",batch);
                Constructor constructor = clazz.getConstructor(new Class[]{CountDownLatch.class,CountDownLatch.class, BlockingDeque.class,RollBack.class, DataSourceTransactionManager.class,Object.class,Map.class});

                ThreadTaskCall task = (ThreadTaskCall) constructor.newInstance(childMonitor, mainMonitor, results, rollback,transactionManager,list,params);
                res.add(executor.submit(task));
            }

            //监测子线程的执行
            childMonitor.await();
            System.out.println("主线程开始执行任务");

            //根据返回结果来确定是否回滚
            for (int i = 0; i < threadCount; i++) {
                ThreadResult result = results.take();
                if (!result.getFlag()) {
                    threadResult.setFlag(false);
                    threadResult.setE(result.getE());
                    rollback.setNeedRoolBack(true);
                    threadResult.setFlag(false);
                    threadResult.setData(null);
                }
            }

            mainMonitor.countDown();
        } catch (Exception e) {
            log.error(e.getMessage());
        }finally {
            executor.shutdown();

        }

        if(rollback.isNeedRoolBack()){
            System.out.println("Fail ");
            TaskMessage.addMessage(taskInstanceId,"--任务异常，需要回滚!!!!!!!");

            //最后一个任务执行完
            TaskMessage.finishMessage(taskInstanceId);
            TaskInstance failedInstance = taskInstanceService.getById(taskInstanceId);
            failedInstance.setLastExcuteTime(System.currentTimeMillis());
            failedInstance.setStatus(TimedTaskStatusEnum.FAILURE.getKey());
            failedInstance.setFailReason("--任务异常， 需要回滚!!!!!!!");
            taskInstanceService.updateById(failedInstance);

        }else{
            System.out.println("Success");
            if(lastTask){
                //最后一个任务执行完
                TaskMessage.finishMessage(taskInstanceId);
                TaskInstance successInstance = taskInstanceService.getById(taskInstanceId);
                successInstance.setStatus(TimedTaskStatusEnum.SUCCESS.getKey());
                successInstance.setLastExcuteTime(System.currentTimeMillis());
                successInstance.setSuccessTime(System.currentTimeMillis());
                // 自动重试后成功，需要清空失败原因
                successInstance.setFailReason(" ");
                taskInstanceService.updateById(successInstance);
            }
        }
        threadResult.setData(res);
        return threadResult;

    }
    /**
     *  队列拆分
     * @param data
     * @param threadCount
     * @return
     */
    private LinkedBlockingQueue<List<Object>> splitQueue( List<Object> data,int threadCount){
        LinkedBlockingQueue<List<Object>> queueBatch = new LinkedBlockingQueue();
        int total = data.size();
        int oneSize = total/threadCount ;
        int start = 0;
        int end = 0;

        for (int i = 0; i <threadCount ; i++) {
            start = i * oneSize;
            end = (i+1)*oneSize;
            if (i<threadCount-1){
                queueBatch.add(data.subList(start,end));
            }else {
                queueBatch.add(data.subList(start,data.size()));
            }
        }
        return queueBatch;
    }
}
