package me.junbin.rabbitmq.spring.retry;

import org.junit.Test;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.concurrent.TimeUnit;

import static me.junbin.commons.ansi.ColorfulPrinter.green;
import static me.junbin.commons.ansi.ColorfulPrinter.yellow;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/29 11:07
 * @description :
 */
public class TestBackOff {

    @Test
    public void testFixedBackOff() throws Exception {
        FixedBackOff backOff = new FixedBackOff();
        // 固定频率（最多5次【估最多失败重试4次】，每次退避时间为 1 秒）
        backOff.setMaxAttempts(5);
        backOff.setInterval(TimeUnit.SECONDS.toMillis(1));

        BackOffExecution execution = backOff.start();

        int counter = 1;
        long nextBackOff;
        while ((nextBackOff = execution.nextBackOff()) != BackOffExecution.STOP) {
            // 业务逻辑
            yellow(String.format("第%d次", counter));

            // 业务处理过程中出现异常，休眠退避时间
            Thread.sleep(nextBackOff);
            counter++;
        }
    }

    @Test
    public void testExponentialBackOff() throws Exception {

        ExponentialBackOff backOff = new ExponentialBackOff();
        // 初始退避时间
        backOff.setInitialInterval(TimeUnit.MILLISECONDS.toMillis(500));
        // 递增倍数（即下次退避时间是上次的多少倍），必须大于 1
        backOff.setMultiplier(2.0);
        // 最大退避时间，当某次退避时间大于该值时，退避时间固定为当前值
        backOff.setMaxInterval(TimeUnit.SECONDS.toMillis(10));
        // 最大累计退避时间，当累计退避时间（即所有失败造成的退避时间求和）达到该值时返回 STOP
        backOff.setMaxElapsedTime(TimeUnit.SECONDS.toMillis(10));

        BackOffExecution execution = backOff.start();

        int counter = 1;
        long nextBackOff;
        while ((nextBackOff = execution.nextBackOff()) != BackOffExecution.STOP) {
            // 业务逻辑
            green(String.format("第%d次", counter));

            // 业务处理过程中出现异常，休眠退避时间
            Thread.sleep(nextBackOff);
            counter++;
        }
    }

}