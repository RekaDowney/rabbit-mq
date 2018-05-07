package me.junbin.rabbitmq.exchange.declare;

import com.rabbitmq.client.*;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.constant.MqConstant;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static me.junbin.rabbitmq.constant.MqConstant.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 22:38
 * @description :
 */
public class TopicExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicExchange.class);

    public static void main(String[] args) throws Exception {
        try (Connection connection = MqConnectionFactory.newConnection()) {

            Channel channel = connection.createChannel();

            LOGGER.info("声明交换机：{}，类型为：{}", TOPIC_EXCHANGE, BuiltinExchangeType.TOPIC.getType());
            channel.exchangeDeclare(TOPIC_EXCHANGE, BuiltinExchangeType.TOPIC, false, true, null);


            LOGGER.info("声明队列：{}", TOPIC_ASTERISK_QUEUE);
            channel.queueDeclare(TOPIC_ASTERISK_QUEUE, false, false, false, null);
            LOGGER.info("将队列{}与交换机{}绑定在一起，路由键为：{}", TOPIC_ASTERISK_QUEUE, TOPIC_EXCHANGE, TOPIC_ASTERISK_ROUTING_KEY);
            channel.queueBind(TOPIC_ASTERISK_QUEUE, TOPIC_EXCHANGE, TOPIC_ASTERISK_ROUTING_KEY);
            LOGGER.debug("消费者开始监听队列{}", TOPIC_ASTERISK_QUEUE);
            channel.basicConsume(TOPIC_ASTERISK_QUEUE, true, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.info("队列 {} 消费消息 --> {}", TOPIC_ASTERISK_QUEUE, MqUtils.utf8String(body));
                }

            });

            LOGGER.info("声明队列：{}", TOPIC_HASH_QUEUE);
            channel.queueDeclare(TOPIC_HASH_QUEUE, false, false, false, null);
            LOGGER.info("将队列{}与交换机{}绑定在一起，路由键为：{}", TOPIC_HASH_QUEUE, TOPIC_EXCHANGE, MqConstant.TOPIC_HASH_ROUTING_KEY);
            channel.queueBind(TOPIC_HASH_QUEUE, TOPIC_EXCHANGE, MqConstant.TOPIC_HASH_ROUTING_KEY);
            LOGGER.debug("消费者开始监听队列{}", TOPIC_HASH_QUEUE);
            channel.basicConsume(TOPIC_HASH_QUEUE, true, new DefaultConsumer(channel) {

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    LOGGER.info("队列 {} 消费消息 --> {}", TOPIC_HASH_QUEUE, MqUtils.utf8String(body));
                }

            });

            String routingKey = "topic.asterisk.";
            String message = "routingKey 为 topic.asterisk. 的消息";
            LOGGER.info("以路由键 {} 发布消息：{}", routingKey, message);
            // 特殊情况：topic.asterisk. 可以被 queue.topic.asterisk 队列消费
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
            // 无法消费
            routingKey = "topic.asterisk..";
            message = "routingKey 为 topic.asterisk.. 的消息";
            LOGGER.info("以路由键 {} 发布消息：{}", routingKey, message);
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
            // 无法消费
            routingKey = "topic.asterisk...";
            message = "routingKey 为 topic.asterisk... 的消息";
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));


            // 特殊情况：topic..hash、topic...hash、topic....hash 可以被 queue.topic.hash 队列消费
            routingKey = "topic..hash";
            message = "routingKey 为 topic..hash 的消息";
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
            routingKey = "topic...hash";
            message = "routingKey 为 topic...hash 的消息";
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
            routingKey = "topic....hash";
            message = "routingKey 为 topic....hash 的消息";
            channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));

            LOGGER.debug("开启生产者线程，路由键动态生成，延迟1秒发布第一条消息，每隔1秒发布一条消息，生产18条消息后结束线程");
            new Timer("生产者定时线程").scheduleAtFixedRate(new TimerTask() {

                private AtomicInteger counter = new AtomicInteger(0);
                private static final String messageTemplate = "第%d条消息";

                private String generateRoutingKey() {
                    int count = counter.get();
                    if (count == 18) {
                        this.cancel();
                    }
                    int type = count % 6;
                    switch (type) {
                        case 0: // 两种队列都可以消费
                            return "topic.asterisk.hash";
                        case 1: // 仅 topic.asterisk.* 队列可以消费
                            return "topic.asterisk." + RandomStringUtils.randomAlphabetic(4);
                        case 2: // 仅 topic.#.hash 队列可以消费
                            int num = RandomUtils.nextInt(0, 4);
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < num; i++) {
                                builder.append(RandomStringUtils.randomAlphabetic(4)).append('.');
                            }
                            return "topic." + builder.toString() + "hash";
                        case 3: // 两种队列都不可以消费，因此 * 表示必须匹配一个单词，如果单词不存在则表示不匹配
                            return "topic.asterisk";
                        case 4: // 仅 topic.#.hash 队列可以消费，因此 # 表示匹配 0 或者多个单词，0 个单词的时候 . 点分符会被忽略
                            return "topic.hash";
                        case 5: // 两种队
                            // 列都不可以消费
                            return RandomStringUtils.randomAlphabetic(4) + '.' + RandomStringUtils.randomAlphabetic(4);
                        default:
                            return "";
                    }
                }

                @Override
                public void run() {
                    try {
                        String message = String.format(messageTemplate, counter.incrementAndGet());
                        String routingKey = generateRoutingKey();
                        LOGGER.debug("以路由键 {} 发布消息：{}", routingKey, message);
                        channel.basicPublish(TOPIC_EXCHANGE, routingKey, null, MqUtils.utf8Data(message));
                    } catch (IOException e) {
                        LOGGER.error("发布消息失败", e);
                    }
                }
            }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1));

            // join 半分钟
            Thread.currentThread().join(TimeUnit.SECONDS.toMillis(30));

            // 解绑，测试 auto delete
            LOGGER.info("解绑队列{}、{}与交换机{}的路由关系，测试 AUTO DELETE 特性", TOPIC_ASTERISK_QUEUE, TOPIC_HASH_QUEUE, TOPIC_EXCHANGE);
            channel.queueUnbind(TOPIC_ASTERISK_QUEUE, TOPIC_EXCHANGE, TOPIC_ASTERISK_ROUTING_KEY);
            channel.queueUnbind(TOPIC_HASH_QUEUE, TOPIC_EXCHANGE, TOPIC_HASH_ROUTING_KEY);

            // 结束当前运行
            System.exit(0);
        }
    }

}