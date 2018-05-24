package me.junbin.rabbitmq.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.junbin.rabbitmq.constant.MqConstant.Rpc.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/24 9:48
 * @description : RPC 同样要求满足幂等性质
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private static int fibonacci(int n) {
        if (n == 0 || n == 1) {
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    public static void main(String[] args) throws Exception {
        AtomicBoolean flag = new AtomicBoolean(false);
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明 RPC 服务端（消费者，回调时走队列），{} 交换机，{} 队列", EXCHANGE, QUEUE);
        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, false, null);
        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

        channel.basicQos(1, true);

        channel.basicConsume(QUEUE, false, MqUtils.newConsumer(channel, QUEUE,
                mi -> {

                    // 计算完毕斐波那契数列之后，将计算结果根据 correlationId 发送到 RPC 回调队列
                    // 获取消息中的数据
                    int num = Integer.parseInt(mi.getUtf8Body());
                    LOGGER.info("开始处理消息（计算斐波那契数列） --> {}", num);
                    // 计算结果
                    String result = Integer.toString(fibonacci(num));
                    LOGGER.info("计算完毕，fibonacci({}) == {}，准备回调", num, result);
                    // 回调
                    AMQP.BasicProperties properties = mi.getProperties();
                    // 指定 correlationId
                    AMQP.BasicProperties replyProp = new AMQP.BasicProperties.Builder().correlationId(properties.getCorrelationId()).build();
                    // 通过默认路由器（默认路由器将每个队列的名称作为路由键，并且默认路由器的 DIRECT 类型的，因此 basicPublish("", "queue-name") 最终会将消息传递到指定队列上）
                    channel.basicPublish(MqConstant.DEFAULT_EXCHANGE, properties.getReplyTo(), false, false, replyProp, MqUtils.utf8Data(result));

                    // 迭代式发送一条假消息（即无效的 correlationId）
                    if (flag.getAndSet(!flag.get())) {
                        AMQP.BasicProperties invalidProp = new AMQP.BasicProperties.Builder().correlationId(UUID.randomUUID().toString()).build();
                        String message = RandomStringUtils.randomAlphanumeric(4);
                        LOGGER.info("发布一条假消息 {} ", message);
                        channel.basicPublish(MqConstant.DEFAULT_EXCHANGE, properties.getReplyTo(), invalidProp, MqUtils.utf8Data(message));
                    }

                    LOGGER.info("回调消息已发出，开始应答当前消息");
                    // 消息应答
                    channel.basicAck(mi.getEnvelope().getDeliveryTag(), false);
                }));
    }

}