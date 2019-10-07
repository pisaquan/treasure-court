package com.sancai.oa.score.service;

import com.sancai.oa.score.entity.ActionScoreRule;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.score.entity.ActionScoreRuleDTO;

import java.util.List;

/**
 * <p>
 * 行为积分的规则 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
public interface IActionScoreRuleService extends IService<ActionScoreRule> {

    /**
     * 查询行为积分规则
     * @return 返回行为积分规则列表
     */
    List<ActionScoreRuleDTO> getScoreRuleList();

    /**
     * 修改积分规则
     * @param id 规则id
     * @param score 积分
     * @return 返回受影响的行数
     */
    int modifyScoreRule(String id, Integer score);

    /**
     * 根据规则key获取一条规则
     * @param key 规则key
     * @return 返回该key对应的规则
     */
    ActionScoreRuleDTO getScoreRuleByKey(String key);
}
