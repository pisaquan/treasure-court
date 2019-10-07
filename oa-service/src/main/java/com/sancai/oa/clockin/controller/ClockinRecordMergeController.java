package com.sancai.oa.clockin.controller;


import com.sancai.oa.clockin.exception.EnumAttendanceRecordError;
import com.sancai.oa.clockin.exception.EnumClockinError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  考勤记录合并前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-02
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/clockinRecordMerge")
public class ClockinRecordMergeController {

    @Autowired
    private IClockinRecordMergeService clockinRecordMergeService;

    /**
     * 合并考勤记录
     * @param companyId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ApiResponse consolidatedAttendanceData(@RequestParam("companyId") String companyId,@RequestParam("taskInstanceId") String taskInstanceId) {
        if (StringUtils.isBlank(companyId)) {
            throw new OaClockinlException(EnumAttendanceRecordError.PARAMETER_COMPANY_ID_IS_NULL);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            throw new OaClockinlException(EnumAttendanceRecordError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        clockinRecordMergeService.consolidatedAttendanceData(companyId,taskInstanceId);
        return ApiResponse.success();
    }
}
