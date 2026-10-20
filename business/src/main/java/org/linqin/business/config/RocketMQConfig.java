package org.linqin.business.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RocketMQConfig {
    @Bean
    public RocketMQTemplate  rocketMQTemplate() {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        // 创建一个默认的 RocketMQ 生产者
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setProducerGroup("default");// 设置生产者的组名
        producer.setNamesrvAddr("http://localhost:9876");//指定 RocketMQ NameServer 地址，生产者从这里找 Broker
        producer.setSendMsgTimeout(3000); // 发送消息超时时间 3000ms（3 秒），超时就报错
        rocketMQTemplate.setProducer(producer); // 把原生生产者塞进模板，模板底层靠它发消息
        return rocketMQTemplate;
    }
}
