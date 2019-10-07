package com.sancai.oa.clockin.service;

import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.sancai.oa.clockin.entity.AttendanceRecordDepartment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 考勤记录和部门的对应关系表 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
public interface IAttendanceRecordDepartmentService extends IService<AttendanceRecordDepartment> {

    /**
     * 删除统计数据
     * @param attendanceRecordList
     */
    void attendanceStatisticDelete(List<AttendanceRecord> attendanceRecordList);

    /**
     * 删除统计积分
     * @param attendanceRecordId
     */
    void deleteAttendanceRecord(String attendanceRecordId);
}
