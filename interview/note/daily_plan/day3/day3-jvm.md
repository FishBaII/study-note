# Day 3：JVM 深度复习

> 计划日期：Week 1 Day 3 | 主题：内存模型、GC、类加载机制
> 输出要求：能画出 JVM 内存结构图

---

## 一、JVM 内存结构全景图

> 面试第一问通常是："请画出 JVM 内存结构图"

### 1.1 内存结构总览（JDK 8+）

```
┌─────────────────────────────────────────────────────────┐
│                     JVM 内存结构 (JDK 8+)                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────┐  ┌──────────────────────┐ │
│  │      线程私有区域         │  │     线程共享区域       │ │
│  ├─────────────────────────┤  ├──────────────────────┤ │
│  │                         │  │                      │ │
│  │  ┌───────────────────┐  │  │  ┌────────────────┐  │ │
│  │  │   程序计数器 (PC)   │  │  │  │      堆         │  │ │
│  │  │   (当前执行字节码行号)│  │  │  │    (Heap)      │  │ │
│  │  └───────────────────┘  │  │  │  ┌──────────┐  │  │ │
│  │                         │  │  │  │  新生代   │  │  │ │
│  │  ┌───────────────────┐  │  │  │  │ Eden     │  │  │ │
│  │  │     虚拟机栈       │  │  │  │  │ S0 / S1  │  │  │ │
│  │  │   (VM Stack)      │  │  │  │  ├──────────┤  │  │ │
│  │  │  ┌─────────────┐  │  │  │  │  │  老年代   │  │  │ │
│  │  │  │  栈帧 Frame  │  │  │  │  │  │  (Old)   │  │  │ │
│  │  │  │  局部变量表  │  │  │  │  │  └──────────┘  │  │ │
│  │  │  │  操作数栈    │  │  │  │  └────────────────┘  │ │
│  │  │  │  动态链接    │  │  │  │                      │ │
│  │  │  │  返回地址    │  │  │  │  ┌────────────────┐  │ │
│  │  │  └─────────────┘  │  │  │  │    方法区/元空间  │  │ │
│  │  └───────────────────┘  │  │  │  (Metaspace)    │  │ │
│  │                         │  │  │  类信息/常量/   │  │ │
│  │  ┌───────────────────┐  │  │  │  静态变量       │  │ │
│  │  │   本地方法栈       │  │  │  └────────────────┘  │ │
│  │  │ (Native Method)   │  │  │                      │ │
│  │  └───────────────────┘  │  │  ┌────────────────┐  │ │
│  │                         │  │  │   运行时常量池   │  │ │
│  │                         │  │  │ (Runtime Const) │  │ │
│  │                         │  │  └────────────────┘  │ │
│  └─────────────────────────┘  └──────────────────────┘ │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │               直接内存 (Direct Memory)             │  │
│  │           (堆外内存，NIO Buffer 使用)               │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 1.2 五大区域速查表

| 区域 | 存储内容 | 线程 | 异常 | JDK 8 变化 |
|------|---------|------|------|-----------|
| **程序计数器** | 当前线程执行的字节码行号指示器 | 私有 | 唯一无 OOM 的区域 | 无变化 |
| **虚拟机栈** | 栈帧（局部变量表、操作数栈、动态链接、返回地址） | 私有 | `StackOverflowError` / `OutOfMemoryError` | 无变化 |
| **本地方法栈** | Native 方法的栈帧 | 私有 | `StackOverflowError` / `OutOfMemoryError` | 无变化 |
| **堆 (Heap)** | 对象实例、数组 | 共享 | `OutOfMemoryError` | **字符串常量池从 PermGen 移到堆** |
| **方法区** | 类信息、常量、静态变量、JIT 编译缓存 | 共享 | `OutOfMemoryError` | **PermGen 移除 → Metaspace（本地内存）** |

### 1.3 方法区的演进：PermGen → Metaspace

| 维度 | JDK 7 及以前 (PermGen) | JDK 8+ (Metaspace) |
|------|----------------------|---------------------|
| 存储位置 | JVM 堆内（`-XX:MaxPermSize`） | **本地内存**（堆外，`-XX:MaxMetaspaceSize`） |
| 默认上限 | ~82MB（32 位）/ 无明确上限（64 位） | **无上限**（受物理内存限制） |
| 类信息卸载 | 困难，容易 OOM | 默认即可卸载（GC 时回收） |
| 字符串常量池 | PermGen 内 | **移到堆内**（可被 GC 回收） |

> **为什么要移除 PermGen？** PermGen 固定大小难以调优；Metaspace 使用本地内存，自动扩容，减少 OOM 风险。

### 1.4 堆内存分代结构

```
                    ┌───────────────────────────────┐
                    │           堆 (Heap)            │
                    │                               │
    ┌───────────────┴───────────────┐               │
    │         新生代 (Young)         │               │
    │  ┌──────────┬───────┬──────┐ │               │
    │  │  Eden    │  S0   │  S1  │ │   老年代 (Old) │
    │  │  (80%)   │ (10%) │(10%) │ │               │
    │  └──────────┴───────┴──────┘ │               │
    │     Minor GC 时复制到 S 区    │  Major/Full GC │
    └──────────────────────────────┴───────────────┘
