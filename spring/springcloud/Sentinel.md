
官网文档： [https://sentinelguard.io/zh-cn/docs/quick-start.html](https://sentinelguard.io/zh-cn/docs/quick-start.html)<br />官方Github：[https://github.com/alibaba/Sentinel](https://github.com/alibaba/Sentinel)

<a name="PmpC1"></a>
## 简介
随着微服务的流行，服务和服务之间的稳定性变得越来越重要。Sentinel 是面向分布式、多语言异构化服务架构的流量治理组件，主要以流量为切入点，从流量路由、流量控制、流量整形、熔断降级、系统自适应过载保护、热点流量防护等多个维度来帮助开发者保障微服务的稳定性。

<a name="BpTzK"></a>
## Sentinel 功能和设计理念
<a name="Gtpvm"></a>
### 流量控制
流量控制在网络传输中是一个常用的概念，它用于调整网络包的发送数据。然而，从系统稳定性角度考虑，在处理请求的速度上，也有非常多的讲究。任意时间到来的请求往往是随机不可控的，而系统的处理能力是有限的。我们需要根据系统的处理能力对流量进行控制。Sentinel 作为一个调配器，可以根据需要把随机的请求调整成合适的形状，如下图所示：<br />![](./pic/flow-grade-view.png)<br />流量控制有以下几个角度:

- 资源的调用关系，例如资源的调用链路，资源和资源之间的关系；
- 运行指标，例如 QPS、线程池、系统负载等；
- 控制的效果，例如直接限流、冷启动、排队等。

Sentinel 的设计理念是让您自由选择控制的角度，并进行灵活组合，从而达到想要的效果。
<a name="K0aK8"></a>
### 熔断降级
<a name="jnhlR"></a>
#### 什么是熔断降级
除了流量控制以外，降低调用链路中的不稳定资源也是 Sentinel 的使命之一。由于调用关系的复杂性，如果调用链路中的某个资源出现了不稳定，最终会导致请求发生堆积。这个问题和 [Hystrix](https://github.com/Netflix/Hystrix/wiki#what-problem-does-hystrix-solve) 里面描述的问题是一样的。<br />![](./pic/degrade-view.png)<br />Sentinel 和 Hystrix 的原则是一致的: 当调用链路中某个资源出现不稳定，例如，表现为 timeout，异常比例升高的时候，则对这个资源的调用进行限制，并让请求快速失败，避免影响到其它的资源，最终产生雪崩的效果。
<a name="DatkC"></a>
#### 熔断降级设计理念
在限制的手段上，Sentinel 和 Hystrix 采取了完全不一样的方法。<br />Hystrix 通过[线程池](https://github.com/Netflix/Hystrix/wiki/How-it-Works#benefits-of-thread-pools)的方式，来对依赖(在我们的概念中对应资源)进行了隔离。这样做的好处是资源和资源之间做到了最彻底的隔离。缺点是除了增加了线程切换的成本，还需要预先给各个资源做线程池大小的分配。<br />Sentinel 对这个问题采取了两种手段:

- 通过并发线程数进行限制

和资源池隔离的方法不同，Sentinel 通过限制资源并发线程的数量，来减少不稳定资源对其它资源的影响。这样不但没有线程切换的损耗，也不需要您预先分配线程池的大小。当某个资源出现不稳定的情况下，例如响应时间变长，对资源的直接影响就是会造成线程数的逐步堆积。当线程数在特定资源上堆积到一定的数量之后，对该资源的新请求就会被拒绝。堆积的线程完成任务后才开始继续接收请求。

- 通过响应时间对资源进行降级

除了对并发线程数进行控制以外，Sentinel 还可以通过响应时间来快速降级不稳定的资源。当依赖的资源出现响应时间过长后，所有对该资源的访问都会被直接拒绝，直到过了指定的时间窗口之后才重新恢复。
<a name="tfomn"></a>
### 系统负载保护
Sentinel 同时提供[系统维度的自适应保护能力](https://sentinelguard.io/zh-cn/docs/system-adaptive-protection.html)。防止雪崩，是系统防护中重要的一环。当系统负载较高的时候，如果还持续让请求进入，可能会导致系统崩溃，无法响应。在集群环境下，网络负载均衡会把本应这台机器承载的流量转发到其它的机器上去。如果这个时候其它的机器也处在一个边缘状态的时候，这个增加的流量就会导致这台机器也崩溃，最后导致整个集群不可用。<br />针对这个情况，Sentinel 提供了对应的保护机制，让系统的入口流量和系统的负载达到一个平衡，保证系统在能力范围之内处理最多的请求。


<a name="zBOP8"></a>
## 使用

<a name="VhGIq"></a>
### Sentinel管理台

1. 下载官方发行jar，[点击进入](https://github.com/alibaba/Sentinel/releases)
2. 使用命令启动`java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar `（默认8080为端口号，可自定义）
3. 启动无误可访问ip:{端口号}进入sentinel管理台（如 [http://localhost:8080/](http://localhost:9090/) ）

![](./pic/dashboard-home.png)

### 服务端开发

1. 依赖引入

```xml
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>

```

2. 配置文件

```yaml
#仅展示sentinel相关配置，其余略
spring:
  application:
  	#自定义项目名称
    name: sentinel-service
  cloud:
    sentinel:
      transport:
        port: 8719
        #使用管理台定义的端口
        dashboard: localhost:8080
```

3. 测试接口

```java
@RestController
@RequestMapping("sentinel")
public class SentinelController {
    
    @GetMapping("success")
    public String saySuccess(){
        return "Success";
    }
    
}
```

4. 启动项目后，打开sentinel管理台

![](./pic/dashboard-menu.png)
> sentinel管理台服务列表默认为懒加载，所以需访问至少一次该服务才能在sentinel管理台显示

![](./pic/dashboard-monitor.png)


<a name="PMDq8"></a>
## 流量控制

![](./pic/flow-rule.png)
资源名：API，例如 /success<br />针对来源：<br />阈值类型： QPS（每秒请求数），线程数（同时访问API的线程数量，并发数）<br />单机阈值：设置上一个的具体数量<br />流控模式：直接，关联（当关联的资源达到阈值，限流自身），链路（只记录指定链路的流量，指定资源从入口进来的流量，如果达到阈值进行限流）<br />温控效果：快速失败， Warm Up（根据codeFactor值（默认为3），从阈值/codeFactor，经过预热时长，才达到设置的QPS阈值），排队等待

| **Field** | **说明** | **默认值** |
| --- | --- | --- |
| resource | 资源名，资源名是限流规则的作用对象 |  |
| count | 限流阈值 |  |
| grade | 限流阈值类型，QPS 或线程数模式 | QPS 模式 |
| limitApp | 流控针对的调用来源 | default，代表不区分调用来源 |
| strategy | 调用关系限流策略：直接、链路、关联 | 根据资源本身（直接） |
| controlBehavior | 流控效果（直接拒绝 / 排队等待 / 慢启动模式），不支持按调用关系限流 | 直接拒绝 |

