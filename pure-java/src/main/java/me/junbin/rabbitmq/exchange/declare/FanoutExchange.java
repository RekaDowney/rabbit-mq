package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
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
 * @description :
 */
public class FanoutExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutExchange.class);

    public static void main(String[] args) throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明交换机：{}，类型为：{}", MqConstant.FANOUT_EXCHANGE, BuiltinExchangeType.FANOUT.getType());
        channel.exchangeDeclare(MqConstant.FANOUT_EXCHANGE, BuiltinExchangeType.FANOUT, false, false, null);

        LOGGER.info("声明队列：{}", MqConstant.FANOUT_QUEUE);
        channel.queueDeclare(MqConstant.FANOUT_QUEUE, false, false, false, null);

        // fanout 类型的交换机将会忽略路由键，因此路由键可以为 null
        LOGGER.info("将队列{}与交换机{}绑定在一起，路由键为空字符串（fanout类型跟路由键无关，但API要求路由键非 null）", MqConstant.FANOUT_QUEUE, MqConstant.FANOUT_EXCHANGE);
        channel.queueBind(MqConstant.FANOUT_QUEUE, MqConstant.FANOUT_EXCHANGE, MqConstant.EMPTY);

        LOGGER.debug("消费者开始监听队列{}", MqConstant.FANOUT_QUEUE);
        channel.basicConsume(MqConstant.FANOUT_QUEUE, true, new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                LOGGER.info("消费消息 --> {}", MqUtils.utf8String(body));
            }

        });

        LOGGER.debug("开启生产者线程，延迟1秒发布第一条消息，每隔2秒发布一条消息");
        new Timer("生产者定时线程").scheduleAtFixedRate(new TimerTask() {

            private int counter = 1;
            private static final String messageTemplate = "第%d条消息";

            @Override
            public void run() {
                try {
                    String message = String.format(messageTemplate, counter++);
                    LOGGER.debug("发布消息：{}", message);
                    // 这里路由键要求非 null
                    channel.basicPublish(MqConstant.FANOUT_EXCHANGE, MqConstant.EMPTY, null, MqUtils.utf8Data(message));
                } catch (IOException e) {
                    LOGGER.error("发布消息失败", e);
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(2));

    }

}