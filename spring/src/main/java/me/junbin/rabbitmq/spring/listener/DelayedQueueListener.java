package me.junbin.rabbitmq.spring.listener;

import me.junbin.rabbitmq.spring.message.DelayedMessage;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:50
 * @description :
 */
@Service("delayedQueueListener")
public class DelayedQueueListener extends AbstractRetryableMessageListener<DelayedMessage> {

    @Override
    ConsumeResult idempotentConsumeRetryable(DelayedMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

    @Override
    ConsumeResult recoverForAllRetryFail(DelayedMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

}