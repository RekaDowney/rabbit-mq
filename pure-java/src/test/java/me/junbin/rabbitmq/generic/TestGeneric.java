package me.junbin.rabbitmq.generic;

import org.junit.Test;

import java.util.*;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/4/28 9:59
 * @description :
 */
public class TestGeneric {

    // https://blog.csdn.net/yiifaa/article/details/73433623
    // https://www.cnblogs.com/drizzlewithwind/p/6100164.html
    @Test
    public void testGeneric() throws Exception {

        // 非限界通配，等价于 ? extends Object，即 ? 是 Object 或者 Object 的子类。
        // 此时只能读取数据，且读取出来的数据都是 Object 类型
        List<?> unbound = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        //
        // unbound.add(new Object());
        // add (capture<?>) in List cannot be applied to (java.lang.Object)

        Object idx0 = unbound.get(0);
        assert idx0.getClass() == Integer.class;
        Object idx1 = unbound.get(1);
        assert Objects.equals(idx1, 2);

        // 上界限定通配，? 是 Number 或者 Number 的子类。
        // 此时只能够读取数据，且读取出来的数据都是 Number 类型
        List<? extends Number> upperBound = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        assert upperBound.get(0).getClass() == Integer.class;
        assert Objects.equals(upperBound.get(1), 2);

        // 下界限定通配，? 是 Number 或者 Number 的父类。可近似看成 Number extends ? extends Object。
        // 此时可以读取数据，但读取出来的数据都是 Object 类型；
        // 此时可以写入数据，但写入的数据必须是 Number 或者 Number 的子类
        List<? super Number> lowerBound = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        assert lowerBound.get(0).getClass() == Integer.class;
        assert Objects.equals(lowerBound.get(1), 2);
        lowerBound.add(5);
        lowerBound.add(6.0);
        assert Objects.equals(lowerBound.get(4), 5);
        assert lowerBound.get(5).getClass() == Double.class;
        assert Objects.equals(lowerBound.get(5), 6.0);

        List<? super Number> consumer = new ArrayList<>(Arrays.asList(2.0, 3.0, 4.0));
        List<? extends Number> producer = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        consumer.addAll(producer);
        assert consumer.size() == 7;
        assert consumer.get(2).getClass() == Double.class;
        assert Objects.equals(consumer.get(2), 4.0);
        assert consumer.get(6).getClass() == Integer.class;
        assert Objects.equals(consumer.get(6), 4);

    }

    // 因为 goods 读取出来的数据都是 T 类型，
    // 要想让 stock 接收 goods 必须保证 stock 存储的泛型是 T 或者 T 的父类，此时才能保证类型兼容
    private <T> void pecs(Collection<? super T> stock, Collection<? extends T> goods) {
        stock.addAll(goods);
    }

    @Test
    public void testCovariant() throws Exception {
        // Java 中的数组是协变的，而泛型不是协变的

        Integer[] integerArray = {12, 13, 14};
        printNumberArray(integerArray);

        List<Integer> integerList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
        // Error: java: 不兼容的类型: java.util.List<java.lang.Integer>无法转换为java.util.List<java.lang.Number>
//        printNumberList(integerList);
    }

    private void printNumberArray(Number[] array) {
        System.out.println(Arrays.toString(array));
    }

    private void printNumberList(List<Number> lst) {
        System.out.println(lst);
    }

    @Test
    public void testCovariantArrayError() throws Exception {
        // 数组的协变在带来便捷的同时也带来了麻烦
        // 由于 Java 支持协变数组，所以 Integer[] 可以赋值给 Number[]
        Number[] array = new Integer[]{12};
        // 由于声明 array 为 Number[] 类型，因此下面的赋值语句编译器不会报错，但运行时报错
        // java.lang.ArrayStoreException: java.lang.Double
        array[0] = 12.3;
    }
}