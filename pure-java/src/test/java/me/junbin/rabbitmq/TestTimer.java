package me.junbin.rabbitmq;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/27 9:51
 * @description :
 */
public class TestTimer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestTimer.class);

    @Test
    public void testTimer() throws Exception {
        Timer timer = new Timer("测试");
        timer.scheduleAtFixedRate(new TimerTask() {

            private AtomicInteger counter = new AtomicInteger(0);

            @Override
            public void run() {
                int count = counter.incrementAndGet();
                LOGGER.info("第{}/10次调度", count);
                if (count == 10) {
                    this.cancel();
                }
            }
        }, TimeUnit.SECONDS.toMillis(2), TimeUnit.SECONDS.toMillis(1));

        Thread.currentThread().join(TimeUnit.SECONDS.toMillis(15));
    }

}