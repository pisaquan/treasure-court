package com.sancai.oa.clockin.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.clockin.entity.ClockinRecordDTO;
import com.sancai.oa.clockin.exception.EnumClockinError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.report.entity.modify.DataMap;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 考勤打卡记录 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-01
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/clockin")
public class ClockinRecordController {

    @Autowired
    IClockinRecordService itClockinRecordService;

    @Autowired
    ClockinService clockinService;
    /**
     * 抓取打卡数据
     *
     * @param companyId
     * @param taskInstanceId
     * @return
     * @throws ApiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse graspClockinRecord(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) throws Exception {
        if (StringUtils.isBlank(companyId)) {
            return ApiResponse.fail(EnumClockinError.PARAMETER_COMPANY_ID_IS_NULL);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            return ApiResponse.fail(EnumClockinError.TASK_INSTANCE_ID_IS_EMPTY);
        }

        clockinService.graspClockinRecord(taskInstanceId,companyId);
        return ApiResponse.success();
    }
    /**
     *
     *
     * 公司打卡记录列表
     * @param clockinRecordDTO
     * @return
     */
    @PostMapping("/clockin_list")
    public ApiResponse clockinRecordList(@RequestBody ClockinRecordDTO clockinRecordDTO){
            List<DataMap> dataMaps = itClockinRecordService.clockinRecordList(clockinRecordDTO);
            return ApiResponse.success(new PageInfo<>(dataMaps));
    }
    /**S
     *
     *
     * 公司打卡记录详情
     * @param id
     * @return
     */
    @GetMapping("/clockin_detail/{id}")
    public ApiResponse clockinRecordDetail(@PathVariable String id) {
        try {
            ClockinRecordDTO clockinRecord = itClockinRecordService.clockinRecordDetail(id);
            if(clockinRecord!=null){
                return  ApiResponse.success(clockinRecord);
            }
            return ApiResponse.fail(EnumClockinError.CLOCKIN_NOT_DATA);
        }catch (Exception e){
            throw new OaClockinlException(EnumClockinError.NO_OPERATION_OK);
        }
    }

}
