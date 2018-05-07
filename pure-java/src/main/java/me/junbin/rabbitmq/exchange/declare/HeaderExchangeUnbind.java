package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/7 14:37
 * @description : headers 类型交换机与队列的绑定如果不指定 {@link Channel#queueBind(String, String, String, Map)} 的最后一个参数，
 * 那么他们绑定的 headers 就变成了 {}，此时所有发送到该交换机的消息都将发送到这种绑定上的队列进行消费
 */
public class HeaderExchangeUnbind {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderExchangeUnbind.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            Map<String, Object> arg1 = new HashMap<>();
            arg1.put(X_MATCH, X_MATCH_ALL);
            arg1.put("username", "rachel");

            Map<String, Object> arg2 = new HashMap<>();
            arg2.put(X_MATCH, X_MATCH_ANY);
            arg2.put("username", "rachel");

            Map<String, Object> arg3 = new HashMap<>();
            arg3.put(X_MATCH, X_MATCH_ANY);
            arg3.put("username", "rachel");
            arg3.put("production", "chocolate");

            LOGGER.info("将{}队列和{}交换机绑定在一起，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg1);
            channel.queueBind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg1);

            LOGGER.info("将{}队列和{}交换机绑定在一起，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg2);
            channel.queueBind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg2);

            LOGGER.info("将{}队列和{}交换机绑定在一起，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg3);
            channel.queueBind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg3);

            // 以上三个绑定都能成功，从而有 { "username": "rachel", "x-match": "any" } 和 { "username": "rachel", "x-match": "all" } 两种 headers

            Thread.sleep(TimeUnit.SECONDS.toMillis(20));

            LOGGER.info("解绑{}队列和{}交换机，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, null);
            // 不指定头部参数时，只会解绑（可能存在的） {} 这种 headers
            channel.queueUnbind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY);

            Thread.sleep(TimeUnit.SECONDS.toMillis(20));

            LOGGER.info("解绑{}队列和{}交换机，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg1);
            channel.queueUnbind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg1);


            LOGGER.info("解绑{}队列和{}交换机，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg2);
            channel.queueUnbind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg2);


            LOGGER.info("解绑{}队列和{}交换机，头部参数为：{}", HEADERS_QUEUE, HEADERS_EXCHANGE, arg3);
            channel.queueUnbind(HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, arg3);

        }
    }

}