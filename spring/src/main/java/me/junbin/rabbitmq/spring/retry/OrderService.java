package me.junbin.rabbitmq.spring.retry;

import me.junbin.commons.gson.Gsonor;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/29 11:37
 * @description :
 */
@Service
public class OrderService {

    private AtomicInteger insertCounter = new AtomicInteger(0);
    private AtomicInteger deleteCounter = new AtomicInteger(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    // 当遇到 RuntimeException、IllegalArgumentException、IllegalStateException 异常时需要重试
    // 当 value/include 和 exclude 都为空时，所有异常都需要重试
    // stateful 标注是否重试是否是有状态的，数据库相关操作时需要置为 true 以便重试时开启新事务
    // backoff 用来指定失败重试的退避策略，默认不配置，表示立即重试，此时会阻塞线程（实际测试发现会休眠1秒）
    // maxAttempts 表示包括首次请求和重试次数的最多执行次数
    @Retryable(
            include = {
                    RuntimeException.class,
                    IllegalArgumentException.class,
                    IllegalStateException.class
            },
            maxAttempts = 4
    )
    public String insert(Object order) {
        LOGGER.info("准备入库订单：{}", Gsonor.SIMPLE.toJson(order));
        int oneToTen = RandomUtils.nextInt(1, 11);
        if (insertCounter.incrementAndGet() < 5) {
            if (oneToTen < 5) {
                throw new RuntimeException("Insert. Less than 5");
            } else if (oneToTen < 7) {
                throw new IllegalArgumentException("Insert. Greater or equal than 5 and Less than 7");
            } else if (oneToTen <= 10) {
                throw new IllegalStateException("Insert. Greater or equal than 7 and Less than 11");
            }
        }
        insertCounter.set(0);
        return "OK";
    }

    // value/include 和 exclude 都为空时，所有异常都需要重试
    // backoff 用来指定失败重试的退避策略，初始退避时间为 0.5 秒，往后每一次退避时间都是上一次的 2 被，最大退避时间为 5 秒
    // maxAttempts 表示包括首次请求和重试次数的最多执行次数
    @Retryable(
            backoff = @Backoff(
                    delay = 500,
                    maxDelay = 5000,
                    multiplier = 2.0
            ),
            maxAttempts = 4
    )
    public String delete(Object order) {
        LOGGER.info("准备删除订单：{}", Gsonor.SIMPLE.toJson(order));
        int oneToTen = RandomUtils.nextInt(1, 11);
        if (deleteCounter.incrementAndGet() < 5) {
            if (oneToTen < 3) {
                throw new RuntimeException("Delete. Less than 3");
            } else if (oneToTen < 6) {
                throw new IllegalArgumentException("Delete. Greater or equal than 3 and Less than 6");
            } else if (oneToTen <= 10) {
                throw new IllegalStateException("Delete. Greater or equal than 6 and Less than 11");
            }
        }
        deleteCounter.set(0);
        return "OK";
    }

/*
    public String delete(Object order, boolean physicalDelete) {
        LOGGER.info("准备删除订单：{}（physical: {}）", Gsonor.SIMPLE.toJson(order), physicalDelete);
        int oneToTen = RandomUtils.nextInt(1, 11);
        if (deleteCounter.incrementAndGet() < 5) {
            if (oneToTen < 3) {
                throw new RuntimeException("Delete. Less than 3");
            } else if (oneToTen < 6) {
                throw new IllegalArgumentException("Delete. Greater or equal than 3 and Less than 6");
            } else if (oneToTen <= 10) {
                throw new IllegalStateException("Delete. Greater or equal than 6 and Less than 11");
            }
        }
        deleteCounter.set(0);
        return "OK";
    }
*/

    // @Recover 当所有重试都失败后执行，此时该注解标注的的第一个参数必须是异常类（用于匹配最后一次重试失败抛出的异常）
    //      剩下的参数按顺序匹配 @Retryable 标注的方法的参数列表。
    // @Recover 标注的方法必须与 @Retryable 标注的方法拥有相同的返回类型，该返回值作为 @Retryable 的返回结果
    @Recover
    public String recoverForRuntimeException(RuntimeException e, Object order) {
        LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
        reset(e.getMessage());
        return "Error: RuntimeException";
    }

    @Recover
    public String recoverForIllegalArgumentException(IllegalArgumentException e, Object order) {
        LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
        reset(e.getMessage());
        return "Error: IllegalArgumentException";
    }

    @Recover
    public String recoverForIllegalStateException(IllegalStateException e, Object order) {
        LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
        reset(e.getMessage());
        return "Error: IllegalStateException";
    }

/*
    // org.springframework.retry.annotation.RecoverAnnotationRecoveryHandler.recover
    // org.springframework.retry.annotation.RecoverAnnotationRecoveryHandler.SimpleMetadata.getArgs()
    // java.lang.System.arraycopy
    // java.lang.ArrayIndexOutOfBoundsException
    @Recover
    public String recoverForRuntimeException(RuntimeException e, Object order, boolean physicalDelete) {
        LOGGER.info("多次入库订单：{} （physical: {}）失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), physicalDelete, e.getMessage());
        reset(e.getMessage());
        return "Error: RuntimeException";
    }

    @Recover
    public String recoverForIllegalArgumentException(IllegalArgumentException e, Object order, boolean physicalDelete) {
        LOGGER.info("多次入库订单：{} （physical: {}）失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), physicalDelete, e.getMessage());
        reset(e.getMessage());
        return "Error: IllegalArgumentException";
    }

    @Recover
    public String recoverForIllegalStateException(IllegalStateException e, Object order, boolean physicalDelete) {
        LOGGER.info("多次入库订单：{} （physical: {}）失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), physicalDelete, e.getMessage());
        reset(e.getMessage());
        return "Error: IllegalStateException";
    }
*/

    private void reset(String errorMessage) {
        if (errorMessage.startsWith("Insert. ")) {
            insertCounter.set(0);
        } else if (errorMessage.startsWith("Delete. ")) {
            deleteCounter.set(0);
        }
    }

}