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
 * @createDate : 2018/5/23 15:59
 * @description :
 */
public class CommonProducer {

    private final String exchange;
    private final String queue;
    private final String routingKey;
    private final BuiltinExchangeType type;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonProducer.class);

    public CommonProducer(String exchange, String queue, String routingKey, BuiltinExchangeType type) {
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
        this.type = type;
    }

    public void startProduce() throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.info("测试 {} 类型的多消费者模式", type.getType());
            channel.exchangeDeclare(exchange, type, false, true, null);
            channel.queueDeclare(queue, false, false, true, null);
            channel.queueBind(queue, exchange, routingKey);

            String pattern = "第%d条消息";
            for (int i = 1; i < 6; i++) {
                String message = String.format(pattern, i);
                LOGGER.info("类型 {} 发布消息 --> {}", type.getType(), message);
                channel.basicPublish(exchange, routingKey, null, MqUtils.utf8Data(message));
            }
        }
    }

}