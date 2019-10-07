package com.sancai.oa.report.thread;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.response.OapiReportListResponse;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.enums.EnumClockinMergeStatus;
import com.sancai.oa.clockin.enums.EnumClockinPointStatus;
import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.core.threadpool.RollBack;
import com.sancai.oa.core.threadpool.ThreadTask;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.examine.utils.MapUtils;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.*;
import com.sancai.oa.report.entity.enums.ReportRecordTypeEnum;
import com.sancai.oa.report.exception.EnumReportError;
import com.sancai.oa.report.exception.OaReportlException;
import com.sancai.oa.report.mapper.ReportDepartmentMapper;
import com.sancai.oa.report.mapper.ReportRecordMapper;
import com.sancai.oa.report.mapper.ReportRuleMapper;
import com.sancai.oa.report.service.IReportRecordService;
import com.sancai.oa.report.service.impl.ReportRecordServiceImpl;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.signin.service.impl.SigninRecordServiceImpl;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import com.sancai.oa.utils.UUIDS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 抓日报任务
 *
 * @Author chenm
 * @create 2019/8/1 15:33
 */

@Slf4j
public class ReportThreadTask extends ThreadTask {


    @Autowired
    private DingDingReportService dingDingReportService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ReportDepartmentMapper tReportDepartmentMapper;

    @Autowired
    private ReportRecordMapper treportRecordMapper;

    @Autowired
    private ReportRuleMapper treportRuleMapper;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private RedisUtil redisUtil;

    private String taskInstanceId;
    private String companyId;
    private long intervalTimeStart;
    private long intervalTimeEnd;
    private List<ReportTemplate> templateList;
    private long startTime;
    private int batch;

    public ReportThreadTask(CountDownLatch childCountDown, CountDownLatch mainCountDown, BlockingDeque<Boolean> result, RollBack rollback, DataSourceTransactionManager transactionManager, Object obj, Map<String, Object> params) {
        super(childCountDown, mainCountDown, result, rollback, transactionManager, obj, params);
    }


    @Override
    public void initParam() {
        this.templateList = (List<ReportTemplate>) obj;

        this.batch = (int) getParam("batch");
        this.taskInstanceId = (String) getParam("taskInstanceId");
        this.companyId = (String) getParam("companyId");
        this.intervalTimeStart = (long) getParam("intervalTimeStart");
        this.intervalTimeEnd = (long) getParam("intervalTimeEnd");

        this.redisUtil = (RedisUtil)getParam("redisUtil");
        this.dingDingReportService = (DingDingReportService) getParam("dingDingReportService");
        this.tReportDepartmentMapper = (ReportDepartmentMapper) getParam("tReportDepartmentMapper");
        this.treportRecordMapper = (ReportRecordMapper) getParam("treportRecordMapper");
        this.treportRuleMapper = (ReportRuleMapper) getParam("treportRuleMapper");
        this.userService = (IUserService) getParam("userService");
        this.taskInstanceService = (ITaskInstanceService) getParam("taskInstanceService");
        this.startTime = (long) getParam("startTime");

    }

    @Override
    public boolean processTask() {
        try {
            //执行方法
            if (this.templateList == null) {
                return false;
            }
            int group = 0;
            for (ReportTemplate template : templateList) {
                group++;
                System.out.println("抓取日报:" + template.getName() + " 开始------");
                TaskMessage.addMessage(taskInstanceId, "抓取日报:" + template.getName() + " 开始------");
                List<OapiReportListResponse.ReportOapiVo> list = getAllUserReportByCompany(companyId, template.getName(), intervalTimeStart, intervalTimeEnd);

                if (list != null && list.size() > 0) {
                    System.out.println("抓取日报:" + template.getName() + "：" + list.size() + "条");
                    TaskMessage.addMessage(taskInstanceId, "抓取日报:" + template.getName() + "：" + list.size() + "条");
                    //导入日报数据，生成日志部门数据

                    importReport(list, template, companyId, taskInstanceId);
                    System.out.println("抓取日报:" + template.getName() + "-保存");
                    TaskMessage.addMessage(taskInstanceId, "抓取日报:" + template.getName() + "-保存");

                }
                long endTime = System.currentTimeMillis();
                int sec = (int) ((endTime - startTime) / 1000);
                System.out.println("--第" + batch + "批,第" + group + "组 抓取日报完成,第" + sec + "秒");
                TaskMessage.addMessage(taskInstanceId, "--第" + batch + "批,第" + group + "组抓取日报完成,第" + sec + "秒");
            }

            return true;
        } catch (Exception e) {
            log.error("日报抓取异常:"+e.getMessage());
            for(StackTraceElement st : e.getStackTrace()){
                log.error(st.toString());
            }
            TaskMessage.addMessage(taskInstanceId, "--第" + batch + "批日报抓取时异常：" + e.getMessage());
            return false;
        }
    }


