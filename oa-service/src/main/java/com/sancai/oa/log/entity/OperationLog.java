package com.sancai.oa.log.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author fanjing
 * @since 2019-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_operation_log")
public class OperationLog  {

    private static final long serialVersionUID=1L;

    private String id;

    /**
     * 操作人员id
     */
    private String operatorUserId;

    /**
     * 操作类型
     */
    private String type;

    /**
     * 操作详情json
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



}
