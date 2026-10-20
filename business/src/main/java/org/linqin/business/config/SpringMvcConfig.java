package org.linqin.business.config;

import jakarta.annotation.Resource;
import org.linqin.common.interceptor.LogInterceptor;
import org.linqin.common.interceptor.MemberInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class SpringMvcConfig implements WebMvcConfigurer {
    @Resource
    LogInterceptor logInterceptor;

    @Resource
    MemberInterceptor memberInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加日志拦截器
        registry.addInterceptor(logInterceptor).addPathPatterns("/**");
        // 添加用户拦截器用于校验制作，全校校验
        registry.addInterceptor(memberInterceptor).addPathPatterns("/**").excludePathPatterns("/hello");
    }
}
