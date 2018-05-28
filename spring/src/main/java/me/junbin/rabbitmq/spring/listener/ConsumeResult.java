package me.junbin.rabbitmq.spring.listener;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 23:16
 * @description : 消费结果
 */
public enum ConsumeResult {

    // 消息完毕
    OK,

    // 消费失败
    FAIL,

    // 重试
    RETRY

}