```

| 分代 | 比例 | 存放内容 | GC 类型 |
|------|------|---------|---------|
| **新生代 (Young)** | 1/3 堆 | 新创建的对象 | Minor GC（频繁） |
| ├ Eden | 8/10 Young | 刚 `new` 出来的对象 | — |
| ├ S0 (From) | 1/10 Young | 经历 1 次 GC 存活的对象 | — |
| └ S1 (To) | 1/10 Young | 与 S0 交替使用 | — |
| **老年代 (Old)** | 2/3 堆 | 长期存活对象（GC 年龄 ≥ 15） | Major GC / Full GC（少而慢） |

> **对象晋升老年代的条件**：
> 1. GC 年龄 ≥ `-XX:MaxTenuringThreshold`（默认 15）
> 2. **动态年龄判断**：同龄对象总大小 > Survivor 区 50%，直接晋升
> 3. **大对象**：超过 `-XX:PretenureSizeThreshold` 的对象直接进入老年代
> 4. **Survivor 空间不足**：Minor GC 时 Survivor 放不下，直接进老年代

---

## 二、垃圾回收（GC）

### 2.1 如何判断对象已死？

#### 2.1.1 引用计数法
- 给对象添加引用计数器，引用 +1，失效 -1，为 0 时回收
- **Java 不用**：无法解决循环引用（A ↔ B 互相引用，但都是垃圾）

#### 2.1.2 可达性分析（Java 采用）
- 从 **GC Roots** 出发，通过引用链搜索，不可达的对象判定为可回收
- **GC Roots 包括**：
  - 虚拟机栈（栈帧中的本地变量表）引用的对象
  - 方法区中**类静态属性**引用的对象
  - 方法区中**常量**引用的对象
  - 本地方法栈中 JNI 引用的对象
  - 所有被 `synchronized` 持有的对象
  - JVM 内部的引用（如系统类加载器）

```
GC Roots
   │
   ├──→ obj1 ──→ obj2 ──→ obj3       ← 存活
   │
   └──→ obj4                          ← 存活

   obj5 ──→ obj6                      ← 不可达，可回收
   (无 GC Roots 可达)
```

### 2.2 四种引用类型

| 引用类型 | 回收时机 | 典型用途 |
|---------|---------|---------|
| **强引用** `Object o = new Object()` | 永不回收（OOM 也不收） | 普通对象 |
| **软引用** `SoftReference` | 内存不足时回收 | 缓存（如图片缓存） |
| **弱引用** `WeakReference` | 下一次 GC 必定回收 | `WeakHashMap`、`ThreadLocal` 的 key |
| **虚引用** `PhantomReference` | 任何时候都可能，仅通知 | 堆外内存回收跟踪（`DirectByteBuffer`） |

### 2.3 GC 算法

#### 2.3.1 四大算法对比

| 算法 | 过程 | 优点 | 缺点 | 应用 |
|------|------|------|------|------|
| **标记-清除** | 标记垃圾 → 清除 | 简单 | **内存碎片** | CMS 老年代（备用） |
| **标记-复制** | 标记存活 → 复制到新空间 → 清空旧空间 | 无碎片、速度快 | 内存利用率低（一半空闲） | **新生代** Minor GC |
| **标记-整理** | 标记存活 → 移动到一端 → 清除边界外 | 无碎片 | 移动对象耗时（STW） | G1 老年代、Parallel Old |
| **分代收集** | 新生代复制 + 老年代标记整理/清除 | 结合优点 | 跨代引用维护 | **主流 JVM** |

#### 2.3.2 标记-复制算法在新生代的运作

```
Before Minor GC:
┌─────────┬────────┬────────┐
│  Eden   │  S0    │  S1    │
│ (满)    │ (有对象)│ (空)   │
└─────────┴────────┴────────┘

① 将 Eden + S0 中存活对象复制到 S1
② 清空 Eden + S0
③ S0 和 S1 角色互换

