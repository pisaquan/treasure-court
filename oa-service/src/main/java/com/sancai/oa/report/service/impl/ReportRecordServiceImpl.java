package com.sancai.oa.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiReportTemplateListbyuseridResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.AttendanceRecord;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordMergeDTO;
import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.clockin.mapper.ClockinRecordMapper;
import com.sancai.oa.clockin.mapper.ClockinRecordMergeMapper;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.examine.entity.ExamineHoliday;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.mapper.ExamineHolidayMapper;
import com.sancai.oa.examine.mapper.ExamineLeaveMapper;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.*;
import com.sancai.oa.report.entity.enums.ReportRecordTypeEnum;
import com.sancai.oa.report.entity.enums.ReportTemplateTypeEnum;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.mapper.ReportDepartmentMapper;
import com.sancai.oa.report.mapper.ReportRecordMapper;
import com.sancai.oa.report.mapper.ReportRuleMapper;
import com.sancai.oa.report.mapper.ReportTemplateMapper;
import com.sancai.oa.report.service.IReportDepartmentService;
import com.sancai.oa.report.service.IReportRecordService;
import com.sancai.oa.report.service.IReportTemplateService;
import com.sancai.oa.report.thread.NoReportUserThreadTask;
import com.sancai.oa.report.thread.ReportThreadTask;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.mapper.ActionScoreRecordMapper;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserExcelDTO;
import com.sancai.oa.user.exception.EnumUserError;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志记录 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Service
@Slf4j
public class ReportRecordServiceImpl extends ServiceImpl<ReportRecordMapper, ReportRecord> implements IReportRecordService {


    @Autowired
    private ReportRuleMapper treportRuleMapper;

    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private ISigninRecordService isigninRecordService;

    @Autowired
    private IReportTemplateService reportTemplateService;
    @Autowired
    private IActionScoreDeductService actionScoreDeductService;

    @Autowired
    private ExamineLeaveMapper examineLeaveMapper;
    @Autowired
    private ExamineHolidayMapper examineHolidayMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private DingDingReportService dingDingReportService;

