package org.linqin.batch.config;

import org.linqin.batch.job.DailyTrainJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {
    /**
     * 声明一个任务
     */
    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(DailyTrainJob.class) // 这个任务要执行哪个Job
                .withIdentity("DailyTrainJob", "group1")// 给Job起名，分组 -- "任务身份证"（JobKey）
                .storeDurably() // 持久化，即使当前没有Trigger绑定它，也留在Scheduler中
                .build(); // 构建出一个JobDetail对象
    }

    /**
     * 声明一个触发器，什么时候触发任务，多久触发一次
     */
    @Bean
    public Trigger trigger() {
        return TriggerBuilder.newTrigger()// 新建一个触发器
                .forJob(jobDetail()) // 绑定哪个任务--JobDetail
                .withIdentity("trigger", "trigger") // 给Trigger起名，分组
                .startNow() // 启动后立刻就开始算时间
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 0 * * ?"))// 设置corn表达式，多久执行一次任务，0 0 0 * * ?每天凌晨 00:00:00 执行一次
                .build();
    }
}
