package com.sancai.oa.examine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 审批模板类型
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExamineTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审批类型id
     */
    @JsonProperty("examine_id")
    private String examineId;

    /**
     * 审批类型名称
     */
    @JsonProperty("examine_name")
    private String examineName;



}
