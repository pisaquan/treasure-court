package com.sancai.oa.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 日志模板
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_report_template")
public class ReportTemplate extends Model<ReportTemplate> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 分公司id
     */
    private String companyId;

    /**
     * 日志模板标识
     */
    private String code;

    /**
     * 日志模板名
     */
    private String name;

    /**
     * 各个公司的同一种模板，比如"门店总日报"group是相同的，之间的规则可以复制
     */
    private String templateGroup;

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

    /**
     * 状态（failure失效，effective有效）
     */
    private String status;

    /**
     * 提交开始时间
     */
    private String beginTime;

    /**
     * 提交结束时间
     */
    private String finishTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
