package org.linqin.batch.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.Resource;
import org.linqin.batch.feign.BusinessFeign;
import org.linqin.common.resp.CommonResp;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Date;

// `@DisallowConcurrentExecution` 加在 Job 类上，
// **禁止同一个任务并发执行**—— 上一次还没跑完，下一次触发就等它跑完再说，不会重复 new 一个 Job 实例同时跑。
@DisallowConcurrentExecution
public class DailyTrainJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainJob.class);

    @Resource
    BusinessFeign businessFeign;

    /**
     *
     * `Job` 接口的 `execute()` 方法（Quartz 到点时就调这个方法）
     * jobExecutionContext：Quartz 传进来的上下文对象，能拿到 JobDetail、Trigger、JobDataMap 里的参数等
     *
     * 意思就算如果没有 MDC.put ("LOG_ID", System.currentTimeMillis () + RandomUtil.randomString (3)); [LOG.info](https://LOG.info)("生成 15 天后的车次数据开始");，
     * 虽然 excute 里面的 CommonResp<Object> commonResp = businessFeign.genDaily (offsetDate);
     * 发 http 请求会被拦截加上日志，但是 batch 里没有日志显示看不出它在干什么业务，不像 controller 里一样手动执行会有日志
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 增加日志流水号
        // 定时任务没有 HTTP 请求，LogInterceptor 管不到，所以**手动**给这次执行打个流水号，让后面所有日志都带同一个号，方便排查。
        MDC.put("LOG_ID", System.currentTimeMillis() + RandomUtil.randomString(3));
        // LogInterceptor 拦截的是请求，http 请求才会走拦截器的 MDC.put 日志，定时任务不会被 LogInterceptor 拦截所有需要手动 MDC.put?
        LOG.info("生成15天后的车次数据");
        Date date = new Date();
        DateTime dateTime = DateUtil.offsetDay(date, 15); //hutool 工具类，给日期加 15 天，**`DateTime`**：hutool 自己的日期类型
        Date offsetDate = dateTime.toJdkDate(); // 转为Date
        CommonResp<Object> commonResp = businessFeign.genDaily(offsetDate);//返回统一响应体 `CommonResp`，里面带 success 和结果信息
        LOG.info("生成15天后的车次数据，结果：{}", commonResp);
    }
}
