package com.sancai.oa.clockin.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClockinRecordMergeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考勤合并结果id
     */
    private String mergeRecordId;

    /**
     * 考勤统计结果id
     */
    private String attendanceRecordId;

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
     * 统计的月份 yyyy-MM格式
     */
    private String statisticalMonth;

    /**
     * 一个员工一个月的考勤数据的json，map结构，key:日期，value这一天的打卡数组
     */
    private String content;

    /**
     * 任务实例id
     */
    private String taskInstanceId;




}
