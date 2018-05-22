`RabbitMQ`提供多种客户端语言支持，`Java`的客户端文档请参考[Java客户端指南][java-client-api]。`RabbitMQ`入门参考[RabbitMQ向导][rabbitmq-tutorial]。

# com.rabbitmq.client.Channel 常用方法解析

# 交换机相关方法

## 交换机声明

### 方法定义

```java

    // 等价于 exchangeDeclare(exchange, type.getType(), false, false, false, null)
    Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type) throws IOException;

    // 等价于 exchangeDeclare(exchange, type.getType(), durable, false, false, null)
    Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable) throws IOException;

    // 等价于 exchangeDeclare(exchange, type.getType(), durable, autoDelete, false, arguments)
    Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException;

    // 等价于 exchangeDeclare(exchange, type.getType(), durable, autoDelete, internal, arguments)
    Exchange.DeclareOk exchangeDeclare(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException;

    // 不等待服务器响应，即声明的交换机不会接收来自 RabbitMQ 的返回消息，此时交换机声明后可能尚未创建完毕，后续相关操作必须通过 exchangeDeclarePassive 之类的手段来确保交换机存在。一般不建议使用
    void exchangeDeclareNoWait(String exchange, BuiltinExchangeType type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException;


    // 等价于 exchangeDeclare(exchange, type, false, false, false, null)
    Exchange.DeclareOk exchangeDeclare(String exchange, String type) throws IOException;

    // 等价于 exchangeDeclare(exchange, type, durable, false, false, null)
    Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable) throws IOException;

    // 等价于 exchangeDeclare(exchange, type, durable, autoDelete, false, arguments)
    Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, Map<String, Object> arguments) throws IOException;

    Exchange.DeclareOk exchangeDeclare(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException;

    void exchangeDeclareNoWait(String exchange, String type, boolean durable, boolean autoDelete, boolean internal, Map<String, Object> arguments) throws IOException;

```
　　交换机支持四种类型，分别是`fanout`、`direct`、`topic`、`headers`。客户端提供了`com.rabbitmq.client.BuiltinExchangeType`枚举来表示这四种类型。

```java

    public enum BuiltinExchangeType {

        DIRECT("direct"), FANOUT("fanout"), TOPIC("topic"), HEADERS("headers");

        private final String type;

        BuiltinExchangeType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

```

### 形参介绍

*exchange*

　　交换机的名称

*type*

　　交换机的类型

+ `fanout` 此类交换机会将所有收到的消息都直接发送到它所绑定的每一个交换机或者队列里。
+ `direct` 如果此类交换机在绑定交换机和队列时指定的`routingKey`（路由键）与消息发布时指定的`routingKey`完全一致，那么此类交换机会将所有收到的消息分别发送到这些交换机和队列里。
+ `topic` 与`direct`模式类似，但更加灵活。路由键采用点分字符串格式（即多个单词采用`.`作为分隔），提供了`*`和`#`模糊匹配。其中`*`匹配一个单词，`#`匹配零个单词或多个使用`.`分隔的单词。此类交换机会将所有收到的消息分别发送到路由键匹配的交换机和队列里。
+ `headers` 生产者在生产消息时需要将自定义头部参数写入到`AMQP.BasicProperties#headers`并与消息体一起发布出去，消费者在执行队列与交换机的绑定时也需要定义`AMQP.BasicProperties#headers`，通过`x-match`（可选值有：`all`：生产者的所有头部参数（除`x-match`）都匹配则消费；`any`：生产者的任意一个头部参数（除`x-match`）匹配则消费）指定消费模式。参考[headers类型交换机实例][exchange-type-headers]

*durable*

　　`durable`表示当前声明的交换机是否是持久化的。持久化的`exchange`、持久化的`queue`、持久化的`message body`三者同时成立时，可以保证`RabbitMQ`在退出或者发生崩溃等情况下消息不会丢失。（特殊情况：诸如消息还未写入到磁盘系统就断电，此时消息将会丢失）

*autoDelete*

　　`autoDelete`表示当前声明的交换机是否在与之相绑定的最后一个交换机或者队列解绑后自动删除。如果该交换机从未与其他交换机或者队列有绑定关系，则自动删除没作用。

*internal*

　　`internal`表示当前声明的交换机是内部的。内部交换机对客户端来说是不可见的，即客户端无法将消息发布到该交换机上，只能通过交换机与交换机的路由来传递消息。

*arguments*

　　`arguments`表示当前声明的交换机的额外参数配置，比如`alternate-exchange`参数，参考[备用路由配置][alternate-exchange]。


## 交换机存在性校验

### 方法定义

```java

    // 检测交换机是否存在，存在则返回 AMQP.Exchange.DeclareOk，不存在则抛出异常
    Exchange.DeclareOk exchangeDeclarePassive(String name) throws IOException;

```

　　


