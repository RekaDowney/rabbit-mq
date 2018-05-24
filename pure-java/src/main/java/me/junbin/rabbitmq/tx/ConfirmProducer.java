package me.junbin.rabbitmq.tx;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.Confirm.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/24 23:05
 * @description : 消息发布者异步确认机制保证消息成功发送到 MQ
 * 参考：<a href="https://blog.csdn.net/u013256816/article/details/55515234">RabbitMQ之消息确认机制（事务+Confirm）</a>
 * 消息发布者异步确认机制相比事务机制，在性能上会好上好几倍
 */
public class ConfirmProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmProducer.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.info("消息发布者确认机制确认消息发布成功。声明 {} 交换机，{} 队列", EXCHANGE, QUEUE);
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, false, null);
            channel.queueDeclare(QUEUE, true, false, false, null);
            channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

//            channel.waitForConfirmsOrDie();

            // 开启消息发布确认机制
            channel.confirmSelect();
            SortedSet<Long> confirmSet = Collections.synchronizedSortedSet(new TreeSet<Long>());
            // 添加确认监听器（异步支持）
            channel.addConfirmListener(new ConfirmListener() {

                public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                    if (multiple) {
                        confirmSet.headSet(deliveryTag + 1).clear();
                    } else {
                        confirmSet.remove(deliveryTag);
                    }
                }

                public void handleNack(long deliveryTag, boolean multiple) throws IOException {
                    throw new IOException(String.format("消息发布失败：deliveryTag=%s，multiple=%b", deliveryTag, multiple));
                }
            });


            String pattern = "第%d条消息";
            for (int i = 1; i < 6; i++) {
                // 获取下一条消息的 deliveryTag
                long seqNo = channel.getNextPublishSeqNo();
                String message = String.format(pattern, i);
                LOGGER.info("发布消息 --> {}", message);
                channel.basicPublish(EXCHANGE, ROUTING_KEY, false, false,
                        MqConstant.MessageProperties.PERSISTENT, MqUtils.utf8Data(message));
                // 将 deliveryTag 加入到消息发布待确认集合中
                confirmSet.add(seqNo);
            }

            TimeUnit.SECONDS.sleep(3);
            if (!confirmSet.isEmpty()) {
                throw new RuntimeException(String.format("消息发布失败，MQ无法收到部分消息（deliveryTag为%s）", confirmSet));
            }
        }
    }

}