package com.sancai.oa.quartz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 定时任务列表参数
 * @Author wangyl
 * @create 2019/8/1 10:55
 */
@Data
public class TaskInstanceDTO {
    /**
     * 当前页
     */
    private Integer page;
    /**
     * 条目数
     */
    private Integer capacity;
    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;
    /**
     * 任务模板id string
     */
    @JsonProperty("task_template_id")
    private String taskTemplateId;

    /**
    * 任务类型
     */
    @JsonProperty("task_type")
    private String taskType;

    /**
     * 任务key
     */
    @JsonProperty("task_key")
    private String taskKey;
    /**
     * 开始时间
     */
    @JsonProperty("start_time")
    private Long startTime;
    /**
     * 结束时间
     */
    @JsonProperty("end_time")
    private Long endTime;

    /**
     * 任务状态
     */
    private String status;

}
