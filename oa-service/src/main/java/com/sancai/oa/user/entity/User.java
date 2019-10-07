package com.sancai.oa.user.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/22 13:14
 */
@Data
@TableName("t_user")
public class User {
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
    @JsonProperty("user_id")
    private String userId;
    /**
     *
     */
    @JsonProperty("dept_id")
    private String deptId;
    /**
     * 手机
     */
    private String mobile;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private Long createTime;

    /**
     * 最后修改时间
     */
    @JsonProperty("modify_time")
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
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 任务实例id
     */
    @JsonProperty("task_instance_id")
    private String taskInstanceId;
    /**
     * 最后离职时间
     */
    @JsonProperty("last_work_day")
    private Long lastWorkDay;


    public User() {

    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
