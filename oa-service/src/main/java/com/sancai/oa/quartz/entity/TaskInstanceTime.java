package com.sancai.oa.quartz.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * <p>
 * 任务实例执行时间区间
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TaskInstanceTime implements Serializable {

    private static final long serialVersionUID = 963441321591184284L;

    /**
     * 执行开始时间
     */
    private long startTime;

    /**
     * 执行结束时间
     */
    private long endTime;
}
