package com.sancai.oa.score.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 员工累计积分返回实体类
 * @author fanjing
 * @date 2019/9/16
 */
@Data
public class ActionUserScoreDTO {

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    private Float score;
}
