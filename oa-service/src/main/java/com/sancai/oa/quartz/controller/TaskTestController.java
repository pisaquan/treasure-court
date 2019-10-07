package com.sancai.oa.quartz.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.exception.EnumCompanyError;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.exception.EnumSystemError;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.quartz.entity.TaskInstance;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.exception.EnumTaskTemplateError;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import com.sancai.oa.quartz.util.SpringContextUtil;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.typestatus.enums.TimedTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 任务测试
 * </p>
 *
 * @author dancer
 * @since 2019-07-31
 */
@ApiVersion(1)
@RestController
@Slf4j
@RequestMapping("{version}/task_test")
public class TaskTestController {

    /**
     * 环境
     */
    @Value("${spring.profiles.active}")
    private String env;



    @Autowired
    private ITaskInstanceService taskInstanceService;

    @Autowired
    private ITaskTemplateService taskTemplateService;

    @Autowired
    private ICompanyService companyService;
    /**
     * 修改系统时间
     *
     * @param time
     * @return
     */
    @GetMapping("/update_date")
    public ApiResponse updateDate(@RequestParam String time) {
        if("prod".equals(env)){
            return ApiResponse.fail(EnumSystemError.PROD_NO_GRAB);
        }

        Runtime runTime = Runtime.getRuntime();
        if (runTime == null) {
            System.err.println("Create runtime false!");
        }
        try {
            FileWriter excutefw = new FileWriter("/usr/updateSysTime.sh");
            BufferedWriter excutebw=new BufferedWriter(excutefw);
            excutebw.write("date -s \"" + time +"\"\r\n");
            excutebw.close();
            excutefw.close();
            String cmd_date ="sh /usr/updateSysTime.sh";
            Runtime.getRuntime().exec(cmd_date);

        } catch (Exception ex) {
            System.out.println("ex:"+ex.getMessage());
            log.error(ex.getMessage());
            return ApiResponse.fail(EnumSystemError.SYSTEM_ERROR);
        }
        return ApiResponse.success(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
    }

    @GetMapping("/show_date")
    public ApiResponse showDate() {
        return ApiResponse.success(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
    }

    /**
     * 定时测试

     * @return
     */
    @GetMapping("/excute_task")
    public ApiResponse excuteTask(@RequestParam String companyId,@RequestParam String group,@RequestParam Long startTime,@RequestParam Long endTime) {
        if("prod".equals(env)){
            return ApiResponse.fail(EnumSystemError.PROD_NO_GRAB);
        }
        QueryWrapper<TaskTemplate> wrapper = new QueryWrapper();
        wrapper.eq("task_key",group);
        TaskTemplate taskTemplate = taskTemplateService.getOne(wrapper);
        if(taskTemplate == null){
            return ApiResponse.fail(EnumTaskTemplateError.TASK_TASK_TEMPLATE_NOT_DATA);
        }
        Company company =  companyService.companyDetail(companyId);
        if(company == null){
            return ApiResponse.fail(EnumCompanyError.COMPANY_NOT_DATA);
        }
        TaskInstance taskInstance = taskInstanceService.saveTask(companyId,group,taskTemplate,company, TimedTaskStatusEnum.FAILURE, startTime,endTime);

        String methodName = taskTemplate.getMethodstr();

        String classStr = taskTemplate.getClassstr();

        Object object = SpringContextUtil.getBean(classStr);
        Method method = ReflectionUtils.findMethod(object.getClass(), methodName, String.class, String.class);

        new Thread((new Runnable() {
            @Override
            public void run() {
                // 批量同步数据
                ReflectionUtils.invokeMethod(method, object,  companyId,taskInstance.getId());
            }
        })).start();

        TaskMessage.addMessage(taskInstance.getId(),"定时任务开始:"+group);

        return ApiResponse.success(taskInstance);
    }


    @GetMapping("/task_message")
    public ApiResponse getMessage(@RequestParam String taskInstanceId) {
            List<String> result = new ArrayList<>();
            List<String> exceptions = TaskMessage.getExceptions();
            List<String> message = TaskMessage.getMessage(taskInstanceId);
            if(exceptions != null && exceptions.size() > 0){
                result.addAll(exceptions);
            }
            if(message != null && message.size() > 0){
                result.addAll(message);
            }

            if(result.size() > 0){
                TaskMessage.clean(taskInstanceId);
            }else{
                if(TaskMessage.isFinishMessage(taskInstanceId)){
                    //任务是否结束
                    ApiResponse ap = new ApiResponse(1);
                    return ap;
                }
            }

            return ApiResponse.success(result);


    }

}

