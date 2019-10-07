package com.sancai.oa.examine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.examine.entity.*;
import com.taobao.api.ApiException;
import java.util.List;


/**
 * <p>
 * 出差 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public interface IExamineBusinessTravelService extends IService<ExamineBusinessTravel> {


    /**
     * 查询出差列表
     *
     * @param requestEntity 请求体封装的实体类
     * @return 返回出差列表分页结果集合
     */
    List<ExamineBusinessTravelDTO> getBusinessTravelList(RequestEntity requestEntity);


    /**
     * 处理审批数据
     *
     * @param taskInstanceId
     * @param companyId
     * @param processCode
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @throws ApiException
     */
    void dealExamineData(String taskInstanceId, String companyId, String processCode, long intervalTimeStart, long intervalTimeEnd);

    /**
     * 更新审批数据
     * 必须晚上更新
     */
    void updateExamineData(String taskInstanceId);


    /**
     * 查询出差记录详情
     *
     * @param id 出差记录
     * @return 返回出差详情
     */
    ExamineBusinessTravelDetailDTO getBusinessTravelDetails(String id);

    /**
     * 根据任务实例id查询出差数据
     * @param instanceId
     * @return
     */
    List<ExamineBusinessTravel> examineBusinessTravelListByInstanceId(String instanceId);

    /**
     * 删除出差审批数据
     * @param examineBusinessTravelList
     */
    void examineBusinessTravelDataDelete(List<ExamineBusinessTravel> examineBusinessTravelList);

    /**
     * 抓取出差数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullBusinessTravelExamineData(String companyId,String taskInstanceId);

}
