package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fanjing
 * @date 2049/7/27
 * @decription 返回数据的实体类
 */
@Data
public class ExamineBusinessTravelDTO extends QueryCommonDTO {

    /**
     * 编号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 目的城市
     */
    @JsonProperty("to_city")
    private String toCity;
    /**
     * 出发时间
     */
    @JsonProperty("start_time")
    private Long startTime;
    /**
     * 返回时间
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
     * 本月内出差天数
     */
    @JsonProperty("this_month_business_travel_days")
    private Float thisMonthBusinessTravelDays;

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
