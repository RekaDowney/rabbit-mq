package me.junbin.rabbitmq.queue.feature;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/22 23:26
 * @description : x-expires 特性表示队列在指定时间内未被访问则自动删除，此时 durable 特性将失效
 */
public class ExpireQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpireQueue.class);

    public static void main(String[] args) throws Exception {

        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(DD_EXCHANGE, BuiltinExchangeType.DIRECT, true);
            Map<String, Object> arguments = new HashMap<>();
            arguments.put(FEATURE_X_EXPIRES, TimeUnit.SECONDS.toMillis(3));
            channel.queueDeclare(DD_EXPIRE_QUEUE, true, false, false, arguments);
            LOGGER.info("声明{}持久化队列，队列参数为{}，该队列3秒钟未被访问（basicGet、basicConsume）则被删除，此时durable不起作用", DD_EXPIRE_QUEUE, arguments);

            // 断言队列存在
            assert channel.queueDeclarePassive(DD_EXPIRE_QUEUE) != null;

            TimeUnit.SECONDS.sleep(5);
            LOGGER.info("队列{}已被自动删除。可以登陆RabbitMQ管理后台查看", DD_EXPIRE_QUEUE);

            // 断言队列不存在
            boolean notExists = false;
            try {
                channel.queueDeclarePassive(DD_EXPIRE_QUEUE);
            } catch (IOException e) {
                notExists = true;
            }
            assert notExists;

            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(10));
        }

    }

}