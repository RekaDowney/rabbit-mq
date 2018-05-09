package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/7 23:02
 * @description : 排他队列是基于连接 {@link Connection} 的，同一连接的不同信道 {@link Channel}
 * 可以访问相同名称的排他队列。
 * ⭐ 当某个排他队列被创建的之后，其他连接无法创建相同名称的（排他）队列
 * ⭐ 排他队列在连接断开的时候就会自动删除，即使设置了 durable 为 {@literal true} 也会被删除
 */
public class ExclusiveQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExclusiveQueue.class);

    public static void main(String[] args) throws Exception {
        runExclusiveQueue("exclusive1", EXCLUSIVE1_QUEUE, DIRECT_EXCLUSIVE1_ROUTING_KEY);
        runExclusiveQueue("exclusive2", EXCLUSIVE2_QUEUE, DIRECT_EXCLUSIVE2_ROUTING_KEY);
    }

    private static void runExclusiveQueue(final String threadName, final String queueName, final String routingKey) throws Exception {
        Thread thread = new Thread(() -> {
            try (Connection connection = MqConnectionFactory.newConnection()) {
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(DIRECT_EXCLUSIVE_EXCHANGE, BuiltinExchangeType.DIRECT, false, false, null);
                channel.queueDeclare(queueName, false, true, false, null);
                channel.queueBind(queueName, DIRECT_EXCHANGE, routingKey, null);

                channel.basicConsume(queueName, true, MqUtils.newConsumer(channel, body -> LOGGER.info("线程 {} 队列 {} 消费消息 --> {}", threadName, queueName, MqUtils.utf8String(body))));

                for (int i = 1; i < 4; i++) {
                    String message = String.format("第%s条消息", i);
                    LOGGER.debug("发布消息： {}", message);
                    channel.basicPublish(DIRECT_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
                }

                Thread.currentThread().join(TimeUnit.SECONDS.toMillis(5));
            } catch (Exception e) {
                LOGGER.error(String.format("线程%s发生异常", threadName), e);
            }
        });
        thread.setName(threadName);
        thread.start();
    }

}