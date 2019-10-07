package com.sancai.oa.quartz.init;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.service.ICompanyService;
import com.sancai.oa.quartz.entity.BaseJob;
import com.sancai.oa.quartz.entity.JobAndTrigger;
import com.sancai.oa.quartz.entity.TaskTemplate;
import com.sancai.oa.quartz.service.IJobAndTriggerService;
import com.sancai.oa.quartz.service.ITaskInstanceService;
import com.sancai.oa.quartz.service.ITaskTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@Order(value = 2)
@Service
public class QuartzInit implements ApplicationRunner {

    @Autowired
    ITaskInstanceService taskInstanceService;
    @Autowired
    ITaskTemplateService taskTemplateService;
    @Autowired
    ICompanyService companyService;
    @Autowired
    IJobAndTriggerService jobAndTriggerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("清理quartz执行开始");
        taskInstanceService.clearQuartz();
        System.out.println("清理quartz执行结束");

        System.out.println("定时任务初始化执行开始");
        // 获取公司列表
        QueryWrapper<Company> wrapperCompany = new QueryWrapper();
        wrapperCompany.lambda().eq(Company::getDeleted, 0L);
        wrapperCompany.orderByAsc(true,"create_time");

        List<Company> companyList = companyService.list(wrapperCompany);
        for (Company company : companyList) {
            System.out.println(company.getName());
        }

        //获取模板列表
        QueryWrapper<TaskTemplate> wrappertask = new QueryWrapper();
        wrappertask.lambda().eq(TaskTemplate::getDeleted, 0L);
        List<TaskTemplate> taskTemplatelist = taskTemplateService.list(wrappertask);
        for (TaskTemplate taskTemplate : taskTemplatelist) {
            System.out.println(taskTemplate.getName());
        }
        //双层遍历，获取任务数据，调用任务添加接口

        // 批次号
        int batch = 0;
        for (Company company : companyList) {
            System.out.println(company.getName());
            for (TaskTemplate taskTemplate : taskTemplatelist) {
                System.out.println(taskTemplate.getName());
                BaseJob job = new BaseJob();

                // 模板名称
                job.setJobName(taskTemplate.getName());
                // job组   tasktemplate type 加上公司名称
                job.setJobGroup(taskTemplate.getType()+","+company.getName());
                // 增加描述信息 如 type taskkey等
                job.setDescription("");
                // cron
                String cron = taskTemplate.getFrequency();

                String newCron = countCron(cron,batch);
                job.setCronExpression(
                        newCron
                );
                job.setSpringBean(taskTemplate.getClassstr());
                job.setMethodName(taskTemplate.getMethodstr());
                job.setCompanyId(company.getId());
                job.setTaskTemplateId(taskTemplate.getId());
                job.setCompanyName(company.getName());
                jobAndTriggerService.addJob(job);
            }
            batch++;
        }

        System.out.println("定时任务初始化执行结束");
    }


    public void initOneCompany(String companyId) throws Exception {
        System.out.println("单个公司定时任务初始化执行开始");
        // 获取公司列表
        QueryWrapper<Company> wrapperCompany = new QueryWrapper();
        wrapperCompany.lambda().eq(Company::getDeleted, 0L);
        wrapperCompany.orderByAsc(true,"create_time");

        List<Company> companyList = companyService.list(wrapperCompany);
        for (Company company : companyList) {
            System.out.println(company.getName());
        }

        //获取模板列表
        QueryWrapper<TaskTemplate> wrappertask = new QueryWrapper();
        wrappertask.lambda().eq(TaskTemplate::getDeleted, 0L);
        List<TaskTemplate> taskTemplatelist = taskTemplateService.list(wrappertask);
        for (TaskTemplate taskTemplate : taskTemplatelist) {
            System.out.println(taskTemplate.getName());
        }
        //双层遍历，获取任务数据，调用任务添加接口

        // 批次号
        int batch = 0;
        for (Company company : companyList) {
            System.out.println(company.getName()+"定时任务初始化开始执行");
            if(company.getId().equals(companyId)){
                for (TaskTemplate taskTemplate : taskTemplatelist) {

                    System.out.println(taskTemplate.getName());
                    BaseJob job = new BaseJob();
                    // 增加描述信息 如 type taskkey等
                    job.setDescription("");
                    // 模板名称
                    job.setJobName(taskTemplate.getName());
                    // job组   tasktemplate type 加上公司名称
                    job.setJobGroup(taskTemplate.getType()+","+company.getName());

                    // cron
                    String cron = taskTemplate.getFrequency();

                    String newCron = countCron(cron,batch);
                    job.setCronExpression(
                            newCron
                    );
                    job.setSpringBean(taskTemplate.getClassstr());
                    job.setMethodName(taskTemplate.getMethodstr());
                    job.setCompanyId(company.getId());
                    job.setTaskTemplateId(taskTemplate.getId());
                    job.setCompanyName(company.getName());
                    jobAndTriggerService.addJob(job);
                }
                break;
            }

            batch++;
        }

        System.out.println("单公司初始化执行结束");
    }


    /**
     * 根据cron 和batch 生成新的cron
     * @param cron
     * @return
     */
    private String countCron(String cron,int batch){
        StringBuffer newCron = new StringBuffer();

        String[] cronList =  cron.split(" ");
        String seconds = cronList[0];
        int second = Integer.parseInt(seconds);
        String mins = cronList[1];
        int min = Integer.parseInt(mins);
        // 加一分钟,并处理临界值问题
        min = min + batch%(60-min) ;
        second = (second + batch)%59;
        // 准备构建新的cron
        cronList[0] = second+"";
        cronList[1] = min+"";
        //构建新的cron
        for(int i =0;i<cronList.length;i++){
            newCron.append(cronList[i]+" ");
        }
        return newCron.toString();
    }
}