package com.sancai.oa.examine.service;

import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.ExamineOutApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.taobao.api.ApiException;

import java.util.List;

/**
 * <p>
 * 外出申请 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
public interface IExamineOutApplyService extends IService<ExamineOutApply> {

    /**
     * 处理审批数据
     * @param taskInstanceId
     * @param companyId
     * @param processCode
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @throws ApiException
     */
    void dealExamineData(String taskInstanceId,String companyId,String processCode,long intervalTimeStart,long intervalTimeEnd);

    /**
     * 更新审批数据
     * 必须晚上更新
     */
    void updateExamineData(String taskInstanceId);

    /**
     * 根据任务实例id查询外出申请数据
     * @param instanceId
     * @return
     */
    List<ExamineOutApply> examineOutApplyListByInstanceId(String instanceId);

    /**
     * 删除外出申请审批数据
     * @param taskInstanceId
     */
    void examineOutApplyDataDelete(String taskInstanceId);

    /**
     * 抓取外出申请数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullOutApplyExamineData(String companyId,String taskInstanceId);
}
