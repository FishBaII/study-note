# Day 4：Spring IoC / AOP 深度复习

> 计划日期：Week 1 Day 4 | 主题：Bean 生命周期、循环依赖、动态代理、事务
> 输出要求：**能口述 Bean 生命周期 10+ 步骤**、能解释三级缓存、能列举 @Transactional 失效场景

---

## 一、IoC 容器核心概念

### 1.1 IoC 与 DI

| 概念 | 含义 | 面试一句话 |
|------|------|-----------|
| **IoC（控制反转）** | 对象的创建、依赖关系由容器管理，而非业务代码 `new` | 把对象的控制权从应用代码交给 Spring 容器 |
| **DI（依赖注入）** | IoC 的实现方式：容器在运行时把依赖「注入」给对象 | 通过构造器 / Setter / 字段注入依赖 |
| **Bean** | 由 Spring 容器管理的对象实例 | 默认单例，作用域可配 `prototype` / `request` 等 |
| **BeanDefinition** | Bean 的元数据（类名、作用域、依赖、初始化方法等） | 容器根据 BD 创建 Bean，而非直接 `new` |

### 1.2 核心容器层次

```
ApplicationContext（应用上下文）
    ├── 继承 BeanFactory（基础容器）
    ├── 额外能力：事件发布、国际化、资源加载、AOP 自动代理
    └── 常用实现：
            ClassPathXmlApplicationContext   （XML 配置，老项目）
            AnnotationConfigApplicationContext（纯注解）
            SpringApplication → AnnotationConfigServletWebServerApplicationContext（Boot）
```

> **面试点**：日常开发几乎都用 `ApplicationContext`；`BeanFactory` 是懒加载，ApplicationContext 在启动时预实例化所有单例 Bean（除非 `@Lazy`）。

### 1.3 Bean 作用域（Scope）

| Scope | 说明 | 典型场景 |
|-------|------|---------|
| `singleton`（默认） | 容器中唯一实例 | Service、Repository |
| `prototype` | 每次 `getBean` 创建新实例 | 有状态对象、每次请求需独立对象 |
| `request` | 每个 HTTP 请求一个实例 | Web 层有状态 Bean |
| `session` | 每个 HTTP Session 一个 | 购物车、用户会话数据 |
| `application` | ServletContext 级别 | 全局 Web 配置 |

---

## 二、Bean 生命周期（面试核心 · 必背 12 步）

> **输出要求**：能按顺序说出以下步骤，并说明 `BeanPostProcessor` 在哪些步骤介入。

### 2.1 完整生命周期流程图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Spring Bean 生命周期（单例 Bean）                      │
└─────────────────────────────────────────────────────────────────────────┘

 ① 实例化 Instantiation
    │  AbstractAutowireCapableBeanFactory#createBeanInstance()
    │  → 反射调用构造器（或工厂方法）创建「原始对象」
    ▼
 ② 属性填充 Populate
    │  populateBean() → 注入 @Autowired / @Value / XML 属性
    ▼
 ③ Aware 回调
    │  若实现了 Aware 接口，依次回调：
    │  BeanNameAware → BeanClassLoaderAware → BeanFactoryAware
    │  ApplicationContextAware（仅 ApplicationContext 容器）
    ▼
 ④ BeanPostProcessor#postProcessBeforeInitialization  ← 所有 BPP 前置处理
    ▼
 ⑤ 初始化 Initialization
    │  a. @PostConstruct（CommonAnnotationBeanPostProcessor）
    │  b. InitializingBean#afterPropertiesSet()
    │  c. 自定义 init-method（@Bean(initMethod="...") / XML init-method）
    ▼
 ⑥ BeanPostProcessor#postProcessAfterInitialization   ← 所有 BPP 后置处理
    │  ★ AOP 代理在此阶段生成（AbstractAutoProxyCreator）
    ▼
 ⑦ Bean 就绪，放入单例池 singletonObjects，可被 getBean 使用
    │
    │  ... 业务运行中 ...
    ▼
 ⑧ 容器关闭 DisposableBean#destroy() / @PreDestroy / destroy-method
