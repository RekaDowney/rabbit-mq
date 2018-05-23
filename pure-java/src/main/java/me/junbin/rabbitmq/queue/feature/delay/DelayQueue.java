package me.junbin.rabbitmq.queue.feature.delay;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 11:50
 * @description :
 */
public class DelayQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelayQueue.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            Map<String, Object> arguments = new HashMap<>();
            // 死信
            // 3秒中内未被消费，则通过 DELAY_EXCHANGE 交换机以 DELAY_ROUTING_KEY 路由键转发消息
            arguments.put(FEATURE_X_MESSAGE_TTL, TimeUnit.SECONDS.toMillis(3));
            arguments.put(FEATURE_DEAD_LETTER_EXCHANGE, HANDLE_DELAY_EXCHANGE);
            arguments.put(FEATURE_DEAD_LETTER_ROUNTING_KEY, HANDLE_DELAY_ROUTING_KEY);
            LOGGER.info("声明延迟交换机 {}，延迟队列 {}（队列参数：{}），延迟路由键 {}", DELAY_EXCHANGE, DELAY_QUEUE, arguments, DELAY_ROUTING_KEY);
            channel.exchangeDeclare(DELAY_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare(DELAY_QUEUE, true, false, false, arguments);
            channel.queueBind(DELAY_QUEUE, DELAY_EXCHANGE, DELAY_ROUTING_KEY);

            LOGGER.info("声明延迟处理交换机 {}，延迟处理队列 {}，延迟处理路由键 {} 以处理延迟队列的消息", HANDLE_DELAY_EXCHANGE, HANDLE_DELAY_QUEUE, HANDLE_DELAY_ROUTING_KEY);
            channel.exchangeDeclare(HANDLE_DELAY_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare(HANDLE_DELAY_QUEUE, true, false, false, null);
            channel.queueBind(HANDLE_DELAY_QUEUE, HANDLE_DELAY_EXCHANGE, HANDLE_DELAY_ROUTING_KEY);

            // 消费者开始监听 延迟处理队列。3秒后消费者才开始收到消息
            channel.basicConsume(HANDLE_DELAY_QUEUE, true, MqUtils.newConsumer(channel, HANDLE_DELAY_QUEUE,
                    messageInfo -> LOGGER.info("队列 {} 开始处理消息 {}", messageInfo.getQueueName(), messageInfo.getBodyString())));

            // 消息从 延迟交换机 发布，到期后通过 延迟处理交换机 通过 延迟处理路由键 发布到 延迟处理队列。最后被消费者消费

            String pattern = "第%d条消息";
            for (int i = 1; i < 6; i++) {
                String message = String.format(pattern, i);
                LOGGER.info("往 {} 延迟交换机发布消息 --> {}", DELAY_EXCHANGE, message);
                channel.basicPublish(DELAY_EXCHANGE, DELAY_ROUTING_KEY, false, false, null, MqUtils.utf8Data(message));
            }

            // 休眠3秒后再发布一条新消息
            TimeUnit.SECONDS.sleep(3);

            String message = "又来了一条消息";
            LOGGER.info("往 {} 延迟交换机发布消息 --> {}", DELAY_EXCHANGE, message);
            channel.basicPublish(DELAY_EXCHANGE, DELAY_ROUTING_KEY, false, false, null, MqUtils.utf8Data(message));

            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(10));
        }
    }

}