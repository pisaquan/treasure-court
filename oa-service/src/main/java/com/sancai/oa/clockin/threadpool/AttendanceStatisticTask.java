package com.sancai.oa.clockin.threadpool;


import com.sancai.oa.clockin.entity.*;
import com.sancai.oa.clockin.enums.EnumClockinMergeStatus;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.service.IAttendanceRecordDepartmentService;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.score.entity.ActionUserScoreDTO;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 考勤统计
 */
public class AttendanceStatisticTask extends ThreadTask {
    private List<ClockinRecordMergeDTO> clockinRecordMergeDTOList;
    private int batch;
    private String taskInstanceId;
    private IActionScoreRecordService actionScoreRecordService;
    private IUserService userService;
    private IAttendanceRecordDepartmentService attendanceRecordDepartmentService;
    private IAttendanceRecordService attendanceRecordService;
    private RedisUtil redisUtil;

    public AttendanceStatisticTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.clockinRecordMergeDTOList = (List<ClockinRecordMergeDTO>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.actionScoreRecordService = (IActionScoreRecordService) getParam("actionScoreRecordService");
        this.userService = (IUserService) getParam("userService");
        this.attendanceRecordDepartmentService = (IAttendanceRecordDepartmentService) getParam("attendanceRecordDepartmentService");
        this.attendanceRecordService = (IAttendanceRecordService) getParam("attendanceRecordService");
        this.redisUtil = (RedisUtil) getParam("redisUtil");
    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     *
     * @return
     */
    @Override
    public boolean processTask() {
        try {
            clockinRecordMergeDTOList.stream().forEach(clockinRecordMergeDTO -> {
                StatisticAttendanceNumber attendanceNumber = new StatisticAttendanceNumber();
                AttendanceRecord attendanceRecord = new AttendanceRecord();
                attendanceRecord.setId(UUIDS.getID());
                attendanceRecord.setUserId(clockinRecordMergeDTO.getUserId());
                List<Integer> departMentList = new ArrayList<>();
                UserDTO user = userService.getUserByUserId(clockinRecordMergeDTO.getUserId(), clockinRecordMergeDTO.getCompanyId());
                if (user != null) {
                    List<UserDepartment> userDepartments = user.getUserDepartments();
                    if(CollectionUtils.isNotEmpty(userDepartments)){
                        departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                        //TODO 根据companyId + UserId + Time + taskInstanceId 缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                        String fetchKey =  OaMapUtils.fetchKey(clockinRecordMergeDTO.getCompanyId(), clockinRecordMergeDTO.getUserId(), clockinRecordMergeDTO.getStatisticalMonth().replaceAll(" ","" ), taskInstanceId);
                        String deptIds = (String) redisUtil.get(fetchKey);
                        if(StringUtils.isNotBlank(deptIds)){
                            departMentList = Arrays.asList(deptIds .split(",")).stream().map(s -> (Integer.valueOf(s.trim()))).collect(Collectors.toList());
                            //TODO 根据companyId + UserId + checkinTime + taskInstanceId 缓存删除
                            redisUtil.del(fetchKey);
                        }

                    }
                }
                departMentList.stream().forEach(departId -> {
                    AttendanceRecordDepartment attendanceRecordDepartment = new AttendanceRecordDepartment();
                    attendanceRecordDepartment.setId(UUIDS.getID());
                    attendanceRecordDepartment.setAttendanceRecordId(attendanceRecord.getId());
                    attendanceRecordDepartment.setDeptId(departId);
                    attendanceRecordDepartment.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                    attendanceRecordDepartment.setDeleted(0);
                    attendanceRecordDepartmentService.save(attendanceRecordDepartment);
                });
                attendanceRecord.setUserName(clockinRecordMergeDTO.getUserName());
                attendanceRecord.setCompanyId(clockinRecordMergeDTO.getCompanyId());
                attendanceRecord.setMonth(clockinRecordMergeDTO.getStatisticalMonth());
                String result = clockinRecordMergeDTO.getContent();
                if (StringUtils.isBlank(result)) {
                    return;
                }
                result = attendanceRecordService.sortContentByTime(result);
                Map<String, Object> finalDataMap = OaMapUtils.stringToMap(result);
                List<LocalDate> absenteeismDayList = new ArrayList<>();
                List<LocalDate> attendanceDayList = new ArrayList<>();
                for (Map.Entry<String, Object> entryMap : finalDataMap.entrySet()) {
                    //某一天的考勤打卡
                    List<Map<String, Object>> valMapList = (List) entryMap.getValue();
                    LocalDate currentDate = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.valueOf(entryMap.getKey())).toLocalDate();
                    attendanceDayList.add(currentDate);
                    //判断这一天是否旷工
                    boolean isAbsenteeism = false;
                    List<Map<String, Object>> valList = valMapList.stream().filter(v -> EnumClockinPointStatus.NOTSIGNED.getKey().equals(v.get(EnumClockinMergeStatus.STATUS.getKey()))).collect(Collectors.toList());
                    if(valMapList.size() == valList.size()){
                        isAbsenteeism = true;
                    }
                    //考勤组为4次
                    if (CollectionUtils.isNotEmpty(valMapList) && valMapList.size() == Integer.parseInt(EnumClockinMergeStatus.WORKINGFOUR.getKey())) {
                        attendanceRecordService.continuousAbsenteeismDaysFour(attendanceNumber, valMapList, attendanceRecord, departMentList, currentDate, absenteeismDayList,isAbsenteeism);
                    } else if (CollectionUtils.isNotEmpty(valMapList) && valMapList.size() == Integer.parseInt(EnumClockinMergeStatus.WORKINGTWO.getKey())) {
                        attendanceRecordService.continuousAbsenteeismDaysTwo(attendanceNumber, valMapList, attendanceRecord, departMentList, currentDate, absenteeismDayList,isAbsenteeism);
                    }
                }
                attendanceRecord.setNotSignedCount(attendanceNumber.getNotSignedCount() + attendanceNumber.getAbsenteeismNotSignedCount());
                attendanceRecord.setEarlyCount(attendanceNumber.getEarlyCount());
                attendanceRecord.setLateCount(attendanceNumber.getLateCount());
                String statisticalMonth = clockinRecordMergeDTO.getStatisticalMonth().trim() + "-01";
                LocalDate localDate = LocalDate.parse(statisticalMonth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int monthDays = localDate.lengthOfMonth();
                LocalDateTime firstDayOfMonth = LocalDateTimeUtils.getDayStart(
                        localDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
                LocalDateTime lastDayOfMonth = LocalDateTimeUtils.getDayEnd(
                        localDate.with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay());
                //根据统计结果计算旷工天数
                float absenteeismDays = attendanceRecordService.countAbsenteeismDays(attendanceNumber, firstDayOfMonth, lastDayOfMonth, attendanceRecord, absenteeismDayList, attendanceDayList);
                attendanceRecord.setAbsenteeismDays(absenteeismDays > monthDays ? monthDays : absenteeismDays);
                //根据统计结果计算计薪天数,1天旷工抵消三天计薪天数
                float salaryDays = attendanceDayList.size() - attendanceNumber.getNoPaidDays() - absenteeismDays * 3;
                attendanceRecord.setSalaryDays(salaryDays);
                //统计实际出勤天数,带薪请假天数
                float leaveDays = attendanceNumber.getSickLeaveDays() + attendanceNumber.getCasualLeaveDays() + attendanceNumber.getMaternityLeaveDays()
                        + attendanceNumber.getMarriageLeaveDays() + attendanceNumber.getFuneralLeaveDays();
                float attendanceDays = attendanceDayList.size() - attendanceNumber.getHolidayDays() - leaveDays - attendanceRecord.getNotAttendanceAbsenteeismDays();
                attendanceRecord.setAttendanceDays(attendanceDays);
                attendanceRecord.setBusinessTravelDays(attendanceNumber.getBusinessTravelDays());
                attendanceRecord.setHolidayDays(attendanceNumber.getHolidayDays());
                attendanceRecord.setSickLeaveDays(attendanceNumber.getSickLeaveDays());
                attendanceRecord.setUnpaidSickLeaveDays(attendanceNumber.getUnPaidSickLeaveDays());
                attendanceRecord.setPaidSickLeaveDays(attendanceNumber.getSickLeaveDays()-attendanceNumber.getUnPaidSickLeaveDays());
                //一个月最多2天带薪病假
                if(attendanceRecord.getPaidSickLeaveDays() > 2){
                    attendanceRecord.setSalaryDays(attendanceRecord.getSalaryDays()- attendanceRecord.getPaidSickLeaveDays() + 2);
                    attendanceRecord.setPaidSickLeaveDays(2f);
                    attendanceRecord.setUnpaidSickLeaveDays(attendanceRecord.getSickLeaveDays() - 2f);
                }
                attendanceRecord.setPersonalLeaveDays(attendanceNumber.getCasualLeaveDays());
                attendanceRecord.setChildbirthLeaveDays(attendanceNumber.getMaternityLeaveDays());
                attendanceRecord.setPaternityLeaveDays(attendanceNumber.getPaternityLeaveDays());
                attendanceRecord.setMarriageLeaveDays(attendanceNumber.getMarriageLeaveDays());
                attendanceRecord.setFuneralLeaveDays(attendanceNumber.getFuneralLeaveDays());
                ActionUserScoreDTO actionUserScoreDTO = actionScoreRecordService.queryUserScore(attendanceRecord.getUserId(), clockinRecordMergeDTO.getCompanyId(),
                        LocalDateTimeUtils.getMilliByTime(firstDayOfMonth), LocalDateTimeUtils.getMilliByTime(lastDayOfMonth));
                if (actionUserScoreDTO != null) {
                    Float score = actionUserScoreDTO.getScore();
                    if (score != null) {
                        attendanceRecord.setScore(score);
                    }
                } else {
                    attendanceRecord.setScore(0F);
                }
                attendanceRecord.setReportLowQualityCount(attendanceRecordService.disqualificationReportCount(attendanceRecord, firstDayOfMonth, lastDayOfMonth));
                attendanceRecord.setUserConfirm(0);
                attendanceRecord.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                attendanceRecord.setDeleted(0);
                attendanceRecord.setTaskInstanceId(taskInstanceId);
                attendanceRecordService.save(attendanceRecord);
            });
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId, "--" + batch + "==考勤合并异常：" + e.getMessage());
            return false;
        }
    }


    /**
     * 打卡数据按时间正序
     * @param content
     * @return
     */
    private String sortTime(String content) {
        Map<String, Object> map = OaMapUtils.stringToMap(content);
        map =OaMapUtils.sortByKey(map,false);
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            List<Map<String, Object>> values = (List<Map<String, Object>>) map.get(name);
            Collections.sort(values, new Comparator<Map<String, Object>>(){
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    String s1 = String.valueOf(o1.get("baseCheckTime"));
                    String s2 = String.valueOf(o2.get("baseCheckTime"));
                    return s1.compareTo(s2);
                }
            });
        }
        JSONObject jsonObject = JSONObject.fromObject(map);
        return jsonObject.toString();
    }
}
