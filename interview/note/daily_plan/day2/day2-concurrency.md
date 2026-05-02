# Day 2：Java 并发编程深度复习

> 计划日期：Week 1 Day 2 | 主题：线程池、synchronized、Lock、volatile、CAS
> 输出要求：能画出线程池工作流程

---

## 一、核心概念速览

### 1.1 线程池（ThreadPoolExecutor）

**线程池的好处**：减少线程创建/销毁的开销，避免无限制创建线程导致 OOM 或过度上下文切换。

#### 7 个核心参数

| 参数 | 含义 |
|------|------|
| `corePoolSize` | 核心线程数，即使空闲也不销毁（除非 `allowCoreThreadTimeOut=true`） |
| `maximumPoolSize` | 最大线程数，核心线程满 + 队列满时才创建额外线程 |
| `keepAliveTime` | 额外空闲线程存活时间（非核心线程） |
| `unit` | `keepAliveTime` 的时间单位 |
| `workQueue` | 阻塞队列，存放等待执行的任务 |
| `threadFactory` | 线程工厂，用于创建新线程 |
| `handler` | 拒绝策略，当线程池 + 队列都满时的处理方式 |

#### 线程池工作流程（面试必画图）

```
                        提交任务
                           │
                           ▼
                    ┌───────────────┐
                    │ 核心线程满了？  │
                    └───────┬───────┘
                    N       │       Y
                    ▼       │       ▼
              创建核心线程    │  ┌──────────────┐
              执行任务       │  │  队列满了？   │
                            │  └──────┬───────┘
                            │  N      │      Y
                            │  ▼      │      ▼
                            │ 入队等待  │  ┌───────────────┐
                            │         │  │ 最大线程满了？  │
                            │         │  └──────┬────────┘
                            │         │  N      │      Y
                            │         │  ▼      │      ▼
                            │         │创建非核心 │  执行拒绝策略
                            │         │线程执行   │
                            │         │          │
                            ▼         ▼          ▼
                       ┌────────────────────────────────┐
                       │         线程执行完任务          │
                       │  非核心线程空闲超过 keepAliveTime │
                       │        → 回收非核心线程         │
                       └────────────────────────────────┘
```

**一句话总结**：核心线程 → 队列 → 非核心线程 → 拒绝策略，四级缓冲。

#### 4 种拒绝策略

| 策略 | 行为 | 适用场景 |
|------|------|---------|
| `AbortPolicy`（默认） | 抛 `RejectedExecutionException` | 必须感知任务被拒绝 |
| `CallerRunsPolicy` | 由调用者线程执行该任务 | 不能丢任务，能承受一定延迟 |
| `DiscardPolicy` | 静默丢弃新任务 | 允许丢任务（如日志采集） |
| `DiscardOldestPolicy` | 丢弃队列中最旧的任务，重试提交 | 优先处理最新数据 |

#### 核心线程数如何设置？

| 任务类型 | 公式 | 原因 |
|----------|------|------|
| **CPU 密集型** | `N + 1`（N=CPU 核数） | 多 1 个线程利用 CPU 空闲间隙（如缺页中断） |
| **I/O 密集型** | `2N` | I/O 期间 CPU 空闲，可让其他线程使用 CPU |

#### 常用阻塞队列对比

| 队列 | 特点 |
|------|------|
| `ArrayBlockingQueue` | 有界，数组结构，FIFO |
| `LinkedBlockingQueue` | 有界（可设）/ 无界，链表结构，吞吐量高于数组 |
| `SynchronousQueue` | 不存储元素，put 必须等待 take |
| `PriorityBlockingQueue` | 无界，支持优先级排序 |

> ⚠️ **阿里巴巴规约**：不要用 `Executors` 创建线程池！`newFixedThreadPool` 和 `newCachedThreadPool` 的队列无界或线程数无界，可能导致 OOM。**必须用 `ThreadPoolExecutor` 构造方法**。

---

### 1.2 synchronized 锁升级过程

> JDK 1.6 之后对 synchronized 做了大量优化，引入了**偏向锁 → 轻量级锁 → 重量级锁**的升级路径。

```
无锁状态
  │
  │ (同一线程反复获取)
  ▼
偏向锁（Mark Word 记录线程 ID）
  │        │
  │        │ (另一个线程竞争，CAS 撤销偏向锁)
  │        ▼
  │     轻量级锁（CAS 自旋获取）
  │        │
  │        │ (自旋 10+ 次仍失败，或竞争加剧)
  │        ▼
  │     重量级锁（操作系统 Monitor，线程阻塞/唤醒）
  │
  ▼
锁释放 → 回到无锁状态（不可降级！）
```

