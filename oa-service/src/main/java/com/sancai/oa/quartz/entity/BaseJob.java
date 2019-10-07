package com.sancai.oa.quartz.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.dingding.notify.DingDingNotifyService;
import com.sancai.oa.dingding.notify.OANotifyDTO;
import com.sancai.oa.dingding.notify.TextNotifyDTO;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.quartz.service.ITaskInstanceService;

import com.sancai.oa.quartz.service.ITaskTemplateService;
import com.sancai.oa.quartz.util.SpringContextUtil;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import com.sancai.oa.utils.DateCalUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author wangyl
 * @date 2019/7/30 13:08
 */
@Slf4j
public class BaseJob implements Job, Serializable {


    private static final long serialVersionUID = 1L;
    private static final String JOB_MAP_KEY = "self";

    public static final String STATUS_RUNNING = "1";
    public static final String STATUS_NOT_RUNNING = "0";
    public static final String CONCURRENT_IS = "1";
    public static final String CONCURRENT_NOT = "0";

    @Autowired
    private ITaskInstanceService taskInstanceService;
    @Autowired
    private ITaskTemplateService taskTemplateService;
    @Autowired
    private DingDingNotifyService dingDingNotifyService;
    @Autowired
    private ICompanyService companyService;
    /**
     * 任务名称
     */
    private String jobName;
    /**
     * 任务分组
     */
    private String jobGroup;
    /**
     * 任务状态 是否启动任务
     */
    private String jobStatus;
    /**
     * cron表达式
     */
    private String cronExpression;
    /**
     * 描述
     */
    private String description;
    /**
     * 任务执行时调用哪个类的方法 包名+类名
     */
    private Class beanClass = this.getClass();
    /**
     * 任务是否有状态
     */
    private String isConcurrent;

    /**
     * Spring bean
     */
    private String springBean;

    /**
     * 任务调用的方法名
     */
    private String methodName;

    /**
     * 任务调用的方法名
     */
    private String companyId;

    /**
     * 定时任务模板id
     */
    private String taskTemplateId;

