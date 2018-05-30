# Spring Retry 框架


## BackOff 退避算法实现重试

　　常用的退避算法有两种，一种是指定次数指定频率，即`FixedBackOff`；另一种是指定次数指定退避指数增长算法，即`ExponentialBackOff`。

### FixedBackOff

```java

    @Test
    public void testFixedBackOff() throws Exception {
        FixedBackOff backOff = new FixedBackOff();
        // 固定频率（最多5次【估最多失败重试4次】，每次时间间隔为 1 秒）
        backOff.setMaxAttempts(5);
        backOff.setInterval(TimeUnit.SECONDS.toMillis(1));

        BackOffExecution execution = backOff.start();

        int counter = 1;
        long nextBackOff;
        while ((nextBackOff = execution.nextBackOff()) != BackOffExecution.STOP) {
            // 业务逻辑
            ColorfulPrinter.red(String.format("第%d次", counter));

            // 业务处理过程中出现异常，休眠退避时间
            Thread.sleep(nextBackOff);
            counter++;
        }
    }

```

　　`FixedBackOff#maxAttempts`指定总执行次数，当所有失败次数达到该值时结束重试，`FixedBackOff#interval`指定每次执行失败的休眠时间（单位为毫秒）。

### ExponentialBackOff

```java

    @Test
    public void testExponentialBackOff() throws Exception {

        ExponentialBackOff backOff = new ExponentialBackOff();
        // 初始退避时间
        backOff.setInitialInterval(TimeUnit.MILLISECONDS.toMillis(500));
        // 递增倍数（即下次退避时间是上次的多少倍），必须大于 1
        backOff.setMultiplier(2.0);
        // 最大退避时间，当某次退避时间大于该值时，退避时间固定为当前值
        backOff.setMaxInterval(TimeUnit.SECONDS.toMillis(10));
        // 最大累计退避时间，当累计退避时间（即所有失败造成的退避时间求和）达到该值时返回 STOP
        backOff.setMaxElapsedTime(TimeUnit.SECONDS.toMillis(10));

        BackOffExecution execution = backOff.start();

        int counter = 1;
        long nextBackOff;
        while ((nextBackOff = execution.nextBackOff()) != BackOffExecution.STOP) {
            // 业务逻辑
            green(String.format("第%d次", counter));

            // 业务处理过程中出现异常，休眠退避时间
            Thread.sleep(nextBackOff);
            counter++;
        }
    }

```

　　`ExponentialBackOff#initialInterval`指定初始退避时间（单位为毫秒）；`ExponentialBackOff#multiplier`指定递增倍数（即下次退避时间是上次的多少倍），必须大于1；`ExponentialBackOff#maxInterval`指定最大退避时间，当某次退避时间大于该值时，退避时间固定为当前值；`ExponentialBackOff#maxElapsedTime`指定最大累计退避时间，当累计退避时间（即所有失败造成的退避时间求和）达到该值时返回 STOP。


## @Retryable 和 @Recover 切入点注解

　　`Spring Retry`提供了切面注解支持，通过`@Retryable`和`@Recover`注解可以实现重试

### 开启 Spring Retry 配置

```xml

    <!-- 扫描 Spring AOP 切面相关注解 -->
    <aop:aspectj-autoproxy expose-proxy="true" proxy-target-class="false"/>

    <!-- 加载 Spring Retry 切面配置 -->
    <bean class="org.springframework.retry.annotation.RetryConfiguration"/>
    
    <bean id="orderService" class="me.junbin.rabbitmq.spring.retry.OrderService"/>

```

　　在`xml`中配置相关的`SpringRetry`所需`Bean`，注意开启了切面注解扫描，因此需要引入`aspectjweaver`和`aopalliance`依赖包。

### 实例

