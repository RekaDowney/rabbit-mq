package me.junbin.rabbitmq.consumer.multi;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 16:38
 * @description :
 */
public class CommonConsumer {

    private final String consumerName;
    private final String exchange;
    private final String queue;
    private final String routingKey;
    private final BuiltinExchangeType type;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonConsumer.class);

    public CommonConsumer(String consumerName, String exchange, String queue, String routingKey, BuiltinExchangeType type) {
        this.consumerName = consumerName;
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
        this.type = type;
    }

    public void startConsume() throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 每次消费1个消息
        channel.basicQos(1, true);

        channel.exchangeDeclare(exchange, type, false, true, null);
        channel.queueDeclare(queue, false, false, true, null);
        channel.queueBind(queue, exchange, routingKey);

        channel.basicConsume(queue, true, MqUtils.newConsumer(channel, queue,
                mi -> LOGGER.info("类型：{}， {} 消费队列 {} 的消息 --> {}", type.getType(), consumerName, mi.getQueueName(), mi.getBodyString())));

/*
        channel.basicConsume(queue, false, MqUtils.newConsumer(channel, queue,
                mi -> {
                    channel.basicAck(mi.getEnvelope().getDeliveryTag(), false);
                    LOGGER.info("类型：{}， {} 消费队列 {} 的消息 --> {}", type.getType(), consumerName, mi.getQueueName(), mi.getBodyString());
                }));
*/

    }

}