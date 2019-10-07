package com.sancai.oa.report.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 日志规则
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_report_rule")
public class ReportRule extends Model<ReportRule> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 日志模板id
     */
    private String templateId;

    /**
     * 规则，每个模板字段不同，规则不同。json格式
     [{'key':'今日任务','allow_empty':false,'min_length':30},{'key':'明日任务','allow_empty':true,'min_length':0}]
     */
    private String fieldRule;

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
