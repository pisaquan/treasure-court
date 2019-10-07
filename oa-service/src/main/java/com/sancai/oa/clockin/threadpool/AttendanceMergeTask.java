package com.sancai.oa.clockin.threadpool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordMerge;
import com.sancai.oa.clockin.enums.EnumClockinMergeStatus;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.clockin.exception.EnumClockinError;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.examine.entity.ExamineBusinessTravel;
import com.sancai.oa.examine.entity.ExamineHoliday;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.ExamineTypeEnum;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;
import com.sancai.oa.examine.service.IExamineBusinessTravelService;
import com.sancai.oa.examine.service.IExamineHolidayService;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.examine.utils.MapUtils;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.ReportDepartment;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.entity.enums.ReportRecordTypeEnum;
import com.sancai.oa.report.service.IReportRecordService;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.ActionScoreRule;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.score.service.IActionScoreRuleService;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.entity.enums.SigninTypeEnum;
import com.sancai.oa.signin.mapper.SigninRecordMapper;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 考勤合并
 */
public class AttendanceMergeTask extends ThreadTask {
    private String companyId;
    private List<ClockinRecord> clockinRecords;
    private int batch;
    private String taskInstanceId;
    private IExamineHolidayService examineHolidayService;
    private IExamineBusinessTravelService examineBusinessTravelService;
    private IExamineLeaveService examineLeaveService;
    private SigninRecordMapper signinRecordMapper;
    private IReportRecordService reportRecordService;
    private IActionScoreRecordService actionScoreRecordService;
    private IActionScoreRuleService actionScoreRuleService;
    private IActionScoreDepartmentService actionScoreDepartmentService;
    private IClockinRecordMergeService clockinRecordMergeService;
    private IUserService userService;
    private IActionScoreDeductService actionScoreDeductService;
    private List<ReportTemplate> reportTemplateList;
    private RedisUtil redisUtil;
    private String taskInstanceStatus;
    private List<ReportRecord> reportRecordList;

