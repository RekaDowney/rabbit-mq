package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.rabbitmq.spring.message.MessageEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 23:00
 * @description :
 */
public abstract class AbstractRetryableMessageListener<T extends MessageEntity> implements ChannelAwareMessageListener {

    @Autowired
    private RetryTemplate retryTemplate;
    @Autowired
    protected MessageConverter messageConverter;

    // 幂等可重试消费。消费失败会自动重试
    abstract ConsumeResult idempotentConsumeRetryable(T message, MessageProperties properties, RetryContext retryContext);

    // 所有重试操作都失败后执行该方法
    abstract ConsumeResult recoverForAllRetryFail(T message, MessageProperties properties, RetryContext retryContext);

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        ConsumeResult result = ConsumeResult.FAIL;
        final MessageProperties prop = message.getMessageProperties();
        try {
            @SuppressWarnings("unchecked") final T msg = (T) messageConverter.fromMessage(message);
            result = retryTemplate.execute(
                    retryContext -> this.idempotentConsumeRetryable(msg, prop, retryContext),
                    retryContext -> this.recoverForAllRetryFail(msg, prop, retryContext)
            );
        } finally {
            long deliveryTag = prop.getDeliveryTag();
            if (result == null) {
                result = ConsumeResult.OK;
            }
            switch (result) {
                case RETRY:
                    channel.basicReject(deliveryTag, true);
                    break;
                case FAIL:
                    channel.basicReject(deliveryTag, false);
                    break;
                case OK:
                default:
                    channel.basicAck(deliveryTag, false);
                    break;
            }
        }
    }

}