# Day 7：Week 1 周总结 + 模拟面试

> 计划日期：Week 1 Day 7 | 主题：Java 核心 + Spring 体系周回顾、模拟面试
> 输出要求：**完成 3 道算法题** + **回答 5 道基础题** + 能串联 Week 1 知识体系

---

## 一、Week 1 知识全景回顾

### 1.1 七天学习地图

```
Week 1：Java 核心 + Spring 原理
┌────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│ Day 1  │ Day 2  │ Day 3  │ Day 4  │ Day 5  │ Day 6  │ Day 7  │
│ 集合   │ 并发   │ JVM    │ IoC/AOP│ Boot   │ Cloud  │ 模拟面 │
├────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│HashMap │线程池  │内存模型│Bean周期│自动配置│ Feign  │算法+基础│
│CHM     │锁/vol  │GC/类加载│三级缓存│Starter │Gateway │题模拟  │
│ArrayList│synchro │OOM排查 │事务失效│ SPI    │Sentinel│        │
└────────┴────────┴────────┴────────┴────────┴────────┴────────┘
```

### 1.2 每日核心输出自检

| 天 | 核心输出 | 自评（1-5） |
|----|---------|------------|
| Day 1 | 手写 HashMap put 伪代码 | ☐ |
| Day 2 | 画出线程池工作流程 | ☐ |
| Day 3 | 画出 JVM 内存结构图 | ☐ |
| Day 4 | 口述 Bean 生命周期 12 步 | ☐ |
| Day 5 | 手写一个 Starter | ☐ |
| Day 6 | 画出服务调用链路 | ☐ |
| Day 7 | 完成模拟面试 | ☐ |

### 1.3 知识串联：一个请求的全生命周期

```
HTTP 请求进入
    │
    ▼
[Day 6] Gateway 路由 + Sentinel 限流 + JWT 鉴权
    │
    ▼
[Day 6] Feign 调用下游（LoadBalancer 选实例）
    │
    ▼
[Day 5] Spring Boot 自动配置创建 Bean、读取 application.yml
    │
    ▼
[Day 4] Controller → Service（AOP 代理）→ @Transactional 事务
    │                    │
    │                    ▼
    │              [Day 4] Bean 生命周期 + 三级缓存
    │
    ▼
[Day 2] 业务逻辑可能用到线程池异步处理
    │
    ▼
[Day 1] 查询结果放入 HashMap 缓存 / ConcurrentHashMap 并发读写
    │
    ▼
[Day 3] 对象在 JVM 堆中分配，GC 回收不再使用的对象
    │
    ▼
返回 JSON 响应
```

---

## 二、Week 1 高频考点速记卡

### 2.1 Java 核心（Day 1-3）

| 考点 | 一句话答案 |
|------|-----------|
| HashMap 1.8 结构 | 数组 + 链表 + 红黑树，尾插，扩容 2 倍 |
| CHM 1.8 线程安全 | CAS 设空桶 + synchronized 锁头节点 |
| 线程池 7 参数 | core/max/keepAlive/unit/workQueue/threadFactory/handler |
| synchronized 锁升级 | 无锁 → 偏向 → 轻量 → 重量 |
| volatile 作用 | 可见性 + 禁止指令重排，不保证原子性 |
| JVM 内存分区 | 堆、栈、方法区、程序计数器、本地方法栈 |
| CMS vs G1 | CMS 标记清除有碎片；G1 分区回收可预测停顿 |
| 类加载双亲委派 | 子加载器先委派父加载器，防止核心类被篡改 |

### 2.2 Spring 体系（Day 4-6）