    /**
     * 按用户id和时间生成分组key
     *
     * @param reportOapiVo
     * @return
     */
    private String fetchGroupKey(OapiReportListResponse.ReportOapiVo reportOapiVo) {
        return reportOapiVo.getCreatorId() + LocalDateTimeUtils.formatDateTimeByYmd(reportOapiVo.getCreateTime());
    }

    /**
     * 导入日报数据，生成日志部门数据
     *
     * @param lists
     * @param template
     * @param companyId
     * @param taskInstanceId
     */
    private void importReport(List<OapiReportListResponse.ReportOapiVo> lists, ReportTemplate template, String companyId, String taskInstanceId) {
        //查出日志规则数据
        QueryWrapper<ReportRule> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("deleted", 0);
        queryWrapper1.eq("template_id", template.getId());
        List<ReportRule> treportRule = treportRuleMapper.selectList(queryWrapper1);
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        List<UserDTO> userList = userService.listUser(companyId, 0, taskInstance.getStartTime(), taskInstance.getEndTime());

        List<OapiReportListResponse.ReportOapiVo> listFinal = new ArrayList<>();
        List<OapiReportListResponse.ReportOapiVo> list = lists.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getCreatorId() + ";" + o.getCreateTime()))), ArrayList::new));
        //取用户一天内最早一次提交的日报数据
        list.stream().collect(Collectors.groupingBy(d -> fetchGroupKey(d))).entrySet().forEach(entry -> {
            OapiReportListResponse.ReportOapiVo firstReport = entry.getValue().stream().min(Comparator.comparing(OapiReportListResponse.ReportOapiVo::getCreateTime)).get();
            listFinal.add(firstReport);
        });

        TaskMessage.addMessage(taskInstanceId, "公司id：" + companyId + "，抓取日报总数:" + listFinal.size() + " 条------");
        for (OapiReportListResponse.ReportOapiVo report : listFinal) {
            UserDTO userMap = userList.stream().filter(User -> User.getUserId().equals(report.getCreatorId())).findAny().orElse(null);
            String userName = " ";
            if(userMap != null && StringUtils.isNotBlank(userMap.getName())){
                userName = userMap.getName();
            }
            String id = UUIDS.getID();
            ReportRecord tRr = new ReportRecord();
            tRr.setId(id);
            tRr.setDeleted(0);
            tRr.setCreateTime(System.currentTimeMillis());
            tRr.setReportTime(report.getCreateTime());
            tRr.setCompanyId(companyId);
            tRr.setUserId(report.getCreatorId());
            tRr.setUserName(userName);
            tRr.setContent(JSON.toJSONString(report.getContents()));
            tRr.setTemplateId(template.getId());
            tRr.setStatus(ReportRecordTypeEnum.NORMAL.getKey());
            tRr.setTaskInstanceId(taskInstanceId);
            tRr.setRemark(report.getRemark());
            int d = treportRecordMapper.insert(tRr);
            if (d <= 0) {
                log.error("日报数据导入发生异常" + tRr.getId());
                TaskMessage.addMessage(taskInstanceId, "日报数据导入发生异常" + tRr.getId());
                throw new OaReportlException(EnumReportError.EXCEPTION_OCCURRED_IN_DAILY_DATA_IMPORT);
            }
            if (userMap == null) {
                continue;
            }
            TaskMessage.addMessage(taskInstanceId, "日报数据导入：根据日志规则查是否合规开始,日报id=" + tRr.getId());
            validReport(tRr, template, treportRule, companyId, userMap);
            TaskMessage.addMessage(taskInstanceId, "日报数据导入：根据日志规则查是否合规结束,日报id=" + tRr.getId());
            if (userMap.getUserDepartments() == null || userMap.getUserDepartments().size() == 0) {
                continue;
            }
            List<Integer> deptList = userMap.getUserDepartments().stream().map(UserDepartment -> Integer.valueOf(UserDepartment.getDeptId())).collect(Collectors.toList());
            //TODO 根据companyId + UserId + Time + taskInstanceId 缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
            String fetchKey =  OaMapUtils.fetchKey(tRr.getCompanyId(), tRr.getUserId(), String.valueOf(tRr.getReportTime()), tRr.getTaskInstanceId());
            String deptIds = (String) redisUtil.get(fetchKey);
            if(StringUtils.isNotBlank(deptIds)){
                deptList = Arrays.asList(deptIds .split(",")).stream().map(s -> (Integer.valueOf(s.trim()))).collect(Collectors.toList());
                //TODO 根据companyId + UserId + checkinTime + taskInstanceId 缓存删除
                redisUtil.del(fetchKey);
            }
            //插入日报和部门关系表
            addReportDepartmentData(deptList, tRr.getId());

        }//for end
    }

    /**
     * 根据日志规则查是否合规
     */
    private void validReport(ReportRecord report, ReportTemplate reportTemplate, List<ReportRule> treportRule, String companyId, UserDTO user) {
        //判断当前日志时间是否在日志规则的开始结束时间区间
        String timeValid = timeRule(report, reportTemplate);
        //在时间范围内
        if (timeValid == null) {
            //在时间范围内，查询具体规则
            reportRule(user, treportRule, reportTemplate.getId(), report, companyId);
        }
    }
    /**
     * 判断当前日志时间是否在日志规则的开始结束时间区间
     *
     * @param reportRecord
     * @param reportTemplate
     * @return
     */
    private String timeRule(ReportRecord reportRecord, ReportTemplate reportTemplate) {
        String begin = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime(), "yyyy-MM-dd") + " " + reportTemplate.getBeginTime();
        String finish = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime(), "yyyy-MM-dd") + " " + reportTemplate.getFinishTime();

        //将日报提交时间格式化秒为00，只用于扣积分判断
        Long reportTimeYmdHs = LocalDateTimeUtils.convertTimeToLong(LocalDateTimeUtils.getTimeYmdHm(reportRecord.getReportTime()));
        //判断日报正常、早交还是晚交
        String timeIsNotValid = LocalDateTimeUtils.isAfterOrBeforeDate(reportTimeYmdHs, LocalDateTimeUtils.convertTimeToLong(begin), LocalDateTimeUtils.convertTimeToLong(finish));

        if (timeIsNotValid != null) {

            String reportTime = LocalDateTimeUtils.formatDateTime(reportRecord.getReportTime());

            //时差
            String times = null;

            //日报早交
            if (ReportRecordTypeEnum.BEFORE.getKey().equals(timeIsNotValid)) {
                times = ReportRecordTypeEnum.BEFORE.getValue() + LocalDateTimeUtils.getDistanceTime(reportTime, begin);
            }
            //日报迟交
            if (ReportRecordTypeEnum.AFTER.getKey().equals(timeIsNotValid)) {
                times = ReportRecordTypeEnum.AFTER.getValue() + LocalDateTimeUtils.getDistanceTime(reportTime, finish);
            }
            String  unqualifiedReason ="未能在规定时间提交日报:" + times;

            reportRecord.setUnqualifiedReason(unqualifiedReason);
            reportRecord.setStatus(ReportRecordTypeEnum.ABNORMAL.getKey());
            reportRecord.setModifyTime(System.currentTimeMillis());

            //改状态为异常abnormal，记录不合格的原因unqualified_reason
            treportRecordMapper.updateById(reportRecord);
        }
        return timeIsNotValid;
    }

    /**
     * 判断日志是否合规
     *
     * @param treportRule  日志规则数据集合
     * @param templateId   模板id
     * @param reportRecord 日志记录
     */
    private void reportRule(UserDTO user, List<ReportRule> treportRule, String templateId, ReportRecord reportRecord, String companyId) {
        ReportRule reportRuleList = treportRule.stream().filter(TReportRule -> TReportRule.getTemplateId().equals(templateId)).findAny().orElse(null);
        if (reportRuleList != null) {
            String fieldRule = reportRuleList.getFieldRule();
            if (StringUtils.isNotBlank(fieldRule)) {
                String reportRecordContent = reportRecord.getContent();
                //判断日报内容是否符合具体日报规则（入参：fieldRule 规则jsonString，reportRecordContent 日报内容jsonString）
                String whetherValid = whetherValidReportContent(fieldRule, reportRecordContent);
                if (StringUtils.isNotBlank(whetherValid)) {
                    // Boolean isWarned = actionScoreRecordService.userIsWarned(reportRecord.getUserId(),reportRecord.getCompanyId(),reportRecord.getReportTime());
                    //  if(!isWarned){
                    //     actionScoreDeductService.reportDeductScore(companyId,user,EnumScoreRule.FIRSTWARNING.getKey());
                    //  }else{
                    //     调用接口，对于日报填写不规范的进行扣分
                    // actionScoreDeductService.reportDeductScore(companyId,user,EnumScoreRule.REPORTFORMATISINVALID.getKey());
                    // }
                    reportRecord.setUnqualifiedReason(whetherValid);
                    reportRecord.setStatus(ReportRecordTypeEnum.ABNORMAL.getKey());
                    treportRecordMapper.updateById(reportRecord);
                }
            }
        }
    }

    /**
     * @param fieldRule           规则jsonString
     * @param reportRecordContent 日报内容jsonString
     * @return String 返回判断日志不合规说明，nul为合规
     */
    private String whetherValidReportContent(String fieldRule, String reportRecordContent) {
        List<FieldRuleDTO> fieldRuleList = JSONArray.parseArray(fieldRule, FieldRuleDTO.class);
        List<FieldRuleDTO> reportRecordList = JSONArray.parseArray(reportRecordContent, FieldRuleDTO.class);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("日志必填项(");
        for (FieldRuleDTO report : reportRecordList) {
            String key = report.getKey();
            String value = report.getValue();
            for (FieldRuleDTO fieldRuledto : fieldRuleList) {
                stringBuffer.append(fieldRuledto.getKey());
                if (key.equals(fieldRuledto.getKey())) {
                    if (fieldRuledto.getAllowEmpty()) {
                        //该项内容不能为空
                        if (StringUtils.isBlank(value)) {
                            stringBuffer.append(")内容为空");
                            return stringBuffer.toString();
                        }
                        //内容长度少于规定长度
                        if (value.length() < fieldRuledto.getMinlength()) {
                            stringBuffer.append(")内容字数少于规定字数");
                            stringBuffer.append(fieldRuledto.getMinlength());
                            return stringBuffer.toString();
                        }

                    }
                }
            }
        }
        return null;
    }


    /**
     * 根据模板名称获取子公司每天所有用户日志记录
     *
     * @param companyId 公司id
     * @return List
     */
    private List<OapiReportListResponse.ReportOapiVo> getAllUserReportByCompany(String companyId, String templateName, long intervalTimeStart, long intervalTimeEnd) {
        List<OapiReportListResponse.ReportOapiVo> allCheckDataList = new ArrayList<>();

        OapiReportListResponse.PageVo logsMap;

        Long pageNo = 0L;
        while (pageNo != null) {
            //根据模板名称获取昨天的日志记录列表
            logsMap = dingDingReportService.reportList(null, pageNo, companyId, templateName, intervalTimeStart, intervalTimeEnd);
            allCheckDataList.addAll(logsMap.getDataList());
            if (logsMap.getHasMore() && logsMap.getNextCursor() != null && logsMap.getNextCursor() > 0) {
                pageNo = logsMap.getNextCursor();
            } else {
                pageNo = null;
            }
        }

        return allCheckDataList;

    }

    /**
     * 插入日志与部门多对多关系表
     *
     * @param deptList 部门id集合
     * @param id       实例id
     * @return boolean
     */
    private void addReportDepartmentData(List<Integer> deptList, String id) {
        if (deptList == null || deptList.size() == 0) {
            return;
        }
        int isNotSuccess = 0;
        for (Integer deptId : deptList) {
            //2.遍历部门插入数据
            ReportDepartment reportDepartment = new ReportDepartment();
            reportDepartment.setDeleted(0);
            reportDepartment.setCreateTime(System.currentTimeMillis());
            reportDepartment.setDeptId(deptId);
            reportDepartment.setReportInstanceId(id);
            reportDepartment.setId(UUIDS.getID());
            int isNot = tReportDepartmentMapper.insert(reportDepartment);
            if (isNot <= 0) {
                isNotSuccess++;
            }
        }
        if (isNotSuccess > 0) {
            log.error("日志数据插入部门关系表失败");
            TaskMessage.addMessage(taskInstanceId, "日志数据插入部门关系表失败");
            throw new OaReportlException(EnumReportError.FAILED_TO_INSERT_DEPARTMENT_RELATIONAL_TABLE);
        }
    }


}
