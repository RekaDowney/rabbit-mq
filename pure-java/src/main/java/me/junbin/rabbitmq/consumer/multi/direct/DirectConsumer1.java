package me.junbin.rabbitmq.consumer.multi.direct;

import com.rabbitmq.client.BuiltinExchangeType;
import me.junbin.rabbitmq.consumer.multi.CommonConsumer;

import static me.junbin.rabbitmq.constant.MqConstant.DirectMC.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 16:44
 * @description :
 */
public class DirectConsumer1 {

    public static void main(String[] args) throws Exception {
        new CommonConsumer("消费者1", EXCHANGE, QUEUE, ROUTING_KEY, BuiltinExchangeType.DIRECT).startConsume();
    }

}
