package me.junbin.rabbitmq.spring.listener;

import me.junbin.rabbitmq.spring.message.TopicMessage;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.retry.RetryContext;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:50
 * @description :
 */
@Service("topicQueueListener")
public class TopicQueueListener extends AbstractRetryableMessageListener<TopicMessage> {

    @Override
    ConsumeResult idempotentConsumeRetryable(TopicMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

    @Override
    ConsumeResult recoverForAllRetryFail(TopicMessage message, MessageProperties properties, RetryContext retryContext) {
        return null;
    }

}