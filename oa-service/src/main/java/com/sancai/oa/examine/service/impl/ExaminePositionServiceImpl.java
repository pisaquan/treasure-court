package com.sancai.oa.examine.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.core.ApiResponse;
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
import com.sancai.oa.examine.mapper.ExaminePositionMapper;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineDepartmentService;
import com.sancai.oa.examine.service.IExaminePositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.TimeConversionUtil;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.enums.ScoreTypeEnum;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.typestatus.enums.ScoreRecordTypeEnum;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
 * 岗位奖罚 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Service
@Slf4j
public class ExaminePositionServiceImpl extends ServiceImpl<ExaminePositionMapper, ExaminePosition> implements IExaminePositionService {

    @Autowired
    DingDingExamineService dingDingExamineService;

    @Autowired
    IExamineDepartmentService examineDepartmentService;

    @Autowired
    IActionScoreRecordService actionScoreRecordService;

    @Autowired
    IActionScoreDepartmentService actionScoreDepartmentService;
	
	@Autowired
    ExaminePositionMapper examinePositionMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private IUserService userService;

    @Autowired
    private IExamineService examineService;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private IExamineActionService examineActionService;


    @Override
    public void dealExamineData(String taskInstanceId,String companyId,String processCode,long intervalTimeStart,long intervalTimeEnd){
        TaskMessage.addMessage(taskInstanceId,"岗位考核抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"模板code:"+processCode);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        int totalCount = 0;
        Long nextCursor = 0L;
        while(nextCursor != null) {
            List<ExaminePosition> examineEntityList = new ArrayList<>();
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
                    if(ExamineFormCompEnum.POSTASSESSMENT.getValue().equals(formComponentValueVo.getName()) ||
                            ExamineFormCompEnum.TABLEFIELD.getKey().equals(formComponentValueVo.getComponentType())){
                        String value = formComponentValueVo.getValue();
                        List<String> valueList = JSONArray.parseArray(value, String.class);
                        valueList.stream().forEach(val -> {
                            ExaminePosition examinePosition = new ExaminePosition();

                            examinePosition.setId(UUIDS.getID());
                            examinePosition.setCompanyId(companyId);
                            examinePosition.setUserId(instanceVo.getOriginatorUserid());

                            //查表获取用户信息
                            UserDTO originatorUser = userService.getUserByUserId(examinePosition.getUserId(),companyId);
                            if(originatorUser != null){
                                examinePosition.setUserName(originatorUser.getName());
                            }else {
                                examinePosition.setUserName(" ");
                            }
                            examinePosition.setProcessCode(examineInstanceDTO.getProcessCode());
                            examinePosition.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                            examinePosition.setProcessTitle(instanceVo.getTitle());
                            examinePosition.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                            examinePosition.setProcessFinishTime(instanceVo.getFinishTime() != null ? instanceVo.getFinishTime().getTime(): null);
                            examinePosition.setProcessStatus(instanceVo.getStatus());
                            examinePosition.setProcessResult(instanceVo.getResult());
                            examinePosition.setFormValueCompany(examineInstanceDTO.getCompany());
                            examinePosition.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                            examinePosition.setDeleted(0);
                            examinePosition.setTaskInstanceId(taskInstanceId);
                            businessTravelFormValue(examinePosition,val);
                            List<Integer> departMentList = new ArrayList<>();
                            //查表获取用户信息
                            UserDTO user = userService.getUserByUserId(examinePosition.getFormValueUserId(),companyId);
                            if(user != null){
                                examinePosition.setFormValueUserName(user.getName());
                                List<UserDepartment> userDepartments = user.getUserDepartments();
                                if(CollectionUtils.isNotEmpty(userDepartments)){
                                    departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                                    //TODO 根据ProcessInstanceId + taskInstanceId + FormValueUserId缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                                    List<Integer> departList = examineActionService.getRecordOriginDeptIdsSaveRedis(examinePosition.getProcessInstanceId(),examinePosition.getTaskInstanceId(),examinePosition.getFormValueUserId());
                                    if(departList != null && departList.size()>0){
                                        departMentList = departList;
                                    }
                                }
                            }
                            departMentList.stream().forEach(departId -> {
                                ExamineDepartment examineDepartmentVO = new ExamineDepartment();
                                examineDepartmentVO.setId(UUIDS.getID());
                                examineDepartmentVO.setExamineInstanceId(examinePosition.getId());
                                examineDepartmentVO.setDeptId(departId);
                                examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                                examineDepartmentVO.setDeleted(0);
                                examineDepartmentList.add(examineDepartmentVO);
                            });
                            examineEntityList.add(examinePosition);
                            //审批通过之后更改积分
                            if(ExamineStatusEnum.COMPLETED.getKey().equals(instanceVo.getStatus()) &&
                                    ExamineStatusEnum.AGREE.getKey().equals(instanceVo.getResult())){
                                ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
                                actionScoreRecord.setId(UUIDS.getID());
                                actionScoreRecord.setUserId(examinePosition.getFormValueUserId());
                                actionScoreRecord.setUserName(examinePosition.getFormValueUserName());
                                actionScoreRecord.setCompanyId(examinePosition.getCompanyId());
                                actionScoreRecord.setSource(ExamineTypeEnum.EXAMINEPOSITION.getKey());
                                actionScoreRecord.setTargetId(examinePosition.getId());
                                actionScoreRecord.setType(examinePosition.getFormValueType());
                                actionScoreRecord.setScore(examinePosition.getFormValueScore());
                                actionScoreRecord.setRemark(examinePosition.getFormValueReason());
                                actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                                actionScoreRecord.setDeleted(0);
                                actionScoreRecord.setScoreRecordTime(examinePosition.getProcessCreateTime());
                                actionScoreRecordList.add(actionScoreRecord);
                                departMentList.stream().forEach(departId -> {
                                    //插入积分变动记录表和部门对应关系表
                                    ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                                    actionScoreDepartment.setId(UUIDS.getID());
                                    actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
                                    actionScoreDepartment.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                                    actionScoreDepartment.setDeptId(departId);
                                    actionScoreDepartment.setDeleted(0);
                                    actionScoreDepartmentList.add(actionScoreDepartment);
                                });
                            }
                        });
                    }
                });
            });

            if(CollectionUtils.isNotEmpty(examineEntityList)){
                for(ExaminePosition examinePosition : examineEntityList){
                    save(examinePosition);
                }
                log.info("分页拉取钉钉岗位考核数据成功");
            }

            TaskMessage.addMessage(taskInstanceId,"抓取了:"+examineEntityList.size()+"条岗位考核数据");
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
        log.info("分期拉取钉钉岗位考核数据成功");
        TaskMessage.addMessage(taskInstanceId,"拉取钉钉外出申请数据成功，共抓取"+totalCount+"条");
    }

    @Override
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExaminePosition> examinePositionList = list(new QueryWrapper<ExaminePosition>()
                .lambda().ne(ExaminePosition::getProcessStatus,"COMPLETED")
                .and(u -> u.ne(ExaminePosition::getProcessStatus,"TERMINATED"))
                .and(u -> u.eq(ExaminePosition::getDeleted,0))
                .and(u -> u.between(ExaminePosition::getProcessCreateTime,lastMonth,nowTime)));
        examinePositionList.stream().forEach(examinePosition -> {
            String processInstanceId = examinePosition.getProcessInstanceId();
            String companyId = examinePosition.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo =
                    dingDingExamineService.examineInstanceGetById(processInstanceId,companyId);
            if(processInstanceTopVo == null){
                return;
            }
            if(ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())){
                return;
            }
            if(processInstanceTopVo.getFinishTime() != null){
                examinePosition.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examinePosition.setProcessResult(processInstanceTopVo.getResult());
            examinePosition.setProcessStatus(processInstanceTopVo.getStatus());
            examinePosition.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId,"上个月开始到现在，共"+examinePositionList.size()+"条岗位考核数据需要更新");
        if(CollectionUtils.isNotEmpty(examinePositionList)){
            examinePositionList = examinePositionList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId,"状态已变更，可以更新的岗位考核数据共"+examinePositionList.size()+"条");
            if(CollectionUtils.isNotEmpty(examinePositionList)){
                for(ExaminePosition examinePosition:examinePositionList){
                    updateById(examinePosition);
                    //积分记录表插入数据
                    if(ExamineStatusEnum.COMPLETED.getKey().equals(examinePosition.getProcessStatus()) &&
                            ExamineStatusEnum.AGREE.getKey().equals(examinePosition.getProcessResult())){
                        ActionScoreRecord actionScoreRecord = new ActionScoreRecord();
                        actionScoreRecord.setId(UUIDS.getID());
                        actionScoreRecord.setUserId(examinePosition.getFormValueUserId());
                        actionScoreRecord.setUserName(examinePosition.getFormValueUserName());
                        actionScoreRecord.setCompanyId(examinePosition.getCompanyId());
                        actionScoreRecord.setSource(ExamineTypeEnum.EXAMINEPOSITION.getKey());
                        actionScoreRecord.setTargetId(examinePosition.getId());
                        actionScoreRecord.setType(examinePosition.getFormValueType());
                        actionScoreRecord.setScore(examinePosition.getFormValueScore());
                        actionScoreRecord.setRemark(examinePosition.getFormValueReason());
                        actionScoreRecord.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
                        actionScoreRecord.setScoreRecordTime(examinePosition.getProcessCreateTime());
                        actionScoreRecord.setDeleted(0);
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
                log.info("更新岗位考核审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"更新岗位考核数据成功，更新数据共"+examinePositionList.size()+"条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    /**
     * 处理钉钉返回的表单数据 到 自定义
     * @param examinePosition
     * @param value
     */
    public void businessTravelFormValue(ExaminePosition examinePosition,String value){
        Map<String,Object> valueMap;
        if(StringUtils.isNotBlank(value)){
            valueMap = OaMapUtils.stringToMap(value);
        }else{
            return;
        }
        if(!OaMapUtils.mapIsAnyBlank(valueMap,ExamineFormCompEnum.ROWVALUE.getKey())){
            List<Map<String,Object>> valMapList = (List)valueMap.get(ExamineFormCompEnum.ROWVALUE.getKey());
            valMapList.stream().forEach(valmap -> {
                if(ExamineFormCompEnum.STAFF.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    List<Map<String,Object>> extendVueList = (List)valmap.get(ExamineFormCompEnum.EXTENDVALUE.getKey());
                    extendVueList.stream().forEach(ex -> {
                        Object userId = ex.get(ExamineFormCompEnum.EMPLID.getKey());
                        if(userId != null){
                            examinePosition.setFormValueUserName(" ");
                            examinePosition.setFormValueUserId(userId.toString());
                        }
                    });
                }

                if(ExamineFormCompEnum.DIMISSIONSTAFF.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object name = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(name != null){
                        examinePosition.setFormValueUserName(name.toString().trim());
                    }
                }

                if(ExamineFormCompEnum.REWARDSPUNISHTYPE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object type = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(type != null){
                        examinePosition.setFormValueType(ScoreTypeEnum.getKeyByValue(type.toString()));
                    }
                }

                if(ExamineFormCompEnum.REWARDSPUNISHRULE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object rule = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(rule != null){
                        examinePosition.setFormValueRule(rule.toString());
                    }
                }

                if(ExamineFormCompEnum.SCORE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object score = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(score != null){
                        examinePosition.setFormValueScore(Math.abs(Float.valueOf(score.toString())));
                    }
                }

                if(ExamineFormCompEnum.REWARDSPUNISHREASON.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object reason = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(reason != null){
                        examinePosition.setFormValueReason(reason.toString());
                    }
                }

                if(ExamineFormCompEnum.ISINSERVICE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))){
                    Object reason = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if(reason != null){
                        examinePosition.setIsInservice(UserStatusTypeEnum.getKeyByValue(reason.toString()));
                    }
                }

            });
        }
    }

    /**
     * 岗位考核列表查询
     * @param requestEntity 请求体封装的实体类
     * @return 返回岗位考核列表分页结果，排序
     */
    @Override
    public List<ActionPositionDataDTO> getExamineListByPage(RequestEntity requestEntity) {
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
        List<ActionPositionDataDTO> list = examinePositionMapper.getExamineListByPage(requestEntity);
        //遍历列表
        if (CollectionUtils.isNotEmpty(list)) {
            for (ActionPositionDataDTO tExamineDTO : list) {
                //将创建时间转为yyyy-MM格式
                String month = new SimpleDateFormat("yyyy-MM").format(Long.parseLong(tExamineDTO.getMonth()));
                tExamineDTO.setMonth(month);
                //转换deptId为deptName
                String deptIds = tExamineDTO.getDeptId();
                //对是否有部门信息进行判断（离职人员没有所属部门）
                if(StringUtils.isNotBlank(deptIds) ){
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
    public List<ExaminePosition> examinePositionListByInstanceId(String instanceId) {
        QueryWrapper<ExaminePosition> examinePositionQueryWrapper = new QueryWrapper<>();
        examinePositionQueryWrapper.lambda().eq(ExaminePosition::getTaskInstanceId,instanceId);
        examinePositionQueryWrapper.lambda().eq(ExaminePosition::getDeleted,0);
        List<ExaminePosition> examinePositionList = list(examinePositionQueryWrapper);
        return examinePositionList;
    }

    @Override
    public void examinePositionDataDelete(List<ExaminePosition> examinePositionList) {
        List<String> ids = new ArrayList<>();
        List<String> recordIds = new ArrayList<>();
        //删除岗位考核审批数据
        examinePositionList.stream().forEach(examinePosition -> {
            examinePosition.setDeleted(1);
            ids.add(examinePosition.getId());
            updateById(examinePosition);
        });
        //删除岗位考核对应部门关系表
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

    @Override
    public void pullPositionExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.EXAMINEPOSITION.getKey())
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
        List<ExaminePosition> examinePositionList = examinePositionListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(examinePositionList)){
            //TODO 根据companyId  + taskInstanceId 把记录对应的部门集合存入缓存
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                List<DataMap> actionRecordLists = examinePositionMapper.positionRecordDeptIdList(companyId,taskInstanceId);
                examineActionService.recordOriginDeptIdsSaveRedis(actionRecordLists);
            }

            //删除岗位考核审批数据
            examinePositionDataDelete(examinePositionList);
        }
        //钉钉抓取岗位考核数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(), 7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取岗位考核数据
            dealExamineData(taskInstanceId, companyId, examine.getCode(), intervalTimeStart, intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    @Override
    public void examinePositionUserConfirm(String id, String userId) {
        ExaminePosition examinePosition = getById(id);
        UserDTO user = userService.getUserByUserId(userId,examinePosition.getCompanyId());
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
            examineDepartmentVO.setDeleted(0);
            examineDepartmentVO.setExamineInstanceId(examinePosition.getId());
            examineDepartmentVO.setDeptId(Integer.parseInt(str));
            examineDepartmentService.save(examineDepartmentVO);
        }
        examinePosition.setFormValueUserId(userId);
        examinePosition.setFormValueUserName(user.getName().trim());
        updateById(examinePosition);
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
            actionScoreDepartment.setCreateTime(LocalDateTimeUtils.getMilliByTime(LocalDateTime.now()));
            actionScoreDepartment.setDeleted(0);
            actionScoreDepartmentService.save(actionScoreDepartment);
        }
    }

}
