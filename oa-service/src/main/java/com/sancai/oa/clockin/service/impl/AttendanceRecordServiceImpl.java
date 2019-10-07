package com.sancai.oa.clockin.service.impl;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.*;
import com.sancai.oa.clockin.enums.EnumClockinMergeStatus;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.clockin.exception.EnumAttendanceRecordError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.mapper.AttendanceRecordMapper;
import com.sancai.oa.clockin.mapper.ClockinRecordMergeMapper;
import com.sancai.oa.clockin.service.IAttendanceRecordDepartmentService;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.clockin.threadpool.AttendanceMergeTask;
import com.sancai.oa.clockin.threadpool.AttendanceStatisticTask;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.notify.DingDingNotifyService;
import com.sancai.oa.dingding.notify.TextNotifyDTO;
import com.sancai.oa.downloadfile.service.IDownloadRecordService;
import com.sancai.oa.examine.entity.ExamineHoliday;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineHolidayService;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.entity.enums.ReportRecordTypeEnum;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.report.service.IReportRecordService;
import com.sancai.oa.report.service.IReportTemplateService;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.ActionScoreRule;
import com.sancai.oa.score.entity.ActionUserScoreDTO;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.score.service.IActionScoreRuleService;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.typestatus.enums.AttendanceConfirmCharacterEnum;
import com.sancai.oa.typestatus.enums.AttendanceStatusEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.mapper.UserMapper;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.*;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


/**
 * <p>
 * 考勤结果 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-03
 */
