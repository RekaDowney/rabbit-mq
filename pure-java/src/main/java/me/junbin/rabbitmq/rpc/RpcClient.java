package me.junbin.rabbitmq.rpc;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import me.junbin.rabbitmq.util.MqUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.junbin.rabbitmq.constant.MqConstant.Rpc.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/24 10:47
 * @description :
 */
public class RpcClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    public static void main(String[] args) throws Exception {
        Connection connection = MqConnectionFactory.newConnection();
        Channel channel = connection.createChannel();


        channel.exchangeDeclare(EXCHANGE, BuiltinExchangeType.DIRECT, true, false, false, null);
        channel.queueDeclare(QUEUE, true, false, false, null);
        channel.queueBind(QUEUE, EXCHANGE, ROUTING_KEY);

        channel.basicQos(1, true);


        // RPC 服务端计算完毕后，消息的回调队列
        Map<String, String> validCorrelationIdNumMap = new HashMap<>();
        String callbackQueue = channel.queueDeclare().getQueue();
        LOGGER.info("声明 RPC 客户端（生产者，提供排他回调队列 --> {}），{} 交换机，{} 队列", callbackQueue, EXCHANGE, QUEUE);
        LOGGER.info("为排他回调队列添加消费者，获取 RPC 服务端的计算结果");
        channel.basicConsume(callbackQueue, false, MqUtils.newConsumer(channel, callbackQueue,
                mi -> {
                    try {
                        String utf8Body = mi.getUtf8Body();
                        AMQP.BasicProperties properties = mi.getProperties();
                        String correlationId = properties.getCorrelationId();
                        if (validCorrelationIdNumMap.containsKey(correlationId)) {
                            LOGGER.info("correlationId 为 {} 的 RCP 任务处理完成，计算结果：fibonacci({}) = {}", correlationId, validCorrelationIdNumMap.get(correlationId), utf8Body);
                        } else {
                            LOGGER.error("收到非法的 correlationId --> {}，消息体为 {}", correlationId, utf8Body);
                        }
                    } finally {
                        LOGGER.info("应答 RPC 服务端");
                        channel.basicAck(mi.getEnvelope().getDeliveryTag(), false);
                    }
                }));


        for (int i = 1; i < 6; i++) {
            String correlationId = UUID.randomUUID().toString();
            AMQP.BasicProperties callbackProperties = new AMQP.BasicProperties.Builder()
                    .replyTo(callbackQueue).correlationId(correlationId)
                    .build();
            String num = Integer.toString(i + RandomUtils.nextInt(10, 20));
            validCorrelationIdNumMap.put(correlationId, num);
            LOGGER.info("发布 RCP 消息： 消息属性：{}，消息体（即求第{}个斐波那契值） --> {}", Gsonor.SIMPLE.toJson(callbackProperties), num, num);
            channel.basicPublish(EXCHANGE, ROUTING_KEY, false, false, callbackProperties, MqUtils.utf8Data(num));
        }


    }

}
