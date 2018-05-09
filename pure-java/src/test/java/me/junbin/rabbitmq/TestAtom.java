package me.junbin.rabbitmq;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/9 11:16
 * @description :
 */
public class TestAtom {

    @Test
    public void testAtomicBoolean() throws Exception {
        AtomicBoolean flag = new AtomicBoolean(false);
        assert !flag.get();

        Thread thread = new Thread(() -> flag.set(true));
        thread.start();

        thread.join(TimeUnit.SECONDS.toMillis(1));
//        TimeUnit.SECONDS.sleep(1);
        assert flag.get();

        flag.set(true);
        assert flag.get();
    }
}