After Minor GC:
┌─────────┬────────┬────────┐
│  Eden   │  S0    │  S1    │
│ (空)    │ (空)   │ (有对象)│
└─────────┴────────┴────────┘
      S1 中的对象 GC 年龄 +1
```

#### 2.3.3 记忆集 (Remembered Set) 与卡表 (Card Table)

> **问题**：Minor GC 只回收新生代，但老年代对象可能引用新生代对象 → 需要额外扫描老年代找 GC Roots → 太慢！

**解决**：**卡表**将老年代划分为 512B 的 Card，如果一个 Card 中的对象引用了新生代对象，就标记为"脏卡"。Minor GC 时只需扫描"脏卡"而非整个老年代。

```
老年代：
┌──┬──┬──┬──┬──┬──┬──┬──┐
│  │  │  │  │  │  │  │  │  每个格子是一个 Card (512B)
└──┴──┴──┴──┴──┴──┴──┴──┘

卡表 (byte[]):
┌─┬─┬─┬─┬─┬─┬─┬─┐
│0│0│1│0│0│1│0│0│  1 = 脏卡（有跨代引用），需要扫描
└─┴─┴─┴─┴─┴─┴─┴─┘
```

### 2.4 三大垃圾收集器深度对比

#### 2.4.1 CMS (Concurrent Mark Sweep)

> **目标是**：最短回收停顿时间（低延迟）

```
CMS 工作流程（4 个阶段 + 1 个初始化）：

① 初始标记 (Initial Mark)         STW ⚡
   └─ 标记 GC Roots 直接关联的对象（快）
② 并发标记 (Concurrent Mark)      并发
   └─ GC Roots Tracing，遍历整个引用链（最耗时）
③ 重新标记 (Remark)               STW ⚡⚡
   └─ 修正并发标记期间变动的引用（比初始标记长但远小于并发标记）
④ 并发清除 (Concurrent Sweep)     并发
   └─ 清除未标记对象
⑤ 并发重置 (Concurrent Reset)     并发
   └─ 重置 GC 数据结构
```

| 维度 | CMS |
|------|-----|
| **算法** | 标记-清除 |
| **收集区域** | 老年代 |
| **优点** | 低停顿、并发收集 |
| **致命缺陷** | ① 内存碎片（标记-清除）② **浮动垃圾**（并发标记时新产生的垃圾）③ 对 CPU 敏感 |
| **触发时机** | `-XX:CMSInitiatingOccupancyFraction`（默认 92% 老年代占用） |
| **状态** | JDK 9 弃用，**JDK 14 移除** |

> **浮动垃圾**：并发标记期间，用户线程仍在运行，新产生的垃圾无法在本次 GC 中被清除，只能等下一次 GC。

#### 2.4.2 G1 (Garbage First)

> **目标是**：在延迟可控的前提下尽可能提高吞吐量

```
G1 堆布局（不再是连续的分代！）：

┌───┬───┬───┬───┬───┬───┬───┬───┐
│ E │ S │ E │ O │ E │ H │ O │ E │  每个格子是一个 Region (1-32MB)
├───┼───┼───┼───┼───┼───┼───┼───┤
│   │ O │ E │ S │ O │ E │ O │   │
└───┴───┴───┴───┴───┴───┴───┴───┘

E=Eden, S=Survivor, O=Old, H=Humongous（大对象区）
```

**G1 核心概念：**

| 概念 | 说明 |
|------|------|
| **Region** | 堆被划分为 ~2048 个大小相等的 Region（1~32MB），不要求连续 |
| **Humongous Region** | 存储 ≥ 0.5 Region 的大对象，多个连续 Region 存一个对象 |
| **RSet (Remembered Set)** | 每个 Region 都有一个 RSet，记录**其他 Region 引用本 Region 对象的指针** |
| **CSet (Collection Set)** | 本次 GC 要回收的 Region 集合 |

**G1 混合 GC (Mixed GC) 流程：**

```
① 初始标记 (Initial Mark)    STW ⚡
   └─ 标记 GC Roots 直接关联的对象，与 Minor GC 一起完成
② 并发标记 (Concurrent Mark) 并发
   └─ 遍历整个引用链
③ 最终标记 (Remark)         STW ⚡⚡
   └─ SATB（Snapshot-At-The-Beginning）处理并发标记期间的引用变化
④ 筛选回收 (Cleanup)        STW ⚡⚡⚡
   └─ 对各个 Region 的回收价值和成本排序，选"性价比最高"的 Region 回收
   └─ 采用标记-复制算法，把存活对象复制到空 Region，整体回收原 Region
