package com.sancai.oa.quartz.controller;

import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.quartz.entity.BaseJob;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import com.sancai.oa.quartz.service.impl.JobAndTriggerServiceImpl;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

/**
 * @Author wangyl
 * @create 2019/7/29 09:39
 */

@ApiVersion(1)
@RestController
@RequestMapping("{version}/task_test")
public class TestQuartzController {
    @Autowired
    ITaskTemplateService taskTemplateService;
    @Autowired
    ICompanyService companyService;
    @Autowired
    private JobAndTriggerServiceImpl jobAndTriggerService;

    /**
     * 任务调度接口
     * @param classStr bean名称
     * @param methodStr 方法名
     * @param group 分组
     * @param cron  cron表达式
     * @param companyId 公司id
     * @return
     * @throws Exception
     */
    @RequestMapping("/test")
        public String test(@RequestParam("classStr") String classStr,
                @RequestParam("methodStr") String methodStr,
                @RequestParam("group") String group,
                @RequestParam("cron") String cron,
                @RequestParam("companyId") String companyId,
                @RequestParam("templateId") String templateId) throws Exception {
            BaseJob job = new BaseJob();
        TaskTemplate taskTemplate = taskTemplateService.getById(templateId);
        Company company= companyService.getById(companyId);
        // 模板名称
        job.setJobName(taskTemplate.getName());
        // job组
        job.setJobGroup(taskTemplate.getName()+","+company.getName() + ","+group);
        // 增加描述信息 如 type taskKey等
        job.setDescription("");
        // cron
        job.setCronExpression(
                cron
        );
        job.setSpringBean(classStr);
        job.setMethodName(methodStr);
        job.setCompanyId(companyId);
        job.setTaskTemplateId(templateId);
        job.setCompanyName(company.getName());
        try {
            jobAndTriggerService.addJob(job);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        return "success";
    }

    @RequestMapping("/method")
    public String method() throws Exception {
//        Method[] methods = Class.forName("com.rmjk.job.util.StringUtil").getMethods();
//        String method = "chk";
//        for (Method m : methods) {
//
//            if (m.getName().equals(method)) {
//                System.out.println(m.getName());
//                try {
//                    m.invoke(Class.forName("com.rmjk.job.util.StringUtil").newInstance(), null);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        return "success";
    }
}
