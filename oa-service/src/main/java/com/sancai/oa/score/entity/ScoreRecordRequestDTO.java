package com.sancai.oa.score.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sancai.oa.examine.entity.QueryCommonDTO;
import lombok.Data;

import java.util.List;

/**
 * 积分变动列表封装请求体的实体类
 * 备注：继承QueryCommonDTO是为了使用工具类QueryCommonUtils的getDeptName方法，转换部门name
 * @author fanjing
 * @date 2019/8/7
 */
@Data
public class ScoreRecordRequestDTO extends QueryCommonDTO {
    /**
     * 当前页
     */
    private Integer page;
    /**
     * 条目数
     */
    private Integer capacity;


    /**
     *部门id,为了接收前端请求体中的dept_id
     */
    @JsonProperty("dept_id")
    private String deptId;
    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;



    /**
     * 员工user_id
     */
    @JsonProperty("user_id")
    private String userId;
    /**
     * 来源
     */
    private String source;

    /**
     * 类型
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
     * 部门集合
     */
    private List<Long>  deptList;

    /**
     * 员工姓名
     */
    private String name;


}
