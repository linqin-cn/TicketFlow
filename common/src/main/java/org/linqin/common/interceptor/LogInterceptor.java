package org.linqin.common.interceptor;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

public class LogInterceptor implements HandlerInterceptor {

    /**
     * `preHandle()`：Controller 方法**执行前**调用
     * **`MDC`** 是 SLF4J 提供的工具类（`org.slf4j.MDC`），全称 Mapped Diagnostic Context。
     * 本质是一个**ThreadLocal<Map>**—— 每个线程一份独立的 Map，互不可见。
     * 日志输出格式写了 `%X{LOG_ID}`，日志框架会去 MDC 里找 LOG_ID 对应的值，输出到日志里。
     * 当前时间 + 加 3 位随机串（62^3 ≈ 23 万种组合）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 增加日志流水号
        MDC.put("LOG_ID",System.currentTimeMillis()+ RandomUtil.randomString(3));
        return true;
    }
}