    @Autowired
    private ReportDepartmentMapper tReportDepartmentMapper;
    @Autowired
    private ReportRecordMapper treportRecordMapper;
    @Autowired
    private IActionScoreRecordService actionScoreRecordService;
    @Autowired
    private IReportDepartmentService iReportDepartmentService;
    @Autowired
    private ClockinRecordMergeMapper clockinRecordMergeMapper;
    @Autowired
    private IAttendanceRecordService attendanceRecordService;
    @Autowired
    private ITaskInstanceService taskInstanceService;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    private ThreadPoolTool threadPoolTool;
    @Autowired
    private IReportRecordService reportRecordService;
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;
    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;
    @Autowired
    private ITaskInstanceService iTaskInstanceService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 导入日报记录数据
     */
    @Override
    public boolean importEveryDayReportData(String taskInstanceId, String companyId){

        TaskInstance taskInstance = iTaskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            return false;
        }
        //如果根据实例id查到值,根据实例id删除之前的数据 , 重新获取抓取开始时间和结束时间
        List<ReportRecord> reportRecordList = reportRecordList(taskInstanceId);
        if(CollectionUtils.isNotEmpty(reportRecordList)){
            //TODO 根据companyId + UserId + checkinTime + taskInstanceId 把记录对应的部门集合存入缓存
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                List<DataMap> reportRecordLists = treportRecordMapper.reportRecordDeptIdList(companyId,taskInstanceId);
                if(reportRecordLists != null && reportRecordLists.size() >0){
                    isigninRecordService.recordOriginDeptIdsSaveRedis(reportRecordLists);
                }

            }
            //删除日报记录，部门，积分记录
            reportRecordDataDelete(reportRecordList);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        long start = taskInstanceTime.getStartTime();
        long end = taskInstanceTime.getEndTime();
        //钉钉日报记录数据，时间跨度如果超过7天，需要拆分为多段

        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(start, end, 7);
        boolean isFinish = false;
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //导入日报记录数据
             importEveryDayReportData(companyId, intervalTimeStart, intervalTimeEnd, taskInstanceId,isFinish);
        }
        TaskMessage.finishMessage(taskInstanceId);
        return true;
    }
    /**
     * 根据实例id查询日报数据
     * @param instanceId
     * @return
     */
    private List<ReportRecord> reportRecordList(String instanceId) {
        QueryWrapper<ReportRecord> reportRecordQueryWrapper = new QueryWrapper<>();
        reportRecordQueryWrapper.lambda().eq(ReportRecord::getTaskInstanceId,instanceId);
        reportRecordQueryWrapper.lambda().eq(ReportRecord::getDeleted,0);
        List<ReportRecord> reportRecordList = list(reportRecordQueryWrapper);
        return reportRecordList;
    }

    /**
     * 删除日报数据
     * @param
     */
    private void reportRecordDataDelete(List<ReportRecord> recordList) {

        List<List<ReportRecord>> reportRecordLists = ListUtils.fixedGrouping(recordList,5000);
        for(List<ReportRecord> reportRecordList :reportRecordLists){
            if(reportRecordList == null || reportRecordList.size() == 0){
                continue;
            }
            List<String> ids = new ArrayList<>();
            List<String> recordIds = new ArrayList<>();
            //删除记录
            reportRecordList.stream().forEach(ReportRecord -> {
                ReportRecord.setDeleted(1);
                ids.add(ReportRecord.getId());
                updateById(ReportRecord);
            });
            //删除部门关系表
            ReportDepartment reportDepartment = new ReportDepartment();
            reportDepartment.setDeleted(1);
            UpdateWrapper<ReportDepartment> reportDepartmentUpdateWrapper = new UpdateWrapper<>();
            reportDepartmentUpdateWrapper.lambda().in(ReportDepartment::getReportInstanceId,ids);
            iReportDepartmentService.update(reportDepartment,reportDepartmentUpdateWrapper);

            //删除积分记录数据
            QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
            actionScoreRecordQueryWrapper.lambda().in(ActionScoreRecord::getTargetId,ids);
            actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted,0);
            List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);
            if(!CollectionUtils.isEmpty(actionScoreRecordList)){
                actionScoreRecordList.stream().forEach(actionScoreRecord -> {
                    actionScoreRecord.setDeleted(1);
                    recordIds.add(actionScoreRecord.getId());
                    actionScoreRecordService.updateById(actionScoreRecord);
                });
                //删除积分记录对应的部门关系表
                ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                actionScoreDepartment.setDeleted(1);
                UpdateWrapper<ActionScoreDepartment> actionScoreDepartmentUpdateWrapper = new UpdateWrapper<>();
                actionScoreDepartmentUpdateWrapper.lambda().in(ActionScoreDepartment::getScoreRecordId,recordIds);
                actionScoreDepartmentService.update(actionScoreDepartment,actionScoreDepartmentUpdateWrapper);
            }
        }


    }

    /**
     * 导入日报记录数据
     *
     * @param companyId 公司id
     * @return boolean false导入失败，true 导入成功
     */
    @Override
    public void importEveryDayReportData(String companyId, long intervalTimeStart, long intervalTimeEnd, String taskInstanceId, boolean isFinish) {
        if (StringUtils.isAllBlank(companyId)) {
            log.error("导入日报数据companyId为空");
            throw new OaReportlException(EnumReportError.PARAMETER_IS_NULL_COMPANYID);
        }
        TaskMessage.addMessage(taskInstanceId,"日报抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> allReportTemplate = dingDingReportService.reportTemplate(null, companyId);
        if (allReportTemplate == null || allReportTemplate.size() == 0) {
            return;
        }
        Map<String, OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> templateMap = allReportTemplate.stream().collect(Collectors.toMap(OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo::getName, a -> a,(k1, k2)->k1));

        for (String templateName : templateMap.keySet()) {
            OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo templateVo = templateMap.get(templateName);
            ReportTemplate reportTemplate = reportTemplateService.reportTemplateDetailByCode(templateVo.getReportCode());
            if(reportTemplate == null){
                reportTemplateService.addTemplate(templateVo,companyId);
            }
        }
        //查询日志模板数据
        QueryWrapper<ReportTemplate> templateQueryWrapper = new QueryWrapper<>();
        templateQueryWrapper.lambda().eq(ReportTemplate::getCompanyId, companyId);
        templateQueryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
        templateQueryWrapper.lambda().eq(ReportTemplate::getStatus, ReportTemplateTypeEnum.VALID.getKey());
        templateQueryWrapper.lambda().ne(ReportTemplate::getName, "拜访记录");
        List<ReportTemplate> reportTemplateList = reportTemplateMapper.selectList(templateQueryWrapper);

        int threadCount = 5;

        long startTime = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<>();
        params.put("taskInstanceId",taskInstanceId);
        params.put("companyId",companyId);
        params.put("intervalTimeStart",intervalTimeStart);
        params.put("intervalTimeEnd",intervalTimeEnd);

        params.put("reportRecordService",reportRecordService);
        params.put("dingDingReportService",dingDingReportService);
        params.put("tReportDepartmentMapper",tReportDepartmentMapper);
        params.put("treportRecordMapper",treportRecordMapper);
        params.put("treportRuleMapper",treportRuleMapper);
        params.put("userService",userService);
        params.put("taskInstanceService",taskInstanceService);
        params.put("redisUtil",redisUtil);

        params.put("startTime",startTime);
        params.put("isFinish",isFinish);
        threadPoolTool.excuteTask(transactionManager,taskInstanceId,reportTemplateList,threadCount,params,isFinish, ReportThreadTask.class);
    }


    /**
     * 未写日报的员工进扣分
     * @param companyId
     * @param start
     * @param end
     */
    private void  reportCheck (String taskInstanceId, String companyId ,long start,long end){
        TaskMessage.addMessage(taskInstanceId,"未写日报的员工进扣分  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(start)+" 到 "+ LocalDateTimeUtils.formatDateTime(end));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 20, 200, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(500));

        List<UserExamineLeaveHolidayDTO> openEmpSimples = new ArrayList<UserExamineLeaveHolidayDTO>();
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(start,end,1);
        //如果是多天，每天都要对未写日报的人做扣分
        for (Pair intervalTime : intervalTimes) {
            UserExamineLeaveHolidayDTO ao = new UserExamineLeaveHolidayDTO();
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            TaskMessage.addMessage(taskInstanceId,"抓取时间范围按每天拆分: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
            //请假 条件company_id， deleted = 0:未删 , process_result = agree审批结果  ， form_value_finish表单内容：结束时间
            List<ExamineLeave> examineLeaves = examineLeavesList(companyId , intervalTimeStart , intervalTimeEnd);
            ao.setExamineLeaveList(examineLeaves);
            //公休假 条件 user_id ，company_id， deleted = 0:未删 , process_result = agree审批结果  ， form_value_finish表单内容：结束时间
            List<ExamineHoliday> examineHolidays = examineHolidaysList(companyId , intervalTimeStart , intervalTimeEnd);
            ao.setExamineHolidayList(examineHolidays);
            QueryWrapper<ReportRecord> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().eq(ReportRecord::getDeleted, 0);
            queryWrapper2.lambda().eq(ReportRecord::getCompanyId, companyId);
            queryWrapper2.lambda().ge(ReportRecord::getReportTime, intervalTimeStart);
            queryWrapper2.lambda().le(ReportRecord::getReportTime, intervalTimeEnd);

            List<ReportRecord>  listRecord = treportRecordMapper.selectList(queryWrapper2);

            Map<String, ReportRecord> reportMap = listRecord.stream().collect(Collectors.toMap(ReportRecord::getUserId, a -> a,(k1, k2)->k1));

            QueryWrapper<User> queryWrapperUser = new QueryWrapper<>();
            queryWrapperUser.eq("company_id",companyId);
            queryWrapperUser.eq("status",0);
            queryWrapperUser.eq("deleted", 0);
            queryWrapperUser.notIn("user_id",reportMap.keySet());

            //未提交日志的所有员工集合
            List<User> users = userService.list(queryWrapperUser);
            TaskMessage.addMessage(taskInstanceId,"抓取日报时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd)+"未写日报人数"+ users.size());

            if(users == null || users.size() == 0){
                continue;
            }

            ao.setUserList(users);
            ao.setIntervalTimeEnd(intervalTimeEnd);
            openEmpSimples.add(ao);

        }


        LinkedBlockingQueue queue = new LinkedBlockingQueue();

        queue.addAll(openEmpSimples);
        TaskMessage.addMessage(taskInstanceId,"对未提交日报的用户检查开始  companyId："+companyId);
        while(true) {
            UserExamineLeaveHolidayDTO ao = (UserExamineLeaveHolidayDTO) queue.poll();
            if (ao == null || ao.getUserList()==null){
                break;
            }
            // 未提交日报的用户检查
            NoReportUserThreadTask task = new NoReportUserThreadTask(companyId, ao.getUserList(),ao.getExamineLeaveList(),ao.getExamineHolidayList(),actionScoreDeductService, ao.getIntervalTimeEnd(),actionScoreRecordService);
            executor.execute(task);
        }
        TaskMessage.addMessage(taskInstanceId,"未提交日报的用户检查结束  companyId："+companyId);
    }




    /**
     * 公休假 条件 user_id ，company_id， deleted = 0:未删 , process_result = agree审批结果  ， form_value_finish表单内容：结束时间
     * @param companyId
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @return
     */
    private List<ExamineHoliday>  examineHolidaysList  (String companyId , long intervalTimeStart ,long intervalTimeEnd){
        QueryWrapper<ExamineHoliday> queryWrapper4 = new QueryWrapper<>();
        queryWrapper4.lambda().eq(ExamineHoliday::getDeleted, 0);
        queryWrapper4.lambda().eq(ExamineHoliday::getCompanyId, companyId);
        queryWrapper4.lambda().eq(ExamineHoliday::getProcessResult, ExamineStatusEnum.AGREE.getKey());
        queryWrapper4.lambda().ge(ExamineHoliday::getFormValueFinish, intervalTimeStart);
        queryWrapper4.lambda().le(ExamineHoliday::getFormValueFinish, intervalTimeEnd);
        List<ExamineHoliday>  examineHolidays = examineHolidayMapper.selectList(queryWrapper4);
        return examineHolidays;
    }

    /**
     * 请假 条件company_id， deleted = 0:未删 , process_result = agree审批结果  ， form_value_finish表单内容：结束时间
     * @param companyId
     * @param intervalTimeStart
     * @param intervalTimeEnd
     * @return
     */
    private List<ExamineLeave>  examineLeavesList  (String companyId , long intervalTimeStart ,long intervalTimeEnd){
        QueryWrapper<ExamineLeave> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.lambda().eq(ExamineLeave::getDeleted, 0);
        queryWrapper3.lambda().eq(ExamineLeave::getCompanyId, companyId);
        queryWrapper3.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
        queryWrapper3.lambda().ge(ExamineLeave::getFormValueFinish, intervalTimeStart);
        queryWrapper3.lambda().le(ExamineLeave::getFormValueFinish, intervalTimeEnd);
        List<ExamineLeave>  examineLeaves = examineLeaveMapper.selectList(queryWrapper3);
        return examineLeaves;
    }

    /**
     * 获取子公司日志记录列表
     *
     * @param reportRecordDTO 入参
     * @return ApiResponse
     */
    @Override
    public List<DataMap> recordListByCompany(ReportRecordDTO reportRecordDTO) {
        if (StringUtils.isBlank(reportRecordDTO.getCompanyId())) {
            throw new OaReportlException(EnumReportError.PARAMETER_IS_NULL);
        }
        int pages = reportRecordDTO.getPage();
        int capacity = reportRecordDTO.getCapacity();
        List<Department> result = departmentService.listDepartment(reportRecordDTO.getCompanyId());
        if(StringUtils.isNotBlank(reportRecordDTO.getDeptId())){
            List<Long> longList = departmentService.listSubDepartment(reportRecordDTO.getCompanyId(),reportRecordDTO.getDeptId());
            reportRecordDTO.setDeptList(longList);
        }
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);
        List<DataMap> treportRecord = treportRecordMapper.reportRecordList(reportRecordDTO);
        if (treportRecord != null && treportRecord.size() > 0) {
            for (DataMap dataMap : treportRecord) {
                String deptId = dataMap.get("dept_ids").toString();
                String deptName = isigninRecordService.getDeptName(result, deptId);
                dataMap.put("dept_name", deptName);
                if (!OaMapUtils.mapIsAnyBlank(dataMap, "status")) {
                    String status = dataMap.get("status").toString();
                    dataMap.put("status", ReportRecordTypeEnum.getMsgByValue(status));
                }
            }
        }
        return treportRecord;
    }


    /**
     * 获取子公司日志记录详情
     *
     * @param id 详情id
     * @return ApiResponse
     */
    @Override
    public DataMap reportDetail(String id) {
        DataMap reportDetail = treportRecordMapper.selectReportDetailsByPrimary(id);
        if (reportDetail != null) {
            if (!OaMapUtils.mapIsAnyBlank(reportDetail, "status")) {
                reportDetail.put("status", ReportRecordTypeEnum.getMsgByValue(reportDetail.get("status").toString()));
            }
        }
        return reportDetail;
    }

    /**
     * 修改日志状态
     *
     * @param reportRecord
     * @return boolean
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void recordStatusAmend(ReportRecord reportRecord) {
        if(StringUtils.isBlank(reportRecord.getId())){
            throw new OaReportlException(EnumUserError.ID_IS_NULL);
        }
        QueryWrapper<ReportRecord> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ReportRecord::getId, reportRecord.getId());
        wrapper.lambda().eq(ReportRecord::getDeleted, 0);
        ReportRecord record =  treportRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new OaReportlException(EnumReportError.NO_DATA_EXIST_AND_STATUS);
        }
        if(!record.getStatus().equals(ReportRecordTypeEnum.ABNORMAL.getKey())){
            throw new OaReportlException(EnumReportError.NO_DATA_EXIST_AND_STATUS);
        }
        UserDTO user = userService.getUserByUserId(record.getUserId(),record.getCompanyId());
        if(user == null){
            throw new OaReportlException(EnumUserError.USER_NOT_FOUND);
        }
        ActionScoreRecord actionScoreRecord = null;
        //查询是否扣过积分(目前有两种情况日报不警告不扣积分：1,提交日报当天未打卡旷工 2,日报早交60分钟以上不警告算旷工)
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted,0);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getCompanyId,record.getCompanyId());
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getUserId,record.getUserId());
        List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);

        if(actionScoreRecordList != null && actionScoreRecordList.size() > 0){
            actionScoreRecord =  actionScoreRecordList.stream().filter(ActionScoreRecord -> ActionScoreRecord.getTargetId().contains(reportRecord.getId())).findAny().orElse(null);
        }
        //加积分
        actionScoreDeductService.reportDeductScore(record.getCompanyId(), user, EnumScoreRule.AMENDSUBMISSIONTIMEINCONSISTENCY.getKey(),record,record.getUnqualifiedReason(),actionScoreRecord);
        //该状态
        record.setStatus(ReportRecordTypeEnum.NORMAL.getKey());
        record.setUnqualifiedReason(record.getUnqualifiedReason()+"(已订正)");
        record.setModifyTime(System.currentTimeMillis());
        treportRecordMapper.updateReportRecordStateById(record);
        //日志订正后之后判断是否同步合并和统计数据
        recountAttendance(record);
    }
    /**
     * 日志订正后之后判断是否同步合并和统计数据
     * @param reportRecord
     */
    private void recountAttendance(ReportRecord reportRecord){
        String userId = reportRecord.getUserId();
        String companyId = reportRecord.getCompanyId();
        Long  reportTime = reportRecord.getReportTime();
        String month = LocalDateTimeUtils.formatDateTime(reportTime,"yyyy-MM");
        List<ClockinRecordMergeDTO> clockinRecordMergeDTOList = clockinRecordMergeMapper.clockinRecordMergeByUserId(companyId,month,userId);
        //员工这个月还没有合并过考勤数据
        if(CollectionUtils.isEmpty(clockinRecordMergeDTOList)){
            return;
        }else {
            //判断员工这个月有没有统计过考勤数据
            QueryWrapper<AttendanceRecord> attendanceRecordQueryWrapper = new QueryWrapper<>();
            attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getUserId,userId);
            attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getCompanyId,companyId);
            attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getMonth,month);
            attendanceRecordQueryWrapper.lambda().eq(AttendanceRecord::getDeleted,0);
            List<AttendanceRecord> attendanceRecordList = attendanceRecordService.list(attendanceRecordQueryWrapper);
            //这个员工这个月还没有统计过考勤数据
            if(CollectionUtils.isEmpty(attendanceRecordList)){
                return;
            }
            //合并表数据
            ClockinRecordMergeDTO recordMergeDTO = clockinRecordMergeDTOList.get(0);
            //统计表数据
            AttendanceRecord attendanceRecord = attendanceRecordList.get(0);

            recordMergeDTO.setAttendanceRecordId(attendanceRecord.getId());
            recordMergeDTO.setTaskInstanceId(attendanceRecord.getTaskInstanceId());
            attendanceRecordService.updateAttendanceResult(recordMergeDTO);
        }

    }

}
