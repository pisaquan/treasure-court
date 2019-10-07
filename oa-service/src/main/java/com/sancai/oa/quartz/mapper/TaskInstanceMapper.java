package com.sancai.oa.quartz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.typestatus.enums.TimedTaskKeyEnum;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 任务实例 Mapper 接口
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Repository
public interface TaskInstanceMapper extends BaseMapper<TaskInstance> {

    /**
     * 一个公司的一个模板的最后一个成功的任务
     * @param taskTemplateId
     * @param companyId
     * @return
     */
    TaskInstance getLastSuccessTaskInstance(@Param("taskTemplateId") String taskTemplateId, @Param("companyId")  String companyId);

    /**
     * 一个公司的一个模板的指定时间的最后一个成功的任务
     * @param taskTemplateId
     * @param companyId
     * @return
     */
    TaskInstance getScheduleTimeLastSuccessTaskInstance(@Param("taskTemplateId") String taskTemplateId, @Param("companyId")  String companyId,@Param("createTime") long createTime);

    /**
     * 定时任务列表查询
     *
     * @param taskInstanceDTO 封装请求体的实体类
     * @return 返回集合
     */

    List<Map> getTaskInstanceList(TaskInstanceDTO taskInstanceDTO);

    /**
     * 定时任务详情
     *
     * @param id 定时任务id
     * @return 返回集合
     */

    Map getTaskInstanceDetail(String id);

    /**
     * 查询表中指定公司的指定模板类型中是否已经存在该时间段的数据
     * @param companyId
     * @param key
     * @param startTime
     * @param endTime
     * @return
     */
    List<TaskInstance> getInstanceListByTime(@Param("companyId") String companyId,@Param("key") String key,@Param("startTime") Long startTime,@Param("endTime") Long endTime);

    /**
     * 清理quartz内容
     */
    void clearDetails();
    /**
     * 清理quartz内容
     */
    void clearTriggers();
    /**
     * 清理quartz内容
     */
    void clearFiredTriggers();
    /**
     * 清理quartz内容
     */
    void clearCronTriggers();
}
