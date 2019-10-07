package com.sancai.oa.examine.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 出差
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_examine_business_travel")
public class ExamineBusinessTravel implements Serializable {

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
     * 表单内容：出差事由
     */
    private String formValueReason;

    /**
     * 表单内容：出发城市
     */
    private String formValueFromCity;

    /**
     * 表单内容：目的城市
     */
    private String formValueToCity;

    /**
     * 表单内容：出发时间
     */
    private Long formValueStartTime;

    /**
     * 表单内容：返回时间
     */
    private Long formValueFinishTime;

    /**
     * 表单内容：出发交通工具
     */
    private String formValueStartTransport;

    /**
     * 表单内容：返回交通工具
     */
    private String formValueFinishTransport;

    /**
     * 表单内容：住宿方式
     */
    private String formValueHotelType;

    /**
     * 表单内容：备注
     */
    private String formValueRemark;

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
     * 钉钉表单原始请假开始时间
     */
    private String formValueStartOriginal;

    /**
     * 钉钉表单原始请假开始时间
     */
    private String formValueFinishOriginal;

}
