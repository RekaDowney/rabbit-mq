package me.junbin.rabbitmq.exchange.delete;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/7 18:34
 * @description :
 */
public class DeleteExchange {

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();
            channel.exchangeDelete("exchange.test");

            channel.exchangeDelete(MqConstant.INTERNAL_EXCHANGE);
            channel.exchangeDelete(MqConstant.EXTERNAL_EXCHANGE);
        }
    }

}