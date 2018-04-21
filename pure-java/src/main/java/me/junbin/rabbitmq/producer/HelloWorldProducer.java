package me.junbin.rabbitmq.producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/4/17 23:21
 * @description :
 */
public class HelloWorldProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldProducer.class);

    public void produce() throws IOException, TimeoutException {
        // 生产者生产完毕消息后就可以关闭 channel 和 connection 以结束生产者线程
        try (Connection connection = MqConnectionFactory.newConnection();
             // 实际上当 connection 关闭时，channel 也会关闭，所以这里可以不将 channel 的定义写到 resource 定义中
             Channel channel = connection.createChannel()) {
            LOGGER.info("声明 {} 队列", MqConstant.HELLO_WORLD_QUEUE);
            // queueDeclare 具有幂等性（幂等性的特点是任意多次操作的结果与一次执行的结果是一致的）
            // 如果 hello-world 队列不存在则自动创建 hello-world 队列，必须保证用户有权限创建队列
            channel.queueDeclare(MqConstant.HELLO_WORLD_QUEUE, false, false, false, null);
            String message = "Hello World From Java Client";
            LOGGER.info("生产者：发布消息 {}", message);

            channel.basicPublish(MqConstant.EMPTY, MqConstant.HELLO_WORLD_QUEUE, false, null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

}