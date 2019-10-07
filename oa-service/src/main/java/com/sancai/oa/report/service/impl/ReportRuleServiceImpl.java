package com.sancai.oa.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sancai.oa.report.entity.ReportRuleDTO;
import com.sancai.oa.report.entity.ReportRule;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.mapper.ReportRuleMapper;
import com.sancai.oa.report.mapper.ReportTemplateMapper;
import com.sancai.oa.report.service.IReportRuleService;
import com.sancai.oa.utils.OaMapUtils;
import com.sancai.oa.utils.UUIDS;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * 日志规则 服务实现类
 * </p>
 *
 * @author  fans
 * @since 2019-07-19
 */
@Service
public class ReportRuleServiceImpl extends ServiceImpl<ReportRuleMapper, ReportRule> implements IReportRuleService {
    @Autowired
    ReportRuleMapper reportRuleMapper;
    @Autowired
    ReportTemplateMapper reportTemplateMapper;

    /**
     * 根据模板id或模板code取规则列表
     *@param map 模板id
     * @return ReportTemplate
     */
    @Override
    public DataMap reportRuleDetail (Map<String,Object> map){
        if(OaMapUtils.mapIsAnyBlank(map,"template_id")&& OaMapUtils.mapIsAnyBlank(map,"template_code")){
            throw new OaReportlException(EnumReportError.PARAMETER_IS_NULL_ID_CODE);
        }
        DataMap tReportRule = reportRuleMapper.reportRuleListByIdOrCode(map);
        if(tReportRule!=null){
            return tReportRule;
        }
        return null;
    }


    /**
     * 修改日志模板规则
     *
     * @param  reportRuleDTO
     * @return boolean
     */
    @Override
    public boolean ruleModify(ReportRuleDTO reportRuleDTO){
        if (StringUtils.isAnyBlank(reportRuleDTO.getTemplateId(),reportRuleDTO.getId(),reportRuleDTO.getStartTime(),reportRuleDTO.getEndTime())) {
            throw new OaReportlException(EnumReportError.PARAMETER_IS_NULL_ID_TIME);
        }
        ReportRule reportRule =  reportRuleMapper.selectById(reportRuleDTO.getId());
        if(reportRule == null){
            throw new OaReportlException(EnumReportError.REPORT_NOT_DATA_RULE_ID);
        }
        ReportRule tsr = new ReportRule();
        tsr.setId(reportRuleDTO.getId());
        tsr.setModifyTime(System.currentTimeMillis());
        tsr.setDeleted(0);
        tsr.setFieldRule(reportRuleDTO.getFieldRule());
        tsr.setTemplateId(reportRuleDTO.getTemplateId());
        int sRof = reportRuleMapper.updateById(tsr);
        ReportTemplate reportTemplate = new ReportTemplate();
        reportTemplate.setBeginTime(reportRuleDTO.getStartTime());
        reportTemplate.setFinishTime(reportRuleDTO.getEndTime());
        reportTemplate.setId(reportRuleDTO.getTemplateId());
        int whetherSucess = reportTemplateMapper.updateById(reportTemplate);
        if (sRof <= 0||whetherSucess <= 0) {
            throw new OaReportlException(EnumReportError.NO_OPERATION_OK_UPDATE);
        }
        return true;
    }

    /**
     * 初始化日志模板规则
     *
     * @param  templateId
     * @return boolean
     */
    @Override
    public boolean initRule(String templateId){
        ReportRule tsr = new ReportRule();
        tsr.setId(UUIDS.getID());
        tsr.setCreateTime(System.currentTimeMillis());
        tsr.setDeleted(0);
        tsr.setTemplateId(templateId);
        int sRof = reportRuleMapper.insert(tsr);
        if (sRof <= 0) {
            throw new OaReportlException(EnumReportError.NO_OPERATION_OK_INSERT);
        }
        return true;
    }





}
