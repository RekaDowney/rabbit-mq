package me.junbin.rabbitmq.consumer;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/17 23:34
 * @description :
 */
public class HelloWorldConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldConsumer.class);

    public void consume() throws IOException, TimeoutException {
/*
        try (Connection connection = MqConnectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
*/
        // 消费者不可以直接关闭 channel 和 connection，因为消费者线程必须等待消息到来后才能够进行消费
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明 {} 队列", MqConstant.HELLO_WORLD_QUEUE);
        // 如果 hello-world 队列不存在则自动创建 hello-world 队列，必须保证用户有权限创建队列
        channel.queueDeclare(MqConstant.HELLO_WORLD_QUEUE, false, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
                String message = new String(body, StandardCharsets.UTF_8);
                LOGGER.info("消费者：消费消息 {}", message);
            }
        };
        channel.basicConsume(MqConstant.HELLO_WORLD_QUEUE, true, consumer);
/*
        }
*/
    }

}