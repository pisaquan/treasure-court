package com.sancai.oa.clockin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 打卡实例和部门的对应关系表
 * </p>
 *
 * @author fans
 * @since 2019-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_clockin_department")
public class ClockinDepartment extends Model<ClockinDepartment> {

    private static final long serialVersionUID=1L;

    /**
     * 编号
     */
    private String id;

    /**
     * 打卡记录id
     */
    private String clockinRecordId;

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

    @Version
    private Integer version;

    @Override
    protected Serializable pkVal() {
        return null;
    }

    public ClockinDepartment() {
    }

    public ClockinDepartment(String id, String clockinRecordId, Integer deptId, Long createTime, Long modifyTime, Integer deleted) {
        this.id = id;
        this.clockinRecordId = clockinRecordId;
        this.deptId = deptId;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.deleted = deleted;
    }
}
