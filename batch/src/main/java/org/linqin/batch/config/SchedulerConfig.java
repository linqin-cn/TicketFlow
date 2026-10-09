package org.linqin.batch.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;

public class SchedulerConfig {
    @Resource
    private MyJobFactory myJobFactory; // 因为 MyJobFactory 上有 @Component，Spring 容器里已经有它了

    /**
     * 创建并配置 Quartz 调度器工厂（SchedulerFactoryBean），把 Quartz 定时器接入 Spring 容器。
     * 方法参数：@Qualifier("dataSource"):Spring 自动从容器里找一个名字叫 "dataSource" 的 DataSource （Spring Boot 根据 `application.yml` 自动配置好的）传入
     * @param dataSource
     * @return配置完成的 SchedulerFactoryBean；Spring 容器会自动调用其 getObject()
     *  方法真正创建 org.quartz.Scheduler 实例并托管其生命周期
     * （随 Spring 容器启动而启动、关闭而关闭）。
     * 其他业务类可通过 @Autowired Scheduler 直接获取该调度器
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("dataSource") DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(myJobFactory);
        factory.setStartupDelay(2); //Spring 完全启动好之后，再等 2 秒才开始跑 Quartz，避免应用没起稳就触发任务
        return factory;
    }
}
