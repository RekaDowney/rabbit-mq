package me.junbin.rabbitmq;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-04-27 23:23
 * @description :
 */
public class TestSort {

    @Test
    public void testX() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.entrySet().add(new ImmutablePair<>("username", 12));
        System.out.println(map);
        map.entrySet().add(new ImmutablePair<>("prodcution", "chocolate"));
        System.out.println(map);
    }

    private List<Integer> source() {
        List<Integer> result = new ArrayList<>(10_0000);
        for (int i = 0; i < 10_0000; i++) {
            result.add(RandomUtils.nextInt(1, 5555555));
        }
        return result;
    }

    @Test
    public void test01() throws Exception {
        // 获取正向排序在3456的数据
        List<Integer> source = source();

        List<Integer> m1 = new ArrayList<>(source);
        System.out.println(LocalDateTime.now());
        m1.sort(Integer::compare);
        System.out.println(m1.get(3455));
        System.out.println(LocalDateTime.now());


    }

}