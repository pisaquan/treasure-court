package com.sancai.oa.quartz.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.exception.EnumTaskInstanceError;
import com.sancai.oa.quartz.exception.EnumTaskTemplateError;
import com.sancai.oa.quartz.mapper.TaskInstanceMapper;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import com.sancai.oa.signinconfirm.exception.OaSigninConfirmlException;
import com.sancai.oa.typestatus.enums.TimedTaskKeyEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.taobao.api.ApiException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务实例 前端控制器
 * </p>
 *
 * @author dancer
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/task")
public class TaskInstanceController {
    @Autowired
    ITaskInstanceService iTaskInstanceService;

    @Autowired
    private TaskInstanceMapper taskInstanceMapper;

    @Autowired
    private ITaskTemplateService taskTemplateService;

    @Autowired
    private ICompanyService companyService;

    /**
     *
     * 定时任务列表
     *
     * @param
     * @return
     */
    @PostMapping("/task_list")
    public ApiResponse taskInstanceList(@RequestBody TaskInstanceDTO taskInstanceDTO) throws ApiException {
        if (StringUtils.isBlank(taskInstanceDTO.getCompanyId())) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_PARAMETER_IS_NULL);
        }
        if (StringUtils.isEmpty(taskInstanceDTO.getPage()+"")) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_PARAMETER_IS_NULL);
        }
        if (StringUtils.isBlank(taskInstanceDTO.getCapacity()+"")) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_PARAMETER_IS_NULL);
        }
        List<Map> dataMaps = new ArrayList<>();
        try {
            dataMaps = iTaskInstanceService.getTaskInstanceList(taskInstanceDTO);
        } catch (Exception e) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_NO_OPERATION_OK);
        }
        return ApiResponse.success(new PageInfo<>(dataMaps));

    }

    /**
     * 定时任务详情
     *
     * @param id 定时任务ID
     * @return
     */
    @GetMapping("/task_detail/{id}")
    public ApiResponse taskInstanceDetail(@PathVariable String id) {

        Map res = new HashMap();
        try {
            res = iTaskInstanceService.getTaskInstanceDetail(id);

        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumTaskInstanceError.TASK_INSTANCE_NO_OPERATION_OK);
        }

        //分页对象
        return ApiResponse.success(res);
    }
    /**
     * 定时任务重试
     *
     * @param id 定时任务ID
     * @return
     */
    @GetMapping("/task_retry/{id}")
    public ApiResponse taskRetry(@PathVariable String id) {
        String status = "";
        try {
            if(iTaskInstanceService.taskPreRetry(id)){
                iTaskInstanceService.taskRetry(id);
                status = "开始执行";
            }else {
                status = "执行中";
            }

        } catch (Exception e) {
            throw new OaSigninConfirmlException(EnumTaskInstanceError.TASK_INSTANCE_NO_OPERATION_OK);
        }

        //分页对象
        return ApiResponse.success(status);
    }

    @PostMapping("/save_task")
    public ApiResponse saveTask(@RequestBody TaskInstanceDTO taskInstanceDTO){
        String companyId = taskInstanceDTO.getCompanyId();
        String key = TimedTaskKeyEnum.valueOf(taskInstanceDTO.getTaskKey()).getKey();
        Long startTime = taskInstanceDTO.getStartTime();
        Long endTime = taskInstanceDTO.getEndTime();
        if (StringUtils.isBlank(companyId) || StringUtils.isBlank(key) || startTime == null || endTime == null) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_PARAMETER_IS_NULL);
        }
        //判断是否有该时间段的数据
        List<TaskInstance> taskInstanceList = taskInstanceMapper.getInstanceListByTime(companyId,key,startTime, endTime);
        if(CollectionUtils.isNotEmpty(taskInstanceList)){
            return ApiResponse.fail(EnumTaskTemplateError.TASK_TIMEZONE_IS_REPETITION);
        }
        QueryWrapper<TaskTemplate> wrapper = new QueryWrapper<>();
        wrapper.eq("task_key",key);
        TaskTemplate taskTemplate = taskTemplateService.getOne(wrapper);
        if(taskTemplate == null){
            return ApiResponse.fail(EnumTaskTemplateError.TASK_TASK_TEMPLATE_NOT_DATA);
        }
        Company company =  companyService.companyDetail(companyId);
        if(company == null){
            return ApiResponse.fail(EnumCompanyError.COMPANY_NOT_DATA);
        }
        try {
            TaskInstance taskInstance = iTaskInstanceService.saveTask(companyId, key,taskTemplate,company,TimedTaskStatusEnum.FAILURE, startTime,endTime);
            return ApiResponse.success(taskInstance);
        } catch (Exception e) {
            return ApiResponse.fail(EnumTaskInstanceError.TASK_INSTANCE_NO_OPERATION_OK);
        }


    }

}