| 考点 | 一句话答案 |
|------|-----------|
| Bean 生命周期 | 实例化→属性注入→Aware→BPP前→初始化→BPP后→销毁 |
| 三级缓存 | singletonObjects / earlySingletonObjects / singletonFactories |
| AOP 代理时机 | postProcessAfterInitialization，CGLIB 为主 |
| 事务失效 | 自调用、非 public、异常类型、多数据源等 |
| 自动配置 | EnableAutoConfiguration → SpringFactoriesLoader → Conditional |
| Starter | 依赖聚合 + 自动配置 + 默认配置 |
| Feign 原理 | 动态代理 → 构建请求 → LoadBalancer → HTTP |
| Sentinel 流控 | 直接 / 关联 / 链路；快速失败 / 预热 / 排队 |

---

## 三、模拟面试 · 基础题（5 道）

> 建议先自己作答，再对照参考答案。每题控制在 3 分钟内口述。

---

### 基础题 1：HashMap 和 ConcurrentHashMap 的区别？CHM 如何保证线程安全？

<details>
<summary>点击查看参考答案</summary>

**HashMap：**
- 非线程安全，多线程 put 可能导致数据丢失、死循环（1.7）或数据不一致
- JDK 1.8：数组 + 链表 + 红黑树，负载因子 0.75，容量 2 的幂

**ConcurrentHashMap：**
- JDK 1.7：Segment 分段锁，并发度 = Segment 数
- JDK 1.8：抛弃 Segment，CAS 插入空桶 + synchronized 锁链表/红黑树头节点
- `size()` 用 `CounterCell` 分散计数
- 不允许 null key/value（二义性问题）

**追问准备：**
- 为什么 1.8 放弃 Segment？→ 锁粒度更细，并发度更高
- get 需要加锁吗？→ 不需要，用 volatile 保证可见性

</details>

---

### 基础题 2：线程池的核心参数和工作流程？拒绝策略有哪些？

<details>
<summary>点击查看参考答案</summary>

**7 个参数：**
1. `corePoolSize` — 核心线程数
2. `maximumPoolSize` — 最大线程数
3. `keepAliveTime` — 非核心线程空闲存活时间
4. `unit` — 时间单位
5. `workQueue` — 任务队列（ArrayBlockingQueue / LinkedBlockingQueue / SynchronousQueue）
6. `threadFactory` — 线程工厂
7. `handler` — 拒绝策略

**工作流程：**
```
提交任务 → 核心线程未满？创建核心线程执行
         → 核心满 → 入队列 → 队列满 → 创建非核心线程
         → 达到最大线程数 → 执行拒绝策略
```

**4 种拒绝策略：**
| 策略 | 行为 |
|------|------|
| AbortPolicy（默认） | 抛 RejectedExecutionException |
| CallerRunsPolicy | 调用者线程执行 |
| DiscardPolicy | 静默丢弃 |
| DiscardOldestPolicy | 丢弃队列最老任务 |

**最佳实践：** 不用 `Executors` 工厂（可能 OOM），用 `ThreadPoolExecutor` 显式配置；队列有界；合理命名线程。

</details>

---

### 基础题 3：Spring 如何解决循环依赖？为什么需要三级缓存？

<details>
<summary>点击查看参考答案</summary>

**前提：** 仅 **单例 Bean + 字段/Setter 注入** 可解决，构造器注入和 prototype 不行。

**三级缓存：**
| 缓存 | 名称 | 存储 |
|------|------|------|
| 一级 | `singletonObjects` | 完整 Bean |
| 二级 | `earlySingletonObjects` | 早期暴露的 Bean（可能是代理） |
| 三级 | `singletonFactories` | ObjectFactory，用于生成早期引用 |

**流程：**
1. 创建 A → 实例化后放三级缓存（ObjectFactory）
2. 填充 A 的属性，发现依赖 B → 去创建 B
3. 创建 B → 实例化后放三级缓存 → 填充属性依赖 A
4. 从三级缓存获取 A 的早期引用（如需 AOP 则此时生成代理）
5. B 完成初始化 → A 完成初始化

**为什么三级？** 二级缓存不够：当 Bean 需要 AOP 代理时，不能只暴露原始对象，三级缓存的 ObjectFactory 可以**延迟决定**暴露原始对象还是代理对象。

