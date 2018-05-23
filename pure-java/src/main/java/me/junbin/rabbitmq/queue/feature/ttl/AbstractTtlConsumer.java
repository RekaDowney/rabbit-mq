package me.junbin.rabbitmq.queue.feature.ttl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
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
 * @createDate : 2018/5/23 9:49
 * @description : 对于非 FANOUT 类型交换机——队列——消费者结构，RabbitMQ 采用轮询方式将消息发送给队列（比如：消息1发给消费者1，消息2发给消费者2）
 * 对于 FANOUT 类型交换机——队列——消费者结构，RabbitMQ 直接发送给所有关联的消费者（比如：消息1发给消费者1，消息1发给消费者2）
 * 综上，消息存在重复消费的问题，比如消息1发给消费者1，消费者1没有即时 ack，此时消费者2 空闲，因此消息1也会发给消费者2，此时消息1就可能被消费者1和消费者2同时成功消费
 */
public abstract class AbstractTtlConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTtlConsumer.class);

    protected final String consumer;
    protected final long secondToSleepPerConsume;

    protected AbstractTtlConsumer(String consumer, long secondToSleepPerConsume) {
        this.consumer = consumer;
        this.secondToSleepPerConsume = secondToSleepPerConsume;
    }

    public void startConsume() throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.debug("声明类型为{}的{}交换机", BuiltinExchangeType.DIRECT.getType(), DD_EXCHANGE);
        channel.exchangeDeclare(DD_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);

        Map<String, Object> arguments = new HashMap<>();
        // TTL 队列，消息 TTL 为 3 秒，超过 3 秒没有被消费者接收则从队列中删除该消息
        arguments.put(MqConstant.FEATURE_X_MESSAGE_TTL, TimeUnit.SECONDS.toMillis(10));
        LOGGER.debug("声明{}持久化队列，队列参数为{}", DD_TTL_QUEUE, arguments);
        channel.queueDeclare(DD_TTL_QUEUE, true, false, false, arguments);

        channel.queueBind(DD_TTL_QUEUE, DD_EXCHANGE, DD_TTL_ROUTING_KEY);

        channel.basicQos(1, true);

        channel.basicConsume(DD_TTL_QUEUE, false, MqUtils.newConsumer(channel, DD_TTL_QUEUE, messageInfo -> {
            try {
                TimeUnit.SECONDS.sleep(this.secondToSleepPerConsume);
            } catch (InterruptedException ignored) {
            }
            String queueName = messageInfo.getQueueName();
            long deliveryTag = messageInfo.getEnvelope().getDeliveryTag();
            LOGGER.info("{} 队列 {} 消费消息 --> {}", this.consumer, queueName, messageInfo.getBodyString());
            channel.basicAck(deliveryTag, false);
        }));
    }

}