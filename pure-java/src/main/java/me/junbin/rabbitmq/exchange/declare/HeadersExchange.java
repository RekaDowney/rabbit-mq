package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-05-07 09:48
 * @description : headers 类型交换机与路由键无关，该类交换机会将收到的所有消息根据 {@link Channel#queueBind(String, String, String, Map)}
 * 中最后一个参数（头部要求）与 {@link Channel#basicPublish(String, String, AMQP.BasicProperties, byte[])} 中 {@link BasicProperties#headers}
 * 的头部进行匹配，满足匹配条件则传递到队列中消费
 * <p>
 * 特别注意：API 规范要求路由键必须非 null
 */
public class HeadersExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersExchange.class);
    private static final List<Pair<String, Object>> NODES = new ArrayList<>();

    static {
        NODES.add(new ImmutablePair<>("username", "reka"));
        NODES.add(new ImmutablePair<>("production", "chocolate"));
        NODES.add(new ImmutablePair<>("company", "Mc.INC"));
    }

    private static Map<String, Object> assemble(int size, Map<String, Object> map) {
        size = size > NODES.size() ? NODES.size() : size;
        for (int i = 0; i < size; i++) {
            Pair<String, Object> pair = NODES.get(i);
            map.put(pair.getKey(), pair.getValue());
        }
        return map;
    }

    private static Map<String, Object> anyHeader(boolean containsXMatch, int size) {
        Map<String, Object> result = new HashMap<>();
        if (containsXMatch) {
            result.put(X_MATCH, X_MATCH_ANY);
        }
        return assemble(size, result);
    }

    private static Map<String, Object> allHeader(boolean containsXMatch, int size) {
        Map<String, Object> result = new HashMap<>();
        if (containsXMatch) {
            result.put(X_MATCH, X_MATCH_ALL);
        }
        return assemble(size, result);
    }

    public static void main(String[] args) throws Exception {
        int size = NODES.size();
        Map<String, Object> allHeader = allHeader(true, size);
        Map<String, Object> anyHeader = anyHeader(true, size);
        try (Connection connection = MqConnectionFactory.newConnection()) {
            Channel channel = connection.createChannel();

            LOGGER.debug("声明类型为{}的{}交换机", BuiltinExchangeType.HEADERS.getType(), HEADERS_EXCHANGE);
            channel.exchangeDeclare(HEADERS_EXCHANGE, BuiltinExchangeType.HEADERS, false, false, null);

            LOGGER.debug("声明{}队列并指定消费者，同时将该队列使用{}头部（只有除 x-match 以外的所有头部都匹配才消费）与{}绑定在一起", X_MATCH_ALL_HEADERS_QUEUE, allHeader, HEADERS_EXCHANGE);
            channel.queueDeclare(X_MATCH_ALL_HEADERS_QUEUE, false, false, false, null);
            channel.basicConsume(X_MATCH_ALL_HEADERS_QUEUE, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.info("队列 {} 消费 {}，消息头部：{}", X_MATCH_ALL_HEADERS_QUEUE, MqUtils.utf8String(body), properties.getHeaders());
                }
            });
            channel.queueBind(X_MATCH_ALL_HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, allHeader);

            LOGGER.debug("声明{}队列并指定消费者，同时将该队列使用{}头部（只要除 x-match 以外的任意一个头部匹配就消费）与{}绑定在一起", X_MATCH_ANY_HEADERS_QUEUE, anyHeader, HEADERS_EXCHANGE);
            channel.queueDeclare(X_MATCH_ANY_HEADERS_QUEUE, false, false, false, null);
            channel.basicConsume(X_MATCH_ANY_HEADERS_QUEUE, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.info("队列 {} 消费 {}，消息头部：{}", X_MATCH_ANY_HEADERS_QUEUE, MqUtils.utf8String(body), properties.getHeaders());
                }
            });
            channel.queueBind(X_MATCH_ANY_HEADERS_QUEUE, HEADERS_EXCHANGE, EMPTY, anyHeader);


            // 生产者
            // 测试所有头部都匹配
            String message = String.format("头部要求为：%s，不包含任何头部", X_MATCH_ALL); // 不被任何队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, null, MqUtils.utf8Data(message));

            Map<String, Object> headers = allHeader(false, size / 2);
            message = String.format("头部要求为：%s，包含头部%s", X_MATCH_ALL, headers); // 被 ANY 队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));

            headers = allHeader(false, size);
            message = String.format("头部要求为：%s，包含头部%s", X_MATCH_ALL, headers); // 被 ALL 和 ANY 队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));

            // 测试任意头部匹配
            message = String.format("头部要求为：%s，不包含任何头部", X_MATCH_ANY); // 不被任何队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, null, MqUtils.utf8Data(message));

            headers = anyHeader(false, size / 2);
            message = String.format("头部要求为：%s，包含头部%s", X_MATCH_ANY, headers); // 被 ANY 队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));

            headers = anyHeader(false, size / 2);
            headers.put("random", RandomStringUtils.randomAlphabetic(4)); // 被 ANY 队列消费
            message = String.format("头部要求为：%s，包含头部%s", X_MATCH_ANY, headers);
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));

            headers = anyHeader(false, size);
            headers.put("random", RandomStringUtils.randomAlphabetic(4));
            message = String.format("头部要求为：%s，包含头部%s", X_MATCH_ANY, headers); // 被 ALL 和 ANY 队列消费
            LOGGER.debug("发布{}的消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));

            LOGGER.debug("等待10秒钟后结束进程");
            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(10));


            // 测试头部为 null 的 headers 类型交换机
            // 绑定时头部为 null 时，交换机收到的所有消息都会发送到绑定的队列上消费
            String queue = "no.header.queue";
            channel.queueDeclare(queue, false, false, true, null);
            channel.queueBind(queue, HEADERS_EXCHANGE, EMPTY, null);
            channel.basicConsume(queue, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.debug("队列 {} 消费消息 --> {}", queue, MqUtils.utf8String(body));
                }
            });
            LOGGER.debug("发布无头消息");
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, null, MqUtils.utf8Data("无头测试")); // 被无头队列消费

            headers = anyHeader(false, 1);
            message = String.format("无头测试，头部为%s", headers);  // 被无头队列和 ANY 队列消费
            LOGGER.debug("发布 {} 消息", message);
            channel.basicPublish(HEADERS_EXCHANGE, EMPTY, new AMQP.BasicProperties.Builder().headers(headers).build(), MqUtils.utf8Data(message));


            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(5));

            channel.queueUnbind(queue, HEADERS_EXCHANGE, EMPTY);
        }
    }

}