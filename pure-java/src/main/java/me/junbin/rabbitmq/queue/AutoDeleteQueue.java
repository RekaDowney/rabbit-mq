package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/10 9:20
 * @description : 拥有 autoDelete 特性的队列会在最后一个相连的消费者断开连接后自动删除
 * 注意：如果从未有消费者与该队列相连，那么该队列不会自动删除
 */
public class AutoDeleteQueue {

    private static final String AD_QUEUE = "queue.ad";
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoDeleteQueue.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.debug("声明 AUTO_DELETE 特性的队列 {}", AD_QUEUE);
            channel.queueDeclare(AD_QUEUE, false, false, true, null);

            LOGGER.debug("为 {} 绑定消费者", AD_QUEUE);
            channel.basicConsume(AD_QUEUE, true, MqUtils.newConsumer(channel, body -> LOGGER.info("{} 队列消费消息 -> {}", AD_QUEUE, MqUtils.utf8String(body))));

            LOGGER.debug("通过默认路由器发布消息到 {}", AD_QUEUE);
            channel.basicPublish(MqConstant.EMPTY, AD_QUEUE, null, MqUtils.utf8Data("来自默认交换机"));

            LOGGER.debug("休眠10秒后结束程序，在这期间可以查看 RabbitMQ 中是否存在 {} 队列，该队列在断开 RabbitMQ 连接后将自动删除", AD_QUEUE);
            TimeUnit.SECONDS.sleep(10);
        }
    }

}