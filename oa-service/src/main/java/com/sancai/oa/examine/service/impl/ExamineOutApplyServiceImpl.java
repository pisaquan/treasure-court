package com.sancai.oa.examine.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.sancai.oa.dingding.examine.DingDingExamineService;
import com.sancai.oa.examine.entity.Examine;
import com.sancai.oa.examine.entity.ExamineInstanceDTO;
import com.sancai.oa.examine.entity.ExamineOutApply;
import com.sancai.oa.examine.entity.enums.ExamineFormCompEnum;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.ExamineTypeEnum;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.mapper.ExamineOutApplyMapper;
import com.sancai.oa.examine.service.IExamineOutApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.TimeConversionUtil;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.LocalDateTimeUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 外出申请 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Service
@Slf4j
public class ExamineOutApplyServiceImpl extends ServiceImpl<ExamineOutApplyMapper, ExamineOutApply> implements IExamineOutApplyService {

    @Autowired
    DingDingExamineService dingDingExamineService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IExamineService examineService;

    @Override
    public void dealExamineData(String taskInstanceId,String companyId,String processCode,long intervalTimeStart,long intervalTimeEnd){
        TaskMessage.addMessage(taskInstanceId,"外出申请抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"模板code:"+processCode);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        int totalCount = 0;
        Long nextCursor = 0L;
        while(nextCursor != null) {
            List<ExamineOutApply> examineEntityList = new ArrayList<>();
            Map<List<ExamineInstanceDTO>, Long> examineInstanceMap = dingDingExamineService.getDingTalkExamineData(nextCursor, companyId, processCode,intervalTimeStart,intervalTimeEnd);
            List<ExamineInstanceDTO> examineInstanceDTOList = new ArrayList<>();
            for (Map.Entry<List<ExamineInstanceDTO>, Long> entryMap : examineInstanceMap.entrySet()) {
                examineInstanceDTOList = entryMap.getKey();
                nextCursor = entryMap.getValue();
                break;
            }
            examineInstanceDTOList.stream().forEach(examineInstanceDTO -> {
                OapiProcessinstanceGetResponse.ProcessInstanceTopVo instanceVo = examineInstanceDTO.getProcessInstanceTopVo();
                ExamineOutApply examineOutApplyVO = new ExamineOutApply();
                examineOutApplyVO.setId(UUIDS.getID());
                examineOutApplyVO.setUserId(instanceVo.getOriginatorUserid());
                examineOutApplyVO.setCompanyId(companyId);
                examineOutApplyVO.setProcessCode(examineInstanceDTO.getProcessCode());
                //查表获取用户信息
                UserDTO user = userService.getUserByUserId(examineOutApplyVO.getUserId(),companyId);
                if(user != null){
                    examineOutApplyVO.setUserName(user.getName());
                }else {
                    examineOutApplyVO.setUserName(" ");
                }

                examineOutApplyVO.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                examineOutApplyVO.setProcessTitle(instanceVo.getTitle());
                examineOutApplyVO.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                if(instanceVo.getFinishTime() != null){
                    examineOutApplyVO.setProcessFinishTime(instanceVo.getFinishTime().getTime());
                }
                examineOutApplyVO.setProcessStatus(instanceVo.getStatus());
                examineOutApplyVO.setProcessResult(instanceVo.getResult());

                outApplyFormValue(examineOutApplyVO,instanceVo.getFormComponentValues());
                examineOutApplyVO.setFormValueCompany(examineInstanceDTO.getCompany());
                if(StringUtils.isNotBlank(instanceVo.getOriginatorDeptId())){
                    examineOutApplyVO.setFormValueDeptId(Integer.parseInt(instanceVo.getOriginatorDeptId()));
                }
                examineOutApplyVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                examineOutApplyVO.setDeleted(0);
                examineOutApplyVO.setTaskInstanceId(taskInstanceId);
                examineEntityList.add(examineOutApplyVO);
            });

            if(CollectionUtils.isNotEmpty(examineEntityList)){
                for(ExamineOutApply examineOutApply : examineEntityList){
                    save(examineOutApply);
                }
                log.info("分页拉取钉钉外出数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"抓取了:"+examineEntityList.size()+"条外出申请数据");
            totalCount += examineEntityList.size();
        }
        log.info("分期拉取钉钉外出数据成功");
        TaskMessage.addMessage(taskInstanceId,"拉取钉钉外出申请数据成功，共抓取"+totalCount+"条");
    }

    @Override
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExamineOutApply> examineOutApplyList = list(new QueryWrapper<ExamineOutApply>()
                .lambda().ne(ExamineOutApply::getProcessStatus,"COMPLETED")
                .and(u -> u.ne(ExamineOutApply::getProcessStatus,"TERMINATED"))
                .and(u -> u.eq(ExamineOutApply::getDeleted,0))
                .and(u -> u.between(ExamineOutApply::getProcessCreateTime,lastMonth,nowTime)));
        examineOutApplyList.stream().forEach(examineOutApply -> {
            String processInstanceId = examineOutApply.getProcessInstanceId();
            String companyId = examineOutApply.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo =
                    dingDingExamineService.examineInstanceGetById(processInstanceId,companyId);
            if(processInstanceTopVo == null){
                return;
            }
            if(ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())){
                return;
            }
            if(processInstanceTopVo.getFinishTime() != null){
                examineOutApply.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examineOutApply.setProcessStatus(processInstanceTopVo.getStatus());
            examineOutApply.setProcessResult(processInstanceTopVo.getResult());
            examineOutApply.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId,"上个月开始到现在，共"+examineOutApplyList.size()+"条外出申请数据需要更新");
        if(CollectionUtils.isNotEmpty(examineOutApplyList)){
            examineOutApplyList = examineOutApplyList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId,"状态已变更，可以更新的外出申请数据共"+examineOutApplyList.size()+"条");
            if(CollectionUtils.isNotEmpty(examineOutApplyList)){
                for(ExamineOutApply examineOutApply:examineOutApplyList){
                    updateById(examineOutApply);
                }
                log.info("更新外出申请审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"更新外出申请数据成功，更新数据共"+examineOutApplyList.size()+"条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    @Override
    public List<ExamineOutApply> examineOutApplyListByInstanceId(String instanceId) {
        QueryWrapper<ExamineOutApply> examineOutApplyQueryWrapper = new QueryWrapper<>();
        examineOutApplyQueryWrapper.lambda().eq(ExamineOutApply::getTaskInstanceId,instanceId);
        examineOutApplyQueryWrapper.lambda().eq(ExamineOutApply::getDeleted,0);
        List<ExamineOutApply> examineOutApplyList = list(examineOutApplyQueryWrapper);
        return examineOutApplyList;
    }

    @Override
    public void examineOutApplyDataDelete(String taskInstanceId) {
        ExamineOutApply examineOutApply = new ExamineOutApply();
        examineOutApply.setDeleted(1);
        UpdateWrapper<ExamineOutApply> examineOutApplyUpdateWrapper = new UpdateWrapper<>();
        examineOutApplyUpdateWrapper.lambda().eq(ExamineOutApply::getTaskInstanceId,taskInstanceId);
        update(examineOutApply,examineOutApplyUpdateWrapper);
    }

    @Override
    public void pullOutApplyExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.OUTAPPLY.getKey())
                .and(e -> e.eq(Examine::getCompanyId, companyId)
                        .and(t -> t.eq(Examine::getDeleted,0))),true);
        if(examine == null){
            log.error(EnumExamineError.EXAMINE_IS_EMPTY.getMessage()+" companyId="+companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_IS_EMPTY);
        }
        if(StringUtils.isBlank(examine.getCode())){
            log.error(EnumExamineError.EXAMINE_CODE_IS_EMPTY.getMessage()+" companyId="+companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_CODE_IS_EMPTY);
        }
        //删除外出申请审批数据
        examineOutApplyDataDelete(taskInstanceId);
        //钉钉抓取外出申请数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(),7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取外出申请数据
            dealExamineData(taskInstanceId,companyId,examine.getCode(),intervalTimeStart,intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    /**
     * 处理钉钉返回的表单数据 到 自定义
     * @param examineOutApplyVO
     * @param formComponentValueVoList
     */
    public void outApplyFormValue(ExamineOutApply examineOutApplyVO, List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList){
        if(CollectionUtils.isEmpty(formComponentValueVoList)){
            return;
        }
        formComponentValueVoList.stream().forEach(formComponentValueVo -> {
            if(ExamineFormCompEnum.TIMERANGE.getValue().equals(formComponentValueVo.getName()) &&
                    ExamineFormCompEnum.DDGOOUTFILED.getKey().equals(formComponentValueVo.getComponentType())){
                String value = formComponentValueVo.getValue();
                List<String> leaveFieldList= JSONArray.parseArray(value, String.class);
                if(leaveFieldList.size() == Integer.parseInt(ExamineFormCompEnum.DDHOLIDAYFIELDLENGTH.getKey())){
                    String startTime = leaveFieldList.get(0)+ ":00";
                    String endTime = leaveFieldList.get(1)+ ":00";
                    if(StringUtils.isNotBlank(leaveFieldList.get(2))){
                        examineOutApplyVO.setFormValueHours(Float.parseFloat(leaveFieldList.get(2)));
                    }
                    LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    LocalDateTime endDateTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    examineOutApplyVO.setFormValueStart(LocalDateTimeUtils.getMilliByTime(startDateTime));
                    examineOutApplyVO.setFormValueFinish(LocalDateTimeUtils.getMilliByTime(endDateTime));
                }
            }

            if(ExamineFormCompEnum.OUTAPPLYREASON.getValue().equals(formComponentValueVo.getName())){
                examineOutApplyVO.setFormValueReason(formComponentValueVo.getValue());
            }

            if(ExamineFormCompEnum.ISNOTPUNCH.getValue().equals(formComponentValueVo.getName())){
                examineOutApplyVO.setFormValueClockin(formComponentValueVo.getValue());
            }

            if(ExamineFormCompEnum.DDGOOUTLOCATION.getValue().equals(formComponentValueVo.getName())){
                examineOutApplyVO.setFormValuePlace(formComponentValueVo.getValue());
            }

            if(ExamineFormCompEnum.NONORMALCLOCK.getValue().equals(formComponentValueVo.getName())){
                examineOutApplyVO.setFormValueNoClockinTime(formComponentValueVo.getValue());
            }

        });

    }
}
