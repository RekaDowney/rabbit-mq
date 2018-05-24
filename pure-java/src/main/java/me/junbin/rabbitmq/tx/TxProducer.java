package me.junbin.rabbitmq.tx;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.junbin.rabbitmq.constant.MqConstant.Tx.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-05-24 22:49
 * @description : 通过事务可以保证消息成功发送到 MQ
 * 但是使用事务会降低 RabbitMQ 性能
 */
public class TxProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TxProducer.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.info("事务机制确认消息发布成功。声明 {} 交换机，{} 队列", EXCHANGE, QUEUE);
            channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, false, null);
            channel.queueDeclare(QUEUE, true, false, false, null);
            channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

            try {
                LOGGER.info("开启事务");
                // 开启事务（将当前 channel 设置成 transaction 模式）
                channel.txSelect();
                LOGGER.info("开启事务成功");

                // 发布消息
                String pattern = "第%d条消息";
                for (int i = 1; i < 6; i++) {
                    String message = String.format(pattern, i);
                    LOGGER.info("发布消息 --> {}", message);
/*
                    if (i == 3) {
                        // 这里的事务是针对当前的5个消息而言的，因此发生异常后之前发布的消息都会被收回
                        throw new Exception("发生异常");
                    }
*/
                    channel.basicPublish(EXCHANGE, ROUTING_KEY, false, false,
                            MqConstant.MessageProperties.PERSISTENT, MqUtils.utf8Data(message));
                }

                // 提交事务
                LOGGER.info("提交事务");
                channel.txCommit();
                LOGGER.info("提交事务完毕");
            } catch (Exception e) {
                LOGGER.error("发布消息时发生异常，开始回滚事务", e);
                // 回滚
                channel.txRollback();
                LOGGER.info("事务回滚成功");
            }
        }
    }

}