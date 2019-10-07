package com.sancai.oa.examine.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.dingding.notify.DingDingNotifyService;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.sancai.oa.user.exception.EnumUserError;
import com.sancai.oa.user.exception.OaUserlException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * <p>
 * 请假 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine_leave")
public class ExamineLeaveController {

    @Autowired
    private IExamineLeaveService examineLeaveService;
    @Autowired
    private DingDingNotifyService dingDingNotifyService;

    /**
     * 抓取请假审批数据
     *
     * @param companyId
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse pullDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        if (StringUtils.isBlank(companyId)) {
            throw new ExamineException(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        examineLeaveService.pullLeaveExamineData(companyId,taskInstanceId);
        //病假带薪发送通知给员工
        dingDingNotifyService.sendToSickLeavePerson(companyId);
        return ApiResponse.success();
    }

    /**
     * 更新请假审批数据
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse updateDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        examineLeaveService.updateExamineData(taskInstanceId);
        //病假带薪发送通知给员工
        dingDingNotifyService.sendToSickLeavePerson(companyId);
        return ApiResponse.success();
    }


    /**
     * 查询请假列表，按请假开始时间倒序，分页
     *
     * @param requestEntity 请求体封装成实体类RequestEntity
     * @return 返回请假列表
     */
    @PostMapping("examine_leave_list")
    public ApiResponse getExamineLeaveList(@RequestBody RequestEntity requestEntity) {
        List<ExamineLeaveDTO> list = null;
        if (StringUtils.isEmpty(requestEntity.getCompanyId())){
            return ApiResponse.fail(EnumExamineError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            list = examineLeaveService.getExamineLeaveList(requestEntity);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_LEAVE_LIST_FAILURE);
        }
        return ApiResponse.success(new PageInfo<>(list));
    }

    /**
     * 请假详情查询
     *
     * @param id 前端请求的记录id
     * @return 返回该记录详情
     */
    @GetMapping("/examine_leave_detail/{id}")
    public ApiResponse getExamineLeaveDetail(@PathVariable String id) {
        ExamineLeaveDetailDTO examineLeaveDetail = null;
        if (StringUtils.isEmpty(id)){
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_ID_IS_EMPTY);
        }
        try {
            examineLeaveDetail = examineLeaveService.getExamineLeaveDetail(id);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_LEAVE_DETAIL_FAILURE);
        }
        if (examineLeaveDetail == null) {
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_DETAIL_IS_EMPTY);
        }
        return ApiResponse.success(examineLeaveDetail);

    }
    /**
     *
     *员工上传病例证明信息
     * @param
     */
    @PostMapping(value = "/uploads_medical_certificate")
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse uploadsMedicalCertificate(HttpServletRequest request , @RequestParam(value = "leaveId") String leaveId , @RequestParam(value = "userId") String userId) throws Exception{
        if(StringUtils.isBlank(leaveId)){
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_ID_IS_EMPTY);
        }
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        if(files == null || files.size() == 0){
            return ApiResponse.fail(EnumUserError.FILE_ISNULL);
        }
           examineLeaveService.uploadsMedicalCertificate(files,leaveId,userId);
           return  ApiResponse.success();
    }
    /**
     *
     *人事审核病例证明
     * @param
     */
    @PostMapping(value = "/check_medical_certificate")
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    @Log( type =  LogOperationTypeEnum.UPDATE,model = LogModelEnum.LEAVE)
    public ApiResponse checkMedicalCertificate(@RequestParam(value = "leaveId") String leaveId , @RequestParam(value = "status") String status){
        if(StringUtils.isBlank(leaveId) || StringUtils.isBlank(status)){
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_ID_IS_EMPTY);
        }
        examineLeaveService.checkMedicalCertificate(leaveId , status);
        return  ApiResponse.success();
    }
    /**
     * 请假详情查询图片
     *
     * @param leaveId 前端请求的记录id
     * @return 返回该记录详情
     */
    @GetMapping("/examine_leave_detail_img/{leaveId}")
    public ApiResponse getExamineLeaveDetailImg(@PathVariable String leaveId) {
        ExamineLeaveDetailDTO examineLeaveDetail = null;
        if (StringUtils.isEmpty(leaveId)){
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_ID_IS_EMPTY);
        }
        try {
            examineLeaveDetail = examineLeaveService.getExamineLeaveDetailImg(leaveId);
        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_LEAVE_DETAIL_FAILURE);
        }
        if (examineLeaveDetail == null) {
            return ApiResponse.fail(EnumExamineError.QUERY_LEAVE_DETAIL_IS_EMPTY);
        }
        return ApiResponse.success(examineLeaveDetail);

    }

}