```

#### 2.4.3 ZGC (Z Garbage Collector)

> **目标是**：极低延迟（< 1ms），TB 级堆

| 维度 | ZGC |
|------|-----|
| **JDK 版本** | JDK 11 实验性，JDK 15 正式 |
| **停顿时间** | **< 1ms**（与堆大小无关） |
| **堆大小** | 8MB ~ 16TB |
| **核心技术** | **染色指针**（Colored Pointers）+ **读屏障** |
| **算法** | 并发标记-整理（不依赖复制） |
| **适用场景** | 超低延迟（金融交易、实时系统）、大内存 |

> **染色指针**：ZGC 在 64 位指针中借用几个 bit 标记对象状态（Marked0/1、Remapped、Finalizable），不依赖堆外元数据，大幅提升效率。

#### 2.4.4 三大收集器对比总结

| 维度 | CMS | G1 | ZGC |
|------|-----|-----|-----|
| **目标停顿时长** | 低（数 ms~数十 ms） | 可控（默认 200ms） | **极低（< 1ms）** |
| **最大堆** | ~几十 GB | ~几十 GB | **16TB** |
| **内存碎片** | 有（标记-清除） | 无（标记-复制） | 无 |
| **并发方式** | 部分并发 | 部分并发 | **全并发** |
| **JDK 状态** | **JDK 14 移除** | JDK 9+ 默认 | JDK 15+ 正式 |
| **调优难度** | 高 | 中 | 低（几乎不用调） |

#### 2.4.5 GC 选择策略

```
场景                      推荐收集器
─────────────────────────────────
JDK 8 默认                 Parallel（吞吐量优先）
低延迟 + 大堆              G1（JDK 8 稳定，JDK 9+ 默认）
极低延迟 (<1ms)            ZGC（JDK 15+）
小内存 + 低延迟            Serial
已从 JDK 14 移除           CMS → 迁移到 G1
```

### 2.5 常见 GC 参数速查

| 参数 | 含义 |
|------|------|
| `-Xms` / `-Xmx` | 初始堆大小 / 最大堆大小 |
| `-Xmn` | 新生代大小 |
| `-XX:SurvivorRatio` | Eden/Survivor 比例（默认 8） |
| `-XX:MaxTenuringThreshold` | 晋升老年代年龄阈值（默认 15） |
| `-XX:+UseG1GC` | 启用 G1 |
| `-XX:+UseZGC` | 启用 ZGC |
| `-XX:MaxGCPauseMillis` | G1 目标停顿时间（默认 200ms） |
| `-XX:+PrintGCDetails` | 打印 GC 详情 |
| `-Xlog:gc*` (JDK 9+) | GC 日志 |

---

## 三、类加载机制

### 3.1 类加载的 7 个阶段

```
加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载
│      │      │      │      │
│      │      │      │      └─ 执行 <clinit>()，初始化静态变量
│      │      │      └─ 符号引用 → 直接引用
│      │      └─ 为静态变量分配内存并赋零值
│      └─ 验证 class 文件的正确性
└─ 读取 class 文件二进制流 → 方法区
```

| 阶段 | 做什么 | 关键点 |
|------|--------|--------|
| **加载** | ① 通过全限定名获取.class 二进制流 ② 转为方法区运行时数据结构 ③ 生成 `Class` 对象 | 由类加载器完成 |
| **验证** | 检查 class 文件合法性（格式、语义、字节码、符号引用） | 可关闭 `-Xverify:none` |
| **准备** | 为**静态变量**分配内存并设置**零值** | `static int a = 1;` → 准备阶段 `a = 0` |
| **解析** | 将常量池的**符号引用**替换为**直接引用** | 可能发生在初始化之后 |
| **初始化** | 执行 `<clinit>()`，为静态变量赋实际值 + 执行静态代码块 | `clinit` 是 JVM 自动生成的 |

> **注意**：准备阶段赋的是零值！例如 `static int value = 123`，准备阶段 `value = 0`，初始化阶段才赋 `value = 123`。
> 但 `static final` 常量（编译期常量）在准备阶段就已经赋值：`static final int value = 123` → 准备阶段即 `123`。

### 3.2 三种类加载器 + 双亲委派机制

#### 3.2.1 类加载器层级

```
┌──────────────────────────────────────┐
│         Bootstrap ClassLoader         │  加载 <JAVA_HOME>/lib (rt.jar)
│         (C++ 实现，JVM 的一部分)        │  JDK 9+ 加载核心模块
└──────────────┬───────────────────────┘
               │ 父加载器
               ▼
