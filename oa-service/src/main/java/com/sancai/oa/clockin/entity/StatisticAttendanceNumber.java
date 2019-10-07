package com.sancai.oa.clockin.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * <p>
 * 统计考勤结果
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StatisticAttendanceNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 不连续的未打卡次数
     */
    private int notSignedCount;

    /**
     * 旷工导致的连续的未打卡次数
     */
    private int absenteeismNotSignedCount;

    /**
     * 早退次数
     */
    private int earlyCount;
    /**
     * 迟到次数
     */
    private int lateCount;
    /**
     * 4次考勤组连续4次未打卡旷工天数
     */
    private int continuousAbsenteeismDaysFour;
    /**
     * 2次考勤组连续2次未打卡旷工天数
     */
    private int continuousAbsenteeismDaysTwo;
    /**
     * 迟到超过60分钟旷工天数
     */
    private float lateAbsenteeismDays;
    /**
     * 早退超过60分钟旷工天数
     */
    private float earlyAbsenteeismDays;
    /**
     * 出差天数
     */
    private float businessTravelDays;
    /**
     * 公休天数
     */
    private float holidayDays;
    /**
     * 病假天数
     */
    private float sickLeaveDays;

    /**
     * 无薪病假天数
     */
    private float unPaidSickLeaveDays;

    /**
     * 事假天数
     */
    private float casualLeaveDays;

    /**
     * 产假天数
     */
    private float maternityLeaveDays;

    /**
     * 陪产假天数
     */
    private float paternityLeaveDays;

    /**
     * 婚假天数
     */
    private float marriageLeaveDays;
    /**
     * 丧假天数
     */
    private float funeralLeaveDays;
    /**
     * 无薪请假天数
     */
    private float noPaidDays;
    /**
     * 缺卡是否被警告
     */
    private boolean isWarnedNotSignedCount;
    /**
     * 迟到早退是否被警告
     */
    private boolean isWarnedLateEarly;

}
