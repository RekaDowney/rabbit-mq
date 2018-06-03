package me.junbin.rabbitmq.spring.listener;

import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.message.HeadersMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 11:04
 * @description :
 */
@Service("headersQueueListener")
public class HeadersQueueListener extends AbstractRetryableMessageListener<HeadersMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersQueueListener.class);

    @Override
    public ConsumeResult idempotentConsumeRetryable(HeadersMessage message, MessageProperties properties, RetryContext retryContext) {
        String json = Gsonor.SIMPLE.toJson(message);
        String headerJson = Gsonor.SIMPLE.toJson(properties.getHeaders());
        if (retryContext.getLastThrowable() != null) {
            LOGGER.info("第{}次重试消费：{}。消息头部：{}。上次消费失败原因：{}", retryContext.getRetryCount(),
                    json, headerJson, retryContext.getLastThrowable().getMessage());
        } else {
            LOGGER.info("消费消息：{}。消息头部：{}", json, headerJson);
        }
        return ConsumeResult.OK;
    }

    @Override
    public ConsumeResult recoverForAllRetryFail(HeadersMessage message, MessageProperties properties, RetryContext retryContext) {
        LOGGER.info("消息{}消费失败达到{}次，丢弃该消息", Gsonor.SIMPLE.toJson(message), retryContext.getRetryCount());
        return ConsumeResult.FAIL;
    }

}