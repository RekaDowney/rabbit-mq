package me.junbin.rabbitmq.spring.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:zhongjunbin@chinamaincloud.com">发送邮件</a>
 * @createDate : 2018/5/30 11:13
 * @description :
 */
@RestController
@RequestMapping(value = "/index")
public class IndexController {

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public Object index() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Welcome");
        return result;
    }

}