package com.sancai.oa.signin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 签到记录
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_signin_record")
public class SigninRecord extends Model<SigninRecord> {

    private static final long serialVersionUID=1L;

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
    @JsonProperty("name")
    private String userName;

    /**
     * 公司id
     */
    @JsonProperty("company_id")
    private String companyId;

    /**
     * 图片url数组
     */
    @JsonProperty("image_list")
    private String imageList;

    /**
     * 地址
     */
    private String place;

    /**
     * 详细地址
     */
    @JsonProperty("detail_place")
    private String detailPlace;

    /**
     * 备注
     */
    private String remark;

    /**
     * 签到时间
     */
    @JsonProperty("checkin_time")
    private Long checkinTime;

    /**
     * 拜访客户
     */
    @JsonProperty("visit_user")
    private String visitUser;

    /**
     * 经度
     */
    @JsonProperty("lng")
    private Float longitude;

    /**
     * 纬度
     */
    @JsonProperty("lat")
    private Float latitude;

    /**
     * 状态：NEW/VALID/INVALID
     */
    private String status;

    /**
     * 是否人工确认过(0:否,1:是)
     */
    private Integer confirm;

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
     * 外出申请单id
     */
    @JsonProperty("out_apply_id")
    private String outApplyId;

    /**
     * 审批者id
     */
    @JsonProperty("confirm_user_id")
    private String confirmUserId;

    /**
     * 无效的原因
     */
    @JsonProperty("invalid_reason")
    private String invalidReason;

    /**
     * 关联的打卡点id
     */
    @JsonProperty("attendance_id")
    private Long attendanceId;

    /**
     * 抓取任务的id
     */
    @JsonProperty("task_instance_id")
    private String taskInstanceId;
    /**
     * 关联签到确认表主键id
     */
    @JsonProperty("signin_confirm_id")
    private String signinConfirmId;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
