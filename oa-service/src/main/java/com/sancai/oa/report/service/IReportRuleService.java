package com.sancai.oa.report.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.sancai.oa.report.entity.ReportRuleDTO;
import com.sancai.oa.report.entity.ReportRule;
import com.sancai.oa.report.entity.modify.DataMap;

import java.util.Map;

/**
 * <p>
 * 日志规则 服务类
 * </p>
 *
 * @author  fans
 * @since 2019-07-19
 */
public interface IReportRuleService extends IService<ReportRule> {
    /**
     * 根据模板id或模板code取规则列表
     *
     * @param map 模板id
     * @return ReportTemplate
     */
    DataMap reportRuleDetail(Map<String, Object> map);
    /**
     * 修改日志模板规则
     *
     * @param  reportRuleDTO
     * @return boolean
     */
    boolean ruleModify(ReportRuleDTO reportRuleDTO);

    /**
     * 初始化日志模板规则
     *
     * @param  templateId
     * @return boolean
     */

    boolean initRule(String templateId);
}