┌──────────────────────────────────────┐
│         Extension ClassLoader         │  加载 <JAVA_HOME>/lib/ext
│         (JDK 9+ 被 Platform 取代)      │  JDK 9+ 平台类加载器
└──────────────┬───────────────────────┘
               │ 父加载器
               ▼
┌──────────────────────────────────────┐
│         Application ClassLoader       │  加载 -classpath / -cp 下的类
│         (AppClassLoader)             │
└──────────────┬───────────────────────┘
               │
               ▼
┌──────────────────────────────────────┐
│         自定义 ClassLoader            │  隔离加载、热部署等
└──────────────────────────────────────┘
```

#### 3.2.2 双亲委派机制（面试核心）

```
加载类的请求：
     │
     ▼
Custom →  App  →  Platform  →  Bootstrap
                      │
         自底向上委派 (check if already loaded)
                      │
         自顶向下查找 (try to load)
                      │
                      ▼
              Bootstrap: /lib → Platform: /lib/ext → App: classpath
                                                       │
                                        都加载不到 → ClassNotFoundException
```

**源码级理解：**

```java
// ClassLoader.loadClass() 核心逻辑
protected Class<?> loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
        // ① 先检查是否已加载
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            try {
                // ② 委托父加载器（如果有）
                if (parent != null) {
                    c = parent.loadClass(name, false);
                } else {
                    // ③ 如果父加载器为 null，委托 Bootstrap
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // 父加载器加载失败
            }
            if (c == null) {
                // ④ 父加载器加载不到，自己加载
                c = findClass(name);
            }
        }
        return c;
    }
}
```

#### 3.2.3 为什么需要双亲委派？

1. **避免类重复加载**：加载过的类不会再被加载，减少内存开销
2. **保护核心 API 安全**：防止自定义一个 `java.lang.String` 类替换 JDK 的 String（自定义 String 会被已加载的 Bootstrap 阻塞）

#### 3.2.4 双亲委派的破坏场景

| 场景 | 破坏方式 | 代表框架 |
|------|---------|---------|
| **SPI 机制** | Base 类由 Bootstrap 加载，但接口实现由第三方提供，需要反向委托 | JDBC、JNDI |
| **热部署** | 相同全限定名的类需重复加载 | Tomcat、OSGi |
| **模块化** | 打破层级，模块间平等 | JDK 9 模块化 |

**SPI 的"逆向委托"**：用 **线程上下文类加载器** (Thread Context ClassLoader) 打破双亲委派。`ServiceLoader` 让 Bootstrap 加载的类可以"向下"请求 AppClassLoader 去加载实现类。

### 3.3 触发类初始化的 6 种情况

1. `new` 关键字创建实例
2. 访问某个**类或接口的静态变量**（不是常量 `final static`）
3. 调用类的**静态方法**
4. 通过**反射**（`Class.forName()`）
5. 初始化子类时，先初始化父类
6. JVM 启动时标记为启动类的类（`main` 方法所在类）

> **不会触发初始化**：① 访问 `final static` 编译期常量 ② 通过数组形式创建 `MyClass[] arr = new MyClass[5]` ③ 访问父类的静态变量（不会初始化子类）

---

## 四、面试高频题（带答案）

### Q1：请画出 JVM 内存结构图

```
线程私有：程序计数器、虚拟机栈、本地方法栈
线程共享：堆（新生代 Eden/S0/S1 + 老年代）、方法区（Metaspace）

JDK 8+ 变化：
- 方法区从 PermGen → Metaspace（本地内存）
- 字符串常量池移到堆中
```

（参考第一部分的内存结构图手绘即可）

### Q2：堆的分代结构？对象如何晋升到老年代？

```
堆分新生代和老年代：
- 新生代：Eden (80%) + S0 (10%) + S1 (10%)，存放新对象 → Minor GC
- 老年代：存放长生命周期对象 → Major/Full GC

