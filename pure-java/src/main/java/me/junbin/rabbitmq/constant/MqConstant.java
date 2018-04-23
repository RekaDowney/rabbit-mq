package me.junbin.rabbitmq.constant;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/17 23:23
 * @description :
 */
public final class MqConstant {

    public static final String HELLO_WORLD_QUEUE = "hello-world";
    public static final String EMPTY = "";

    public static final String FANOUT_EXCHANGE = "exchange.fanout";
    public static final String DIRECT_EXCHANGE = "exchange.direct";
    public static final String TOPIC_EXCHANGE = "exchange.topic";
    public static final String HEADERS_EXCHANGE = "exchange.headers";
    public static final String DURABLE_EXCHANGE = "exchange.durable";
    public static final String INTERNAL_EXCHANGE = "exchange.internal";

    public static final String FANOUT_QUEUE = "queue.fanout";
    public static final String DIRECT_QUEUE = "queue.direct";
    public static final String TOPIC_QUEUE = "queue.topic";
    public static final String HEADERS_QUEUE = "queue.headers";
    public static final String DURABLE_QUEUE = "queue.durable";
    public static final String INTERNAL_QUEUE = "queue.internal";


}