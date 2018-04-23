package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 21:18
 * @description : direct 类型交换机与路由键无关，该类交换机会将收到的所有消息都推送到路由键一致的与之相绑定的队列中
 * 特别注意：路由键最大长度为 255 个字节
 * 拥有 durable 特性的交换机不会在 RabbitMQ 重启时消失
 */
public class DirectExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectExchange.class);

    public static void main(String[] args) throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明交换机：{}，类型为：{}", MqConstant.DIRECT_EXCHANGE, BuiltinExchangeType.DIRECT.getType());
        channel.exchangeDeclare(MqConstant.DIRECT_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);

        LOGGER.info("声明队列：{}", MqConstant.DIRECT_QUEUE);
        channel.queueDeclare(MqConstant.DIRECT_QUEUE, false, false, false, null);

        LOGGER.info("将队列{}与交换机{}绑定在一起，路由键为：{}", MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, MqConstant.DIRECT_ROUTING_KEY);
        channel.queueBind(MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, MqConstant.DIRECT_ROUTING_KEY);

        LOGGER.debug("消费者开始监听队列{}", MqConstant.DIRECT_QUEUE);
        channel.basicConsume(MqConstant.DIRECT_QUEUE, true, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                LOGGER.info("消费消息 --> {}", MqUtils.utf8String(body));
            }

        });

        LOGGER.debug("开启生产者线程，路由键为：{}，延迟1秒发布第一条消息，每隔2秒发布一条消息", MqConstant.DIRECT_ROUTING_KEY);
        new Timer(String.format("路由键为%s的生产者定时线程", MqConstant.DIRECT_ROUTING_KEY)).scheduleAtFixedRate(new TimerTask() {

            private int counter = 1;
            private static final String messageTemplate = "第%d条消息";

            @Override
            public void run() {
                try {
                    String message = String.format(messageTemplate, counter++);
                    LOGGER.debug("发布消息：{}", message);
                    // 这里路由键要求非 null
                    channel.basicPublish(MqConstant.DIRECT_EXCHANGE, MqConstant.DIRECT_ROUTING_KEY, null, MqUtils.utf8Data(message));
                } catch (IOException e) {
                    LOGGER.error("发布消息失败", e);
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(2));

        LOGGER.debug("开启生产者线程，路由键为随机字符串，延迟1秒发布第一条消息，每隔5秒发布一条消息");
        new Timer("路由键为随机字符串的生产者定时线程").scheduleAtFixedRate(new TimerTask() {

            private int counter = 1;
            private static final String messageTemplate = "第%d条消息（路由键随机）";

            @Override
            public void run() {
                try {
                    String message = String.format(messageTemplate, counter++);
                    String routingKey = RandomStringUtils.randomAlphabetic(4, 8);
                    LOGGER.debug("以路由键{}发布消息：{}", routingKey, message);
                    channel.basicPublish(MqConstant.DIRECT_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
                } catch (IOException e) {
                    LOGGER.error("发布消息失败", e);
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(5));

    }

}