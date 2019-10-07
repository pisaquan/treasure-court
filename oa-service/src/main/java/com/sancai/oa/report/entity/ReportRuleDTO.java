package com.sancai.oa.report.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 日志模板规则
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ReportRuleDTO extends Model<ReportRuleDTO> {

    private static final long serialVersionUID=1L;

    /**
     * 规则id
     */
    private String id;

    /**
     * 模板id
     */
    @JsonProperty("template_id")
    private String templateId;

    /**
     * 提交开始时间
     */
    @JsonProperty("start_time")
    private String startTime;

    /**
     * 提交结束时间
     */
    @JsonProperty("end_time")
    private String endTime;

    /**
     * 规则，每个模板字段不同，规则不同,json字符串格式
     */
    @JsonProperty("field_rule")
    private String fieldRule;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
