package me.junbin.rabbitmq.spring.constant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 16:18
 * @description :
 */
@Component
public class SpringMqConstant {

    @Autowired
    private Environment environment;
    @Value("${rabbitmq.direct.routing.key}")
    private String directRoutingKey;
    @Value("${rabbitmq.fanout.exchange}")
    private String fanoutExchange;
    @Value("${rabbitmq.direct.exchange}")
    private String directExchange;
    @Value("${rabbitmq.topic.exchange}")
    private String topicExchange;
    @Value("${rabbitmq.headers.exchange}")
    private String headersExchange;
    @Value("${rabbitmq.delayed.exchange}")
    private String delayedExchange;
    @Value("${rabbitmq.delayed.routing.key}")
    private String delayedRoutingKey;

    public <T> T get(String key, Class<T> type) {
        return environment.getProperty(key, type);
    }

    public String getDirectRoutingKey() {
        return directRoutingKey;
    }

    public String getFanoutExchange() {
        return fanoutExchange;
    }

    public String getDirectExchange() {
        return directExchange;
    }

    public String getTopicExchange() {
        return topicExchange;
    }

    public String getHeadersExchange() {
        return headersExchange;
    }

    public String getDelayedExchange() {
        return delayedExchange;
    }

    public String getDelayedRoutingKey() {
        return delayedRoutingKey;
    }

}