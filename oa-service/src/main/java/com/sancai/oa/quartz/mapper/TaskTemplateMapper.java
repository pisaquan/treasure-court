package com.sancai.oa.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.quartz.entity.TaskTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务模板 Mapper 接口
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Repository
public interface TaskTemplateMapper extends BaseMapper<TaskTemplate> {
    /**
     * 定时任务名称列表查询
     *
     * @return 返回集合
     */

    List<Map> getTaskTemplateNameList();
}
