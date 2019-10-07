package com.sancai.oa.clockin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户一个月的打卡记录
 * @Author chenm
 * @create 2019/8/3 11:46
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_clockin_record")
public class ClockinRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 编号
     */
    private String id;

    /**
     * 员工id
     */
    private String userId;

    /**
     * 员工姓名
     */
    private String userName;

    /**
     * 公司id
     */
    private String companyId;

    /**
     * 月份 yyyy-MM格式
     */
    private String month;

    /**
     * 一个员工一个月的考勤数据的json，map结构，key:日期，value这一天的4次打卡的数组
     */
    private String content;

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
     * 任务实例id
     */
    private String taskInstanceId;

    @Version
    private Integer version;

}
