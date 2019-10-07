package com.sancai.oa.examine.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.mapper.ExamineBusinessTravelMapper;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineBusinessTravelService;
import com.sancai.oa.examine.service.IExamineDepartmentService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.TimeConversionUtil;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.modify.DataMap;
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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 出差 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Slf4j
@Service
public class ExamineBusinessTravelServiceImpl extends ServiceImpl<ExamineBusinessTravelMapper, ExamineBusinessTravel> implements IExamineBusinessTravelService {

    @Autowired
    DingDingExamineService dingDingExamineService;

    @Autowired
    IExamineDepartmentService examineDepartmentService;


    @Autowired
    ExamineBusinessTravelMapper examineBusinessTravelMapper;

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
    public void dealExamineData(String taskInstanceId, String companyId, String processCode, long intervalTimeStart, long intervalTimeEnd) {
        TaskMessage.addMessage(taskInstanceId,"出差抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"模板code:"+processCode);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        int totalCount = 0;
        Long nextCursor = 0L;
        while (nextCursor != null) {
            List<ExamineBusinessTravel> examineEntityList = new ArrayList<>();
            List<ExamineDepartment> examineDepartmentList = new ArrayList<>();
            Map<List<ExamineInstanceDTO>, Long> examineInstanceMap = dingDingExamineService.getDingTalkExamineData(nextCursor, companyId, processCode, intervalTimeStart, intervalTimeEnd);
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
                    if (ExamineFormCompEnum.BUSINESSTRIPDETAIL.getValue().equals(formComponentValueVo.getName())) {
                        String value = formComponentValueVo.getValue();
                        List<String> valueList = JSONArray.parseArray(value, String.class);
                        valueList.stream().forEach(val -> {
                            ExamineBusinessTravel examineBusinessTravel = new ExamineBusinessTravel();
                            examineBusinessTravel.setId(UUIDS.getID());
                            examineBusinessTravel.setUserId(instanceVo.getOriginatorUserid());
                            examineBusinessTravel.setCompanyId(companyId);
                            examineBusinessTravel.setProcessCode(examineInstanceDTO.getProcessCode());
                            List<Integer> departMentList = new ArrayList<>();
                            //查表获取用户信息
                            UserDTO user = userService.getUserByUserId(examineBusinessTravel.getUserId(),companyId);
                            if(user != null){
                                examineBusinessTravel.setUserName(user.getName());
                                List<UserDepartment> userDepartments = user.getUserDepartments();
                                if(CollectionUtils.isNotEmpty(userDepartments)){
                                    departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                                    //TODO 根据ProcessInstanceId + taskInstanceId 缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                                    List<Integer> departList = examineActionService.getRecordOriginDeptIdsSaveRedis(examineInstanceDTO.getProcessInstanceId(),taskInstanceId,null);
                                    if(departList != null && departList.size()>0){
                                        departMentList = departList;
                                    }
                                }
                            }else {
                                examineBusinessTravel.setUserName(" ");
                            }
                            departMentList.stream().forEach(departId -> {
                                ExamineDepartment examineDepartmentVO = new ExamineDepartment();
                                examineDepartmentVO.setId(UUIDS.getID());
                                examineDepartmentVO.setExamineInstanceId(examineBusinessTravel.getId());
                                examineDepartmentVO.setDeptId(departId);
                                examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                                examineDepartmentVO.setDeleted(0);
                                examineDepartmentList.add(examineDepartmentVO);
                            });

                            examineBusinessTravel.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                            examineBusinessTravel.setProcessTitle(instanceVo.getTitle());
                            examineBusinessTravel.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                            examineBusinessTravel.setProcessFinishTime(instanceVo.getFinishTime() != null ? instanceVo.getFinishTime().getTime() : null);
                            examineBusinessTravel.setProcessStatus(instanceVo.getStatus());
                            examineBusinessTravel.setProcessResult(instanceVo.getResult());
                            examineBusinessTravel.setFormValueCompany(examineInstanceDTO.getCompany());
                            examineBusinessTravel.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                            examineBusinessTravel.setDeleted(0);
                            examineBusinessTravel.setTaskInstanceId(taskInstanceId);
                            businessTravelFormValue(examineBusinessTravel, val);
                            examineEntityList.add(examineBusinessTravel);
                        });
                    }
                });
            });

            if (CollectionUtils.isNotEmpty(examineEntityList)) {
                for (ExamineBusinessTravel examineBusinessTravel : examineEntityList) {
                    save(examineBusinessTravel);
                }
                log.info("分页拉取钉钉出差数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"抓取了:"+examineEntityList.size()+"条出差数据");
            totalCount += examineEntityList.size();

            if (CollectionUtils.isNotEmpty(examineDepartmentList)) {
                for (ExamineDepartment examineDepartment : examineDepartmentList) {
                    examineDepartmentService.save(examineDepartment);
                }
                log.info("分页插入审批实例和部门对应关系表成功");
            }

        }
        log.info("分期拉取钉钉出差数据成功");
        TaskMessage.addMessage(taskInstanceId,"拉取钉钉出差数据成功，共抓取"+totalCount+"条");
    }

    @Override
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExamineBusinessTravel> examineBusinessTravelList = list(new QueryWrapper<ExamineBusinessTravel>()
                .lambda().ne(ExamineBusinessTravel::getProcessStatus, "COMPLETED")
                .and(u -> u.ne(ExamineBusinessTravel::getProcessStatus, "TERMINATED"))
                .and(u -> u.eq(ExamineBusinessTravel::getDeleted, 0))
                .and(u -> u.between(ExamineBusinessTravel::getProcessCreateTime, lastMonth, nowTime)));
        examineBusinessTravelList.stream().forEach(examineBusinessTravel -> {
            String processInstanceId = examineBusinessTravel.getProcessInstanceId();
            String companyId = examineBusinessTravel.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo =
                    dingDingExamineService.examineInstanceGetById(processInstanceId, companyId);
            if(processInstanceTopVo == null){
                return;
            }
            if (ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())) {
                return;
            }
            if (processInstanceTopVo.getFinishTime() != null) {
                examineBusinessTravel.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examineBusinessTravel.setProcessStatus(processInstanceTopVo.getStatus());
            examineBusinessTravel.setProcessResult(processInstanceTopVo.getResult());
            examineBusinessTravel.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId,"上个月开始到现在，共"+examineBusinessTravelList.size()+"条出差数据需要更新");
        if (CollectionUtils.isNotEmpty(examineBusinessTravelList)) {
            examineBusinessTravelList = examineBusinessTravelList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId,"状态已变更，可以更新的出差数据共"+examineBusinessTravelList.size()+"条");
            if (CollectionUtils.isNotEmpty(examineBusinessTravelList)) {
                for(ExamineBusinessTravel examineBusinessTravel:examineBusinessTravelList){
                    updateById(examineBusinessTravel);
                }
                log.info("更新出差审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"更新出差数据成功，更新数据共"+examineBusinessTravelList.size()+"条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    /**
     * 处理钉钉返回的表单数据 到 自定义
     *
     * @param examineBusinessTravel
     * @param value
     */
    public void businessTravelFormValue(ExamineBusinessTravel examineBusinessTravel, String value) {
        Map<String, Object> valueMap;
        if (StringUtils.isNotBlank(value)) {
            valueMap = OaMapUtils.stringToMap(value);
        } else {
            return;
        }
        if (!OaMapUtils.mapIsAnyBlank(valueMap, ExamineFormCompEnum.ROWVALUE.getKey())) {
            List<Map<String, Object>> valMapList = (List) valueMap.get(ExamineFormCompEnum.ROWVALUE.getKey());
            valMapList.stream().forEach(valmap -> {
                if (ExamineFormCompEnum.TRAVELREASON.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object reason = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (reason != null) {
                        examineBusinessTravel.setFormValueReason(reason.toString());
                    }
                }
                if (ExamineFormCompEnum.FROMCITY.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object fromCity = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (fromCity != null) {
                        examineBusinessTravel.setFormValueFromCity(fromCity.toString());
                    }
                }
                if (ExamineFormCompEnum.TOCITY.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object toCity = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (toCity != null) {
                        examineBusinessTravel.setFormValueToCity(toCity.toString());
                    }
                }

                if (ExamineFormCompEnum.DDDATERANGEFIELD.getKey().equals(valmap.get(ExamineFormCompEnum.COMPONENTTYPE.getKey()))) {
                    List<String> dateRangeList = (List) valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (Integer.parseInt(ExamineFormCompEnum.DDDATERANGELENGTH.getKey()) == dateRangeList.size()) {
                        String startTime = dateRangeList.get(0);
                        String endTime = dateRangeList.get(1);
                        examineBusinessTravel.setFormValueStartOriginal(startTime);
                        examineBusinessTravel.setFormValueFinishOriginal(endTime);
                        if (startTime.endsWith(ExamineFormCompEnum.MORNING.getValue())) {
                            startTime = startTime.replace(ExamineFormCompEnum.MORNING.getValue(), "06:00:00");
                        } else if (startTime.endsWith(ExamineFormCompEnum.AFTERNOON.getValue())) {
                            startTime = startTime.replace(ExamineFormCompEnum.AFTERNOON.getValue(), "13:00:00");
                        } else {
                            startTime = startTime + " " + "06:00:00";
                        }
                        if (endTime.endsWith(ExamineFormCompEnum.MORNING.getValue())) {
                            endTime = endTime.replace(ExamineFormCompEnum.MORNING.getValue(), "12:30:00");
                        } else if (endTime.endsWith(ExamineFormCompEnum.AFTERNOON.getValue())) {
                            endTime = endTime.replace(ExamineFormCompEnum.AFTERNOON.getValue(), "20:00:00");
                        } else {
                            endTime = endTime + " " + "20:00:00";
                        }
                        LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                        LocalDateTime endDateTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                        examineBusinessTravel.setFormValueStartTime(LocalDateTimeUtils.getMilliByTime(startDateTime));
                        examineBusinessTravel.setFormValueFinishTime(LocalDateTimeUtils.getMilliByTime(endDateTime));
                    }
                }

                if (ExamineFormCompEnum.STARTTRANSPORT.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object startTransport = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    if (startTransport != null) {
                        examineBusinessTravel.setFormValueStartTransport(startTransport.toString());
                    }
                }
                if (ExamineFormCompEnum.FINISHTRANSPORT.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object finishTransport = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    examineBusinessTravel.setFormValueFinishTransport(finishTransport.toString());
                }

                if (ExamineFormCompEnum.HOTELTYPE.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object hotelType = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    examineBusinessTravel.setFormValueHotelType(hotelType.toString());
                }

                if (ExamineFormCompEnum.REMARK.getValue().equals(valmap.get(ExamineFormCompEnum.LABEL.getKey()))) {
                    Object remark = valmap.get(ExamineFormCompEnum.COMONENTVALUE.getKey());
                    examineBusinessTravel.setFormValueRemark(remark.toString());
                }
            });
        }
    }



    /**
     * 查询出差列表
     *
     * @param requestEntity 请求体封装的实体类
     * @return 返回出差列表分页结果集合
     */
    @Override
    public List<ExamineBusinessTravelDTO> getBusinessTravelList(RequestEntity requestEntity) {
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
        List<ExamineBusinessTravelDTO> list = examineBusinessTravelMapper.getBusinessTravelListByPage(requestEntity);
        //调用工具类,将部门id转换为部门name
        if (CollectionUtils.isNotEmpty(list)) {
            QueryCommonUtils.setDeptNameAll(list, deptSet);
        }
        return list;
    }


    /**
     * 查询出差记录详情
     *
     * @param id 出差记录
     * @return 返回出差详情
     */
    @Override
    public ExamineBusinessTravelDetailDTO getBusinessTravelDetails(String id) {

        ExamineBusinessTravelDetailDTO businessTravelDetail = examineBusinessTravelMapper.getBusinessTravelDetails(id);
        if (businessTravelDetail != null) {
            String deptIds = businessTravelDetail.getDeptName();
            StringBuilder builder = new StringBuilder();
            if (deptIds.contains(",")) {
                String[] split = deptIds.split(",");
                for (String s : split) {
                    if(builder.length()==0){
                        builder.append(redisUtil.hmget(s).get("name"));
                    }else{
                        builder.append(","+redisUtil.hmget(s).get("name"));
                    }
                }
                businessTravelDetail.setDeptName(builder + "");
                return businessTravelDetail;
            }
            businessTravelDetail.setDeptName((String) redisUtil.hmget(deptIds).get("name"));
            return businessTravelDetail;
        }
        return null;
    }

    @Override
    public List<ExamineBusinessTravel> examineBusinessTravelListByInstanceId(String instanceId) {
        QueryWrapper<ExamineBusinessTravel> examineBusinessTravelQueryWrapper = new QueryWrapper<>();
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getTaskInstanceId,instanceId);
        examineBusinessTravelQueryWrapper.lambda().eq(ExamineBusinessTravel::getDeleted,0);
        List<ExamineBusinessTravel> examineBusinessTravelList = list(examineBusinessTravelQueryWrapper);
        return examineBusinessTravelList;
    }

    @Override
    public void examineBusinessTravelDataDelete(List<ExamineBusinessTravel> examineBusinessTravelList) {
        List<String> ids = new ArrayList<>();
        //删除出差审批数据
        examineBusinessTravelList.stream().forEach(examineBusinessTravel -> {
            examineBusinessTravel.setDeleted(1);
            ids.add(examineBusinessTravel.getId());
            updateById(examineBusinessTravel);
        });
        if(CollectionUtils.isEmpty(ids)){
            return;
        }
        //删除对应部门关系表
        ExamineDepartment examineDepartment = new ExamineDepartment();
        examineDepartment.setDeleted(1);
        UpdateWrapper<ExamineDepartment> examineDepartmentUpdateWrapper = new UpdateWrapper<>();
        examineDepartmentUpdateWrapper.lambda().in(ExamineDepartment::getExamineInstanceId,ids);
        examineDepartmentService.update(examineDepartment,examineDepartmentUpdateWrapper);
    }

    @Override
    public void pullBusinessTravelExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.BUSINESSTRAVEL.getKey())
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
        //如果根据实例id查到值,根据实例id删除之前的已经保存的数据 , 重新获取抓取开始时间和结束时间
        List<ExamineBusinessTravel> examineBusinessTravelList = examineBusinessTravelListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(examineBusinessTravelList)){
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                //TODO 根据processInstanceId + taskInstanceId 把记录对应的部门集合存入缓存
                List<DataMap> actionRecordLists = examineBusinessTravelMapper.businessRecordDeptIdList(companyId,taskInstanceId);
                examineActionService.recordOriginDeptIdsSaveRedis(actionRecordLists);
            }
            //删除出差审批数据
            examineBusinessTravelDataDelete(examineBusinessTravelList);
        }
        //钉钉抓取出差数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(),7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取出差数据
            dealExamineData(taskInstanceId,companyId,examine.getCode(),intervalTimeStart,intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }


}
