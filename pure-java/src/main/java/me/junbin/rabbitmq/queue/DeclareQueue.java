package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/14 10:21
 * @description : {@link Channel#queueDeclare()} 方法生成的队列名称格式为 amq.gen-${22个随机字符}
 */
public class DeclareQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeclareQueue.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            // 某一次生成的队列名称为 amq.gen-SDtGxWTcXzHCYqLcROlv0A
            String queueName = channel.queueDeclare().getQueue();
            assert !Objects.equals(queueName, MqConstant.EMPTY);
            LOGGER.info("调用 com.rabbitmq.client.Channel.queueDeclare() 非持久化，排他并且自动删除的队列。生成后的队列名称为：{}", queueName);
        }
    }

}