
package com.sancai.oa.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.report.entity.ReportRule;
import com.sancai.oa.report.entity.modify.DataMap;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * <p>
 * 日志规则 Mapper 接口
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Repository
public interface ReportRuleMapper extends BaseMapper<ReportRule> {



    /**
     * 根据模板id或模板code取规则
     * @param map
     * @return
     */
     DataMap reportRuleListByIdOrCode(Map<String, Object> map);
}
