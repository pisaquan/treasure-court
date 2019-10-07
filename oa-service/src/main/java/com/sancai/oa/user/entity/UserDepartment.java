package com.sancai.oa.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户部门关系表
 * </p>
 *
 * @author wangyl
 * @since 2019-09-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_user_department")
public class UserDepartment extends Model<UserDepartment> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 用户表主键id
     */
    @JsonProperty("u_id")
    private String uId;

    /**
     * 部门id
     */
    @JsonProperty("dept_id")
    private String deptId;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;

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
     * 抓取任务的id
     */
    @JsonProperty("task_instance_id")
    private String taskInstanceId;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
