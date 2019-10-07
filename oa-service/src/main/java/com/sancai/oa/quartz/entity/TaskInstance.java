package com.sancai.oa.quartz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任务实例
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_task_instance")
public class TaskInstance extends Model<TaskInstance> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 任务模板id
     */
    private String taskTemplateId;

    /**
     * 任务名
     */
    private String taskName;

    /**
     * 状态
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryTime;

    /**
     * 错误原因
     */
    private String failReason;

    /**
     * 启动参数
     */
    private String params;

    /**
     * 最后执行时间
     */
    private Long lastExcuteTime;

    /**
     * 成功时间
     */
    private Long successTime;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删,2:执行中)
     */
    private Integer deleted;

    /**
     * 公司id
     */
    private String companyId;
    /**
     * 分组
     */
    private String type;

    /**
     * 手工重试次数
     */
    private Integer manualRetryTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    /**
     * 开始时间
     */
    private Long startTime;
    /**
     * 截止时间
     */
    private Long endTime;
}
