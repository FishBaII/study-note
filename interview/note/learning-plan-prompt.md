# Java 后端开发知识库 · 学习复习计划 Prompt

> 基于 6 年 Java 开发经验的系统性知识库构建与面试复习计划

---

## 使用说明

这是一个**知识库 Prompt 模板**，你可以：

1. **整体使用**：将本文档作为 AI 对话的 System Prompt，让 AI 成为你的私人学习助手
2. **按模块使用**：复制某个模块的 Prompt 片段，针对该模块深度学习
3. **定期复习**：按照复习计划时间表，定期让 AI 出题或讲解

```
System Prompt 用法：
将本文档内容复制到 AI 对话开头，然后说："请按照这个学习计划，今天帮我复习 [模块名]"
```

---

## 一、知识库全景图

> 以下是你当前技术栈的全貌，标注了掌握程度和优先级

| 模块 | 子项 | 掌握程度 | 面试权重 | 复习优先级 |
|------|------|---------|---------|-----------|
| Java 核心 | 集合框架、并发编程、JVM | ⭐⭐⭐ | 高 | P0 |
| Java 核心 | Stream/Lambda、泛型、反射 | ⭐⭐⭐⭐ | 中 | P1 |
| Spring 体系 | Spring Boot、Spring Cloud | ⭐⭐⭐⭐ | 高 | P0 |
| Spring 体系 | IoC/AOP 原理、Spring Security | ⭐⭐⭐ | 高 | P0 |
| 数据库 | MySQL 调优、索引原理 | ⭐⭐⭐ | 高 | P0 |
| 数据库 | Redis 缓存/集群、分库分表 | ⭐⭐⭐⭐ | 高 | P0 |
| 中间件 | RabbitMQ、Kafka、Elasticsearch | ⭐⭐⭐ | 高 | P1 |
| 分布式 | Seata、Canal、DynamicDatasource | ⭐⭐⭐⭐ | 中 | P1 |
| DevOps | Jenkins、Docker、CI/CD | ⭐⭐⭐⭐ | 中 | P2 |
| 系统设计 | 架构设计、数据库设计 | ⭐⭐⭐ | 高 | P0 |
| 算法 | LeetCode 高频题 | ⭐⭐ | 高 | P0 |

---

## 二、分模块学习 Prompt

### 模块 1：Java 核心基础

```
【Prompt 片段 - Java 核心】

你是一位 Java 技术专家。请帮我复习以下 Java 核心知识点，我的经验是 6 年 Java 后端开发。

请按以下结构输出：
1. 核心概念整理（每个概念 3 句话以内）
2. 常见面试题（带答案）
3. 易错点/坑
4. 一道代码实战题

需要覆盖的知识点：
- Java 集合框架：HashMap 原理（1.7 vs 1.8）、ConcurrentHashMap、ArrayList vs LinkedList
- 并发编程：synchronized vs Lock、线程池（ThreadPoolExecutor 参数与拒绝策略）、volatile、CAS
- JVM：内存模型（堆、栈、方法区）、垃圾回收（CMS、G1、ZGC）、类加载机制
- Java 9-11 新特性：模块化、var、HttpClient、集合工厂方法

请先从「集合框架」开始，给我 3 道 HashMap 相关的面试题。
```

### 模块 2：Spring 体系

```
【Prompt 片段 - Spring 体系】

你是一位 Spring 技术专家。我是一名 6 年经验的 Java 开发者，平时使用 Spring Boot + Spring Cloud 进行微服务开发。

请帮我深度复习 Spring 体系，按以下结构输出：
1. 原理深度解析（附源码级说明）
2. 面试高频问题 Top 10（附参考答案）
3. 实际项目中的最佳实践
4. 一道场景设计题

需要覆盖：
- IoC 容器：Bean 生命周期、循环依赖解决（三级缓存）、@Autowired vs @Resource
- AOP：JDK 动态代理 vs CGLIB、切面优先级、事务失效场景
- Spring Boot：自动配置原理（@SpringBootApplication）、Starter 机制、SPI 机制
- Spring Cloud：Feign 原理、GateWay 路由与过滤器、Sentinel 流控降级
- Spring Security + JWT：认证授权流程、密码加密、Token 刷新机制

请先从「IoC 容器和 Bean 生命周期」开始深度讲解。
```

