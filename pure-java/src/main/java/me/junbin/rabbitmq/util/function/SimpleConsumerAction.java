package me.junbin.rabbitmq.util.function;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/7 23:10
 * @description :
 */
@FunctionalInterface
public interface SimpleConsumerAction {

    void handle(byte[] body);

}