```

### 2.2 12 步口述版（背这个）

| 序号 | 阶段 | 关键类/注解 |
|------|------|------------|
| 1 | 解析 BeanDefinition，注册到容器 | `BeanDefinitionRegistry` |
| 2 | **实例化**：调用构造器创建对象 | `createBeanInstance()` |
| 3 | **属性赋值**：依赖注入 | `populateBean()` |
| 4 | **Aware 回调** | `BeanNameAware` 等 |
| 5 | `BeanPostProcessor.postProcessBeforeInitialization` | 如 `@PostConstruct` 的前置逻辑 |
| 6 | **初始化**：`@PostConstruct` | `InitDestroyAnnotationBeanPostProcessor` |
| 7 | **初始化**：`InitializingBean.afterPropertiesSet()` | 实现接口的 Bean |
| 8 | **初始化**：自定义 `init-method` | `@Bean(initMethod)` |
| 9 | `BeanPostProcessor.postProcessAfterInitialization` | **AOP 代理生成** |
| 10 | Bean 放入 **一级缓存** `singletonObjects`，可使用 | — |
| 11 | 容器关闭：`@PreDestroy` | — |
| 12 | `DisposableBean.destroy()` / `destroy-method` | 资源释放 |

### 2.3 BeanPostProcessor vs BeanFactoryPostProcessor

| 接口 | 介入时机 | 典型实现 |
|------|---------|---------|
| `BeanFactoryPostProcessor` | Bean **实例化之前**，修改 BeanDefinition | `PropertySourcesPlaceholderConfigurer`（解析 `${}`） |
| `BeanPostProcessor` | Bean **实例化之后**，初始化前后 | `AutowiredAnnotationBeanPostProcessor`、`CommonAnnotationBeanPostProcessor`、**AOP 代理** |

```java
// 自定义 BPP 示例：打印 Bean 初始化耗时
@Component
public class TimingBeanPostProcessor implements BeanPostProcessor {
    private final Map<String, Long> start = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        start.put(beanName, System.nanoTime());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Long t = start.remove(beanName);
        if (t != null) {
            long ms = (System.nanoTime() - t) / 1_000_000;
            if (ms > 100) {
                log.warn("Bean [{}] init took {}ms", beanName, ms);
            }
        }
        return bean; // 可返回代理对象
    }
}
```

---

## 三、循环依赖与三级缓存

### 3.1 什么是循环依赖？

```java
@Service
public class AService {
    @Autowired
    private BService b;  // A 依赖 B
}

@Service
public class BService {
    @Autowired
    private AService a;  // B 依赖 A → 循环
}
```

> **结论**：Spring **默认能解** 单例 + **字段/Setter 注入** 的循环依赖；**构造器注入** 的循环依赖在启动时直接失败（除非 `@Lazy`）。

### 3.2 三级缓存结构

| 缓存 | Map 名称 | 存储内容 | 何时放入 |
|------|---------|---------|---------|
| **一级** | `singletonObjects` | 完全初始化好的单例 Bean | 初始化完成后 |
| **二级** | `earlySingletonObjects` | 早期暴露的 Bean（可能是半成品） | 从三级取出并去代理后 |
| **三级** | `singletonFactories` | `ObjectFactory` 工厂，用于生成早期引用 | 实例化后、属性填充前 |

```
创建 A 的流程（简化）：

