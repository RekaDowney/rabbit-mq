package me.junbin.rabbitmq.spring.config;

import me.junbin.commons.util.Jsr310Utils;
import org.springframework.amqp.core.AnonymousQueue;

import java.time.YearMonth;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/25 15:06
 * @description : 队列名称生成策略
 */
public class MonthQueueNameStrategy implements AnonymousQueue.NamingStrategy {

    @Override
    public String generateName() {
        YearMonth now = YearMonth.now();
        return Jsr310Utils.format(now, "yyyyMM.queue");
    }

}