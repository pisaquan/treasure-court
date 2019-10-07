package com.sancai.oa.examine.controller;

import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.service.IExamineOutApplyService;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 外出申请 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine_out_apply")
public class ExamineOutApplyController {

    @Autowired
    private IExamineOutApplyService examineOutApplyService;

    /**
     * 抓取外出申请审批数据
     * @param companyId
     * @param taskInstanceId
     * @return
     * @throws ApiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse pullDingTalkExamineData(@RequestParam("companyId") String companyId,@RequestParam("taskInstanceId") String taskInstanceId){
        if(StringUtils.isBlank(companyId)){
            throw new ExamineException(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        if(StringUtils.isBlank(taskInstanceId)){
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        examineOutApplyService.pullOutApplyExamineData(companyId,taskInstanceId);
        return ApiResponse.success();
    }

    /**
     * 更新外出申请审批数据
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse updateDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        examineOutApplyService.updateExamineData(taskInstanceId);
        return ApiResponse.success();
    }
}
