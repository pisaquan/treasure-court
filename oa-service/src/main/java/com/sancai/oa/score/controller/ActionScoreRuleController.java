package com.sancai.oa.score.controller;


import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.score.entity.ActionScoreRuleDTO;
import com.sancai.oa.score.exception.EnumScoreError;
import com.sancai.oa.score.exception.OaScoreException;
import com.sancai.oa.score.service.IActionScoreRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行为积分的规则 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/score")
public class ActionScoreRuleController {

    @Autowired
    IActionScoreRuleService actionScoreRuleService;


    /**
     * 查询积分规则列表：按积分由小到大排序，不分页
     *
     * @return 返回积分规则列表集合
     */
    @PostMapping("score_rule_list")
    public ApiResponse getScoreRuleList() {
        List<ActionScoreRuleDTO> list = null;
        try {
            list = actionScoreRuleService.getScoreRuleList();
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.QUERY_SCORERULE_FAILURE);
        }
        return ApiResponse.success(list);
    }

    /**
     * 修改积分规则
     * map 规则Id 和积分值
     *
     * @return 返回code和message
     */
    @PostMapping("score_rule_modify")
    @Log(type= LogOperationTypeEnum.UPDATE,model= LogModelEnum.SCORE)
    public ApiResponse modifyScoreRule(@RequestBody Map<String, Object> map) {
        if (StringUtils.isEmpty(map.get("id"))) {
            return ApiResponse.fail(EnumScoreError.UPDATE_SCORERULE_PARAM_IS_EMPTY);
        }
        if ((Integer) map.get("score") < 0 || (Integer) map.get("score") > 100) {
            return ApiResponse.fail(EnumScoreError.PARAM_SCORE_IS_BELOWSTANDARD);
        }
        int i = actionScoreRuleService.modifyScoreRule((String) map.get("id"), (Integer) map.get("score"));
        if (i != 1) {
            throw new OaScoreException(EnumScoreError.UPDATE_SCORERULE_FAILURE);
        }
        return ApiResponse.success();
    }

    /**
     * 根据规则key取一条规则
     *
     * @param key 规则key
     * @return 返回该规则key对应的积分规则
     */
    @GetMapping("/score_rule_query/{key}")
    public ApiResponse getRuleByRuleKey(@PathVariable(value = "key", required = true) String key) {
        ActionScoreRuleDTO actionScoreRuleDTO = null;
        try {
            actionScoreRuleDTO = actionScoreRuleService.getScoreRuleByKey(key);
        } catch (Exception e) {
            throw new OaScoreException(EnumScoreError.QUERY_RULE_FAILURE);
        }
        if (actionScoreRuleDTO == null) {
            return ApiResponse.fail(EnumScoreError.QUERY_IS_EMPTY);
        }
        return ApiResponse.success(actionScoreRuleDTO);
    }
}
