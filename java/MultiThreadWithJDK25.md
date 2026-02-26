
```java

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 演示并对比虚拟线程 (Project Loom) 与传统并发模型的差异。
 */

/**
 * JVM 内部操作，无需操作系统介入： 虚拟线程的核心优势体现在 I/O 阻塞时的切换上。
 * 当一个虚拟线程因为 I/O 而被“停放”（Park）时，JVM 做的仅仅是：
 1. 把它栈上的数据从平台线程的栈复制到堆上的一个小对象里。
 2. 让平台线程（“超级员工”）立即去执行另一个已经准备好的虚拟线程。
 * 这个过程完全在 JVM 内部完成，本质上只是改变了一些内存指针的指向，不涉及任何内核态切换，也不需要操作系统调度器介入。
 * 这就像“超级员工”把一张需要等待的任务单贴在墙上，然后立刻拿起桌上另一张可以做的任务单，几乎没有浪费任何时间。CPU
 缓存也能保持“热”状态，性能损失极小。
 */


/**
 * 假设cpu为6个，现有12个任务，每个任务（io密集型）需要执行2s
 现在我们看看虚拟线程是如何处理这 12 个任务的：

 1. 任务启动 (时间点: 0秒):
 * 你将 12 个任务提交给 newVirtualThreadPerTaskExecutor()。
 * JVM 瞬间创建了 12 个虚拟线程，并将它们挂载到默认的 6 个平台线程上开始运行。

 2. 遇到 I/O 阻塞 (时间点: 接近0秒):
 * 几乎在启动的同时，这 12 个任务全部走到了需要 I/O 等待 2 秒的那行代码。
 * 关键点来了： 当每个虚拟线程遇到 I/O 阻塞时，JVM 会立刻把它从平台线程上“卸载”下来。
 * 这意味着，几乎在瞬间，全部 12 个任务都进入了“等待”状态，而那 6 个宝贵的平台线程全部被释放了，变得空闲。

 3. 等待期间 (0-2秒):
 * 在这 2 秒内，12 个任务的 I/O 等待是由操作系统在后台处理的。JVM
 的平台线程可以去干别的活（如果还有的话），或者就保持空闲。没有任何线程资源因为“傻等”而被占用。

 4. 任务完成 (时间点: 2秒):
 * 大约在 2 秒后，操作系统完成了全部 12 个任务的 I/O 操作。
 * 这 12 个虚拟线程重新变为“可运行”状态，JVM
 再次将它们挂载到平台线程上，以完成后续工作（在这个例子里没有后续工作了）。

 所以，使用虚拟线程的总耗时，只取决于那批任务中最长的一个 I/O 等待时间。

 总耗时大约是 2 秒。
 */
@SpringBootTest
class VirtualThreadsTest {

    /**
     * JDK 25+ 风格: 使用虚拟线程
     * 此测试在虚拟线程中启动 10 个任务以演示其执行流程。代码直观且易于理解。
     * 每个任务都会打印其开始和结束信息，并休眠 1 秒，以模拟一个阻塞的 I/O 操作。
     * 由于所有任务都在虚拟线程中并发执行，整个测试的完成时间将略多于 1 秒，而不是 10 秒。
     */
    @Test
    void runManyConcurrentTasksWithVirtualThreads() {
        // 为了方便观察输出，我们将任务数量减少到 10。
        // 原测试中的 100,000 任务量用于演示可伸缩性。
        final int numberOfTasks = 10;
        final AtomicInteger completedTasks = new AtomicInteger(0);

        System.out.printf("--- 【虚拟线程测试】开始：准备启动 %d 个任务。 ---\n", numberOfTasks);

        // 使用 try-with-resources 语句确保执行器被关闭。
        // newVirtualThreadPerTaskExecutor() 是一种为每个任务启动一个新虚拟线程的简单方法。
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < numberOfTasks; i++) {
                final int taskId = i + 1;
                executor.submit(() -> {
                    System.out.printf("虚拟线程任务 %d 已开始。线程: %s\n", taskId, Thread.currentThread());
                    try {
                        // 模拟一个阻塞操作
                        Thread.sleep(Duration.ofSeconds(1));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    completedTasks.incrementAndGet();
                    System.out.printf("虚拟线程任务 %d 已完成。线程: %s\n", taskId, Thread.currentThread());
                });
            }
        } // 执行器在此处自动关闭，并等待所有任务完成。

        System.out.println("--- 【虚拟线程测试】所有任务已提交并等待完成... ---");
        System.out.printf("--- 【虚拟线程测试】结束：总共完成了 %d 个任务。 ---\n", completedTasks.get());

        assertEquals(numberOfTasks, completedTasks.get(), "所有 " + numberOfTasks + " 个任务都应该完成");
    }

    /**
     * JDK 17 风格: 使用 CompletableFuture 和平台线程池
     * 为了应对高并发，传统方式需要使用异步编程模型（如 CompletableFuture）和平台线程池。
     * 这种方式代码更复杂，需要处理回调和显式的线程池管理。
     */
    @Test
    void runManyConcurrentTasksWithCompletableFuture_JDK17_Style() {
        final int numberOfTasks = 10;
        final AtomicInteger completedTasks = new AtomicInteger(0);

        System.out.printf("--- 【CompletableFuture测试】开始：准备启动 %d 个任务。 ---\n", numberOfTasks);

        // 1. 必须手动创建一个固定大小的平台线程池。线程数是有限的宝贵资源。
        // 如果任务数远超核心线程数，任务将在队列中等待。
        ExecutorService platformThreadPool = Executors.newFixedThreadPool(10);
        
        try {
            List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfTasks)
                .mapToObj(i -> {
                    final int taskId = i + 1;
                    // 2. 使用 runAsync 将任务提交到线程池中进行异步执行。
                    return CompletableFuture.runAsync(() -> {
                        System.out.printf("CompletableFuture任务 %d 已开始。线程: %s\n", taskId, Thread.currentThread());
                        try {
                            // 模拟一个阻塞操作
                            Thread.sleep(Duration.ofSeconds(1));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        completedTasks.incrementAndGet();
                        System.out.printf("CompletableFuture任务 %d 已完成。线程: %s\n", taskId, Thread.currentThread());
                    }, platformThreadPool); // 必须传入执行器
                })
                .collect(Collectors.toList());

            // 3. 必须手动等待所有异步任务完成。
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            // 4. 必须手动关闭线程池。
            platformThreadPool.shutdown();
        }

        System.out.println("--- 【CompletableFuture测试】所有任务已完成。 ---");
        System.out.printf("--- 【CompletableFuture测试】结束：总共完成了 %d 个任务。 ---\n", completedTasks.get());
        assertEquals(numberOfTasks, completedTasks.get(), "所有 " + numberOfTasks + " 个任务都应该完成");
    }
}

```