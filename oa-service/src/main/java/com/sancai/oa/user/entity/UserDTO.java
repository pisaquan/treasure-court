package com.sancai.oa.user.entity;

import lombok.Data;

import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/22 14:01
 */
@Data
public class UserDTO {
    /**
     * 主键
     */
    private String id;
    /**
     * 用户名
     */
    private String name;
    /**
     * 用户id
     */
    private String userId;
    /**
     *
     */
    private String deptId;
    /**
     * 手机
     */
    private String mobile;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 状态（1 .离职  2，试用期；3，正式；5，待离职；-1，无状态）
     */
    private Integer status;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 任务实例id
     */
    private String taskInstanceId;
    /**
     * 最后离职时间
     */
    private Long lastWorkDay;

    /**
     * 用户部门关系
     */
    private List<UserDepartment> userDepartments;
}
