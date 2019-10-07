package com.sancai.oa.score.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 行为积分的规则
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_action_score_rule")
public class ActionScoreRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 加项/减项
     */
    private String type;

    /**
     * 规则的分数
     */
    private Integer score;

    /**
     * 规则标识key
     */
    private String key;

    /**
     * 规则名称
     */
    private String name;

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
