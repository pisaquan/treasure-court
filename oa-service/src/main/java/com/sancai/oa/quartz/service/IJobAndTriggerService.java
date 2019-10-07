package com.sancai.oa.quartz.service;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.quartz.entity.BaseJob;
import com.sancai.oa.quartz.entity.JobAndTrigger;
import org.quartz.SchedulerException;

/**
 * @author wangyl
 * @date 2019/7/30 13:08
 */
public interface IJobAndTriggerService {
    public PageInfo<JobAndTrigger> queryJob(int pageNum, int pageSize);
    public void addJob(BaseJob job) throws SchedulerException;
    public void pauseJob(String jobName, String jobGroupName) throws SchedulerException;
    public void resumeJob(String jobName, String jobGroupName) throws SchedulerException;
    public void rescheduleJob(String jobName, String jobGroupName, String cronExpression, String description) throws SchedulerException;
    public void deleteJob(String jobName, String jobGroupName) throws SchedulerException;
}
