package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.converter.RabbitUtf8GsonMessageConverter;
import me.junbin.rabbitmq.spring.message.FanoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/6/1 18:23
 * @description :
 */
@Service("simpleQueueListener")
public class SimpleQueueListener {

    @Autowired
    private RabbitUtf8GsonMessageConverter messageConverter;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleQueueListener.class);

    // 参考： org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter.invokeListenerMethod()#391

    // org.springframework.amqp.rabbit.listener.exception.ListenerExecutionFailedException:
    // Failed to invoke target method 'handleMessage' with argument type =
    // [class me.junbin.rabbitmq.spring.message.FanoutMessage], value = [{FanoutMessage{id=2, body='第2条消息'}}]

    // Caused by: java.lang.NoSuchMethodException: me.junbin.rabbitmq.spring.listener.SimpleQueueListener.handleMessage(me.junbin.rabbitmq.spring.message.FanoutMessage)
    public void handleMessage(Message message, Channel channel) throws Exception {
        FanoutMessage fanoutMessage = messageConverter.fromMessage(message, FanoutMessage.class);
        LOGGER.info("收到消息：{}", Gsonor.SIMPLE.toJson(fanoutMessage));
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    // 正确的 method
    public void handleMessage(FanoutMessage message) throws Exception {
        LOGGER.info("收到消息：{}", Gsonor.SIMPLE.toJson(message));
    }


}