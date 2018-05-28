package me.junbin.rabbitmq.spring.message;

import java.util.Objects;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/5/28 10:58
 * @description :
 */
public class HeadersMessage implements MessageEntity {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String body;

    public HeadersMessage() {
    }

    public HeadersMessage(Long id, String body) {
        this.id = id;
        this.body = body;
    }

    @Override
    public String toString() {
        return "HeadersMessage{" +
                "id=" + id +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeadersMessage that = (HeadersMessage) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, body);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}