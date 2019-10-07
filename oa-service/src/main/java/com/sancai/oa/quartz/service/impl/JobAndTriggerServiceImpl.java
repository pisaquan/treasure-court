package com.sancai.oa.quartz.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sancai.oa.quartz.entity.BaseJob;
import com.sancai.oa.quartz.entity.JobAndTrigger;
import com.sancai.oa.quartz.mapper.JobAndTriggerMapper;
import com.sancai.oa.quartz.service.IJobAndTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wangyl
 * @date 2019/7/30 13:08
 */
@Slf4j
@Service
public class JobAndTriggerServiceImpl implements IJobAndTriggerService {

    private final JobAndTriggerMapper jobAndTriggerMapper;
    /**
     * Scheduler代表一个调度容器,一个调度容器可以注册多个JobDetail和Trigger.当Trigger和JobDetail组合,就可以被Scheduler容器调度了
     */
    private final Scheduler scheduler;

    @Autowired
    public JobAndTriggerServiceImpl(JobAndTriggerMapper jobAndTriggerMapper, Scheduler scheduler) {
        this.jobAndTriggerMapper = jobAndTriggerMapper;
        this.scheduler = scheduler;
    }

    /**
     * 查询任务列表
     *
     * @param pageNum  页号
     * @param pageSize 每页大小
     * @return
     */
    @Override
    public PageInfo<JobAndTrigger> queryJob(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<JobAndTrigger> list = jobAndTriggerMapper.getJobAndTriggerDetails();
        PageInfo<JobAndTrigger> page = new PageInfo<JobAndTrigger>(list);
        return page;
    }

    /**
     * 添加一个任务
     *
     * @param job 任务
     * @throws SchedulerException
     */
    @Override
    public void addJob(BaseJob job) throws SchedulerException {

        /** 创建JobDetail实例,绑定Job实现类
         * JobDetail 表示一个具体的可执行的调度程序,job是这个可执行调度程序所要执行的内容
         * 另外JobDetail还包含了这个任务调度的方案和策略**/
        // 指明job的名称，所在组的名称，以及绑定job类
        JobDetail jobDetail = JobBuilder.newJob(job.getBeanClass())
                .withIdentity(job.getJobKey())
                .withDescription(job.getDescription())
                .usingJobData(job.getDataMap())
                .usingJobData("class", job.getSpringBean())
                .usingJobData("group", job.getJobGroup())
                .usingJobData("companyId", job.getCompanyId())
                .usingJobData("taskTemplateId", job.getTaskTemplateId())
                .usingJobData("instanceName", job.getJobName() + "," + job.getCompanyName())
                .build();

        /**
         * Trigger代表一个调度参数的配置,什么时候去调度
         */
        //定义调度触发规则, 使用cronTrigger规则
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .usingJobData("method", job.getMethodName())
                .startNow()
                .build();
        // 根据jobname和jobgroup获取任务trigger，有的话则任务已存在不再添加任务，否则添加
        JobDetail jobDetail1Tmp = scheduler.getJobDetail(JobKey.jobKey(job.getJobName(), job.getJobGroup()));

        if (null != jobDetail1Tmp && null != jobDetail1Tmp.getJobClass()) {

        } else {
            //将任务和触发器注册到任务调度中去
            scheduler.scheduleJob(jobDetail, trigger);
        }


        //判断调度器是否启动
        if (!scheduler.isStarted()) {
            scheduler.start();
        }
        log.info(String.format("定时任务:%s.%s-已添加到调度器!", job.getJobGroup(), job.getJobName()));
    }

    /**
     * 根据任务名和任务组名来暂停一个任务
     *
     * @param jobName      job名称
     * @param jobGroupName job组名称
     * @throws SchedulerException
     */
    @Override
    public void pauseJob(String jobName, String jobGroupName) throws SchedulerException {
        scheduler.pauseJob(JobKey.jobKey(jobName, jobGroupName));
    }

    /**
     * 根据任务名和任务组名来恢复一个任务
     *
     * @param jobName
     * @param jobGroupName
     * @throws SchedulerException
     */
    @Override
    public void resumeJob(String jobName, String jobGroupName) throws SchedulerException {
        scheduler.resumeJob(JobKey.jobKey(jobName, jobGroupName));
    }

    /**
     * @param jobName
     * @param jobGroupName
     * @param cronExpression
     * @param description
     * @throws SchedulerException
     */
    @Override
    public void rescheduleJob(String jobName, String jobGroupName, String cronExpression, String description) throws SchedulerException {

        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

        // 按新的cronExpression表达式重新构建trigger
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withDescription(description).withSchedule(scheduleBuilder).build();
//        if(trigger == null){
//            throw new PowerYourselfException(ResponseCode.JOB_HAS_REMINDED);
//        }
        // 按新的trigger重新设置job执行
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    /**
     * 根据任务名和任务组名来删除一个任务
     *
     * @param jobName
     * @param jobGroupName
     * @throws SchedulerException
     */
    @Override
    public void deleteJob(String jobName, String jobGroupName) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroupName);
        //先暂停
        scheduler.pauseTrigger(triggerKey);
        //取消调度
        scheduler.unscheduleJob(triggerKey);
        //删除任务
        boolean flag = scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));
    }

}
