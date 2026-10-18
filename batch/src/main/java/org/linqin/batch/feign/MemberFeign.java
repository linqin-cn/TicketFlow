package org.linqin.batch.feign;

import org.linqin.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * batch 模块的定时任务 **PassengerJob** 用来调用 **member 服务**的 "初始化乘客数据" 接口的 Feign 客户端
 * 到点了 batch 不直接操作数据库，而是通过 HTTP 让 member 服务自己初始化乘客（张三、李四、王五）
 */
@FeignClient(value = "member")
public interface MemberFeign {

    // 注入对象后调用.init()方法，发送http请求，初始化乘客数据
    @GetMapping("/member/passenger/init")
    CommonResp init();

}