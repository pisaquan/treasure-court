package com.sancai.oa.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.ReportRecordDTO;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.score.entity.ActionScoreRecord;

import java.util.List;


/**
 * <p>
 * 日志记录 服务类
 * </p>
 *
 * @author  fans
 * @since 2019-07-19
 */
public interface IReportRecordService extends IService<ReportRecord> {


    /**
     * 导入日报记录数据（分段）
     *
     * @param companyId 公司id
     * @return boolean false导入失败，true 导入成功
     */
    boolean importEveryDayReportData(String taskInstanceId, String companyId) ;
    /**
     * 导入日报记录数据
     * @param companyId 公司id
     * @return  boolean false导入失败，true 导入成功
     */
    void importEveryDayReportData(String companyId ,long intervalTimeStart,long intervalTimeEnd,String taskInstanceId,boolean isFinish);

    /**
     *
     *
     * 获取子公司日志记录列表
     * @param reportRecordDTO 入参数据
     * @return ApiResponse
     */

    List<DataMap> recordListByCompany(ReportRecordDTO reportRecordDTO) throws Exception;

    /**
     * 获取子公司日志记录详情
     *
     * @param id 记录详情id
     * @return ApiResponse
     */
    DataMap reportDetail(String id);

    /**
     * 修改日志状态
     *
     * @param
     * @return ApiResponse
     */
    void recordStatusAmend(ReportRecord reportRecord);









}
