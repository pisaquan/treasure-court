package com.sancai.oa.signinconfirm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 签到确认
 * </p>
 *
 * @author wangyl
 * @since 2019-08-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_signin_confirm")
public class SigninConfirm extends Model<SigninConfirm> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    private String userId;

    /**
     * 部门id
     */
    private Integer deptId;

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
     * 员工姓名
     */
    private String userName;

    /**
     * 审批者id
     */
    private String confirmUserId;

    /**
     * 任务日期 yyyy-MM-dd
     */
    private String day;

    /**
     * 钉钉待办id
     */
    private String ddWorkrecordId;

    /**
     * 已完成/未完成
     */
    private String status;
    /**
     * 公司id
     */
    private String companyId;
    /**
     * 发送任务的id
     */
    private String taskInstanceId;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