1. 实例化 A（尚未注入 B）→ 将 A 的 ObjectFactory 放入三级缓存
2. 填充 A 的属性 → 需要 B → 去创建 B
3. 实例化 B → B 的工厂放入三级缓存
4. 填充 B 的属性 → 需要 A → 从三级缓存 getObject() 拿到 A 的早期引用（可能是代理）
5. B 完成初始化 → 放入一级缓存
6. 回到 A，注入 B，A 完成初始化 → 放入一级缓存
```

### 3.3 为什么需要三级缓存？（面试高频）

- **二级就够了？** 若 Bean **不需要 AOP 代理**，二级缓存确实够用。
- **三级的作用**：存 `ObjectFactory`，**延迟**决定是否包装代理；只有发生循环依赖、且需要 AOP 时，才通过工厂生成**代理对象**的早期引用，保证注入的是最终代理，而非原始对象。

```java
// DefaultSingletonBeanRegistry#getSingleton（简化逻辑）
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    Object singletonObject = this.singletonObjects.get(beanName);      // ① 一级
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        singletonObject = this.earlySingletonObjects.get(beanName);    // ② 二级
        if (singletonObject == null) {
            ObjectFactory<?> factory = this.singletonFactories.get(beanName); // ③ 三级
            if (factory != null) {
                singletonObject = factory.getObject();  // 可能返回 CGLIB/JDK 代理
                this.earlySingletonObjects.put(beanName, singletonObject);
                this.singletonFactories.remove(beanName);
            }
        }
    }
    return singletonObject;
}
```

### 3.4 无法解决的循环依赖

| 场景 | 原因 | 解决方案 |
|------|------|---------|
| **构造器循环依赖** | 实例化阶段就需要对方，三级缓存尚未建立 | `@Lazy` 延迟注入、改 Setter/字段注入、重构拆分 |
| **prototype 循环依赖** | 每次 new，无法缓存早期引用 | 改为 singleton 或重新设计 |
| **多例 + 单例混合** | prototype 不参与三级缓存 | 避免 prototype 依赖 singleton 再被其依赖 |

---

## 四、依赖注入：@Autowired vs @Resource vs @Inject

| 维度 | `@Autowired` | `@Resource` | `@Inject` (JSR-330) |
|------|-------------|-------------|---------------------|
| 来源 | Spring | JDK (`javax.annotation`) | JDK |
| 默认装配 | **byType** | **byName**（可指定 name） | byType |
| 支持 `@Qualifier` | ✅ | ❌（用 name 属性） | ✅ |
| 构造器/字段/方法 | 均可 | 字段/方法 | 均可 |
| required | `required=false` 可可选 | 默认必须存在 | 类似 |

```java
// 同类型多个 Bean 时
@Autowired
@Qualifier("orderServiceImpl")
private OrderService orderService;

@Resource(name = "orderServiceImpl")
private OrderService orderService2;
```

> **项目建议**：团队统一一种风格；Spring 项目常用 `@Autowired` + `@Qualifier`；与 Java EE 混用时常用 `@Resource`。

---

## 五、AOP 原理深度解析

### 5.1 AOP 核心术语

| 术语 | 含义 |
|------|------|
| **切面 Aspect** | 横切逻辑的模块化，如 `@Aspect` 类 |
| **连接点 Join Point** | 程序执行中的点（方法调用、异常抛出等） |
| **切点 Pointcut** | 匹配哪些连接点，如 `execution(* com.xxx.service.*.*(..))` |
| **通知 Advice** | 切点处执行的逻辑：Before / After / Around / AfterReturning / AfterThrowing |
| **织入 Weaving** | 把切面逻辑应用到目标对象：Spring AOP 为**运行时织入（代理）** |

### 5.2 Spring AOP vs AspectJ

| 维度 | Spring AOP | AspectJ |
|------|-----------|---------|
| 织入时机 | 运行时动态代理 | 编译期 / 类加载期 |
| 代理方式 | JDK / CGLIB | 字节码增强，无代理限制 |
| 切点范围 | 主要是 **方法** | 字段、构造器、静态方法等 |
| 性能 | 略低（反射/代理） | 更高 |
| 使用成本 | 低，与 Spring 集成好 | 需 ajc 编译器或 Load-Time Weaving |

### 5.3 JDK 动态代理 vs CGLIB

| 维度 | JDK 动态代理 | CGLIB |
|------|-------------|-------|
| 要求 | 目标类**必须实现接口** | 可代理**无接口**的类 |
| 原理 | `Proxy.newProxyInstance` + `InvocationHandler` | 继承目标类，生成子类覆盖方法 |
| 限制 | 只能代理接口方法 | 不能代理 `final` 类/方法；内部类需注意 |
| Spring 选择 | 有接口 → 默认 JDK | 无接口 → CGLIB；可强制 `proxyTargetClass=true` |

```
                    调用 client.method()
                           │
                           ▼
              ┌────────────────────────┐
              │   代理对象 Proxy      │
              └───────────┬────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
   JDK Proxy                         CGLIB Subclass
   实现同一接口                        继承目标类
   InvocationHandler.invoke()        MethodInterceptor.intercept()
          │                               │
          └───────────┬───────────────────┘
                      ▼
              执行切面链（拦截器链）
                      ▼
              调用 target 真实方法
```

### 5.4 切面执行顺序

```java
@Aspect
@Order(1)  // 数字越小优先级越高（先执行 Around 的外层）
public class LogAspect { ... }

