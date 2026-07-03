# Day 5：Spring Boot 深度复习

> 计划日期：Week 1 Day 5 | 主题：自动配置、Starter、SPI
> 输出要求：**能手写一个 Starter**、能口述自动配置加载流程、能对比 `spring.factories` 与 Boot 3 新机制

---

## 一、Spring Boot 核心概念

### 1.1 为什么需要 Spring Boot？

| 痛点（传统 Spring） | Boot 的解法 |
|-------------------|------------|
| XML / Java 配置繁琐 | **约定优于配置**，零 XML 启动 |
| 依赖版本冲突（Jar Hell） | **Starter** 一站式依赖管理 |
| 内嵌容器需单独部署 WAR | 内嵌 Tomcat/Jetty/Undertow，打 Jar 即运行 |
| 第三方组件接入成本高 | **自动配置** + SPI 扩展机制 |

### 1.2 核心注解拆解

```java
@SpringBootApplication
// 等价于以下三个注解的组合：
// @SpringBootConfiguration  → 本质是 @Configuration，标记配置类
// @EnableAutoConfiguration  → 开启自动配置（核心！）
// @ComponentScan            → 扫描当前包及子包下的 @Component
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

| 注解 | 作用 | 面试一句话 |
|------|------|-----------|
| `@SpringBootConfiguration` | 标记为配置类 | 等价 `@Configuration` |
| `@EnableAutoConfiguration` | 导入 `AutoConfigurationImportSelector` | 自动配置入口 |
| `@ComponentScan` | 组件扫描 | 默认只扫启动类所在包 |
| `@ConditionalOnXxx` | 条件装配 | 满足条件才注册 Bean |

### 1.3 Spring Boot 启动流程（精简版）

```
main()
  │
  ▼
SpringApplication.run()
  │
  ├─ 1. 创建 SpringApplication 实例
  │      ├─ 推断应用类型（Servlet / Reactive / None）
  │      ├─ 加载 ApplicationContextInitializer
  │      └─ 加载 ApplicationListener
  │
  ├─ 2. run() 核心流程
  │      ├─ 准备 Environment（加载 application.yml/properties）
  │      ├─ 打印 Banner
  │      ├─ 创建 ApplicationContext
  │      ├─ prepareContext()：注册 Bean、执行 Initializer
  │      ├─ refreshContext()：★ 刷新容器（IoC 核心，见 Day 4）
  │      └─ afterRefresh()：启动内嵌 Web 容器
  │
  └─ 3. 返回 ApplicationContext
```

> **面试点**：`refreshContext()` 内部调用 `AbstractApplicationContext#refresh()`，与 Day 4 Bean 生命周期直接关联。

---

## 二、自动配置原理（面试核心）

### 2.1 @EnableAutoConfiguration 做了什么？

```java
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration { }
```

`AutoConfigurationImportSelector` 实现 `DeferredImportSelector`，在容器刷新后期加载所有自动配置类。

### 2.2 自动配置加载流程图

```
@EnableAutoConfiguration
        │
        ▼
AutoConfigurationImportSelector#getAutoConfigurationEntry()
        │
        ├─ getCandidateConfigurations()
        │      │
        │      ▼
        │  SpringFactoriesLoader.loadFactoryNames(
        │      EnableAutoConfiguration.class, classLoader)
        │      │
        │      ▼
        │  读取所有 jar 中 META-INF/spring.factories
        │  的 org.springframework.boot.autoconfigure.EnableAutoConfiguration 键
        │      │
        │      ▼
        │  得到候选配置类列表（如 DataSourceAutoConfiguration、
        │  RedisAutoConfiguration、WebMvcAutoConfiguration ...）
        │
        ├─ 去重（AutoConfigurationImportFilter）
        ├─ 排除 @SpringBootApplication(exclude = {...}) 指定的类
        └─ 按 @ConditionalOnXxx 条件过滤 → 最终注册到容器
```

