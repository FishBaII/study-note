# 介绍

Future 类是异步思想的典型运用，主要用在一些需要执行耗时任务的场景，避免程序一直原地等待耗时任务执行完成，执行效率太低。
当我们执行某一耗时的任务时，可以将这个耗时任务交给一个子线程去异步执行，同时我们可以干点其他事情，不用傻傻等待耗时任务执行完成。
等我们的事情干完后，我们再通过 Future 类获取到耗时任务的执行结果。这样一来，程序的执行效率就明显提高了。

这其实就是多线程中经典的 Future 模式，你可以将其看作是一种设计模式，核心思想是异步调用，主要用在多线程领域，并非 Java 语言独有。

```java
// V 代表了Future执行的任务返回值的类型
public interface Future<V> {
    // 取消任务执行
    // 成功取消返回 true，否则返回 false
    boolean cancel(boolean mayInterruptIfRunning);
    // 判断任务是否被取消
    boolean isCancelled();
    // 判断任务是否已经执行完成
    boolean isDone();
    // 获取任务执行结果
    V get() throws InterruptedException, ExecutionException;
    // 指定时间内没有返回计算结果就抛出 TimeOutException 异常
    V get(long timeout, TimeUnit unit)

        throws InterruptedException, ExecutionException, TimeoutExceptio

}
```
-----

## CompletableFuture

### 介绍

Future 在实际使用过程中存在一些局限性比如不支持异步任务的编排组合、获取计算结果的 get() 方法为阻塞调用。

Java 8 才被引入CompletableFuture 类可以解决Future 的这些缺陷。CompletableFuture 除了提供了更为好用和强大的 Future 特性之外，还提供了函数式编程、异步任务编排组合（可以将多个异步任务串联起来，组成一个完整的链式调用）等能力。

下面我们来简单看看 CompletableFuture 类的定义。

```java
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> {
}
```

CompletionStage 接口描述了一个异步计算的阶段。很多计算可以分成多个阶段或步骤，此时可以通过它将所有步骤组合起来，形成异步计算的流水线。


### 简单使用


#### 创建
常见的创建 CompletableFuture 对象的方法如下：

* 通过 new 关键字。

```java
    // 通过 new 关键字创建 CompletableFuture 对象这种使用方式可以看作是将 CompletableFuture 当做 Future 来使用。
    CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
    // 调用 complete() 方法为其传入结果，这表示 resultFuture 已经被完成了。
    resultFuture.complete(rpcResponse);
    
```


* 基于 CompletableFuture 自带的静态工厂方法：runAsync()、supplyAsync() 。
其中runAsync()没有异步返回值，supplyAsync()可获取异步返回值

```java

    static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
    // 使用自定义线程池(推荐)
    static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor);
    static CompletableFuture<Void> runAsync(Runnable runnable);
    // 使用自定义线程池(推荐)
    static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor);

    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> System.out.println("hello!"));
    future.get();// 输出 "hello!"
    CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "hello!");
    assertEquals("hello!", future2.get());

```


#### 处理异步结果

* thenApply()
* thenAccept()
* thenRun()
* whenComplete()

```java
    
    //thenApply()获取异步结果处理后返回结果
    CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "hello!").thenApply(s -> s + "world!");
    assertEquals("hello!world!", future1.get());

    //thenAccept()获取异步结果并处理，但不返回结果
    CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> "hello!").thenAccept(System.out::println);
    //hello!

    //thenRun()不获取异步结果且不返回结果
    CompletableFuture<Void> future3 = CompletableFuture.supplyAsync(() -> "hello!").thenRun(() -> System.out.println("world!"));
    //world!


    //whenComplete()获取异步结果和异常对象，自身不返回结果但get()返回上一步结果
    CompletableFuture<String> future4 = CompletableFuture.supplyAsync(() -> "hello!")
            .whenComplete((res, ex) -> {
                // res 代表返回的结果
                // ex 的类型为 Throwable ，代表抛出的异常
                System.out.println(res);
                // 这里没有抛出异常所有为 null
                assertNull(ex);
            });
    assertEquals("hello!", future4.get());
    //hello!
```


#### 异常处理

handle(),exceptionally() 方法来处理任务执行过程中可能出现的抛出异常的情况。


```java

    CompletableFuture<String> future
            = CompletableFuture.supplyAsync(() -> {
        if (true) {
            throw new RuntimeException("Computation error!");
        }
        return "hello!";
    }).handle((res, ex) -> {
        // res 代表返回的结果
        // ex 的类型为 Throwable ，代表抛出的异常
        return res != null ? res : "world!";
    });
    assertEquals("world!", future.get());
    
    
    CompletableFuture<String> future
            = CompletableFuture.supplyAsync(() -> {
        if (true) {
            throw new RuntimeException("Computation error!");
        }
        return "hello!";
    }).exceptionally(ex -> {
        System.out.println(ex.toString());// CompletionException
        return "world!";
    });
    assertEquals("world!", future.get());

```

