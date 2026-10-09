package org.linqin.batch.controller;

import org.linqin.batch.req.CronJobReq;
import org.linqin.batch.resp.CronJobResp;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.linqin.common.resp.CommonResp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller层--定时任务的**后台管理接口**通过 HTTP 请求**动态地增、删、改、查、立即执行、暂停、恢复**定时任务
 * 每次从 Spring 取得的 sched 是同一个，## 整个应用里 Scheduler 只有一个
 * 你每次调 `schedulerFactoryBean.getScheduler()` 拿到的都是**同一个 Scheduler 实例**，所以对它调 pause/resume/run/add/delete 全部作用于同一个调度中心，操作是实时生效的
 * 回忆一下 `SchedulerConfig` 里那个 `@Bean` 方法：
 *
 * ```
 * @Bean
 * public SchedulerFactoryBean schedulerFactoryBean(...) {
 *     SchedulerFactoryBean factory = new SchedulerFactoryBean();
 *     ...
 *     return factory;
 * }
 * ```
 *
 * - `@Bean` 注解的方法，Spring **整个容器只调一次**，返回的对象注册为**单例 Bean**。
 * - 这个 `SchedulerFactoryBean` 内部有一个字段持有着真正的 `org.quartz.Scheduler` 对象，在初始化时创建一次。
 * - 之后任何地方调 `factory.getScheduler()`，都是 `return this.scheduler`，**永远返回同一个**。
 */
@RestController // 这是个 REST 接口类，返回值自动转 JSON
@RequestMapping("/admin/job")//所有接口 URL 前缀都是 /admin/job
public class JobController {
    private static Logger LOG = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    /**
     * 手动执行任务
     * 前端发送请求--@RequestBody CronJobReq cornJobReq把 HTTP 请求体里的 JSON **自动反序列化**成 Java 对象
     * 再从请求对象里取出两个字段：任务类名和组名。这两个合起来就是之前反复讲的 "任务身份证"（JobKey）。
     * 手动执行定时任务
     * 这里的执行不会干扰定时任务里的Cron表达式指定的定时触发任务
     * @param cornJobReq 请求体，包含任务类名和任务组名
     * @return 通用响应对象
     * @throws SchedulerException 调度器异常
     */
    @PostMapping(value = "/run")
    public CommonResp<Object> run(@RequestBody CronJobReq cornJobReq) throws SchedulerException {
        String jobClassName = cornJobReq.getName();
        String jobGroupName = cornJobReq.getGroup();
        LOG.info("手动执行任务开始:{},{}",jobClassName,jobGroupName);
        schedulerFactoryBean.getScheduler() // 从 Spring 配好的工厂拿到真正的 Quartz `Scheduler` 对象。
                .triggerJob(JobKey.jobKey(jobClassName, jobGroupName)); // // 按 "类名 + 组名" 拼出任务的身份证。 **让 Quartz 立刻触发一次这个任务**—— 不管 cron 表达式设定的时间，插队马上跑一次 `execute()`。
        return new CommonResp<>();
    }

    /**
     * 手动创建定时任务
     * @param cronJobReq
     * @return
     */
    @PostMapping(value = "/add")
    public CommonResp add(@RequestBody CronJobReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        String cronExpression = cronJobReq.getCronExpression();
        String description = cronJobReq.getDescription();
        LOG.info("创建定时任务开始：{}，{}，{}，{}", jobClassName, jobGroupName, cronExpression, description);
        CommonResp commonResp = new CommonResp();

        try {
            // 通过SchedulerFactory获取一个调度器实例
            Scheduler sched = schedulerFactoryBean.getScheduler();

            // 启动调度器
            sched.start();

            //构建job信息
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(jobClassName)).withIdentity(jobClassName, jobGroupName).build();

            //表达式调度构建器(即任务执行的时间)
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, jobGroupName).withDescription(description).withSchedule(scheduleBuilder).build();

