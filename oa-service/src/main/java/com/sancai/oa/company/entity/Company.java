package com.sancai.oa.company.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 分公司
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_company")
public class Company implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 公司名
     */
    private String name;

    /**
     * 应用id
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 应用key
     */
    @JsonProperty("app_key")
    private String appKey;

    /**
     * 应用密钥
     */
    @JsonProperty("app_secret")
    private String appSecret;

    /**
     * 创建时间
     */
    @JsonProperty("create_time")
    private Long createTime;

    /**
     * 最后修改时间
     */
    @JsonProperty("modify_time")
    private Long modifyTime;

    /**
     * 是否删除(0:未删,1:已删)
     */
    private Integer deleted;
    /**
     * 企业id
     */
    @JsonProperty("corp_id")
    private String corpId;
    /**
     * 通知用户id
     */
    @JsonProperty("user_ids")
    private String userIds;
}
