package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/10 14:19
 * @description : x-message-ttl 特性一旦遇到多消费者就会失灵
 */
public class TtlQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(TtlQueue.class);

    /**
     * 队列一旦声明，{@link Channel#queueDeclare(String, boolean, boolean, boolean, Map)}，最后一个参数 arguments 就无法再变更
     * Caused by: com.rabbitmq.client.ShutdownSignalException: channel error;
     * protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED -
     * inequivalent arg 'x-message-ttl' for queue 'queue.durable.direct' in vhost '/mq01':
     * received '2000' but current is '1000', class-id=50, method-id=10)
     */
    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.debug("声明类型为{}的{}交换机", BuiltinExchangeType.DIRECT.getType(), DD_EXCHANGE);
            channel.exchangeDeclare(DD_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);

            Map<String, Object> arguments = new HashMap<>();
            // TTL 队列，消息 TTL 为 3 秒，超过 3 秒没有被消费者接收则从队列中删除该消息
            arguments.put(MqConstant.FEATURE_X_MESSAGE_TTL, TimeUnit.SECONDS.toMillis(3));
            LOGGER.debug("声明{}持久化队列，队列参数为{}并指定消费者", DD_QUEUE, arguments);
            channel.queueDeclare(DD_QUEUE, true, false, false, arguments);

            channel.queueBind(DD_QUEUE, DD_EXCHANGE, DD_TTL_ROUTING_KEY);

            // 当前信道的每个消费者同一时间最多消费 1 个消息
            channel.basicQos(1, true);

//            Thread thread1 = newConsumerThread("线程1", 10);
//            Thread thread2 = newConsumerThread("线程2", 10);

            String pattern = "第%d条消息";
            for (int i = 1; i < 5; i++) {
//                new AMQP.BasicProperties.Builder().expiration()
                String message = String.format(pattern, i);
                LOGGER.info("发布消息 --> {}", message);
                channel.basicPublish(DD_EXCHANGE, DD_TTL_ROUTING_KEY, false, false, null, MqUtils.utf8Data(message));
            }

            TimeUnit.SECONDS.sleep(1);
//            thread1.start();
//            thread2.start();

            channel.basicConsume(DD_QUEUE, false, MqUtils.newConsumer(channel, DD_QUEUE, messageInfo -> {
                try {
                    TimeUnit.SECONDS.sleep(7);
                } catch (InterruptedException ignored) {
                }
                String queueName = messageInfo.getQueueName();
                long deliveryTag = messageInfo.getEnvelope().getDeliveryTag();
                LOGGER.info("消费者 1 队列 {} 消费消息 --> {}", queueName, messageInfo.getBodyString());
                channel.basicAck(deliveryTag, false);
            }));

            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(10));
        }
    }

    private static Thread newConsumerThread(final String threadName, final int sleepSecond) {
        return new Thread(() -> {
            try {
                Connection connection = MqConnectionFactory.newConnection();
                Channel channel = connection.createChannel();
                channel.basicConsume(DD_QUEUE, false, MqUtils.newConsumer(channel, DD_QUEUE, messageInfo -> {
                    String queueName = messageInfo.getQueueName();
                    LOGGER.info("{} 队列 {} 消费消息 --> {}", threadName, queueName, messageInfo.getBodyString());
                    long deliveryTag = messageInfo.getEnvelope().getDeliveryTag();
                    try {
                        TimeUnit.SECONDS.sleep(sleepSecond);
                    } catch (InterruptedException ignored) {
                    }
                    channel.basicAck(deliveryTag, false);
                }));
            } catch (TimeoutException | IOException e) {
                LOGGER.error("{} 消费消息失败。原因： --> {}", threadName, e.getCause().getMessage());
            }
        }, threadName);
    }

}