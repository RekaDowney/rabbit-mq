package me.junbin.rabbitmq.spring.listener;

import me.junbin.rabbitmq.spring.message.HeadersMessage;
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

    @Override
    ConsumeResult idempotentConsumeRetryable(HeadersMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

    @Override
    ConsumeResult recoverForAllRetryFail(HeadersMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

}