package com.sancai.oa.report.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.dingtalk.api.response.OapiReportTemplateListbyuseridResponse;
import com.sancai.oa.report.entity.ReportTemplate;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 日志模板 服务类
 * </p>
 *
 * @author  fans
 * @since 2019-07-19
 */
public interface IReportTemplateService extends IService<ReportTemplate> {

    /**
     * 导入日报模板数据
     *@param companyId 公司id
     * @return boolean
     */
     boolean importReportTemplateData(String companyId,String taskInstanceId);


    void addTemplate(OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo reportMap, String companyId);

    /**
     * 日报模板列表
     *
     * @return List
     */

    List<ReportTemplate> reportTemplateList(Map<String,Object> map);

    /**
     * 日报模板详情
     *@param id 模板详情id
     * @return ReportTemplate
     */
    ReportTemplate reportTemplateDetail(String id);

    /**
     * 日报模板详情
     *@param code code
     * @return ReportTemplate
     */
    ReportTemplate reportTemplateDetailByCode(String code);

}
