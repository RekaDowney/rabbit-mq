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
 * @createDate : 2018/5/23 9:24
 * @description :
 */
public class TtlProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TtlProducer.class);

    public static void main(String[] args) throws Exception {
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

        String pattern = "第%d条消息";
        for (int i = 1; i < 5; i++) {
            String message = String.format(pattern, i);
            LOGGER.info("发布消息 --> {}", message);
            channel.basicPublish(DD_EXCHANGE, DD_TTL_ROUTING_KEY, false, false, null, MqUtils.utf8Data(message));
        }
    }

}