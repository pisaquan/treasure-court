package com.sancai.oa.examine.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * @author fanjing
 * @date 20197/30
 * @decription 返回前端的出差详情实体类
 */
@Data
public class ExamineBusinessTravelDetailDTO {

    /**
     * 编号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;
    /**
     * 部门
     */
    @JsonProperty("dept_name")
    private String deptName;
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
     * 出差事由
     */
    private String reason;

    /**
     * 出发城市
     */
    @JsonProperty("from_city")
    private String fromCity;
    /**
     * 出发交通工具
     */
    @JsonProperty("start_transport")
    private String startTransport;

    /**
     * 返回交通工具
     */
    @JsonProperty("finish_transport")
    private String finishTransport;
    /**
     * 住宿安排
     */
    @JsonProperty("hotel_type")
    private String hotelType;

    /**
     * 备注
     */
    private String remark;

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