对象晋升的 4 种路径：
1. 普通晋升：GC 年龄 ≥ MaxTenuringThreshold (默认 15)
2. 动态年龄：同龄对象 > Survivor 区 50%，直接进老年代
3. 大对象：超过 -XX:PretenureSizeThreshold 的值，直接进老年代
4. 空间担保失败：Survivor 放不下，直接进老年代
```

### Q3：CMS 和 G1 有什么区别？

| 维度 | CMS | G1 |
|------|-----|-----|
| 算法 | 标记-清除（老年代） | 标记-复制 + 标记-整理 |
| 内存布局 | 连续分代 | Region 化（~2048 个独立区块） |
| 碎片 | 有内存碎片 | 无碎片 |
| 停顿时间 | 不可控 | 可预测（`MaxGCPauseMillis`） |
| 大对象 | 直接进老年代 | Humongous Region |
| 适用场景 | 低延迟（已被 G1 取代） | 大堆 + 可预测延迟 |
| JDK 状态 | **JDK 14 移除** | **JDK 9+ 默认** |

### Q4：什么情况下会产生 Full GC？如何排查？

**触发 Full GC 的场景：**
1. `System.gc()` 显式调用
2. 老年代空间不足
3. 方法区（Metaspace）空间不足
4. 新生代空间担保失败（Minor GC 前判断老年代剩余空间 < 新生代所有对象之和）
5. CMS 并发模式失败（Concurrent Mode Failure），退化为 Serial Old Full GC

**排查思路：**
```
1. jstat -gc <pid> 1000 10  查看 GC 统计
2. jmap -heap <pid>         查看堆使用情况
3. jmap -histo:live <pid>   查看对象分布
4. 开启 GC 日志：-Xlog:gc* (JDK 9+) 或 -XX:+PrintGCDetails
5. 使用 MAT / JProfiler 分析 dump 文件
```

### Q5：类加载的 7 个阶段？双亲委派机制？

**7 个阶段**：加载 → 验证 → 准备 → 解析 → 初始化 → 使用 → 卸载

- **加载**：获取 .class 二进制流，转为方法区数据结构，生成 Class 对象
- **验证**：检查 class 文件合法性
- **准备**：为静态变量分配内存并赋零值
- **解析**：符号引用 → 直接引用
- **初始化**：`<clinit>()` 执行静态赋值和静态代码块

**双亲委派**：加载类时，先向上委派父加载器检查是否已加载，再自顶向下尝试加载。避免类重复和安全风险。

### Q6：哪些对象可以作为 GC Roots？

- 虚拟机栈引用的对象（局部变量、参数）
- 静态属性引用的对象
- 常量引用的对象（字符串常量池）
- 本地方法栈中 JNI 引用的对象
- `synchronized` 持有的对象
- JVM 内部引用（系统类加载器、基本类型的 Class 对象）

### Q7：强引用、软引用、弱引用、虚引用的区别？

| 类型 | GC 行为 | 用途 |
|------|--------|------|
| 强引用 | 宁可 OOM 也不回收 | 普通 `new` |
| 软引用 | 内存不足时回收 | 缓存 |
| 弱引用 | 下一次 GC 必回收 | `WeakHashMap`、`ThreadLocal` |
| 虚引用 | 仅通知，无法通过它获取对象 | 堆外内存回收追踪 |

### Q8：Minor GC / Major GC / Full GC 区别？

| GC 类型 | 区域 | 触发条件 | STW |
|---------|------|---------|-----|
| **Minor GC** | 新生代 | Eden 满 | 是（短暂） |
| **Major GC** | 老年代 | 老年代满 | 是（较长，通常伴随 Minor GC） |
| **Full GC** | 整个堆 + Metaspace | 老年代/Metaspace 满 / `System.gc()` | 是（最长） |

### Q9：JDK 9+ 类加载器有什么变化？

- **Extension ClassLoader** → **Platform ClassLoader**（加载 JDK 模块）
- Bootstrap 不再加载 `rt.jar`，改为加载模块化系统
- 类加载器不再是 URLClassLoader 子类

### Q10：OOM 如何排查？

```
1. 确认 JVM 参数：-Xms -Xmx -XX:MaxMetaspaceSize
2. 获取 dump：-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=xxx
3. 用 MAT/Eclipse Memory Analyzer 分析：
   - Dominator Tree：看谁占用了大量内存
   - Histogram：看哪个类的实例最多
   - Leak Suspects：自动检测疑似泄漏
4. 判断是内存泄漏还是内存不足：
   - 泄漏：某个对象数量持续增长
   - 不足：业务量确实大，需调大堆
```

---

## 五、实战练习

### 练习 1：JVM 内存分配分析

```java
// 分析以下代码的内存分配情况
public class MemoryAllocation {
    private static final int CONST = 100;           // ①
    private static String staticStr = "hello";      // ②
    private int instanceVar = 0;                    // ③
    
