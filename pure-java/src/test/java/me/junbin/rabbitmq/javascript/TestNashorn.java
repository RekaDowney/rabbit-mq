package me.junbin.rabbitmq.javascript;

import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 11:37
 * @description : 参考<a href="https://www.jianshu.com/p/467aaf5254f8">Java 8 Nashorn 教程</a>
 */
public class TestNashorn {

    @Test
    public void test01() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        System.out.println(engine.getClass().getName());
        String content = "中文ABC+";
        System.out.println(engine.eval("encodeURIComponent(\"" + content + "\")"));

        ScriptEngine nashorn = manager.getEngineByName("Nashorn");
        assert engine != nashorn && engine.getClass() == nashorn.getClass();
        System.out.println(nashorn.getClass().getName());
    }

}