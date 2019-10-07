package com.sancai.oa.clockin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 考勤结果
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_attendance_record")
public class AttendanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    private String userId;

    /**
     * 员工姓名
     */
    private String userName;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 月份yyyy-MM
     */
    private String month;

    /**
     * 未打卡次数
     */
    private Integer notSignedCount;

    /**
     * 早退次数
     */
    private Integer earlyCount;

    /**
     * 迟到次数
     */
    private Integer lateCount;

    /**
     * 旷工天数
     */
    private Float absenteeismDays;

    /**
     * 计薪天数
     */
    private Float salaryDays;

    /**
     * 实际出勤天数
     */
    private Float attendanceDays;

    /**
     * 出差天数
     */
    private Float businessTravelDays;

    /**
     * 公休天数
     */
    private Float holidayDays;

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
     * 婚假天数
     */
    private Float marriageLeaveDays;

    /**
     * 丧假天数
     */
    private Float funeralLeaveDays;

    /**
     * 积分变动数
     */
    private Float score;

    /**
     * 日报不合格数
     */
    private Integer reportLowQualityCount;

    /**
     * 员工是否确认(0:未确认,1:已确认)
     */
    private Integer userConfirm;

    /**
     * 员工确认时间
     */
    private Long userConfirmTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;

    /**
     * 任务实例id
     */
    private String taskInstanceId;

    /**
     * 未出勤旷工天数
     */
    private Float notAttendanceAbsenteeismDays;

    /**
     * 缺卡旷工天数
     */
    private Float notSignedAbsenteeismDays;

    /**
     * 迟到旷工天数
     */
    private Float lateAbsenteeismDays;

    /**
     * 早退旷工天数
     */
    private Float earlyAbsenteeismDays;

    /**
     * 日报早交旷工天数
     */
    private Float reportEarlyAbsenteeismDays;

    /**
     * 日报晚交旷工天数
     */
    private Float reportLateAbsenteeismDays;

    /**
     * 带薪病假
     */
    private Float paidSickLeaveDays;

    /**
     * 无薪病假
     */
    private Float unpaidSickLeaveDays;

    /**
     * 陪产假天数
     */
    private Float paternityLeaveDays;


}
