package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.rabbitmq.spring.message.FanoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:50
 * @description :
 */
@Service("fanoutQueueListener2")
public class FanoutQueueListener2 extends AbstractMessageListener<FanoutMessage> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FanoutQueueListener2.class);

    @Override
    public ConsumeResult handleMessage(FanoutMessage message, MessageProperties properties, Channel channel) throws Exception {
        try {
            LOGGER.info("消费消息：{}", message);
            return ConsumeResult.OK;
        } catch (Exception e) {
            return ConsumeResult.FAIL;
        }
    }

}