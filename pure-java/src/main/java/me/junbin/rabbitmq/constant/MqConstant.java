package me.junbin.rabbitmq.constant;

import com.rabbitmq.client.AMQP;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/17 23:23
 * @description :
 */
public final class MqConstant {

    public static final String HELLO_WORLD_QUEUE = "hello-world";
    public static final String EMPTY = "";

    public static final String DEFAULT_EXCHANGE = EMPTY;
    public static final String FANOUT_EXCHANGE = "exchange.fanout";
    public static final String DIRECT_EXCHANGE = "exchange.direct";
    public static final String TOPIC_EXCHANGE = "exchange.topic";
    public static final String HEADERS_EXCHANGE = "exchange.headers";
    public static final String EXTERNAL_EXCHANGE = "exchange.external";
    public static final String INTERNAL_EXCHANGE = "exchange.internal";
    public static final String DURABLE_EXCHANGE = "exchange.durable";
    public static final String DD_EXCHANGE = "exchange.durable.direct";
    public static final String DELAY_EXCHANGE = "exchange.delay";
    public static final String HANDLE_DELAY_EXCHANGE = "exchange.delay.handler";

    public static final String FANOUT_QUEUE = "queue.fanout";
    public static final String DIRECT_QUEUE = "queue.direct";
    public static final String TOPIC_ASTERISK_QUEUE = "queue.topic.asterisk";
    public static final String TOPIC_HASH_QUEUE = "queue.topic.hash";
    public static final String HEADERS_QUEUE = "queue.headers";
    public static final String X_MATCH_ALL_HEADERS_QUEUE = "queue.headers.all";
    public static final String X_MATCH_ANY_HEADERS_QUEUE = "queue.headers.any";
    public static final String INTERNAL_QUEUE = "queue.internal";
    public static final String DURABLE_QUEUE = "queue.durable";
    public static final String DD_TTL_QUEUE = "queue.ttl.durable.direct";
    public static final String DD_EXPIRE_QUEUE = "queue.expire.durable.direct";
    public static final String DELAY_QUEUE = "queue.delay";
    public static final String HANDLE_DELAY_QUEUE = "queue.delay.handler";

    public static final String DIRECT_ROUTING_KEY = "direct.routing";
    public static final String TOPIC_ASTERISK_ROUTING_KEY = "topic.asterisk.*";
    public static final String TOPIC_HASH_ROUTING_KEY = "topic.#.hash";
    public static final String X_MATCH = "x-match";
    public static final String X_MATCH_ALL = "all";
    public static final String X_MATCH_ANY = "any";

    public static final String DIRECT_EXCLUSIVE_EXCHANGE = "exchange.direct.exclusive";
    public static final String EXCLUSIVE1_QUEUE = "queue.exclusive.1";
    public static final String DIRECT_EXCLUSIVE1_ROUTING_KEY = "topic.exclusive.1";
    public static final String EXCLUSIVE2_QUEUE = "queue.exclusive.2";
    public static final String DIRECT_EXCLUSIVE2_ROUTING_KEY = "topic.exclusive.2";
    public static final String DD_TTL_ROUTING_KEY = "dd.ttl.queue.exchange";
    public static final String AUTO_DELETE_QUEUE = "queue.auto.delete";

    public static final String DELAY_ROUTING_KEY = "delay.key";
    public static final String HANDLE_DELAY_ROUTING_KEY = "delay.handle.key";

    // 特性
    public static final String FEATURE_X_MESSAGE_TTL = "x-message-ttl";
    public static final String FEATURE_X_EXPIRES = "x-expires";
    public static final String FEATURE_X_MAX_LENGTH = "x-max-length";
    public static final String FEATURE_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String FEATURE_DEAD_LETTER_ROUNTING_KEY = "x-dead-letter-routing-key";
    public static final String FEATURE_X_MAX_PRIORITY = "x-max-priority";

    public static final class FanoutMC {
        public static final String EXCHANGE = "fanout.mc.exchange";
        public static final String QUEUE = "fanout.mc.queue";
        public static final String ROUTING_KEY = "";
    }

    public static final class DirectMC {
        public static final String EXCHANGE = "direct.mc.exchange";
        public static final String QUEUE = "direct.mc.queue";
        public static final String ROUTING_KEY = "direct.mc.routing.key";
    }

    public static final class TopicMC {
        public static final String EXCHANGE = "topic.mc.exchange";
        public static final String QUEUE = "topic.mc.queue";
        public static final String ROUTING_KEY = "topic.mc.routing.key";
    }

    public static final class HeadersMC {
        public static final String EXCHANGE = "headers.mc.exchange";
        public static final String QUEUE = "headers.mc.queue";
        public static final String ROUTING_KEY = "headers.mc.routing.key";
    }

    public static final class EQM {
        public static final String EXCHANGE = "eqm.exchange";
        public static final String QUEUE = "eqm.queue";
        public static final String ROUTING_KEY = "eqm.routing.key";
    }

    public static final class MessageProperties {

        private static final Integer PERSISTENT_DELIVERY_MODE = 2;
        private static final String APPLICATION_JSON = "application/json";
        private static final String TEXT_PLAIN = "text/plain";

        public static final AMQP.BasicProperties PERSISTENT = new AMQP.BasicProperties.Builder()
                .deliveryMode(PERSISTENT_DELIVERY_MODE).build();

        public static final AMQP.BasicProperties PERSISTENT_JSON = new AMQP.BasicProperties.Builder()
                .deliveryMode(PERSISTENT_DELIVERY_MODE).contentType(APPLICATION_JSON).build();

        public static final AMQP.BasicProperties PERSISTENT_TEXT = new AMQP.BasicProperties.Builder()
                .deliveryMode(PERSISTENT_DELIVERY_MODE).contentType(TEXT_PLAIN).build();

    }

    public static final class Rpc {

        public static final String EXCHANGE = "rpc.exchange";
        public static final String QUEUE = "rpc.queue";
        public static final String ROUTING_KEY = "rpc.routing.key";

    }

    public static final class Tx {

        public static final String EXCHANGE = "tx.exchange";
        public static final String QUEUE = "tx.queue";
        public static final String ROUTING_KEY = "tx.routing.key";

    }

    public static final class Confirm {

        public static final String EXCHANGE = "tx.exchange";
        public static final String QUEUE = "tx.queue";
        public static final String ROUTING_KEY = "tx.routing.key";

    }

}