@Aspect
@Order(2)
public class TransactionAspect { ... }
```

**同一切点多个通知的执行顺序**（简化记忆）：

```
@Around 前半段
  → @Before
  → 目标方法
  → @AfterReturning / @AfterThrowing
  → @After
  → @Around 后半段
```

> `@Order` 控制的是**多个切面之间**的嵌套顺序；同一切面内通知顺序由声明顺序和 `@Order` on advice 决定。

### 5.5 简易 JDK 动态代理手写

```java
public class JdkProxyDemo {
    public static void main(String[] args) {
        UserService target = new UserServiceImpl();
        UserService proxy = (UserService) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            (proxyObj, method, methodArgs) -> {
                System.out.println("[Before] " + method.getName());
                Object result = method.invoke(target, methodArgs);
                System.out.println("[After] " + method.getName());
                return result;
            });
        proxy.findById(1L);
    }
}
```

---

## 六、Spring 事务（@Transactional）

### 6.1 事务传播行为（7 种）

| 传播行为 | 含义 |
|---------|------|
| `REQUIRED`（默认） | 有事务就加入，没有就新建 |
| `REQUIRES_NEW` | 总是新建，挂起当前事务 |
| `NESTED` | 嵌套事务（Savepoint），外层回滚则内层也回滚 |
| `SUPPORTS` | 有就加入，没有就非事务执行 |
| `NOT_SUPPORTED` | 非事务执行，挂起当前事务 |
| `MANDATORY` | 必须在事务内，否则抛异常 |
| `NEVER` | 不能在事务内，否则抛异常 |

### 6.2 事务失效场景（面试必背 ≥ 8 条）

| # | 失效场景 | 原因 |
|---|---------|------|
| 1 | **方法非 public** | Spring AOP 默认不代理 protected/private |
| 2 | **同类内部自调用** | `this.save()` 不走代理，切面不生效 |
| 3 | **异常被 catch 未抛出** | 默认只回滚 **RuntimeException / Error** |
| 4 | **抛受检异常未配置 rollbackFor** | 默认不回滚 `Exception` |
| 5 | **数据库引擎不支持事务** | 如 MyISAM |
| 6 | **未被 Spring 管理** | `new Service()` 而非容器 Bean |
| 7 | **传播行为设置不当** | 如 `NOT_SUPPORTED` |
| 8 | **多数据源未配 @Transactional 指定** | 事务管理器绑错数据源 |

```java
@Service
public class OrderService {
    @Transactional
    public void createOrder() {
        this.updateStock();  // ❌ 自调用，事务不生效
    }

    @Transactional
    public void updateStock() { ... }
}

// ✅ 解决：注入自身代理、拆 Service、或 AopContext.currentProxy()
@Service
public class OrderService {
    @Autowired
    private OrderService self;  // 注入的是代理对象

    public void createOrder() {
        self.updateStock();  // ✅ 走代理
    }
}
```

### 6.3 事务实现原理（一句话）

```
@EnableTransactionManagement
  → TransactionManagementConfigurationSelector
  → 注册 InfrastructureAdvisor（BeanFactoryTransactionAttributeSourceAdvisor）
  → 匹配 @Transactional 方法
  → 通过 AOP 代理包装为 TransactionInterceptor
  → 调用前 TransactionManager.getTransaction()
  → 调用后 commit / rollback
