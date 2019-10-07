package com.sancai.oa.report.controller;

import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;

import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.report.entity.ReportRuleDTO;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.service.IReportRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 日志规则 前端控制器
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/report_rule")
public class ReportRuleController {
    @Autowired
    IReportRuleService iReportRuleService;
    /**
     * 根据模板id或模板code取规则列表
     *
     * @param
     * @return
     */
    @PostMapping("/template_id_rule_list")
    public ApiResponse reportRuleDetail(@RequestBody Map<String,Object> map) {
        try {
            DataMap tReportRules = iReportRuleService.reportRuleDetail(map);
            if (tReportRules != null) {
                return ApiResponse.success(tReportRules);
            }
            return ApiResponse.fail(EnumReportError.REPORT_NOT_DATA);
        } catch (Exception e) {
            throw new OaReportlException(EnumReportError.NO_OPERATION_OK);
        }
    }


    /**
     * 修改日志模板规则
     *
     * @param reportRuleDTO
     * @return
     */
    @PostMapping("/update_rule")
    @Log( type =  LogOperationTypeEnum.UPDATE,model = LogModelEnum.REPORT_RULE)
    public ApiResponse ruleModify(@RequestBody ReportRuleDTO reportRuleDTO){
        boolean whetherSuccess = iReportRuleService.ruleModify(reportRuleDTO);
        if (whetherSuccess) {
            return ApiResponse.success();
        }
        return ApiResponse.fail(EnumCompanyError.NO_OPERATION_OK);
    }

}

