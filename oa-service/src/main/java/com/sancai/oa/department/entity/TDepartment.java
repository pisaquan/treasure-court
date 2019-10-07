package com.sancai.oa.department.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 部门表
 * </p>
 *
 * @author wangyl
 * @since 2019-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_department")
public class TDepartment extends Model<TDepartment> {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 上级部门id
     */
    private String parentId;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 部门级别
     */
    private Integer level;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 抓取任务的id
     */
    private String taskInstanceId;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
