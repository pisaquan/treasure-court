package com.sancai.oa.clockin.service;

import com.sancai.oa.clockin.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 考勤结果 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
public interface IAttendanceRecordService extends IService<AttendanceRecord> {


    /**
     * 修改考勤结果
     * @param clockinRecordMergeDTO
     */
    void updateAttendanceResult(ClockinRecordMergeDTO clockinRecordMergeDTO);

    /**
     * 考勤列表查询
     *
     * @param attendanceRecordDTO 封装请求体的实体类
     * @return 返回集合
     */

    List<Map> getAttendanceRecordList(AttendanceRecordDTO attendanceRecordDTO);

    /**
     * 考勤详情
     *
     * @param id 考勤id
     * @return 返回集合
     */

    Map getAttendanceRecordDetail(String id);
    /**
     * 考勤结果确认
     *
     * @param id 考勤id
     * @return 返回集合
     */

    Map attendanceRecordConfirm(String id,String user_id);

    /**
     * 考勤打卡点订正
     * @param id 考勤结果id
     * @param checkPointId 缺卡点当天的日期
     * @param day 缺卡点的id
     */
    int updateNotSignedPoint(String id, String checkPointId, Long day);

    /**
     * 分页统计考勤结果
     * @param companyId
     * @return
     */

    void pageStatisticAttendanceResult(String companyId ,String taskInstanceId);

    /**
     * 导出考勤结果Excel
     * @param downloadQueryConditionDTO 考勤结果导出Excel查询条件封装的实体类
     * @return 返回
     */
    void generateAttendanceExcel(DownloadQueryConditionDTO downloadQueryConditionDTO) throws NoSuchFieldException, IllegalAccessException;


    /**
     * 根据任务id查询统计数据
     * @param taskInstanceId
     * @return
     */
    List<AttendanceRecord> attendanceRecordByTaskId(String taskInstanceId);

   /**
     * 考勤结果发送
     * @param companyId
     * @param month
     */
    void sendToAttendanceConfirmation(String companyId ,String month);

    /**
     * 考勤结果发送（个人）
     * @param id
     */
    void sendToAttendanceConfirmationPerson(String id);

    /**
     * 考勤组为4次的打卡点数据统计
     * @param attendanceNumber
     * @param valMapList
     * @param attendanceRecord
     * @param departMentList
     * @param currentDate
     * @param absenteeismDayList
     */
    void continuousAbsenteeismDaysFour(StatisticAttendanceNumber attendanceNumber, List<Map<String, Object>> valMapList, AttendanceRecord attendanceRecord, List<Integer> departMentList, LocalDate currentDate, List<LocalDate> absenteeismDayList,boolean isAbsenteeism);

    /**
     * 考勤组为2次的打卡点数据统计
     * @param attendanceNumber
     * @param valMapList
     * @param attendanceRecord
     * @param departMentList
     * @param currentDate
     * @param absenteeismDayList
     */
    void continuousAbsenteeismDaysTwo(StatisticAttendanceNumber attendanceNumber, List<Map<String, Object>> valMapList, AttendanceRecord attendanceRecord, List<Integer> departMentList,LocalDate currentDate,List<LocalDate> absenteeismDayList,boolean isAbsenteeism);

    /**
     * 根据统计结果计算旷工天数
     * @param attendanceNumber
     * @param firstDayOfMonth
     * @param lastDayOfMonth
     * @param attendanceRecord
     * @param absenteeismDayList
     * @param attendanceDayList
     * @return
     */
    float countAbsenteeismDays(StatisticAttendanceNumber attendanceNumber, LocalDateTime firstDayOfMonth, LocalDateTime lastDayOfMonth, AttendanceRecord attendanceRecord, List<LocalDate> absenteeismDayList, List<LocalDate> attendanceDayList);

    /**
     * 统计本月的不合格日报数量
     * @param attendanceRecord
     * @param firstDayOfMonth
     * @param lastDayOfMonth
     * @return
     */
    int disqualificationReportCount(AttendanceRecord attendanceRecord, LocalDateTime firstDayOfMonth, LocalDateTime lastDayOfMonth);

    /**
     * 对json字符按日期进行排序
     * @param content
     * @return
     */
    String sortContentByTime (String content);
}
