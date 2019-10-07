package com.sancai.oa.examine.service;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.examine.entity.ActionPositionDataDTO;
import com.sancai.oa.examine.entity.ExamineBusinessTravel;
import com.sancai.oa.examine.entity.ExaminePosition;
import com.baomidou.mybatisplus.extension.service.IService;

import com.taobao.api.ApiException;

import com.sancai.oa.examine.entity.RequestEntity;

import java.util.List;


/**
 * <p>
 * 岗位奖罚 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public interface IExaminePositionService extends IService<ExaminePosition> {

    /**
     * 查询岗位考核列表，结果分页，按审批完成时间倒序
     * @param requestEntity 请求体封装的实体类
     * @return 返回查询列表的分页结果
     */
    List<ActionPositionDataDTO> getExamineListByPage(RequestEntity requestEntity);




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
     * 根据任务实例id查询岗位考核审批数据
     * @param instanceId
     * @return
     */
    List<ExaminePosition> examinePositionListByInstanceId(String instanceId);

    /**
     * 删除岗位考核审批数据
     * @param examinePositionList
     */
    void examinePositionDataDelete(List<ExaminePosition> examinePositionList);

    /**
     * 抓取岗位考核数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullPositionExamineData(String companyId,String taskInstanceId);


    /**
     * 岗位考核用户确认
     * @param id
     * @param userId
     */
    void examinePositionUserConfirm(String id,String userId);
}