#### 各锁状态对比

| 锁状态 | 实现方式 | 适用场景 | 开销 |
|--------|---------|---------|------|
| **偏向锁** | Mark Word 记录持有线程 ID | 只有一个线程反复获取锁 | 极低 |
| **轻量级锁** | CAS 自旋 + Lock Record | 线程**交替**执行，无实际竞争 | 中等（消耗 CPU） |
| **重量级锁** | OS Mutex，未获取的线程阻塞 | 实际竞争激烈 | 高（涉及系统调用 + 上下文切换） |

> **关键点**：锁只能升级不能降级（偏向锁批量重偏向/撤销除外）。

#### synchronized 的使用方式

| 修饰对象 | 锁的对象 | 范围 |
|----------|---------|------|
| 普通方法 | `this`（当前实例） | 同一实例互斥 |
| 静态方法 | `类.class` | 所有实例互斥 |
| 代码块 `synchronized(obj)` | `obj` | 指定对象互斥 |
| 代码块 `synchronized(this)` | `this` | 同一实例互斥 |

---

### 1.3 ReentrantLock（显式锁）

> `ReentrantLock` 是基于 **AQS**（AbstractQueuedSynchronizer）实现的显式锁，JDK 1.5 引入。

#### synchronized vs ReentrantLock

| 维度 | synchronized | ReentrantLock |
|------|-------------|---------------|
| 实现 | JVM 关键字，C++ 实现 | JDK 类，基于 AQS（纯 Java） |
| 锁释放 | 自动释放（代码块/方法退出） | **必须**在 finally 中手动 `unlock()` |
| 可中断 | 不支持（一直阻塞） | 支持 `lockInterruptibly()` |
| 超时获取 | 不支持 | 支持 `tryLock(time, unit)` |
| 公平性 | 非公平 | 可选公平/非公平 |
| 条件变量 | `wait/notify`（单条件队列） | `Condition`（多条件队列） |
| 性能 | JDK 1.6+ 优化后相差不大 | 略低（额外对象开销） |

> **选型建议**：能用 synchronized 就用 synchronized（简洁、自动释放），需要可中断/超时/公平锁/多条件时才用 ReentrantLock。

#### AQS 简介

AQS 内部维护两个队列：
- **同步队列（CLH 双向链表）**：存放等待获取锁的线程，队头是即将获取锁的线程
- **条件队列（单向链表）**：存放因 `await()` 而等待的线程，被 `signal()` 唤醒后移到同步队列队尾

---

### 1.4 volatile 关键字

| 维度 | 说明 |
|------|------|
| **保证可见性** | 一个线程修改 volatile 变量后，其他线程立即可见（通过**内存屏障**强制刷入主存并失效其他 CPU 缓存行） |
| **禁止指令重排序** | 通过内存屏障禁止 JVM/JIT 对 volatile 变量的读写操作进行重排序 |
| **不保证原子性** | `volatile int i; i++;` → 非原子，多线程仍不安全！（读-改-写 三步） |

#### 典型使用场景

1. **状态标记位**：
```java
volatile boolean running = true;
// 线程 A 设置 running = false，线程 B 立刻退出循环
```

2. **DCL（双重检查锁定）单例**：
```java
public class Singleton {
    private static volatile Singleton instance;  // volatile 防止指令重排
    
    public static Singleton getInstance() {
        if (instance == null) {                  // 第一次检查
            synchronized (Singleton.class) {
                if (instance == null) {          // 第二次检查
                    instance = new Singleton();  // ①分配内存 ②初始化 ③赋值引用
                    // 没有 volatile 时可能 ①→③→②，其他线程拿到未初始化的对象
                }
            }
        }
        return instance;
    }
}
```

#### volatile 实现原理（JMM 层面）

- **写 volatile**：在写操作后插入 **StoreStore** + **StoreLoad** 屏障，强制刷主存
- **读 volatile**：在读操作前插入 **LoadLoad** + **LoadStore** 屏障，强制从主存取、禁止后续操作重排到读之前

---

### 1.5 CAS（Compare And Swap）

> CAS 是一条 CPU 原子指令（`cmpxchg`），用于实现无锁并发。Java 通过 `Unsafe` 类的 native 方法封装。

```
CAS(V, E, N)
  V: 内存地址（要更新的变量）
  E: 期望值（expected）
  N: 新值（new）

逻辑：如果 V 的当前值 == E，则将 V 更新为 N，返回 true；否则返回 false。

整个过程是 CPU 级别的原子操作。
```

#### ABA 问题

