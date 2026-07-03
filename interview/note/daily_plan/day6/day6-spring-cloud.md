# Day 6：Spring Cloud 深度复习

> 计划日期：Week 1 Day 6 | 主题：Feign、GateWay、Sentinel
> 输出要求：**能画出服务调用链路**、能口述 Feign 调用流程、能配置 Gateway 路由与 Sentinel 流控规则

---

## 一、Spring Cloud 体系概览

### 1.1 微服务核心问题

| 问题域 | 传统单体 | 微服务方案 |
|--------|---------|-----------|
| 服务发现 | 本地调用 | Nacos / Eureka / Consul |
| 负载均衡 | 无 | Ribbon（已维护模式）/ Spring Cloud LoadBalancer |
| 远程调用 | 无 | OpenFeign / RestTemplate |
| 网关路由 | 无 | Spring Cloud Gateway |
| 流量治理 | 无 | Sentinel / Resilience4j |
| 配置管理 | 本地文件 | Nacos Config / Spring Cloud Config |
| 链路追踪 | 无 | Sleuth + Zipkin / SkyWalking |

### 1.2 Spring Cloud 常用组件关系

```
                        ┌─────────────────┐
                        │   API Gateway   │  ← 统一入口、鉴权、限流
                        │  (Gateway)      │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
        ┌──────────┐      ┌──────────┐      ┌──────────┐
        │ order-svc│      │ user-svc │      │ product  │
        │          │─────→│          │      │   -svc   │
        │  Feign   │      │          │      │          │
        └────┬─────┘      └────┬─────┘      └────┬─────┘
             │                 │                  │
             └─────────────────┼──────────────────┘
                               ▼
                    ┌─────────────────┐
                    │  Nacos / Eureka │  ← 服务注册与发现
                    │  (Registry)     │
                    └─────────────────┘
                               │
                    ┌─────────────────┐
                    │    Sentinel     │  ← 流控、熔断、降级
                    │  (Dashboard)    │
                    └─────────────────┘
```

### 1.3 版本对应（面试常问）

| Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|-------------|-------------|---------------------|
| 2.6.x | 2021.0.x (Jubilee) | 2021.0.x |
| 2.7.x | 2021.0.x | 2021.0.x |
| 3.0.x | 2022.0.x (Kilburn) | 2022.0.x |
| 3.2.x | 2023.0.x (Leyton) | 2023.0.x |

> 生产环境务必查官方 Release Train 兼容表，版本不匹配是启动失败头号原因。

---

## 二、OpenFeign 声明式 HTTP 客户端

### 2.1 Feign 是什么？

Feign 是一个**声明式** HTTP 客户端：开发者定义接口 + 注解，Feign 在运行时生成动态代理实现远程调用。

```java
@FeignClient(value = "user-service", contextId = "userService1")
public interface UserService {
    @GetMapping("/user")
    Result getUserByName(@RequestParam("name") String name);

    @PostMapping("/user")
    Result saveUser(@RequestBody User user);
}
```

### 2.2 Feign 调用完整链路（必画）

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Feign 一次调用全流程                              │
└──────────────────────────────────────────────────────────────────────────┘

 业务代码 userService.getUserByName("Jayce")
        │
        ▼
 ① JDK 动态代理（Feign 为接口生成的 Proxy）
        │
        ▼
 ② ReflectiveFeign#invoke() → 解析方法注解构建 RequestTemplate
        │   （HTTP Method、URL、Header、Body、参数映射）
        ▼
 ③ Feign 拦截器链（RequestInterceptor）
        │   常见：传递 Authorization、TraceId、自定义 Header
        ▼
 ④ 编码器 Encoder（如 SpringEncoder 将对象序列化为 JSON）
        │
        ▼
 ⑤ 负载均衡选择实例（Spring Cloud LoadBalancer）
        │   从 Nacos/Eureka 获取 user-service 实例列表
        │   按策略（RoundRobin / Random）选一个 ip:port
        ▼
 ⑥ HTTP 客户端发送请求
        │   默认：JDK HttpURLConnection
        │   推荐：OkHttp / Apache HttpClient（连接池）
        ▼
 ⑦ 目标服务处理请求并返回响应
        │
        ▼
 ⑧ 解码器 Decoder（如 SpringDecoder 将 JSON 反序列化为 Result）
        │
        ▼
 ⑨ （可选）Sentinel / 熔断器检查 → 失败走 fallback
        │
        ▼
 ⑩ 返回结果给业务代码