@Slf4j
@Service
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements IAttendanceRecordService {

    @Autowired
    private IAttendanceRecordDepartmentService attendanceRecordDepartmentService;

    @Autowired
    private IActionScoreRecordService actionScoreRecordService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Autowired
    private IActionScoreRuleService actionScoreRuleService;

    @Autowired
    private IReportRecordService reportRecordService;

    @Autowired
    private AttendanceRecordMapper attendanceRecordMapper;

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private ClockinRecordMergeMapper clockinRecordMergeMapper;

    @Autowired
    private DingDingNotifyService dingDingNotifyService;

    @Autowired
    private IUserService userService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private IClockinRecordMergeService clockinRecordMergeService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IDownloadRecordService downloadRecordService;

    @Autowired
    private IReportTemplateService reportTemplateService;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IExamineLeaveService examineLeaveService;

    @Autowired
    private IExamineHolidayService examineHolidayService;

    @Autowired
    private ICompanyService companyService;

    @Value("${filePath.zipStorePath}")
    private String zipStorePath;

    @Autowired
    private ThreadPoolTool threadPoolTool;

    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ISigninRecordService signinRecordService;

    @Autowired
    private IDepartmentService departmentServiceImpl;

    @Autowired
    private QueryCommonUtils queryCommonUtils;


    @Override
    public Map getAttendanceRecordDetail(String id) {
        Map res = attendanceRecordMapper.getAttendanceRecordDetail(id);
        List<Department> result = departmentService.listDepartment(res.get("company_id") + "");

        Map deptmap = new HashMap();
        for (Department d : result) {
            deptmap.put(d.getId(), d.getName());
        }

        String[] deptIds = (res.get("dept_id") + "").split(",");
        StringBuffer deptNames = new StringBuffer();
        String deptname = "";
        for (String deptId : deptIds) {
            deptNames.append(deptmap.get(deptId) + ",");
        }
        if(deptIds.length>0){
            deptname = deptNames.toString().substring(0,deptNames.toString().length()-1);
        }
        res.put("dept_name", deptname);
        String content = res.get("clock_in_every_day") + "";
        //数据按时间排序
        String contents = sortTime(content);

        res.put("clock_in_every_day", contents);
        res.remove("dept_id");
        res.remove("company_id");
        String confirm = res.get("confirm") + "";
        if (!StringUtils.isEmpty(confirm)) {
            res.put("confirm", AttendanceConfirmCharacterEnum.getvalueBykey(confirm));
        }
        String salaryDays = res.get("salary_days") + "";
        if (!StringUtils.isEmpty(salaryDays)&&salaryDays.contains("-")) {
            res.put("salary_days","0");
        }
        return res;
    }


    @Override
    public Map attendanceRecordConfirm(String id, String user_id) {
        AttendanceRecord attendanceRecord = this.getById(id);
        // 设值为用户已确认状态
        attendanceRecord.setUserConfirm(Integer.parseInt(AttendanceConfirmCharacterEnum.USER_CONFIRMD.getKey()));
        attendanceRecordMapper.updateById(attendanceRecord);
        return null;
    }

    @Override
    public List<Map> getAttendanceRecordList(AttendanceRecordDTO attendanceRecordDTO) {
        if (StringUtils.isNotEmpty(attendanceRecordDTO.getDeptId())) {
            attendanceRecordDTO.setDeptIdInt(Integer.parseInt(attendanceRecordDTO.getDeptId()));
        }


        if (StringUtils.isNotEmpty(attendanceRecordDTO.getFullAttendance())) {
            attendanceRecordDTO.setFullAttendanceInt(Integer.parseInt(attendanceRecordDTO.getFullAttendance()));
        }

        if (null != attendanceRecordDTO.getConfirm()) {
            attendanceRecordDTO.setConfirmStr(attendanceRecordDTO.getConfirm() + "");
        }

        if(StringUtils.isNotEmpty(attendanceRecordDTO.getDeptId())){
            List<Long> deptList = departmentService.listSubDepartment(attendanceRecordDTO.getCompanyId(),attendanceRecordDTO.getDeptId());
            attendanceRecordDTO.setDeptList(deptList);
        }

        PageHelper.startPage(attendanceRecordDTO.getPage(), attendanceRecordDTO.getCapacity());
        List<Map> res = attendanceRecordMapper.attendanceRecordList(attendanceRecordDTO);

        List<Department> result = departmentService.listDepartment(attendanceRecordDTO.getCompanyId());

        List<String> deptlist = new ArrayList<String>();
        Map deptmap = new HashMap();
        for (Department d : result) {
            deptmap.put(d.getId(), d.getName());
        }
        // 处理部门id 到部门名称
        for (Map m : res) {
            String[] deptIds = (m.get("dept_id") + "").split(",");
            StringBuffer deptNames = new StringBuffer();
            for (String deptId : deptIds) {
                deptNames.append(deptmap.get(deptId) + ",");
            }
            m.put("dept_name", deptNames.toString().substring(0, deptNames.toString().length() - 1));
            String confirm = m.get("confirm") + "";
            if (!StringUtils.isEmpty(confirm)) {
                m.put("confirm", AttendanceConfirmCharacterEnum.getvalueBykey(confirm));
            }
            String salaryDays = m.get("salary_days") + "";
            if (!StringUtils.isEmpty(salaryDays)&&salaryDays.contains("-")) {
                m.put("salary_days","0");
            }
        }
        return res;
    }


    @Override
    public void updateAttendanceResult(ClockinRecordMergeDTO clockinRecordMergeDTO) {
        String attendanceRecordId = clockinRecordMergeDTO.getAttendanceRecordId();
        if (StringUtils.isNotBlank(attendanceRecordId)) {
            //删除已经生成的考勤积分数据
            attendanceRecordDepartmentService.deleteAttendanceRecord(attendanceRecordId);
        }
        StatisticAttendanceNumber attendanceNumber = new StatisticAttendanceNumber();
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setId(clockinRecordMergeDTO.getAttendanceRecordId());
        attendanceRecord.setUserId(clockinRecordMergeDTO.getUserId());
        List<Integer> departMentList = new ArrayList<>();
        //查表获取用户信息
        UserDTO user = userService.getUserByUserId(clockinRecordMergeDTO.getUserId(), clockinRecordMergeDTO.getCompanyId());
        if (user != null) {
            List<UserDepartment> userDepartments = user.getUserDepartments();
            if(CollectionUtils.isNotEmpty(userDepartments)){
                departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
            }
        }
        attendanceRecord.setUserName(clockinRecordMergeDTO.getUserName());
        attendanceRecord.setCompanyId(clockinRecordMergeDTO.getCompanyId());
        attendanceRecord.setMonth(clockinRecordMergeDTO.getStatisticalMonth());
        String result = clockinRecordMergeDTO.getContent();
        if (StringUtils.isBlank(result)) {
            return;
        }
        result = sortContentByTime(result);
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
                continuousAbsenteeismDaysFour(attendanceNumber, valMapList, attendanceRecord, departMentList,currentDate,absenteeismDayList,isAbsenteeism);
            } else if (CollectionUtils.isNotEmpty(valMapList) && valMapList.size() == Integer.parseInt(EnumClockinMergeStatus.WORKINGTWO.getKey())) {
                continuousAbsenteeismDaysTwo(attendanceNumber, valMapList, attendanceRecord, departMentList,currentDate,absenteeismDayList,isAbsenteeism);
            }
        }
        attendanceRecord.setNotSignedCount(attendanceNumber.getNotSignedCount()+attendanceNumber.getAbsenteeismNotSignedCount());
        attendanceRecord.setEarlyCount(attendanceNumber.getEarlyCount());
        attendanceRecord.setLateCount(attendanceNumber.getLateCount());
        String StatisticalMonth = clockinRecordMergeDTO.getStatisticalMonth().trim() + "-01";
        LocalDate localDate = LocalDate.parse(StatisticalMonth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int monthDays = localDate.lengthOfMonth();
        LocalDateTime firstDayOfMonth = LocalDateTimeUtils.getDayStart(
                localDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay());
        LocalDateTime lastDayOfMonth = LocalDateTimeUtils.getDayEnd(
                localDate.with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay());
        //根据统计结果计算旷工天数
        float absenteeismDays = countAbsenteeismDays(attendanceNumber,firstDayOfMonth,lastDayOfMonth,attendanceRecord,absenteeismDayList,attendanceDayList);
        attendanceRecord.setAbsenteeismDays(absenteeismDays > monthDays ? monthDays : absenteeismDays);
        //根据统计结果计算计薪天数,1天旷工抵消三天计薪天数
        float salaryDays = attendanceDayList.size() - attendanceNumber.getNoPaidDays() - absenteeismDays * 3;
        attendanceRecord.setSalaryDays(salaryDays);
        //统计实际出勤天数
        //带薪请假天数
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
            attendanceRecord.setSalaryDays(attendanceRecord.getSalaryDays()- attendanceRecord.getPaidSickLeaveDays() + 2f);
            attendanceRecord.setPaidSickLeaveDays(2f);
            attendanceRecord.setUnpaidSickLeaveDays(attendanceRecord.getSickLeaveDays() - 2f);
        }
        attendanceRecord.setPersonalLeaveDays(attendanceNumber.getCasualLeaveDays());
        attendanceRecord.setChildbirthLeaveDays(attendanceNumber.getMaternityLeaveDays());
        attendanceRecord.setPaternityLeaveDays(attendanceNumber.getPaternityLeaveDays());
        attendanceRecord.setMarriageLeaveDays(attendanceNumber.getMarriageLeaveDays());
        attendanceRecord.setFuneralLeaveDays(attendanceNumber.getFuneralLeaveDays());

        //统计积分,调用统计积分接口
        ActionUserScoreDTO actionUserScoreDTO = actionScoreRecordService.queryUserScore(attendanceRecord.getUserId(),clockinRecordMergeDTO.getCompanyId(),
                LocalDateTimeUtils.getMilliByTime(firstDayOfMonth), LocalDateTimeUtils.getMilliByTime(lastDayOfMonth));
        if (actionUserScoreDTO != null) {
            Float score = actionUserScoreDTO.getScore();
            if (score != null) {
                attendanceRecord.setScore(score);
            }
        } else {
            attendanceRecord.setScore(0F);
        }
        //日报不合格数，调用接口
        attendanceRecord.setReportLowQualityCount(disqualificationReportCount(attendanceRecord, firstDayOfMonth, lastDayOfMonth));
        attendanceRecord.setUserConfirm(0);
        attendanceRecord.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        attendanceRecord.setDeleted(0);
        attendanceRecord.setTaskInstanceId(clockinRecordMergeDTO.getTaskInstanceId());
        updateById(attendanceRecord);
        log.info("更新统计数据成功");
    }


    /**
     * 统计本月的不合格日报数量
     * @param attendanceRecord
     * @param firstDayOfMonth
     * @param lastDayOfMonth
     * @return
     */
    @Override
    public int disqualificationReportCount(AttendanceRecord attendanceRecord, LocalDateTime firstDayOfMonth, LocalDateTime lastDayOfMonth) {
        List<String> statusList = new ArrayList<>();
        statusList.add(ReportRecordTypeEnum.ABNORMAL.getKey());
        statusList.add(ReportRecordTypeEnum.WARN.getKey());
        QueryWrapper<ReportRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportRecord::getUserId, attendanceRecord.getUserId());
        queryWrapper.lambda().eq(ReportRecord::getCompanyId, attendanceRecord.getCompanyId());
        queryWrapper.lambda().in(ReportRecord::getStatus, statusList);
        queryWrapper.lambda().eq(ReportRecord::getStatus, ReportRecordTypeEnum.ABNORMAL.getKey());
        queryWrapper.lambda().between(ReportRecord::getReportTime, LocalDateTimeUtils.getMilliByTime(firstDayOfMonth), LocalDateTimeUtils.getMilliByTime(lastDayOfMonth));
        queryWrapper.lambda().eq(ReportRecord::getDeleted, 0);
        return reportRecordService.count(queryWrapper);
    }

    /**
     * 根据迟到早退时间及缺卡次数生成积分变动记录,第一次警告不扣分
     *
     * @param enumScoreRuleKey
     * @param attendanceRecord
     * @param departMentList
     */
    public void unClockInlateEarlyScoreRecord(String enumScoreRuleKey, AttendanceRecord attendanceRecord, List<Integer> departMentList, StatisticAttendanceNumber attendanceNumber,Long recordTime) {
        QueryWrapper<ActionScoreRule> actionScoreRuleQueryWrapper = new QueryWrapper<>();
        actionScoreRuleQueryWrapper.lambda().eq(ActionScoreRule::getKey, enumScoreRuleKey);
        actionScoreRuleQueryWrapper.lambda().eq(ActionScoreRule::getDeleted, 0);
        ActionScoreRule actionScoreRule = actionScoreRuleService.getOne(actionScoreRuleQueryWrapper);
        ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
        actionScoreRecord.setId(UUIDS.getID());
        actionScoreRecord.setUserId(attendanceRecord.getUserId());
        actionScoreRecord.setUserName(attendanceRecord.getUserName());
        actionScoreRecord.setCompanyId(attendanceRecord.getCompanyId());
        actionScoreRecord.setSource(enumScoreRuleKey);
        actionScoreRecord.setTargetId(attendanceRecord.getId());
        actionScoreRecord.setType(actionScoreRule.getType());
        actionScoreRecord.setScore(Float.valueOf(actionScoreRule.getScore()));
        actionScoreRecord.setRemark(EnumScoreRule.getValueBykey(enumScoreRuleKey));
        actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
        actionScoreRecord.setDeleted(0);
        actionScoreRecord.setScoreRecordTime(recordTime);
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
        if (!attendanceNumber.isWarnedNotSignedCount()&&EnumScoreRule.NOTSIGNEDFIRSTWARNING.getKey().equals(enumScoreRuleKey)) {
            attendanceNumber.setWarnedNotSignedCount(true);
        }
        if (!attendanceNumber.isWarnedLateEarly()&&EnumScoreRule.LATEEARLYFIRSTWARNING.getKey().equals(enumScoreRuleKey)) {
            attendanceNumber.setWarnedLateEarly(true);
        }
    }


    /**
     * 考勤缺卡点订正
     *
     * @param id           考勤结果id
     * @param checkPointId 缺卡点的id
     * @param day          缺卡点当天的日期
     * @return 返回1代表修改成功，其他代表失败
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public int updateNotSignedPoint(String id, String checkPointId, Long day) {
        //获取某个员工某一月的考勤记录content和对应的clockinId
        Map<String, String> resultMap = attendanceRecordMapper.getContentById(id);
        String content = resultMap.get("content");
        Map stringToMap = JSON.parseObject(content, Map.class);
        //获取对应某一天的四次打卡记录
        List<Map<String, Object>> listMap = (List<Map<String, Object>>) stringToMap.get(day + "");
        if (CollectionUtils.isEmpty(listMap)) {
            throw new OaClockinlException(EnumAttendanceRecordError.ATTENDANCE_RECORD_NO_CORRESPONDING_RECORD);
        }
        for (Map<String, Object> map : listMap) {
            System.out.println(map.get("id"));
            if (checkPointId.equals(map.get("id") + "")) {
                map.put("status", AttendanceStatusEnum.MANUALCORRECTION.getKey());
            }
        }
        String newContent = JSON.toJSONString(stringToMap);
        //ClockinRecordMerge对象封装修改后的content以及对应的clockinId
        ClockinRecordMerge recordMerge = new ClockinRecordMerge();
        recordMerge.setContent(newContent);
        recordMerge.setClockinId(resultMap.get("clockinId") + "");
        //设置修改时间
        recordMerge.setModifyTime(System.currentTimeMillis());
        int result = clockinRecordMergeMapper.updateContentByClockinId(recordMerge);
        if (result == 1) {
            //通过mapper的查询方法封装ClockinRecordMergeDTO对象
            ClockinRecordMergeDTO clockinRecordMergeDTO = attendanceRecordMapper.queryDTOById(id);
            clockinRecordMergeDTO.setContent(newContent);
            //调用统计考勤结果方法
            updateAttendanceResult(clockinRecordMergeDTO);
        }
        return result;
    }


    @Override
    public void pageStatisticAttendanceResult(String companyId, String taskInstanceId) {
        String month;
        try {
            //重试删除这个任务实例的统计及相关的数据
            TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
            if (taskInstance == null) {
                log.error(EnumAttendanceRecordError.STATISTIC_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
                throw new OaClockinlException(EnumAttendanceRecordError.STATISTIC_INSTANCE_IS_EMPTY);
            }
            month = taskInstanceService.resetMonth(taskInstance);
            if(StringUtils.isBlank(month)){
                log.error(EnumAttendanceRecordError.STATISTIC_MONTH_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
                throw new OaClockinlException(EnumAttendanceRecordError.STATISTIC_MONTH_IS_EMPTY);
            }
            //如果根据实例id查到值,根据实例id删除之前的已经保存的数据 , 重新获取合并的月份
            List<AttendanceRecord> attendanceRecordList = attendanceRecordByTaskId(taskInstanceId);
            if(CollectionUtils.isNotEmpty(attendanceRecordList)){
                if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                   //TODO 根据companyId  + taskInstanceId 把记录对应的部门集合存入缓存
                    List<DataMap> actionRecordLists = attendanceRecordMapper.attendanceRecordDeptIdList(companyId,taskInstanceId);
                    signinRecordService.recordOriginDeptIdsSaveRedis(actionRecordLists);
                }
                //删除统计的数据
                attendanceRecordDepartmentService.attendanceStatisticDelete(attendanceRecordList);
            }
            Integer totalNumber = 0;
            //分页数
            Integer pageSize = 100;
            //当前页游标
            Integer cursor = 1;
            long start = System.currentTimeMillis();
            while (cursor != null) {
                int page = cursor;
                long starts = System.currentTimeMillis();
                PageHelper.startPage(cursor, pageSize);
                List<ClockinRecordMergeDTO> clockinRecordMergeDTOList = clockinRecordMergeMapper.selectClockinRecordMergeDTO(companyId, month);
                TaskMessage.addMessage(taskInstanceId,"分页统计考勤数据，第"+cursor+"页共"+clockinRecordMergeDTOList.size()+"条数据");
                boolean isFinish = false;
                if (clockinRecordMergeDTOList.size() < pageSize) {
                    cursor = null;
                    isFinish = true;
                } else {
                    cursor++;
                }
                Map<String,Object> params = new HashMap<>();
                params.put("taskInstanceId",taskInstanceId);
                params.put("batch",cursor);
                params.put("actionScoreRecordService",actionScoreRecordService);
                params.put("attendanceRecordService",this);
                params.put("userService",userService);
                params.put("redisUtil",redisUtil);
                params.put("attendanceRecordDepartmentService",attendanceRecordDepartmentService);
                threadPoolTool.excuteTask(transactionManager,taskInstanceId,clockinRecordMergeDTOList,5,params,isFinish, AttendanceStatisticTask.class);
                long ends = System.currentTimeMillis();
                int secs = (int) ((ends - starts)/1000);
                TaskMessage.addMessage(taskInstanceId,"第"+page+"页统计考勤数据成功，共"+clockinRecordMergeDTOList.size()+"条数据,耗时:"+secs+"秒");
                totalNumber += clockinRecordMergeDTOList.size();
            }
            long end = System.currentTimeMillis();
            int sec = (int) ((end - start)/1000);
            TaskMessage.addMessage(taskInstanceId,"累计统计考勤数据共"+totalNumber+"条,总耗时:"+sec+"秒");
        } catch (Exception e) {
            TaskMessage.addMessage(taskInstanceId,"异常："+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        try {
            //发送通知给分公司的人事专员
            dingDingNotifyService.sendToHrNotify(companyId, month);
        } catch (Exception e) {
            TaskMessage.addMessage(taskInstanceId,"异常："+e.getMessage());
            log.error("发送通知失败，companyId="+companyId+" 月份 month="+month+" 异常："+e.getMessage());
        }
    }

    /**
     * 导出考勤结果Excel
     *
     * @param downloadQueryConditionDTO 考勤结果导出Excel查询条件封装的实体类
     * @return 返回
     */
    @Override
    public synchronized void generateAttendanceExcel(DownloadQueryConditionDTO downloadQueryConditionDTO) throws NoSuchFieldException {
        TemplateExportParams params = null;
        Integer days = null;
        Map<String, List> stringListMap = null;
        Map<String, ExcelOneObjectVO> oneObjectVOMap = null;
        List<String> superDeptNameList = null;
        String month = downloadQueryConditionDTO.getMonth();
        if (StringUtils.isNotBlank(month)) {
            //根据指定年月获取对应模板
            days = DateCalUtil.getDaysByMonth(month);
            String templatePath = "template"+ File.separator +"modelCompany" + days + ".xlsx";
            params = new TemplateExportParams(ZipUtil.convertTemplatePath(templatePath),true);
        }
        try {
            List<Object> list = getDeptAttendanceList(downloadQueryConditionDTO);
            stringListMap = (Map<String, List>) list.get(0);
            oneObjectVOMap = (Map<String, ExcelOneObjectVO>) list.get(1);
            superDeptNameList = (List<String>) list.get(2);
        } catch (IllegalAccessException e) {
            throw new OaClockinlException(EnumAttendanceRecordError.QUERY_ATTENDANCE_LIST_FAILURE);
        }
        //新建Excel，用来复制别的Excel中的sheet
        XSSFWorkbook newExcelCreat = new XSSFWorkbook();
        Workbook workBook = null;
        //创建存放导出的zip压缩文件的文件夹
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
        //生成zip的路径
        String path = zipStorePath  + "zip" + File.separator + yyyyMMdd + File.separator;
        File fileZipDirectory = new File(path );
        //生成Excel的临时目录
        File fileExcelDirectory = new File(zipStorePath + File.separator + "temp");
        if (!fileZipDirectory.exists()) {
            fileZipDirectory.mkdirs();
        }
        if(!fileExcelDirectory.exists()){
            fileExcelDirectory.mkdir();
        }
        //文件全路径名的集合
        List<String> fileNames = new ArrayList<String>();
        //获取公司名称
        Company company = companyService.companyDetail(downloadQueryConditionDTO.getCompanyId());
        if(company == null){
            return;
        }
        String companyName = company.getName();
        //获取下载部门的名称
        Set deptSet = redisUtil.sGet(downloadQueryConditionDTO.getCompanyId());
        String deptName = QueryCommonUtils.getDeptName(deptSet, downloadQueryConditionDTO.getDeptId() + "");
        if(downloadQueryConditionDTO.getDeptId() == null){
            deptName = companyName;
        }
        int num = 0;
        int index = 0;
        int i;
        for (String s : superDeptNameList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("one", oneObjectVOMap.get(s));
            map.put("list", stringListMap.get(s));
            try {
                num++;
                index++;
                //设置工作簿的名称
                params.setSheetName(deptName);
                //根据模板导出一个Excel
                workBook = ExcelExportUtil.exportExcel(params, map);
                //合并序号和姓名相同内容的单元格
                mergeSameCell(workBook, days);
                //获取生成Excel中的sheet
                XSSFSheet oldSheet = (XSSFSheet) workBook.getSheetAt(0);
                XSSFSheet newSheet = null;
                if(index == 1){
                    newSheet = newExcelCreat.createSheet(oldSheet.getSheetName());
                }else{
                    newSheet = newExcelCreat.getSheetAt(0);
                }
                 i = POIUtils.copySheet(newExcelCreat, oldSheet, newSheet);
            } catch (Exception e) {
                throw new OaClockinlException(EnumAttendanceRecordError.COPY_EXCEL_FAILURE);
            }
          //当Excel中Sheet的行数大于600或者输出最后一个部门考勤内容时，作为一个新的Excel导出
                if (  num == stringListMap.keySet().size() || i>=600) {
                    try {
                        String fileName = companyName+"考勤统计" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        //导出excel的路径,File.separator跨平台分隔符
                        String fullFilePath = fileExcelDirectory + File.separator + fileName + ".xlsx";
                        fileNames.add(fullFilePath);
                        FileOutputStream os = new FileOutputStream(fullFilePath);
                        newExcelCreat.write(os);
                        newExcelCreat = new XSSFWorkbook();
                        index = 0;
                    } catch (Exception e) {
                        throw new OaClockinlException(EnumAttendanceRecordError.EXPORT_EXCEL_FAILURE);
                    }
                }

        }
        //将excel文件生成压缩文件.zip
        String fileName = "AttendanceRecord" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String zipPath = fileZipDirectory + File.separator + fileName + ".zip";
        File zipfile = new File(zipPath);
        ArrayList<File> list = new ArrayList<>();
        for (int j = 0, n1 = fileNames.size(); j < n1; j++) {
            list.add(new File(fileNames.get(j)));
        }
        try {
            ZipUtil.zipFiles(list, zipfile);
            //赋予文件读权限，赋予上一级文件夹执行权限
            Runtime.getRuntime().exec("chmod 001 "+path);
            Runtime.getRuntime().exec("chmod 004 "+zipPath);
        } catch (Exception e) {
            throw new OaClockinlException(EnumAttendanceRecordError.EXPORT_EXCEL_ZIP_FAILURE);
        }


        //压缩文件导出之后，删除目录中的excel文件
        ZipUtil.deleteExcel(fileNames);

        //调用update方法，将数据库中的状态改为下载完成
        downloadRecordService.updateDownloadRecordStatus(downloadQueryConditionDTO.getId(), zipPath, true);

    }

    @Override
    public List<AttendanceRecord> attendanceRecordByTaskId(String taskInstanceId) {
        QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapper = new QueryWrapper<>();
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getTaskInstanceId,taskInstanceId);
        attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getDeleted,0);
        List<AttendanceRecord> attendanceRecordList = list(attendanceRecordQueryWrapper);
        return attendanceRecordList;
    }

    /**
     * 获取根据条件查询的一个部门或者一个公司下所有部门对应的map
     *
     * @param downloadQueryConditionDTO 查询条件封装的实体类
     * @return 返回部门名称，部门考勤记录的键值对
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private List<Object> getDeptAttendanceList(DownloadQueryConditionDTO downloadQueryConditionDTO) throws NoSuchFieldException, IllegalAccessException {
        String date = downloadQueryConditionDTO.getMonth();
        //从redis中取出companyId对应的部门列表
        Set deptSet = redisUtil.sGet(downloadQueryConditionDTO.getCompanyId());
        //1.查询考勤复合结果集合（统计天数，考勤数据content等）
        //1.1如何deptId存在，查询其叶子部门
        Long deptId1 = downloadQueryConditionDTO.getDeptId();
        if(null != deptId1){
            List<Long> deptList = departmentService.listSubDepartment(downloadQueryConditionDTO.getCompanyId(), downloadQueryConditionDTO.getDeptId()+"");
            downloadQueryConditionDTO.setDeptList(deptList);
        }

        List<AttendanceComplexResultDTO> attendanceComplexResultDTOS = clockinRecordMergeService.queryAttendanceComplexResult(downloadQueryConditionDTO);
        //创建集合存放部门的全路径
        List<String> superDeptNameList = new ArrayList<>();
        //根据部门id将考勤复合记录进行分组成多个集合
        Map<String, List<AttendanceComplexResultDTO>> deptMap = attendanceComplexResultDTOS.stream().collect(Collectors.groupingBy(t -> t.getDeptId()));
        //创建map,将数据，以部门名称作为key,以考勤记录list作为value
        LinkedHashMap<String, List> attendanceMap = new LinkedHashMap<>();
        //创建map，将部门名称作为key，每个sheet的动态显示内容对象ExcelOneObjectVO作为value
        Map<String, ExcelOneObjectVO> oneObjectMap = new HashMap<>();
        //deptMap，key为部门id
        for (String deptId : deptMap.keySet()) {
            int number = 0;
            //新建一个集合，存储该部门下的所有记录
            ArrayList<ExcelOneLineVO> excelOneLineVOS = new ArrayList<>();
            List<AttendanceComplexResultDTO> attendanceComplexResultDTOSList = deptMap.get(deptId);
            //遍历一个部门下的所有考勤记录
            for (AttendanceComplexResultDTO attendanceComplexResultDTO : attendanceComplexResultDTOSList) {
                //将数据库中查询的一个人的一条考勤记录拆分成四条Excel中的记录
                //获得map结构的content
                String content = attendanceComplexResultDTO.getContent();
                Map<String, List<Map<String, Object>>> contentMap = JSON.parseObject(content, Map.class);
                //1.创建一天对应的六个打卡点对象
                ExcelOneLineVO clockinOne = new ExcelOneLineVO();
                ExcelOneLineVO clockinTwo = new ExcelOneLineVO();
                ExcelOneLineVO clockinThree = new ExcelOneLineVO();
                ExcelOneLineVO clockinFour = new ExcelOneLineVO();
                ExcelOneLineVO clockinFive = new ExcelOneLineVO();
                ExcelOneLineVO clockinSix = new ExcelOneLineVO();
                //2.设置每一行对象ExcelOneLineVO的name
                clockinOne.setName(attendanceComplexResultDTO.getName());
                clockinTwo.setName(attendanceComplexResultDTO.getName());
                clockinThree.setName(attendanceComplexResultDTO.getName());
                clockinFour.setName(attendanceComplexResultDTO.getName());
                clockinFive.setName(attendanceComplexResultDTO.getName());
                clockinSix.setName(attendanceComplexResultDTO.getName());
                //定义比较变量
                int compareVar = 1;
                //遍历contentMap,key为每一天的毫秒值
                for (String s1 : contentMap.keySet()) {
                    String s = new SimpleDateFormat("dd").format(Long.parseLong(s1));
                    int day = Integer.parseInt(s);
                    //一个人每一天对应的打卡记录(1-6条)
                    List<Map<String, Object>> list = contentMap.get(s1);
                    //对一天的记录按照baseCheckTime排序
                    Collections.sort(list, new Comparator<Map<String, Object>>(){
                        @Override
                        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                            String s1 = String.valueOf(o1.get("baseCheckTime"));
                            String s2 = String.valueOf(o2.get("baseCheckTime"));
                            return s1.compareTo(s2);
                        }
                    });
                    //记录部门为考勤组的一个月中最多打卡次数
                    if (compareVar < list.size()) {
                        compareVar = list.size();
                    }
                    //设置每一行的考勤打卡状态
                    //设置上午上班打卡
                    String statusOne = (String) list.get(0).get("status");
                    if("SICKLEAVE".equals(statusOne)){
                        statusOne = list.get(0).get("ISPAID")+statusOne;
                    }
                    setProperty(clockinOne, "day" + day, AttendanceStatusEnum.getvalueBykey(statusOne));
                    //设置上午下班打卡
                    if (list.size() >= 2) {
                        String statusTwo = (String) list.get(1).get("status");
                        if("SICKLEAVE".equals(statusTwo)){
                            statusTwo = list.get(1).get("ISPAID")+statusTwo;
                        }
                        setProperty(clockinTwo, "day" + day, AttendanceStatusEnum.getvalueBykey(statusTwo));
                    }
                    if (list.size() >= 3) {
                        String statusThree = (String) list.get(2).get("status");
                        if("SICKLEAVE".equals(statusThree)){
                            statusThree = list.get(2).get("ISPAID")+statusThree;
                        }
                        setProperty(clockinThree, "day" + day, AttendanceStatusEnum.getvalueBykey(statusThree));
                    }
                    if (list.size() >= 4) {
                        String statusFour = (String) list.get(3).get("status");
                        if("SICKLEAVE".equals(statusFour)){
                            statusFour = list.get(3).get("ISPAID")+statusFour;
                        }
                        setProperty(clockinFour, "day" + day, AttendanceStatusEnum.getvalueBykey(statusFour));
                    }
                    if (list.size() >= 5) {
                        String statusFive = (String) list.get(4).get("status");
                        if("SICKLEAVE".equals(statusFive)){
                            statusFive = list.get(4).get("ISPAID")+statusFive;
                        }
                        setProperty(clockinFive, "day" + day, AttendanceStatusEnum.getvalueBykey(statusFive));
                    }
                    if (list.size() == 6) {
                        String statusSix = (String) list.get(5).get("status");
                        if("SICKLEAVE".equals(statusSix)){
                            statusSix = list.get(5).get("ISPAID")+statusSix;
                        }
                        setProperty(clockinSix, "day" + day, AttendanceStatusEnum.getvalueBykey(statusSix));
                    }

                }
                //3.设置漏打卡
                clockinOne.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                clockinTwo.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                clockinThree.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                clockinFour.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                clockinFive.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                clockinSix.setNotSignedCount(attendanceComplexResultDTO.getNotSignedCount());
                //4.设置病假天数
                clockinOne.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                clockinTwo.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                clockinThree.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                clockinFour.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                clockinFive.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                clockinSix.setSickLeaveDays(attendanceComplexResultDTO.getSickLeaveDays());
                //5.设置事假天数
                clockinOne.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                clockinTwo.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                clockinThree.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                clockinFour.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                clockinFive.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                clockinSix.setPersonalLeaveDays(attendanceComplexResultDTO.getPersonalLeaveDays());
                //6.设置产假天数
                clockinOne.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                clockinTwo.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                clockinThree.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                clockinFour.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                clockinFive.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                clockinSix.setChildbirthLeaveDays(attendanceComplexResultDTO.getChildbirthLeaveDays());
                //7.设置旷工天数
                clockinOne.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                clockinTwo.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                clockinThree.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                clockinFour.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                clockinFive.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                clockinSix.setAbsenteeismDays(attendanceComplexResultDTO.getAbsenteeismDays());
                //8.设置公休天数
                clockinOne.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                clockinTwo.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                clockinThree.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                clockinFour.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                clockinFive.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                clockinSix.setHolidayDays(attendanceComplexResultDTO.getHolidayDays());
                //9.设置实际出勤天数
                clockinOne.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                clockinTwo.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                clockinThree.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                clockinFour.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                clockinFive.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                clockinSix.setAttendanceDays(attendanceComplexResultDTO.getAttendanceDays());
                //10.设置计薪天数
                Float salaryDays = attendanceComplexResultDTO.getSalaryDays();
                //判断统计天数，如果为负数，设置为0
                if(salaryDays < 0F){
                    salaryDays = 0F;
                }
                clockinOne.setSalaryDays(salaryDays);
                clockinTwo.setSalaryDays(salaryDays);
                clockinThree.setSalaryDays(salaryDays);
                clockinFour.setSalaryDays(salaryDays);
                clockinFive.setSalaryDays(salaryDays);
                clockinSix.setSalaryDays(salaryDays);
                //11.设置迟到次数
                clockinOne.setLateCount(attendanceComplexResultDTO.getLateCount());
                clockinTwo.setLateCount(attendanceComplexResultDTO.getLateCount());
                clockinThree.setLateCount(attendanceComplexResultDTO.getLateCount());
                clockinFour.setLateCount(attendanceComplexResultDTO.getLateCount());
                clockinFive.setLateCount(attendanceComplexResultDTO.getLateCount());
                clockinSix.setLateCount(attendanceComplexResultDTO.getLateCount());
                //12.设置早退次数
                clockinOne.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                clockinTwo.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                clockinThree.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                clockinFour.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                clockinFive.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                clockinSix.setEarlyCount(attendanceComplexResultDTO.getEarlyCount());
                //13.设置婚假天数
                clockinOne.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                clockinTwo.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                clockinThree.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                clockinFour.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                clockinFive.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                clockinSix.setMarriageLeaveDays(attendanceComplexResultDTO.getMarriageLeaveDays());
                //14.设置丧假天数
                clockinOne.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                clockinTwo.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                clockinThree.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                clockinFour.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                clockinFive.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                clockinSix.setFuneralLeaveDays(attendanceComplexResultDTO.getFuneralLeaveDays());
                //15.设置陪产假天数
                clockinOne.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                clockinTwo.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                clockinThree.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                clockinFour.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                clockinFive.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                clockinSix.setPaternityLeaveDays(attendanceComplexResultDTO.getPaternityLeaveDays());
                //添加到一个部门对应的集合中,并设置序号
                excelOneLineVOS.add(clockinOne);
                int num = ++number;
                clockinOne.setId(num);
                if (compareVar >= 2) {
                    excelOneLineVOS.add(clockinTwo);
                    clockinTwo.setId(num);
                }
                if (compareVar >= 3) {
                    excelOneLineVOS.add(clockinThree);
                    clockinThree.setId(num);
                }
                if (compareVar >= 4) {
                    excelOneLineVOS.add(clockinFour);
                    clockinFour.setId(num);
                }
                if (compareVar >= 5) {
                    excelOneLineVOS.add(clockinFive);
                    clockinFive.setId(num);
                }
                if (compareVar >= 6) {
                    excelOneLineVOS.add(clockinSix);
                    clockinSix.setId(num);
                }
            }
            //获取s即dept_id对应的部门名称
            String deptName = queryCommonUtils.getThreeLevelDeptName(downloadQueryConditionDTO.getCompanyId(), deptId);
            //查询部门全路径作为key,将一个部门下的所有考勤记录作为value存入map中
            String departmentName = departmentServiceImpl.getSuperiorsDepartmentName(downloadQueryConditionDTO.getCompanyId(), deptId);
            attendanceMap.put(departmentName, excelOneLineVOS);
            superDeptNameList.add(departmentName);
            //封装ExcelOneObjectVO
            ExcelOneObjectVO oneObjectVO = new ExcelOneObjectVO();
            oneObjectVO.setYear(date.split("-")[0]);
            oneObjectVO.setMonth(date.split("-")[1]);
            oneObjectVO.setDeptName(deptName);
            //获取公司名称
            String companyName = null;
            Company company = companyService.companyDetail(downloadQueryConditionDTO.getCompanyId());
            if(company != null){
                companyName = company.getName();
            }
            oneObjectVO.setCompanyName(companyName);
            //查询在职人员数量
            Integer inOfficeCount = userMapper.queryInService(Integer.parseInt(deptId));
            if(inOfficeCount == null){
                oneObjectVO.setOnTheJob(0);
            }else{
                oneObjectVO.setOnTheJob(inOfficeCount);
            }
            //查询离职人员数量
            Map map = DateCalUtil.calFirstLastLongTime(Integer.parseInt(date.split("-")[0]), Integer.parseInt(date.split("-")[1]));
            Long firstDayLongTime = (Long) map.get("firstDayLongTime");
            Long lastDayLongTime = (Long) map.get("lastDayLongTime");
            Integer dimissionCount = userMapper.queryDimission(deptId,firstDayLongTime,lastDayLongTime );
            if (dimissionCount == null) {
                oneObjectVO.setOffTheJob(0);
            } else {
                oneObjectVO.setOffTheJob(dimissionCount);
            }
            Integer daysByMonth = DateCalUtil.getDaysByMonth(date);
            for (Integer i = 1; i <= daysByMonth; i++) {
                String dateToWeek = DateCalUtil.dateToWeek(date + "-" + i);
                //通过反射设置属性
                setProperty(oneObjectVO, "day" + i, dateToWeek);
            }

            oneObjectMap.put(departmentName, oneObjectVO);
        }
        ArrayList<Object> list = new ArrayList<>();
        list.add(attendanceMap);
        list.add(oneObjectMap);
        //排序后的全路径部门名称集合
        sortSuperDeptNameList(superDeptNameList);
        list.add(superDeptNameList);
        return list;

    }


    /**
     * 根据统计结果计算旷工天数
     *
     * @param attendanceNumber
     * @return
     */
    @Override
    public float countAbsenteeismDays(StatisticAttendanceNumber attendanceNumber,LocalDateTime firstDayOfMonth, LocalDateTime lastDayOfMonth,AttendanceRecord attendanceRecord,List<LocalDate> absenteeismDayList,List<LocalDate> attendanceDayList) {
        //旷工天数
        float absenteeismDays;
        //不连续的缺卡次数
        int notSignedFourCount = attendanceNumber.getNotSignedCount();
        //4次缺卡及以上算0.5天旷工
        absenteeismDays = attendanceNumber.getContinuousAbsenteeismDaysFour() + attendanceNumber.getContinuousAbsenteeismDaysTwo()
                + notSignedAbsenteeismDays(notSignedFourCount);
        attendanceRecord.setNotAttendanceAbsenteeismDays((float) (attendanceNumber.getContinuousAbsenteeismDaysFour() +
                attendanceNumber.getContinuousAbsenteeismDaysTwo()));
        attendanceRecord.setNotSignedAbsenteeismDays(notSignedAbsenteeismDays(notSignedFourCount));
        //迟到导致的旷工
        absenteeismDays += attendanceNumber.getLateAbsenteeismDays();
        //早退导致的旷工
        absenteeismDays += attendanceNumber.getEarlyAbsenteeismDays();
        //日报超过1个小时未交早交，累计导致的旷工
        absenteeismDays += reportAbsenteeismDays(firstDayOfMonth,lastDayOfMonth,attendanceRecord,absenteeismDayList,attendanceDayList);

        if (attendanceNumber.getLateCount() > 3) {
            ++absenteeismDays;
            attendanceRecord.setLateAbsenteeismDays(attendanceNumber.getLateAbsenteeismDays()+1);
        }else{
            attendanceRecord.setLateAbsenteeismDays(attendanceNumber.getLateAbsenteeismDays());
        }
        if (attendanceNumber.getEarlyCount() > 3) {
            ++absenteeismDays;
            attendanceRecord.setEarlyAbsenteeismDays(attendanceNumber.getEarlyAbsenteeismDays()+1);
        }else{
            attendanceRecord.setEarlyAbsenteeismDays(attendanceNumber.getEarlyAbsenteeismDays());
        }

        return absenteeismDays;
    }

    /**
     * 统计员工日报迟交早交超过60分钟导致的旷工
     * @param firstDayOfMonth
     * @param lastDayOfMonth
     * @param attendanceRecord
     * @return
     */
    private float reportAbsenteeismDays(LocalDateTime firstDayOfMonth, LocalDateTime lastDayOfMonth,AttendanceRecord attendanceRecord,List<LocalDate> absenteeismDayList,List<LocalDate> attendanceDayList){
        float reportAbsenteeismDays = 0;
        int reportEarlyCount = 0;
        //晚交60分钟以上或未交次数
        int reportLateCount = 0;
        QueryWrapper<ReportRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ReportRecord::getUserId, attendanceRecord.getUserId());
        queryWrapper.lambda().eq(ReportRecord::getCompanyId, attendanceRecord.getCompanyId());
        queryWrapper.lambda().and(e -> e.eq(ReportRecord::getStatus, ReportRecordTypeEnum.ABNORMAL.getKey())
                .or().eq(ReportRecord::getStatus, ReportRecordTypeEnum.WARN.getKey()));
        queryWrapper.lambda().between(ReportRecord::getReportTime, LocalDateTimeUtils.getMilliByTime(firstDayOfMonth), LocalDateTimeUtils.getMilliByTime(lastDayOfMonth));
        queryWrapper.lambda().eq(ReportRecord::getDeleted, 0);
        List<ReportRecord> reportRecordList = reportRecordService.list(queryWrapper);
        for(ReportRecord reportRecord : reportRecordList){
            ReportTemplate reportTemplate = reportTemplateService.getById(reportRecord.getTemplateId());
            LocalDateTime reportDateTime = LocalDateTimeUtils.getDateTimeOfTimestamp(reportRecord.getReportTime());
            LocalDate reportDate = reportDateTime.toLocalDate();
            String beginTime = reportTemplate.getBeginTime();
            String finishTime = reportTemplate.getFinishTime();
            LocalDateTime beginDateTime = LocalDateTime.of(reportDate, LocalTime.parse(beginTime));
            LocalDateTime finishDateTime = LocalDateTime.of(reportDate, LocalTime.parse(finishTime));

            if(reportDateTime.isBefore(beginDateTime)){
                ++reportEarlyCount;
                if(reportDateTime.isBefore(beginDateTime.minusHours(1))){
                    reportAbsenteeismDays += 0.5;
                }
            }
            if(reportDateTime.isAfter(finishDateTime.plusMinutes(61).minusNanos(1))){
                ++reportLateCount;
            }
        }
        //员工当月未提交的日报次数
        int unCommittedCount = reportUnCommittedCount(attendanceRecord,absenteeismDayList,attendanceDayList);
        reportLateCount += unCommittedCount;

        if(reportEarlyCount > 3){
            ++reportAbsenteeismDays;
            attendanceRecord.setReportEarlyAbsenteeismDays(reportAbsenteeismDays+1);
        }else{
            attendanceRecord.setReportEarlyAbsenteeismDays(reportAbsenteeismDays);
        }
        reportAbsenteeismDays += reportLateCount/3;
        attendanceRecord.setReportLateAbsenteeismDays((float)(reportLateCount/3));
        return reportAbsenteeismDays;
    }


    /**
     *  日报未提交个数
     * @param attendanceRecord
     * @return
     */
    private int reportUnCommittedCount(AttendanceRecord attendanceRecord,List<LocalDate> absenteeismDayList,List<LocalDate> attendanceDayList){
        int unCommittedCount = 0;
        for(LocalDate monthDay : attendanceDayList){
            LocalDateTime startDateTime = LocalDateTimeUtils.getDayStart(monthDay.atStartOfDay());
            LocalDateTime endDateTime = LocalDateTimeUtils.getDayEnd(monthDay.atStartOfDay());
            QueryWrapper<ReportRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ReportRecord::getUserId, attendanceRecord.getUserId());
            queryWrapper.lambda().eq(ReportRecord::getCompanyId, attendanceRecord.getCompanyId());
            queryWrapper.lambda().between(ReportRecord::getReportTime, LocalDateTimeUtils.getMilliByTime(startDateTime), LocalDateTimeUtils.getMilliByTime(endDateTime));
            queryWrapper.lambda().eq(ReportRecord::getDeleted, 0);
            List<ReportRecord> reportRecordList = reportRecordService.list(queryWrapper);
            if(CollectionUtils.isNotEmpty(reportRecordList)){
                continue;
            }
            //查看是否请假
            LocalDateTime dayStartTime = LocalDateTime.of(monthDay, LocalTime.of(9,00, 00));
            LocalDateTime dayEndTime = LocalDateTime.of(monthDay, LocalTime.of(18, 00, 00));
            QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getUserId, attendanceRecord.getUserId());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getCompanyId, attendanceRecord.getCompanyId());
            examineLeaveQueryWrapper.lambda().le(ExamineLeave::getFormValueStart, LocalDateTimeUtils.getMilliByTime(dayStartTime));
            examineLeaveQueryWrapper.lambda().ge(ExamineLeave::getFormValueFinish, LocalDateTimeUtils.getMilliByTime(dayEndTime));
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
            examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
            List<ExamineLeave> examineLeaveResult = examineLeaveService.list(examineLeaveQueryWrapper);
            if(CollectionUtils.isNotEmpty(examineLeaveResult)){
                continue;
            }
            //查看是否休假
            QueryWrapper<ExamineHoliday> examineHolidayQueryWrapper = new QueryWrapper<>();
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getUserId,  attendanceRecord.getUserId());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getCompanyId,  attendanceRecord.getCompanyId());
            examineHolidayQueryWrapper.lambda().le(ExamineHoliday::getFormValueStart, LocalDateTimeUtils.getMilliByTime(dayStartTime));
            examineHolidayQueryWrapper.lambda().ge(ExamineHoliday::getFormValueFinish, LocalDateTimeUtils.getMilliByTime(dayEndTime));
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getProcessResult, ExamineStatusEnum.AGREE.getKey());
            examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getDeleted, 0);
            List<ExamineHoliday> examineHolidayResult = examineHolidayService.list(examineHolidayQueryWrapper);
            if(CollectionUtils.isNotEmpty(examineHolidayResult)){
                continue;
            }
            if(!absenteeismDayList.contains(monthDay)){
                ++unCommittedCount;
            }
        }
        return unCommittedCount;
    }




    /**
     * 一个月不连续的缺卡，核算考勤（4次缺卡及以上算0.5天旷工）
     *
     * @param notSignedCount
     * @return
     */
    private float notSignedAbsenteeismDays(int notSignedCount) {
        float days = 0;
        for (int i = 4; i <= notSignedCount; i++) {
            days += 0.5;
        }
        return days;
    }


    /**
     * 打卡数据按时间正序
     *
     * @param content
     * @return
     */
    private String sortTime(String content) {
        Map<String, Object> map = OaMapUtils.stringToMap(content);
        Map<String, Object> mapSort = new LinkedHashMap<>();
        List<Map.Entry<String, Object>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        for (Map.Entry<String, Object> mapping : list) {

            //内部排序
            List<JSONObject> cps = (List<JSONObject>) mapping.getValue();

            // 按照打卡时间正序排序
            Collections.sort(cps, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    String s1 = String.valueOf(o1.get("baseCheckTime"));
                    String s2 = String.valueOf(o2.get("baseCheckTime"));
                    return s1.compareTo(s2);
                }
            });
            mapSort.put(mapping.getKey(), cps);
        }
        net.sf.json.JSONObject jsonObject = JSONObject.fromObject(mapSort);
        return jsonObject.toString();
    }

    /**
     * 打卡数据按时间正序
     * @param content
     * @return
     */
    @Override
    public String sortContentByTime(String content) {
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

    /**
     * 考勤组为2次的打卡点数据统计
     *
     * @param attendanceNumber
     * @param valMapList
     * @param attendanceRecord
     * @param departMentList
     */
    @Override
    public void continuousAbsenteeismDaysTwo(StatisticAttendanceNumber attendanceNumber, List<Map<String, Object>> valMapList, AttendanceRecord attendanceRecord, List<Integer> departMentList,LocalDate currentDate,List<LocalDate> absenteeismDayList,boolean isAbsenteeism) {
        if(isAbsenteeism){
            attendanceNumber.setAbsenteeismNotSignedCount(attendanceNumber.getAbsenteeismNotSignedCount() + 2);
            //1天内连续2次缺卡，算一天旷工
            attendanceNumber.setContinuousAbsenteeismDaysTwo(attendanceNumber.getContinuousAbsenteeismDaysTwo() + 1);
            absenteeismDayList.add(currentDate);
        }else{
            for (int i = 0; i < valMapList.size(); i++) {
                Map<String, Object> val = valMapList.get(i);
                if (!OaMapUtils.mapIsAnyBlank(val, EnumClockinMergeStatus.STATUS.getKey())) {
                    if (EnumClockinPointStatus.NOTSIGNED.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object userTime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        attendanceNumber.setNotSignedCount(attendanceNumber.getNotSignedCount() + 1);
                        //根据缺卡次数生成积分变动记录,第一次警告不计分
                        if (!attendanceNumber.isWarnedNotSignedCount()&&attendanceNumber.getNotSignedCount()>0) {
                            unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                        } else {
                            if (attendanceNumber.getNotSignedCount() == 2) {
                                unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDTWO.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                            } else if (attendanceNumber.getNotSignedCount() == 3) {
                                unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDTHREE.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                            }
                        }
                    }
                    if (EnumClockinPointStatus.EARLY.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object baseTime = val.get(EnumClockinMergeStatus.BASECHECKTIME.getKey());
                        Object userTime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        if (baseTime != null && userTime != null) {
                            LocalDateTime baseCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(baseTime.toString()));
                            LocalDateTime userCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(userTime.toString()));
                            if (userCheckTime.isBefore(baseCheckTime.minusHours(1))) {
                                attendanceNumber.setEarlyAbsenteeismDays(attendanceNumber.getEarlyAbsenteeismDays() + 0.5f);
                            }

                            if (userCheckTime.isAfter(baseCheckTime.minusMinutes(30).minusNanos(1))) {
                                //根据早退时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LEAVEEARLYTHIRTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                                }
                            }
                            if (userCheckTime.isBefore(baseCheckTime.minusMinutes(30)) &&
                                    userCheckTime.isAfter(baseCheckTime.minusMinutes(60).minusNanos(1))) {
                                //根据早退时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LEAVEEARLYSIXTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                                }
                            }

                        }
                        attendanceNumber.setEarlyCount(attendanceNumber.getEarlyCount() + 1);
                    }
                    if (EnumClockinPointStatus.LATE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object usertime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        Object basetime = val.get(EnumClockinMergeStatus.BASECHECKTIME.getKey());
                        if (basetime != null && usertime != null) {
                            LocalDateTime userCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(usertime.toString()));
                            LocalDateTime baseCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(basetime.toString()));
                            if (userCheckTime.isAfter(baseCheckTime.plusMinutes(61).minusNanos(1))) {
                                attendanceNumber.setLateAbsenteeismDays(attendanceNumber.getLateAbsenteeismDays() + 0.5f);
                            }
                            if (userCheckTime.isBefore(baseCheckTime.plusMinutes(31))) {
                                //根据迟到时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATETHIRTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }
                            if (userCheckTime.isAfter(baseCheckTime.plusMinutes(31).minusNanos(1)) &&
                                    userCheckTime.isBefore(baseCheckTime.plusMinutes(61))) {
                                //根据迟到时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATESIXTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }

                        }
                        attendanceNumber.setLateCount(attendanceNumber.getLateCount() + 1);
                    }
                    if (LeaveTypeEnum.CASUALLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setCasualLeaveDays(attendanceNumber.getCasualLeaveDays() + 0.5f);
                    }
                    if (EnumClockinPointStatus.BUSINESSTRAVEL.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        attendanceNumber.setBusinessTravelDays(attendanceNumber.getBusinessTravelDays() + 0.5f);
                    }
                    if (EnumClockinPointStatus.HOLIDAY.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        attendanceNumber.setHolidayDays(attendanceNumber.getHolidayDays() + 0.5f);
                    }
                    if (LeaveTypeEnum.SICKLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                                attendanceNumber.setUnPaidSickLeaveDays(attendanceNumber.getUnPaidSickLeaveDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setSickLeaveDays(attendanceNumber.getSickLeaveDays() + 0.5f);
                    }
                    if (LeaveTypeEnum.MATERNITYLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setMaternityLeaveDays(attendanceNumber.getMaternityLeaveDays() + 0.5f);
                    }
                    if (LeaveTypeEnum.PATAERNITYLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setPaternityLeaveDays(attendanceNumber.getPaternityLeaveDays() + 0.5f);
                    }
                    if (LeaveTypeEnum.MARRIAGELEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setMarriageLeaveDays(attendanceNumber.getMarriageLeaveDays() + 0.5f);
                    }
                    if (LeaveTypeEnum.FUNERALLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.5f);
                            }
                        }
                        attendanceNumber.setFuneralLeaveDays(attendanceNumber.getFuneralLeaveDays() + 0.5f);
                    }
                }
            }
        }
    }


    /**
     * 考勤组为4次的打卡点数据统计
     * @param attendanceNumber
     * @param valMapList
     * @param attendanceRecord
     * @param departMentList
     */
    @Override
    public void continuousAbsenteeismDaysFour(StatisticAttendanceNumber attendanceNumber, List<Map<String, Object>> valMapList, AttendanceRecord attendanceRecord, List<Integer> departMentList,LocalDate currentDate,List<LocalDate> absenteeismDayList,boolean isAbsenteeism) {
        if(isAbsenteeism){
            attendanceNumber.setAbsenteeismNotSignedCount(attendanceNumber.getAbsenteeismNotSignedCount() + 4);
            //1天内连续4次未打卡，算一天旷工
            attendanceNumber.setContinuousAbsenteeismDaysFour(attendanceNumber.getContinuousAbsenteeismDaysFour() + 1);
            absenteeismDayList.add(currentDate);
        }else{
            for (int i = 0; i < valMapList.size(); i++) {
                Map<String, Object> val = valMapList.get(i);
                if (!OaMapUtils.mapIsAnyBlank(val, EnumClockinMergeStatus.STATUS.getKey(), EnumClockinMergeStatus.ID.getKey())) {
                    if (EnumClockinPointStatus.NOTSIGNED.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object userTime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        attendanceNumber.setNotSignedCount(attendanceNumber.getNotSignedCount() + 1);
                        //根据缺卡次数生成积分变动记录,第一次警告不计分
                        if (!attendanceNumber.isWarnedNotSignedCount()&&attendanceNumber.getNotSignedCount()>0) {
                            unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                        } else {
                            if (attendanceNumber.getNotSignedCount() == 2) {
                                unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDTWO.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                            } else if (attendanceNumber.getNotSignedCount() == 3) {
                                unClockInlateEarlyScoreRecord(EnumScoreRule.NOTSIGNEDTHREE.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(userTime.toString()));
                            }
                        }
                    }
                    if (EnumClockinPointStatus.EARLY.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object basetime = val.get(EnumClockinMergeStatus.BASECHECKTIME.getKey());
                        Object usertime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        if (basetime != null && usertime != null) {
                            LocalDateTime baseCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(basetime.toString()));
                            LocalDateTime userCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(usertime.toString()));
                            if (userCheckTime.isBefore(baseCheckTime.minusMinutes(60))) {
                                attendanceNumber.setEarlyAbsenteeismDays(attendanceNumber.getEarlyAbsenteeismDays() + 0.5f);
                            }
                            if (userCheckTime.isAfter(baseCheckTime.minusMinutes(30).minusNanos(1))) {
                                //根据早退时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LEAVEEARLYTHIRTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }
                            if (userCheckTime.isBefore(baseCheckTime.minusMinutes(30)) &&
                                    userCheckTime.isAfter(baseCheckTime.minusMinutes(60).minusNanos(1))) {
                                //根据早退时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LEAVEEARLYSIXTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }
                        }
                        attendanceNumber.setEarlyCount(attendanceNumber.getEarlyCount() + 1);
                    }
                    if (EnumClockinPointStatus.LATE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object basetime = val.get(EnumClockinMergeStatus.BASECHECKTIME.getKey());
                        Object usertime = val.get(EnumClockinMergeStatus.USERCHECKTIME.getKey());
                        if (basetime != null && usertime != null) {
                            LocalDateTime baseCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(basetime.toString()));
                            LocalDateTime userCheckTime = LocalDateTimeUtils.getDateTimeOfTimestamp(Long.parseLong(usertime.toString()));
                            if (userCheckTime.isAfter(baseCheckTime.plusMinutes(61).minusNanos(1))) {
                                attendanceNumber.setLateAbsenteeismDays(attendanceNumber.getLateAbsenteeismDays() + 0.5f);
                            }

                            if (userCheckTime.isBefore(baseCheckTime.plusMinutes(31))) {
                                //根据迟到时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATETHIRTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }
                            if (userCheckTime.isAfter(baseCheckTime.plusMinutes(31).minusNanos(1)) &&
                                    userCheckTime.isBefore(baseCheckTime.plusMinutes(61))) {
                                //根据迟到时间生成积分变动记录
                                if (!attendanceNumber.isWarnedLateEarly()) {
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATEEARLYFIRSTWARNING.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }else{
                                    unClockInlateEarlyScoreRecord(EnumScoreRule.LATESIXTYMINUTES.getKey(), attendanceRecord, departMentList, attendanceNumber,Long.parseLong(usertime.toString()));
                                }
                            }
                        }
                        attendanceNumber.setLateCount(attendanceNumber.getLateCount() + 1);
                    }
                    if (EnumClockinPointStatus.BUSINESSTRAVEL.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        attendanceNumber.setBusinessTravelDays(attendanceNumber.getBusinessTravelDays() + 0.25f);
                    }
                    if (EnumClockinPointStatus.HOLIDAY.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        attendanceNumber.setHolidayDays(attendanceNumber.getHolidayDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.CASUALLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setCasualLeaveDays(attendanceNumber.getCasualLeaveDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.SICKLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                                attendanceNumber.setUnPaidSickLeaveDays(attendanceNumber.getUnPaidSickLeaveDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setSickLeaveDays(attendanceNumber.getSickLeaveDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.MATERNITYLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setMaternityLeaveDays(attendanceNumber.getMaternityLeaveDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.PATAERNITYLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setPaternityLeaveDays(attendanceNumber.getPaternityLeaveDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.MARRIAGELEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setMarriageLeaveDays(attendanceNumber.getMarriageLeaveDays() + 0.25f);
                    }
                    if (LeaveTypeEnum.FUNERALLEAVE.getKey().equals(val.get(EnumClockinMergeStatus.STATUS.getKey()))) {
                        Object isPaid = val.get(EnumClockinPointStatus.ISPAID.getKey());
                        if (isPaid != null) {
                            if (LeaveTypeEnum.UNPAID.getKey().equals(isPaid.toString())) {
                                attendanceNumber.setNoPaidDays(attendanceNumber.getNoPaidDays() + 0.25f);
                            }
                        }
                        attendanceNumber.setFuneralLeaveDays(attendanceNumber.getFuneralLeaveDays() + 0.25f);
                    }
                }
            }
        }
    }


    /**
     * 通过反射设置对象的属性值
     *
     * @param obj          该类的对象
     * @param propertyName 属性名
     * @param value        值
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setProperty(Object obj, String propertyName, String value) throws NoSuchFieldException, IllegalAccessException {
        //获取obj类的字节文件对象
        Class<?> aClass = obj.getClass();
        //获取该类的成员变量
        Field field = aClass.getDeclaredField(propertyName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    /**
     * 合并相同内容的单元格
     *
     */
    private void mergeSameCell(Workbook workbook, Integer days) {
        //判断序号列下相同的序号进行合并，合并序号的同时，合并其他列相同的考勤内容
        //定义每次要跳过的行数
        int m = 0;
        //获得sheet工作簿
        Sheet sheetAt0 = workbook.getSheetAt(0);
        int totalRows = sheetAt0.getLastRowNum();
        //遍历行，从第6行开始才是数据行
        for (int i = 6; i < totalRows - 2; i += m) {
            String number = null;
            String number1 = null;
            String number2 = null;
            String number3 = null;
            String number4 = null;
            String number5 = null;
            number = sheetAt0.getRow(i).getCell(0).getStringCellValue();
            //获取每一个单元格的值
            if ((i + 1) < totalRows - 1) {
                number1 = sheetAt0.getRow(i + 1).getCell(0).getStringCellValue();
            }
            if ((i + 2) < totalRows - 1) {
                number2 = sheetAt0.getRow(i + 2).getCell(0).getStringCellValue();
            }
            if ((i + 3) < totalRows - 1) {
                number3 = sheetAt0.getRow(i + 3).getCell(0).getStringCellValue();
            }
            if ((i + 4) < totalRows - 1) {
                number4 = sheetAt0.getRow(i + 4).getCell(0).getStringCellValue();
            }
            if ((i + 5) < totalRows - 1) {
                number5 = sheetAt0.getRow(i + 5).getCell(0).getStringCellValue();
            }
            //将第一个单元格的值和它下面其他单元格的值进行对比
            //如果该员工一天只有一条记录（只打一次卡），不需合并，接着对比下面数据
            if (!number.equals(number1)) {
                m = 1;
                continue;
            }
            if (number.equals(number5)) {
                mergeAllCol(i, 5, sheetAt0, days);
                m = 6;
            } else if (number.equals(number4)) {
                mergeAllCol(i, 4, sheetAt0, days);
                m = 5;
            } else if (number.equals(number3)) {
                mergeAllCol(i, 3, sheetAt0, days);
                //证明有4条记录，所以设置跳过行数为4
                m = 4;
            } else if (number.equals(number2)) {
                mergeAllCol(i, 2, sheetAt0, days);
                m = 3;
            } else if (number.equals(number1)) {
                mergeAllCol(i, 1, sheetAt0, days);
                //证明有2条记录，所以设置跳过行数为2
                m = 2;
            }
        }

    }

    @Override
    public void sendToAttendanceConfirmation(final String companyId, String month) {
        // 异步调用
        CompletableFuture cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            //执行异常，自己获取、自己处理
            dingDingNotifyService.sendToAttendanceConfirmation(companyId, month);
            return "success";
        })
                .thenApplyAsync(s -> {
                    QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapperSuccess = new QueryWrapper<>();
                    attendanceRecordQueryWrapperSuccess.lambda().eq(AttendanceRecord::getMonth,month);
                    attendanceRecordQueryWrapperSuccess.lambda().eq(AttendanceRecord::getDeleted,0);
                    attendanceRecordQueryWrapperSuccess.lambda().eq(AttendanceRecord::getUserConfirm,1);
                    attendanceRecordQueryWrapperSuccess.lambda().eq(AttendanceRecord::getCompanyId,companyId);
                    List<AttendanceRecord>  attendanceRecordList = attendanceRecordMapper.selectList(attendanceRecordQueryWrapperSuccess);

                    int size = attendanceRecordList.size();

                    QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapper = new QueryWrapper<>();
                    attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getMonth,month);
                    attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getDeleted,0);
                    attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getUserConfirm,0);
                    attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getCompanyId,companyId);
                    List<AttendanceRecord>  attendanceRecordListFail = attendanceRecordMapper.selectList(attendanceRecordQueryWrapper);

                    int sizeFail = attendanceRecordListFail.size();

                    TextNotifyDTO textNotifyDTO = new TextNotifyDTO();
                    textNotifyDTO.setCompanyId(companyId);
                    SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                    Date d1= new Date();
                    String str1 = sdf1.format(d1);
                    textNotifyDTO.setContent("考勤结果发送任务,执行成功，发送成功"+size+"条,发送失败"+sizeFail+"条,最后执行时间:"+str1);
                    textNotifyDTO.setToAllUser(false);
                    dingDingNotifyService.taskExceptionNotice(textNotifyDTO);

                    return "success";
                })
                .exceptionally(ex -> {

                    TextNotifyDTO textNotifyDTO = new TextNotifyDTO();
                    textNotifyDTO.setCompanyId(companyId);
                    SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                    Date d1= new Date();
                    String str1 = sdf1.format(d1);
                    textNotifyDTO.setContent("考勤结果发送任务,执行失败,失败原因："+ex.getMessage()+",最后执行时间:"+str1);
                    textNotifyDTO.setToAllUser(false);
                    dingDingNotifyService.taskExceptionNotice(textNotifyDTO);
                    //失败通知
                    return "failed!";
                });
    }

    @Override
    public void sendToAttendanceConfirmationPerson(String id) {
        dingDingNotifyService.sendToAttendanceConfirmationPerson(id);
    }

    /**
     * 抽取合并序号，姓名，考勤内容的公共方法
     *
     * @param start 一名员工的第一条记录的行号
     * @param num   start+num是该员工最后一条记录的行号
     * @param sheetAt0 工作簿
     * @param days 月份对应的总天数
     */
    private void mergeAllCol(Integer start, Integer num, Sheet sheetAt0, Integer days) {
        //第0列（序号列）到第（15+days）列（实际出勤日）
        for (int i = 0; i <= 15 + days; i++) {
            String startValue = null;
            String numValue = null;
            //如果该员工的第一条数据和最后一条数据相同，合并该员工这一列的数据
            boolean flag = true;
            for (int j = start; j < start+num; j++) {
                startValue = sheetAt0.getRow(j).getCell(i).getStringCellValue();
                numValue = sheetAt0.getRow(j+1).getCell(i).getStringCellValue();
                if(startValue != numValue){
                    flag = false;
                }
            }
            if(flag == true){
                CellRangeAddress cellRangeAddressi = new CellRangeAddress(start, start + num, i, i);
                sheetAt0.addMergedRegion(cellRangeAddressi);
            }

        }
    }

    /**
     * 对部门全路径进行排序（降序）
     * @param superDeptName
     */
    private void sortSuperDeptNameList(List<String> superDeptName){
        Collections.sort(superDeptName, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s2.compareTo(s1);
            }
        });
    }
}