## 交换机绑定与解绑

### 方法定义

```java

    // 等价于 exchangeBind(destination, source, durable, routingKey, null)
    Exchange.BindOk exchangeBind(String destination, String source, String routingKey) throws IOException;

    Exchange.BindOk exchangeBind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException;

    // 不等待服务器响应，即交换机绑定不会接收来自 RabbitMQ 的返回消息，此时交换机绑定请求发出去后可能尚未绑定成功，一般不建议使用
    void exchangeBindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException;

    // 等价于 exchangeUnbind(destination, source, durable, routingKey, null)
    Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey) throws IOException;

    Exchange.UnbindOk exchangeUnbind(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException;

    // 不等待服务器响应，即交换机解绑不会接收来自 RabbitMQ 的返回消息，此时交换机解绑请求发出去后可能尚未解绑成功，一般不建议使用
    void exchangeUnbindNoWait(String destination, String source, String routingKey, Map<String, Object> arguments) throws IOException;

```

### 形参介绍

*destination* 

　　表示目标交换机，通常该交换机具有`internal`特性（即不可发布消息的交换机）

*source* 

　　表示源交换机，通常该交换机不具有`internal`特性（即可以发布消息的交换机）

*durable* 

　　表示本次绑定是否是持久化的

*routingKey* 

　　表示本次绑定所采用的路由键（交换机为`direct`或者`topic`类型时有用，其他类型建议直接用`""`空字符串）

*arguments* 

　　表示绑定的额外参数，用于`headers`类型交换机的头部参数。其他类型交换机直接使用`null`即可。


## 交换机删除

### 方法定义

```java

    // 等价于 exchangeDelete(exchange, false)
    Exchange.DeleteOk exchangeDelete(String exchange) throws IOException;

    Exchange.DeleteOk exchangeDelete(String exchange, boolean ifUnused) throws IOException;

    // 不等待服务器响应，即交换机删除不会接收来自 RabbitMQ 的返回消息，此时删除操作可能尚未完成，一般不建议使用
    void exchangeDeleteNoWait(String exchange, boolean ifUnused) throws IOException;

```

### 形参介绍

*exchange*

　　交换机名称

*ifUnused*

　　表示是否只在交换机没有被使用的情况下删除交换机。`true`表示只有此交换机没有被使用时才删除；`false`表示强制删除该交换机（即使该交换机正在使用）


# 队列相关方法

## 队列声明

### 方法定义

```java

    // 等价于 queueDeclare("", false, true, true, null)
    // 实际生成的队列名称由 RabbitMQ 确定
    Queue.DeclareOk queueDeclare() throws IOException;

    // 声明队列的最常用方法
    Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException;

    // 不等待服务端响应，即声明的队列不会接收来自 RabbitMQ 的返回消息，效率更高，但安全性较低，依赖于心跳检测来发现错误操作，一般很少使用
    void queueDeclareNoWait(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException;

    // 检测队列是否存在，存在则返回 AMQP.Queue.DeclareOk，不存在或者队列是其他连接的排他队列则抛出异常
    Queue.DeclareOk queueDeclarePassive(String queue) throws IOException;

```

### 形参介绍

*queue*

　　队列的名称

*durable*

　　`durable`表示当前声明的队列是否是持久化的。持久化的`exchange`、持久化的`queue`、持久化的`message body`三者同时成立时，可以保证`RabbitMQ`在退出或者发生崩溃等情况下消息不会丢失。（特殊情况：诸如消息还未写入到磁盘系统就断电，此时消息将会丢失）

*exclusive*

　　`exclusive`表示当前声明的队列是否是排他的。

　　排他队列只在首次声明该队列的连接中可见，并在连接断开时自动删除（当前连接的专有队列）。即排他队列有以下特性：

+ 排他队列是基于连接可见的，同一连接的不同信道可以同时访问该连接创建的排他队列。
+ `首次`表示当某个队列被声明为排他队列后，其他连接不允许创建相同名称的排他队列。（普通队列可以在不同连接中声明）。
+ 即使排他队列同时标记为`durable`，一旦连接关闭或者客户端退出，该排他队列依然会被自动删除，因此`durable`标志无效。

*autoDelete*

　　`autoDelete`表示当前声明的队列是否在最后一个消费者断连时自动删除。如果该队列从未与消费者建立连接，则自动删除没作用。

*arguments*

　　`arguments`表示当前声明的队列的额外参数配置，主要有：

