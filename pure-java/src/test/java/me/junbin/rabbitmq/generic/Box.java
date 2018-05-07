package me.junbin.rabbitmq.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/4/28 10:04
 * @description :
 */
public class Box<T> implements Serializable {

    private T content;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Box.class);

    public Box() {
    }

    public Box(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Box{" +
                "content=" + content +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box<?> box = (Box<?>) o;
        return Objects.equals(content, box.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content);
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    // 生产指从泛型中获取数据，消费指将数据存入到泛型中

    // 限定泛型可以同时执行生产（get）消费（set）操作，并且所有数据交互都被限定为 T 类型
    public static <T> void boundOperation(Box<T> box, T content) {
        LOGGER.debug("限定泛型可以同时执行生产（从泛型中获取数据）消费（将数据写入泛型中）操作");
        LOGGER.info("将原始数据：{} 更改为：{}", box.getContent(), content);
        box.setContent(content);
    }

    // 非限界通配（等同于 ? extends Object），只能执行生产操作，此时的 ? 表示 Object 或者 Object 的子类型
    public static void unboundWildcard(Box<?> box) {
        LOGGER.debug("非限界通配，只能执行生产操作（从泛型中获取数据）");
        // 由于是非限界通配，因此取出来的数据只能够是 Object 以兼容所有类型
        Object content = box.getContent();
        LOGGER.info("获取数据：{}", content);
    }

    // 限界通配，只能执行生产操作，此时的 ? 表示 T 或者 T 的子类型，因此读取出来的数据可以使用 T 表示类型
    public static <T> void boundWildcard(Box<? extends T> box) {
        LOGGER.debug("限界通配，只能执行生产操作（从泛型中获取数据）");
        T content = box.getContent();
        LOGGER.info("获取数据：{}", content);
    }

    // 下界限定通配，生产操作读取的数据只能够用 Object 存储，因为此时 T 的类型上限只能是 Object
    // 执行消费操作，此时 ? 表示 T 或者 T 的父类型，因此可以将数据 T 写入到泛型中
    public static <T> void lowerBoundWildcard(Box<? super T> box, T cnt) {
        Object content = box.getContent();
        box.setContent(cnt);
    }


}