#### 组合 CompletableFuture

**thenCompose()** 按顺序链接两个 CompletableFuture 对象，实现异步的任务链。
它的作用是将前一个任务的返回结果作为下一个任务的输入参数，从而形成一个**依赖,先后关系**。

```java
        
        CompletableFuture<String> future
                = CompletableFuture.supplyAsync(() -> "hello!")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "future!"))
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + "world!"))
                ;
        assertEquals("hello!future!world!", future.get());

```

**thenCombine()** 链接两个 CompletableFuture 对象, 会在两个任务都执行完成后，把两个任务的结果合并。两个任务是并行执行的，它们之间并**没有先后依赖顺序**。


```java

        //thenCombine(), 链接两个completableFuture，并行异步完成后，合并结果
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> {
                    System.out.println("future1 done!");
                    return "hello!";
                })
                .thenCombine(CompletableFuture.supplyAsync(
                        () -> {
                            System.out.println("future2 done!");
                            return "world!";
                        }), (s1, s2) -> {
                    System.out.println("thenCombine done!");
                    return s1 + s2;
                });
        assertEquals("hello!world!", completableFuture.get());

```

**acceptEitherAsync()** 链接两个CompletableFuture对象, 在其中一个执行完成后，马上异步处理这个结果，
其中**acceptEither()**为非异步处理。

```java
        //acceptEither()，链接两个completableFuture，其中一个执行完毕后，处理这个结果
        CompletableFuture<Void> completableFuture
                = CompletableFuture.supplyAsync(() -> {
                    //do something in sync
                    System.out.println("future1 done!");
                    return "hello!";
                })
                .acceptEither(CompletableFuture.supplyAsync(
                        () -> {
                            //do something in sync
                            System.out.println("future2 done!");
                            return "world!";
                        }), (res) -> {
                    //when future1 done or future2 done, do something with res
                    System.out.println(res);
                });
```

#### 并行运行多个CompletableFuture

**allOf()**这个静态方法来并行运行多个 CompletableFuture。它们之间互不相关，并行运行。


```java

        Random rand = new Random();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("future1 done...");
            }
            return "abc";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("future2 done...");
            }
            return "efg";
        });

        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(future1, future2);
        //调用 join() 可以让程序等future1 和 future2 都运行完了之后再继续执行，不调用future也会异步运行，只是不能保证都完成了
        completableFuture.join();
        assertTrue(completableFuture.isDone());
        System.out.println("all futures done...");

```


**anyOf()** 方法不会等待所有的 CompletableFuture 都运行完成之后再返回，只要有一个执行完成即可！

```java

        Random rand = new Random();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("future1 done...");
            }
            return "abc";
        });
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000 + rand.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("future2 done...");
            }
            return "efg";
        });

        CompletableFuture<Object> completableFuture = CompletableFuture.anyOf(future1, future2);
        assertFalse(completableFuture.isDone());
        System.out.println("one futures done...");
        
```


#### 线程池

CompletableFuture 默认使用ForkJoinPool.commonPool() 作为执行器，这个线程池是全局共享的，可能会被其他任务占用，导致性能下降或者饥饿。
因此，建议使用自定义的线程池来执行 CompletableFuture 的异步任务，可以提高并发度和灵活性。

```java

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    
    CompletableFuture.runAsync(() -> {
         //...
    }, executor);
    
```

自定义线程池配置类

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("MY_EXECUTOR")
    public Executor myExecutor(){

        //get async config from file
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1);
        executor.setThreadNamePrefix("my-task-");
        executor.initialize();
        return executor;

    }

}
```


### 实际应用

1. 多线程访问第三方api，汇总返回

```java
public class AsyncService {

    //a bean for accessed third-party APIs
    @Autowired
    private AppService appService;
    
    public void process() {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        //skip init request list
        List<Account> reqList = new ArrayList<>();
        List<Account> rspList = new ArrayList<>();
        reqList.forEach(req -> futureList.add(CompletableFuture.runAsync(() -> callByClient(req, rspList))));
        //wait until all threads done
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        //output all response list
        System.out.println(rspList.size());
    }
    
    //call third-party APIs and handle response, skip exception catch
    private void callByClient(Account account, List<Account> rspList){
        Account accountResponse = appService.outputAny(account);
        System.out.println(accountResponse);
        rspList.add(accountResponse);
    }
    
}

```




