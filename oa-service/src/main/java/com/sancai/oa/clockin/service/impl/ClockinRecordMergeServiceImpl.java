package com.sancai.oa.clockin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.AttendanceComplexResultDTO;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordMerge;
import com.sancai.oa.clockin.entity.DownloadQueryConditionDTO;
import com.sancai.oa.clockin.exception.EnumAttendanceRecordError;
import com.sancai.oa.clockin.exception.OaClockinlException;
import com.sancai.oa.clockin.mapper.ClockinRecordMergeMapper;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.clockin.threadpool.AttendanceMergeTask;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.ThreadPoolTool;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.service.IExamineBusinessTravelService;
import com.sancai.oa.examine.service.IExamineHolidayService;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.ReportDepartment;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.report.entity.ReportTemplate;
import com.sancai.oa.report.entity.enums.ReportTemplateTypeEnum;
import com.sancai.oa.report.mapper.ReportTemplateMapper;
import com.sancai.oa.report.service.IReportRecordService;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.mapper.ActionScoreRecordMapper;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.score.service.IActionScoreRuleService;
import com.sancai.oa.signin.mapper.SigninRecordMapper;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-08-02
 */
@Slf4j
@Service
public class ClockinRecordMergeServiceImpl extends ServiceImpl<ClockinRecordMergeMapper, ClockinRecordMerge> implements IClockinRecordMergeService {

    @Autowired
    private IClockinRecordService clockinRecordService;

    @Autowired
    private SigninRecordMapper signinRecordMapper;

    @Autowired
    private IExamineLeaveService examineLeaveService;

    @Autowired
    private IExamineHolidayService examineHolidayService;

    @Autowired
    private IExamineBusinessTravelService examineBusinessTravelService;

    @Autowired
    private ClockinRecordMergeMapper clockinRecordMergeMapper;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IReportRecordService reportRecordService;

    @Autowired
    private IActionScoreRecordService actionScoreRecordService;

    @Autowired
    private IActionScoreRuleService actionScoreRuleService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Autowired
    private IUserService userService;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private ThreadPoolTool threadPoolTool;

    @Autowired
    private IActionScoreDeductService actionScoreDeductService;

