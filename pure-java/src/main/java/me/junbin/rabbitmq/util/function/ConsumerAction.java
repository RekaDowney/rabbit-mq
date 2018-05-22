package me.junbin.rabbitmq.util.function;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import me.junbin.rabbitmq.util.MqUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/22 11:43
 * @description :
 */
@FunctionalInterface
public interface ConsumerAction {

    void handle(MessageInfo messageInfo) throws IOException;

    class MessageInfo {

        private String queueName;
        private String consumerTag;
        private Envelope envelope;
        private AMQP.BasicProperties properties;
        private byte[] body;

        public MessageInfo() {
        }

        public MessageInfo(String queueName, String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) {
            this.queueName = queueName;
            this.consumerTag = consumerTag;
            this.envelope = envelope;
            this.properties = properties;
            this.body = body;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getConsumerTag() {
            return consumerTag;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        public Envelope getEnvelope() {
            return envelope;
        }

        public void setEnvelope(Envelope envelope) {
            this.envelope = envelope;
        }

        public AMQP.BasicProperties getProperties() {
            return properties;
        }

        public void setProperties(AMQP.BasicProperties properties) {
            this.properties = properties;
        }

        public byte[] getBody() {
            return body;
        }

        public void setBody(byte[] body) {
            this.body = body;
        }

        public String getBodyString() {
            return MqUtils.utf8String(this.body);
        }

        public String getBodyString(Charset charset) {
            return new String(this.body, charset);
        }

    }

}