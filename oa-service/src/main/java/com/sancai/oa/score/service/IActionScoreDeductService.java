package com.sancai.oa.score.service;

import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;

/**
 * 判断日志是否符合规则积分扣除接口
 * @author fanjing
 * @date 2019/8/10
 */
public interface IActionScoreDeductService {

    /**
     * 根据传来的ruleKey在积分规则中找出应扣分数后再积分记录表中添加记录
     * @param ruleKey ruleKey为积分规则中的规则Key字段
     */
     void reportDeductScore(String companyId, UserDTO user, String ruleKey, ReportRecord reportRecord , String remark , String taskInstanceId );

    /**
     * 日报积分订正
     *
     * @param companyId 公司Id
     * @param user      用户
     * @param ruleKey   ruleKey为积分规则中的规则Key字段
     */
    void reportDeductScore(String companyId, UserDTO user, String ruleKey, ReportRecord reportRecord, String remark, ActionScoreRecord actionScoreRecord);
}
