

### 创建线程

Thread: 使用构造方法或继承，提供了更多线程控制方法（如 start(), interrupt(), join() 等）
Runable: 接口，需实现run()方法，可以多个线程共享Runable实例，适合跟线程池等工具配合开发
Runable: 接口，需实现call()方法，有返回值；Callable 可以抛出检查异常，Runnable 不能；


```java
public class ThreadTest {

    @Test
    void test() throws ExecutionException, InterruptedException {

        Thread thread = new Thread(() -> System.out.println(Thread.currentThread().getName() + ":Thread任务"));
        Runnable runnable = () -> {
            System.out.println(Thread.currentThread().getName() + ":Runnable任务");
        };

        //这里方便测试使用Executors创建线程池，实际工作请使用ThreadPoolExecutor构造方法
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> simpleCallable = () -> Thread.currentThread().getName() + ":Callable任务";
        
        
        thread.start();
        new Thread(runnable).start();
        
        //Thread 没有机制处理返回值，而 Callable 需要专门的机制（如 Future）来处理结果
        Future<String> randomFuture = executor.submit(simpleCallable);
        //使用Future.get()阻塞获取Callable返回
        System.out.println(randomFuture.get());
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        //使用FutureTask.get()阻塞获取Callable返回
        System.out.println(futureTask.get());
        
        
        //Thread-0:Thread任务
        //Thread-1:Runnable任务
        //pool-1-thread-1:Callable任务
        //Thread-2:Callable任务

    }
    
}
```

