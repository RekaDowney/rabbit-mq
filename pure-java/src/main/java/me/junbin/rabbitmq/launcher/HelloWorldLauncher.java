package me.junbin.rabbitmq.launcher;

import me.junbin.rabbitmq.consumer.HelloWorldConsumer;
import me.junbin.rabbitmq.producer.HelloWorldProducer;

import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/18 11:41
 * @description :
 */
public class HelloWorldLauncher {

    public static void main(String[] args) throws Exception {
        // 启动生产者线程生产一个消息，生产完毕后关闭生产者线程
        new HelloWorldProducer().produce();

        // 先启动消费者线程，等待消费
        new HelloWorldConsumer().consume();

        // 休眠三秒后再次生产一个消息
        TimeUnit.SECONDS.sleep(3);
        new HelloWorldProducer().produce();
    }

}