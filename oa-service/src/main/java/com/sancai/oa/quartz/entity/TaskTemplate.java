package com.sancai.oa.quartz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 任务模板
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_task_template")
public class TaskTemplate extends Model<TaskTemplate> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 任务模板名
     */
    private String name;

    /**
     * 抓取任务/合并任务
     */
    private String type;

    /**
     * 枚举:考勤抓取、请假抓取、签到合并等
     */
    private String taskKey;

    /**
     * 执行类
     */
    private String classstr;

    /**
     * 执行方法
     */
    private String methodstr;

    /**
     * 下一个执行的任务模板
     */
    private String nextTaskTemplateId;

    /**
     * 频率，cron表达式
     */
    private String frequency;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
