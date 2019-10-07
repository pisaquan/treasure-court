package com.sancai.oa.examine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.examine.entity.*;
import com.taobao.api.ApiException;

import java.util.List;


/**
 * <p>
 * 公休假 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
public interface IExamineHolidayService extends IService<ExamineHoliday> {

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
     * 查询公休列表
     *
     * @param requestEntity 前端请求体的封装实体类
     * @return 返回数据对象集合
     */
    List<ExamineHolidayDTO> getExamineHolidayList(RequestEntity requestEntity);


    /**
     * 根据公休记录id获取详情记录
     *
     * @param id 公休记录id
     * @return 返回一条公休记录详情
     */
    ExamineHolidayDetailDTO getExamineHolidayDetail(String id);

    /**
     * 根据任务实例id查询休假数据
     * @param instanceId
     * @return
     */
    List<ExamineHoliday> examineHolidayListByInstanceId(String instanceId);

    /**
     * 删除休假审批数据
     * @param examineHolidayList
     */
    void examineHolidayDataDelete(List<ExamineHoliday> examineHolidayList);

    /**
     * 抓取休假数据
     * @param companyId
     * @param taskInstanceId
     */
    void pullHolidayExamineData(String companyId,String taskInstanceId);

}
