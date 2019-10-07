package com.sancai.oa.score.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 行为积分变动记录
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_action_score_record")
public class ActionScoreRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 员工姓名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 来源，行为奖罚，迟到，缺卡，等
     */
    private String source;

    /**
     * 关联的目标的编号
     */
    private String targetId;

    /**
     * 加项/减项
     */
    private String type;

    /**
     * 分数
     */
    private Float score;

    /**
     * 备注
     */
    private String remark;

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
     * 积分记录生成时间
     */
    private Long scoreRecordTime;


}
