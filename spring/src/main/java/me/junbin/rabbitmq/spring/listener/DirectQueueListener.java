package me.junbin.rabbitmq.spring.listener;

import com.rabbitmq.client.Channel;
import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.message.DirectMessage;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 11:02
 * @description :
 */
@Service("directQueueListener")
public class DirectQueueListener extends AbstractMessageListener<DirectMessage> {

    private AtomicBoolean flag = new AtomicBoolean(false);
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectQueueListener.class);

    // 由于配置 currency 为 2，因此是采用线程池将消费逻辑放到线程中执行，所以看到的不会是顺序消费
    @Override
    public ConsumeResult handleMessage(DirectMessage message, MessageProperties properties, Channel channel) throws Exception {
        try {
            if (!flag.get()) {
                if (RandomUtils.nextInt(1, 3) == 1) {
                    flag.set(true);
                    throw new IllegalStateException("当前状态不对");
                } else {
                    throw new RuntimeException("逻辑异常");
                }
            }
            flag.set(false);
            LOGGER.info("成功消费消息：{}", Gsonor.SIMPLE.toJson(message));
            return ConsumeResult.OK;
        } catch (IllegalStateException e) {
            LOGGER.warn("消息 {} 消费失败，异常信息：{}，即将重试", Gsonor.SIMPLE.toJson(message), e.getMessage());
            return ConsumeResult.RETRY;
        } catch (Exception e) {
            LOGGER.warn("消息 {} 消费失败，异常信息：{}", Gsonor.SIMPLE.toJson(message), e.getMessage());
            return ConsumeResult.FAIL;
        }
    }

}