```

---

## 七、面试高频题（带答案）

### Q1：Bean 生命周期有哪些阶段？AOP 在哪个阶段生效？

**答**：见本文 **第二节 12 步**；AOP 代理在 **`postProcessAfterInitialization`** 阶段由 `AbstractAutoProxyCreator` 生成。若 Bean 参与循环依赖，可能提前通过三级缓存暴露**代理的早期引用**。

### Q2：Spring 如何解决循环依赖？为什么需要三级缓存？

**答**：仅 **单例 + 字段/Setter 注入** 可通过三级缓存解决：实例化后先把 `ObjectFactory` 放入三级缓存，属性填充时若依赖对方未完成，则从缓存取早期引用。三级缓存用于在需要 AOP 时，通过工厂**延迟**生成代理对象，保证注入的是最终代理。

### Q3：构造器注入的循环依赖能解决吗？

**答**：默认**不能**。构造器阶段必须拿到完整依赖，此时对方可能尚未实例化。可用 `@Lazy` 注入代理占位，或改为 Setter/字段注入，或重构消除循环。

### Q4：JDK 动态代理和 CGLIB 区别？Spring 如何选择？

**答**：见 **5.3 对比表**。Spring Boot 2.x 起默认 **CGLIB**（`spring.aop.proxy-target-class=true`），即使存在接口也常用 CGLIB，避免接口代理的局限。

### Q5：@Transactional 失效有哪些场景？如何排查？

**答**：见 **6.2 表格**。排查：① 是否走代理（打日志看 `getClass().getName()` 是否含 `$$`）；② 是否自调用；③ 异常类型与 `rollbackFor`；④ 事务管理器与数据源是否匹配。

### Q6：@Autowired 和 @Resource 区别？

**答**：`@Autowired` 默认 **byType**，Spring 提供；`@Resource` 默认 **byName**，JDK 提供。多实现类时前者用 `@Qualifier`，后者用 `name` 属性。

### Q7：BeanFactory 和 ApplicationContext 区别？

**答**：`ApplicationContext` 是 `BeanFactory` 子接口，增加事件、国际化、资源访问；**默认预实例化单例 Bean**（除 `@Lazy`）。生产环境几乎都用 ApplicationContext。

### Q8：Spring AOP 和 AspectJ 有什么区别？

**答**：Spring AOP 运行时代理，主要切方法；AspectJ 编译期/类加载期织入，能力更强。Spring `@Aspect` 默认基于 **Spring AOP（代理）**，不是完整 AspectJ 编译织入。

### Q9：同一个类中 @Transactional 方法调用另一个 @Transactional 方法，传播行为如何？

**答**：若通过 **`this` 自调用**，两个方法的 `@Transactional` **都不生效**（未走代理）。若通过**注入的代理**调用，内层方法按自己的传播属性参与或新建事务。

### Q10：如何强制 Spring 使用 CGLIB？

**答**：`@EnableAspectJAutoProxy(proxyTargetClass = true)` 或配置 `spring.aop.proxy-target-class=true`（Boot 2.x+ 默认 true）。

---

## 八、核心流程图（面试白板必备）

### 8.1 getBean 简化流程

```
getBean(name)
    │
    ├─ 一级缓存 singletonObjects 有？ ──Yes──→ 返回
    │
    ├─ 正在创建且二级/三级有早期引用？ ──Yes──→ 返回早期对象（循环依赖）
    │
    └─ createBean()
           ├─ createBeanInstance()     实例化
           ├─ addSingletonFactory()    放三级缓存
           ├─ populateBean()           属性注入（可能触发依赖 Bean 创建）
           ├─ initializeBean()         初始化 + BPP
           └─ addSingleton()           放入一级缓存，清理二三级
```

### 8.2 @Transactional 调用链

```
Controller
    → OrderService$$SpringCGLIB$$0.createOrder()   // 代理入口
        → TransactionInterceptor.invoke()
            → 开启事务 getTransaction()
            → 调用 target.createOrder()
            → commit / rollback
```

---

## 九、手写代码与实战

### 9.1 实现 BeanPostProcessor 统计慢 Bean

见 **2.3 节** 示例，面试可手写骨架。

### 9.2 验证事务自调用失效

```java
@SpringBootTest
class TransactionSelfInvokeTest {
    @Autowired
    private AccountService accountService;

    @Test
    void selfInvokeRollbackFails() {
        try {
            accountService.transferWithSelfInvoke(); // 内部 this.debit() 无事务
        } catch (Exception ignored) {}
        // 断言：debit 未回滚 → 证明自调用失效
    }
}

@Service
public class AccountService {
    @Transactional
    public void transferWithSelfInvoke() {
        debit();
        throw new RuntimeException("boom");
    }

    @Transactional
    public void debit() {
        // UPDATE balance ...
    }
}
```

### 9.3 练习：判断下列代码能否解决循环依赖

```java
// 场景 A
@Service
public class Alpha {
    public Alpha(Beta beta) {}
}
@Service
public class Beta {
    public Beta(Alpha alpha) {}
}

// 场景 B
@Service
public class Alpha {
    @Autowired private Beta beta;
}
@Service
public class Beta {
    @Autowired private Alpha alpha;
}

