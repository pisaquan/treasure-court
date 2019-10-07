package com.sancai.oa.clockin.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <p>
 * 打卡记录
 * </p>
 *
 * @author fans
 * @since 2019-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ClockinRecordDTO {

    private static final long serialVersionUID=1L;

    /**
     * 第几页
     */
    private Integer page;

    /**
     * 每页显示条数
     */
    private Integer capacity;

    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 提交开始时间
     */
    @JsonProperty("start_time")
    private Long startTime;

    /**
     * 提交结束时间
     */
    @JsonProperty("end_time")
    private Long endTime;


    /**
     * 用户名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 部门id
     */
    @JsonProperty("dept_id")
    private String deptId;
    /**
     * 月份
     */
    private String month;
    /**
     * 打卡内容
     */
    private String content;
    /**
     * 打卡记录id
     */
    private String id;

    /**
     * 部门idList
     */
    private List<Long> deptList;

}