+ `x-message-ttl`：即`Message TTL`，特性`Features=TTL`，数据类型为整型数值。可以用来设置整个队列中所有消息的生命周期或者在发布消息时为某个消息指定生命周期，单位为毫秒（ms）。当`TTL`跌至`0`时，消息将会从队列中删除，如果该队列绑定了多个消费者，则该特性失效。参考[队列消息生命周期和队列生命周期限制][ttl]。
+ `x-expires`：即`Auto Expire`，特性`Features=Exp`，数据类型为整型数值。当队列在指定时间内没有被访问（包括但不限于`basicConsume`、`basicGet`、`queueDeclare`）就会被删除，单位为毫秒（ms）。注意：设置了该特性后，`durable`配置将失效。参考[队列消息生命周期和队列生命周期限制][ttl]。
+ `x-max-length`：即`Max Length`，特性`Features=Lim`，数据类型为整型数值。限制队列中消息的数量，当队列中消息的数量大于指定值时，将超出数量的最早几条消息删除（类似`LRU`算法），单位为个。参考[队列消息数量限制][x-max-length]。
+ `x-dead-letter-exchange`：即`Dead letter exchange`，特性`Features=DLX`，数据类型为字符串（交换机的名称）。用于将死信消息转发到指定交换机（该交换机称为死信收容交换机），即当触发`x-message-ttl`或者`x-max-length`时根据将消息转发到死信收容交换机而不是直接删除。参考[死信收容交换机及死信收容路由键][dlx-dlk]。
+ `x-dead-letter-routing-key`：即`Dead letter routing key`，特性`Features=DLK`，数据类型为字符串（路由键的名称或者队列的名称）。用于将死信消息从死信收容交换机路由到指定的队列中。参考[死信收容交换机及死信收容路由键][dlx-dlk]。
+ `x-max-priority`：即`Maximum priority`，数据类型为整型数值，建议取值为`1`到`10`。优先级队列中的消息可以在发布时设置优先级（消息的优先级为无符号短整型，即`0`到`255`，建议优先级不高于队列的优先级），优先级越高的消息越先被消费。优先级队列需要消耗更多的磁盘、内存以及`CPU`，谨慎使用。参考[队列优先级][x-max-priority]。

### 方法介绍

*Queue.DeclareOk queueDeclare() throws IOException;*

　　声明队列，等同于执行`queueDeclare("", false, true, true, null)`。即生成排他的

*Queue.DeclareOk queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments) throws IOException*

　　声明队列，

*Queue.DeclareOk queueDeclarePassive(String queue) throws IOException;*

　　队列可访问性检测，如果队列不存在则抛出异常`com.rabbitmq.client.ShutdownSignalException`，如果队列是其他队列的

```java

    // 队列不存在时抛出异常信息
    Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no queue '${queueName}' in vhost '${vhost}', class-id=50, method-id=10)

```


# Channel 其他方法介绍

## basicPublish 消息发布

### 方法定义

```java

    basicPublish(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException;

    basicPublish(String exchange, String routingKey, boolean mandatory, AMQP.BasicProperties props, byte[] body) throws IOException;

    basicPublish(String exchange, String routingKey, boolean mandatory, boolean immediate, AMQP.BasicProperties props, byte[] body) throws IOException;

```

### 形参介绍

*exchange*

　　交换器（交换机）名称

_routingKey_

　　路由键

_mandatory_

　　`true`表示`exchange`根据自身类型和消息的路由键找不到符合条件的`queue`时立即执行`basicReturn`将消息返回给生产者
　　`false`表示当出现找不到`queue`时直接将消息丢弃
　　`true`简单理解：要求至少将消息路由到一个`queue`中，否则将消息返回给生产者
　　实例参考：`https:blog.csdn.net/u013256816/article/details/54914525`
    
_immediate_

　　`true`表示当交换器将消息路由到`queue`时若发现某个`queue`上没有绑定消费者，那么该消息将不放入该`queue`中；如果此时所有路由到的`queue`都没有消费者，那么执行`basicReturn`将消息返回给生产者
　　`false`表示出现所有`queue`都没有消费者时直接将消息丢弃
　　`true`简单理解：要求至少路由到的`queue`中有一个`queue`有消费者，否则将消息返回给生产者
　　**特别注意**：`immediate`字段从`RabbitMQ 3.0`开始就被移除了，因此该字段不可使用

_props_

　　消息的属性字段，用于配置消息持久化，自定义消息头部等

_body_

　　消息主体

　　`mandatory`和`immediate`是`AMQP`协议中`basicPublish`方法的两个标志位。两者都能实现当消息传递过程中出现消息不可达时将消息返回给生产者的功能

## basicQos 

### 方法定义

```java

    // 等价于 basicQos(0, prefetchCount, false)
    void basicQos(int prefetchCount) throws IOException;

    // 等价于 basicQos(0, prefetchCount, global)
    void basicQos(int prefetchCount, boolean global) throws IOException;
    
    void basicQos(int prefetchSize, int prefetchCount, boolean global) throws IOException;

```

