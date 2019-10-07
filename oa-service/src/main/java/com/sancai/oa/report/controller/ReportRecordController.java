package com.sancai.oa.report.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.ReportRecordDTO;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.service.IReportRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 日志记录 前端控制器
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/report_record")
public class ReportRecordController {
    @Autowired
    private IReportRecordService itReportRecordService;

    /**
     * 导入日报记录数据(之前必须导入日报模板数据)
     *
     * @return: false导入失败，true 导入成功
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean importEveryDayReportData(String companyId ,String taskInstanceId ){
        return itReportRecordService.importEveryDayReportData(taskInstanceId,companyId);
    }

    /**
     * 获取子公司日志记录列表
     *
     * @return
     */
    @PostMapping("/record_list")
    public ApiResponse recordListByCompany(@RequestBody ReportRecordDTO reportRecordDTO) throws Exception {
            List<DataMap>  list = itReportRecordService.recordListByCompany(reportRecordDTO);
            return ApiResponse.success(new PageInfo<>(list));
    }
    /**
     * 获取子公司日志记录详情
     * @return
     */
    @GetMapping("/report_detail/{id}")
    public ApiResponse reportDetail(@PathVariable String id) {
        try {
            DataMap dataMap = itReportRecordService.reportDetail(id);
            if(dataMap!=null){
                return ApiResponse.success(dataMap);
            }
            return ApiResponse.fail(EnumReportError.REPORT_NOT_DATA);
        }catch (Exception e){
            throw new OaReportlException(EnumReportError.NO_OPERATION_OK);
        }
    }
    /**
     * 日报状态修正
     *
     * @return
     */
    @PostMapping("/record_status_amend")
    @Log( type =  LogOperationTypeEnum.UPDATE,model = LogModelEnum.REPORT)
    public ApiResponse recordStatusAmend(@RequestBody ReportRecord reportRecord){
         itReportRecordService.recordStatusAmend(reportRecord);
        return ApiResponse.success();
    }
}

