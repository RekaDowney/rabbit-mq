package me.junbin.rabbitmq.spring;

import me.junbin.commons.gson.Gsonor;
import me.junbin.rabbitmq.spring.constant.MqConstant;
import me.junbin.rabbitmq.spring.constant.SpringMqConstant;
import me.junbin.rabbitmq.spring.message.FanoutMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 16:24
 * @description :
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:config/applicationContext.xml",
        "classpath:config/applicationContext-mq.xml"})
public class TestFanout {

    @Autowired
    private RetryTemplate retryTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SpringMqConstant springMqConstant;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestFanout.class);

    @Test
    public void testProduceConsume() throws Exception {
        // 通过
        for (int i = 1; i <= 10; i++) {
            FanoutMessage message = new FanoutMessage((long) i, String.format("第%d条消息", i));
            retryTemplate.execute(
                    // 每当执行的方法抛出异常，将会执行重试
                    retryContext -> {
                        retryContext.setAttribute(MqConstant.MESSAGE, message);
                        int retryCount = retryContext.getRetryCount();
                        Throwable lastThrowable = retryContext.getLastThrowable();
                        if (lastThrowable != null) {
                            LOGGER.info("第{}次重试发送消息：{}失败，异常：{}", retryCount, Gsonor.SIMPLE.toJson(message), lastThrowable.getMessage());
                        } else {
                            LOGGER.info("第1次发送消息：{}", Gsonor.SIMPLE.toJson(message));
                        }
                        rabbitTemplate.convertAndSend(springMqConstant.getFanoutExchange(), MqConstant.EMPTY, message);
                        return null;
                    },
                    // 所有重试失败后，执行下面的方法
                    retryContext -> {
                        FanoutMessage fanoutMessage = (FanoutMessage) retryContext.getAttribute(MqConstant.MESSAGE);
                        int retryCount = retryContext.getRetryCount();
                        Throwable lastThrowable = retryContext.getLastThrowable();
                        LOGGER.error(String.format("消息 %s 重试发送%d次都失败", Gsonor.SIMPLE.toJson(fanoutMessage), retryCount), lastThrowable);
                        return null;
                    }
            );
        }

        TimeUnit.MILLISECONDS.sleep(500);
    }

}