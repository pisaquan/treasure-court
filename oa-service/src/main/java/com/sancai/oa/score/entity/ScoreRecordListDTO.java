package com.sancai.oa.score.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sancai.oa.examine.entity.QueryCommonDTO;
import lombok.Data;

/**
 * 积分变动列表返回实体类
 * @author fanjing
 * @date 2019/8/8
 */
@Data
public class ScoreRecordListDTO  extends QueryCommonDTO {

    /**
     * id
     */
    private String id;
    /**
     * 部门名称
     */
    @JsonProperty("dept_name")
    private String deptName;

    /**
     * 名称
     */
    private String name;
    /**
     * 变动来源
     */
    private String source;

    /**
     * 类型
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
     * 用户id
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 积分记录生成时间
     */
    @JsonProperty("score_record_time")
    private Long scoreRecordTime;
}
