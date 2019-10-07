package com.sancai.oa.examine.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import lombok.Data;

/**
 * 行为考核列表接口和岗位考核列表接口共用
 * @author fanjing
 * @date 2019/7/26
 */
@Data
public class ActionPositionDataDTO extends QueryCommonDTO {

    /**
     * 编号
     */
    private String id;
    /**
     * 月份
     */
    private String month;
    /**
     * 员工姓名
     */
    private String name;
    /**
     * 员工id
     */
    @JsonProperty("form_user_id")
    private String formUserId;

    /**
     * 发起人
     */
    @JsonProperty("create_examine_user_name")
    private String createExamineUserName;
    /**
     * 类型
     */
    private String type;
    /**
     * 积分
     */
    private Float score;
    /**
     * 原因
     */
    private String reason;
    /**
     * 审批开始时间
     */
    @JsonProperty("process_create_time")
    private Long processCreateTime;

    /**
     * 是否在职
     */
    @JsonProperty("is_inservice")
    private String isInservice;
}
