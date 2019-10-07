package com.sancai.oa.quartz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskInstanceDTO;
import com.sancai.oa.quartz.entity.TaskInstanceTime;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.exception.EnumTaskTemplateError;
import com.sancai.oa.quartz.mapper.TaskInstanceMapper;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import com.sancai.oa.quartz.util.SpringContextUtil;
import com.sancai.oa.typestatus.enums.TimedTaskKeyEnum;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.typestatus.enums.TimedTaskTypeEnum;
import com.sancai.oa.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 任务实例 服务实现类
 * </p>
 *
 * @author wangyl
 * @since 2019-07-31
 */
@Service
@Slf4j
public class TaskInstanceServiceImpl extends ServiceImpl<TaskInstanceMapper, TaskInstance> implements ITaskInstanceService {


    @Autowired
    private  TaskInstanceMapper taskInstanceMapper;
    @Autowired
    private ITaskTemplateService taskTemplateService;
    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Override
    public List<Map> getTaskInstanceList(TaskInstanceDTO taskInstanceDTO) {
        int pages = Integer.valueOf(taskInstanceDTO.getPage());
        int capacity = Integer.valueOf(taskInstanceDTO.getCapacity());
        //每页的大小为capacity，查询第page页的结果
        PageHelper.startPage(pages, capacity);
        List<Map> res = taskInstanceMapper.getTaskInstanceList(taskInstanceDTO);
        for(Map m:res){
            String status = m.get("status").toString();
            String statusDecode = TimedTaskStatusEnum.getvalueBykey(status);
            m.put("status",statusDecode);
            String taskType = m.get("task_type").toString();
            String taskKey = m.get("task_key").toString();

            m.put("task_type", TimedTaskTypeEnum.getvalueBykey(taskType));
            m.put("task_key", TimedTaskKeyEnum.getvalueBykey(taskKey));
            String success = m.get("success_time").toString();
            if ("0".equals(success)){
                m.put("success_time",null);
            }
            String start = m.get("start_time").toString();
            if ("0".equals(start)){
                m.put("start_time",null);
            }
            String end = m.get("end_time").toString();
            if ("0".equals(end)){
                m.put("end_time",null);
            }

            String last= m.get("last_excute_time").toString();
            if ("0".equals(last)){
                m.put("last_excute_time",null);
            }

            String create= m.get("create_time").toString();
            if ("0".equals(create)){
                m.put("create_time",null);
            }
        }
        return res;
    }

    @Override
    public Map getTaskInstanceDetail(String id) {
        Map m =taskInstanceMapper.getTaskInstanceDetail(id);
        String status = m.get("status").toString();
        String statusDecode = TimedTaskStatusEnum.getvalueBykey(status);
        m.put("status",statusDecode);
        String success = m.get("success_time").toString();
        if ("0".equals(success)){
            m.put("success_time",null);
        }
        String taskType = m.get("task_type").toString();
        String taskKey = m.get("task_key").toString();

        m.put("task_type", TimedTaskTypeEnum.getvalueBykey(taskType));
        m.put("task_key", TimedTaskKeyEnum.getvalueBykey(taskKey));
        String manualRetryTime = m.get("manual_retry_time").toString();
        // 执行中、重试中的不能再重试 其他状态可以
        if(TimedTaskStatusEnum.EXECUTING.getKey().equals(status)||TimedTaskStatusEnum.RETRYING.getKey().equals(status)){
            m.put("manual_retry", "N");
        }else{
            m.put("manual_retry", "Y");
        }


        return m;
    }

    @Override
    public TaskInstance getLastSuccessTaskInstance(String templateId, String companyId) {
        return taskInstanceMapper.getLastSuccessTaskInstance(templateId,companyId);
    }


    @Override
    public TaskInstance getScheduleTimeLastSuccessTaskInstance(String templateId, String companyId, long createTime) {
        return taskInstanceMapper.getScheduleTimeLastSuccessTaskInstance(templateId,companyId,createTime);
    }

    /**
     * 重试接口
     * @param id 定时任务id
     * @return
     */
    @Override

    public void taskRetry(String id) {

        log.info("retry is running ！！！");
        TaskInstance tTaskInstance = taskInstanceMapper.selectById(id);

        //使用CompletableFuture 调用执行方法

        // 执行重试
        tTaskInstance.setStatus(TimedTaskStatusEnum.RETRYING.getKey());
        tTaskInstance.setManualRetryTime(tTaskInstance.getManualRetryTime()+1);
        tTaskInstance.setLastExcuteTime(System.currentTimeMillis());

        taskInstanceService.updateById(tTaskInstance);

        try {
            tInstance(tTaskInstance, 0);
        } catch (Exception e) {
            log.error( "公司："+tTaskInstance.getCompanyId() +",taskTemplateId:" + tTaskInstance.getTaskTemplateId() + "实例名称: " +tTaskInstance.getTaskName() +",execute is not retry well done ！！！");
        }

    }

