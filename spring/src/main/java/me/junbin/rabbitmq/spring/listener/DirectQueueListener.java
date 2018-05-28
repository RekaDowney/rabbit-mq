package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.rabbitmq.spring.message.DirectMessage;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 11:02
 * @description :
 */
@Service("directQueueListener")
public class DirectQueueListener extends AbstractMessageListener<DirectMessage> {

    @Override
    void handleMessage(DirectMessage message, MessageProperties properties, Channel channel) throws Exception {

    }

}