### 模块 3：数据库与持久层

```
【Prompt 片段 - 数据库】

你是一位数据库专家。请帮我系统性复习数据库知识，我的经验包括 MySQL、Oracle、Redis。

请按以下结构输出：
1. 核心原理
2. SQL 优化实战案例
3. 面试高频问题（附答案）
4. 动手练习题

需要覆盖：
- MySQL：索引原理（B+Tree、聚簇索引 vs 非聚簇索引）、SQL 执行计划（EXPLAIN）、乐观锁 vs 悲观锁
- 分库分表：ShardingSphere-JDBC 使用、分片策略、分布式 ID
- 动态数据源：DynamicDatasource 原理、多数据源事务管理
- Redis：数据类型应用场景、缓存穿透/击穿/雪崩、集群部署（Cluster/Sentinel）、持久化（RDB/AOF）
- MyBatis：动态 SQL、插件机制、缓存（一级/二级）、MyBatis-Plus 增强
- JPA：实体关系映射、JPQL、审计功能
- Canal：MySQL binlog 增量同步、与 Elasticsearch 联动

请先从「MySQL 索引原理与 SQL 调优」开始，给我 5 个真实场景的 SQL 优化案例。
```

### 模块 4：分布式系统

```
【Prompt 片段 - 分布式系统】

你是一位分布式系统专家。请帮我复习分布式相关技术，我在实际项目中使用过 Seata、Sentinel、RabbitMQ、Kafka。

请按以下结构输出：
1. 原理讲解
2. 面试高频问题
3. 实际项目踩坑经验
4. 架构设计题

需要覆盖：
- 分布式事务：Seata AT/TCC 模式、CAP 理论、BASE 理论
- 消息队列：RabbitMQ（死信队列、延迟队列、消息可靠性）、Kafka（分区机制、消费者组、消息不丢失）
- 服务治理：Sentinel（流控/熔断/降级）、GateWay（限流/路由/跨域）
- 分布式锁：Redis 实现 vs ZooKeeper 实现
- 分布式 ID：雪花算法、美团 Leaf

请先从「分布式事务 - Seata AT 模式」开始讲解，并给我一道场景题。
```

### 模块 5：系统设计与架构

```
【Prompt 片段 - 系统设计】

你是一位系统架构专家。请帮我训练系统设计能力，我有 6 年 Java 后端经验，目前求职目标是中大厂。

请按以下结构训练：
1. 设计方法论（4 步法）
2. 常见系统设计题（附答案框架）
3. 我的项目经验提炼
4. Mock 面试模拟

需要覆盖的设计题：
- 设计一个短链接系统（类似 TinyURL）
- 设计一个秒杀系统
- 设计一个实时排行榜
- 设计一个消息推送系统
- 设计一个 API 网关
- 设计一个分布式事务协调器

请从「秒杀系统设计」开始，先给我需求分析框架，然后我们一步步完善设计。
```

### 模块 6：DevOps 与工具链

```
【Prompt 片段 - DevOps】

你是一位 DevOps 专家。请帮我整理以下工具的常用命令和最佳实践：

需要覆盖：
- Docker：Dockerfile 编写、docker-compose、镜像优化
- Jenkins：Pipeline 脚本、多环境部署、自动化测试集成
- Linux：常用排查命令（top、jstack、jmap、netstat、tcpdump）
- ELK：日志收集架构、Kibana 查询语法
- Grafana：监控面板配置、告警规则

请给我一个「生产环境常见问题排查 Checklist」。
```

### 模块 7：算法与数据结构

```
【Prompt 片段 - 算法】

你是一位算法教练。我是一名 Java 后端开发者，需要准备面试算法题。我目前 LeetCode 刷题量较少。

请帮我制定一个 30 天刷题计划，每天 3 题，按以下分类：
- Week 1：数组、链表、栈、队列
- Week 2：哈希表、二叉树、堆
- Week 3：动态规划、贪心
- Week 4：回溯、图、设计题

今天请先给我 Day 1 的 3 道题（数组类，中等难度），附带解题思路。
```

---

## 三、30 天复习冲刺计划

> 适用于面试前 1 个月的冲刺复习

