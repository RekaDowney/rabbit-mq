package me.junbin.rabbitmq;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/5/7 15:31
 * @description :
 */
public class TestTimestamp {

    @Test
    public void test01() throws Exception {
        LocalDateTime time = LocalDateTime.ofEpochSecond(1506332315, 0, ZoneOffset.UTC);
        System.out.println(time);

        LocalDateTime end = LocalDateTime.ofEpochSecond(1525678482, 0, ZoneOffset.UTC);
        System.out.println(end);


    }

    private LocalDateTime of(long timestamp) {
        return LocalDateTime.ofEpochSecond(1525678482, 0, ZoneOffset.UTC);
    }

}