### 2.3 以 DataSource 为例看自动配置类结构

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({ DataSourceConfiguration.Hikari.class, ... })
public class DataSourceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.datasource.type",
                             havingValue = "com.zaxxer.hikari.HikariDataSource")
    static class Hikari {
        @Bean
        @ConfigurationProperties(prefix = "spring.datasource.hikari")
        HikariDataSource dataSource(DataSourceProperties properties) {
            // 根据配置文件创建 HikariDataSource
        }
    }
}
```

**自动配置类三板斧：**

1. `@ConditionalOnClass` — classpath 有某类才生效
2. `@ConditionalOnMissingBean` — 用户未自定义 Bean 才生效（**允许覆盖**）
3. `@EnableConfigurationProperties` — 绑定 `application.yml` 配置到 Properties 类

### 2.4 常用条件注解

| 注解 | 条件 |
|------|------|
| `@ConditionalOnClass` | classpath 存在指定类 |
| `@ConditionalOnMissingClass` | classpath 不存在指定类 |
| `@ConditionalOnBean` | 容器中存在指定 Bean |
| `@ConditionalOnMissingBean` | 容器中不存在指定 Bean |
| `@ConditionalOnProperty` | 配置文件中指定属性满足条件 |
| `@ConditionalOnWebApplication` | 当前是 Web 应用 |
| `@ConditionalOnExpression` | SpEL 表达式为 true |

### 2.5 spring.factories vs Boot 3 新机制

| 维度 | Spring Boot 2.x | Spring Boot 3.x |
|------|----------------|-----------------|
| 配置文件 | `META-INF/spring.factories` | `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| 格式 | `key=全限定类名1,全限定类名2` | 每行一个全限定类名 |
| SPI 通用机制 | `spring.factories` 仍可用于其他 SPI | 自动配置专用新文件 |

```properties
# Boot 2.x — META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.starter.MyAutoConfiguration
```

```
# Boot 3.x — META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.example.starter.MyAutoConfiguration
```

---

## 三、Starter 机制

### 3.1 Starter 是什么？

Starter = **依赖聚合** + **自动配置** + **默认配置**

```
spring-boot-starter-web
    ├── spring-boot-starter（核心）
    ├── spring-boot-starter-tomcat（内嵌容器）
    ├── spring-web / spring-webmvc
    └── jackson（JSON 序列化）
```

### 3.2 官方 Starter 命名规范

| 类型 | 命名 | 示例 |
|------|------|------|
| 官方 Starter | `spring-boot-starter-{name}` | `spring-boot-starter-data-redis` |
| 第三方 Starter | `{name}-spring-boot-starter` | `mybatis-spring-boot-starter` |

### 3.3 Starter 模块结构（推荐）

```
my-spring-boot-starter/
├── my-spring-boot-autoconfigure/   # 自动配置模块
│   ├── MyProperties.java           # 配置属性类
│   ├── MyAutoConfiguration.java    # 自动配置类
│   └── META-INF/spring/
│       └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── my-spring-boot-starter/         # Starter 模块（仅 pom 依赖聚合）
    └── pom.xml                     # 依赖 autoconfigure + 第三方库
```

> **最佳实践**：自动配置代码与 Starter 依赖分离，方便高级用户只引 autoconfigure 而排除不需要的传递依赖。

---

## 四、SPI 机制（Java SPI vs Spring SPI）

### 4.1 Java SPI

```java
// 1. 定义接口
public interface PaymentService { void pay(); }

// 2. 实现类
public class AlipayService implements PaymentService { ... }

// 3. META-INF/services/com.example.PaymentService 文件内容：
//    com.example.AlipayService

// 4. 加载
ServiceLoader<PaymentService> loader = ServiceLoader.load(PaymentService.class);
```

| 特点 | 说明 |
|------|------|
| 加载时机 | 懒加载，遍历 `ServiceLoader` 时实例化 |
| 实例化方式 | 反射，无 Spring 管理 |
| 典型用途 | JDBC Driver、SLF4J 绑定 |

### 4.2 Spring SPI（SpringFactoriesLoader）

Spring 扩展了 Java SPI 思想，通过 `META-INF/spring.factories` 注册多种工厂：

```properties
# 常见 key
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.MyAutoConfiguration

org.springframework.context.ApplicationContextInitializer=\
  com.example.MyContextInitializer

org.springframework.context.ApplicationListener=\
  com.example.MyApplicationListener

org.springframework.boot.env.EnvironmentPostProcessor=\
  com.example.MyEnvironmentPostProcessor
```

### 4.3 第三方 Bean 注册方式对比