            sched.scheduleJob(jobDetail, trigger);

        } catch (SchedulerException e) {
            LOG.error("创建定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("创建定时任务失败:调度异常");
        } catch (ClassNotFoundException e) {
            LOG.error("创建定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("创建定时任务失败：任务类不存在");
        }
        LOG.info("创建定时任务结束：{}", commonResp);
        return commonResp;
    }

    /**
     * 暂停定时任务--设置定时任务后可以开始定时执行也可以暂停不执行
     * @param cronJobReq
     * @return
     */
    @PostMapping(value = "/puase")
    public CommonResp pause(@RequestBody CronJobReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        LOG.info("暂停定时任务开始：{}，{}", jobClassName, jobGroupName);
        CommonResp commonResp = new CommonResp();
        try {
            Scheduler sched = schedulerFactoryBean.getScheduler(); // 从 Spring 配好的工厂拿到真正的 Quartz `Scheduler` 对象。
            sched.pauseJob(JobKey.jobKey(jobClassName, jobGroupName)); // 按 "类名 + 组名" 拼出任务的身份证。 **让 Quartz 暂停这个任务**—— 不管 cron 表达式设定的时间，暂停不再触发 `execute()`。
        } catch (SchedulerException e) {
            LOG.error("暂停定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("暂停定时任务失败:调度异常");
        }
        LOG.info("暂停定时任务完成：{}", commonResp);
        return commonResp;
    }

    /**
     * 重启定时任务
     * @param cronJobReq
     * @return
     */
    @RequestMapping(value = "/resume")
    public CommonResp resume(@RequestBody CronJobReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        LOG.info("重启定时任务开始：{}，{}", jobClassName, jobGroupName);
        CommonResp commonResp = new CommonResp();
        try {
            Scheduler sched = schedulerFactoryBean.getScheduler();
            sched.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            LOG.error("重启定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("重启定时任务失败:调度异常");
        }
        LOG.info("重启定时任务结束：{}", commonResp);
        return commonResp;
    }
    @RequestMapping(value = "/reschedule")
    public CommonResp reschedule(@RequestBody CronJobReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        String cronExpression = cronJobReq.getCronExpression();
        String description = cronJobReq.getDescription();
        LOG.info("更新定时任务开始：{}，{}，{}，{}", jobClassName, jobGroupName, cronExpression, description);
        CommonResp commonResp = new CommonResp();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(jobClassName, jobGroupName);
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            CronTriggerImpl trigger1 = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
            trigger1.setStartTime(new Date()); // 重新设置开始时间
            CronTrigger trigger = trigger1;

            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withDescription(description).withSchedule(scheduleBuilder).build();

            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            LOG.error("更新定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("更新定时任务失败:调度异常");
        }
        LOG.info("更新定时任务结束：{}", commonResp);
        return commonResp;
    }

    @RequestMapping(value = "/delete")
    public CommonResp delete(@RequestBody CronJobReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        LOG.info("删除定时任务开始：{}，{}", jobClassName, jobGroupName);
        CommonResp commonResp = new CommonResp();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseTrigger(TriggerKey.triggerKey(jobClassName, jobGroupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobClassName, jobGroupName));
            scheduler.deleteJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
            LOG.error("删除定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("删除定时任务失败:调度异常");
        }
        LOG.info("删除定时任务结束：{}", commonResp);
        return commonResp;
    }

    /**
     * `content` 装的是 **`List<CronJobResp>`**
     * 最终返回内容为
     * {
     *   "success": true,
     *   "content": [
     *     {
     *       "name": "DailyTrainJob",
     *       "group": "test",
     *       "cronExpression": "0 0 0 * * ?",
     *       "nextFireTime": "2026-10-10T00:00:00.000+08:00",
     *       "preFireTime": "2026-10-09T00:00:00.000+08:00",
     *       "description": "每天生成15天后的车次",
     *       "state": "NORMAL"
     *     },
     *     {
     *       "name": "PassengerJob",
     *       "group": "test",
     *       "cronExpression": "0 0 1 * * ?",
     *       "nextFireTime": "...",
     *       "preFireTime": "...",
     *       "description": "...",
     *       "state": "PAUSED"
     *     }
     *   ]
     * }
     * @return
     */
    @RequestMapping(value="/query")
    public CommonResp query() {
        LOG.info("查看所有定时任务开始");
        CommonResp commonResp = new CommonResp();
        List<CronJobResp> cronJobDtoList = new ArrayList();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    CronJobResp cronJobResp = new CronJobResp();
                    cronJobResp.setName(jobKey.getName());
                    cronJobResp.setGroup(jobKey.getGroup());

                    //get job's trigger
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
                    cronJobResp.setNextFireTime(cronTrigger.getNextFireTime());
                    cronJobResp.setPreFireTime(cronTrigger.getPreviousFireTime());
                    cronJobResp.setCronExpression(cronTrigger.getCronExpression());
                    cronJobResp.setDescription(cronTrigger.getDescription());
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(cronTrigger.getKey());
                    cronJobResp.setState(triggerState.name());

                    cronJobDtoList.add(cronJobResp);
                }

            }
        } catch (SchedulerException e) {
            LOG.error("查看定时任务失败:" + e);
            commonResp.setSuccess(false);
            commonResp.setMessage("查看定时任务失败:调度异常");
        }
        commonResp.setContent(cronJobDtoList);
        LOG.info("查看定时任务结束：{}", commonResp);
        return commonResp;
    }
}