</details>

---

### 基础题 4：Spring Boot 自动配置原理？

<details>
<summary>点击查看参考答案</summary>

1. `@SpringBootApplication` 包含 `@EnableAutoConfiguration`
2. 导入 `AutoConfigurationImportSelector`（实现 `DeferredImportSelector`）
3. 通过 `SpringFactoriesLoader` 加载所有 jar 中 `META-INF/spring.factories`（Boot 3 为 `AutoConfiguration.imports`）里注册的自动配置类
4. 经 `@ConditionalOnClass`、`@ConditionalOnMissingBean` 等条件过滤
5. 满足条件的配置类注册 Bean 到容器

**用户可覆盖：** 自定义同类型 Bean（`@ConditionalOnMissingBean` 跳过自动配置）或 `@SpringBootApplication(exclude=...)` 排除。

**举例：** classpath 有 `RedisTemplate` 类 + 用户未自定义 → `RedisAutoConfiguration` 读取 `spring.data.redis.*` 自动创建 `RedisTemplate`。

</details>

---

### 基础题 5：描述一次 Feign 远程调用的完整过程

<details>
<summary>点击查看参考答案</summary>

1. 业务代码调用 Feign 接口方法
2. Feign 动态代理拦截调用，解析 `@GetMapping` 等注解
3. 构建 `RequestTemplate`（URL、Method、Header、Body）
4. `RequestInterceptor` 链处理（如传递 Token、TraceId）
5. `Encoder` 将参数编码（如 JSON 序列化）
6. `LoadBalancer` 从 Nacos/Eureka 获取服务实例列表，选择目标地址
7. HTTP 客户端（默认 HttpURLConnection，推荐 OkHttp）发送请求
8. 目标服务处理并返回响应
9. `Decoder` 解码响应体为目标 Java 对象
10. 若配置了 Sentinel / fallback，异常时走降级逻辑

**追问准备：**
- 负载均衡用什么？→ Spring Cloud LoadBalancer（替代 Ribbon）
- 同服务多 Feign 接口冲突？→ 加 `contextId`

</details>

---

## 四、模拟面试 · 算法题（3 道）

> Week 1 算法主题：**数组、链表、栈**（对应学习计划 Week 1 算法模块）
> 难度：中等 | 建议每题 20-25 分钟

---

### 算法题 1：三数之和（LeetCode 15）

**题目：** 给定整数数组 `nums`，找出所有和为 0 且不重复的三元组。

```
输入：nums = [-1, 0, 1, 2, -1, -4]
输出：[[-1, -1, 2], [-1, 0, 1]]
```

**思路：**
1. 排序数组
2. 固定第一个数 `nums[i]`，在 `[i+1, n)` 范围用双指针找两数之和为 `-nums[i]`
3. 去重：跳过相同的 `nums[i]`、`left`、`right`

```java
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    Arrays.sort(nums);
    int n = nums.length;

    for (int i = 0; i < n - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;  // 去重

        int left = i + 1, right = n - 1;
        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];
            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                while (left < right && nums[left] == nums[left + 1]) left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++;
                right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
```

| 复杂度 | 值 |
|--------|-----|
| 时间 | O(n²) |
| 空间 | O(1) 不计结果集 |

**面试要点：**
- 为什么排序？→ 双指针前提 + 去重
- 双指针移动条件？→ sum 与 0 比较决定移动 left 或 right

---

### 算法题 2：环形链表 II（LeetCode 142）

**题目：** 给定链表头节点 `head`，返回链表开始入环的第一个节点；无环返回 `null`。

```
输入：head = [3,2,0,-4], pos = 1（尾节点连接到索引 1）
输出：索引 1 的节点
```

**思路：Floyd 判圈算法**
1. **快慢指针**相遇说明有环
2. 一个指针从头出发，另一个从相遇点出发，同步前进，再次相遇即为环入口

