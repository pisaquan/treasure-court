package com.sancai.oa.examine.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiAttendanceGetusergroupResponse;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceCheckType;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.examine.DingDingExamineService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.entity.*;
import com.sancai.oa.examine.entity.enums.ExamineFormCompEnum;
import com.sancai.oa.examine.entity.enums.ExamineStatusEnum;
import com.sancai.oa.examine.entity.enums.ExamineTypeEnum;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;
import com.sancai.oa.examine.exception.EnumExamineError;
import com.sancai.oa.examine.exception.ExamineException;
import com.sancai.oa.examine.mapper.ExamineLeaveMapper;
import com.sancai.oa.examine.service.IExamineActionService;
import com.sancai.oa.examine.service.IExamineDepartmentService;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.examine.service.IExamineService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.typestatus.enums.LeaveCountTypeEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.user.service.IUserService;

import com.sancai.oa.utils.FileUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.sancai.oa.utils.OaMapUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * <p>
 * 请假 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Slf4j
@Service
public class ExamineLeaveServiceImpl extends ServiceImpl<ExamineLeaveMapper, ExamineLeave> implements IExamineLeaveService {

    @Autowired
    DingDingExamineService dingDingExamineService;

    @Autowired
    IExamineDepartmentService examineDepartmentService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ExamineLeaveMapper examineLeaveMapper;

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


    @Value("${filePath.zipStorePath}")
    private String zipStorePath;

    @Override
    public void dealExamineData(String taskInstanceId, String companyId, String processCode, long intervalTimeStart, long intervalTimeEnd) {
        TaskMessage.addMessage(taskInstanceId, "请假抓取  companyId：" + companyId);
        TaskMessage.addMessage(taskInstanceId, "模板code:" + processCode);
        TaskMessage.addMessage(taskInstanceId, "抓取时间范围: " + LocalDateTimeUtils.formatDateTime(intervalTimeStart) + " 到 " + LocalDateTimeUtils.formatDateTime(intervalTimeEnd));
        int totalCount = 0;
        Long nextCursor = 0L;
        while (nextCursor != null) {
            List<ExamineLeave> examineEntityList = new ArrayList<>();
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
                ExamineLeave examineLeaveVO = new ExamineLeave();

                examineLeaveVO.setId(UUIDS.getID());
                examineLeaveVO.setUserId(instanceVo.getOriginatorUserid());
                examineLeaveVO.setCompanyId(examineInstanceDTO.getCompanyId());
                examineLeaveVO.setProcessCode(examineInstanceDTO.getProcessCode());

                List<Integer> departMentList = new ArrayList<>();
                //查表获取用户信息
                UserDTO user = userService.getUserByUserId(examineLeaveVO.getUserId(), companyId);
                if (user != null) {
                    examineLeaveVO.setUserName(user.getName().trim());
                    List<UserDepartment> userDepartments = user.getUserDepartments();
                    if (CollectionUtils.isNotEmpty(userDepartments)) {
                        departMentList = userDepartments.stream().map(UserDepartment::getDeptId).collect(Collectors.toList()).stream().map(Integer::parseInt).collect(Collectors.toList());
                        //TODO 根据ProcessInstanceId + taskInstanceId 缓存查历史部门集合，deptList不为空重新赋值，为空按用户当前的部门插入
                        List<Integer> departList = examineActionService.getRecordOriginDeptIdsSaveRedis(examineInstanceDTO.getProcessInstanceId(), taskInstanceId, null);
                        if (departList != null && departList.size() > 0) {
                            departMentList = departList;
                        }
                    }
                } else {
                    examineLeaveVO.setUserName(" ");
                }
                departMentList.stream().forEach(departId -> {
                    ExamineDepartment examineDepartmentVO = new ExamineDepartment();
                    examineDepartmentVO.setId(UUIDS.getID());
                    examineDepartmentVO.setExamineInstanceId(examineLeaveVO.getId());
                    examineDepartmentVO.setDeptId(departId);
                    examineDepartmentVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                    examineDepartmentVO.setDeleted(0);
                    examineDepartmentList.add(examineDepartmentVO);
                });

                examineLeaveVO.setProcessInstanceId(examineInstanceDTO.getProcessInstanceId());
                examineLeaveVO.setProcessTitle(instanceVo.getTitle());
                examineLeaveVO.setProcessCreateTime(instanceVo.getCreateTime().getTime());
                if (instanceVo.getFinishTime() != null) {
                    examineLeaveVO.setProcessFinishTime(instanceVo.getFinishTime().getTime());
                }
                examineLeaveVO.setProcessStatus(instanceVo.getStatus());
                examineLeaveVO.setProcessResult(instanceVo.getResult());
                leaveFormValue(examineLeaveVO, instanceVo.getFormComponentValues());
                examineLeaveVO.setFormValueCompany(examineInstanceDTO.getCompany());
                examineLeaveVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                examineLeaveVO.setDeleted(0);
                examineLeaveVO.setTaskInstanceId(taskInstanceId);
                examineLeaveVO.setSendNotifyStatus(0);
                examineEntityList.add(examineLeaveVO);

            });

