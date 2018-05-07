package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;
import static me.junbin.rabbitmq.constant.MqConstant.*;


/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/5/7 17:07
 * @description : 内部交换机无法发布消息，只能够通过 {@link Channel#exchangeBind(String, String, String)} 方法将内部交换机绑定到外部交换机上，
 * 随后通过外部交换机发布消息，以 外部交换机 -> 内部交换机 -> 队列 的消息流方式进行消息消费
 *
 * 如果使用了 {@link Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])} 方法通过内部交换机发布消息，那么程序不会报错，但会
 * 导致后续的所有消息无法正确路由到队列上进行消费
 */
public class InternalExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalExchange.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();
            // 内部交换机（客户端不可直接通过该交换机发布消息）
            channel.exchangeDeclare(INTERNAL_EXCHANGE, FANOUT, false, false, true, null);
            channel.queueDeclare(INTERNAL_QUEUE, false, false, false, null);
            channel.queueBind(INTERNAL_QUEUE, INTERNAL_EXCHANGE, EMPTY);

            // 外部交换机（客户端发送到该交换机的消息都直接转到内部交换机上）
            channel.exchangeDeclare(EXTERNAL_EXCHANGE, FANOUT, false, false, false, null);
            channel.exchangeBind(INTERNAL_EXCHANGE, EXTERNAL_EXCHANGE, EMPTY);

            LOGGER.info("将类型为{}的{}内部交换机与类型为{}的{}外部交换机绑定在一起", FANOUT.getType(), INTERNAL_EXCHANGE, FANOUT.getType(), EXTERNAL_EXCHANGE);

            channel.basicConsume(INTERNAL_QUEUE, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.info("{} 队列消费消息 --> {}", INTERNAL_QUEUE, MqUtils.utf8String(body));
                }
            });


            String message = String.format("通过外部交换机 %s 发布消息", EXTERNAL_EXCHANGE);
            LOGGER.debug(message);
            channel.basicPublish(EXTERNAL_EXCHANGE, EMPTY, null, MqUtils.utf8Data(message));

            // 如果使用 basicPublish(INTERNAL_EXCHANGE) 方式发布消息，虽然 RabbitMQ 没有报错，但实际上会造成程序逻辑错误
            // 此时导致当前以及后续的所有消息都无法路由到队列上进行消费（消息丢失）
/*
            message = String.format("通过内部交换机 %s 发布消息", INTERNAL_EXCHANGE);
            LOGGER.debug(message);
            channel.basicPublish(INTERNAL_EXCHANGE, EMPTY, null, MqUtils.utf8Data(message));
*/

            message = String.format("通过外部交换机 %s 发布消息", EXTERNAL_EXCHANGE);
            LOGGER.debug(message);
            channel.basicPublish(EXTERNAL_EXCHANGE, EMPTY, null, MqUtils.utf8Data(message));

            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(10));

        }
    }

}