| 方式 | 侵入性 | 推荐度 | 说明 |
|------|--------|--------|------|
| `spring.factories` / `AutoConfiguration.imports` | 低 | ⭐⭐⭐ | 标准 Starter 方式 |
| `@ComponentScan` 指定包 | 中 | ⭐⭐ | 使用者需在启动类加扫描路径 |
| `@Import` + 自定义 `@EnableXxx` | 低 | ⭐⭐⭐ | 语义清晰，如 `@EnableFeignClients` |
| 直接 `@Bean` 注册 | 高 | ⭐ | 仅适合内部项目 |

> 详见仓库笔记：`spring/springbootSPI/SpringBootSPI.md`

---

## 五、手写 Starter 完整实战（输出要求）

### 5.1 需求

封装一个 `demo-spring-boot-starter`，实现：

1. 读取配置 `demo.prefix`（默认 `Hello`）
2. 自动注册 `DemoService` Bean
3. 提供 `demoService.greet(name)` 方法返回 `{prefix}, {name}!`

### 5.2 项目结构

```
demo-spring-boot-starter/
├── pom.xml
└── src/main/
    ├── java/com/example/demo/
    │   ├── DemoProperties.java
    │   ├── DemoService.java
    │   └── DemoAutoConfiguration.java
    └── resources/META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### 5.3 pom.xml

```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>demo-spring-boot-starter</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

### 5.4 配置属性类

```java
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {
    /** 问候前缀，默认 Hello */
    private String prefix = "Hello";

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
}
```

### 5.5 核心服务

```java
public class DemoService {
    private final DemoProperties properties;

    public DemoService(DemoProperties properties) {
        this.properties = properties;
    }

    public String greet(String name) {
        return properties.getPrefix() + ", " + name + "!";
    }
}
```

### 5.6 自动配置类

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(DemoService.class)
@EnableConfigurationProperties(DemoProperties.class)
@ConditionalOnProperty(prefix = "demo", name = "enabled",
                       havingValue = "true", matchIfMissing = true)
public class DemoAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DemoService demoService(DemoProperties properties) {
        return new DemoService(properties);
    }
}
```

### 5.7 注册自动配置（Boot 3）

```
# META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.example.demo.DemoAutoConfiguration
```

### 5.8 使用者接入

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>demo-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```yaml
demo:
  enabled: true
  prefix: Hi
```

```java
@RestController
public class HelloController {
    @Autowired
    private DemoService demoService;

    @GetMapping("/hello")
    public String hello(@RequestParam String name) {
        return demoService.greet(name);  // 输出：Hi, Jayce!
    }
}
```

### 5.9 进阶：自定义 @Enable 注解（可选）

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(DemoAutoConfiguration.class)
public @interface EnableDemo { }
```

使用者可在配置类上 `@EnableDemo` 显式开启，适合需要精细控制的场景。

---

## 六、面试高频题（带答案）

### Q1：Spring Boot 自动配置原理？

**答**：`@EnableAutoConfiguration` 导入 `AutoConfigurationImportSelector`，通过 `SpringFactoriesLoader` 读取所有 jar 中 `META-INF/spring.factories`（Boot 3 为 `AutoConfiguration.imports`）里注册的 `EnableAutoConfiguration` 配置类，再经 `@ConditionalOnXxx` 条件过滤，将满足条件的配置类注册为 Bean。用户可通过 `@SpringBootApplication(exclude=...)` 或配置 `spring.autoconfigure.exclude` 排除。

### Q2：如何自定义 Starter？

**答**：① 创建 `xxx-spring-boot-autoconfigure` 模块，编写 `@Configuration` + `@ConditionalOnXxx` + `@EnableConfigurationProperties`；② 在 `META-INF/spring/...AutoConfiguration.imports` 注册；③ 创建 `xxx-spring-boot-starter` 模块聚合依赖；④ 使用者引入 Starter 依赖即可，无需 `@ComponentScan`。

### Q3：@ConfigurationProperties 和 @Value 区别？

| 维度 | @ConfigurationProperties | @Value |
|------|-------------------------|--------|
| 绑定方式 | 批量绑定前缀下所有属性 | 单个属性 |
| 类型安全 | 强类型，支持嵌套对象 | 字符串为主 |
| 松散绑定 | 支持 `demo-prefix` ↔ `demoPrefix` | 不支持 |
| JSR-303 校验 | 支持 `@Validated` | 不支持 |
| 推荐场景 | 多属性配置类 | 单个零散配置 |

### Q4：Spring Boot 如何读取配置文件？优先级？