            if (CollectionUtils.isNotEmpty(examineEntityList)) {
                for (ExamineLeave examineLeave : examineEntityList) {
                    save(examineLeave);
                }
                log.info("分页拉取钉钉请假数据成功");
            }

            TaskMessage.addMessage(taskInstanceId, "抓取了:" + examineEntityList.size() + "条请假数据");
            totalCount += examineEntityList.size();

            if (CollectionUtils.isNotEmpty(examineDepartmentList)) {
                for (ExamineDepartment examineDepartment : examineDepartmentList) {
                    examineDepartmentService.save(examineDepartment);
                }
                log.info("分页插入审批实例和部门对应关系表成功");
            }
        }
        log.info("分期拉取钉钉请假数据成功");
        TaskMessage.addMessage(taskInstanceId, "拉取钉钉请假数据成功，共抓取" + totalCount + "条");
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class})
    public void updateExamineData(String taskInstanceId) {
        Long nowTime = LocalDateTimeUtils.getMilliByTime(LocalDateTime.now());
        LocalDateTime lastMonthFirstDay = LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        Long lastMonth = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(lastMonthFirstDay));
        List<ExamineLeave> examineLeaveList = list(new QueryWrapper<ExamineLeave>()
                .lambda().ne(ExamineLeave::getProcessStatus, "COMPLETED")
                .and(u -> u.ne(ExamineLeave::getProcessStatus, "TERMINATED"))
                .and(u -> u.eq(ExamineLeave::getDeleted, 0))
                .and(u -> u.between(ExamineLeave::getProcessCreateTime, lastMonth, nowTime)));
        examineLeaveList.stream().forEach(examineLeave -> {
            String processInstanceId = examineLeave.getProcessInstanceId();
            String companyId = examineLeave.getCompanyId();
            OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = dingDingExamineService.examineInstanceGetById(processInstanceId, companyId);
            if (processInstanceTopVo == null) {
                return;
            }
            if (ExamineStatusEnum.RUNNING.getKey().equals(processInstanceTopVo.getStatus())) {
                return;
            }
            if (processInstanceTopVo.getFinishTime() != null) {
                examineLeave.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
            }
            examineLeave.setProcessStatus(processInstanceTopVo.getStatus());
            examineLeave.setProcessResult(processInstanceTopVo.getResult());
            examineLeave.setModifyTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        });
        TaskMessage.addMessage(taskInstanceId, "上个月开始到现在，共" + examineLeaveList.size() + "条请假数据需要更新");
        if (CollectionUtils.isNotEmpty(examineLeaveList)) {
            examineLeaveList = examineLeaveList.stream().filter(e ->
                    !ExamineStatusEnum.RUNNING.getKey().equals(e.getProcessStatus())).collect(Collectors.toList());
            TaskMessage.addMessage(taskInstanceId, "状态已变更，可以更新的请假数据共" + examineLeaveList.size() + "条");
            if (CollectionUtils.isNotEmpty(examineLeaveList)) {
                for (ExamineLeave examineLeave : examineLeaveList) {
                    updateById(examineLeave);
                }
                log.info("更新请假审批数据成功");
            }
            TaskMessage.addMessage(taskInstanceId, "更新请假数据成功，更新数据共" + examineLeaveList.size() + "条");
        }
        TaskMessage.finishMessage(taskInstanceId);
    }


    /**
     * 处理钉钉返回的表单数据 到 自定义
     *
     * @param examineLeaveVO
     * @param formComponentValueVoList
     */
    public void leaveFormValue(ExamineLeave examineLeaveVO, List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList) {
        if (CollectionUtils.isEmpty(formComponentValueVoList)) {
            return;
        }
        formComponentValueVoList.stream().forEach(formComponentValueVo -> {

            if (ExamineFormCompEnum.SALARY.getValue().equals(formComponentValueVo.getName())) {
                examineLeaveVO.setFormValueSalary(LeaveTypeEnum.getKeyByValue(formComponentValueVo.getValue()));
            }
            if (ExamineFormCompEnum.TIMERANGE.getValue().equals(formComponentValueVo.getName()) &&
                    ExamineFormCompEnum.DDHOLIDAYFIELD.getKey().equals(formComponentValueVo.getComponentType())) {
                String value = formComponentValueVo.getValue();
                List<String> leaveFieldList = JSONArray.parseArray(value, String.class);
                if (leaveFieldList.size() == Integer.parseInt(ExamineFormCompEnum.DDHOLIDAYFIELDLENGTH.getKey())) {
                    examineLeaveVO.setFormValueType(LeaveTypeEnum.getKeyByValue(leaveFieldList.get(4)));
                    String startTime = leaveFieldList.get(0);
                    String endTime = leaveFieldList.get(1);
                    examineLeaveVO.setFormValueStartOriginal(startTime);
                    examineLeaveVO.setFormValueFinishOriginal(endTime);
                    if (startTime.endsWith(ExamineFormCompEnum.MORNING.getValue())) {
                        startTime = startTime.replace(ExamineFormCompEnum.MORNING.getValue(), "06:00:00");
                    } else if (startTime.endsWith(ExamineFormCompEnum.AFTERNOON.getValue())) {
                        startTime = startTime.replace(ExamineFormCompEnum.AFTERNOON.getValue(), "13:00:00");
                    } else if (startTime.length() >= 16) {
                        LocalDateTime startDateTime = LocalDateTime.parse(startTime + ":00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                        LocalDateTime baseDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.parse("12:00:00"));
                        if (startDateTime.isBefore(baseDateTime)) {
                            startTime = startTime.substring(0, startTime.indexOf(" ")) + " " + "06:00:00";
                        } else {
                            startTime = startTime.substring(0, startTime.indexOf(" ")) + " " + "13:00:00";
                        }
                    } else {
                        startTime = startTime + " " + "06:00:00";
                    }

                    if (endTime.endsWith(ExamineFormCompEnum.MORNING.getValue())) {
                        endTime = endTime.replace(ExamineFormCompEnum.MORNING.getValue(), "12:30:00");
                    } else if (endTime.endsWith(ExamineFormCompEnum.AFTERNOON.getValue())) {
                        endTime = endTime.replace(ExamineFormCompEnum.AFTERNOON.getValue(), "20:00:00");
                    } else if (endTime.length() >= 16) { // 年月日 时分
                        LocalDateTime endDateTime = LocalDateTime.parse(endTime + ":00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                        LocalDateTime baseDateTime = LocalDateTime.of(endDateTime.toLocalDate(), LocalTime.parse("17:00:00"));
                        if (endDateTime.isBefore(baseDateTime)) {
                            endTime = endTime.substring(0, endTime.indexOf(" ")) + " " + "12:30:00";
                        } else {
                            endTime = endTime.substring(0, endTime.indexOf(" ")) + " " + "20:00:00";
                        }
                    } else {
                        endTime = endTime + " " + "20:00:00";
                    }

                    examineLeaveVO.setFormValueDays(LocalDateTimeUtils.getDifferDateTime(startTime, endTime));
                    LocalDateTime startDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    LocalDateTime endDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm"));
                    examineLeaveVO.setFormValueStart(LocalDateTimeUtils.getMilliByTime(startDateTime));
                    examineLeaveVO.setFormValueFinish(LocalDateTimeUtils.getMilliByTime(endDateTime));
                }
            }

            if (ExamineFormCompEnum.REASON.getValue().equals(formComponentValueVo.getName())) {
                examineLeaveVO.setFormValueReason(formComponentValueVo.getValue());
            }
        });

    }

    /**
     * 请假列表查询
     *
     * @param requestEntity 封装请求体的实体类
     * @return 返回请假列表分页结果，排序
     */
    @Override
    public List<ExamineLeaveDTO> getExamineLeaveList(RequestEntity requestEntity) {

        //从redis中取出companyId对应的部门列表
        Set deptSet = redisUtil.sGet(requestEntity.getCompanyId());
        //部门查询条件存在时，查询部门及其子部门的所有信息
        String deptId = requestEntity.getDeptId();
        if (StringUtils.isNotBlank(deptId)) {
            List<Long> deptList = departmentService.listSubDepartment(requestEntity.getCompanyId(), requestEntity.getDeptId());
            requestEntity.setDeptList(deptList);
        }
        //分页查询，每页的容量为capacity，查询第page页
        PageHelper.startPage(requestEntity.getPage(), requestEntity.getCapacity());
        List<ExamineLeaveDTO> list = examineLeaveMapper.getExamineLeaveList(requestEntity);
        //调用工具类,将部门id转换为部门name
        if (CollectionUtils.isNotEmpty(list)) {
            QueryCommonUtils.setDeptNameAll(list, deptSet);
            for (ExamineLeaveDTO examineLeaveDTO : list) {
                //将类型转换为中文
                String type = LeaveCountTypeEnum.getvalueBykey(examineLeaveDTO.getType());
                examineLeaveDTO.setType(type);
            }
        }
        return list;
    }

    /**
     * 请假详情查询
     *
     * @param id 请假记录id
     * @return 返回请假记录详情
     */
    @Override
    public ExamineLeaveDetailDTO getExamineLeaveDetail(String id) {
        ExamineLeaveDetailDTO examineLeaveDetail = examineLeaveMapper.getExamineLeaveDetail(id);
        if (examineLeaveDetail != null) {
            //通过枚举将数据库中类型英文转换为中文
            String type = LeaveCountTypeEnum.getvalueBykey(examineLeaveDetail.getType());
            examineLeaveDetail.setType(type);
            //将部门Id转换成部门名称
            String deptIds = examineLeaveDetail.getDeptName();
            if (deptIds.contains(",")) {
                StringBuilder builder = new StringBuilder();
                for (String s : deptIds.split(",")) {
                    if (builder.length() == 0) {
                        builder.append(redisUtil.hmget(s).get("name"));
                    } else {
                        builder.append("," + redisUtil.hmget(s).get("name"));
                    }
                }
                examineLeaveDetail.setDeptName(builder + "");
                return examineLeaveDetail;
            }
            examineLeaveDetail.setDeptName((String) redisUtil.hmget(deptIds).get("name"));
            return examineLeaveDetail;
        }
        return null;
    }

    /**
     * 根据任务实例id查询请假数据
     *
     * @param instanceId
     * @return
     */
    @Override
    public List<ExamineLeave> examineLeaveListByInstanceId(String instanceId) {
        QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getTaskInstanceId, instanceId);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
        List<ExamineLeave> examineLeaveList = list(examineLeaveQueryWrapper);
        return examineLeaveList;
    }

    /**
     * 删除请假审批数据
     *
     * @param examineLeaveList
     */
    @Override
    public void examineLeaveDataDelete(List<ExamineLeave> examineLeaveList) {
        List<String> ids = new ArrayList<>();
        //删除请假审批数据
        examineLeaveList.stream().forEach(examineLeave -> {
            examineLeave.setDeleted(1);
            ids.add(examineLeave.getId());
            updateById(examineLeave);
        });
        //删除对应部门关系表
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        ExamineDepartment examineDepartment = new ExamineDepartment();
        examineDepartment.setDeleted(1);
        UpdateWrapper<ExamineDepartment> examineDepartmentUpdateWrapper = new UpdateWrapper<>();
        examineDepartmentUpdateWrapper.lambda().in(ExamineDepartment::getExamineInstanceId, ids);
        examineDepartmentService.update(examineDepartment, examineDepartmentUpdateWrapper);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {Exception.class, RuntimeException.class})
    public void pullLeaveExamineData(String companyId, String taskInstanceId) {
        TaskInstance taskInstance = taskInstanceService.getById(taskInstanceId);
        if (taskInstance == null) {
            log.error(EnumExamineError.TASK_INSTANCE_IS_EMPTY.getMessage() + " taskInstanceId=" + taskInstanceId);
            throw new ExamineException(EnumExamineError.TASK_INSTANCE_IS_EMPTY);
        }
        TaskInstanceTime taskInstanceTime = taskInstanceService.resetStartAndEndTime(taskInstance);
        Examine examine = examineService.getOne(new QueryWrapper<Examine>()
                .lambda().eq(Examine::getExamineGroup, ExamineTypeEnum.LEAVE.getKey())
                .and(e -> e.eq(Examine::getCompanyId, companyId)
                        .and(t -> t.eq(Examine::getDeleted, 0))), true);
        if (examine == null) {
            log.error(EnumExamineError.EXAMINE_IS_EMPTY.getMessage() + " companyId=" + companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_IS_EMPTY);
        }
        if (StringUtils.isBlank(examine.getCode())) {
            log.error(EnumExamineError.EXAMINE_CODE_IS_EMPTY.getMessage() + " companyId=" + companyId);
            throw new ExamineException(EnumExamineError.EXAMINE_CODE_IS_EMPTY);
        }
        //如果根据实例id查到值,根据实例id删除之前的已经保存的数据 , 重新获取抓取开始时间和结束时间
        List<ExamineLeave> examineLeaveList = examineLeaveListByInstanceId(taskInstanceId);
        if (CollectionUtils.isNotEmpty(examineLeaveList)) {
            //TODO 根据companyId  + taskInstanceId 把记录对应的部门集合存入缓存
            if (!TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())) {
                List<DataMap> actionRecordLists = examineLeaveMapper.leaveRecordDeptIdList(companyId, taskInstanceId);
                examineActionService.recordOriginDeptIdsSaveRedis(actionRecordLists);
            }

            //删除请假审批数据
            examineLeaveDataDelete(examineLeaveList);
        }
        //钉钉抓取请假数据，最多抓取7天的，如果超过7天，需要拆分为多段
        List<Pair> intervalTimes = LocalDateTimeUtils.getIntervalTimes(taskInstanceTime.getStartTime(),
                taskInstanceTime.getEndTime(), 7);
        for (Pair intervalTime : intervalTimes) {
            long intervalTimeStart = (long) intervalTime.getKey();
            long intervalTimeEnd = (long) intervalTime.getValue();
            //抓取请假数据
            dealExamineData(taskInstanceId, companyId, examine.getCode(), intervalTimeStart, intervalTimeEnd);
        }
        TaskMessage.finishMessage(taskInstanceId);
    }

    /**
     * 字符串集合进行拼接返回拼接字符串
     *
     * @param stringList
     * @return
     */
    private String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append("&");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }


    /**
     * 员工上传病例证明信息
     *
     * @param multipartFile
     * @param leaveId
     * @param userId
     */
    @Override
    public void uploadsMedicalCertificate(List<MultipartFile> multipartFile, String leaveId, String userId) throws Exception {

        if (multipartFile.size() > 3) {
            throw new ExamineException(EnumExamineError.UPLOADS_MEDICAL_CERTIFICATE_FILE_MAX);
        }

        QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getId, leaveId);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
        ExamineLeave examineLeave = examineLeaveMapper.selectOne(examineLeaveQueryWrapper);
        if (examineLeave == null) {
            throw new ExamineException(EnumExamineError.QUERY_LEAVE_DETAIL_IS_EMPTY);
        }
        if (StringUtils.isNotBlank(examineLeave.getCaseReportUrl())) {
            throw new ExamineException(EnumExamineError.UPLOADS_MEDICAL_CERTIFICATE_FILE_EXIST);
        }
        /*
        if(!examineLeave.getUserId().equals(userId)){
            throw new ExamineException(EnumExamineError.UPLOADS_MEDICAL_CERTIFICATE_USER_ERROR);
        }
        UserDTO user = userService.getUserByUserId(userId,examineLeave.getCompanyId());
        if(user == null){
            throw new ExamineException(EnumExamineError.USER_IS_EMPTY);
        }*/
        //上传图片
        List<String> li = new ArrayList<>();
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
        //生成zip的路径
        String path = zipStorePath + "img";
        String pic1Str;
        String picUrl;
        for (int i = 0; i < multipartFile.size(); i++) {
            String suffix = multipartFile.get(i).getOriginalFilename().substring(multipartFile.get(i).getOriginalFilename().lastIndexOf("."));
            //重命名图片
            String picName = System.currentTimeMillis() + new Random().nextInt(100) + suffix;
            if (!multipartFile.get(i).isEmpty()) {

                String paths = path + File.separator + userId + File.separator + yyyyMMdd + File.separator;
                File saveFile = new File(paths);
                if (!saveFile.exists()) {
                    saveFile.mkdirs();
                }
                String url = saveFile + File.separator + picName;
                File save = new File(url);
                MultipartFile mfile = multipartFile.get(i);
                mfile.transferTo(save);
                //获取图片地址(用户id+图片名称)
                pic1Str = "img" + File.separator + userId + File.separator + yyyyMMdd + File.separator + picName;
                li.add(pic1Str);
                //赋予文件读权限，赋予上一级文件夹执行权限
                Runtime.getRuntime().exec("chmod 001 " + path + File.separator);
                Runtime.getRuntime().exec("chmod 001 " + path + File.separator + userId + File.separator);
                Runtime.getRuntime().exec("chmod 001 " + paths);
                Runtime.getRuntime().exec("chmod 004 " + url);

            }
        }
        picUrl = listToString(li);

        if (StringUtils.isBlank(picUrl)) {
            throw new ExamineException(EnumExamineError.UPLOADS_MEDICAL_CERTIFICATE_FAILURE);
        }
        examineLeave.setCaseReportUrl(picUrl);
        //2：已发送已上传
        examineLeave.setSendNotifyStatus(2);
        examineLeaveMapper.updateById(examineLeave);
    }

    /**
     * 人事审核病例证明
     *
     * @param leaveId
     */
    @Override
    public void checkMedicalCertificate(String leaveId, String status) {

        QueryWrapper<ExamineLeave> examineLeaveQueryWrapper = new QueryWrapper<>();
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getId, leaveId);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getDeleted, 0);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getSendNotifyStatus, 2);
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getFormValueSalary, LeaveTypeEnum.PAID.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getFormValueType, LeaveTypeEnum.SICKLEAVE.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessResult, ExamineStatusEnum.AGREE.getKey());
        examineLeaveQueryWrapper.lambda().eq(ExamineLeave::getProcessStatus, ExamineStatusEnum.COMPLETED.getKey());
        examineLeaveQueryWrapper.lambda().isNull(ExamineLeave::getCaseReportStatus);
        ExamineLeave examineLeave = examineLeaveMapper.selectOne(examineLeaveQueryWrapper);
        if (examineLeave == null) {
            throw new ExamineException(EnumExamineError.QUERY_LEAVE_DETAIL_IS_EMPTY);
        }
        if (status.equals(ExamineStatusEnum.AGREE.getKey())) {
            examineLeave.setCaseReportStatus(ExamineStatusEnum.AGREE.getKey());
        } else {
            examineLeave.setCaseReportStatus(ExamineStatusEnum.REFUSE.getKey());
        }
        examineLeaveMapper.updateById(examineLeave);
    }

    /**
     * 根据请假记录id获取详情记录病例图片
     *
     * @param id 请假记录id
     * @return 返回请假记录详情
     */
    @Override
    public ExamineLeaveDetailDTO getExamineLeaveDetailImg(String id) {
        ExamineLeaveDetailDTO examineLeaveDetail = examineLeaveMapper.getExamineLeaveDetailImg(id);
        if (examineLeaveDetail != null) {
            List<String> picList = new ArrayList<>();
            examineLeaveDetail.setCaseReportUrlList(picList);
            String imagePath = examineLeaveDetail.getCaseReportUrl();
            if (StringUtils.isNotBlank(imagePath)) {
                picList = Arrays.asList(imagePath.split("&")).stream().map(s -> (s.trim())).collect(Collectors.toList());
                examineLeaveDetail.setCaseReportUrlList(picList);
            }
            return examineLeaveDetail;
        }
        return null;
    }


}
