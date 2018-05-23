package me.junbin.rabbitmq.consumer.multi.direct;

import com.rabbitmq.client.BuiltinExchangeType;
import me.junbin.rabbitmq.consumer.multi.CommonProducer;

import static me.junbin.rabbitmq.constant.MqConstant.DirectMC.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 17:01
 * @description : 测试表明：
 * 针对 {@link BuiltinExchangeType#DIRECT} 类型的交换机 - 队列 - 多消费者模式，队列会轮询地将消息发送给消费者。
 * 此时由消费者保证消费不重复，或者消费操作是幂等的
 */
public class DirectProducer {

    public static void main(String[] args) throws Exception {
        new CommonProducer(EXCHANGE, QUEUE, ROUTING_KEY, BuiltinExchangeType.DIRECT).startProduce();
    }

}