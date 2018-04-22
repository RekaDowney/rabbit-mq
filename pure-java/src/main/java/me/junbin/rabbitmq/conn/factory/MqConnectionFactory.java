package me.junbin.rabbitmq.conn.factory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import me.junbin.rabbitmq.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/17 23:11
 * @description :
 */
public class MqConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqConnectionFactory.class);

    private static class Holder {
        private static final ConnectionFactory FACTORY = new ConnectionFactory();

        static {
            LOGGER.debug("创建 RabbitMQ 连接池");
            FACTORY.setHost(ConfigUtils.MQ_TRANSLATOR.getAsString("rabbitmq.host"));
            FACTORY.setPort(ConfigUtils.MQ_TRANSLATOR.getAsInt("rabbitmq.port"));
            FACTORY.setUsername(ConfigUtils.MQ_TRANSLATOR.getAsString("rabbitmq.username"));
            FACTORY.setPassword(ConfigUtils.MQ_TRANSLATOR.getAsString("rabbitmq.password"));
            FACTORY.setVirtualHost(ConfigUtils.MQ_TRANSLATOR.getAsString("rabbitmq.vhost"));
            // 支持通过 URI 形式构造连接池
            // FACTORY.setUri("amqp://username:password@host:port/vhost");
        }
    }

    public static Connection newConnection() throws IOException, TimeoutException {
        LOGGER.debug("新建 RabbitMQ 连接");
        return Holder.FACTORY.newConnection();
    }

}