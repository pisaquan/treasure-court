package com.sancai.oa.signinconfirm.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.quartz.exception.EnumTaskInstanceError;
import com.sancai.oa.signinconfirm.exception.EnumSigninConfirmError;
import com.sancai.oa.signinconfirm.exception.OaSigninConfirmlException;
import com.sancai.oa.signinconfirm.service.ISigninConfirmService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author wangyl
 * @since 2019-08-02
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/signin")
public class SigninConfirmController {

    @Autowired
    private ISigninConfirmService signinConfirmService;

    /**
     * 签到确认列表
     *
     * @param map
     * @return
     */
    @PostMapping("/signin_confirm_list")
    public ApiResponse signinConfirmListByCompany(@RequestBody Map<String, Object> map) {
        if (StringUtils.isBlank(map.get("page")+"")) {
            return ApiResponse.fail(EnumSigninConfirmError.SIGNINCONFIRM_PARAMETER_IS_NULL);
        }
        if (StringUtils.isBlank(map.get("capacity")+"")) {
            return ApiResponse.fail(EnumSigninConfirmError.SIGNINCONFIRM_PARAMETER_IS_NULL);
        }
        if (StringUtils.isBlank(map.get("company_id")+"")) {
            return ApiResponse.fail(EnumSigninConfirmError.SIGNINCONFIRM_PARAMETER_IS_NULL);
        }

        List<Map> res = new ArrayList<Map>();
        try {
            res = signinConfirmService.signinConfirmListByCompany(map);
        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NO_OPERATION_OK);
        }
        return ApiResponse.success(new PageInfo<>(res));
    }

    /**
     * 签到确认详情
     *
     * @param id
     * @return
     */
    @GetMapping("/signin_confirm_detail/{id}")
    public ApiResponse signinConfirmDetailById(@PathVariable String id){
        Map res = new HashMap();
        try {
            res = signinConfirmService.signinConfirmDetailById(id);
        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumSigninConfirmError.SIGNINCONFIRM_NO_OPERATION_OK);
        }
        return ApiResponse.success(res);
    }

    /**
     * 确认签到状态
     * @param map 签到map
     * @return
     */
    @PostMapping("/signin_record_status_modfy")
    @Log(type = LogOperationTypeEnum.UPDATE, model = LogModelEnum.SIGNIN_CONFIRM)
    public ApiResponse signinConfirm(@RequestBody Map<String, Object> map) throws Exception{
        String id= map.get("id")+"";
        String status = map.get("status")+"";

        Object user_id = map.get("user_id");
        String userId ="";
        if(null!=user_id){
            userId = user_id.toString();
        }
        Object admin_id = map.get("admin_id");
        String adminId ="";
        if(null!=admin_id){
            adminId = admin_id.toString();
        }
        Object attendance_id = map.get("attendance_id");
        String attendanceId ="";
        if(null!=attendance_id){
            attendanceId = attendance_id.toString();
        }
        try {
             signinConfirmService.signinConfirm(id,status,userId,adminId,attendanceId);
        } catch (Exception e) {
            throw e;
        }
        return ApiResponse.success();
    }
}

