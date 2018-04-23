package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.impl.ValueWriter;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 21:18
 * @description :
 * 注意：路由键最大长度为 255 个字节，具体参考 {@link ValueWriter#writeShortstr(java.lang.String)}
 */
public class RoutingKeyMaxLength {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingKeyMaxLength.class);

    public static void main(String[] args) throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();

        LOGGER.info("声明交换机：{}，类型为：{}", MqConstant.DIRECT_EXCHANGE, BuiltinExchangeType.DIRECT.getType());
        channel.exchangeDeclare(MqConstant.DIRECT_EXCHANGE, BuiltinExchangeType.DIRECT, true, false, null);

        LOGGER.info("声明队列：{}", MqConstant.DIRECT_QUEUE);
        channel.queueDeclare(MqConstant.DIRECT_QUEUE, false, false, false, null);

        // 交换机和队列之间可以有任意多个 routingKey
        String routingKey254 = RandomStringUtils.randomAlphabetic(254);
        LOGGER.info("将队列{}与交换机{}绑定在一起，路由键长度：{}字节", MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, MqUtils.utf8Data(routingKey254).length);
        channel.queueBind(MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, routingKey254);

        String routingKey255 = RandomStringUtils.randomAlphabetic(255);
        LOGGER.info("将队列{}与交换机{}绑定在一起，路由键长度：{}字节", MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, MqUtils.utf8Data(routingKey255).length);
        channel.queueBind(MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, routingKey255);

        String routingKey256 = RandomStringUtils.randomAlphabetic(256);
        LOGGER.info("将队列{}与交换机{}绑定在一起，路由键长度：{}字节", MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, MqUtils.utf8Data(routingKey256).length);
        // Exception in thread "main" java.lang.IllegalArgumentException: Short string too long; utf-8 encoded length = 256, max = 255.
        channel.queueBind(MqConstant.DIRECT_QUEUE, MqConstant.DIRECT_EXCHANGE, routingKey256);
    }

}