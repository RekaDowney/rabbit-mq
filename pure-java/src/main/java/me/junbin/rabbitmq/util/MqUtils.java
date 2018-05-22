package me.junbin.rabbitmq.util;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.util.function.ConsumerAction;
import me.junbin.rabbitmq.util.function.SimpleConsumerAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 21:23
 * @description :
 */
public abstract class MqUtils {

    public static byte[] utf8Data(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String utf8String(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    public static Consumer newConsumer(final Channel channel, final SimpleConsumerAction action) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                action.handle(body);
            }
        };
    }

    public static Consumer newConsumer(final Channel channel, final String queueName, final ConsumerAction consumerAction) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                consumerAction.handle(new ConsumerAction.MessageInfo(queueName, consumerTag, envelope, properties, body));
            }
        };
    }

}