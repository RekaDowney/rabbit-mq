package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.rabbitmq.spring.message.MessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRetryableMessageListener.class);

    /**
     * 幂等可重试消费。抛出异常会自动重试
     *
     * @param message      消息体
     * @param properties   消息属性
     * @param retryContext 重试上下文
     * @return 消费结果，{@link ConsumeResult#OK} 表示消费成功，{@link ConsumeResult#FAIL} 表示消费失败，消息将会丢失，{@link ConsumeResult#RETRY} 表示消息会重新入队消费
     * @throws Exception 消费过程中发生的异常
     */
    public abstract ConsumeResult idempotentConsumeRetryable(T message, MessageProperties properties, RetryContext retryContext) throws Exception;

    /**
     * 所有重试操作都失败后执行本方法，通常用于日志记录操作，可以根据 {@link RetryContext#getLastThrowable()} 决定消费结果
     *
     * @param message      消息体
     * @param properties   消息属性
     * @param retryContext 重试上下文
     * @return 消费结果，{@link ConsumeResult#OK} 表示消费成功，{@link ConsumeResult#FAIL} 表示消费失败，消息将会丢失，{@link ConsumeResult#RETRY} 表示消息会重新入队消费
     */
    public abstract ConsumeResult recoverForAllRetryFail(T message, MessageProperties properties, RetryContext retryContext);

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        ConsumeResult result = ConsumeResult.FAIL;
        final MessageProperties prop = message.getMessageProperties();
        long deliveryTag = prop.getDeliveryTag();
        try {
            @SuppressWarnings("unchecked") final T msg = (T) messageConverter.fromMessage(message);
            LOGGER.info("消息[deliveryTag = {}]实体转换成功", deliveryTag);
            result = retryTemplate.execute(
                    retryContext -> this.idempotentConsumeRetryable(msg, prop, retryContext),
                    retryContext -> this.recoverForAllRetryFail(msg, prop, retryContext)
            );
        } finally {
            if (result == null) {
                result = ConsumeResult.FAIL;
            }
            switch (result) {
                case RETRY:
                    LOGGER.info("消息[deliveryTag = {}]消费失败，重新入队", deliveryTag);
                    channel.basicReject(deliveryTag, true);
                    break;
                case FAIL:
                    LOGGER.info("消息[deliveryTag = {}]消费失败，丢弃消息", deliveryTag);
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