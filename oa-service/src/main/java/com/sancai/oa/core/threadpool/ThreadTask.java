package com.sancai.oa.core.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;

/**
 * 多线程任务
 * @Author chenm
 * @create 2019/8/27 14:43
 */
@Slf4j
public abstract class ThreadTask implements Runnable {

    /**
     * 监控子任务的执行
     */
    private CountDownLatch childMonitor;
    /**
     * 监控主线程
     */
    private CountDownLatch mainMonitor;
    /**
     * 存储线程的返回结果
     */
    private BlockingDeque<Boolean> resultList;
    /**
     * 回滚类
     */
    private RollBack rollback;


    private Map<String,Object> params;

    protected Object obj;
    protected DataSourceTransactionManager transactionManager;
    protected TransactionStatus status;

    public ThreadTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj,Map<String,Object> params) {
        this.childMonitor = childCountDown;
        this.mainMonitor = mainCountDown;
        this.resultList = result;
        this.rollback = rollback;
        this.transactionManager = transactionManager;
        this.obj = obj;
        this.params = params;
        initParam();
    }

    /**
     * 事务回滚
     */
    private void rollBack() {
        System.out.println(Thread.currentThread().getName()+"开始回滚");
        transactionManager.rollback(status);
    }

    /**
     * 事务提交
     */
    private void submit() {
        System.out.println(Thread.currentThread().getName()+"提交事务");
        transactionManager.commit(status);
    }

    protected Object getParam(String key){
        return params.get(key);
    }

    public abstract void initParam();

    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     * @return
     */
    public abstract boolean processTask();

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+"子线程开始执行任务");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = transactionManager.getTransaction(def);

        Boolean result = processTask();
        //向队列中添加处理结果
        resultList.add(result);
        //等待其他的子线程执行完成，一起执行主线程的判断逻辑
        childMonitor.countDown();
        try {
            //等待主线程的判断逻辑执行完，执行下面的是否回滚逻辑
            mainMonitor.await();
        } catch (Exception e) {
           log.error(e.getMessage());
        }

        System.out.println(Thread.currentThread().getName()+"子线程执行剩下的任务");
        //需要回滚
        if (rollback.isNeedRoolBack()) {
            rollBack();
        }else{
            //事务提交
            submit();
        }
    }
}