package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 23:01
 * @description : 交换机一旦声明，则无法变更类型type、持久化特性durable、自动删除特性autoDelete以及额外参数配置arguments。
 * 任何一项的变更都将导致 {@link com.rabbitmq.client.ShutdownSignalException}，错误状态码为 {@code 406}
 */
public class ExchangeConfigChange {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeConfigChange.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();
            String exchange = "exchange.test";
            LOGGER.info("初始声明：将交换机{}声明为{}类型，durable为{}，autoDelete为{}", exchange, BuiltinExchangeType.FANOUT.getType(), false, true);
            channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, false, true, null);

            // 交换机一旦声明，则不允许变更类型
            // com.rabbitmq.client.ShutdownSignalException: channel error;
            // protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED -
            // inequivalent arg 'type' for exchange 'exchange.test' in vhost '/mq01':
            // received 'direct' but current is 'fanout', class-id=40, method-id=10)
//            LOGGER.info("类型变更声明：将交换机{}的类型从{}变更为{}，durable、autoDelete 和 arguments 维持原样", exchange, BuiltinExchangeType.FANOUT.getType(), BuiltinExchangeType.DIRECT.getType());
//            channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, false, true, null);


            // 交换机一旦声明，则不允许变更 durable 持久化特性
            // com.rabbitmq.client.ShutdownSignalException: channel error;
            // protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED -
            // inequivalent arg 'durable' for exchange 'exchange.test' in vhost '/mq01':
            // received 'true' but current is 'false', class-id=40, method-id=10)
//            LOGGER.info("持久化特性变更声明：将交换机{}的持久化特性从{}变更为{}，type、autoDelete 和 arguments 维持原样", exchange, false, true);
//            channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, true, true, null);


            // 交换机一旦声明，则不允许变更 autoDelete 自动删除特性
            // com.rabbitmq.client.ShutdownSignalException: channel error;
            // protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED -
            // inequivalent arg 'auto_delete' for exchange 'exchange.test' in vhost '/mq01':
            // received 'false' but current is 'true', class-id=40, method-id=10)
//            LOGGER.info("自动删除特性变更声明：将交换机{}的自动删除特性从{}变更为{}，type、durable 和 arguments 维持原样", exchange, true, false);
//            channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, false, false, null);


            // 交换机一旦声明，则不允许变更 arguments 额外参数配置
            // com.rabbitmq.client.ShutdownSignalException: channel error;
            // protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED -
            // inequivalent arg 'alternate-exchange' for exchange 'exchange.test' in vhost '/mq01':
            // received the value 'exchange.fanout' of type 'longstr' but current is none, class-id=40, method-id=10)
//            Map<String, Object> arguments = new HashMap<>();
//            // 配置备用路由
//            arguments.put("alternate-exchange", MqConstant.FANOUT_EXCHANGE);
//            LOGGER.info("自动删除特性变更声明：将交换机{}的额外参数配置从{}变更为{}，type、durable 和 autoDelete 维持原样", exchange, null, arguments);
//            channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT, false, true, arguments);

        }
    }

}