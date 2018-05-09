package me.junbin.rabbitmq.queue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import me.junbin.rabbitmq.conn.factory.MqConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/9 9:58
 * @description :
 */
public class ExclusiveQueueFeature {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExclusiveQueueFeature.class);

    public static void main(String[] args) throws Exception {
        String queueName = "queue.name";
        // 相同名称的排他队列不可在不同连接中重复创建
        cantCreateSameExQueue(queueName);

        TimeUnit.SECONDS.sleep(5);
        System.out.println("\n\n\n");

        // 相同名称，1个排他，1个普通队列在不同连接中也不可重复创建
        cantOneExQueueOneNotExQueue(queueName);
    }

    private static void cantCreateSameExQueue(String queueName) throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            try (Connection connection = MqConnectionFactory.newConnection()) {
                Channel channel = connection.createChannel();
                LOGGER.info("{} 准备声明排他队列 {} ", Thread.currentThread().getName(), queueName);
                channel.queueDeclare(queueName, false, true, false, null);
                LOGGER.info("{} 声明排他队列 {} 成功", Thread.currentThread().getName(), queueName);
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception e) {
                LOGGER.error("{} 声明排他队列 {} 失败。原因： --> {}", Thread.currentThread().getName(), queueName, e.getCause().getMessage());
//                LOGGER.error(String.format("%s 声明排他队列 %s 失败", Thread.currentThread().getName(), queueName), e);
            }
        }, "线程1");
        thread1.start();

        thread1.join(TimeUnit.MILLISECONDS.toMillis(500));

        new Thread(() -> {
            try (Connection connection = MqConnectionFactory.newConnection()) {
                Channel channel = connection.createChannel();
                LOGGER.info("{} 准备声明排他队列 {} ", Thread.currentThread().getName(), queueName);
                channel.queueDeclare(queueName, false, true, false, null);
                LOGGER.info("{} 声明排他队列 {} 成功", Thread.currentThread().getName(), queueName);
            } catch (Exception e) {
                LOGGER.error("{} 声明排他队列 {} 失败。原因： --> {}", Thread.currentThread().getName(), queueName, e.getCause().getMessage());
                // Caused by: com.rabbitmq.client.ShutdownSignalException: channel error;
                // protocol method: #method<channel.close>(reply-code=405,
                // reply-text=RESOURCE_LOCKED - cannot obtain exclusive access to locked queue
                // 'queue.name' in vhost '/mq01', class-id=50, method-id=10)
//                LOGGER.error(String.format("%s 声明排他队列 %s 失败", Thread.currentThread().getName(), queueName), e);
            }
        }, "线程2").start();
    }

    private static void cantOneExQueueOneNotExQueue(String queueName) throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            try (Connection connection = MqConnectionFactory.newConnection()) {
                Channel channel = connection.createChannel();
                LOGGER.info("{} 准备声明排他队列 {}", Thread.currentThread().getName(), queueName);
                channel.queueDeclare(queueName, true, true, false, null);
                LOGGER.info("{} 声明排他队列 {} 成功", Thread.currentThread().getName(), queueName);
                TimeUnit.SECONDS.sleep(3);
            } catch (Exception e) {
                LOGGER.error("{} 声明排他队列 {} 失败。原因： --> {}", Thread.currentThread().getName(), queueName, e.getCause().getMessage());
            }
        }, "线程1");
        thread1.start();

        thread1.join(TimeUnit.MILLISECONDS.toMillis(500));

        new Thread(() -> {
            try (Connection connection = MqConnectionFactory.newConnection()) {
                Channel channel = connection.createChannel();
                LOGGER.info("{} 准备声明普通队列 {}", Thread.currentThread().getName(), queueName);
                channel.queueDeclare(queueName, false, false, false, null);
                LOGGER.info("{} 声明普通队列 {} 成功", Thread.currentThread().getName(), queueName);
                TimeUnit.SECONDS.sleep(3);
                LOGGER.info("{} 准备删除普通队列 {}", Thread.currentThread().getName(), queueName);
                channel.queueDelete(queueName);
                LOGGER.info("{} 删除普通队列 {} 成功", Thread.currentThread().getName(), queueName);
            } catch (Exception e) {
                LOGGER.error("{} 声明或者删除普通队列 {} 失败。原因： --> {}", Thread.currentThread().getName(), queueName, e.getCause().getMessage());
            }
        }, "线程2").start();

    }

}