```
线程 1：CAS(A → B)，读取到 A
线程 2：CAS(A → B)，修改成功
线程 2：CAS(B → A)，改回 A
线程 1：CAS 执行，发现还是 A，修改成功
→ 线程 1 不知道 A 曾被改为 B 又改回来！
```

**解决方案**：`AtomicStampedReference` / `AtomicMarkableReference`，加版本号。

#### CAS 在 Java 中的应用

| 类/机制 | CAS 应用 |
|----------|---------|
| `AtomicInteger/AtomicLong/AtomicReference` | `compareAndSet()` |
| `ConcurrentHashMap`（JDK 1.8） | put 时空桶先用 CAS 尝试设置 |
| `ReentrantLock` | AQS 的 `state` 用 CAS 修改 |
| `LongAdder` | 分散到 `Cell[]` 数组，减少 CAS 竞争 |

---

## 二、面试高频题（带答案）

### Q1：线程池的 7 个参数分别是什么？工作流程是怎样的？

**7 个参数**：corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler

**流程图**（口述 + 白板画图）：
```
提交任务 → 核心线程未满 → 创建核心线程执行
                  ↓（核心线程满）
           → 入队列等待
                  ↓（队列满）
           → 创建非核心线程执行（总数 ≤ maximumPoolSize）
                  ↓（最大线程满）
           → 执行拒绝策略
```

---

### Q2：synchronized 锁升级过程是怎样的？

**偏向锁 → 轻量级锁 → 重量级锁（只升不降）**

1. **偏向锁**：Mark Word 记录持有线程 ID，同一线程反复获取无需 CAS
2. **轻量级锁**：有其他线程竞争时，撤销偏向锁，线程通过 CAS 自旋尝试获取
3. **重量级锁**：自旋超过一定次数（默认 10 次）或竞争线程数超过阈值，膨胀为重量级锁，未获取的线程进入阻塞状态

---

### Q3：synchronized 和 ReentrantLock 的区别？

| 维度 | synchronized | ReentrantLock |
|------|-------------|---------------|
| 实现层级 | JVM 内置 | JDK 类（AQS） |
| 锁释放 | 自动 | 手动 finally unlock |
| 中断响应 | 不支持 | `lockInterruptibly()` |
| 超时获取 | 不支持 | `tryLock(time, unit)` |
| 公平性 | 非公平 | 可选 |
| 条件变量 | 单条件 `wait/notify` | 多条件 `Condition` |

---

### Q4：volatile 能保证原子性吗？为什么？DCL 为什么需要 volatile？

**不能保证原子性**。`i++` 是三步操作（读-改-写），volatile 只保证单次读/写的可见性，不保证复合操作的原子性。

**DCL 需要 volatile**：`new Singleton()` 不是原子操作 — ①分配内存 ②初始化对象 ③赋值给引用。没有 volatile 时可能指令重排为 ①→③→②，其他线程在第一次 `if (instance == null)` 检查时拿到未初始化完成的对象，导致 NPE。

---

### Q5：什么是 CAS？有什么优缺点？

**CAS（Compare And Swap）**：CPU 原子指令，比较内存值与期望值，相等则更新。

| 优点 | 缺点 |
|------|------|
| 无锁，避免上下文切换 | **ABA 问题**（用版本号解决） |
| 高并发下性能优于锁 | **自旋消耗 CPU**（竞争激烈时不如锁） |
| 实现简单 | 只能保证**单个变量**的原子操作 |

---

### Q6：ThreadLocal 原理与内存泄漏问题

**原理**：每个线程内部有一个 `ThreadLocalMap`，key 是 `ThreadLocal` 的弱引用，value 是线程私有的变量副本。

**内存泄漏原因**：
- `ThreadLocalMap` 的 Entry 继承 `WeakReference`，key 是弱引用
- 当 `ThreadLocal` 对象没有外部强引用时，GC 会回收 key
- 但 **value 是强引用**，如果线程不销毁（如线程池），value 永远不会被回收 → 内存泄漏

**解决方法**：使用完 `ThreadLocal` 后**必须调用 `remove()`**。

```java
// 正确用法
ThreadLocal<String> tl = new ThreadLocal<>();
try {
    tl.set("value");
    // 使用...
} finally {
    tl.remove();  // 必须！
}
```

---

## 三、核心流程图（面试白板必备）

### 3.1 线程池工作流程（详细版）

