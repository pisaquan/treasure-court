package com.sancai.oa.examine.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 外出申请
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_examine_out_apply")
public class ExamineOutApply implements Serializable {

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
     * 表单内容：开始时间
     */
    private Long formValueStart;

    /**
     * 表单内容：结束时间
     */
    private Long formValueFinish;

    /**
     * 表单内容：经办事由
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
     * 表单内容：是否可以正常打卡
     */
    private String formValueClockin;

    /**
     * 表单内容：外出地点
     */
    private String formValuePlace;

    /**
     * 表单内容：时长
     */
    private Float formValueHours;

    /**
     * 表单内容：未能正常打卡时间
     */
    private String formValueNoClockinTime;

    /**
     * 表单内容：部门id
     */
    private Integer formValueDeptId;


    /**
     * 外出签到确认任务表id
     */
    private String signinConfirmId;

    /**
     * 抓取任务的id
     */
    private String taskInstanceId;


}