// 场景 C
@Service
@Scope("prototype")
public class Alpha {
    @Autowired private Beta beta;
}
@Service
public class Beta {
    @Autowired private Alpha alpha;
}
```

<details>
<summary>答案</summary>

```
场景 A：❌ 启动失败。构造器循环依赖，三级缓存无法介入。
场景 B：✅ 可解决。单例 + 字段注入，经典三级缓存场景。
场景 C：❌ 通常失败。prototype 不参与单例三级缓存；且 Beta 单例依赖 prototype Alpha 每次不同。
```
</details>

---

## 十、易错点 / 坑

| # | 易错点 | 说明 |
|---|--------|------|
| 1 | 把 Bean 生命周期说成只有 5 步 | 面试需说到 BPP、Aware、三种初始化、销毁 |
| 2 | 认为所有循环依赖都能解 | 构造器、prototype 不行 |
| 3 | 认为 `@Transactional` 加在接口上就够 | 注解需在**实现类方法**上（或接口且开启 aspectj 模式），且方法须 public |
| 4 | 混淆 `@Order` 与 `@Priority` | Spring 切面用 `@Order`；`@Priority` 是 JSR-250，部分场景生效 |
| 5 | 以为 final 方法能被 CGLIB 增强 | **final 方法无法被覆盖**，切面不生效 |
| 6 | 混淆 Filter、Interceptor、Aspect | Servlet Filter → Spring MVC HandlerInterceptor → AOP Aspect，层次不同 |
| 7 | `rollbackFor = Exception.class` 遗漏 | 受检异常默认不回滚 |
| 8 | 多切面时事务切面顺序错误 | 事务 `@Order` 通常要比日志切面**更外层**（数值更小），否则日志可能在事务外 |

---

## 十一、对比速查表

### IoC 相关

| 对比项 | 说明 |
|--------|------|
| 工厂模式 vs IoC | 工厂仍由代码主动获取；IoC 由容器主动注入 |
| 懒加载 `@Lazy` | 首次 getBean 才创建；可打破部分循环依赖 |
| `@Component` vs `@Bean` | 前者类路径扫描；后者在 `@Configuration` 中方法显式注册 |

### AOP / 事务相关

| 对比项 | JDK 代理 | CGLIB |
|--------|---------|-------|
| 需要接口 | 是 | 否 |
| 性能 | 略好（简单场景） | 创建子类有开销 |
| final 方法 | 不涉及 | **不能代理** |

---

## 十二、关联笔记（study-note 仓库）

| 主题 | 路径 | 说明 |
|------|------|------|
| Spring Security + JWT | `spring/LoginBySpringSecurity.md` | 认证授权（Week 1 后可串联） |
| JWT 实现 | `spring/JWT.md` | Token 机制 |
| Spring Boot SPI | `spring/springbootSPI/SpringBootSPI.md` | **Day 5** 自动配置预热 |

---

## 十三、今日自测 Checklist

- [ ] 能按顺序口述 Bean 生命周期 **12 步**
- [ ] 能说出 `BeanPostProcessor` 在初始化前后的介入点
- [ ] 能解释 AOP 代理在 **哪个 BPP 阶段** 生成
- [ ] 能画出三级缓存名称及各自存储内容
- [ ] 能解释为什么需要三级缓存（AOP + 循环依赖）
- [ ] 能说出 **3 种** 无法解决的循环依赖场景
- [ ] 能对比 `@Autowired` 与 `@Resource`（默认装配方式）
- [ ] 能对比 JDK 代理与 CGLIB（原理、限制）
- [ ] 能列举 **≥ 8 种** `@Transactional` 失效场景
- [ ] 能解释事务自调用失效原因及 2 种解决方案
- [ ] 能说出 7 种事务传播行为中 `REQUIRED` / `REQUIRES_NEW` / `NESTED` 的区别
- [ ] 能对比 Spring AOP 与 AspectJ
- [ ] 完成本节 **3 道** 循环依赖 / 事务判断练习

---

## 十四、延伸阅读（可选，30 分钟）

1. 阅读 `AbstractAutowireCapableBeanFactory#createBean` 源码（IDE 跳转即可，不必通读）
2. 阅读 `DefaultSingletonBeanRegistry#getSingleton` 理解三级缓存
3. Debug 一个带 `@Transactional` 的 Service，确认运行时代理类名含 `CGLIB`

---

> **Day 4 完成！** 明天 Day 5：Spring Boot（自动配置、Starter、SPI）— 输出要求：能手写一个 Starter
