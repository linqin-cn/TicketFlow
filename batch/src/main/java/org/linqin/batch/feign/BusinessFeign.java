package org.linqin.batch.feign;

import lombok.Data;
import org.linqin.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;

/**
 * `BusinessFeign` 是 **OpenFeign 声明式 HTTP 客户端接口**——
 * 让 batch 模块（定时任务）能像调用本地方法一样调用 business 模块（业务服务）的 HTTP 接口，不用自己写 RestTemplate、拼 URL、解析 JSON。
 * **`@FeignClient`**：标记这是一个 Feign 客户端接口。
 * Spring 启动时会**动态生成这个接口的实现类**（JDK 动态代理），你 @Resource 注入的就是这个代理对象。
 *
 * **`value = "business"`**：要调用的服务名。这个名字对应 Nacos 注册中心里 business 服务注册的名字。
 * Feign 拿到这个名字后去 Nacos 查实际 IP:Port。
 *  ---降级策略---
 * **`fallback = BusinessFeignFallback.class`**：降级类。
 * 当 business 服务不可用（宕机、超时、网络错误）时，OpenFeign 会自动调用这个类里的对应方法，如hello对应降级类的hello方法，而不是抛异常。
 */
/* @FeignClient(name = "business", url = "http://127.0.0.1:8002/business")
 开发调试用 —— 不经过 Nacos，直接写死目标地址。这样就不用启动 Nacos 也能联调。一般生产环境走注册中心，本地调试可以切到 url 模式
 - **`name`**：这个 FeignClient 的**身份证 / Bean 名称**，不管走不走 Nacos 都得有，用来在 Spring 容器里唯一标识它。
 - **`url`**：可选，写了就**直连这个地址，不走 Nacos**。不写才去 Nacos 按 name 查地址。
 `name` 和 `value` 是同一个属性，写哪个都是一个意思，
 **Feign 只负责 "发 HTTP 请求"，它从来不直接查注册中心**。查注册中心这一步是 Spring Cloud 的另一套组件LoadBalancer（负载均衡器）干的：
Feign 本身不找注册中心，是 LoadBalancer 通过 DiscoveryClient 接口去问的，引 Nacos 依赖就问 Nacos，引 Zookeeper 依赖就问 Zookeeper，业务代码零改动
 */
@FeignClient(value = "business", fallback = BusinessFeignFallback.class)  // 指定服务名和降级类
public interface BusinessFeign {
    //- 声明式 HTTP：调 `hello()` 方法 = 发一个 `GET http://{business地址}/business/hello` 请求。
    //- 返回值 String：Feign 自动把响应体转成字符串。
    @GetMapping("/business/hello")
    String hello();

    //- 调 `genDaily(date)` = 发 `GET http://{business地址}/business/admin/daily-train/gen-daily/2026-10-25`。
    //- **`@PathVariable`**：把 date 填到 URL 里的 `{date}` 占位符。
    //- **`@DateTimeFormat(pattern = "yyyy-MM-dd")`**：把 Date 对象格式化成 `2026-10-25` 这种字符串拼到 URL。
    // Feign 自动把 business 返回的 JSON 反序列化成 CommonResp 对象。
    @GetMapping("/business/admin/daily-train/gen-daily/{date}")
    CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);
}