```java
public ListNode detectCycle(ListNode head) {
    ListNode slow = head, fast = head;

    // 阶段一：判断是否有环
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) break;
    }
    if (fast == null || fast.next == null) return null;

    // 阶段二：找环入口
    slow = head;
    while (slow != fast) {
        slow = slow.next;
        fast = fast.next;
    }
    return slow;
}
```

**数学证明（面试加分）：**
- 设头到环入口距离 a，环入口到相遇点距离 b，相遇点回到环入口距离 c
- 慢指针走了 a + b，快指针走了 a + b + n(b + c)
- 2(a + b) = a + b + n(b + c) → a = (n-1)(b+c) + c
- 即从头走 a 步 = 从相遇点走 a 步 = 环入口

| 复杂度 | 值 |
|--------|-----|
| 时间 | O(n) |
| 空间 | O(1) |

---

### 算法题 3：每日温度（LeetCode 739）

**题目：** 给定每日温度数组 `temperatures`，返回数组 `answer`，其中 `answer[i]` 是第 i 天后要等多少天才能遇到更高温度；没有则 0。

```
输入：temperatures = [73, 74, 75, 71, 69, 72, 76, 73]
输出：[1, 1, 4, 2, 1, 1, 0, 0]
```

**思路：单调栈（递减栈）**
- 栈存**下标**，栈内对应温度单调递减
- 当前温度 > 栈顶温度时，弹出栈顶，计算天数差

```java
public int[] dailyTemperatures(int[] temperatures) {
    int n = temperatures.length;
    int[] answer = new int[n];
    Deque<Integer> stack = new ArrayDeque<>();  // 存下标

    for (int i = 0; i < n; i++) {
        while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
            int prevIndex = stack.pop();
            answer[prevIndex] = i - prevIndex;
        }
        stack.push(i);
    }
    return answer;
}
```

| 复杂度 | 值 |
|--------|-----|
| 时间 | O(n) 每个元素最多入栈出栈一次 |
| 空间 | O(n) |

**面试要点：**
- 单调栈适用场景：「找左边/右边第一个更大/更小元素」
- 为什么存下标而非温度？→ 需要计算距离

---

## 五、综合模拟面试（45 分钟流程）

> 可按以下流程自我模拟，或请同事 / AI 扮演面试官追问。

### 5.1 流程安排

| 阶段 | 时间 | 内容 |
|------|------|------|
| 自我介绍 | 2 min | 6 年经验、技术栈、求职意向 |
| 技术基础 | 15 min | 本文第三、四节题目 + 追问 |
| 项目深挖 | 10 min | 挑一个微服务项目讲架构 |
| 算法 | 15 min | 三数之和 或 每日温度 |
| 反问 | 3 min | 团队技术栈、业务方向 |

### 5.2 项目深挖示范问题

1. 你们的微服务是怎么做服务拆分的？注册中心用的什么？
2. Gateway 层做了哪些事情？限流阈值怎么定的？
3. 有没有遇到过 Feign 调用超时的问题？怎么排查和解决的？
4. 分布式事务怎么处理的？（可衔接 Week 2 Seata）
5. 线上出现过 OOM 吗？怎么排查的？（衔接 Day 3 JVM）

### 5.3 模拟评分表

| 维度 | 优秀（9-10） | 良好（7-8） | 需加强（<7） |
|------|------------|------------|------------|
| 基础概念 | 准确 + 能讲源码/原理 | 准确但缺乏深度 | 概念模糊 |
| 表达逻辑 | 结构化、有层次 | 基本清楚 | 跳跃、遗漏关键点 |
| 算法 | 15 min 内 bug-free | 思路对有小 bug | 无思路 |
| 项目经验 | 有量化指标 + 技术决策 | 能描述但缺亮点 | 讲不清自己的贡献 |

---

## 六、Week 1 薄弱点诊断

