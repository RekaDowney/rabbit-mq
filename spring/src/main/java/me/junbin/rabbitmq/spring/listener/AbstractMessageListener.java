package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.rabbitmq.spring.message.MessageEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:52
 * @description :
 */
public abstract class AbstractMessageListener<T extends MessageEntity> implements ChannelAwareMessageListener {

    @Autowired
    protected MessageConverter messageConverter;

    abstract void handleMessage(T message, MessageProperties properties, Channel channel) throws Exception;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        @SuppressWarnings("unchecked")
        T msg = (T) messageConverter.fromMessage(message);
        MessageProperties properties = message.getMessageProperties();
        this.handleMessage(msg, properties, channel);
    }

}