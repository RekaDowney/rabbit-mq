package me.junbin.rabbitmq.queue.feature.ttl;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/23 9:52
 * @description :
 */
public class TtlConsumer2 extends AbstractTtlConsumer {

    public TtlConsumer2(String consumer, long secondToSleepPerConsume) {
        super(consumer, secondToSleepPerConsume);
    }

    public static void main(String[] args) throws Exception {
        new TtlConsumer2("消费者2", 7).startConsume();
    }

}