package com.sancai.oa.score.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.score.entity.ActionScoreRule;
import com.sancai.oa.score.entity.ActionScoreRuleDTO;
import com.sancai.oa.score.mapper.ActionScoreRuleMapper;
import com.sancai.oa.score.service.IActionScoreRuleService;
import com.sancai.oa.typestatus.enums.ScoreRecordTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 行为积分的规则 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Service
public class ActionScoreRuleServiceImpl extends ServiceImpl<ActionScoreRuleMapper, ActionScoreRule> implements IActionScoreRuleService {

    @Autowired
    ActionScoreRuleMapper actionScoreRuleMapper;


    /**
     * 查询积分规则列表，按积分从小到大排序，不分页
     *
     * @return 返回列表集合
     */
    @Override
    public List<ActionScoreRuleDTO> getScoreRuleList() {
        PageHelper.orderBy("score ASC");
        List<ActionScoreRuleDTO> scoreRuleList = actionScoreRuleMapper.getScoreRuleList();
        for (ActionScoreRuleDTO actionScoreRuleDTO : scoreRuleList) {
            String type = ScoreRecordTypeEnum.getvalueBykey(actionScoreRuleDTO.getType());
            actionScoreRuleDTO.setType(type);
        }
        return scoreRuleList;
    }

    /**
     * 修改积分规则
     *
     * @param id    规则id
     * @param score 积分
     * @return 返回受影响的行数
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})

    public int modifyScoreRule(String id, Integer score) {
        //根据id查询一条积分记录
        ActionScoreRule actionScoreRule = actionScoreRuleMapper.selectById(id);
        actionScoreRule.setScore(score);
        actionScoreRule.setModifyTime(System.currentTimeMillis());
        int i = actionScoreRuleMapper.updateById(actionScoreRule);
        return i;
    }

    /**
     * 根据规则key获取一条规则
     *
     * @param key 规则key
     * @return 返回规则记录DTO
     */
    @Override
    public ActionScoreRuleDTO getScoreRuleByKey(String key) {
        ActionScoreRuleDTO scoreRuleByKey = actionScoreRuleMapper.getScoreRuleByKey(key);
        String type = ScoreRecordTypeEnum.getvalueBykey(scoreRuleByKey.getType());
        scoreRuleByKey.setType(type);
        return scoreRuleByKey;
    }
}
