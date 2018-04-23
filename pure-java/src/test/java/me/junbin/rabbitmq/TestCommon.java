package me.junbin.rabbitmq;

import me.junbin.rabbitmq.util.MqUtils;
import org.junit.Test;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/4/23 22:21
 * @description :
 */
public class TestCommon {

    @Test
    public void testStringByteLength() throws Exception {
        String key = "abcdefg.def";
        System.out.println(key.length());
        System.out.println(MqUtils.utf8Data(key).length);
    }

}