```

### 2.3 核心注解与配置

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.client")  // 扫描 Feign 接口
public class OrderApplication { }
```

| 配置项 | 说明 |
|--------|------|
| `@FeignClient(value="服务名")` | 注册中心中的服务 ID |
| `@FeignClient(contextId="xxx")` | 同服务多接口时避免 Bean 名冲突 |
| `@FeignClient(url="http://...")` | 直连模式，不走注册中心 |
| `@FeignClient(fallbackFactory=...)` | 降级工厂，可获取异常信息 |
| `feign.client.config.default.connectTimeout` | 连接超时 |
| `feign.client.config.default.readTimeout` | 读取超时 |

### 2.4 熔断降级：fallback vs fallbackFactory

```java
// 推荐：fallbackFactory 可拿到异常原因
@Component
public class UserFallbackFactory implements FallbackFactory<UserService> {
    @Override
    public UserService create(Throwable cause) {
        log.error("Feign call failed", cause);
        return new UserService() {
            @Override
            public Result getUserByName(String name) {
                return Result.fail("服务降级：" + cause.getMessage());
            }
            // ...
        };
    }
}
```

> **注意**：Hystrix 已进入维护模式，新项目用 **Sentinel** 或 **Resilience4j** 做熔断。

### 2.5 Feign 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 同服务多 Feign 接口 Bean 冲突 | 默认 Bean 名相同 | 加 `contextId` |
| GET 请求 Body 丢失 | Feign 默认不支持 GET+Body | 换 POST 或自定义 Contract |
| 传参未加注解 | 非 SpringMvc 契约下参数无法映射 | 参数加 `@RequestParam` / `@PathVariable` |
| 首次调用慢 | 懒加载 + 冷启动 | 配置 `eager-load` 或预热 |
| 超时 | 默认超时短 | 配置 connectTimeout / readTimeout |

---

## 三、Spring Cloud Gateway 网关

### 3.1 为什么需要网关？

```
没有网关：
  客户端 → 直接调用 N 个微服务
  问题：每个服务都要实现鉴权、限流、日志、跨域 → 重复劳动

有网关：
  客户端 → Gateway → 微服务
  网关统一：路由、认证、限流、灰度、日志、协议转换
```

### 3.2 Gateway 核心概念

| 概念 | 说明 | 类比 |
|------|------|------|
| **Route（路由）** | 由一个 ID、目标 URI、断言集合、过滤器集合组成 | 一条路由规则 |
| **Predicate（断言）** | 匹配条件，决定请求是否走该路由 | if 条件 |
| **Filter（过滤器）** | 修改请求/响应 | 中间件 |
| **GlobalFilter** | 全局过滤器，作用于所有路由 | 全局拦截器 |

### 3.3 Gateway 请求处理流程

```
Client Request
      │
      ▼
┌─────────────────────────────────────────┐
│           Gateway Handler Mapping        │
│   遍历所有 Route，Predicate 匹配          │
└─────────────────┬───────────────────────┘
                  │ 匹配到 Route
                  ▼
┌─────────────────────────────────────────┐
│         Gateway Filter Chain             │
│                                          │
│  GlobalFilter (pre)                      │
│      → Route Filter (pre)                │
│      → 转发到目标服务（LoadBalancer）     │
│      → Route Filter (post)               │
│      → GlobalFilter (post)               │
└─────────────────┬───────────────────────┘
                  ▼
            Client Response
```

