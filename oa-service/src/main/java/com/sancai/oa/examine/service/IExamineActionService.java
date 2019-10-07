package com.sancai.oa.examine.service;


import com.sancai.oa.examine.entity.ExamineAction;
import com.sancai.oa.examine.entity.ExaminePosition;
import com.sancai.oa.report.entity.modify.DataMap;
import com.taobao.api.ApiException;

import com.sancai.oa.examine.entity.ActionPositionDataDTO;
import com.sancai.oa.examine.entity.RequestEntity;

import java.util.List;


/**
 * @author fanjing
 * @date 2019/7/26
 * @description 岗位列表查询
 */
public interface IExamineActionService {


    /**
     * 查询行为考核列表
     * @param requestEntity 请求体的封装类
     * @return 返回行为列表集合
     */
    List<ActionPositionDataDTO> getExamineActionList(RequestEntity requestEntity);



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
     * 根据任务实例id查询行为考核审批数据
     * @param instanceId
     * @return
     */
    List<ExamineAction> examineActionListByInstanceId(String instanceId);

    /**
     * 删除行为考核审批数据
     * @param examineActionList
     */
    void examineActionDataDelete(List<ExamineAction> examineActionList);

    /**
     * 抓取行为考核数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullActionExamineData(String companyId,String taskInstanceId);


    /**
     * 行为考核用户确认
     * @param id
     * @param userId
     */
    void examineActionUserConfirm(String id,String userId);

    /**
     * 根据process_instance_id批审实例id + taskInstanceId 或 FormValueUserId 把记录对应的部门集合存入缓存
     *
     * @param recordLists 记录集合
     */
    void recordOriginDeptIdsSaveRedis(List<DataMap> recordLists);

    /**
     * 根据process_instance_id批审实例id + taskInstanceId 或 FormValueUserId 表单用户id 缓存取出记录对应的部门集
     */
    List<Integer> getRecordOriginDeptIdsSaveRedis(String processInstanceId, String taskInstanceId ,String formValueUserId);
}


