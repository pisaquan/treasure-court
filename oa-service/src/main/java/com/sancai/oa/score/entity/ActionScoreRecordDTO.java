package com.sancai.oa.score.entity;

import lombok.Data;

/**
 *查询一名员工每月的积分总和封装参数的实体类
 * @author fanjing
 * @date 2019/8/13
 */
@Data
public class ActionScoreRecordDTO {
    /**
     * 员工id
     */
    private String userId;
    /**
     * 一个月的开始时间
     */
    private Long startTime;

    /**
     * 一个月的结束时间
     */
    private Long endTime;

    /**
     * 公司id
     */
    private String companyId;
}
