package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 此类为请假列表，出差列表，公休列表的三个接口返回前端DTO的基类
 * @author fanjing
 * @date 2019/8/5
 */
@Data
public class QueryCommonDTO {

    /**
     * 部门名称
     */
    @JsonProperty("dept_name")
    public String deptName;

    /**
     * 部门id
     */
    @JsonProperty("dept_id")
    public String deptId;





}