```mermaid
flowchart TD
    A[提交任务] --> B{核心线程数 < corePoolSize?}
    B -- Yes --> C[创建核心线程执行任务]
    B -- No --> D{工作队列已满?}
    D -- No --> E[任务入队列等待执行]
    D -- Yes --> F{线程数 < maximumPoolSize?}
    F -- Yes --> G[创建非核心线程执行任务]
    F -- No --> H[执行拒绝策略]
    
    C --> I[任务执行完成]
    E --> J[等待核心线程空闲后执行]
    J --> I
    G --> I
    
    I --> K{线程空闲时间 > keepAliveTime?}
    K -- Yes --> L{是核心线程?}
    L -- No --> M[回收非核心线程]
    L -- Yes --> N[保留核心线程]
    K -- No --> N
```

### 3.2 synchronized 锁升级路径

```mermaid
flowchart LR
    A[无锁] -->|同一线程反复获取| B[偏向锁]
    B -->|其他线程竞争| C[轻量级锁<br/>CAS自旋]
    C -->|自旋失败/竞争加剧| D[重量级锁<br/>线程阻塞]
    
    style A fill:#90EE90
    style B fill:#87CEEB
    style C fill:#FFD700
    style D fill:#FF6B6B
```

---

## 四、手写代码

### 4.1 手写线程池任务执行流程伪代码

```java
public class ThreadPoolExecutorFlow {

    // ---------- 执行流程伪代码 ----------
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException();
        
        int c = ctl.get();  // ctl 高 3 位存状态，低 29 位存线程数
        
        // Step 1: 当前线程数 < 核心线程数 → 尝试直接创建核心线程
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true)) {  // true 表示核心线程
                return;
            }
            c = ctl.get();  // 重新读取状态
        }
        
        // Step 2: 线程池 RUNNING 状态 → 尝试入队列
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            // 二次检查：入队后线程池可能已关闭
            if (!isRunning(recheck) && remove(command)) {
                reject(command);
            } else if (workerCountOf(recheck) == 0) {
                // 防止任务在队列中但没有工作线程
                addWorker(null, false);
            }
        }
        
        // Step 3: 队列满了 → 尝试创建非核心线程
        else if (!addWorker(command, false)) {  // false 表示非核心线程
            // Step 4: 创建非核心线程也失败 → 拒绝
            reject(command);
        }
    }
    
    // ---------- 拒绝策略示例（CallerRunsPolicy） ----------
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();  // 由调用者线程直接执行
            }
        }
    }
}
```

### 4.2 手写 DCL 单例（volatile 版）

```java
public class Singleton {
    // volatile 防止指令重排
    private static volatile Singleton instance;
    
    private Singleton() {}
    
    public static Singleton getInstance() {
        if (instance == null) {                      // ① 第一次检查（无锁，快速路径）
            synchronized (Singleton.class) {         // ② 加类锁
                if (instance == null) {              // ③ 第二次检查（防止重复创建）
                    instance = new Singleton();      // ④ 创建实例
                    // 无 volatile 时：分配内存 → 赋值引用 → 初始化对象（重排后）
                    // 有 volatile 时：分配内存 → 初始化对象 → 赋值引用（安全）
                }
            }
        }
        return instance;
    }
}
```

### 4.3 手写生产者-消费者（wait/notify）

```java
class BoundedBuffer<T> {
    private final LinkedList<T> buffer = new LinkedList<>();
    private final int capacity;
    
    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }
    
    // ---------- 生产者 ----------
    public synchronized void put(T item) throws InterruptedException {
        while (buffer.size() == capacity) {  // while 而不是 if——防止虚假唤醒！
            wait();                           // 释放锁，进入等待队列
        }
        buffer.add(item);
        notifyAll();                          // 唤醒所有等待的消费者
    }
    
    // ---------- 消费者 ----------
    public synchronized T take() throws InterruptedException {
        while (buffer.isEmpty()) {
            wait();
        }
        T item = buffer.removeFirst();
        notifyAll();
        return item;
    }
}
```

---

## 五、实战练习

### 练习 1：线程池参数分析

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,      // corePoolSize
    4,      // maximumPoolSize
    60, TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(5),  // 容量 5
    new ThreadPoolExecutor.AbortPolicy()
);

// 问题：依次提交 10 个任务，执行顺序是什么？哪些任务被拒绝？
// 任务假设执行时间足够长（不会立即完成）
```

<details>
<summary>答案</summary>

```
1. 任务 1-2：核心线程满了 → 创建 2 个核心线程直接执行
2. 任务 3-7：入队列（容量 5）→ 队列满
3. 任务 8-9：队列满 + 核心线程满 → 创建非核心线程（max=4，已用 2，还可创建 2 个）
4. 任务 10：max 满了 + 队列满 → AbortPolicy → 抛 RejectedExecutionException

执行顺序（假设任务执行时间相同）：
  先完成：任务 1、2、8、9（4 个线程同时执行）
  再完成：任务 3、4、5、6、7（按入队顺序 FIFO）
  任务 10：被拒绝！
