package com.sancai.oa.signin.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.entity.SigninRecordDTO;
import com.sancai.oa.signin.exception.EnumSigninError;
import com.sancai.oa.signin.exception.OaSigninlException;
import com.sancai.oa.signin.service.ISendOutingSigninTaskService;
import com.sancai.oa.signin.service.ISigninRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 签到记录 前端控制器
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/signin")
public class SigninRecordController {

    @Autowired
    private ISigninRecordService tSigninRecordService;
    @Autowired
    private ISendOutingSigninTaskService sendOutingSigninTaskService;



    /**
     *
     *
     * 分公司下的签到记录列表
     * @param signinRecordDTO
     * @return
     */
    @PostMapping("/signin_record_list")
    public ApiResponse signinRuleListByCompany(@RequestBody SigninRecordDTO signinRecordDTO) {
        try {
            List<DataMap> list = tSigninRecordService.signinRecordListByCompany(signinRecordDTO);
            return ApiResponse.success(new PageInfo<>(list));
        }catch (Exception e){
            throw new OaSigninlException(EnumSigninError.NO_OPERATION_OK);
        }
    }
    /**
     *
     *
     * 分公司下的签到记录详情
     * @param id
     * @return
     */
    @GetMapping("/signin_record_detail/{id}")
    public ApiResponse signinRuleListByCompany(@PathVariable String id) {
        try {
            SigninRecord tSigninRecord = tSigninRecordService.signinRuleDetail(id);
            if(tSigninRecord == null){
                return ApiResponse.fail(EnumSigninError.SIGNIN_NOT_DATA);
            }
            return ApiResponse.success(tSigninRecord);
        }catch (Exception e){
            throw new OaSigninlException(EnumSigninError.NO_OPERATION_OK);
        }
    }

    /**
     *
     * 签到数据导入
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean importEveryDayCheckinData(String companyId,String taskInstanceId) throws Exception{
        return tSigninRecordService.importEveryDayCheckinData(taskInstanceId,companyId);
    }

    /**
     *
     * 发送外出签到确认任务
     * @return
     */
    public boolean sendOutingSigninTask(String companyId,String taskInstanceId) throws Exception{
        try {
            //companyId  = "C5BD8B0F1F4C40AEA94E7A4294EEC228";
            //taskInstanceId = "60D02DEFE44D4EDEBDDACC981ADABFA8";
            return sendOutingSigninTaskService.sendOutingSignin(taskInstanceId, companyId);
        } catch (Exception e) {
            //异常撤回消息通知
            tSigninRecordService.recallNotify(taskInstanceId,companyId);
            throw e;
        }
    }
}

