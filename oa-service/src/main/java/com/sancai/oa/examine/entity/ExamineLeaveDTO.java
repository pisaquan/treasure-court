package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 请假
 * @author fanjing
 * @date 2019/8/3
 */
@Data
public class ExamineLeaveDTO  extends QueryCommonDTO {

    /**
     * 编号
     */
    public String id;

    /**
     * 姓名
     */
    public String name;


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
     * 本月内请假天数
     */
    @JsonProperty("this_month_leave_days")
    private Float thisMonthLeaveDays;

    /**
     * 是否带薪
     */
    @JsonProperty("form_value_salary")
    private String formValueSalary;

    /**
     * 病例证明审核状态
     */
    @JsonProperty("case_report_status")
    private String caseReportStatus;

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
