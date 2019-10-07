package com.sancai.oa.clockin.mapper;

import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.clockin.entity.AttendanceRecordDTO;
import com.sancai.oa.clockin.entity.ClockinRecordMergeDTO;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 考勤结果 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@Repository
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {
    /**
     * 考勤结果列表
     * @param attendanceRecordDTO
     * @return
     */
    List<Map> attendanceRecordList(AttendanceRecordDTO attendanceRecordDTO);
    /**
     * 考勤详情
     *
     * @param id 考勤id
     * @return 返回集合
     */

    Map getAttendanceRecordDetail(String id);


    /**
     * 考勤缺卡点订正
     * @param id 考勤结果id
     * @return 返回考勤打卡记录的合并结果中的content和clockinId
     */
    Map<String,String> getContentById(String id);

    /**
     * 根据id查询信息封装ClockinRecordMergeDTO对象
     * @param id t_attendance_record表的id
     * @return ClockinRecordMergeDTO对象
     */
    ClockinRecordMergeDTO queryDTOById(String id);
    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> attendanceRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);

}