    public void method() {
        int localVar = 10;                          // ④
        Object obj = new Object();                  // ⑤
        String str = "world";                       // ⑥
        String str2 = new String("world");          // ⑦
    }
}
```

<details>
<summary>答案</summary>

```
① CONST：方法区（Metaspace）的常量池，因为是 static final 编译期常量
② staticStr：引用在 Metaspace，实际字符串对象在堆的字符串常量池（JDK 8+）
③ instanceVar：在堆中，属于每个对象实例
④ localVar：在虚拟机栈的局部变量表中
⑤ obj：引用在虚拟机栈，实际对象在堆的新生代 Eden 区
⑥ str：引用在虚拟机栈，实际对象在堆的字符串常量池（字面量）
⑦ str2：引用在虚拟机栈，实际对象在堆的新生代 Eden 区（new 出来的）
```
</details>

### 练习 2：判断对象是否存活

```
已知 GC Roots 为 {A, C}：

A → B1 → B2
C → D → B2
   E → F

问题：哪些对象存活？哪些可回收？
```

<details>
<summary>答案</summary>

```
存活对象：A, B1, B2, C, D
可回收对象：E, F（无 GC Roots 可达）

注意：B2 被 A 和 C 共同引用，只要有一条路径从 GC Roots 可达，就是存活的。
```
</details>

### 练习 3：类加载顺序分析

```java
class Parent {
    static int a = 1;
    static { System.out.println("Parent static block"); }
    { System.out.println("Parent instance block"); }
    Parent() { System.out.println("Parent constructor"); }
}

class Child extends Parent {
    static int b = 2;
    static { System.out.println("Child static block"); }
    { System.out.println("Child instance block"); }
    Child() { System.out.println("Child constructor"); }
}

public class Test {
    public static void main(String[] args) {
        System.out.println("--- 1 ---");
        Child.b = 3;
        System.out.println("--- 2 ---");
        new Child();
    }
}
```

<details>
<summary>答案</summary>

```
--- 1 ---
Parent static block
Child static block
--- 2 ---
Parent instance block
Parent constructor
Child instance block
Child constructor

解析：
1. Child.b = 3 触发 Child 的初始化（访问静态变量）
   但初始化 Child 前必须先初始化 Parent → 打印 "Parent static block"
   然后执行 Child 的静态代码块 → 打印 "Child static block"
2. new Child() 触发实例化（此时类已加载，不会再执行 static）
   先执行父类实例化：instance block → constructor
   再执行子类实例化：instance block → constructor
```
</details>

### 练习 4：CMS vs G1 参数配置

```bash
# 问题：以下是 CMS 还是 G1 的配置？各项含义是什么？
-XX:+UseConcMarkSweepGC
-XX:CMSInitiatingOccupancyFraction=75
-XX:+UseCMSCompactAtFullCollection
-XX:CMSFullGCsBeforeCompaction=5
```

<details>
<summary>答案</summary>

```
这是 CMS 配置：

-XX:+UseConcMarkSweepGC          启用 CMS 作为老年代收集器
-XX:CMSInitiatingOccupancyFraction=75
          老年代占用达到 75% 时触发 CMS GC
          （默认 92%，设低一点避免 Concurrent Mode Failure）
-XX:+UseCMSCompactAtFullCollection
          Full GC 后执行内存整理（解决碎片问题）
-XX:CMSFullGCsBeforeCompaction=5
          每 5 次 Full GC 进行一次内存整理
          （0 表示每次 Full GC 都整理）

生产建议：迁移到 G1（-XX:+UseG1GC）
```
</details>

### 练习 5：OOM 场景模拟与解决

```java
// 场景：以下代码会 OOM 吗？是哪种 OOM？
// 场景 1
List<byte[]> list = new ArrayList<>();
while (true) {
    list.add(new byte[1024 * 1024]); // 1MB
}

// 场景 2
void recursive(int n) {
    recursive(n + 1);
}

// 场景 3
// JVM 参数：-XX:MaxMetaspaceSize=10m
// 使用 CGLIB 动态生成大量类
```

<details>
<summary>答案</summary>

```
场景 1 → java.lang.OutOfMemoryError: Java heap space
  堆空间不足。解决方案：增大 -Xmx 或排查对象创建是否合理。

场景 2 → java.lang.StackOverflowError
  栈溢出（方法递归太深，栈帧撑爆虚拟机栈）。
  解决方案：优化递归逻辑或用循环替代；增大 -Xss（一般不建议）。

场景 3 → java.lang.OutOfMemoryError: Metaspace
  Metaspace（方法区）空间不足，通常是大量动态生成类。
  解决方案：增大 -XX:MaxMetaspaceSize；优化类的生成。
