package me.junbin.rabbitmq.spring.listener;

import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.constant.MqConstant;
import me.junbin.rabbitmq.spring.message.FanoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:50
 * @description :
 */
@Service("fanoutQueueListener")
public class FanoutQueueListener extends AbstractRetryableMessageListener<FanoutMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutQueueListener.class);

    @Override
    ConsumeResult idempotentConsumeRetryable(FanoutMessage message, MessageProperties properties, RetryContext retryContext) {
        retryContext.setAttribute(MqConstant.MESSAGE, message);
        if (retryContext.getLastThrowable() != null) {
            LOGGER.info("第{}次重试消费：{}。上次消费失败原因：{}", retryContext.getRetryCount(),
                    Gsonor.SIMPLE.toJson(message), retryContext.getLastThrowable().getMessage());
        } else {
            LOGGER.info("第1次消费消息：{}", Gsonor.SIMPLE.toJson(message));
        }
        return ConsumeResult.OK;
    }

    @Override
    ConsumeResult recoverForAllRetryFail(FanoutMessage message, MessageProperties properties, RetryContext retryContext) {
        assert retryContext.getAttribute(MqConstant.MESSAGE) == message;
        LOGGER.info("消息{}消费失败达到{}次", Gsonor.SIMPLE.toJson(message), retryContext.getRetryCount());
        return ConsumeResult.FAIL;
    }

}