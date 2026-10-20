package org.linqin.business.config;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class BusinessApplication {
    private static Logger LOG = LoggerFactory.getLogger(BusinessApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BusinessApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("启动成功！！");
        LOG.info("测试地址: \thttp://127.0.0.1:{}{}/hello", env.getProperty("spring.application.name"));

        // 限流规则
        initFlowRules();
        LOG.info("限流规则初始化成功！！");
    }


    private static void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();// 新建一条规则列表
        FlowRule rule = new FlowRule(); // 新建一条规则
        // 匹配的是`ConfirmOrderService.java`的doConfirm方法，而不是ConfirmOrderController的doConfirm方法，Controller 层的资源名是 **`confirmOrderDo`**
        rule.setResource("doConfirm"); // 指定这条规则作用在哪个资源上。资源名可以是接口 URL、方法名、或者你自定义的字符串标识
        /* 设置**阈值类型**为 QPS（每秒查询数）。Sentinel 有两种限流维度：
        - `FLOW_GRADE_QPS`：按每秒通过的请求数限流
        - `FLOW_GRADE_THREAD`：按同时处理的线程数限流*/
        rule.setGrade(1); // 设置流控模式，1 表示 QPS（每秒请求数），0 表示线程数
        // 对资源 `"doConfirm"` 做限流，**每秒最多放行 10 次请求**，超出的请求被拦截。
        rule.setCount(10); // 设置阈值，QPS 模式下表示
    }
}
