package me.junbin.rabbitmq.queue.feature.ttl;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 9:52
 * @description :
 */
public class TtlConsumer1 extends AbstractTtlConsumer {

    public TtlConsumer1(String consumer, long secondToSleepPerConsume) {
        super(consumer, secondToSleepPerConsume);
    }

    public static void main(String[] args) throws Exception {
        new TtlConsumer1("消费者1", 15).startConsume();
    }

}