    /**
     * 定时任务预重试接口
     * @param id 定时任务id
     * @return  false 不需要执行重试，true 需要执行重试
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean taskPreRetry(String id) {

        Map res = new HashMap();
        log.info("retry is running ！！！");
        TaskInstance tTaskInstance = new TaskInstance();
        //初始化设值为false
        tTaskInstance = taskInstanceMapper.selectById(id);
        //使用CompletableFuture 调用执行方法
        // 重试中或者执行中不能在执行重试方法
        if(tTaskInstance.getStatus().equals(TimedTaskStatusEnum.RETRYING.getKey())||tTaskInstance.getStatus().equals(TimedTaskStatusEnum.EXECUTING.getKey())){
            //重试执行中状态
            return false;
        }else {
            // 未执行，则触发重试
            tTaskInstance.setStatus(TimedTaskStatusEnum.RETRYING.getKey());
            taskInstanceService.updateById(tTaskInstance);
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {Exception.class, RuntimeException.class})
    public TaskInstanceTime resetStartAndEndTime(TaskInstance taskInstance) {
        //默认是从昨天0点0分0秒开始,23点59分59秒结束
        LocalDateTime startDay = LocalDateTime.now().plusDays(-1);
        LocalDateTime endDay = startDay;
        long start = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(startDay));
        long end = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayEnd(endDay));
        if(TimedTaskStatusEnum.SUCCESS.getKey().equals(taskInstance.getStatus()) ||
                TimedTaskStatusEnum.FAILURE.getKey().equals(taskInstance.getStatus()) ||
                TimedTaskStatusEnum.RETRYING.getKey().equals(taskInstance.getStatus())){
            if(null!=taskInstance.getStartTime()){
                start = taskInstance.getStartTime();
            }
            if(null!=taskInstance.getEndTime()){
                end = taskInstance.getEndTime();
            }
        }else if(TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
            taskInstance.setStartTime(start);
            taskInstance.setEndTime(end);
            updateById(taskInstance);
        }
        TaskInstanceTime taskInstanceTime = new TaskInstanceTime();
        taskInstanceTime.setStartTime(start);
        taskInstanceTime.setEndTime(end);
        return taskInstanceTime;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {Exception.class, RuntimeException.class})
    public String resetMonth(TaskInstance taskInstance) {
        String month = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        month = lastMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        if(TimedTaskStatusEnum.SUCCESS.getKey().equals(taskInstance.getStatus()) ||
                TimedTaskStatusEnum.FAILURE.getKey().equals(taskInstance.getStatus()) ||
                TimedTaskStatusEnum.RETRYING.getKey().equals(taskInstance.getStatus())){
            LocalDateTime createDateTime = LocalDateTimeUtils.getDateTimeOfTimestamp(taskInstance.getStartTime());
            LocalDate localDate = createDateTime.toLocalDate();
            month = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }else if(TimedTaskStatusEnum.EXECUTING.getKey().equals(taskInstance.getStatus())){
            LocalDateTime firstDayOfMonth = lastMonth.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
            Long startTime = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayStart(firstDayOfMonth));
            LocalDateTime lastDayOfMonth = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay();
            Long endTime = LocalDateTimeUtils.getMilliByTime(LocalDateTimeUtils.getDayEnd(lastDayOfMonth));
            taskInstance.setStartTime(startTime);
            taskInstance.setEndTime(endTime);
            updateById(taskInstance);
        }
        return month;
    }




    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = {Exception.class, RuntimeException.class})
    public void clearQuartz() {
        taskInstanceMapper.clearDetails();
        taskInstanceMapper.clearFiredTriggers();
        taskInstanceMapper.clearCronTriggers();
        taskInstanceMapper.clearTriggers();
    }

    /**
     * 调用spring容器获取bean和具体执行方法，执行任务
     *
     * @param tTaskInstance 任务实例
     * @throws Exception
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void tInstance(TaskInstance tTaskInstance, int count) {
        // 使用定制的Executor在前一个阶段上异步应用函数
        CompletableFuture cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            if (count > 3) {
                return "success";
            }

            // 执行异常，自己获取、自己处理
            methodClass(tTaskInstance);


            log.info("success count : " + count);
            return "success";
        })
                .thenApplyAsync(s -> {
                    if (count > 3) {
                        return "success";
                    }


                    tTaskInstance.setRetryTime(count);
                    tTaskInstance.setModifyTime(System.currentTimeMillis());
                    tTaskInstance.setStatus(TimedTaskStatusEnum.SUCCESS.getKey());
                    tTaskInstance.setSuccessTime(System.currentTimeMillis());
                    // 自动重试后成功，需要清空失败原因
                    tTaskInstance.setFailReason(" ");
                    if(tTaskInstance.getManualRetryTime()==null||tTaskInstance.getManualRetryTime()==0){
                        tTaskInstance.setLastExcuteTime(System.currentTimeMillis());
                    }


                    log.info("successInstance: " + tTaskInstance);
                    taskInstanceService.updateById(tTaskInstance);

                    return "success";
                })
                .exceptionally(ex -> {
                    //失败后执行逻辑
                    // 记录失败次数以及原因
                    if (count > 3) {
                        return "failed";
                    }
                    TaskInstance tTaskInstanceTmp = taskInstanceService.getById(tTaskInstance.getId());

                    tTaskInstance.setStatus(TimedTaskStatusEnum.FAILURE.getKey());
                    tTaskInstance.setRetryTime(count);
                    tTaskInstance.setFailReason(ex.getMessage());
                    String totalFailReason = "";
                    taskInstanceService.getById(tTaskInstance.getId());
                    StringBuffer failReason = new StringBuffer();
                    failReason.append("第"+count+"次失败原因:"+ex.getMessage()+System.getProperty("line.separator"));

                    failReason.append("第"+count+"次失败详情:");
                    StackTraceElement[] stackTraceElements = ex.getCause().getStackTrace();
                    for (StackTraceElement se:stackTraceElements){
                        failReason.append(se.getClassName())
                                .append("→")
                                .append(se.getFileName())
                                .append("→")
                                .append(se.getMethodName())
                                .append("→")
                                .append(se.getLineNumber()+System.getProperty("line.separator"))
                        ;
                    }

                    if(count==0){
                        totalFailReason = failReason.toString();
                    }else{
                        totalFailReason = tTaskInstanceTmp.getFailReason() + failReason.toString();
                    }
                    tTaskInstance.setFailReason(totalFailReason);
                    if (count > 0) {
                        tTaskInstance.setModifyTime(System.currentTimeMillis());
                    }
                    if(tTaskInstance.getManualRetryTime()==null||tTaskInstance.getManualRetryTime()==0){
                        tTaskInstance.setLastExcuteTime(System.currentTimeMillis());
                    }

                    log.error(" We have an exception - " + ex.getMessage());
                    log.error("failed count : " + count);

                    taskInstanceService.updateById(tTaskInstance);
                    //失败重试
                    tInstance(tTaskInstance, count + 1);

                    return "failed!";
                });
        if(null!=cf.getNow(null)){
            log.info("retry is well done : " );
        }
    }

    /**
     * 调用spring容器获取bean和具体执行方法，执行任务
     *
     * @param tTaskInstance 任务实例
     * @throws Exception
     */