```
</details>

---

## 六、易错点/坑

| # | 易错点 | 说明 |
|---|--------|------|
| 1 | 混淆 PermGen 和 Metaspace | JDK 8+ 方法区 = Metaspace（本地内存），不再是 PermGen（堆内） |
| 2 | 以为只要 `final` 就是编译期常量 | 必须是 `static final` 且值在编译期确定（如 `= 123`、`= "abc"`）；`= new Object()` 不是编译期常量 |
| 3 | 误解 Minor GC 时间 | Minor GC 很快但仍有 STW，不是无停顿 |
| 4 | CMS 碎片导致 Concurrent Mode Failure | CMS 标记-清除产生碎片，碎片严重时老年代无法分配大对象 → Full GC |
| 5 | 认为 CMS 完全无停顿 | CMS 初始标记和重新标记有 STW，只是停顿比 Parallel 短 |
| 6 | `GC Roots` 范围记不全 | 忘记本地方法栈引用和 `synchronized` 持有的对象 |
| 7 | 字符串常量池位置变化 | JDK 7 从 PermGen 移到堆，JDK 8 方法区移到 Metaspace 但字符串常量池仍在堆 |
| 8 | `jmap -dump` 导致 STW | 生产环境大堆 dump 会触发长时间停顿，建议用 `-XX:+HeapDumpOnOutOfMemoryError` 自动 dump |

---

## 七、JVM 参数速查表

### 堆参数

| 参数 | 含义 | 示例 |
|------|------|------|
| `-Xms` | 初始堆大小 | `-Xms2g` |
| `-Xmx` | 最大堆大小 | `-Xmx4g` |
| `-Xmn` | 新生代大小 | `-Xmn1g` |
| `-Xss` | 线程栈大小 | `-Xss256k` |
| `-XX:SurvivorRatio` | Eden/S0 比例 | `-XX:SurvivorRatio=8` |
| `-XX:MaxTenuringThreshold` | 晋升老年代 GC 年龄 | `-XX:MaxTenuringThreshold=15` |

### GC 参数

| 参数 | 含义 | 收集器 |
|------|------|--------|
| `-XX:+UseG1GC` | 启用 G1 | G1 |
| `-XX:+UseZGC` | 启用 ZGC | ZGC |
| `-XX:MaxGCPauseMillis` | G1 目标停顿时间 | G1 |
| `-XX:G1HeapRegionSize` | G1 Region 大小 | G1 |
| `-XX:ConcGCThreads` | 并发 GC 线程数 | G1/CMS |
| `-XX:ParallelGCThreads` | 并行 GC 线程数 | 通用 |

### 内存溢出排查参数

| 参数 | 含义 |
|------|------|
| `-XX:+HeapDumpOnOutOfMemoryError` | OOM 时自动生成 dump |
| `-XX:HeapDumpPath=/path/to/dump` | dump 文件路径 |
| `-XX:MaxMetaspaceSize` | 最大 Metaspace 大小 |
| `-XX:+PrintGCDetails` | 打印 GC 详情 |
| `-Xlog:gc*` (JDK 9+) | GC 日志（统一的日志系统） |

### 生产环境推荐基线

```bash
# JDK 8 + G1
-Xms4g -Xmx4g \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/logs/heapdump.hprof \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-Xloggc:/app/logs/gc-%t.log

# JDK 17+ ZGC
-Xms8g -Xmx8g \
-XX:+UseZGC \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/app/logs/heapdump.hprof \
-Xlog:gc*:file=/app/logs/gc-%t.log:time,level,tags
```

---

## 八、今日自测 Checklist

- [ ] 能画出 JVM 内存结构图（5 大区域 + 线程私有/共享区分）
- [ ] 能说出 JDK 8 PermGen → Metaspace 的变化及原因
- [ ] 能画出堆的分代结构（Eden/S0/S1/Old 比例）
- [ ] 能说出对象晋升老年代的 4 种路径
- [ ] 能解释可达性分析和 GC Roots 的类别
- [ ] 能对比 4 种 GC 算法（标记清除/复制/整理/分代）
- [ ] 能画出 CMS 的 4 阶段流程（含哪些阶段 STW）
- [ ] 能解释 G1 的 Region 布局和 Mixed GC 流程
- [ ] 能对比 CMS vs G1 vs ZGC（停顿时间、碎片、堆大小、算法）
- [ ] 能说出类加载的 7 个阶段及每阶段做什么
- [ ] 能画图解释双亲委派机制
- [ ] 能说出破坏双亲委派的 3 种场景
- [ ] 能区分 `final static` 常量 vs `static` 变量在准备阶段的赋值差异
- [ ] 能说出 Minor GC / Major GC / Full GC 的区别
- [ ] 能说出 OOM 的排查思路和常用工具

---

> **Day 3 完成！** 明天 Day 4：Spring IoC/AOP（Bean 生命周期、代理、事务）