package me.junbin.rabbitmq.util;

import me.junbin.commons.prop.KVTranslator;

import java.io.IOException;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/4/17 23:14
 * @description :
 */
public abstract class ConfigUtils {

    // 不需要懒加载
/*
    private static final class Holder {
        private static final KVTranslator MQ_TRANSLATOR;

        static {
            try {
                MQ_TRANSLATOR = KVTranslator.properties(ConfigUtils.class.getResourceAsStream("/bundle/rabbitmq.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
*/


    public static final KVTranslator MQ_TRANSLATOR;

    static {
        try {
            MQ_TRANSLATOR = KVTranslator.properties(ConfigUtils.class.getResourceAsStream("/bundle/rabbitmq.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}