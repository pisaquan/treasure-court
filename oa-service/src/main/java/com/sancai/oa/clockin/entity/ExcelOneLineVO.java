package com.sancai.oa.clockin.entity;

import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import lombok.Data;

/**
 * Excel中每一行记录对应的实体类
 *
 * @author fanjing
 * @date 2019/8/17
 */
@Data
public class ExcelOneLineVO extends ExcelExportEntity {


    /**
     * 序号
     */
    private Integer id;
    /**
     * 姓名
     */
    private String name;

    /**
     * 每月第一天
     */
    private String day1;
    private String day2;
    private String day3;
    private String day4;
    private String day5;
    private String day6;
    private String day7;
    private String day8;
    private String day9;
    private String day10;
    private String day11;
    private String day12;
    private String day13;
    private String day14;
    private String day15;
    private String day16;
    private String day17;
    private String day18;
    private String day19;
    private String day20;
    private String day21;
    private String day22;
    private String day23;
    private String day24;
    private String day25;
    private String day26;
    private String day27;
    private String day28;
    private String day29;
    private String day30;
    private String day31;

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
    private Float absenteeismDays;

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
     * 加班天数
     */
    private Float overTimeDays;

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
