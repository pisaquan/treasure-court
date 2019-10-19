package com.sancai.oa.examine.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.examine.DingDingExamineService;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.entity.enums.ExamineFormCompEnum;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.ExamineTypeEnum;
import com.sancai.oa.examine.entity.enums.UserStatusTypeEnum;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.mapper.ExamineActionMapper;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineDepartmentService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.TimeConversionUtil;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.mapper.TaskInstanceMapper;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.enums.ScoreTypeEnum;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.typestatus.enums.ScoreRecordTypeEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 行为奖罚 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Service
@Slf4j

public class ExamineActionServiceImpl extends ServiceImpl<ExamineActionMapper, ExamineAction> implements IExamineActionService {

    @Autowired
    private DingDingExamineService dingDingExamineService;

    @Autowired
    private IExamineDepartmentService examineDepartmentService;

    @Autowired
    private IActionScoreRecordService actionScoreRecordService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;
	
	@Autowired
    private RedisUtil redisUtil;
	
	@Autowired
    private ExamineActionMapper examineActionMapper;

	@Autowired
	private IUserService userService;

    @Autowired
    private IExamineService examineService;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private ISigninRecordService signinRecordService;


    @Override
    public void dealExamineData(String taskInstanceId,String companyId,String processCode,long intervalTimeStart,long intervalTimeEnd){
        TaskMessage.addMessage(taskInstanceId,"行为考核抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"模板code:"+processCode);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        Long nextCursor = 0L;
        int totalCount = 0;
        while(nextCursor != null) {
            List<ExamineAction> examineEntityList = new ArrayList<>();
            List<ExamineDepartment> examineDepartmentList = new ArrayList<>();
            List<ActionScoreRecord> actionScoreRecordList = new ArrayList<>();
            List<ActionScoreDepartment> actionScoreDepartmentList = new ArrayList<>();
            Map<List<ExamineInstanceDTO>, Long> examineInstanceMap = dingDingExamineService.getDingTalkExamineData(nextCursor, companyId, processCode,intervalTimeStart,intervalTimeEnd);
            List<ExamineInstanceDTO> examineInstanceDTOList = new ArrayList<>();
            for (Map.Entry<List<ExamineInstanceDTO>, Long> entryMap : examineInstanceMap.entrySet()) {
                examineInstanceDTOList = entryMap.getKey();
                nextCursor = entryMap.getValue();
                break;
            }
            examineInstanceDTOList.stream().forEach(examineInstanceDTO -> {
                OapiProcessinstanceGetResponse.ProcessInstanceTopVo instanceVo = examineInstanceDTO.getProcessInstanceTopVo();
                List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList = instanceVo.getFormComponentValues();
                formComponentValueVoList.stream().forEach(formComponentValueVo -> {
                    if(ExamineFormCompEnum.BEHAVIORASSESSMENT.getValue().equals(formComponentValueVo.getName()) ||
                            ExamineFormCompEnum.TABLEFIELD.getKey().equals(formComponentValueVo.getComponentType())){
                        String value = formComponentValueVo.getValue();
                        List<String> valueList = JSONArray.parseArray(value, String.class);
                        valueList.stream().forEach(val -> {
                            ExamineAction examineAction = new ExamineAction();
                            examineAction.setId(UUIDS.getID());
                            examineAction.setCompanyId(companyId);
                            examineAction.setUserId(instanceVo.getOriginatorUserid());
                            //查表获取用户信息
                            UserDTO originatorUser = userService.getUserByUserId(examineAction.getUserId(),companyId);
                            if(originatorUser != null){
                                examineAction.setUserName(originatorUser.getName());
                            }else {
                                examineAction.setUserName(" ");
                            }
                            examineAction.setProcessCode(examineInstanceDTO.getProcessCode());
                            examineAction.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                            examineAction.setProcessTitle(instanceVo.getTitle());
                            examineAction.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                            examineAction.setProcessFinishTime(instanceVo.getFinishTime() != null ? instanceVo.getFinishTime().getTime(): null);
                            examineAction.setProcessStatus(instanceVo.getStatus());
                            examineAction.setProcessResult(instanceVo.getResult());
                            examineAction.setFormValueCompany(examineInstanceDTO.getCompany());
                            examineAction.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                            examineAction.setDeleted(0);
                            examineAction.setTaskInstanceId(taskInstanceId);
                            businessTravelFormValue(examineAction,val);
                            List<Integer> departMentList = new ArrayList<>();
                            //查表获取用户信息
                            UserDTO user = userService.getUserByUserId(examineAction.getFormValueUserId(),companyId);
                            if(user != null){
                                examineAction.setFormValueUserName(user.getName());
                                List<UserDepartment> userDepartments = user.getUserDepartments();
                                if(CollectionUtils.isNotEmpty(userDepartments)){
                                    departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                                    //TODO 根据ProcessInstanceId + taskInstanceId + form_value_user_id(表单内容的用户id)缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                                    List<Integer> departList =  getRecordOriginDeptIdsSaveRedis(examineAction.getProcessInstanceId(),examineAction.getTaskInstanceId(),examineAction.getFormValueUserId());
                                    if(departList != null && departList.size()>0){
                                        departMentList = departList;
                                    }
                                }
                            }
                            departMentList.stream().forEach(departId -> {
                                ExamineDepartment examineDepartmentVO = new ExamineDepartment();
                                examineDepartmentVO.setId(UUIDS.getID());
                                examineDepartmentVO.setExamineInstanceId(examineAction.getId());
                                examineDepartmentVO.setDeptId(departId);
                                examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                                examineDepartmentVO.setDeleted(0);
                                examineDepartmentList.add(examineDepartmentVO);
                            });
                            examineEntityList.add(examineAction);
                            //审批通过之后更改积分
                            if(ExamineStatusEnum.COMPLETED.getKey().equals(instanceVo.getStatus()) &&
                                    ExamineStatusEnum.AGREE.getKey().equals(instanceVo.getResult())){
                                ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
                                actionScoreRecord.setId(UUIDS.getID());
                                actionScoreRecord.setUserId(examineAction.getFormValueUserId());
                                actionScoreRecord.setUserName(examineAction.getFormValueUserName());
                                actionScoreRecord.setCompanyId(examineAction.getCompanyId());
                                actionScoreRecord.setSource(ExamineTypeEnum.EXAMINEACTION.getKey());
                                actionScoreRecord.setTargetId(examineAction.getId());
                                actionScoreRecord.setType(examineAction.getFormValueType());
                                actionScoreRecord.setScore(examineAction.getFormValueScore());
                                actionScoreRecord.setRemark(examineAction.getFormValueReason());
                                actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                                actionScoreRecord.setDeleted(0);
                                actionScoreRecord.setScoreRecordTime(examineAction.getProcessCreateTime());
                                actionScoreRecordList.add(actionScoreRecord);
                                departMentList.stream().forEach(departId -> {
                                    //插入积分变动记录表和部门对应关系表
                                    ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                                    actionScoreDepartment.setId(UUIDS.getID());
                                    actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
                                    actionScoreDepartment.setDeptId(departId);
                                        actionScoreDepartment.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                                    actionScoreDepartment.setDeleted(0);
                                    actionScoreDepartmentList.add(actionScoreDepartment);
                                });
                            }
                        });
                    }
                });
            });
            if(CollectionUtils.isNotEmpty(examineEntityList)){
                for(ExamineAction examineAction : examineEntityList){
                    save(examineAction);
                }
                log.info("分页拉取钉钉行为考核数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"抓取了:"+examineEntityList.size()+"条行为考核数据");
            totalCount += examineEntityList.size();
            if(CollectionUtils.isNotEmpty(examineDepartmentList)){
                for(ExamineDepartment examineDepartment : examineDepartmentList){
                    examineDepartmentService.save(examineDepartment);
                }
                log.info("分页插入审批实例和部门对应关系表成功");
            }
            if(CollectionUtils.isNotEmpty(actionScoreRecordList)){
                for(ActionScoreRecord actionScoreRecord : actionScoreRecordList){
                    actionScoreRecordService.save(actionScoreRecord);
                }
            }
            if(CollectionUtils.isNotEmpty(actionScoreDepartmentList)){
                for(ActionScoreDepartment actionScoreDepartment : actionScoreDepartmentList){
                    actionScoreDepartmentService.save(actionScoreDepartment);
                }
            }
        }
        log.info("分期拉取钉钉行为考核数据成功");
        TaskMessage.addMessage(taskInstanceId,"拉取钉钉行为考核数据成功，共抓取"+totalCount+"条");
    }

    @Override
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExamineAction> examineActionList = list(new QueryWrapper<ExamineAction>()
                .lambda().ne(ExamineAction::getProcessStatus,"COMPLETED")
                .and(u -> u.ne(ExamineAction::getProcessStatus,"TERMINATED"))
                .and(u -> u.eq(ExamineAction::getDeleted,0))
                .and(u -> u.between(ExamineAction::getProcessCreateTime,lastMonth,nowTime)));
        examineActionList.stream().forEach(examineAction -> {
            String processInstanceId = examineAction.getProcessInstanceId();
            String companyId = examineAction.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo =
                    dingDingExamineService.examineInstanceGetById(processInstanceId,companyId);
            if(processInstanceTopVo == null){
                return;
            }
            if(ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())){
                return;
            }
            if (processInstanceTopVo.getFinishTime() != null) {
                examineAction.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examineAction.setProcessStatus(processInstanceTopVo.getStatus());
            examineAction.setProcessResult(processInstanceTopVo.getResult());
            examineAction.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId,"上个月开始到现在，共"+examineActionList.size()+"条行为考核数据需要更新");
        if (CollectionUtils.isNotEmpty(examineActionList)) {
            examineActionList = examineActionList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId,"状态已变更，可以更新的行为考核数据共"+examineActionList.size()+"条");
            if(CollectionUtils.isNotEmpty(examineActionList)){
                for(ExamineAction examineAction :examineActionList){
                    updateById(examineAction);
                    //积分记录表插入数据
                    if(ExamineStatusEnum.COMPLETED.getKey().equals(examineAction.getProcessStatus()) &&
                            ExamineStatusEnum.AGREE.getKey().equals(examineAction.getProcessResult())){
                        ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
                        actionScoreRecord.setId(UUIDS.getID());
                        actionScoreRecord.setUserId(examineAction.getFormValueUserId());
                        actionScoreRecord.setUserName(examineAction.getFormValueUserName());
                        actionScoreRecord.setCompanyId(examineAction.getCompanyId());
                        actionScoreRecord.setSource(ExamineTypeEnum.EXAMINEACTION.getKey());
                        actionScoreRecord.setTargetId(examineAction.getId());
                        actionScoreRecord.setType(examineAction.getFormValueType());
                        actionScoreRecord.setScore(examineAction.getFormValueScore());
                        actionScoreRecord.setRemark(examineAction.getFormValueReason());
                        actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                        actionScoreRecord.setDeleted(0);
                        actionScoreRecord.setScoreRecordTime(examineAction.getProcessCreateTime());
                        actionScoreRecordService.save(actionScoreRecord);
                        List<Integer> departMentList = new ArrayList<>();
                        //查表获取用户信息
                        UserDTO user = userService.getUserByUserId(actionScoreRecord.getUserId(),actionScoreRecord.getCompanyId());
                        if(user != null){
                            List<UserDepartment> userDepartments = user.getUserDepartments();
                            if(CollectionUtils.isNotEmpty(userDepartments)){
                                departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                            }
                        }
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
                log.info("更新行为考核审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"更新行为考核数据成功，更新数据共"+examineActionList.size()+"条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    @Override
    public List<ExamineAction> examineActionListByInstanceId(String instanceId) {
        QueryWrapper<ExamineAction> examineActionQueryWrapper = new QueryWrapper<>();
        examineActionQueryWrapper.lambda().eq(ExamineAction::getTaskInstanceId,instanceId);
        examineActionQueryWrapper.lambda().eq(ExamineAction::getDeleted,0);
        List<ExamineAction> examineActionList = list(examineActionQueryWrapper);
        return examineActionList;
    }

    @Override
    public void examineActionDataDelete(List<ExamineAction> examineActionList) {
        List<String> ids = new ArrayList<>();
        List<String> recordIds = new ArrayList<>();
        //删除行为考核审批数据
        examineActionList.stream().forEach(examineAction -> {
            examineAction.setDeleted(1);
            ids.add(examineAction.getId());
            updateById(examineAction);
        });
        //删除行为考核对应部门关系表
        ExamineDepartment examineDepartment = new ExamineDepartment();
        examineDepartment.setDeleted(1);
        UpdateWrapper<ExamineDepartment> examineDepartmentUpdateWrapper = new UpdateWrapper<>();
        examineDepartmentUpdateWrapper.lambda().in(ExamineDepartment::getExamineInstanceId,ids);
        examineDepartmentService.update(examineDepartment,examineDepartmentUpdateWrapper);

        //删除积分记录数据
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().in(ActionScoreRecord::getTargetId,ids);
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

    private String fetchKey(String processInstanceId , String taskInstanceId , String formValueUserId){
        if(StringUtils.isNotBlank(formValueUserId)){
            //岗位，行为表单process_instance_id批审实例id + taskInstanceId + form_value_user_id
            return processInstanceId+"&"+ taskInstanceId + "&" + formValueUserId;
        }else{
            //请假、出差、公休process_instance_id批审实例id + taskInstanceId
            return processInstanceId + "&" + taskInstanceId;
        }
    }
    /**
     * 根据process_instance_id批审实例id + taskInstanceId + form_value_user_id把记录对应的部门集合存入缓存
     * @param recordLists 记录集合
     */
    @Override
    public void recordOriginDeptIdsSaveRedis ( List<DataMap> recordLists){
            if(recordLists != null && recordLists.size()>0){
                Map<String, List<DataMap>> recordListmap = recordLists.stream() .collect(Collectors.groupingBy(d -> fetchKey(d.get("process_instance_id").toString() ,d.get("task_instance_id").toString(),String.valueOf(d.get("form_value_user_id"))) ));
                for (String fetchKey : recordListmap.keySet()) {
                    List<DataMap> recordList = recordListmap.get(fetchKey);
                    if(recordList != null && recordList.size()>0){
                        DataMap record = recordList.get(0);
                        if(!OaMapUtils.mapIsAnyBlank(record,"deptList")){
                            redisUtil.set(fetchKey,record.get("deptList"),1 * 60 * 60);
                        }
                    }
                }
            }
    }
    /**
     * 根据process_instance_id批审实例id + taskInstanceId + formValueUserId 缓存取出记录对应的部门集

     */
    @Override
    public List<Integer> getRecordOriginDeptIdsSaveRedis ( String processInstanceId ,String taskInstanceId ,String formValueUserId){
            TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
            if (taskInstance == null) {
                return null;
            }
            List<Integer> departMentList = null;
            //重试任务中从缓存中取出
            if(TimedTaskStatusEnum.SUCCESS.getKey().equals(taskInstance.getStatus()) ||TimedTaskStatusEnum.FAILURE.getKey().equals(taskInstance.getStatus()) ||
                    TimedTaskStatusEnum.RETRYING.getKey().equals(taskInstance.getStatus())){
                String fetchKey = fetchKey(processInstanceId,taskInstanceId,formValueUserId);
                String deptIds = (String) redisUtil.get(fetchKey);
                if(StringUtils.isNotBlank(deptIds)){
                    departMentList = Arrays.asList(deptIds .split(",")).stream().map(s -> (Integer.valueOf(s.trim()))).collect(Collectors.toList());
                    //TODO 根据ProcessInstanceId + taskInstanceId 缓存删除
                    redisUtil.del(fetchKey);
                }
            }
            return departMentList;
    }

    @Override
    public void pullActionExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.EXAMINEACTION.getKey())
                .and(e -> e.eq(Examine::getCompanyId, companyId)
                        .and(t -> t.eq(Examine::getDeleted, 0))), true);
        if (examine == null) {
            log.error(EnumExamineError.EXAMINE_IS_EMPTY.getMessage()+" companyId="+companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_IS_EMPTY);
        }
        if (StringUtils.isBlank(examine.getCode())) {
            log.error(EnumExamineError.EXAMINE_CODE_IS_EMPTY.getMessage()+" companyId="+companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_CODE_IS_EMPTY);
        }
        //如果根据实例id查到值,根据实例id删除之前的已经保存的数据 , 重新获取抓取开始时间和结束时间
        List<ExamineAction> examineActionList = examineActionListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(examineActionList)){

            //TODO 根据companyId  + taskInstanceId 把记录对应的部门集合存入缓存
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                List<DataMap> actionRecordLists = examineActionMapper.actionRecordDeptIdList(companyId,taskInstanceId);
                recordOriginDeptIdsSaveRedis(actionRecordLists);
            }

            //删除行为考核审批数据
            examineActionDataDelete(examineActionList);
        }
        //钉钉抓取行为考核数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(), 7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取行为考核数据
            dealExamineData(taskInstanceId, companyId, examine.getCode(), intervalTimeStart, intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    /**
     * 处理钉钉返回的表单数据 到 自定义
     *
     * @param examineAction
     * @param value
     */
    public void businessTravelFormValue(ExamineAction examineAction,String value) {
        Map<String, Object> valueMap;
        if (StringUtils.isNotBlank(value)) {
            valueMap = OaMapUtils.stringToMap(value);
        } else {
            return;
        }
        if (!OaMapUtils.mapIsAnyBlank(valueMap, ExamineFormCompEnum.ROWVALUE.getKey())) {
            List<Map<String, Object>> valMapList = (List) valueMap.get(ExamineFormCompEnum.ROWVALUE.getKey());
            valMapList.stream().forEach(valmap -> {
                if (ExamineFormCompEnum.STAFF.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    List<Map<String, Object>> extendVueList = (List) valmap.get(ExamineFormCompEnum.EXTENDVALUE.getKey());
                    extendVueList.stream().forEach(ex -> {
                        Object userId = ex.get(ExamineFormCompEnum.EMPLID.getKey());
                        if (userId != null) {
                            examineAction.setFormValueUserId(userId.toString());
                            //userName设置默认值
                            examineAction.setFormValueUserName(" ");
                        }
                    });
                }

                if(ExamineFormCompEnum.DIMISSIONSTAFF.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object name = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(name != null){
                        examineAction.setFormValueUserName(name.toString().trim());
                    }
                }

                if (ExamineFormCompEnum.REWARDSPUNISHTYPE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object type = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (type != null) {
                        examineAction.setFormValueType(ScoreTypeEnum.getKeyByValue(type.toString()));
                    }
                }


                if(ExamineFormCompEnum.BEHAVIORPUNISHRULE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object rule = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (rule != null) {
                        examineAction.setFormValueRule(rule.toString());
                    }
                }

                if(ExamineFormCompEnum.SCORE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object score = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (score != null) {
                        examineAction.setFormValueScore(Math.abs(Float.valueOf(score.toString())));
                    }
                }

                if(ExamineFormCompEnum.ISINSERVICE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object reason = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(reason != null){
                        examineAction.setIsInservice(UserStatusTypeEnum.getKeyByValue(reason.toString()));
                    }
                }

                if (ExamineFormCompEnum.REWARDSPUNISHREASON.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object reason = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (reason != null) {
                        examineAction.setFormValueReason(reason.toString());
                    }
                }

            });
        }
    }

    /**
     * 行为考核列表查询
     * @param requestEntity 前端请求体的实体类
     * @return 返回实体类的集合
     */
    @Override
    public List<ActionPositionDataDTO> getExamineActionList(RequestEntity requestEntity) {

        //从redis中取出companyId对应的部门列表
        Set deptSet = redisUtil.sGet(requestEntity.getCompanyId());
        //部门查询条件存在时，查询部门及其子部门的所有信息
        String deptId = requestEntity.getDeptId();
        if(StringUtils.isNotBlank(deptId)) {
            List<Long> deptList = departmentService.listSubDepartment(requestEntity.getCompanyId(), requestEntity.getDeptId());
            requestEntity.setDeptList(deptList);
        }
        //分页查询，每页的容量为capacity，查询第page页
        PageHelper.startPage(requestEntity.getPage(), requestEntity.getCapacity());
        List<ActionPositionDataDTO> list = examineActionMapper.getExamineListByPage(requestEntity);
        //遍历列表
        if (CollectionUtils.isNotEmpty(list)) {
            for (ActionPositionDataDTO tExamineDTO : list) {
                //将创建时间转为yyyy-MM格式
                tExamineDTO.setMonth(new SimpleDateFormat("yyyy-MM").format(Long.parseLong(tExamineDTO.getMonth())));
                //转换deptId为deptName
                String deptIds = tExamineDTO.getDeptId();
                //对是否有部门信息进行判断（离职人员没有所属部门）
                if(StringUtils.isNotBlank(deptIds)) {
                    String deptName = QueryCommonUtils.getDeptName(deptSet, deptIds);
                    tExamineDTO.setDeptName(deptName);
                }
                //将数据库中的类型英文字段获取枚举类中对应的value
                String type = ScoreRecordTypeEnum.getvalueBykey(tExamineDTO.getType());
                tExamineDTO.setType(type);
            }
        }
        return list;

    }
    @Override
    public void examineActionUserConfirm(String id, String userId) {
        ExamineAction examineAction = this.getById(id);
        UserDTO user = userService.getUserByUserId(userId,examineAction.getCompanyId());
        if(null == user){
            throw new ExamineException(EnumExamineError.USER_IS_EMPTY);
        }
        if(CollectionUtils.isEmpty(user.getUserDepartments())){
            throw new ExamineException(EnumExamineError.USER_DEPART_IS_EMPTY);
        }
        List<UserDepartment> userDepartments = user.getUserDepartments();
        List<String> departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList());
        for(String str:departMentList) {
            ExamineDepartment examineDepartmentVO = new ExamineDepartment();
            examineDepartmentVO.setId(UUIDS.getID());
            examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
            examineDepartmentVO.setExamineInstanceId(examineAction.getId());
            examineDepartmentVO.setDeleted(0);
            examineDepartmentVO.setDeptId(Integer.parseInt(str));
            examineDepartmentService.save(examineDepartmentVO);
        }
        examineAction.setFormValueUserId(userId);
        examineAction.setFormValueUserName(user.getName().trim());
        updateById(examineAction);
        //判断是否有积分记录，积分记录是否有对应的部门关系
        QueryWrapper<ActionScoreRecord> actionScoreRecordQueryWrapper = new QueryWrapper<>();
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getTargetId,id);
        actionScoreRecordQueryWrapper.lambda().eq(ActionScoreRecord::getDeleted,0);
        ActionScoreRecord actionScoreRecord = actionScoreRecordService.getOne(actionScoreRecordQueryWrapper,true);
        if(actionScoreRecord == null){
            return;
        }
        actionScoreRecord.setUserId(userId);
        actionScoreRecordService.updateById(actionScoreRecord);
        for(String str:departMentList) {
            ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
            actionScoreDepartment.setId(UUIDS.getID());
            actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
            actionScoreDepartment.setDeptId(Integer.parseInt(str));
            actionScoreDepartment.setDeleted(0);
            actionScoreDepartment.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
            actionScoreDepartmentService.save(actionScoreDepartment);
        }
    }
}