### 形参介绍

*prefetchSize*

　　`RabbitMQ`未实现该功能

*prefetchCount*

　　若某个消费者正在处理`prefetchCount`个消息，则该消费者不会再接收来自服务端的新消息（此时服务端会将消息发送到其他不忙碌的消费者），只有当该消费者发送`basicAck`后服务端才会将新消息发送到该消费端。即每个消费者同一时间最多消费`prefetchCount`个消息。

*global*

　　`false`表示对信道上的每一个新的消费者都应用`prefetchCount`配置
　　`true`表示对信道上的所有消费者都应用`prefetchCount`配置

## basicAck 消息回执

### 方法定义

```java

    void basicAck(long deliveryTag, boolean multiple) throws IOException;

```

### 形参介绍

_deliveryTag_

　　消息索引，表示向服务端确认（应答）该消息已成功消费

_multiple_ 

　　是否批量应答，`true`表示向服务端确认（应答）所有（未确认、未应答）消息索引小于等于`deliveryTag`的消息已成功消费；
　　`false`表示仅仅确认（应答）指定消息索引的消息。

## basicConsume 消息消费

### 方法定义

```java

    // 等价于 basicConsume(queue, false, callback)
    String basicConsume(String queue, Consumer callback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", callback);
    String basicConsume(String queue, boolean autoAck, Consumer callback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, false, false, null, callback);
    String basicConsume(String queue, boolean autoAck, String consumerTag, Consumer callback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", false, false, arguments, callback)
    String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, Consumer callback) throws IOException;

    // 等价于 basicConsume(queue, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel
    String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException;

    // 等价于 basicConsume(queue, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel
    String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", false, false, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel
    String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", false, false, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, "", false, false, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, false, false, null, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel
    String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, false, false, null, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, false, false, null, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, String consumerTag, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel
    String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 等价于 basicConsume(queue, autoAck, consumerTag, noLocal, exclusive, arguments, consumer)
    // 其中 consumer 以 deliverCallback 实现覆盖 handleDelivery，以 cancelCallback 实现覆盖 handleCancel，以 shutdownSignalCallback 实现覆盖 handleShutdownSignal
    String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, DeliverCallback deliverCallback, CancelCallback cancelCallback, ConsumerShutdownSignalCallback shutdownSignalCallback) throws IOException;

    // 最终调用方法
    String basicConsume(String queue, boolean autoAck, String consumerTag, boolean noLocal, boolean exclusive, Map<String, Object> arguments, Consumer callback) throws IOException;

```

### 形参介绍

_queue_

　　消费者是与哪个队列关联的

_autoAck_

　　消息消费时是否自动确认回执。`true`表示当消息被`Consumer#handleDelivery`执行之后将会自动向服务端应答该消息消费成功；
　　`false`表示消息不会自动确认回执，需要程序显式执行`Channel#basicAck(deliveryTag, multiple)`向服务端应答。

_consumerTag_

　　客户端生成消费者标签，用于建立上下文关联。可以由我们自己定义（必须保证唯一）；也可以设置为空串，此时`RabbitMQ`将会自动生成一个唯一的标签。
　　当生产者发出`Channel#basicCancel`取消消息消费时用到，服务端可以通过`Consumer#handleCancel`处理这种消息取消。

_noLocal_

　　`true`表示在当前信道中（即当消费者和生产者在相同信道）发布的消息不要发送到该消费者。_注意_：`RabbitMQ`不支持该功能。

_exclusive_

　　`true`表示当前消费者是排他的。具体实例场景与说明参考：[排他消费者应用][exclusive-consume]

_arguments_

　　消息消费的其他参数配置。（？？）

_callback_

　　与指定队列相关联的消费者


## 超链接定义

[x-max-priority]: https://www.rabbitmq.com/priority.html "队列优先级以及消息优先级"
[x-max-length]: https://www.rabbitmq.com/maxlength.html "队列消息数量限制"
[dlx-dlk]: https://www.rabbitmq.com/dlx.html "死信收容交换机和死信收容路由键"
[ttl]: https://www.rabbitmq.com/ttl.html "队列消息生命周期和队列生命周期限制"
[java-client-api]: https://www.rabbitmq.com/api-guide.html "RabbitMQ客户端_Java版本"
[rabbitmq-tutorial]: https://www.rabbitmq.com/getstarted.html "RabbitMQ向导"
[alternate-exchange]: http://www.rabbitmq.com/ae.html "备用交换机"
[exchange-type-headers]: https://www.cnblogs.com/telwanggs/p/7124635.html "headers类型交换机"
[exclusive-consume]: https://blog.csdn.net/cadem/article/details/70157494 "使用排他消费者保证消息消费时有序"