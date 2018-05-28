package me.junbin.rabbitmq.spring.converter;

import com.google.gson.Gson;
import me.junbin.commons.gson.Gsonor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractJsonMessageConverter;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/26 21:51
 * @description :
 */
public class RabbitUtf8GsonMessageConverter extends AbstractJsonMessageConverter {

    private Gson gson = Gsonor.SIMPLE.getGson();
    private ClassMapper classMapper = new DefaultClassMapper();
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public RabbitUtf8GsonMessageConverter() {
    }

    public RabbitUtf8GsonMessageConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] bytes = this.gson.toJson(object).getBytes(UTF8);
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
//        messageProperties.setContentEncoding(charset.name());
        messageProperties.setContentLength(bytes.length);
        this.classMapper.fromClass(object.getClass(), messageProperties);
        return new Message(bytes, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            if (MessageProperties.CONTENT_TYPE_JSON.equals(properties.getContentType())) {
//                String encoding = properties.getContentEncoding();
//                Charset encodingCharset = encoding == null ? this.charset : Charset.forName(encoding);
                Class<?> targetClass = this.classMapper.toClass(message.getMessageProperties());
                content = this.gson.fromJson(new String(message.getBody(), UTF8), targetClass);
            }
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }

    public Gson getGson() {
        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public ClassMapper getClassMapper() {
        return classMapper;
    }

    @Override
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

}