    public AttendanceMergeTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }

    @Override
    public void initParam() {
        this.clockinRecords = (List<ClockinRecord>) obj;
        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.examineHolidayService = (IExamineHolidayService) getParam("examineHolidayService");
        this.examineBusinessTravelService = (IExamineBusinessTravelService) getParam("examineBusinessTravelService");
        this.examineLeaveService = (IExamineLeaveService) getParam("examineLeaveService");
        this.signinRecordMapper = (SigninRecordMapper) getParam("signinRecordMapper");
        this.reportRecordService = (IReportRecordService) getParam("reportRecordService");
        this.actionScoreRecordService = (IActionScoreRecordService) getParam("actionScoreRecordService");
        this.actionScoreRuleService = (IActionScoreRuleService) getParam("actionScoreRuleService");
        this.actionScoreDepartmentService = (IActionScoreDepartmentService) getParam("actionScoreDepartmentService");
        this.clockinRecordMergeService = (IClockinRecordMergeService) getParam("clockinRecordMergeService");
        this.userService = (IUserService) getParam("userService");
        this.actionScoreDeductService = (IActionScoreDeductService) getParam("actionScoreDeductService");
        this.reportTemplateList = (List<ReportTemplate>) getParam("reportTemplateList");
        this.redisUtil = (RedisUtil) getParam("redisUtil");
        this.taskInstanceStatus = (String) getParam("taskInstanceStatus");
        this.reportRecordList = (List<ReportRecord>) getParam("reportRecordList");


    }


    /**
     * 执行任务,返回false表示任务执行错误，需要回滚
     *
     * @return
     */
    @Override
    public boolean processTask() {
        try {

            for (ClockinRecord clockinRecord : clockinRecords) {
                TaskMessage.addMessage(taskInstanceId, clockinRecord.getUserName() + "-" + clockinRecord.getMonth());
                ClockinRecordMerge clockinRecordMerge = new ClockinRecordMerge();
                clockinRecordMerge.setId(UUIDS.getID());
                clockinRecordMerge.setClockinId(clockinRecord.getId());
                clockinRecordMerge.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                clockinRecordMerge.setDeleted(0);
                String content = clockinRecord.getContent();
                if (StringUtils.isBlank(content)) {
                    continue;
                }
                Map<String, Object> originalDataMap = MapUtils.stringToMap(content);
                List<LocalDate> absenteeismDayList = new ArrayList<>();
                List<LocalDate> attendanceDayList = new ArrayList<>();
                originalDataMap.forEach((k, v) -> {
                    List<Map<String, Object>> valMapList = (List) v;
                    LocalDate currentDate = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.valueOf(k)).toLocalDate();
                    attendanceDayList.add(currentDate);
                    //一天内缺卡计数器
                    int counter = 0;
                    for (Map<String, Object> val : valMapList) {
                        if (!MapUtils.mapIsAnyBlank(val, EnumClockinMergeStatus.STATUS.getKey(), EnumClockinMergeStatus.ID.getKey(), EnumClockinMergeStatus.BASECHECKTIME.getKey())) {
                            //本次打卡基准时间
                            Long baseTime = Long.parseLong(val.get(EnumClockinMergeStatus.BASECHECKTIME.getKey()).toString());
                            String userId = clockinRecord.getUserId();
                            if (!EnumClockinPointStatus.NORMAL.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                                //签到合并逻辑开始
                                Long clockinId = Long.parseLong(val.get(EnumClockinMergeStatus.ID.getKey()).toString());
                                QueryWrapper<SigninRecord> signinRecordWrapper = new QueryWrapper<>();
                                signinRecordWrapper.lambda().eq(SigninRecord::getAttendanceId, clockinId);
                                signinRecordWrapper.lambda().eq(SigninRecord::getCompanyId, companyId);
                                signinRecordWrapper.lambda().eq(SigninRecord::getStatus, SigninTypeEnum.VALID.getKey());
                                signinRecordWrapper.lambda().eq(SigninRecord::getDeleted, 0);
                                List<SigninRecord> signinRecordResult = signinRecordMapper.selectList(signinRecordWrapper);
                                if (CollectionUtils.isNotEmpty(signinRecordResult)) {
                                    val.put(EnumClockinMergeStatus.STATUS.getKey(), EnumClockinPointStatus.SIGNIN.getKey());
                                    continue;
                                }
                            }

                            //请假合并逻辑开始
                            QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
                            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getUserId, userId);
                            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getCompanyId, companyId);
                            examineLeaveQueryWrapper.lambda().le(ExamineLeave::getFormValueStart, baseTime);
                            examineLeaveQueryWrapper.lambda().ge(ExamineLeave::getFormValueFinish, baseTime);
                            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
                            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
                            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
                            List<ExamineLeave> examineLeaveResult = examineLeaveService.list(examineLeaveQueryWrapper);
                            if (CollectionUtils.isNotEmpty(examineLeaveResult)) {
                                ExamineLeave examineLeave = examineLeaveResult.get(0);
                                val.put(EnumClockinMergeStatus.STATUS.getKey(), examineLeave.getFormValueType());
                                val.put(EnumClockinPointStatus.ISPAID.getKey(), examineLeave.getFormValueSalary());
                                if(LeaveTypeEnum.SICKLEAVE.getKey().equals(examineLeave.getFormValueType())){
                                    val.put(EnumClockinPointStatus.ISPAID.getKey(), LeaveTypeEnum.UNPAID.getKey());
                                    if(LeaveTypeEnum.PAID.getKey().equals(examineLeave.getFormValueSalary()) && ExamineStatusEnum.AGREE.getKey().equals(examineLeave.getCaseReportStatus())){
                                        val.put(EnumClockinPointStatus.ISPAID.getKey(), LeaveTypeEnum.PAID.getKey());
                                    }
                                }
                                continue;
                            }
                            //休假合并逻辑开始
                            QueryWrapper<ExamineHoliday> examineHolidayQueryWrapper = new QueryWrapper<>();
                            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getUserId, userId);
                            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getCompanyId, companyId);
                            examineHolidayQueryWrapper.lambda().le(ExamineHoliday::getFormValueStart, baseTime);
                            examineHolidayQueryWrapper.lambda().ge(ExamineHoliday::getFormValueFinish, baseTime);
                            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
                            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessResult, ExamineStatusEnum.AGREE.getKey());
                            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getDeleted, 0);
                            List<ExamineHoliday> examineHolidayResult = examineHolidayService.list(examineHolidayQueryWrapper);
                            if (CollectionUtils.isNotEmpty(examineHolidayResult)) {
                                val.put(EnumClockinMergeStatus.STATUS.getKey(), EnumClockinPointStatus.HOLIDAY.getKey());
                                continue;
                            }
                            //出差合并逻辑开始
                            QueryWrapper<ExamineBusinessTravel> examineBusinessTravelQueryWrapper = new QueryWrapper<>();
                            examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getUserId, userId);
                            examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getCompanyId, companyId);
                            examineBusinessTravelQueryWrapper.lambda().le(ExamineBusinessTravel::getFormValueStartTime, baseTime);
                            examineBusinessTravelQueryWrapper.lambda().ge(ExamineBusinessTravel::getFormValueFinishTime, baseTime);
                            examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
                            examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getProcessResult, ExamineStatusEnum.AGREE.getKey());
                            examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getDeleted, 0);
                            List<ExamineBusinessTravel> examineBusinessTravelResult = examineBusinessTravelService.list(examineBusinessTravelQueryWrapper);
                            if (CollectionUtils.isNotEmpty(examineBusinessTravelResult)) {
                                val.put(EnumClockinMergeStatus.STATUS.getKey(), EnumClockinPointStatus.BUSINESSTRAVEL.getKey());
                                continue;
                            }
                            if (EnumClockinPointStatus.NOTSIGNED.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                                ++counter;
                                if (counter == valMapList.size()) {
                                    absenteeismDayList.add(currentDate);
                                }
                            }
                        }
                    }
                });
                JSONObject jsonObject = (JSONObject) JSON.toJSON(originalDataMap);
                String finalData = jsonObject.toString();
                clockinRecordMerge.setContent(finalData);
                clockinRecordMerge.setTaskInstanceId(taskInstanceId);
                clockinRecordMergeService.save(clockinRecordMerge);

                //根据请假休假判断未提交的日报并扣分
                TaskMessage.addMessage(taskInstanceId, "--" + batch + "==考勤合并ok");
                //进行日报异常处理
                reportUnCommitted(clockinRecord, absenteeismDayList, attendanceDayList);
            }

            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            TaskMessage.addMessage(taskInstanceId, "--" + batch + "==考勤合并异常：" + e.getMessage());
            return false;
        }
    }


    /**
     *
     * 根据请假休假核算员工未提交日报的积分
     *
     * @param clockinRecord
     */
    private void reportUnCommitted(ClockinRecord clockinRecord, List<LocalDate> absenteeismDayList, List<LocalDate> attendanceDayList) {
        boolean reportLateisWarned = false;
        boolean reportEarlyisWarned = false;
        List<Integer> departMentList = new ArrayList<>();
        //查表获取用户信息
        UserDTO user = userService.getUserByUserId(clockinRecord.getUserId(), clockinRecord.getCompanyId());
        if (user != null) {
            List<UserDepartment> userDepartments = user.getUserDepartments();
            if (CollectionUtils.isNotEmpty(userDepartments)) {
                departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
            }
        }
        //对每月日期进行排序
        Collections.sort(attendanceDayList, new Comparator<LocalDate>() {
            @Override
            public int compare(LocalDate o1, LocalDate o2) {
                return o1.compareTo(o2);
            }
        });

        for (LocalDate monthDay : attendanceDayList) {
            if (absenteeismDayList.contains(monthDay)) {
                continue;
            }
            //查看是否请假
            LocalDateTime dayStartTime = LocalDateTime.of(monthDay, LocalTime.of(9, 00, 00));
            LocalDateTime dayEndTime = LocalDateTime.of(monthDay, LocalTime.of(18, 00, 00));
            QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getUserId, clockinRecord.getUserId());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getCompanyId, clockinRecord.getCompanyId());
            examineLeaveQueryWrapper.lambda().le(ExamineLeave::getFormValueStart, LocalDateTimeUtils.getMilliByTime(dayStartTime));
            examineLeaveQueryWrapper.lambda().ge(ExamineLeave::getFormValueFinish, LocalDateTimeUtils.getMilliByTime(dayEndTime));
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
            List<ExamineLeave> examineLeaveResult = examineLeaveService.list(examineLeaveQueryWrapper);
            if (CollectionUtils.isNotEmpty(examineLeaveResult)) {
                continue;
            }
            //查看是否休假
            QueryWrapper<ExamineHoliday> examineHolidayQueryWrapper = new QueryWrapper<>();
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getUserId, clockinRecord.getUserId());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getCompanyId, clockinRecord.getCompanyId());
            examineHolidayQueryWrapper.lambda().le(ExamineHoliday::getFormValueStart, LocalDateTimeUtils.getMilliByTime(dayStartTime));
            examineHolidayQueryWrapper.lambda().ge(ExamineHoliday::getFormValueFinish, LocalDateTimeUtils.getMilliByTime(dayEndTime));
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessResult, ExamineStatusEnum.AGREE.getKey());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getDeleted, 0);
            List<ExamineHoliday> examineHolidayResult = examineHolidayService.list(examineHolidayQueryWrapper);
            if (CollectionUtils.isNotEmpty(examineHolidayResult)) {
                continue;
            }
            LocalDateTime startDateTime = LocalDateTimeUtils.getDayStart(monthDay.atStartOfDay());
            LocalDateTime endDateTime = LocalDateTimeUtils.getDayEnd(monthDay.atStartOfDay());
            List<ReportRecord> reportRecordLists = reportRecordList.stream().filter(reportRecord -> reportRecord.getUserId().equals(clockinRecord.getUserId())
                    && reportRecord.getCompanyId().equals(clockinRecord.getCompanyId())
                    && reportRecord.getDeleted() == 0
                    && reportRecord.getReportTime() >= LocalDateTimeUtils.getMilliByTime(startDateTime)
                    && reportRecord.getReportTime() <= LocalDateTimeUtils.getMilliByTime(endDateTime)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(reportRecordLists)) {
                //今天提交日报，对日报异常状态进行扣分、警告
                Map<String, Object> reportMap = reportAbnormal(reportRecordLists, user,reportLateisWarned,reportEarlyisWarned);
                if(!OaMapUtils.mapIsAnyBlank(reportMap,"reportLateisWarned","reportEarlyisWarned")){
                    reportLateisWarned = (boolean)reportMap.get("reportLateisWarned");
                    reportEarlyisWarned = (boolean)reportMap.get("reportEarlyisWarned");
                }
            }else{
                //扣积分，第一次警告
                if (reportLateisWarned) {
                    reportUnCommittedRecord(EnumScoreRule.REPORTISNULL.getKey(), clockinRecord.getUserId(),
                            clockinRecord.getUserName(), clockinRecord.getCompanyId(), departMentList,
                            monthDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                } else {
                    reportUnCommittedRecord(EnumScoreRule.REPORTELATEFIRSTWARNING.getKey(), clockinRecord.getUserId(),
                            clockinRecord.getUserName(), clockinRecord.getCompanyId(), departMentList,
                            monthDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    reportLateisWarned = true;
                }
            }
        }
    }

    /**
     * 每天提交异常日报进行积分警告处理
     *
     * @param reportRecordList   日报数据
     * @param user               用户数据
     */
    private Map<String, Object> reportAbnormal(List<ReportRecord> reportRecordList, UserDTO user,boolean reportLateisWarned,boolean reportEarlyisWarned) {
        ReportRecord reportRecord = reportRecordList.get(0);
        Map<String, Object> objectMap = new HashMap<>();
        //异常,警告
        if (!reportRecord.getStatus().equals(ReportRecordTypeEnum.NORMAL.getKey())) {
            /**
             * 1.日报扣积分前查询日报提交当天，有没有打卡
             *   1.1 如果有一次打卡，则根据提交时间扣除对应的积分；
             *   1.2 如果打卡点状态都是未打卡，则判断该员工有没有上班
             *     1.2.1 如果正常请假，则不扣分。判断正常请假：审批通过的公休、请假的日期范围内包括今天（如果某天请假公休是半天，则该天需要写日报）
             *     1.2.2 如果上班，则扣积分（因为必须写日报）。判断上班：审批通过的出差的日期范围内包括今天 或 今天有签到，
             *     1.2.3 既没上班也没请假，一天的打卡状态都是未打卡时，算为旷工，旷工时不警告，不扣分
             */
            if (reportTemplateList == null || reportTemplateList.size() == 0) {
                return null;
            }
            ReportTemplate reportTemplate = reportTemplateList.stream().filter(TReportTemplate -> TReportTemplate.getId().equals(reportRecord.getTemplateId())).findAny().orElse(null);
            if (reportTemplate == null) {
                return null;
            }
            //进行异常日报扣积分、警告、旷工处理
            objectMap = timeRule(reportRecord, reportTemplate, user, companyId, taskInstanceId,reportLateisWarned,reportEarlyisWarned);
            Object timeIsNotValid = objectMap.get("timeIsNotValid");
            if (timeIsNotValid == null) {
                //记录抓取时异常，到每月统计时，正常
                reportRecord.setUnqualifiedReason("");
                reportRecord.setStatus(ReportRecordTypeEnum.NORMAL.getKey());
                reportRecord.setModifyTime(System.currentTimeMillis());
                //改状态为正常NORMAL，清空记录不合格的原因
                reportRecordService.updateById(reportRecord);
            }
        }
        return objectMap;
    }

    /**
     * 查看是否出差,是否签到
     *
     * @param reportRecord
     * @param dayStartTime
     * @param dayEndTime
     * @return
     */
    private boolean travelSignin(ReportRecord reportRecord, LocalDateTime dayStartTime, LocalDateTime dayEndTime) {
        boolean flag = false;
        //查看是否出差
        QueryWrapper<ExamineBusinessTravel> examineBusinessTravelQueryWrapper = new QueryWrapper<>();
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getUserId, reportRecord.getUserId());
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getCompanyId, reportRecord.getCompanyId());
        examineBusinessTravelQueryWrapper.lambda().le(ExamineBusinessTravel::getFormValueStartTime, LocalDateTimeUtils.getMilliByTime(dayStartTime));
        examineBusinessTravelQueryWrapper.lambda().ge(ExamineBusinessTravel::getFormValueFinishTime, LocalDateTimeUtils.getMilliByTime(dayEndTime));
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getProcessResult, ExamineStatusEnum.AGREE.getKey());
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getDeleted, 0);
        List<ExamineBusinessTravel> examineBusinessTravelList = examineBusinessTravelService.list(examineBusinessTravelQueryWrapper);
        if (CollectionUtils.isNotEmpty(examineBusinessTravelList)) {
            flag = true;
        }

        //查看是否签到
        QueryWrapper<SigninRecord> signinRecordQueryWrapper = new QueryWrapper<>();
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getUserId, reportRecord.getUserId());
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getCompanyId, reportRecord.getCompanyId());
        signinRecordQueryWrapper.lambda().le(SigninRecord::getCheckinTime, LocalDateTimeUtils.getMilliByTime(dayStartTime));
        signinRecordQueryWrapper.lambda().ge(SigninRecord::getCheckinTime, LocalDateTimeUtils.getMilliByTime(dayEndTime));
        signinRecordQueryWrapper.lambda().eq(SigninRecord::getDeleted, 0);
        List<SigninRecord> signinRecordList = signinRecordMapper.selectList(signinRecordQueryWrapper);
        if (CollectionUtils.isNotEmpty(signinRecordList)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 进行异常日报扣积分、警告、旷工处理
     *
     * @param reportRecord
     * @param reportTemplate
     * @param user
     * @param companyId
     * @return
     */
    private Map<String, Object> timeRule(ReportRecord reportRecord, ReportTemplate reportTemplate, UserDTO user, String companyId, String taskInstanceId,boolean reportLateisWarned,boolean reportEarlyisWarned) {
        String begin = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime(), "yyyy-MM-dd") + " " + reportTemplate.getBeginTime();
        String finish = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime(), "yyyy-MM-dd") + " " + reportTemplate.getFinishTime();

        //将日报提交时间格式化秒为00，只用于扣积分判断
        Long reportTimeYmdHs = LocalDateTimeUtils.convertTimeToLong(LocalDateTimeUtils.getTimeYmdHm(reportRecord.getReportTime()));
        //判断日报正常、早交还是晚交
        String timeIsNotValid = LocalDateTimeUtils.isAfterOrBeforeDate(reportTimeYmdHs, LocalDateTimeUtils.convertTimeToLong(begin), LocalDateTimeUtils.convertTimeToLong(finish));
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("reportLateisWarned", reportLateisWarned);
        objectMap.put("reportEarlyisWarned", reportEarlyisWarned);
        if (timeIsNotValid != null) {

            String reportTime = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime());

            //积分规则
            String enumScoreRule = null;
            //不合格原因
            String unqualifiedReason = null;
            //状态
            String recordStatus = null;

            Map<String, String> map = new HashMap<>(16);
            //调用接口，对于日报填写不规范的进行扣分
            LocalDateTime reportTimes = LocalDateTimeUtils.getDateTimeOfTimestamp(reportTimeYmdHs);

            //日报早交
            if (ReportRecordTypeEnum.BEFORE.getKey().equals(timeIsNotValid)) {
                map = reportBeforeCommit(reportTime, reportTimes, reportRecord, begin,reportEarlyisWarned);
            }
            //日报迟交
            if (ReportRecordTypeEnum.AFTER.getKey().equals(timeIsNotValid)) {
                map = reportAfterCommit(reportTime, reportTimes, reportRecord, finish,reportLateisWarned);
            }

            if (!OaMapUtils.mapIsAnyBlank(map, "unqualifiedReason")) {
                unqualifiedReason = map.get("unqualifiedReason");
            }
            if (!OaMapUtils.mapIsAnyBlank(map, "recordStatus")) {
                recordStatus = map.get("recordStatus");
            }
            if (!OaMapUtils.mapIsAnyBlank(map, "enumScoreRule")) {
                enumScoreRule = map.get("enumScoreRule");
            }

            if (enumScoreRule != null) {
                //扣积分
                actionScoreDeductService.reportDeductScore(companyId, user, enumScoreRule, reportRecord, unqualifiedReason, taskInstanceId);
                if (EnumScoreRule.REPORTELATEFIRSTWARNING.getKey().equals(enumScoreRule)) {
                    objectMap.put("reportLateisWarned", true);
                }
                if (EnumScoreRule.REPORTELEAVEEARLYFIRSTWARNING.getKey().equals(enumScoreRule)) {
                    objectMap.put("reportEarlyisWarned", true);
                }
            }
            reportRecord.setUnqualifiedReason(unqualifiedReason);
            reportRecord.setStatus(recordStatus);
            reportRecord.setModifyTime(System.currentTimeMillis());

            //改状态为异常abnormal，记录不合格的原因unqualified_reason
            reportRecordService.updateById(reportRecord);
        }
        objectMap.put("timeIsNotValid", timeIsNotValid);
        return objectMap;
    }


    /**
     * 日报早交
     * 1.日报早交第一次警告(60分钟（含60分钟）以内第一次警告，60分钟以上不警告直接算旷工一天)
     * 2.早交30分钟及以内，扣1分/次
     * 3.早交60分钟及以内，扣2分/次
     *
     * @param reportTime   日报提交时间
     * @param reportTimes  日报提交时间修正秒后的时间
     * @param reportRecord 日报记录
     * @param begin        标准开始时间
     * @return Map<String   ,   String>
     */
    private Map<String, String> reportBeforeCommit(String reportTime, LocalDateTime reportTimes, ReportRecord reportRecord, String begin,boolean reportEarlyisWarned) {
        //积分规则
        String enumScoreRule = null;
        //时间差
        String times = ReportRecordTypeEnum.BEFORE.getValue() + LocalDateTimeUtils.getDistanceTime(reportTime, begin);
        //不合格原因
        String unqualifiedReason;
        //状态
        String recordStatus;
        Map<String, String> map = new HashMap<>();
        LocalDateTime beginTime = LocalDateTimeUtils.getDateTimeOfTimestamp(LocalDateTimeUtils.convertTimeToLong(begin));

        //日报早交60分钟以上,扣1分/次(如：标准时间 18:00:00 - 20:00:00 , 16:59:59提交 旷工)
        boolean beforeSixty = reportTimes.isBefore(beginTime.minusMinutes(60));


        if (beforeSixty) {
            //日报早交60分钟以上不警告算旷工
            unqualifiedReason = "未能在规定时间提交日报:" + times + "不警告算旷工";
            recordStatus = ReportRecordTypeEnum.ABNORMAL.getKey();

        } else {
            //日报早交第一次警告(60分钟（含60分钟）以内第一次警告
            if (!reportEarlyisWarned) {

                unqualifiedReason = "未能在规定时间提交日报:" + times + "(第一次警告)";
                recordStatus = ReportRecordTypeEnum.WARN.getKey();
                enumScoreRule = EnumScoreRule.REPORTELEAVEEARLYFIRSTWARNING.getKey();

            } else {

                unqualifiedReason = "未能在规定时间提交日报:" + times;
                recordStatus = ReportRecordTypeEnum.ABNORMAL.getKey();

                //日报早交30分钟及以内,扣1分/次(如：标准时间 18:00:00 - 20:00:00 , 17:59:59提交 扣1分)
                boolean beforeOne = reportTimes.isAfter(beginTime.minusMinutes(31));
                //日报早交60分钟及以内,扣2分/次(如：标准时间 18:00:00 - 20:00:00 , 17:00:59提交 扣2分)
                boolean beforeTwo = reportTimes.isBefore(beginTime.minusMinutes(30)) && reportTimes.isAfter(beginTime.minusMinutes(60).minusSeconds(1));

                if (beforeOne) {
                    enumScoreRule = EnumScoreRule.REPORTEARLYDELIVERYTHIRTYMINUTES.getKey();
                }
                if (beforeTwo) {
                    enumScoreRule = EnumScoreRule.REPORTEARLYDELIVERYSIXTYMINUTES.getKey();
                }

            }

        }
        map.put("unqualifiedReason", unqualifiedReason);
        map.put("recordStatus", recordStatus);
        map.put("enumScoreRule", enumScoreRule);
        map.put("times", times);
        return map;

    }

    /**
     * 日报晚交
     * 1.第一次是警告(不算旷工)
     * 2.晚交30分钟以内，扣1分/次
     * 3.晚交60分钟以内，扣1分/次
     * 4.晚交60分钟以上未发，扣2分/次
     *
     * @param reportTime   日报提交时间
     * @param reportTimes  日报提交时间修正秒后的时间
     * @param reportRecord 日报记录
     * @param finish       标准结束时间
     * @return Map<String   ,   String>
     */
    private Map<String, String> reportAfterCommit(String reportTime, LocalDateTime reportTimes, ReportRecord reportRecord, String finish,boolean reportLateisWarned) {
        //积分规则
        String enumScoreRule = null;
        //时间差
        String times = ReportRecordTypeEnum.AFTER.getValue() + LocalDateTimeUtils.getDistanceTime(reportTime, finish);
        //不合格原因
        String unqualifiedReason;
        //状态
        String recordStatus;
        Map<String, String> map = new HashMap<>();

        //第一次是警告(不算旷工)

        if (!reportLateisWarned) {
            unqualifiedReason = "未能在规定时间提交日报:" + times + "(第一次警告)";
            recordStatus = ReportRecordTypeEnum.WARN.getKey();
            enumScoreRule = EnumScoreRule.REPORTELATEFIRSTWARNING.getKey();

        } else {
            unqualifiedReason = "未能在规定时间提交日报:" + times;

            recordStatus = ReportRecordTypeEnum.ABNORMAL.getKey();

            LocalDateTime finishTime = LocalDateTimeUtils.getDateTimeOfTimestamp(LocalDateTimeUtils.convertTimeToLong(finish));
            //日报迟交30分钟以内,扣1分/次(如：标准时间 18:00:00 - 20:00:00 , 20:00:59提交不扣分 ，20:01:00提交扣1分，20:30:59提交扣1分)
            boolean after = reportTimes.isBefore(finishTime.plusMinutes(31));
            //日报迟交60分钟及以内,扣1分/次(如：标准时间 18:00:00 - 20:00:00 , 20:31:00提交扣1分， 21:00:59提交扣1分)
            boolean afterOne = reportTimes.isAfter(finishTime.plusMinutes(30)) && reportTimes.isBefore(finishTime.plusMinutes(60).plusSeconds(1));
            //日报迟交60分钟以上,扣2分/次(如：标准时间 18:00:00 - 20:00:00 ,  21:01:00提交 扣2分)
            boolean afterTwo = reportTimes.isAfter(finishTime.plusMinutes(59));

            if (after) {
                enumScoreRule = EnumScoreRule.REPORTLATEDELIVERYTHIRTYMINUTES.getKey();
            }
            if (afterOne) {
                enumScoreRule = EnumScoreRule.REPORLATEDELIVERYESIXTYMINUTES.getKey();
            }
            if (afterTwo) {
                enumScoreRule = EnumScoreRule.REPORLATEDELIVERYESIXTYMINUTESMORE.getKey();
            }

        }
        map.put("unqualifiedReason", unqualifiedReason);
        map.put("recordStatus", recordStatus);
        map.put("enumScoreRule", enumScoreRule);
        map.put("times", times);
        return map;

    }


    /**
     * 扣除未提交日报的积分
     *
     * @param enumScoreRuleKey
     * @param userId
     * @param userName
     * @param companyId
     * @param departMentList
     * @param monthDay
     */
    private void reportUnCommittedRecord(String enumScoreRuleKey, String userId, String userName, String companyId, List<Integer> departMentList, String monthDay) {
        QueryWrapper<ActionScoreRule> actionScoreRuleQueryWrapper = new QueryWrapper<>();
        actionScoreRuleQueryWrapper.lambda().eq(ActionScoreRule::getKey, enumScoreRuleKey);
        actionScoreRuleQueryWrapper.lambda().eq(ActionScoreRule::getDeleted, 0);
        ActionScoreRule actionScoreRule = actionScoreRuleService.getOne(actionScoreRuleQueryWrapper);
        ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
        actionScoreRecord.setId(UUIDS.getID());
        actionScoreRecord.setUserId(userId);
        actionScoreRecord.setUserName(userName);
        actionScoreRecord.setCompanyId(companyId);
        actionScoreRecord.setSource(enumScoreRuleKey);
        actionScoreRecord.setTargetId(monthDay.trim() + "-" + userId.trim());
        actionScoreRecord.setType(actionScoreRule.getType());
        actionScoreRecord.setScore(Float.valueOf(actionScoreRule.getScore()));
        actionScoreRecord.setRemark(monthDay + EnumScoreRule.getValueBykey(enumScoreRuleKey));
        actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
        actionScoreRecord.setDeleted(0);
        String actionScoreDay = monthDay.trim() + " 23:59:59";
        LocalDateTime actionScoreDayTime = LocalDateTime.parse(actionScoreDay, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
        actionScoreRecord.setScoreRecordTime(LocalDateTimeUtils.getMilliByTime(actionScoreDayTime));
        actionScoreRecordService.save(actionScoreRecord);
        departMentList.stream().forEach(departId -> {
            //插入积分变动记录表和部门对应关系表
            ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
            actionScoreDepartment.setId(UUIDS.getID());
            actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
            actionScoreDepartment.setDeptId(departId);
            actionScoreDepartment.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
            actionScoreDepartment.setDeleted(0);
            actionScoreDepartmentService.save(actionScoreDepartment);
        });
    }

}