### Week 1：Java 核心 + Spring 原理

| 天 | 主题 | 复习内容 | 输出要求 |
|----|------|---------|---------|
| Day 1 | Java 集合 | HashMap、ConcurrentHashMap、ArrayList | 手写 HashMap get/put 伪代码 |
| Day 2 | 并发编程 | 线程池、锁、volatile | 能画出线程池工作流程 |
| Day 3 | JVM | 内存模型、GC、类加载 | 能画出 JVM 内存结构图 |
| Day 4 | Spring IoC/AOP | Bean 生命周期、代理、事务 | 能说出 Bean 生命周期 10+ 步骤 |
| Day 5 | Spring Boot | 自动配置、Starter、SPI | 能手写一个 Starter |
| Day 6 | Spring Cloud | Feign、GateWay、Sentinel | 能画出服务调用链路 |
| Day 7 | 周总结 | 模拟面试 | 3 道算法题 + 5 道基础题 |

### Week 2：数据库 + 中间件

| 天 | 主题 | 复习内容 | 输出要求 |
|----|------|---------|---------|
| Day 8 | MySQL | 索引、SQL 调优、锁 | 写 3 个 SQL 优化案例 |
| Day 9 | Redis | 数据结构、缓存策略、集群 | 画 Redis Cluster 架构图 |
| Day 10 | RabbitMQ | 消息可靠性、死信队列 | 画消息流转图 |
| Day 11 | Kafka | 分区、消费者组、ISR | 对比 RabbitMQ vs Kafka |
| Day 12 | Elasticsearch | 倒排索引、搜索优化 | 解释 ES 搜索原理 |
| Day 13 | 分库分表 | ShardingSphere | 设计一个分库分表方案 |
| Day 14 | 周总结 | 模拟面试 | 3 道算法题 + 数据库场景题 |

### Week 3：分布式 + 系统设计

| 天 | 主题 | 复习内容 | 输出要求 |
|----|------|---------|---------|
| Day 15 | 分布式事务 | Seata、CAP | 对比 AT/TCC/MQ 方案 |
| Day 16 | 分布式锁 | Redis/ZK 实现 | 手写 Redisson 分布式锁 |
| Day 17 | 分布式 ID | 雪花算法 | 手写雪花算法 |
| Day 18 | 系统设计(1) | 秒杀系统 | 画出完整架构图 |
| Day 19 | 系统设计(2) | 短链接系统 | 写出数据库设计 |
| Day 20 | 系统设计(3) | 消息推送 | 写出 API 设计 |
| Day 21 | 周总结 | 系统设计 Mock | 2 道系统设计题 |

### Week 4：项目深挖 + 算法 + 总结

| 天 | 主题 | 复习内容 | 输出要求 |
|----|------|---------|---------|
| Day 22 | 项目深挖 | DBS 财富 API 项目 | 写出 STAR 法则项目描述 |
| Day 23 | 项目深挖 | 海控通用后台/E-Platform | 写出技术亮点 |
| Day 24 | 项目深挖 | 搜索系统/自提商城 | 写出架构设计 |
| Day 25 | 算法冲刺 | Top 100 高频题 | 完成 5 道中等题 |
| Day 26 | 算法冲刺 | Top 100 高频题 | 完成 5 道中等题 |
| Day 27 | 算法冲刺 | 动态规划 | 完成 3 道 DP 题 |
| Day 28 | 模拟面试 | 完整模拟面试 | 技术面 + 算法 + 系统设计 |
| Day 29 | 查漏补缺 | 薄弱点复习 | 整理错题本 |
| Day 30 | 终级模拟 | 终面模拟 | HR 面 + 技术终面 |

---

## 四、面试知识点速查清单

### 必背 50 题

