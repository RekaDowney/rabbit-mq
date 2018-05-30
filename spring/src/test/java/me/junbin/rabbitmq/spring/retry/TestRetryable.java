package me.junbin.rabbitmq.spring.retry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static me.junbin.commons.ansi.ColorfulPrinter.cyan;
import static me.junbin.commons.ansi.ColorfulPrinter.green;
import static me.junbin.commons.ansi.ColorfulPrinter.yellow;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/29 11:36
 * @description :
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext-retry.xml"})
public class TestRetryable {

    @Autowired
    private OrderService orderService;

    @Test
    public void testRetryable() throws Exception {
        Map<String, Object> order = new HashMap<>();
        order.put("id", 1L);
        cyan(orderService.insert(order));

        System.out.println("\n\n\n");

        green(orderService.delete(order));

        yellow(orderService.delete(order));
    }

}