```java

    @Service
    public class OrderService {
    
        private AtomicInteger insertCounter = new AtomicInteger(0);
        private AtomicInteger deleteCounter = new AtomicInteger(0);
        private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    
        // 当遇到 RuntimeException、IllegalArgumentException、IllegalStateException 异常时需要重试
        // 当 value/include 和 exclude 都为空时，所有异常都需要重试
        // stateful 标注是否重试是否是有状态的，数据库相关操作时需要置为 true 以便重试时开启新事务
        // backoff 用来指定失败重试的退避策略，默认不配置，表示立即重试，此时会阻塞线程（实际测试发现会休眠1秒）
        // maxAttempts 表示包括首次请求和重试次数的最多执行次数
        @Retryable(
                include = {
                        RuntimeException.class,
                        IllegalArgumentException.class,
                        IllegalStateException.class
                },
                maxAttempts = 4
        )
        public String insert(Object order) {
            LOGGER.info("准备入库订单：{}", Gsonor.SIMPLE.toJson(order));
            int oneToTen = RandomUtils.nextInt(1, 11);
            if (insertCounter.incrementAndGet() < 5) {
                if (oneToTen < 5) {
                    throw new RuntimeException("Insert. Less than 5");
                } else if (oneToTen < 7) {
                    throw new IllegalArgumentException("Insert. Greater or equal than 5 and Less than 7");
                } else if (oneToTen <= 10) {
                    throw new IllegalStateException("Insert. Greater or equal than 7 and Less than 11");
                }
            }
            insertCounter.set(0);
            return "OK";
        }
    
        // value/include 和 exclude 都为空时，所有异常都需要重试
        // backoff 用来指定失败重试的退避策略，初始退避时间为 0.5 秒，往后每一次退避时间都是上一次的 2 被，最大退避时间为 5 秒
        // maxAttempts 表示包括首次请求和重试次数的最多执行次数
        @Retryable(
                backoff = @Backoff(
                        delay = 500,
                        maxDelay = 5000,
                        multiplier = 2.0
                ),
                maxAttempts = 4
        )
        public String delete(Object order) {
            LOGGER.info("准备删除订单：{}", Gsonor.SIMPLE.toJson(order));
            int oneToTen = RandomUtils.nextInt(1, 11);
            if (deleteCounter.incrementAndGet() < 5) {
                if (oneToTen < 3) {
                    throw new RuntimeException("Delete. Less than 3");
                } else if (oneToTen < 6) {
                    throw new IllegalArgumentException("Delete. Greater or equal than 3 and Less than 6");
                } else if (oneToTen <= 10) {
                    throw new IllegalStateException("Delete. Greater or equal than 6 and Less than 11");
                }
            }
            deleteCounter.set(0);
            return "OK";
        }
    
        // 所有被
        // @Recover 当所有重试都失败后执行，此时该注解标注的的第一个参数必须是异常类（用于匹配最后一次重试失败抛出的异常）
        //      剩下的参数按顺序匹配 @Retryable 标注的方法的参数列表。
        // @Recover 标注的方法必须与 @Retryable 标注的方法拥有相同的返回类型，该返回值作为 @Retryable 的返回结果
        @Recover
        public String recoverForRuntimeException(RuntimeException e, Object order) {
            LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
            reset(e.getMessage());
            return "Error: RuntimeException";
        }
    
        @Recover
        public String recoverForIllegalArgumentException(IllegalArgumentException e, Object order) {
            LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
            reset(e.getMessage());
            return "Error: IllegalArgumentException";
        }
    
        @Recover
        public String recoverForIllegalStateException(IllegalStateException e, Object order) {
            LOGGER.info("多次入库订单：{} 失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), e.getMessage());
            reset(e.getMessage());
            return "Error: IllegalStateException";
        }
    
        private void reset(String errorMessage) {
            if (errorMessage.startsWith("Insert. ")) {
                insertCounter.set(0);
            } else if (errorMessage.startsWith("Delete. ")) {
                deleteCounter.set(0);
            }
        }
    
    }

```

　　`@Retryable`和`@Recover`注解标注的方法必须拥有相同的返回类型，当`@Retryable`的所有重试都失败时，`@Recover`返回值将作为`@Retryable`方法的返回值。
`@Recover`的参数列表第一个参数必须是异常类型，第二个参数往后与`@Retryable`的参数列表保持一致。

