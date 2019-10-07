package com.sancai.oa.examine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 钉钉用户获取考勤组信息
 * </p>
 *
 * @author fans
 * @since 2019-09-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ExamineTimeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 打卡类型
     */
    private String checkType;


    /**
     * 打卡时间
     */
    private String checkTimeString;



}
