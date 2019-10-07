package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 审批表单
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExamineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 分公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 审批名称
     */
    private String name;

    /**
     * 审批流程模板标识
     */
    private String code;

    /**
     * 各个公司的同一种审批表单，比如"休假申请"group是相同的
     */
    @JsonProperty("examine_group")
    private String examineGroup;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private Long createTime;

    /**
     * 最后修改时间
     */
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;


}