　　_特别注意_：如果`@Recover`修饰的方法存在重构，则非常可能发生`java.lang.ArrayIndexOutOfBoundsException`异常，比如在`OrderService`中添加如下的方法。
重新执行时将会抛出异常。而且当一个类中出现多个`@Retryable`注解标注的方法时，`@Recover`需要通过额外的手段来确认是哪个方法所有重试都失败。因此不建议使用切面注解的方式来实现`SpringRetry`。

```java

    // org.springframework.retry.annotation.RecoverAnnotationRecoveryHandler.recover 
    // org.springframework.retry.annotation.RecoverAnnotationRecoveryHandler.SimpleMetadata.getArgs()
    // java.lang.System.arraycopy
    // java.lang.ArrayIndexOutOfBoundsException
    @Recover
    public String recoverForRuntimeException(RuntimeException e, Object order, boolean physicalDelete) {
        LOGGER.info("多次入库订单：{} （physical: {}）失败，异常信息：{}", Gsonor.SIMPLE.toJson(order), physicalDelete, e.getMessage());
        reset(e.getMessage());
        return "Error: RuntimeException";
    }

```

## RetryTemplate 模板方法重试

　　`RetryTemplate`通过`RetryPolicy`指定重试策略和`BackOffPolicy`指定重试时的退避策略来实现重试功能，常用的方法有
`public final <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoveryCallback<T> recoveryCallback) throws E`和
`public final <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback, RecoveryCallback<T> recoveryCallback), RetryState retryState throws E, ExhaustedRetryException`
这两个方法。`RetryCallback`函数式接口用来描述支持重试的方法（必须携带返回值），`RecoveryCallback`函数式接口用来描述所有重试操作都失败的恢复操作（必须携带返回值），`RetryState`接口用来描述重试过程中的状态（以确定是否需要回滚）。

　　`RetryCallback`和`RecoveryCallback`的入参都是`RetryContext`，通过该上下文可以获取当前重试次数（`retryCount`为`0`表示正在执行原生方法，还未重试）；可以获取上一次重试抛出的异常；还可以从`RetryCallback`传递参数到`RecoveryCallback`等。

### 实例

```java

    public class TestRetryTemplate {
    
        private static final Logger LOGGER = LoggerFactory.getLogger(TestRetryTemplate.class);
    
        @Test
        public void test01() throws Exception {
            // 关于 RetryTemplate#execute(RetryCallback, RecoveryCallback, RetryState) 的第三个参数（用于有状态的重试）参考：https://blog.csdn.net/broadview2006/article/details/72841056
            RetryTemplate retryTemplate = new RetryTemplate();
            RetryPolicy retryPolicy = new SimpleRetryPolicy(5);
            retryTemplate.setRetryPolicy(retryPolicy);
            ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(TimeUnit.MILLISECONDS.toMillis(500));
            backOffPolicy.setMaxInterval(TimeUnit.SECONDS.toMillis(10));
            backOffPolicy.setMultiplier(2.0);
            retryTemplate.setBackOffPolicy(backOffPolicy);
            retryTemplate.setThrowLastExceptionOnExhausted(true);
    
            AtomicInteger counter = new AtomicInteger(0);
    
            // RetryCallback 第一次执行时 getRetryCount 返回 0，retryContext.getLastThrowable() 返回 null。
            // 第一次执行完毕后。如果期间抛出异常，则 retryCount 加 1，lastThrowable 表示该异常；否则 retryCount 不再自增
            Integer actions = retryTemplate.execute(
                    retryContext -> {
                        retryContext.setAttribute("key", "value");
                        LOGGER.info("第{}次重试", retryContext.getRetryCount());
                        if (counter.incrementAndGet() < 2) {
                            throw new RuntimeException();
                        }
                        return retryContext.getRetryCount();
                    },
                    retryContext -> {
                        LOGGER.info("尝试{}次都失败", retryContext.getRetryCount());
                        assert "value" == retryContext.getAttribute("key");
                        return retryContext.getRetryCount();
                    }
            );
    
            assert actions == 1;
        }
    
    }

```

## 建议

　　优先建议使用`RetryTemplate`进行重试、其次是自己根据`BackOff`实现重试，最后才建议使用切入点注解进行重试。














