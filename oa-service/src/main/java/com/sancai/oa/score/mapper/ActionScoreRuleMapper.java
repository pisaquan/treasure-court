package com.sancai.oa.score.mapper;

import com.sancai.oa.score.entity.ActionScoreRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.score.entity.ActionScoreRuleDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 行为积分的规则 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Repository
public interface ActionScoreRuleMapper extends BaseMapper<ActionScoreRule> {

    /**
     * 查询行为积分规则列表
     * @return 返回规则列表DTO集合
     */
    List<ActionScoreRuleDTO> getScoreRuleList();

    /**
     * 根据规则key查询一条规则
     * @param key 规则key
     * @return 返回规则记录DTO
     */
    ActionScoreRuleDTO getScoreRuleByKey(String key);
}
