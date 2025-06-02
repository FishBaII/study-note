## 介绍

使用线程池的好处是减少在创建和销毁线程上所消耗的时间以及系统资源开销，解决资源不足的问题。
如果不使用线程池，有可能会造成系统创建大量同类线程而导致消耗完内存或者“过度切换”的问题。


## 使用

线程池的创建可以通过创建 ThreadPoolExecutor 对象或者调用 Executors 的工厂方法来创建线程池

> 不建议使用Executors方式创建，因为使用无参的构造函数可能导致内存资源等异常

### 构造函数与参数

```java

     /**
      * ThreadPoolExecutor的构造器
      */
     public ThreadPoolExecutor(int corePoolSize,//核心线程数量
                               int maximumPoolSize,//线程池的最大线程数
                               long keepAliveTime,//当线程数大于核心线程数时，多余的空闲线程存活的最长时间
                               TimeUnit unit,//同上，时间单位
                               BlockingQueue<Runnable> workQueue,//用来储存等待执行任务的队列
                               ThreadFactory threadFactory,//(可选)executor 创建新线程的时候会用到
                               RejectedExecutionHandler handler//（可选）当队列和线程池都满了时拒绝任务的策略
     );
     
```


1. corePoolSize：核心线程池大小， 当新的任务到线程池后，线程池会创建新的线程（即使有空闲线程），直到核心线程池已满。
2. maximumPoolSize：线程池能创建的线程的最大数目；当核心空闲线程已满，等待队列已满情况下才会去创建额外线程，总线程数量不可超过此值
3. keepAliveTime：程池的额外工作线程空闲后，保持存活的时间，核心线程不会被销毁，如果此值为0，则额外空闲线程在等待队列为空时会立马销毁
4. TimeUnit： 时间单位
5. BlockingQueue：如果没有空余线程，任务会先进入此等待队列，如果超出等待任务数量超过队列最大值，则会创建额外线程，如果无法再创建线程则按照拒绝策略处理
    * ArrayBlockingQueue：基于有界阻塞数组，先进先出
    * LinkedBlockingQueue：基于阻塞链表结构，先进先出,吞吐量比数组更高
    * SynchronousQueue：一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态
    * PriorityBlockingQueue：一个具有优先级得无限阻塞队列。
6. threadFactory：线程工厂
7. RejectedExecutionHandler： 当队列和线程池都满了时拒绝任务的策略
   * ThreadPoolExecutor.AbortPolicy：抛出 RejectedExecutionException来拒绝新任务的处理。
   * ThreadPoolExecutor.CallerRunsPolicy：调用执行自己的线程运行任务，也就是直接在调用execute方法的线程中运行(run)被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这种策略会降低对于新任务提交速度，影响程序的整体性能。如果您的应用程序可以承受此延迟并且你要求任何一个任务请求都要被执行的话，你可以选择这个策略。
   * ThreadPoolExecutor.DiscardPolicy： 不处理新任务，直接丢弃掉。
   * ThreadPoolExecutor.DiscardOldestPolicy： 此策略将丢弃最早的未处理的任务请求。

### 其他常用api

* execute()方法用于提交不需要返回值和不关心任务状态的任务，所以无法判断任务是否被线程池执行成功与否；
* submit()方法用于提交需要返回值的任务。线程池会返回一个 Future 类型的对象，通过这个 Future 对象可以判断任务是否执行成功 ，
1. 任务状态监控（isDone()）
2. 任务取消(cancel())
3. 阻塞等待任务完成即使没有返回(get())
4. 异常处理
```java
Future<?> future = executor.submit(() -> {
    throw new RuntimeException("任务异常");
});

try {
    future.get();  // 这里会抛出ExecutionException包装的原始异常
} catch (ExecutionException e) {
    Throwable cause = e.getCause();  // 获取原始异常
}
```
5. 特殊返回值处理
```java
Future<String> future = executor.submit(runnableTask, "预设结果");
String result = future.get();  // 获取"预设结果"
```

* shutdown（） :关闭线程池，线程池的状态变为 SHUTDOWN。线程池不再接受新任务了，但是队列里的任务得执行完毕。
* shutdownNow（） :关闭线程池，线程的状态变为 STOP。线程池会终止当前正在运行的任务，并停止处理排队的任务并返回正在等待执行的 List。
* isShutDown 当调用 shutdown() 方法后返回为 true。
* isTerminated 当调用 shutdown() 方法后，并且所有提交的任务完成后返回为 true

----

### 例子

