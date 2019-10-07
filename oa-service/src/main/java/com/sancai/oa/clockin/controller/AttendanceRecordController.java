package com.sancai.oa.clockin.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.clockin.entity.AttendanceRecordDTO;
import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import com.sancai.oa.clockin.exception.EnumAttendanceRecordError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.signinconfirm.exception.OaSigninConfirmlException;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 考勤结果 前端控制器
 * 考勤结果统计 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/attendance_record")
public class AttendanceRecordController {

    @Autowired
    private IAttendanceRecordService iAttendanceRecordService;



    /**
     * 考勤打卡点订正
     *
     * @param map 请求体封装
     * @return 返回code message
     */
    @PostMapping("not_signed_point_update")
    @Log(type = LogOperationTypeEnum.UPDATE,model = LogModelEnum.ATTENDANCE)
    public ApiResponse updateNotSignedPoint(@RequestBody Map<String, Object> map) {
        String id = (String) map.get("id");
        Long day = (Long) map.get("day");
        String checkPointId = (String) map.get("check_point_id");
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(checkPointId)) {
            return ApiResponse.fail(EnumAttendanceRecordError.ATTENDANCE_RECORD_PARAM_EMPTY);
        }
        int i = iAttendanceRecordService.updateNotSignedPoint(id, checkPointId, day);
        if (i != 1) {
            return ApiResponse.fail(EnumAttendanceRecordError.UPDATE_NOT_SIGNED_POINT_FAILURE);
        }
        return ApiResponse.success();

    }

    /**
     * 统计考勤数据
     *
     * @param companyId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public ApiResponse statisticAttendanceResult(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        if (StringUtils.isBlank(companyId)) {
            throw new OaClockinlException(EnumAttendanceRecordError.PARAMETER_COMPANY_ID_IS_NULL);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            throw new OaClockinlException(EnumAttendanceRecordError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        iAttendanceRecordService.pageStatisticAttendanceResult(companyId, taskInstanceId);
        return ApiResponse.success();
    }


    /**
     * 公司考勤记录列表
     *
     * @param
     * @return
     */
    @PostMapping("/record_list")
    public ApiResponse clockinRecordList(@RequestBody AttendanceRecordDTO attendanceRecordDTO) throws ApiException {
        if (StringUtils.isBlank(attendanceRecordDTO.getCompanyId())) {
            return ApiResponse.fail(EnumAttendanceRecordError.ATTENDANCE_RECORD_PARAM_EMPTY);
        }
        if (StringUtils.isEmpty(attendanceRecordDTO.getPage() + "")) {
            return ApiResponse.fail(EnumAttendanceRecordError.ATTENDANCE_RECORD_PARAM_EMPTY);
        }
        if (StringUtils.isBlank(attendanceRecordDTO.getCapacity() + "")) {
            return ApiResponse.fail(EnumAttendanceRecordError.ATTENDANCE_RECORD_PARAM_EMPTY);
        }

        List<Map> dataMaps = null;
        try {
            dataMaps = iAttendanceRecordService.getAttendanceRecordList(attendanceRecordDTO);
        } catch (Exception e) {
            throw e;
        }

        if (dataMaps != null && dataMaps.size() > 0) {
            return ApiResponse.success(new PageInfo<>(dataMaps));
        }
        return ApiResponse.success(new PageInfo<>(new ArrayList<>()));
    }

    /**
     * 获取考勤详情
     *
     * @param id 考勤ID
     * @return
     */
    @GetMapping("/record_detail/{id}")
    public ApiResponse recordDetail(@PathVariable String id) {

        Map res = new HashMap();
        try {
            res = iAttendanceRecordService.getAttendanceRecordDetail(id);

        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumAttendanceRecordError.ATTENDANCE_RECORD_NOT_DATA);
        }

        //分页对象
        return ApiResponse.success(res);
    }

    /**
     * 考勤结果发送
     *
     * @param map 参数
     * @return
     */
    @PostMapping("/record_send")
    public ApiResponse recordSend(@RequestBody Map<String, Object> map) {
        String companyId = map.get("company_id") + "";
        String month = map.get("month") + "";
        try {
            //调用发送接口
            iAttendanceRecordService.sendToAttendanceConfirmation(companyId, month);
        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumAttendanceRecordError.ATTENDANCE_RECORD_CHK_REEOR);
        }

        //分页对象
        return ApiResponse.success();
    }
    /**
     * 考勤结果发送(个人)
     *
     * @param id 参数
     * @return
     */
    @GetMapping("/record_send_person/{id}")
    @Log(type = LogOperationTypeEnum.UPDATE, model = LogModelEnum.ATTENDANCE)
    public ApiResponse recordSendByPerson(@PathVariable String id) {

        try {
            //调用发送接口
            iAttendanceRecordService.sendToAttendanceConfirmationPerson(id);
        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumAttendanceRecordError.ATTENDANCE_RECORD_CHK_REEOR);
        }

        //分页对象
        return ApiResponse.success();
    }
    /**
     * 考勤结果确认
     *
     * @param map 考勤map
     * @return
     */
    @PostMapping("/record_confirm")
    public ApiResponse recordConfirm(@RequestBody Map<String, Object> map) {
        String id = map.get("id") + "";
        String userId = map.get("user_id") + "";
        try {
            iAttendanceRecordService.attendanceRecordConfirm(id, userId);
        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumAttendanceRecordError.ATTENDANCE_RECORD_CHK_REEOR);
        }

        //分页对象
        return ApiResponse.success();
    }
}