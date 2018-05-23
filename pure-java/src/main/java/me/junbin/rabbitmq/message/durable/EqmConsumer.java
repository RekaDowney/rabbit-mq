package me.junbin.rabbitmq.message.durable;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.EQM.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 21:45
 * @description : 交换机、队列、消息持久化
 */
public class EqmConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EqmConsumer.class);

    public static void main(String[] args) throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明持久化交换机 {}，持久化队列 {}，路由键 {}", EXCHANGE, QUEUE, ROUTING_KEY);
        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);
        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

        int prefetchCount = 1;
        LOGGER.info("指定当前信道的每一个消费者同一时间最多消费{}个消息", prefetchCount);
        channel.basicQos(prefetchCount, true);

        // 当消费完 1 个或者 2 个消息时，systemctl stop rabbitmq-server.service
        // 然后查看消息消费情况
        // systemctl start rabbitmq-server.service 之后再重启消费者，看消费情况
        channel.basicConsume(QUEUE, false, MqUtils.newConsumer(channel, QUEUE, mi -> {
            String queueName = mi.getQueueName();
            String bodyString = mi.getBodyString();
            LOGGER.info("开始消费队列 {} 消息 --> {}", queueName, bodyString);
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ignored) {
            }
            channel.basicAck(mi.getEnvelope().getDeliveryTag(), false);
            LOGGER.info("消息[{}]消费完毕", bodyString);
        }));

    }

}