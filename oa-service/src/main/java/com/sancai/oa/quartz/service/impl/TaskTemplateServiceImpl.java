package com.sancai.oa.quartz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.mapper.TaskTemplateMapper;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务模板 服务实现类
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Service
public class TaskTemplateServiceImpl extends ServiceImpl<TaskTemplateMapper, TaskTemplate> implements ITaskTemplateService {

}
