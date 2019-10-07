package com.sancai.oa.report.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.report.entity.ReportRecordDTO;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 日志记录 Mapper 接口
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Repository
public interface ReportRecordMapper extends BaseMapper<ReportRecord> {

    /**
     * 根据主键查询详情
     * @param id
     * @return
     */
    DataMap selectReportDetailsByPrimary(@Param(value = "id") String id);

    /**
     * 公司下的日志记录
     * @param map
     * @return
     */
    List<DataMap> reportRecordListByCompanyId(Map<String, Object> map);
    /**
     * 公司某部门下的日志记录
     * @param map
     * @return
     */
    List<DataMap> reportRecordListByDeptId(Map<String, Object> map);

    /**
     * 公司下的日志记录
     * @param reportRecordDTO
     * @return
     */
    List<DataMap> reportRecordList(ReportRecordDTO reportRecordDTO);

    /**
     * 修改日志记录状态
     * @param reportRecord
     */
    int updateReportRecordStateById(ReportRecord reportRecord);


    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> reportRecordDeptIdList(@Param(value = "companyId") String companyId ,@Param(value = "taskInstanceId") String taskInstanceId);


}
