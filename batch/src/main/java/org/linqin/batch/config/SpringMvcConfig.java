package org.linqin.batch.config;

import jakarta.annotation.Resource;
import org.linqin.common.interceptor.LogInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class SpringMvcConfig implements WebMvcConfigurer {

    @Resource
    LogInterceptor logInterceptor;
    /**
     * 添加日志拦截器--前端请求被 LogInterceptor拦截器生成流水号，放入当前线程Map(MDC里),再控制台打印，
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**");
    }

}
