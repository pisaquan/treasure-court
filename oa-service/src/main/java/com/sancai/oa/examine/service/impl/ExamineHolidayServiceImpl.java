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
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.mapper.ExamineHolidayMapper;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineDepartmentService;
import com.sancai.oa.examine.service.IExamineHolidayService;
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
import com.sancai.oa.typestatus.enums.HolidayCountTypeEnum;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.taobao.api.ApiException;
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
 * 公休假 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Slf4j
@Service

public class ExamineHolidayServiceImpl extends ServiceImpl<ExamineHolidayMapper, ExamineHoliday> implements IExamineHolidayService {

    @Autowired
    DingDingExamineService dingDingExamineService;

    @Autowired
    IExamineDepartmentService examineDepartmentService;

    @Autowired
    ExamineHolidayMapper examineHolidayMapper;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private IUserService userService;

    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private IExamineService examineService;

    @Autowired
    private IDepartmentService departmentService;
    @Autowired
    private IExamineActionService examineActionService;

    @Override
    public void dealExamineData(String taskInstanceId,String companyId,String processCode,long intervalTimeStart,long intervalTimeEnd) {
        TaskMessage.addMessage(taskInstanceId,"休假抓取  companyId："+companyId);
        TaskMessage.addMessage(taskInstanceId,"模板code:"+processCode);
        TaskMessage.addMessage(taskInstanceId,"抓取时间范围: "+LocalDateTimeUtils.formatDateTime(intervalTimeStart)+" 到 "+ LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        int totalCount = 0;
        Long nextCursor = 0L;
        while (nextCursor != null) {
            List<ExamineHoliday> examineEntityList = new ArrayList<>();
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
                ExamineHoliday examineHolidayVO = new ExamineHoliday();
                examineHolidayVO.setId(UUIDS.getID());
                examineHolidayVO.setUserId(instanceVo.getOriginatorUserid());
                examineHolidayVO.setCompanyId(companyId);
                examineHolidayVO.setProcessCode(examineInstanceDTO.getProcessCode());
                List<Integer> departMentList = new ArrayList<>();
                //查表获取用户信息
                UserDTO user = userService.getUserByUserId(examineHolidayVO.getUserId(),companyId);
                if(user != null){
                    examineHolidayVO.setUserName(user.getName());
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
                    examineHolidayVO.setUserName(" ");
                }
                departMentList.stream().forEach(departId -> {
                    ExamineDepartment examineDepartmentVO = new ExamineDepartment();
                    examineDepartmentVO.setId(UUIDS.getID());
                    examineDepartmentVO.setExamineInstanceId(examineHolidayVO.getId());
                    examineDepartmentVO.setDeptId(departId);
                    examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                    examineDepartmentVO.setDeleted(0);
                    examineDepartmentList.add(examineDepartmentVO);
                });

                examineHolidayVO.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                examineHolidayVO.setProcessTitle(instanceVo.getTitle());
                examineHolidayVO.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                examineHolidayVO.setProcessFinishTime(instanceVo.getFinishTime() != null ? instanceVo.getFinishTime().getTime() : null);
                examineHolidayVO.setProcessStatus(instanceVo.getStatus());
                examineHolidayVO.setProcessResult(instanceVo.getResult());
                holidayFormValue(examineHolidayVO, instanceVo.getFormComponentValues());
                examineHolidayVO.setFormValueCompany(examineInstanceDTO.getCompany());
                examineHolidayVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                examineHolidayVO.setDeleted(0);
                examineHolidayVO.setTaskInstanceId(taskInstanceId);
                examineEntityList.add(examineHolidayVO);
            });

            if (CollectionUtils.isNotEmpty(examineEntityList)) {
                for (ExamineHoliday examineHoliday : examineEntityList) {
                    save(examineHoliday);
                }
                log.info("分页拉取钉钉休假数据成功");
            }

            TaskMessage.addMessage(taskInstanceId,"抓取了:"+examineEntityList.size()+"条休假数据");
            totalCount += examineEntityList.size();

            if (CollectionUtils.isNotEmpty(examineDepartmentList)) {
                for (ExamineDepartment examineDepartment : examineDepartmentList) {
                    examineDepartmentService.save(examineDepartment);
                }
                log.info("分页插入审批实例和部门对应关系表成功");
            }
        }
        log.info("分期拉取钉钉休假数据成功");
        TaskMessage.addMessage(taskInstanceId,"拉取钉钉休假数据成功，共抓取"+totalCount+"条");
    }

    @Override
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExamineHoliday> examineHolidayList = list(new QueryWrapper<ExamineHoliday>()
                .lambda().ne(ExamineHoliday::getProcessStatus, "COMPLETED")
                .and(u -> u.ne(ExamineHoliday::getProcessStatus, "TERMINATED"))
                .and(u -> u.eq(ExamineHoliday::getDeleted, 0))
                .and(u -> u.between(ExamineHoliday::getProcessCreateTime, lastMonth, nowTime)));
        examineHolidayList.stream().forEach(examineHoliday -> {
            String processInstanceId = examineHoliday.getProcessInstanceId();
            String companyId = examineHoliday.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo =
                    dingDingExamineService.examineInstanceGetById(processInstanceId, companyId);
            if(processInstanceTopVo == null){
                return;
            }
            if (ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())) {
                return;
            }
            if (processInstanceTopVo.getFinishTime() != null) {
                examineHoliday.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examineHoliday.setProcessStatus(processInstanceTopVo.getStatus());
            examineHoliday.setProcessResult(processInstanceTopVo.getResult());
            examineHoliday.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId,"上个月开始到现在，共"+examineHolidayList.size()+"条休假数据需要更新");
        if (CollectionUtils.isNotEmpty(examineHolidayList)) {
            examineHolidayList = examineHolidayList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId,"状态已变更，可以更新的休假数据共"+examineHolidayList.size()+"条");
            if (CollectionUtils.isNotEmpty(examineHolidayList)) {
                for(ExamineHoliday examineHoliday:examineHolidayList){
                    updateById(examineHoliday);
                }
                log.info("更新休假审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId,"更新休假数据成功，更新数据共"+examineHolidayList.size()+"条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }


    /**
     * 处理钉钉返回的表单数据 到 自定义
     *
     * @param examineHolidayVO
     * @param formComponentValueVoList
     */
    public void holidayFormValue(ExamineHoliday examineHolidayVO, List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList  ) {
        if (CollectionUtils.isEmpty(formComponentValueVoList)) {
            return;
        }
        formComponentValueVoList.stream().forEach(formComponentValueVo -> {

            if (ExamineFormCompEnum.HOLIDAYTYPE.getValue().equals(formComponentValueVo.getName())) {
                examineHolidayVO.setFormValueType(LeaveTypeEnum.getKeyByValue(formComponentValueVo.getValue()));
            }
            if (ExamineFormCompEnum.TIMERANGE.getValue().equals(formComponentValueVo.getName()) &&
                    ExamineFormCompEnum.DDDATERANGEFIELD.getKey().equals(formComponentValueVo.getComponentType())) {
                String value = formComponentValueVo.getValue();
                List<String> leaveFieldList = JSONArray.parseArray(value, String.class);
                if (leaveFieldList.size() == Integer.parseInt(ExamineFormCompEnum.DDDATERANGELENGTH.getKey())) {
                    String startTime = leaveFieldList.get(0);
                    String endTime = leaveFieldList.get(1);
                    examineHolidayVO.setFormValueStartOriginal(startTime);
                    examineHolidayVO.setFormValueFinishOriginal(endTime);
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
                        endTime = endTime.replace(ExamineFormCompEnum.AFTERNOON.getValue(),"20:00:00");
                    } else {
                        endTime = endTime + " " + "20:00:00";
                    }
                    examineHolidayVO.setFormValueDays(LocalDateTimeUtils.getDifferDateTime(startTime,endTime));
                    LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    LocalDateTime endDateTime = LocalDateTime.parse(endTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    examineHolidayVO.setFormValueStart(LocalDateTimeUtils.getMilliByTime(startDateTime));
                    examineHolidayVO.setFormValueFinish(LocalDateTimeUtils.getMilliByTime(endDateTime));
                }
            }

            if (ExamineFormCompEnum.REASON.getValue().equals(formComponentValueVo.getName())) {
                examineHolidayVO.setFormValueReason(formComponentValueVo.getValue());
            }
        });
    }


    /**
     * 查询公休列表
     *
     * @param requestEntity 前端请求体的封装实体类
     * @return 返回公休列表，结果分页，排序
     */
    @Override
    public List<ExamineHolidayDTO> getExamineHolidayList(RequestEntity requestEntity) {

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
        List<ExamineHolidayDTO> list = examineHolidayMapper.getExamineHolidayList(requestEntity);
        //使用工具类,将部门id转换为部门name
        if (CollectionUtils.isNotEmpty(list)) {
            QueryCommonUtils.setDeptNameAll(list, deptSet);
            for (ExamineHolidayDTO examineHolidayDTO : list) {
                //将数据库中的类型英文字段获取枚举类中对应的value
                String type = HolidayCountTypeEnum.getvalueBykey(examineHolidayDTO.getType());
                examineHolidayDTO.setType(type);
            }
        }
        return list;
    }

    /**
     * 查询公休详情记录
     *
     * @param id 公休记录id
     * @return 返回一条公休记录
     */
    @Override
    public ExamineHolidayDetailDTO getExamineHolidayDetail(String id) {
        ExamineHolidayDetailDTO holidayDetail = examineHolidayMapper.getHolidayDetail(id);
        if (holidayDetail != null) {
            //将数据库中的类型英文字段获取枚举类中对应的value
            String type = HolidayCountTypeEnum.getvalueBykey(holidayDetail.getType());
            holidayDetail.setType(type);
            String deptIds = holidayDetail.getDeptName();
            if (deptIds.contains(",")) {
                StringBuilder builder = new StringBuilder();
                String[] split = deptIds.split(",");
                for (String s : split) {
                    if(builder.length()==0){
                        builder.append(redisUtil.hmget(s).get("name"));
                    }else{
                        builder.append(","+redisUtil.hmget(s).get("name"));
                    }
                }
                holidayDetail.setDeptName(builder + "");
                return holidayDetail;
            }
            holidayDetail.setDeptName((String) redisUtil.hmget(deptIds).get("name"));
            return holidayDetail;
        }
        return null;
    }

    @Override
    public List<ExamineHoliday> examineHolidayListByInstanceId(String instanceId) {
        QueryWrapper<ExamineHoliday> examineHolidayQueryWrapper = new QueryWrapper<>();
        examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getTaskInstanceId,instanceId);
        examineHolidayQueryWrapper.lambda().eq(ExamineHoliday::getDeleted,0);
        List<ExamineHoliday> examineHolidayList = list(examineHolidayQueryWrapper);
        return examineHolidayList;
    }

    @Override
    public void examineHolidayDataDelete(List<ExamineHoliday> examineHolidayList) {
        List<String> ids = new ArrayList<>();
        //删除休假审批数据
        examineHolidayList.stream().forEach(examineHoliday -> {
            ids.add(examineHoliday.getId());
            examineHoliday.setDeleted(1);
            updateById(examineHoliday);
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
    public void pullHolidayExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage()+" taskInstanceId="+taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.HOLIDAY.getKey())
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
        List<ExamineHoliday> examineHolidayList = examineHolidayListByInstanceId(taskInstanceId);
        if(CollectionUtils.isNotEmpty(examineHolidayList)){
            //TODO process_instance_id  + taskInstanceId 把记录对应的部门集合存入缓存
            if(!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
                List<DataMap> actionRecordLists = examineHolidayMapper.holidayRecordDeptIdList(companyId,taskInstanceId);
                examineActionService.recordOriginDeptIdsSaveRedis(actionRecordLists);
            }

            //删除休假审批数据
            examineHolidayDataDelete(examineHolidayList);
        }
        //钉钉抓取休假数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(), 7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取休假数据
            dealExamineData(taskInstanceId, companyId, examine.getCode(), intervalTimeStart, intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

}