    public void methodClass(TaskInstance tTaskInstance) {
        String taskTemplateId = tTaskInstance.getTaskTemplateId();
        TaskTemplate taskTemplate = taskTemplateService.getById(taskTemplateId);

        String methodName = taskTemplate.getMethodstr();

        String classStr = taskTemplate.getClassstr();
        String companyId = tTaskInstance.getCompanyId();

        Object object = SpringContextUtil.getBean(classStr);
        Method method = ReflectionUtils.findMethod(object.getClass(), methodName, String.class, String.class);

        Object o = ReflectionUtils.invokeMethod(method, object,  companyId,tTaskInstance.getId());


    }

    @Override
    public TaskInstance saveTask(String companyId, String key,TaskTemplate taskTemplate,Company company, TimedTaskStatusEnum status,Long startTime,Long endTime){
        TaskInstance tTaskInstance = new TaskInstance();
        String taskInstanceId = UUIDS.getID();
        tTaskInstance.setDeleted(0);
        tTaskInstance.setTaskTemplateId(taskTemplate.getId());
        tTaskInstance.setId(taskInstanceId);
        tTaskInstance.setTaskName(company.getName()+"-"+taskTemplate.getName());
        tTaskInstance.setCreateTime(System.currentTimeMillis());
        tTaskInstance.setCompanyId(companyId);
        tTaskInstance.setType(key);
        tTaskInstance.setRetryTime(0);
        tTaskInstance.setManualRetryTime(0);
        tTaskInstance.setStartTime(startTime);
        tTaskInstance.setEndTime(endTime);
        //状态
        tTaskInstance.setStatus(status.getKey());
        taskInstanceService.save(tTaskInstance);
        return tTaskInstance;

    }
}