### 3.4 路由配置（YAML）

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-route
          uri: lb://user-service          # lb:// 开启负载均衡
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1              # 去掉 /api，转发 /user/**
            - AddRequestHeader=X-Gateway, true

        - id: order-service-route
          uri: lb://order-service
          predicates:
            - Path=/api/order/**
            - Method=GET,POST
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter    # 限流
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                key-resolver: "#{@ipKeyResolver}"
```

### 3.5 常用 Predicate

| Predicate | 示例 | 说明 |
|-----------|------|------|
| `Path` | `Path=/user/{id}` | 路径匹配 |
| `Method` | `Method=GET` | HTTP 方法 |
| `Header` | `Header=X-Token, \d+` | 请求头正则 |
| `Query` | `Query=username` | 查询参数存在 |
| `After/Before/Between` | 时间窗口 | 定时生效 |
| `RemoteAddr` | `RemoteAddr=192.168.1.0/24` | IP 白名单 |
| `Weight` | `Weight=group1, 8` | 权重灰度 |

### 3.6 常用 Filter

| Filter | 作用 |
|--------|------|
| `StripPrefix=n` | 去掉路径前 n 段 |
| `PrefixPath=/xxx` | 添加路径前缀 |
| `AddRequestHeader` | 添加请求头 |
| `RewritePath` | 正则重写路径 |
| `RequestRateLimiter` | 令牌桶限流（需 Redis） |
| `Retry` | 失败重试 |
| `CircuitBreaker` | 熔断（Resilience4j） |

### 3.7 自定义 GlobalFilter

```java
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (token == null || !validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;  // 数值越小优先级越高
    }
}
```

> Gateway 基于 **WebFlux（Reactor）**，过滤器返回 `Mono<Void>`，与 Servlet 模型不同。

### 3.8 Gateway vs Zuul

| 维度 | Zuul 1.x | Spring Cloud Gateway |
|------|---------|---------------------|
| 底层 | Servlet 阻塞 IO | WebFlux 非阻塞 |
| 性能 | 较低 | 较高 |
| 维护状态 | 停止新特性 | 官方主推 |
| 过滤器模型 | Filter | GatewayFilter + GlobalFilter |

---

## 四、Sentinel 流量治理

### 4.1 Sentinel 核心能力

```
                    ┌─────────────┐
                    │  Sentinel   │
                    └──────┬──────┘
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
      流量控制          熔断降级         系统保护
     (Flow Rule)    (Degrade Rule)   (System Rule)
           │               │               │
           ▼               ▼               ▼
      QPS/线程数      慢调用/异常比      CPU/Load/RT
      关联/链路       时间窗口恢复       入口 QPS
      预热/排队
```

### 4.2 Sentinel vs Hystrix

| 维度 | Hystrix | Sentinel |
|------|---------|----------|
| 隔离策略 | 线程池 / 信号量 | 信号量（无线程池隔离） |
| 熔断策略 | 异常比例 | 慢调用比例、异常比例、异常数 |
| 流控 | 有限支持 | 丰富（QPS、线程、关联、链路、热点） |
| 规则管理 | 代码 / 配置 | Dashboard 动态推送 |
| 维护状态 | 停止开发 | 活跃维护（阿里） |

### 4.3 接入配置

```yaml
spring:
  application:
    name: order-service
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080   # Sentinel 控制台
        port: 8719                # 客户端与控制台通信端口
      eager: true                 # 取消懒加载，启动即注册
```

```java
@RestController
public class OrderController {

    @GetMapping("/order/{id}")
    @SentinelResource(value = "getOrder", blockHandler = "handleBlock")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }

    // 限流/熔断时的处理方法（参数列表 = 原方法 + BlockException）
    public Order handleBlock(Long id, BlockException ex) {
        return Order.fallback();
    }
}
```

### 4.4 流控规则（Flow Rule）

| 字段 | 说明 |
|------|------|
| `resource` | 资源名（@SentinelResource 的 value） |
| `grade` | QPS（每秒请求数）或线程数 |
| `count` | 阈值 |
| `strategy` | 直接 / 关联 / 链路 |
| `controlBehavior` | 快速失败 / Warm Up 预热 / 排队等待 |

```java
// 编程式规则：QPS 超过 2 则限流
FlowRule rule = new FlowRule();
rule.setResource("getOrder");
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
rule.setCount(2);
FlowRuleManager.loadRules(Collections.singletonList(rule));
```

**三种流控模式：**

```
直接：对当前资源限流
关联：当关联资源（如写库）达阈值时，限流当前资源（如读库）
链路：只限制从指定入口（如 /api/order）进来的流量
```

### 4.5 熔断降级规则（Degrade Rule）

| 策略 | 触发条件 | 恢复 |
|------|---------|------|
| 慢调用比例 | RT > 阈值 且比例 > 设定值 | 时间窗口后半开探测 |
| 异常比例 | 异常数/总请求 > 比例 | 时间窗口后恢复 |
| 异常数 | 单位时间内异常数 > 阈值 | 时间窗口后恢复 |

```
正常 → 慢调用/异常增加 → 熔断（快速失败）→ 时间窗口 → 半开（放行少量请求）→ 正常/继续熔断
```

### 4.6 热点参数限流

```java
@GetMapping("/product")
@SentinelResource(value = "getProduct", blockHandler = "handleHotKey")
public Product getProduct(@RequestParam Long productId) { ... }

// 规则：productId=爆款商品 时限 QPS=2，其他商品 QPS=100
ParamFlowRule rule = new ParamFlowRule("getProduct")
    .setParamIdx(0)
    .setCount(100);
ParamFlowItem item = new ParamFlowItem()
    .setObject(String.valueOf(10086L))
    .setClassType(Long.class.getName())
    .setCount(2);
rule.setParamFlowItemList(Collections.singletonList(item));
```

### 4.7 Sentinel 整合 Gateway

```yaml
spring:
  cloud:
    sentinel:
      scg:
        fallback:
          mode: response
          response-body: '{"code":429,"msg":"请求过于频繁"}'
```

Gateway 路由 ID 即为 Sentinel 资源名，可在 Dashboard 对网关路由配置流控。

---

## 五、完整服务调用链路（输出要求 · 必画）

### 5.1 典型微服务调用全景图

```
┌─────────┐
│ Browser │
│ / App   │
└────┬────┘
     │ ① HTTPS  GET /api/order/1001
     ▼
┌────────────────────────────────────────────────────────────┐
│                  Spring Cloud Gateway                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐ │
│  │ AuthFilter   │→ │ RateLimiter  │→ │ Route Predicate  │ │
│  │ 校验 JWT     │  │ (Sentinel)   │  │ Path=/api/order  │ │
│  └──────────────┘  └──────────────┘  └────────┬─────────┘ │
└─────────────────────────────────────────────────┼───────────┘
                                                  │ ② lb://order-service
                                                  ▼
┌────────────────────────────────────────────────────────────┐
│                     order-service                           │
│                                                             │
│  OrderController.getOrder(1001)                             │
│       │                                                     │
│       ▼                                                     │
│  @SentinelResource 检查流控/熔断                             │
│       │                                                     │
│       ▼                                                     │
│  OrderService ──Feign──→ user-service.getUser(userId)  ③   │
│       │                        │                            │
│       │                        ▼                            │
│       │              LoadBalancer 选实例                   │
│       │                        │                            │
│       ├──Feign──→ product-service.getProduct(pid)      ④   │
│       │                                                     │
│       ▼                                                     │
│  聚合结果 → 返回 JSON                                        │
└────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│    Nacos    │     │   Sentinel   │     │   Redis     │
│  服务注册    │     │   Dashboard  │     │  缓存/限流   │
│  配置中心    │     │   规则推送    │     │             │
└─────────────┘     └──────────────┘     └─────────────┘
```

### 5.2 调用链路 10 步口述版

| 步骤 | 环节 | 说明 |
|------|------|------|
| 1 | 客户端请求 | 访问 Gateway 统一入口 `/api/xxx` |
| 2 | Gateway 全局过滤器 | JWT 鉴权、TraceId 注入 |
| 3 | Gateway 路由匹配 | Predicate 匹配 Path/Method |
| 4 | Gateway 限流 | Sentinel / RequestRateLimiter |
| 5 | 负载均衡转发 | `lb://service-name` → Nacos 取实例 |
| 6 | 目标服务接收 | Controller 处理，Sentinel 资源检查 |
| 7 | 服务间 Feign 调用 | 代理 → 编码 → LoadBalancer → HTTP |
| 8 | 下游服务处理 | 可能继续链式调用 |
| 9 | 响应返回 | Decoder 解码 → 聚合 → JSON 响应 |
| 10 | 异常降级 | 超时/熔断 → fallback / blockHandler |

---

## 六、面试高频题（带答案）

### Q1：Feign 的工作原理？

**答**：Feign 通过 `@EnableFeignClients` 扫描接口，利用 JDK 动态代理生成实现类。调用时解析 `@GetMapping` 等注解构建 `RequestTemplate`，经编码器序列化后，通过 LoadBalancer 从注册中心获取实例地址，使用 HTTP 客户端发送请求，最后经解码器反序列化返回。可插拔 Encoder/Decoder/Contract/拦截器。

### Q2：Feign 如何实现负载均衡？

**答**：Spring Cloud 中 Feign 整合 **Spring Cloud LoadBalancer**（替代 Ribbon）。`@FeignClient("user-service")` 的 value 作为服务名，LoadBalancer 从 Nacos/Eureka 拉取实例列表，按 RoundRobin 等策略选择目标 `host:port`。

### Q3：Gateway 的路由匹配流程？

**答**：请求进入 `DispatcherHandler`，`RoutePredicateHandlerMapping` 遍历所有 Route，对每个 Route 的 Predicate 集合做 AND 匹配，第一个全部满足的 Route 被选中，然后执行其 Filter 链，最终通过 `lb://` 或 `http://` 转发到下游服务。

### Q4：Gateway 和 Zuul 的区别？

**答**：见 **3.8 对比表**。核心差异：Gateway 基于 WebFlux 非阻塞，Zuul 1.x 基于 Servlet 阻塞；Spring Cloud 官方已主推 Gateway。

### Q5：Gateway 如何实现限流？

**答**：① 内置 `RequestRateLimiter` 过滤器 + Redis 令牌桶；② 整合 Sentinel 对路由 ID 做流控；③ 自定义 GlobalFilter 实现限流逻辑。

### Q6：Sentinel 流控模式有哪些？

**答**：**直接**（限当前资源）、**关联**（关联资源达阈值时限当前资源）、**链路**（只限指定入口的流量）。效果上有快速失败、Warm Up 预热、匀速排队。

### Q7：Sentinel 熔断策略有哪些？

**答**：**慢调用比例**（RT 超阈值且占比超限）、**异常比例**、**异常数**。触发后进入熔断时间窗口，结束后半开探测。

### Q8：Feign 调用超时如何配置？

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
      user-service:    # 针对特定服务
        readTimeout: 3000
```

### Q9：如何保证 Feign 调用传递 Token？

```java
@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor authInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String token = attrs.getRequest().getHeader("Authorization");
                template.header("Authorization", token);
            }
        };
    }
}
```

### Q10：生产环境 Sentinel 规则如何持久化？

**答**：默认规则存内存，重启丢失。生产用 **Nacos / Apollo / Redis** 等数据源持久化，通过 `sentinel-datasource-nacos` 将规则写入 Nacos，Dashboard 修改后推送到 Nacos，各服务订阅自动更新。

---

## 七、手写代码与实战

### 7.1 最小 Feign 客户端

```java
@FeignClient(name = "user-service", fallbackFactory = UserFallbackFactory.class)
public interface UserClient {
    @GetMapping("/users/{id}")
    UserDTO getById(@PathVariable("id") Long id);
}
```

### 7.2 最小 Gateway 路由（Java Config）

```java
@Bean
public RouteLocator customRoutes(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("user_route", r -> r.path("/api/user/**")
            .filters(f -> f.stripPrefix(1))
            .uri("lb://user-service"))
        .build();
}
```

### 7.3 练习：设计调用链路的限流策略

```
场景：秒杀下单接口 /api/order/seckill
要求：
  1. Gateway 层全局限流 QPS 10000
  2. order-service 该接口 QPS 5000
  3. 同一用户 1 秒内最多 1 次请求
```

<details>
<summary>参考答案</summary>

```
1. Gateway：RequestRateLimiter（Redis 令牌桶）或 Sentinel 网关流控，资源名 order-seckill-route
2. order-service：@SentinelResource("seckill") + FlowRule QPS=5000
3. 热点限流：ParamFlowRule 对 userId 参数 idx=0，count=1，durationInSec=1
   或在 Gateway 用自定义 KeyResolver 按 userId 限流
```
</details>

---

## 八、易错点 / 坑

| # | 易错点 | 说明 |
|---|--------|------|
| 1 | Gateway 引入 `spring-boot-starter-web` | 与 WebFlux 冲突，应用 `spring-cloud-starter-gateway` |
| 2 | Feign 接口不加 Spring MVC 注解 | 参数无法正确映射到 HTTP 请求 |
| 3 | 同服务多个 `@FeignClient` 不加 `contextId` | Bean 定义冲突，启动失败 |
| 4 | Sentinel 控制台看不到服务 | 默认懒加载，需先访问接口；或设 `eager=true` |
| 5 | `blockHandler` 方法签名错误 | 必须与原方法参数一致 + 末尾加 `BlockException` |
| 6 | Gateway `StripPrefix` 与 Controller 路径不匹配 | 剥前缀后路径对不上，404 |
| 7 | Feign 默认不重试 | 需配置 `Retryer` 或 Spring Retry |
| 8 | 混淆 Gateway Filter 与 GlobalFilter | GatewayFilter 绑路由；GlobalFilter 全局生效 |

---

## 九、对比速查表

| 对比项 | Feign | RestTemplate |
|--------|-------|-------------|
| 风格 | 声明式接口 | 编程式 |
| 负载均衡 | 内置 | 需 `@LoadBalanced` |
| 可读性 | 高 | 低 |
| 推荐度 | ⭐⭐⭐ | 维护模式 |

| 对比项 | Sentinel | Hystrix |
|--------|----------|---------|
| 线程隔离 | 无（信号量） | 有（线程池） |
| 规则动态推送 | Dashboard | 有限 |
| 热点限流 | 支持 | 不支持 |
| 维护 | 活跃 | 停止 |

---

## 十、关联笔记（study-note 仓库）

| 主题 | 路径 | 说明 |
|------|------|------|
| Feign 使用 | `spring/springcloud/Feign.md` | 注解、Fallback 示例 |
| Gateway | `spring/springcloud/GateWay.md` | 路由、断言、过滤器 |
| Sentinel | `spring/springcloud/Sentinel.md` | 流控、热点、Dashboard |
| Spring Security | `spring/LoginBySpringSecurity.md` | 网关鉴权可串联 |

---

## 十一、今日自测 Checklist

- [ ] 能画出 **Feign 调用 10 步** 链路图
- [ ] 能画出 **Gateway → 微服务 → Feign 下游** 完整架构图
- [ ] 能说出 Gateway 三大核心概念（Route / Predicate / Filter）
- [ ] 能配置 YAML 路由（Path + StripPrefix + 限流）
- [ ] 能对比 Gateway 与 Zuul
- [ ] 能说出 Sentinel 三种流控模式和三种熔断策略
- [ ] 能写出 `@SentinelResource` + `blockHandler` 示例
- [ ] 能解释 Feign 如何传递 Header（RequestInterceptor）
- [ ] 能说出 Sentinel 规则持久化方案
- [ ] 完成本节 **限流策略设计** 练习

---

## 十二、延伸阅读（可选，30 分钟）

1. 阅读 `spring/springcloud/Feign.md` 中的 FallbackFactory 示例
2. 阅读 Gateway 自定义 `GlobalFilter` 代码（`GateWay.md` 第 98 行起）
3. 本地启动 Sentinel Dashboard，对测试接口配 QPS 规则观察效果

---

> **Day 6 完成！** 明天 Day 7：Week 1 周总结 + 模拟面试 — 3 道算法题 + 5 道基础题
