package com.sancai.oa.quartz.config;

import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;


/**
 * 对quartz.rpoperties进行读取
 * @author wangyl
 * @date 2019/7/30 13:08
 */
@Configuration
public class SchedulerConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MyJobFactory myJobFactory;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setOverwriteExistingJobs(true);

        // 延时启动1
        factory.setStartupDelay(20);
        // 加载quartz数据源配置
        factory.setQuartzProperties(quartzProperties());

        // 自定义Job Factory，用于Spring注入
        factory.setJobFactory(myJobFactory);

        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /**
     * quartz初始化监听器
     * @return
     */
    @Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }

    /**
     * 通过SchedulerFactoryBean获取Scheduler的实例
     * @return
     * @throws Exception
     */
    @Bean(name="scheduler")
    public Scheduler scheduler() throws Exception {
        return schedulerFactoryBean().getScheduler();
    }


}
