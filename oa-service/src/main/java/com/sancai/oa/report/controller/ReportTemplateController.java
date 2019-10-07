package com.sancai.oa.report.controller;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;

import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.service.IReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 日志模板 前端控制器
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/report_template")
public class ReportTemplateController {

    @Autowired
    IReportTemplateService itReportTemplateService;


    /**
     * 日报模板列表
     *
     * @return List
     */

    @PostMapping("/template_list")
    public ApiResponse reportTemplateList(@RequestBody Map<String,Object> map){
            List<ReportTemplate> reportTemplates = itReportTemplateService.reportTemplateList(map);
            return ApiResponse.success(new PageInfo<>(reportTemplates));
    }
    /**
     * 日报模板详情
     *@param id 模板详情id
     * @return ReportTemplate
     */
    @GetMapping("/template_detail/{id}")
    public ApiResponse reportTemplateDetail(@PathVariable String id) {
        try {
            ReportTemplate reportTemplate = itReportTemplateService.reportTemplateDetail(id);
            if(reportTemplate !=null){
                return ApiResponse.success(reportTemplate);
            }
            return ApiResponse.fail(EnumReportError.REPORT_NOT_DATA);
        }catch (Exception e){
            throw new OaReportlException(EnumReportError.NO_OPERATION_OK);
        }
    }





    /**
     * @Title:导入企业的所有日志模板
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean importreportTemplate(String companyId ,String taskInstanceId ){
         //companyId= "F4754EF7878141A5A52C833769064D1F";
        return itReportTemplateService.importReportTemplateData(companyId,taskInstanceId);
    }

}

