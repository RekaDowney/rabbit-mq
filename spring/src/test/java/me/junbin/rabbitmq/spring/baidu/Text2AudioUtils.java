package me.junbin.rabbitmq.spring.baidu;

import me.junbin.commons.gson.Gsonor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/31 9:54
 * @description : <a href="http://yuyin.baidu.com/docs/tts/136">百度语音API</a>
 */
public abstract class Text2AudioUtils {

    private static final RestTemplate restTemplate;
    private static final HttpEntity<Object> NULL_REQUEST_ENTITY = null;
    private static final String TEXT2AUDIO_URL = "https://ss0.baidu.com/6KAZsjip0QIZ8tyhnq/text2audio?cuid=dict&ctp=1&pdt=30&tex={0}&lan={1}&vol={2}&spd={3}";

    public enum Language {

        // 仅支持 zh 和 en 两种语言

        ZH(Locale.CHINESE),

        EN(Locale.ENGLISH),;

        private final Locale value;

        Language(Locale value) {
            this.value = value;
        }

    }

    static {
        restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        converters.add(new GsonHttpMessageConverter() {
            {
                this.setGson(Gsonor.SIMPLE.getGson());
            }
        });
        restTemplate.setMessageConverters(converters);
        // 4XX 和 5XX 不抛出 RestClientResponseException 异常（包括：HttpClientErrorException、 HttpServerErrorException、HttpStatusCodeException）
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
    }

    /**
     * @param text           文本，需要转换为语音的文本
     * @param targetLanguage 语言，需要转换的语音语言，如果tex与lan不匹配，则会自动翻译
     * @param volume         音量，1到100
     * @param speed          语速
     * @return 语音的字节数组，可以
     */
    public static byte[] text2audio(String text, String targetLanguage, int volume, int speed) {
        ResponseEntity<byte[]> result = restTemplate.exchange(TEXT2AUDIO_URL, HttpMethod.GET, NULL_REQUEST_ENTITY, byte[].class, text, targetLanguage, volume, speed);
        if (result.getStatusCode() == HttpStatus.OK) {
            return result.getBody();
        }
        // 响应非 200（不抛出 RestClientResponseException 异常） 直接返回 null
        return null;
    }

    public static byte[] text2audio(String text, Language targetLanguage, int volume, int speed) {
        return text2audio(text, targetLanguage.value.getLanguage(), volume, speed);
    }

}

