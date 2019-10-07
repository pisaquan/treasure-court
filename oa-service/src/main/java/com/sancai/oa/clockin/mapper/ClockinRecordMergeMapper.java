package com.sancai.oa.clockin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.clockin.entity.AttendanceComplexResultDTO;
import com.sancai.oa.clockin.entity.ClockinRecordMerge;
import com.sancai.oa.clockin.entity.ClockinRecordMergeDTO;
import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-02
 */
@Repository
public interface ClockinRecordMergeMapper extends BaseMapper<ClockinRecordMerge> {

    /**
     * 查询上个月的合并之后的考勤数据
     *
     * @param companyId
     * @param month
     * @return
     */
    List<ClockinRecordMergeDTO> selectClockinRecordMergeDTO(@Param("companyId") String companyId, @Param("month") String month);

    /**
     * 修改content后更新merge表
     *
     * @param recordMerge 修改后的content和对应的clockinId封装的对象
     */
    int updateContentByClockinId(ClockinRecordMerge recordMerge);

    /**
     * 根据条件查询筛选考勤结果
     *
     * @param downloadQueryConditionDTO 考勤结果导出Excel查询条件封装的实体类
     * @return 返回考勤复合结果集合
     */
    List<AttendanceComplexResultDTO> queryAttendanceComplexResult(DownloadQueryConditionDTO downloadQueryConditionDTO);

    /**
     * 查询员工这个月合并的考勤数据
     * @param companyId
     * @param month
     * @return
     */
    List<ClockinRecordMergeDTO> clockinRecordMergeByUserId(@Param("companyId")  String companyId, @Param("month") String month, @Param("userId") String userId);

}
