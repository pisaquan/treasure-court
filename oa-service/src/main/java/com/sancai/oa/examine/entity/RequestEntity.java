package com.sancai.oa.examine.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author fanjing
 * @date 2019/7/25
 * @description 前端请求参数封装实体类
 */
@Data
public class RequestEntity {

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
     * 部门id
     */
    @JsonProperty("dept_id")
    private String deptId;
    /**
     * 姓名
     */

    private String name;
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
     * 请假类型
     */
    private String type;

    /**
     * 是否在职
     */
    @JsonProperty("is_inservice")
    private String isInservice;

    /**
     * 查询部门及其子部门信息集合
     */
    private List<Long> deptList;
}