**答**：通过 `Environment` 抽象，支持 properties/yml/yaml。高优先级覆盖低优先级，常见顺序（高→低）：

```
命令行参数 > SPRING_APPLICATION_JSON > 系统属性 > 操作系统环境变量
> jar 外 application-{profile}.properties > jar 内 application-{profile}.properties
> jar 外 application.properties > jar 内 application.properties
```

### Q5：spring-boot-starter-parent 做了什么？

**答**：继承 `spring-boot-dependencies` BOM 统一版本号；配置默认 Java 编译版本；设置资源过滤；配置 repackage 插件打可执行 Fat Jar。实际项目也常改用 `dependencyManagement` 导入 BOM 而不继承 parent。

### Q6：Fat Jar 启动原理？

**答**：`spring-boot-maven-plugin` repackage 后，Jar 结构为 `BOOT-INF/classes`（业务代码）+ `BOOT-INF/lib`（依赖）+ `org.springframework.boot.loader.JarLauncher`。`java -jar` 时 `JarLauncher` 自定义类加载器加载嵌套 Jar，再启动 `main` 方法。

### Q7：如何禁用某个自动配置？

```java
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
// 或
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

### Q8：Spring Boot Actuator 有哪些常用端点？

| 端点 | 作用 |
|------|------|
| `/actuator/health` | 健康检查 |
| `/actuator/info` | 应用信息 |
| `/actuator/metrics` | 指标监控 |
| `/actuator/env` | 环境变量 |
| `/actuator/beans` | 所有 Bean |
| `/actuator/loggers` | 日志级别动态调整 |

### Q9：Java SPI 和 Spring SPI 区别？

**答**：Java SPI 通过 `ServiceLoader` + `META-INF/services` 加载接口实现，实例不由 Spring 管理；Spring SPI 通过 `SpringFactoriesLoader` + `spring.factories` 加载多种扩展点，与 Spring 容器生命周期集成，是自动配置的基础。

### Q10：为什么 @SpringBootApplication 默认扫不到第三方 jar 里的 @Component？

**答**：`@ComponentScan` 默认只扫描**启动类所在包及其子包**。第三方 jar 中的 `@Service` 等注解不会被扫描，需通过 `spring.factories` 自动配置或 `@Import` 机制注册 Bean。这正是 Starter 自动配置存在的原因。

---

## 七、核心流程图（面试白板必备）

### 7.1 自动配置决策流程

```
classpath 有 Redis 相关类？
    │
    ├─ No  → RedisAutoConfiguration 不加载
    │
    └─ Yes → 检查 @ConditionalOnMissingBean(RedisTemplate)
                │
                ├─ 用户已自定义 → 跳过
                └─ 未自定义 → 读取 spring.data.redis.* 创建 RedisTemplate
```

### 7.2 Starter 装配全景

```
使用者 pom 引入 my-starter
        │
        ▼
传递依赖 my-autoconfigure + 第三方 SDK
        │
        ▼
Spring Boot 启动 → 读取 AutoConfiguration.imports
        │
        ▼
MyAutoConfiguration 条件满足 → 注册 Bean
        │
        ▼
使用者 @Autowired 直接使用，零配置
```

---

## 八、手写代码与实战

### 8.1 面试手写：最小自动配置类骨架

```java
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyProperties.class)
public class MyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties props) {
        return new MyService(props);
    }
}
```

### 8.2 练习：判断下列配置是否生效

```yaml
# 场景 A：classpath 无 redis 依赖，配置了 spring.data.redis.host
# 场景 B：自定义了 RedisTemplate Bean，未排除 RedisAutoConfiguration
# 场景 C：demo.enabled=false，Starter 有 @ConditionalOnProperty(matchIfMissing=true)
```

<details>
<summary>答案</summary>

```
场景 A：RedisAutoConfiguration 因 @ConditionalOnClass 不满足而不加载，配置项被忽略。
场景 B：自动配置检测到 @ConditionalOnMissingBean(RedisTemplate)，用户 Bean 优先，自动配置跳过。
场景 C：matchIfMissing=true 表示缺省启用；显式 false 时 DemoAutoConfiguration 不加载。
```
</details>

### 8.3 练习：将 Boot 2.x spring.factories 迁移到 Boot 3

```properties
# 旧文件 META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.FooAutoConfiguration,\
  com.example.BarAutoConfiguration