```java
public class MyRunnable implements Runnable {

    private String command;

    public MyRunnable(String s) {
        this.command = s;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End. Time = " + new Date());
    }

    private void processCommand() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return this.command;
    }
}

```


```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorDemo {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 6;
    private static final Long KEEP_ALIVE_TIME = 1L;
    
    
    public static void main(String[] args) {

        //使用阿里巴巴推荐的创建线程池的方式
        //通过ThreadPoolExecutor构造函数自定义参数创建
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 10; i++) {
            //创建WorkerThread对象（WorkerThread类实现了Runnable 接口）
            Runnable worker = new MyRunnable("" + i);
            //执行Runnable
            executor.execute(worker);
        }
        //终止线程池
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
}



```

output
```
pool-2-thread-2 Start. Time = Sat Jan 04 16:29:35 CST 2025
pool-2-thread-3 Start. Time = Sat Jan 04 16:29:35 CST 2025
pool-2-thread-4 Start. Time = Sat Jan 04 16:29:35 CST 2025
pool-2-thread-1 Start. Time = Sat Jan 04 16:29:35 CST 2025
pool-2-thread-1 End. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-3 End. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-4 End. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-3 Start. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-4 Start. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-2 End. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-1 Start. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-2 Start. Time = Sat Jan 04 16:29:37 CST 2025
pool-2-thread-3 End. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-4 End. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-1 End. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-2 End. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-4 Start. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-3 Start. Time = Sat Jan 04 16:29:39 CST 2025
pool-2-thread-3 End. Time = Sat Jan 04 16:29:41 CST 2025
pool-2-thread-4 End. Time = Sat Jan 04 16:29:41 CST 2025
```


### spring内置线程池ThreadPoolTaskExecutor

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("MY_EXECUTOR")
    public Executor myExecutor(){

        //get async config from file
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("my-task-");
        executor.initialize();
        return executor;

    }

}
```

### 调度线程池ScheduledThreadPoolExecutor

ScheduledThreadPoolExecutor 主要用来在给定的延迟后运行任务，或者定期执行任务。

1. 当调用 ScheduledThreadPoolExecutor 的 scheduleAtFixedRate() 方法或者 scheduleWithFixedDelay() 方法时，
会向 ScheduledThreadPoolExecutor 的 DelayQueue 添加一个实现了 RunnableScheduledFuture 接口的 ScheduledFutureTask 。
2. 线程池中的线程从 DelayQueue 中获取 ScheduledFutureTask，然后执行任务。

>- 复杂场景及生产环境建议使用其他定时任务框架，如Quartz、xxl-job等，可以支持集群部署，数据持久化，任务失败处理等更多功能


```java
public class Main {
    
    //传入核心线程数创建ScheduledThreadPoolExecutor，其他构造器参数可参考ThreadPoolExecutor构造器参数说明
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
            CORE_POOL_SIZE
    );

    //scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
    //command: 执行的任务
    //initialDelay: 初始延迟的时间
    //delay: 上次执行结束，延迟多久执行
    //unit：单位
    executor.scheduleWithFixedDelay(()->{
        System.out.println(Thread.currentThread().getName() + " Start. Time = " + new Date());
        try {
            Thread.currentThread().sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
   }, 1, 2, TimeUnit.SECONDS);
        
}

```

输出如下，任务执行时间为1000ms，执行完等待时间为2000ms
```
//pool-3-thread-1 Start. Time = Sun Jan 12 00:30:47 CST 2025
//pool-3-thread-1 Start. Time = Sun Jan 12 00:30:51 CST 2025
//pool-3-thread-1 Start. Time = Sun Jan 12 00:30:54 CST 2025
//pool-3-thread-1 Start. Time = Sun Jan 12 00:30:57 CST 2025
//....
```


### 核心线程数的设置

* CPU 密集型任务(N+1)： 这种任务消耗的主要是 CPU 资源，可以将线程数设置为 N（CPU 核心数）+1，
比 CPU 核心数多出来的一个线程是为了防止线程偶发的缺页中断，或者其它原因导致的任务暂停而带来的影响。
一旦任务暂停，CPU 就会处于空闲状态，而在这种情况下多出来的一个线程就可以充分利用 CPU 的空闲时间。
* I/O 密集型任务(2N)： 这种任务应用起来，系统会用大部分的时间来处理 I/O 交互，而线程在处理 I/O 的时间段内不会占用 CPU 来处理，
这时就可以将 CPU 交出给其它线程使用。因此在 I/O 密集型任务的应用中，我们可以多配置一些线程，具体的计算方法是 2N。