    @Autowired
    private ActionScoreRecordMapper actionScoreRecordMapper;
    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public void consolidatedAttendanceData(String companyId, String taskInstanceId) {
        try {
            //重试删除这个任务实例的合并数据
            TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
            if (taskInstance == null) {
                log.error(EnumAttendanceRecordError.CONSOLIDATED_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
                throw new OaClockinlException(EnumAttendanceRecordError.CONSOLIDATED_INSTANCE_IS_EMPTY);
            }
            String month = taskInstanceService.resetMonth(taskInstance);
            if(StringUtils.isBlank(month)){
                log.error(EnumAttendanceRecordError.CONSOLIDATED_MONTH_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
                throw new OaClockinlException(EnumAttendanceRecordError.CONSOLIDATED_MONTH_IS_EMPTY);
            }

            //删除日报异常产生的积分记录数据
            reportRecordDataDelete(taskInstanceId);
            //删除合并的数据
            attendanceDataDelete(taskInstanceId);
            //删除已经扣过的积分
            deleteUnCommittedScore(month,companyId);

            QueryWrapper<ClockinRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ClockinRecord::getCompanyId, companyId);
            queryWrapper.lambda().eq(ClockinRecord::getMonth, month);
            queryWrapper.lambda().eq(ClockinRecord::getDeleted,0);
            queryWrapper.orderByDesc("create_time","id");

            ///查询month本月日报数据
            LocalDateTime localDateTime = LocalDateTimeUtils.getDateTimeOfTimestamp(LocalDateTimeUtils.convertTimeToLong(month + "-01 00:00:00"));
            LocalDateTime firstDayOfMonth = LocalDateTimeUtils.getDayStart(localDateTime.with(TemporalAdjusters.firstDayOfMonth()));
            LocalDateTime lastDayOfMonth = LocalDateTimeUtils.getDayEnd(localDateTime.with(TemporalAdjusters.lastDayOfMonth()));
            Long startTime = LocalDateTimeUtils.getMilliByTime(firstDayOfMonth);
            Long endTime = LocalDateTimeUtils.getMilliByTime(lastDayOfMonth);

            QueryWrapper<ReportRecord> reportRecordQueryWrapper = new QueryWrapper<>();
            reportRecordQueryWrapper.lambda().eq(ReportRecord::getCompanyId, companyId);
            reportRecordQueryWrapper.lambda().eq(ReportRecord::getDeleted, 0);
            reportRecordQueryWrapper.lambda().ge(ReportRecord::getReportTime, startTime);
            reportRecordQueryWrapper.lambda().le(ReportRecord::getReportTime, endTime);
            List<ReportRecord> reportRecordList = reportRecordService.list(reportRecordQueryWrapper);

            //查询日志模板数据
            QueryWrapper<ReportTemplate> templateQueryWrapper = new QueryWrapper<>();
            templateQueryWrapper.lambda().eq(ReportTemplate::getCompanyId, companyId);
            templateQueryWrapper.lambda().eq(ReportTemplate::getDeleted, 0);
            templateQueryWrapper.lambda().eq(ReportTemplate::getStatus, ReportTemplateTypeEnum.VALID.getKey());
            templateQueryWrapper.lambda().ne(ReportTemplate::getName, "拜访记录");
            List<ReportTemplate> reportTemplateList = reportTemplateMapper.selectList(templateQueryWrapper);

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
                List<ClockinRecord> clockinRecordList = clockinRecordService.list(queryWrapper);
                TaskMessage.addMessage(taskInstanceId,"分页合并考勤数据，第"+cursor+"页共"+clockinRecordList.size()+"条数据");
                boolean isFinish = false;
                if (clockinRecordList.size() < pageSize) {
                    cursor = null;
                    isFinish = true;
                } else {
                    cursor++;
                }

                Map<String,Object> params = new HashMap<>();
                params.put("taskInstanceId",taskInstanceId);
                params.put("companyId",companyId);
                params.put("batch",cursor);
                params.put("examineHolidayService",examineHolidayService);
                params.put("examineBusinessTravelService",examineBusinessTravelService);
                params.put("examineLeaveService",examineLeaveService);
                params.put("signinRecordMapper",signinRecordMapper);
                params.put("reportRecordService",reportRecordService);
                params.put("actionScoreRecordService",actionScoreRecordService);

                params.put("actionScoreRuleService",actionScoreRuleService);
                params.put("actionScoreDepartmentService",actionScoreDepartmentService);
                params.put("clockinRecordMergeService",this);
                params.put("userService",userService);
                params.put("actionScoreDeductService",actionScoreDeductService);



                params.put("reportTemplateList",reportTemplateList);
                params.put("redisUtil",redisUtil);
                params.put("taskInstanceStatus",taskInstance.getStatus());
                params.put("reportRecordList",reportRecordList);


                threadPoolTool.excuteTask(transactionManager,taskInstanceId,clockinRecordList,5,params,isFinish, AttendanceMergeTask.class);
                long ends = System.currentTimeMillis();
                int secs = (int) ((ends - starts)/1000);
                TaskMessage.addMessage(taskInstanceId,"第"+page+"页合并考勤数据成功，共"+clockinRecordList.size()+"条数据,耗时:"+secs+"秒");
                totalNumber += clockinRecordList.size();

            }
            long end = System.currentTimeMillis();
            int sec = (int) ((end - start)/1000);

            TaskMessage.addMessage(taskInstanceId,"累计合并考勤数据共"+totalNumber+"条,总耗时:"+sec+"秒");
        }catch (Exception e){
            TaskMessage.addMessage(taskInstanceId,"异常："+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 删除日报异常产生的积分记录数据
     * @param
     */
    private void reportRecordDataDelete(String taskInstanceId) {
        List<String> recordIds = new ArrayList<>();
        //删除积分记录数据
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().likeRight(ActionScoreRecord::getTargetId,taskInstanceId);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted,0);
        List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);
        if(CollectionUtils.isEmpty(actionScoreRecordList)){
            return;
        }
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
    /**
     * 根据条件查询筛选考勤结果
     *
     * @param downloadQueryConditionDTO 考勤结果导出Excel查询条件封装的实体类
     * @return 返回考勤复合结果集合
     */
    @Override
    public List<AttendanceComplexResultDTO> queryAttendanceComplexResult(DownloadQueryConditionDTO downloadQueryConditionDTO) {
        List<AttendanceComplexResultDTO> attendanceComplexResultDTOS = clockinRecordMergeMapper.queryAttendanceComplexResult(downloadQueryConditionDTO);
        return attendanceComplexResultDTOS;
    }

    @Override
    public void attendanceDataDelete(String taskInstanceId) {
        ClockinRecordMerge clockinMerge = new ClockinRecordMerge();
        clockinMerge.setDeleted(1);
        UpdateWrapper<ClockinRecordMerge> clockinRecordMergeUpdateWrapper = new UpdateWrapper<>();
        clockinRecordMergeUpdateWrapper.lambda().eq(ClockinRecordMerge::getTaskInstanceId,taskInstanceId);
        update(clockinMerge,clockinRecordMergeUpdateWrapper);
    }

    @Override
    public List<ClockinRecordMerge> recordMergeByTaskId(String taskInstanceId) {
        QueryWrapper<ClockinRecordMerge> clockinRecordMergeQueryWrapper = new QueryWrapper<>();
        clockinRecordMergeQueryWrapper.lambda().eq(ClockinRecordMerge::getTaskInstanceId,taskInstanceId);
        clockinRecordMergeQueryWrapper.lambda().eq(ClockinRecordMerge::getDeleted,0);
        List<ClockinRecordMerge> clockinRecordMergeList = list(clockinRecordMergeQueryWrapper);
        return clockinRecordMergeList;
    }

    /**
     * 删除已经扣过的积分
     * @param month
     * @param companyId
     */
    private void deleteUnCommittedScore(String month,String companyId){
        List<String> recordIds = new ArrayList<>();
        //删除积分记录数据
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().likeRight(ActionScoreRecord::getTargetId, month);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getCompanyId,companyId);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted, 0);
        List<ActionScoreRecord> actionScoreRecordList = actionScoreRecordService.list(actionScoreRecordQueryWrapper);
        if(CollectionUtils.isEmpty(actionScoreRecordList)){
            return;
        }
        actionScoreRecordList.stream().forEach(actionScoreRecord -> {
            actionScoreRecord.setDeleted(1);
            recordIds.add(actionScoreRecord.getId());
            actionScoreRecordService.updateById(actionScoreRecord);
        });
        //删除积分记录对应的部门关系表
        ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
        actionScoreDepartment.setDeleted(1);
        UpdateWrapper<ActionScoreDepartment> actionScoreDepartmentUpdateWrapper = new UpdateWrapper<>();
        actionScoreDepartmentUpdateWrapper.lambda().in(ActionScoreDepartment::getScoreRecordId, recordIds);
        actionScoreDepartmentService.update(actionScoreDepartment, actionScoreDepartmentUpdateWrapper);
    }
}