完成模拟面试后，对照以下清单标记薄弱项：

### 6.1 知识薄弱点

| 模块 | 常见薄弱点 | 补救 |
|------|-----------|------|
| HashMap | 扩容迁移、树化条件 | 重读 Day 1，手写 put 伪代码 |
| 线程池 | 队列选型、参数设置 | 重读 Day 2，画工作流程图 |
| JVM | GC 算法区别、OOM 排查 | 重读 Day 3，做一次 jmap 实操 |
| Bean 生命周期 | 步骤遗漏、BPP 时机 | 重读 Day 4，背 12 步表 |
| 自动配置 | Conditional 注解混淆 | 重读 Day 5，手写 Starter |
| Spring Cloud | 调用链路说不清 | 重读 Day 6，画三遍链路图 |

### 6.2 算法薄弱点

| 题型 | 掌握标志 | 未掌握则 |
|------|---------|---------|
| 双指针 | 能独立做三数之和 | 刷 LC 15、18、11 |
| 链表 | 能独立做环形链表 | 刷 LC 141、142、19 |
| 单调栈 | 能识别「下一个更大元素」 | 刷 LC 739、496、84 |

---

## 七、错题本模板

```markdown
## 错题记录

### 题目：[题目名] LC [编号]
- 日期：2026-07-03
- 错误原因：[思路错 / 边界遗漏 / 复杂度不够]
- 正确思路：[...]
- 相似题：[LC xxx]

### 面试题：[题目]
- 我的回答：[...]
- 不足：[...]
- 标准答案要点：[...]
```

---

## 八、Week 2 预习指引

| 天 | 主题 | 预习建议 |
|----|------|---------|
| Day 8 | MySQL 索引与 SQL 调优 | 回顾现有 SQL 慢查询案例 |
| Day 9 | Redis 缓存与集群 | 阅读 `dataBase/Redis.md` |
| Day 10 | RabbitMQ | 阅读 `middleware/RabbitMQ.md` |
| Day 11 | Kafka | 阅读 `middleware/Kafka.md` |
| Day 12 | Elasticsearch | 准备倒排索引概念 |
| Day 13 | 分库分表 | 阅读 `dataBase/Sharding-JDBC.md` |
| Day 14 | 周总结 | 数据库场景题 + 算法 |

---

## 九、今日自测 Checklist

- [ ] 完成 **5 道** 基础题自答并对照参考答案
- [ ] 完成 **3 道** 算法题（至少独立写出核心代码）
- [ ] 能串联描述「一个 HTTP 请求从 Gateway 到 DB 再返回」的完整链路
- [ ] 填写 Week 1 每日核心输出自评表
- [ ] 标记薄弱点并记录到错题本
- [ ] 能口述 Week 1 速记卡中 **16 个** 考点
- [ ] （可选）完成 45 分钟完整模拟面试流程

---

## 十、Week 1 总结

```
✅ Day 1  Java 集合框架    — HashMap / CHM / ArrayList
✅ Day 2  并发编程          — 线程池 / 锁 / volatile
✅ Day 3  JVM              — 内存模型 / GC / 类加载
✅ Day 4  Spring IoC/AOP   — Bean 生命周期 / 三级缓存 / 事务
✅ Day 5  Spring Boot      — 自动配置 / Starter / SPI
✅ Day 6  Spring Cloud     — Feign / Gateway / Sentinel
✅ Day 7  周总结 + 模拟面试 — 算法 3 题 + 基础 5 题
```

**Week 1 核心能力目标：**
1. Java 基础扎实，能讲清集合、并发、JVM 原理
2. Spring 全家桶原理清晰，从 Bean 创建到微服务调用链路贯通
3. 算法入门：掌握双指针、链表判环、单调栈三个套路

---

> **Week 1 完成！** 明天进入 Week 2：数据库 + 中间件（Day 8：MySQL 索引与 SQL 调优）
