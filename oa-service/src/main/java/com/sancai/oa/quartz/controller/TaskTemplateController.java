package com.sancai.oa.quartz.controller;


import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.quartz.exception.EnumTaskInstanceError;
import com.sancai.oa.quartz.exception.EnumTaskTemplateError;
import com.sancai.oa.quartz.mapper.TaskTemplateMapper;
import com.taobao.api.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务模板 前端控制器
 * </p>
 *
 * @author dancer
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@RequestMapping("{version}/task_template")
public class TaskTemplateController {
    @Autowired
    private  TaskTemplateMapper taskTemplateMapper;
    /**
     *
     * 定时任务模板列表
     *
     * @param
     * @return
     */
    @PostMapping("/template_list")
    public ApiResponse taskInstanceList() throws ApiException {

        List<Map> dataMaps = new ArrayList<>();
        try {
            dataMaps = taskTemplateMapper.getTaskTemplateNameList();
        } catch (Exception e) {
            return ApiResponse.fail(EnumTaskTemplateError.TASK_TASK_TEMPLATE_NOT_DATA);
        }
        return ApiResponse.success(new PageInfo<>(dataMaps));

    }
}

