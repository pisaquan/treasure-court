package com.sancai.oa.score.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 积分规则列表返回DTO
 * @author fanjing
 * @date 2019/8/6
 */
@Data
public class ActionScoreRuleDTO {
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
    @JsonProperty("rule_key")
    private String ruleKey;

    /**
     * 规则名称
     */
    private String name;


}
