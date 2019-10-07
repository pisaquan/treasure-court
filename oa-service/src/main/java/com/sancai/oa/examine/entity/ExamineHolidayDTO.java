package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公休
 * @author fanjing
 * @date 2019/7/30
 */

@Data
public class ExamineHolidayDTO extends QueryCommonDTO {
    /**
     * 编号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;


    /**
     * 请假类型
     */
    private String type;
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
     * 天数
     */
    private Float days;
    /**
     * 审批完成时间
     */
    @JsonProperty("process_finish_time")
    private Long processFinishTime;

    /**
     * 本月内公休天数
     */
    @JsonProperty("this_month_holiday_days")
    private Float thisMonthHolidayDays;

    /**
     * 钉钉表单原始请假开始时间
     */
    @JsonProperty("form_value_start_original")
    private String formValueStartOriginal;

    /**
     * 钉钉表单原始请假开始时间
     */
    @JsonProperty("form_value_finish_original")
    private String formValueFinishOriginal;



}
