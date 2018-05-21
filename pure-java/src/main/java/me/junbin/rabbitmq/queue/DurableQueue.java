package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/10 10:01
 * @description : 持久化队列在 RabbitMQ 重启之后依然存在，非持久化队列在 RabbitMQ 重启后将丢失
 */
public class DurableQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DurableQueue.class);
    private static final String DURABLE_QUEUE = "queue.durable";

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {

            Channel channel = connection.createChannel();

            LOGGER.info("声明持久化队列 {}", DURABLE_QUEUE);
            channel.queueDeclare(DURABLE_QUEUE, true, false, false, null);
        }
    }

}