```

<details>
<summary>答案</summary>

```
新建 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports：

com.example.FooAutoConfiguration
com.example.BarAutoConfiguration

每行一个类名，无需 key= 前缀。
```
</details>

---

## 九、易错点 / 坑

| # | 易错点 | 说明 |
|---|--------|------|
| 1 | 自动配置类加 `@ComponentScan` | 错误！应通过 `AutoConfiguration.imports` 或 `spring.factories` 注册 |
| 2 | 自动配置类被组件扫描扫到 | 应用 `@Configuration` 且不被 `@ComponentScan` 覆盖；仅通过 imports 加载 |
| 3 | `@ConditionalOnMissingBean` 写在错误位置 | 应写在 `@Bean` 方法上，而非类上（类上语义是「整个配置类」） |
| 4 | Starter 中引入 `spring-boot-starter-web` | Starter 应轻量，避免引入不必要的传递依赖 |
| 5 | 混淆 `spring.factories` 的两个用途 | `EnableAutoConfiguration` 是自动配置；其他 key 是不同 SPI 扩展点 |
| 6 | Boot 2 写法用于 Boot 3 项目 | Boot 3 需改用 `AutoConfiguration.imports` + Jakarta EE 包名 |
| 7 | 配置类未加 `@EnableConfigurationProperties` | Properties 类不会绑定配置文件，字段为 null |
| 8 | 忘记配置 `spring-boot-configuration-processor` | IDE 无法提示自定义配置项的 metadata |

---

## 十、对比速查表

| 对比项 | Spring | Spring Boot |
|--------|--------|-------------|
| 配置方式 | XML / 大量 Java Config | 自动配置 + 外部化配置 |
| 部署 | 外置 Tomcat WAR | 内嵌容器可执行 Jar |
| 依赖管理 | 手动对齐版本 | Starter + BOM |
| 监控 | 需自行集成 | Actuator 开箱即用 |

| 对比项 | @ComponentScan | 自动配置 |
|--------|---------------|---------|
| 扫描范围 | 指定包路径 | classpath + 条件注解 |
| 适用对象 | 本项目的 @Component | 第三方 jar 中的配置类 |
| 触发方式 | 启动类注解 | `@EnableAutoConfiguration` |

---

## 十一、关联笔记（study-note 仓库）

| 主题 | 路径 | 说明 |
|------|------|------|
| Spring Boot SPI 实战 | `spring/springbootSPI/SpringBootSPI.md` | 第三方 Bean 注册完整示例 |
| SPI 示例代码 | `spring/springbootSPI/third/` | third 组件源码 |
| SPI 使用者 | `spring/springbootSPI/parent/` | parent 工程接入示例 |
| JPA + Boot | `dataBase/JPA-SpringBoot.md` | 数据层自动配置延伸 |

---

## 十二、今日自测 Checklist

- [ ] 能拆解 `@SpringBootApplication` 三个子注解的作用
- [ ] 能口述自动配置从 `@EnableAutoConfiguration` 到 Bean 注册的完整链路
- [ ] 能说出 **5 种** 常用 `@ConditionalOnXxx` 注解
- [ ] 能解释 `@ConditionalOnMissingBean` 如何实现「用户配置优先」
- [ ] 能对比 Boot 2 `spring.factories` 与 Boot 3 `AutoConfiguration.imports`
- [ ] 能说出 Starter 命名规范及推荐模块拆分方式
- [ ] 能**手写**一个包含 Properties + AutoConfiguration + imports 的最小 Starter
- [ ] 能对比 Java SPI 与 Spring SPI 的区别
- [ ] 能说出配置文件加载优先级（至少 4 层）
- [ ] 能解释 Fat Jar 的启动原理
- [ ] 完成本节 **2 道** 判断练习

---

## 十三、延伸阅读（可选，30 分钟）

1. Debug 启动流程：在 `AutoConfigurationImportSelector#getAutoConfigurationEntry` 打断点，观察加载了哪些配置类
2. 阅读 `DataSourceAutoConfiguration` 源码，理解条件装配实战
3. 阅读仓库 `spring/springbootSPI/SpringBootSPI.md`，对照 third/parent 工程跑一遍

---

> **Day 5 完成！** 明天 Day 6：Spring Cloud（Feign、GateWay、Sentinel）— 输出要求：能画出服务调用链路
