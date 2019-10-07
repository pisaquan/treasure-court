package com.sancai.oa.clockin.entity;

import lombok.Data;

/**
 * Excel中动态显示离职在职人数，部门名称，星期，等封装的实体类
 * @author fanjing
 * @date 2019/8/23
 */
@Data
public class ExcelOneObjectVO {

    /**
     * 在职人数
     */
    private Integer onTheJob;
    /**
     * 离职人数
     */
    private Integer offTheJob;
    /**
     * 年份
     */
    private String year;

    /**
     * 月份
     */
    private String month;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 每一天对应的星期
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
     * 公司名称
     */
    private String companyName;

}
