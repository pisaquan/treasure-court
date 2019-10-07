package com.sancai.oa.clockin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.clockin.entity.AttendanceComplexResultDTO;
import com.sancai.oa.clockin.entity.ClockinRecordMerge;
import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-02
 */
public interface IClockinRecordMergeService extends IService<ClockinRecordMerge> {


    /**
     * 合并考勤记录
     *
     * @param companyId
     */
    void consolidatedAttendanceData(String companyId, String taskInstanceId);


    /**
     * 根据条件查询筛选考勤结果
     *
     * @param downloadQueryConditionDTO 考勤结果导出Excel查询条件封装的实体类
     * @return 返回考勤复合结果集合
     */
    List<AttendanceComplexResultDTO> queryAttendanceComplexResult(DownloadQueryConditionDTO downloadQueryConditionDTO);

    /**
     * 根据任务id删除考勤数据
     * @param taskInstanceId
     */
    void attendanceDataDelete(String taskInstanceId);

    /**
     * 通过任务ID查询合并记录
     * @param taskInstanceId
     * @return
     */
    List<ClockinRecordMerge> recordMergeByTaskId(String taskInstanceId);


}