    /**
     * 公司名称
     */
    private String companyName;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getTaskTemplateId() {
        return taskTemplateId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public void setTaskTemplateId(String taskTemplateId) {
        this.taskTemplateId = taskTemplateId;
    }

    /**
     * 为了将执行后的任务持久化到数据库中
     */
    @JsonIgnore
    private JobDataMap dataMap = new JobDataMap();

    public BaseJob() {
    }

    /**
     * 任务名称和组构成任务key
     *
     * @return
     */
    public JobKey getJobKey() {
        return JobKey.jobKey(jobName, jobGroup);
    }

    public JobDataMap getDataMap() {
        if (dataMap.size() == 0) {
            dataMap.put(JOB_MAP_KEY, this);
        }
        return dataMap;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    public String getIsConcurrent() {
        return isConcurrent;
    }

    public void setIsConcurrent(String isConcurrent) {
        this.isConcurrent = isConcurrent;
    }

    public String getSpringBean() {
        return springBean;
    }

    public void setSpringBean(String springBean) {
        this.springBean = springBean;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ITaskInstanceService getTaskInstanceService() {
        return taskInstanceService;
    }

    public void setTaskInstanceService(ITaskInstanceService taskInstanceService) {
        this.taskInstanceService = taskInstanceService;
    }

    /**
     * 调度执行方法
     *
     * @param context
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void execute(JobExecutionContext context) {
        log.info("execute is running ！！！");

        // 此处执行具体任务，检测异常重试3次 ，如果失败则记录失败原因

        TaskInstance tTaskInstance = new TaskInstance();
        String taskTemplateId = context.getJobDetail().getJobDataMap().get("taskTemplateId") + "";
        String instanceName = context.getJobDetail().getJobDataMap().get("instanceName") + "";
        String companyId = context.getJobDetail().getJobDataMap().get("companyId") + "";

        TaskTemplate taskTemplate = taskTemplateService.getById(taskTemplateId);
        String id = UUIDS.getID();
        tTaskInstance.setDeleted(0);
        tTaskInstance.setTaskTemplateId(taskTemplateId);
        tTaskInstance.setId(id);
        System.out.println("主键:" + id);
        tTaskInstance.setTaskName(instanceName);
        tTaskInstance.setCreateTime(System.currentTimeMillis());
        tTaskInstance.setCompanyId(companyId);
        tTaskInstance.setType(taskTemplate.getType());
        tTaskInstance.setRetryTime(0);
        tTaskInstance.setManualRetryTime(0);
        //状态执行中
        tTaskInstance.setStatus(TimedTaskStatusEnum.EXECUTING.getKey());
        taskInstanceService.save(tTaskInstance);

        //使用CompletableFuture 调用执行方法
        try {
            tInstance(context, tTaskInstance, 0);
        } catch (Exception e) {
            log.error( "公司："+companyId +",taskTemplateId:" + taskTemplateId + "实例名称: " +instanceName +",execute is not well done ！！！");
        }
        log.info("execute is end ！！！");
    }

    /**
     * 调用spring容器获取bean和具体执行方法，执行任务
     *
     * @param context job上下文
     * @throws Exception
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void tInstance(JobExecutionContext context, TaskInstance tTaskInstance, int count) {
        // 使用定制的Executor在前一个阶段上异步应用函数
        CompletableFuture cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            if (count > 3) {
                return "success";
            }

            // 执行异常，自己获取、自己处理
            methodClass(context,tTaskInstance.getId());


            log.info("success count : " + count);
            return "success";
        },executor)
        .thenApplyAsync(s -> {
            if (count > 3) {
                return "success";
            }
            // 成功后执行逻辑
            TaskInstance successInstance = new TaskInstance();

            successInstance.setId(tTaskInstance.getId());
            successInstance.setDeleted(0);
            successInstance.setTaskTemplateId(tTaskInstance.getTaskTemplateId());
            successInstance.setId(tTaskInstance.getId());
            successInstance.setTaskName(tTaskInstance.getTaskName());
            successInstance.setCreateTime(tTaskInstance.getCreateTime());
            successInstance.setCompanyId(tTaskInstance.getCompanyId());
            successInstance.setLastExcuteTime(System.currentTimeMillis());

            if (null != successInstance.getRetryTime()) {
                successInstance.setRetryTime(count);
            } else {
                successInstance.setRetryTime(0);
            }
            tTaskInstance.setModifyTime(System.currentTimeMillis());
            successInstance.setStatus(TimedTaskStatusEnum.SUCCESS.getKey());
            successInstance.setSuccessTime(System.currentTimeMillis());
            // 自动重试后成功，需要清空失败原因
            successInstance.setFailReason(" ");
            log.info("successInstance: " + successInstance);
            taskInstanceService.updateById(successInstance);

            return "success";
        })
        .exceptionally(ex -> {
            //失败后执行逻辑
            // 记录失败次数以及原因
            if (count > 3) {
                return "failed";
            }
            TaskInstance failedInstance = taskInstanceService.getById(tTaskInstance.getId());
            if(null==failedInstance){
                failedInstance = new TaskInstance();
            }
            failedInstance.setId(tTaskInstance.getId());
            failedInstance.setDeleted(0);
            failedInstance.setTaskTemplateId(tTaskInstance.getTaskTemplateId());
            failedInstance.setId(tTaskInstance.getId());
            failedInstance.setTaskName(tTaskInstance.getTaskName());
            failedInstance.setCreateTime(tTaskInstance.getCreateTime());
            failedInstance.setCompanyId(tTaskInstance.getCompanyId());
            failedInstance.setLastExcuteTime(System.currentTimeMillis());


            failedInstance.setStatus(TimedTaskStatusEnum.FAILURE.getKey());
            failedInstance.setRetryTime(count);
            String totalFailReason = "";
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

            if(!StringUtils.isEmpty(failedInstance.getFailReason())){
                totalFailReason = failedInstance.getFailReason() + failReason.toString();
            }else{
                totalFailReason = failReason.toString();
            }

            failedInstance.setFailReason(totalFailReason);
            if (count > 0) {
                failedInstance.setModifyTime(System.currentTimeMillis());
            }
            log.error("Oops! We have an exception - " + ex.getMessage());
            log.error("failed count : " + count);

            taskInstanceService.updateById(failedInstance);
            //失败重试
            tInstance(context, tTaskInstance, count + 1);
            // 三次失败，发送文本消息给相应人员
            if(3==count){
                OANotifyDTO notify = null;
                try {

                    SimpleDateFormat sdf1 =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" );
                    Date d1= new Date();
                    String str1 = sdf1.format(d1);

                    StringBuffer content = new StringBuffer();
                    Company company = companyService.getById(tTaskInstance.getCompanyId());
                    companyId =  context.getJobDetail().getJobDataMap().get("companyId") + "";
                    String userId=company.getUserIds();
                    String title = "定时任务执行失败通知" ;
                    TaskTemplate taskTemplate = taskTemplateService.getById(tTaskInstance.getTaskTemplateId());

                    notify = new OANotifyDTO(companyId, userId, null, title);

                    notify.addParam("失败时间",str1);

                    String name= InetAddress.getLocalHost().getHostName();
                    String ip= InetAddress.getLocalHost().getHostAddress();
                    notify.addParam("服务器名称",name);
                    notify.addParam("服务器IP地址",ip);
                    notify.addParam("公司",company.getName());
                    notify.addParam("任务",taskTemplate.getName());
                    notify.addParam("实例ID",tTaskInstance.getId());
                    notify.addParam("失败原因", ex.getMessage());


                } catch (UnknownHostException e) {
                    log.info(e.getMessage());
                }
                dingDingNotifyService.sendOAMessage(notify);
            }
            return "failed!";
        });


//        System.out.println(cf.getNow(null));
        System.out.println("end" + count);
    }

    /**
     * 调用spring容器获取bean和具体执行方法，执行任务
     *
     * @param context job上下文
     * @throws Exception
     */
    public void methodClass(JobExecutionContext context,String taskInstanceId) {

        String methodName = context.getTrigger().getJobDataMap().get("method") + "";

        String classStr = context.getJobDetail().getJobDataMap().get("class") + "";
        String group = context.getJobDetail().getJobDataMap().get("group") + "";
        String companyId = context.getJobDetail().getJobDataMap().get("companyId") + "";

        Object object = SpringContextUtil.getBean(classStr);
        Method method = ReflectionUtils.findMethod(object.getClass(), methodName, String.class, String.class);

        Object o = ReflectionUtils.invokeMethod(method, object,  companyId,taskInstanceId);

    }

    static ExecutorService executor = newFixedThreadPool(3, new ThreadFactory() {
        int count = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "execute thread" + count++);
        }
    });


}
