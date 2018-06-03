package me.junbin.rabbitmq.spring;

import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.constant.MqConstant;
import me.junbin.rabbitmq.spring.constant.SpringMqConstant;
import me.junbin.rabbitmq.spring.message.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/30 17:21
 * @description :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-mq.xml"})
public class TestProduceMessage {

    @Autowired
    private RetryTemplate retryTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SpringMqConstant springMqConstant;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestProduceMessage.class);

    // 先启动 Tomcat 作为消费者，然后执行单元测试生产消息
    @Test
    public void testProduceDirectMessage() throws Exception {
        for (int i = 1; i < 11; i++) {
            final DirectMessage message = new DirectMessage((long) i, String.format("第%d条消息", i));
            retryTemplate.execute(
                    retryContext -> {
                        int retryCount = retryContext.getRetryCount();
                        Throwable lastThrowable = retryContext.getLastThrowable();
                        if (lastThrowable != null) {
                            LOGGER.info("第{}次重试发送消息：{}失败，异常：{}", retryCount, Gsonor.SIMPLE.toJson(message), lastThrowable.getMessage());
                        } else {
                            LOGGER.info("发送消息：{}", Gsonor.SIMPLE.toJson(message));
                        }
                        rabbitTemplate.convertAndSend(springMqConstant.getDirectExchange(), springMqConstant.getDirectRoutingKey(), message);
                        return null;
                    },
                    retryContext -> {
                        int retryCount = retryContext.getRetryCount();
                        Throwable lastThrowable = retryContext.getLastThrowable();
                        LOGGER.error(String.format("消息 %s 重试发送%d次都失败", Gsonor.SIMPLE.toJson(message), retryCount), lastThrowable);
                        return null;
                    }
            );
        }
    }

    @Test
    public void testProduceTopicMessage() throws Exception {
        // spring.topic.#.routing.key
        final String topicRoutingKeyPattern = "spring.topic.%s.routing.key";
        for (int i = 1; i < 11; i++) {
            final TopicMessage message = new TopicMessage((long) i, String.format("第%d条消息", i));
            final String exchange = springMqConstant.getTopicExchange();
            String routingKey = String.format(topicRoutingKeyPattern, RandomStringUtils.randomAlphanumeric(4));
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            LOGGER.info("发送消息：{}", Gsonor.SIMPLE.toJson(message));
        }
    }

    @Test
    public void testProduceHeadersMessage() throws Exception {
        final String exchange = springMqConstant.getHeadersExchange();
        // 添加头部信息，消息头匹配模式为 X-MATCH any
        MessagePostProcessor appendHeadersProcessor = message -> {
            int one2Three = RandomUtils.nextInt(1, 4);
            switch (one2Three) {
                case 1:
                    message.getMessageProperties().setHeader("username", "reka");
                    break;
                case 2:
                    message.getMessageProperties().setHeader("age", 24);
                    break;
                case 3:
                    message.getMessageProperties().setHeader("username", "reka");
                    message.getMessageProperties().setHeader("age", 24);
                    break;
            }
            return message;
        };

        for (int i = 1; i < 11; i++) {
            final HeadersMessage message = new HeadersMessage((long) i, String.format("第%d条消息", i));
            // Spring 支持 routingKey 为 null，因为 Spring 内部会将 null 转成空串
            rabbitTemplate.convertAndSend(exchange, MqConstant.EMPTY, message, appendHeadersProcessor);
            LOGGER.info("发送消息：{}", Gsonor.SIMPLE.toJson(message));
        }
    }

    @Test
    public void testProduceDelayedMessage() throws Exception {
        String exchange = springMqConstant.getDelayedExchange();
        String routingKey = springMqConstant.getDelayedRoutingKey();
        for (int i = 1; i < 11; i++) {
            final DelayedMessage message = new DelayedMessage((long) i, String.format("第%d条消息", i));
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            LOGGER.info("发送延迟消息：{}", Gsonor.SIMPLE.toJson(message));
        }
    }

    @Test
    public void testProduceAutoDeleteMessage() throws Exception {
        for (int i = 1; i < 11; i++) {
            final FanoutMessage message = new FanoutMessage((long) i, String.format("第%d条消息", i));
            rabbitTemplate.convertAndSend("auto.delete.fanout.exchange", null, message);
            LOGGER.info("发送消息：{}", Gsonor.SIMPLE.toJson(message));
        }
    }

}