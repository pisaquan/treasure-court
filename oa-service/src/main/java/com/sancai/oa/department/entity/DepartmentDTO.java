package com.sancai.oa.department.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
@Data
public class DepartmentDTO {

    /**
     * 部门id
     */
    private String id;
    /**
     * 上级部门id
     */
    private String parentid;
    /**
     * 部门层级
     */
    private Long level;
    /**
     *  部门名称
     */
    private String name;
    /**
     * 下级部门集合
     */
    private List<DepartmentDTO> children;

}
