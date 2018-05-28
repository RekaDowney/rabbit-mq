package me.junbin.rabbitmq.spring.retry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 23:29
 * @description :
 */
public class TestRetryTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRetryTemplate.class);

    @Test
    public void test01() throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();
        RetryPolicy retryPolicy = new SimpleRetryPolicy(5);
        retryTemplate.setRetryPolicy(retryPolicy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(TimeUnit.MILLISECONDS.toMillis(500));
        backOffPolicy.setMaxInterval(TimeUnit.SECONDS.toMillis(10));
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setThrowLastExceptionOnExhausted(true);

        AtomicInteger counter = new AtomicInteger(0);

        // RetryCallback 第一次执行时 getRetryCount 返回 0，retryContext.getLastThrowable() 返回 null。
        // 第一次执行完毕后。如果期间抛出异常，则 retryCount 加 1，lastThrowable 表示该异常；否则 retryCount 不再自增
        Integer actions = retryTemplate.execute(
                retryContext -> {
                    retryContext.setAttribute("key", "value");
                    LOGGER.info("第{}次重试", retryContext.getRetryCount());
                    if (counter.incrementAndGet() < 2) {
                        throw new RuntimeException();
                    }
                    return retryContext.getRetryCount();
                },
                retryContext -> {
                    LOGGER.info("尝试{}次都失败", retryContext.getRetryCount());
                    assert "value" == retryContext.getAttribute("key");
                    return retryContext.getRetryCount();
                }
        );

        assert actions == 1;
    }

}