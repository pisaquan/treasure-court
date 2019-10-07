package com.sancai.oa.quartz.mapper;


import com.sancai.oa.quartz.entity.JobAndTrigger;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 查询任务相关信息
 * @author wangyl
 * @date 2019/7/30 13:08
 */
@Mapper
@Repository
public interface JobAndTriggerMapper {
    public List<JobAndTrigger> getJobAndTriggerDetails();
}
