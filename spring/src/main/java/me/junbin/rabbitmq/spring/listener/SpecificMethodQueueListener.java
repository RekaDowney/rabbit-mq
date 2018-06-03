package me.junbin.rabbitmq.spring.listener;

import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.message.FanoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/6/1 10:32
 * @description :
 */
@Service("specificMethodQueueListener")
public class SpecificMethodQueueListener extends AbstractRetryableMessageListener<FanoutMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificMethodQueueListener.class);

    @Override
    public ConsumeResult idempotentConsumeRetryable(FanoutMessage message, MessageProperties properties, RetryContext retryContext) throws Exception {
        LOGGER.info("消费消息：{}", Gsonor.SIMPLE.toJson(message));
        return ConsumeResult.OK;
    }

    @Override
    public ConsumeResult recoverForAllRetryFail(FanoutMessage message, MessageProperties properties, RetryContext retryContext) {
        if (retryContext.getLastThrowable() instanceof RuntimeException) {
            LOGGER.warn("消息：{} 消费失败", Gsonor.SIMPLE.toJson(message));
            return ConsumeResult.FAIL;
        }
        LOGGER.info("消息：{} 消费失败，准备重试", Gsonor.SIMPLE.toJson(message));
        return ConsumeResult.RETRY;
    }

}