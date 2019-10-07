package com.sancai.oa.examine.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 行为奖罚
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_examine_action")
public class ExamineAction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    private String userId;

    /**
     * 员工姓名
     */
    private String userName;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 审批模板标识
     */
    private String processCode;

    /**
     * 批审实例id
     */
    private String processInstanceId;

    /**
     * 审批标题
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
     * 表单内容：类别
     */
    private String formValueType;

    /**
     * 表单内容：员工
     */
    private String formValueUserId;

    /**
     * 表单内容：员工姓名
     */
    private String formValueUserName;

    /**
     * 表单内容：依据
     */
    private String formValueRule;

    /**
     * 表单内容：分数
     */
    private Float formValueScore;

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


    /**
     * 抓取任务的id
     */
    private String taskInstanceId;

    /**
     * 是否在职
     */
    private String isInservice;

}
