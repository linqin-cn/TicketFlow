package org.linqin.batch.config;

import jakarta.annotation.Resource;
import org.linqin.common.interceptor.LogInterceptor;

public class SpringMvcConfig {

    @Resource
    LogInterceptor logInterceptor;
    /**
     * 添加日志拦截器--前端请求被 LogInterceptor拦截器生成流水号，放入当前线程Map(MDC里),再控制台打印，
     */

}
