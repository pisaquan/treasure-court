package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 返回前端的请假详情实体类
 * @author fanjing
 * @date 2019/8/3
 */
@Data
public class ExamineLeaveDetailDTO {
    /**
     * 编号
     */
    private String id;

    /**
     * 姓名
     */
    private String name;
    /**
     * 部门id
     */
    @JsonProperty("dept_name")
    private String deptName;

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
     * 请假原因
     */
    private String reason;

    /**
     * 是否带薪
     */
    @JsonProperty("form_value_salary")
    private String formValueSalary;

    /**
     * 病例报告信息url
     */
    @JsonProperty("case_report_url")
    private String caseReportUrl;

    /**
     * 带薪病假通知是否已发送：0:未发送,1:已发送未上传，2：已发送已上传
     */
    @JsonProperty("send_notify_status")
    private Integer sendNotifyStatus;

    /**
     * 病例证明状态
     */
    @JsonProperty("case_report_status")
    private String caseReportStatus;

    /**
     * 病例报告信息urlList
     */
    @JsonProperty("case_report_url_list")
    private List<String> caseReportUrlList;

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
