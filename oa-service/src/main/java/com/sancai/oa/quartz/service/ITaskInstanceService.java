package com.sancai.oa.quartz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.typestatus.enums.TimedTaskKeyEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务实例 服务类
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
public interface ITaskInstanceService extends IService<TaskInstance> {

    /**
     * 一个公司的一个模板的最后一个成功的任务
     *
     * @param templateId
     * @param companyId
     * @return
     */
    public TaskInstance getLastSuccessTaskInstance(String templateId, String companyId);

    /**
     * 一个公司的一个模板的指定时间之前的最后一个成功的任务
     * @param templateId
     * @param companyId
     * @param createTime
     * @return
     */
    public TaskInstance getScheduleTimeLastSuccessTaskInstance(String templateId, String companyId,long createTime);
	
    /**
     * 定时任务列表查询
     *
     * @param taskInstanceDTO 封装请求体的实体类
     * @return 返回集合
     */

    List<Map> getTaskInstanceList(TaskInstanceDTO taskInstanceDTO);

    /**
     * 定时任务详情
     *
     * @param id 定时任务id
     * @return 返回集合
     */

    Map getTaskInstanceDetail(String id);
	
    /**
     * 定时任务重试
     *
     * @param id 定时任务id
     * @return 返回集合
     */

    void taskRetry(String id);

    /**
     * 定时任务预重试
     *
     * @param id 定时任务id
     * @return 返回是否需要重试
     */

    boolean taskPreRetry(String id);

    /**
     * 根据任务实例的状态重设开始和结束时间
     * @param taskInstance
     */
    TaskInstanceTime resetStartAndEndTime(TaskInstance taskInstance);

    /**
     * 根据任务实例的状态重设合并或统计的月份
     * @param taskInstance
     */
    String resetMonth(TaskInstance taskInstance);

    /**
     * 添加定时任务
     * @param companyId
     * @param key
     * @param status
     * @param startTime
     * @param endTime
     * @return
     */
    TaskInstance saveTask(String companyId, String key, TaskTemplate taskTemplate, Company company, TimedTaskStatusEnum status, Long startTime, Long endTime);

    /**
     * 清理quartz内容
     */
    void clearQuartz();
}
