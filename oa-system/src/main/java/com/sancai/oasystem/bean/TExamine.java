package com.sancai.oasystem.bean;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 审批表单
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TExamine implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 分公司id
     */
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
     * 审批图标
     */
    private String iconUrl;

    /**
     * 各个公司的同一种审批表单，比如"休假申请"group是相同的
     */
    private String group;

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


}