```
请 AI 逐题讲解以下面试高频问题：

【Java 基础】
1. HashMap put 过程（源码级）
2. ConcurrentHashMap 如何保证线程安全
3. synchronized 锁升级过程
4. ThreadLocal 原理与内存泄漏
5. 线程池 7 个参数与 4 种拒绝策略

【JVM】
6. JVM 内存结构（堆、栈、方法区、程序计数器）
7. GC 算法（标记清除、复制、标记整理、分代收集）
8. CMS vs G1 区别
9. 类加载机制（双亲委派）
10. OOM 排查思路

【Spring】
11. Bean 生命周期
12. Spring 循环依赖如何解决
13. @Transactional 失效场景
14. Spring Boot 自动配置原理
15. Feign 调用流程

【数据库】
16. MySQL 索引结构（B+Tree）为什么不用 Hash/B-Tree
17. 聚簇索引 vs 非聚簇索引
18. 最左前缀原则
19. SQL 执行计划 EXPLAIN 各字段含义
20. Redis 缓存穿透/击穿/雪崩 解决方案

【分布式】
21. CAP 理论
22. Seata AT 模式流程
23. RabbitMQ 消息可靠性保证
24. Kafka 消息不丢失配置
25. Sentinel 熔断策略

【系统设计】
26. 如何设计一个秒杀系统
27. 如何设计一个短链接系统
28. 如何设计一个 API 网关
29. 如何设计一个分布式 ID 生成器
30. 如何设计一个限流器
```

---

## 五、项目经验 STAR 法则模板

```
【Prompt 片段 - 项目包装】

请帮我用 STAR 法则包装以下项目经验：

S (Situation 背景): [项目的业务背景和技术挑战]
T (Task 任务): [我的职责和目标]
A (Action 行动): [我具体做了什么，用了什么技术]
R (Result 结果): [量化成果，如性能提升、用户量等]

要求：
- 突出我的技术决策和架构设计能力
- 量化成果指标
- 控制在 200 字以内
- 面试官追问 3 个可能的问题及回答

请根据我提供的简历项目，帮我包装「海控科技通用后台管理系统」这个项目。
```

---

## 六、模拟面试 Prompt

```
【Prompt 片段 - 模拟面试】

你现在是一位大厂 Java 后端面试官。我是一名 6 年经验的 Java 开发者。

请对我进行一场 45 分钟的模拟面试，流程如下：
1. 开场：请我做个 2 分钟的自我介绍
2. 项目深挖（15 分钟）：针对我的 DBS 星展银行项目深入提问
3. 技术基础（15 分钟）：Java、Spring、数据库各 2-3 题
4. 系统设计（10 分钟）：一道中等难度系统设计题
5. 算法（5 分钟）：一道 LeetCode 中等题
6. 面试反馈：指出我的不足和改进建议

请现在开始，先说你的第一个问题。
```

---

## 七、持续更新机制

```
【定期维护 Prompt】

每当我完成一个模块的学习，请帮我：
1. 更新这个知识库文档的学习进度
2. 标记已掌握的内容为 ✅
3. 根据面试反馈调整复习优先级
4. 补充新学到的面试题和最佳实践
```

---

## 附录：当前知识库状态 (基于 study-note)

| 笔记 | 状态 | 待补充 |
|------|------|--------|
| SpringBoot SPI | ✅ 已记录 | 示例代码 |
| Sentinel | ✅ 已记录 | 生产配置案例 |
| Feign | ✅ 已记录 | 源码分析 |
| GateWay | ✅ 已记录 | 自定义过滤器 |
| Redis | ✅ 已记录 | 分布式锁实现 |
| Redis-Cluster | ✅ 已记录 | 运维命令 |
| Seata | ✅ 已记录 | AT vs TCC 对比 |
| RabbitMQ | ✅ 已记录 | 消息可靠性 |
| Canal | ✅ 已记录 | 监控配置 |
| JWT + SpringSecurity | ✅ 已记录 | OAuth2 扩展 |
| MyBatis-Plus | 📝 TODO | 关联查询笔记 |
| Kafka | ✅ 已记录 | 消费者组配置 |
| Docker | ✅ 已记录 | docker-compose |
| Jenkins | ✅ 已记录 | Pipeline 脚本 |
| JDK 9-11 新特性 | ✅ 已记录 | JDK 17 更新 |
| JDK HttpClient | ✅ 已记录 | 性能对比 |
| Sharding-JDBC | ✅ 已记录 | 读写分离 |
| DynamicDatasource | ✅ 已记录 | 事务管理 |
| JPA | ✅ 已记录 | 性能优化 |

---

> **文档版本**: v1.0
> **创建日期**: 2026-05-02
> **适用场景**: Java 后端开发面试复习 / AI 辅助学习