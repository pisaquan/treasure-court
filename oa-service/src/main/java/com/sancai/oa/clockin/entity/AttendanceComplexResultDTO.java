package com.sancai.oa.clockin.entity;

import lombok.Data;

/**
 * 考勤复合结果实体类（统计天数，考勤数据json等）
 * @author fanjing
 * @date 2019/8/19
 */
@Data
public class AttendanceComplexResultDTO {

    /**
     * 序号
     */
    private String id;
    /**
     * 姓名
     */
    private String name;

    /**
     * 用户id
     */
    private String userId;
    /**
     * 部门id
     */
    private String deptId;

    /**
     * 考勤数据json
     */
    private String content;

    /**
     * 漏打卡
     */
    private Integer notSignedCount;

    /**
     * 病假天数
     */
    private Float sickLeaveDays;

    /**
     * 事假天数
     */
    private Float personalLeaveDays;

    /**
     * 产假天数
     */
    private Float childbirthLeaveDays;

    /**
     * 旷工天数
     */
    private  Float absenteeismDays;

    /**
     * 公休天数
     */
    private Float holidayDays;
    /**
     * 实际出勤天数
     */
    private Float attendanceDays;

    /**
     * 计薪天数
     */
    private Float salaryDays;


    /**
     * 迟到次数
     */
    private Integer lateCount;

    /**
     * 早退次数
     */
    private Integer earlyCount;
    /**
     * 婚假天数
     */
    private Float marriageLeaveDays;

    /**
     * 丧假天数
     */
    private Float funeralLeaveDays;

    /**
     * 陪产假天数
     */
    private Float paternityLeaveDays;



}
