package com.sancai.oa.examine.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.log.config.Log;
import com.sancai.oa.log.config.LogModelEnum;
import com.sancai.oa.log.config.LogOperationTypeEnum;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行为奖罚 前端控制器
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Slf4j
@ApiVersion(1)
@RestController
@RequestMapping("{version}/examine_action")
public class ExamineActionController {

    @Autowired
    private IExamineActionService examineActionService;

    /**
     * 抓取行为考核审批数据
     *
     * @param companyId
     * @param taskInstanceId
     * @return
     * @throws ApiException
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse pullDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        if (StringUtils.isBlank(companyId)) {
            throw new ExamineException(EnumExamineError.COMPANY_ID_IS_EMPTY);
        }
        if (StringUtils.isBlank(taskInstanceId)) {
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_ID_IS_EMPTY);
        }
        examineActionService.pullActionExamineData(companyId,taskInstanceId);
        return ApiResponse.success();
    }

    /**
     * 更新行为考核审批数据
     * @param taskInstanceId
     * @return
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    public ApiResponse updateDingTalkExamineData(@RequestParam("companyId") String companyId, @RequestParam("taskInstanceId") String taskInstanceId) {
        examineActionService.updateExamineData(taskInstanceId);
        return ApiResponse.success();

    }

    /**
     * 行为考核列表查询
     *
     * @param requestEntity
     * @return
     */
    @PostMapping("examine_action_list")
    public ApiResponse getExamineActionList(@RequestBody RequestEntity requestEntity) {
        List<ActionPositionDataDTO> list = null;
        if (StringUtils.isEmpty(requestEntity.getCompanyId())){
            return ApiResponse.fail(EnumExamineError.QUERY_PARAM_COMPANYID_IS_EMPTY);
        }
        try {
            list = examineActionService.getExamineActionList(requestEntity);

        } catch (Exception e) {
            throw new ExamineException(EnumExamineError.QUERY_EXAMINE_ACTION_LIST_FAILED);
        }
        return ApiResponse.success(new PageInfo<>(list));


    }

    /**
     * 行为考核用户确认
     *
     * @param map
     * @return
     */
    @PostMapping("examine_action_user_confirm")
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class, OaException.class})
    @Log(type = LogOperationTypeEnum.UPDATE, model = LogModelEnum.EXAMINE_ACTION)
    public ApiResponse examineActionUserConfirm(@RequestBody Map<String, Object> map) {
        String id = (String) map.get("id");
        String userId = (String) map.get("user_id");

        try {
            examineActionService.examineActionUserConfirm(id, userId);
        } catch (Exception e) {
            log.error("行为考核用户确认失败 ,失败原因为 : " +e.getMessage());
            StackTraceElement[] stackTraceElements = e.getCause().getStackTrace();
            for (StackTraceElement se:stackTraceElements){
                log.error("class name →" +se.getClassName()+"， filename   →" + se.getFileName() +" ， methodName →" + se.getMethodName() + " , lineNum → " + se.getLineNumber()+System.getProperty("line.separator"));
            }
            throw new ExamineException(EnumExamineError.CONFIRM_ACTION_LIST_FAILED);
        }

        return ApiResponse.success();
    }


}
