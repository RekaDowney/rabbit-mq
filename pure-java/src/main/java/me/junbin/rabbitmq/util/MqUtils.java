package me.junbin.rabbitmq.util;

import java.nio.charset.StandardCharsets;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 21:23
 * @description :
 */
public abstract class MqUtils {

    public static byte[] utf8Data(String message) {
        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String utf8String(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

}