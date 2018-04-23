package me.junbin.rabbitmq.exchange.exists;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/22 17:44
 * @description :
 */
public class ExchangeExistence {

    // 每次创建新的 vhost，默认都将创建以下 7 个交换机
    // direct 类型，durable 特性，仅支持消息发布操作，不支持对该交换机的其他任何操作（包括存在性检测、删除、与其他交换机或者队列的绑定与解绑）
    private static final String EMPTY = "";
    // fanout 类型，durable 特性
    private static final String AMQ_FANOUT = "amq.fanout";
    // direct 类型，durable 特性
    private static final String AMQ_DIRECT = "amq.direct";
    // topic 类型，durable 特性
    private static final String AMQ_TOPIC = "amq.topic";
    // headers 类型，durable 特性
    private static final String AMQ_HEADERS = "amq.headers";
    // headers 类型，durable 特性
    private static final String AMQ_MATCH = "amq.match";
    // topic 类型，durable、internal 特性，不支持消息发布操作
    private static final String AMQ_RABBITMQ_TRACE = "amq.rabbitmq.trace";

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            AMQP.Exchange.DeclareOk existence;

            existence = channel.exchangeDeclarePassive(AMQ_FANOUT);
            assert existence != null;

            existence = channel.exchangeDeclarePassive(AMQ_DIRECT);
            assert existence != null;

            existence = channel.exchangeDeclarePassive(AMQ_TOPIC);
            assert existence != null;

            existence = channel.exchangeDeclarePassive(AMQ_HEADERS);
            assert existence != null;

            existence = channel.exchangeDeclarePassive(AMQ_MATCH);
            assert existence != null;

            existence = channel.exchangeDeclarePassive(AMQ_RABBITMQ_TRACE);
            assert existence != null;

            // 默认交换机不允许访问
            // com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=403, reply-text=ACCESS_REFUSED - operation not permitted on the default exchange, class-id=40, method-id=10)
            existence = channel.exchangeDeclarePassive(EMPTY);
        }

    }

}