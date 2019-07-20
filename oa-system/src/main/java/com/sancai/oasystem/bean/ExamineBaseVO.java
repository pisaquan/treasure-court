package com.sancai.oasystem.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *  审批表单实体VO
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExamineBaseVO implements Serializable {

    private static final long serialVersionUID = 2859503492205713863L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    private String userId;

    /**
     * 审批模板标识
     */
    private String processCode;

    /**
     * 批审实例id
     */
    private String processInstanceId;

    /**
     * 批审标题
     */
    private String processTitle;

    /**
     * 审批开始时间
     */
    private Long processCreateTime;

    /**
     * 审批完成时间
     */
    private Long processFinishTime;

    /**
     * 审批状态
     */
    private String processStatus;

    /**
     * 审批结果
     */
    private String processResult;

    /**
     * 表单内容：所属公司
     */
    private String formValueCompany;

    /**
     * 表单内容：是否带薪
     */
    private String formValueSalary;

    /**
     * 表单内容：类型
     */
    private String formValueType;

    /**
     * 表单内容：开始时间
     */
    private Long formValueStart;

    /**
     * 表单内容：结束时间
     */
    private Long formValueFinish;

    /**
     * 表单内容：天数
     */
    private Integer formValueDays;

    /**
     * 表单内容：原因
     */
    private String formValueReason;

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