```
</details>

### 练习 2：volatile 原子性验证

```java
public class VolatileTest {
    private static volatile int count = 0;
    
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) count++;
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) count++;
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println(count);  // 期望 20000，实际 < 20000
    }
}
```

<details>
<summary>答案</summary>

输出结果小于 20000。`count++` 不是原子操作（读-改-写），volatile 只保证可见性不保证原子性。

**修复方案**：使用 `AtomicInteger` 的 `incrementAndGet()` 或 `synchronized`。
</details>

### 练习 3：判断死锁

```java
// 线程 A：
synchronized (lock1) {
    Thread.sleep(100);
    synchronized (lock2) { ... }
}

// 线程 B：
synchronized (lock2) {
    Thread.sleep(100);
    synchronized (lock1) { ... }
}

// 问题：会死锁吗？如何排查？
```

<details>
<summary>答案</summary>

**会死锁**。A 持有 lock1 等 lock2，B 持有 lock2 等 lock1。

**排查方法**：
- `jstack <pid>` 查看线程栈，会显示 `Found 1 deadlock`
- `jconsole` / `VisualVM` 可视化检测
- 代码层面：按固定顺序获取锁避免死锁
</details>

---

## 六、易错点/坑

| # | 易错点 | 说明 |
|---|--------|------|
| 1 | 用 `Executors` 创建线程池 | `newFixedThreadPool` 队列无界 → OOM；`newCachedThreadPool` 线程数无界 → OOM。**必须用 `ThreadPoolExecutor`** |
| 2 | 线程池 `submit()` 吞异常 | `submit` 返回 `Future`，必须 `future.get()` 才能感知异常；`execute` 会直接抛出 |
| 3 | `volatile` 误以为原子 | `volatile int i; i++;` 不原子！用 `AtomicInteger` 或加锁 |
| 4 | `wait()` 不用 `while` | `if` 判断条件被唤醒后不会重新检查，可能因**虚假唤醒**导致错误 |
| 5 | `synchronized` 锁的对象变了 | 锁 `String` 或包装类的引用被重新赋值后，锁的对象变化，同步失效 |
| 6 | `ThreadLocal` 用完不 `remove()` | 线程池场景下线程复用，value 一直存在 → 内存泄漏 + 脏数据 |
| 7 | `Lock` 不放在 `finally` 中释放 | 异常后锁未释放 → 其他线程永远阻塞 |

---

## 七、对比速查表

### 锁体系全家福

```
Java 锁
├── synchronized（隐式锁，JVM 内置）
│   ├── 偏向锁 → 轻量级锁 → 重量级锁（锁升级）
│   └── wait/notify/notifyAll（Monitor 条件队列）
│
├── Lock 接口（显式锁，JDK 类）
│   ├── ReentrantLock（可重入锁，基于 AQS）
│   │   ├── 公平锁 / 非公平锁
│   │   ├── tryLock / lockInterruptibly
│   │   └── Condition（多条件队列）
│   ├── ReentrantReadWriteLock（读写锁）
│   │   ├── 读锁（共享）
│   │   └── 写锁（排他）
│   └── StampedLock（JDK 8，乐观读）
│
├── CAS（无锁，CPU 原子指令）
│   ├── AtomicInteger / AtomicLong / AtomicReference
│   └── LongAdder（JDK 8，减少 CAS 竞争）
│
└── volatile（轻量同步，内存可见性）
    └── 适合状态标记 + DCL
```

---

## 八、今日自测 Checklist

- [ ] 能画出线程池工作流程图（4 步：核心线程 → 队列 → 非核心线程 → 拒绝）
- [ ] 能说出线程池 7 个参数的含义
- [ ] 能说出 4 种拒绝策略及其适用场景
- [ ] 能解释 `synchronized` 锁升级过程（偏向 → 轻量级 → 重量级）
- [ ] 能对比 `synchronized` vs `ReentrantLock`（至少 5 点）
- [ ] 能解释 `volatile` 的 3 个特性（可见性、禁止重排、不保证原子性）
- [ ] 能写出 DCL 单例并解释为什么需要 `volatile`
- [ ] 能解释 CAS 原理和 ABA 问题及解决方案
- [ ] 能说出 `ThreadLocal` 内存泄漏原因和解决方案
- [ ] 能手写生产者-消费者（`wait`/`notify` 模式）
- [ ] 能解释 `submit()` vs `execute()` 的区别
- [ ] 能说出为什么不能用 `Executors` 创建线程池

---

> **Day 2 完成！** 明天 Day 3：JVM（内存模型、GC、类加载机制）