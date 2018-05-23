package me.junbin.rabbitmq.message.durable;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.junbin.rabbitmq.constant.MqConstant.EQM.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 21:46
 * @description :
 */
public class EqmProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EqmProducer.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.info("声明持久化交换机 {}，持久化队列 {}，路由键 {}", EXCHANGE, QUEUE, ROUTING_KEY);
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
            channel.queueDeclare(QUEUE, true, false, false, null);
            channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

            String pattern = "第%d条消息";
            for (int i = 1; i < 6; i++) {
                String message = String.format(pattern, i);
                LOGGER.info("发布消息 --> {}", message);
                channel.basicPublish(EXCHANGE, ROUTING_KEY, false, false,
                        MqConstant.MessageProperties.PERSISTENT, MqUtils.utf8Data(message));
            }
        }
    }

}