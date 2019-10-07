package com.sancai.oa.clockin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 考勤列表参数
 * @Author wangyl
 * @create 2019/8/1 10:55
 */
@Data
public class AttendanceRecordDTO {
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
     * 部门id string
     */
    @JsonProperty("dept_id")
    private String deptId;

    /**
    * 部门id int
     */
    private Integer deptIdInt;
    /**
     * 姓名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     *是否全勤
     */
    @JsonProperty("full_attendance")
    private String fullAttendance;

    private int fullAttendanceInt;
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
     * 月份
     */
    private String month;
    /**
     * 员工是否确认
     * (0:未发送,1:已发送未确认，2：员工已确认)
     * int
     */
    private Integer confirm;
    /**
     * 员工是否确认字符串
     * (0:未发送,1:已发送未确认，2：员工已确认)
     * int
     */
    private String confirmStr;

    /**
     * 部门集合
     */
    private List<Long> deptList;
}
