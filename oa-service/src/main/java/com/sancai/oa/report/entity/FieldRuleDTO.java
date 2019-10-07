package com.sancai.oa.report.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 日志规则相关字段实体类
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FieldRuleDTO{

    private static final long serialVersionUID=1L;

    /**
     * 'key':日志规则字段名称
     */
    private String key;

    /**
     * 'value':钉钉日报数据字段值
     */
    private String value;

    /**
     * allow_empty 日志规则字段是否可为空
     */
    private Boolean allowEmpty;

    /**
     * 日志规则字段最小长度
     */
    private Long minlength;




}
