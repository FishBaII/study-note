<a name="jGbYo"></a>
### 网关简介
微服务背景下，一个系统被拆分为多个服务，但是像安全认证，流量控制，日志，监控等功能是每个服务都需要的，没有网关的话，我们就需要在每个服务中单独实现，这使得我们做了很多重复的事情并且没有一个全局的视图来统一管理这些功能。<br /> 综上：一般情况下，网关都会提供请求转发、安全认证（身份/权限认证）、流量控制、负载均衡、容灾、日志、监控这些功能。 上面介绍了这么多功能，实际上，网关主要做了一件事情：请求过滤

<a name="MzcRy"></a>
### 常见网关

- **Netflix Zuu：**lZuul 是 Netflix 开发的一款提供动态路由、监控、弹性、安全的网关服务。 Zuul 主要通过过滤器（类似于 AOP）来过滤请求，从而实现网关必备的各种功能。 
- **Spring Cloud Gateway：** SpringCloud Gateway 属于 Spring Cloud 生态系统中的网关，其诞生的目标是为了替代老牌网关 **Zuul **。准确点来说，应该是 Zuul 1.x。SpringCloud Gateway 起步要比 Zuul 2.x 更早。

<a name="WO7OO"></a>
### Gateway特点
Gateway是在Spring生态系统之上构建的API网关服务，基于Spring 5，Spring Boot 2和 Project Reactor等技术。Gateway旨在提供一种简单而有效的方式来对API进行路由，以及提供一些强大的过滤器功能， 例如：熔断、限流、重试等。具有如下特性： 

- 基于Spring Framework 5, Project Reactor 和 Spring Boot 2.0 进行构建； 
- 动态路由：能够匹配任何请求属性； 
- 可以对路由指定 Predicate（断言）和 Filter（过滤器）； 
- 集成Hystrix的断路器功能； 
- 集成 Spring Cloud 服务发现功能； 
- 易于编写的 Predicate（断言）和 Filter（过滤器）； 
- 请求限流功能； 
- 支持路径重写；

![](https://raw.githubusercontent.com/spring-cloud/spring-cloud-gateway/master/docs/src/main/asciidoc/images/spring_cloud_gateway_diagram.png#crop=0&crop=0&crop=1&crop=1&from=url&id=RUaMw&margin=%5Bobject%20Object%5D&originHeight=595&originWidth=443&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
<a name="LTb9b"></a>
### Maven引入

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```


> 与spring-boot-starter-web冲突


<a name="mO8Wk"></a>
### 配置路由
有两种不同德配置路由方法，分别是yml配置和bean配置。
<a name="NqkrB"></a>
#### yml配置
![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606161708-e6f11a73-3219-4a27-b055-a86dbe68ce81.png#averageHue=%232d2c2b&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u53d3413a&margin=%5Bobject%20Object%5D&originHeight=180&originWidth=524&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u9c5f5e07-e743-4798-8790-3efa70849a4&title=)

<a name="pouYi"></a>
#### Java Bean配置
![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606190981-2e680874-9b8a-4657-9803-647281bb82b0.png#averageHue=%23f9f9f8&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u35cacf44&margin=%5Bobject%20Object%5D&originHeight=656&originWidth=1030&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u0ae41652-073f-4954-8bac-677bacdb465&title=)

<a name="GskyZ"></a>
### 常见Route Predicate断言使用
在上面的处理过程中，有一个重要的点就是讲请求和路由进行匹配，这时候就需要用到predicate，它是决定了一个请求走哪一个路由。

```yaml
predicates:
	- After=2019-09-24T16:30:00+08:00[Asia/Shanghai] #指定时间后的请求能访问此路由
	- Before=2019-09-24T16:30:00+08:00[Asia/Shanghai] #指定时间前的请求能访问此路由 
	- Between=2019-09-24T16:30:00+08:00[Asia/Shanghai], 2019-09-25T16:30:00+08:00[Asia/Shanghai] #指定时间段能访问 
	- Cookie=username,123 # cookie 带有username=123的请求可以访问此路由 
  - Header=X-ONE-HEADER, \d+ #包含指定请求头的请求才能匹配，第二个参数为正则表达式 
  - Method=GET #指定请求方式可以匹配 
  - Path=/user/{id} #指定路径可以匹配 
  - Query=username #带有指定参数能匹配，如/list?username=369 
  - RemoteAddr=192.168.1.1 #指定ip发起的调用才能匹配 
  - Weight=group1, 8 # 80%请求会匹配到该路由（假设另一路由断言为group1, 2）
```


<a name="Uqf8V"></a>
### Router Filter
Predict决定了请求由哪一个路由处理，在路由处理之前，需要经过“pre”类型的过滤器处理，处理返回响应之后，可以由“post”类型的过滤器处理。<br />过滤器可以做参数校验、权限校验、流量监控、日志输出、协议转换等<br />路由过滤器可用于修改进入的HTTP请求和返回的HTTP响应，路由过滤器只能指定路由进行使用。Spring Cloud Gateway 内置了多种路由过滤器，他们都由GatewayFilter的工厂类来产生，下面我们介绍下常用路由过滤器的用法。<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606500101-5f4c894b-ba7b-41ae-82f4-f102d161eb92.png#averageHue=%232d2c2b&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=uc91911e4&margin=%5Bobject%20Object%5D&originHeight=219&originWidth=481&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u9c9fb274-8576-4237-a8c7-e14331b9e98&title=)

<a name="lYUbS"></a>
#### 使用方法
```yaml
filters: 
	- AddRequestParameter=username, admin 		#请求/list 相当于请求/list?username=admin
	- AddRequestHeader=X-Request-Foo, Bar	#加上一对请求头，名称为X-Request-Foo，值为Bar
	- StripPrefix=2 	#删除请求路径的前两位（匹配断言之后过滤） 
	- PrefixPath=/user #对匹配请求加上路径前缀/user
	- RewritePath=/foo/(?<segment>.*), /$\{segment}	#配置了RewritePath过滤器工厂，此工厂将/foo/(?.*)重写为{segment}
```



Hystrix GatewayFilter：<br />引入依赖spring-cloud-starter-netflix-hystrix<br />添加服务降级处理类<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606621008-72195bce-5998-4dcf-9dd8-1478f60e8ca3.png#averageHue=%23f7f5f4&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=ue0b5ea56&margin=%5Bobject%20Object%5D&originHeight=369&originWidth=566&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=uc5899a7d-dc7c-4085-a86c-136484b2137&title=)<br />添加配置<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606634005-2ab6f678-5317-4272-aa69-68a266899c63.png#averageHue=%23f7f6f6&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=uc8f853f8&margin=%5Bobject%20Object%5D&originHeight=163&originWidth=394&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=udbb1045e-f1eb-4d66-b75d-9e11e881814&title=)


RequestRateLimiter GatewayFilter：<br />RequestRateLimiter 过滤器可以用于限流，使用RateLimiter实现来确定是否允许当前请求继续进行，如果请求太大默认会返回HTTP 429-太多请求状态。

添加依赖<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606704200-132ba9a7-88f0-4408-b4a8-e2fcc91ee103.png#averageHue=%23f7f6f6&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u6de33400&margin=%5Bobject%20Object%5D&originHeight=151&originWidth=697&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u1df1abe5-3e8c-45ca-a316-09d0a075fc2&title=)<br />添加限流策略的配置类，这里有两种策略一种是根据请求参数中的username进行限流，另一种是根据访问IP进行限流；<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606721626-49039369-c971-466b-98c8-97c12294bf45.png#averageHue=%23f7f6f4&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u4171f9fe&margin=%5Bobject%20Object%5D&originHeight=387&originWidth=933&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u8e7604cf-d035-493d-be87-67edc97d4ef&title=)<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606746157-285c1cb0-7f35-458c-843f-a676787b7c4d.png#averageHue=%23f7f7f7&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u02f5ed9e&margin=%5Bobject%20Object%5D&originHeight=403&originWidth=743&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u434d3dc7-6fb2-46b5-9066-94e9ea942a7&title=)

Retry GatewayFilter：<br />对路由请求进行重试的过滤器，可以根据路由请求返回的HTTP状态码来确定是否进行重试。<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669606788898-85069d1d-44eb-49f3-a1c6-c1e86e659902.png#averageHue=%23f7f7f7&clientId=ud5d54ad1-1e3a-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=uc5712fb0&margin=%5Bobject%20Object%5D&originHeight=327&originWidth=825&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=uf885830b-1487-46b2-b99c-1be854de8af&title=)
> statuses: org.springframework.http.HttpStatus枚举中定义的值或枚举名字


<a name="Jul4u"></a>
#### 自定义过滤器
自定义过滤器需要实现GatewayFilter(全局GlobalFilter)和Ordered 2个接口。写一个RequestTimeFilter，代码如下：
```java
public class RequestTimeFilter implements GatewayFilter, Ordered {

    private static final Log log = LogFactory.getLog(GatewayFilter.class);
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                    if (startTime != null) {
                        log.info(exchange.getRequest().getURI().getRawPath() + ": " + (System.currentTimeMillis() - startTime) + "ms");
                    }
                })
        );

    }

    @Override
    public int getOrder() {
        //值越大则优先级越低
        return 0;
    }
}
```

此处是一个“pre”类型的过滤器，然后再chain.filter的内部类中的run()方法中相当于”post”过滤器

<a name="sXgip"></a>
#### 自定义过滤器工厂
自定义过滤器工厂类可以在配置文件中配置过滤器。<br />过滤器工厂的顶级接口是GatewayFilterFactory，我们可以直接继承它的两个抽象类来简化开发AbstractGatewayFilterFactory和AbstractNameValueGatewayFilterFactory，这两个抽象类的区别就是前者接收一个参数（像StripPrefix和我们创建的这种），后者接收两个参数（像AddResponseHeader）。<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1669692411662-dd1b01a3-4fc1-405b-a5d2-95e122a8a2ed.png#averageHue=%233c3f41&clientId=ua4f7dd8b-80eb-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u346f0d61&margin=%5Bobject%20Object%5D&originHeight=339&originWidth=788&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u1fe2fbaa-aab3-402c-91ac-a5da63e6b27&title=)


```java
public class RequestTimeGatewayFilterFactory extends AbstractGatewayFilterFactory<RequstTimeGatewayFilterFactory.Config> {     
    private static final Log log = LogFactory.getLog(GatewayFilter.class);     
    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";     
    private static final String KEY = "withParams";     
    @Override     
    public List<String> shortcutFieldOrder() {         
        return Arrays.asList(KEY);     
    }     
    public RequestTimeGatewayFilterFactory() {         
        super(Config.class);     
    }     
    @Override     
    public GatewayFilter apply(Config config) {         
        return (exchange, chain) -> {             
            exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());             
            return chain.filter(exchange).then(                     
                Mono.fromRunnable(() -> {                         
                    Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);                         
                    if (startTime != null) {                             
                        StringBuilder sb = new StringBuilder(exchange.getRequest().getURI().getRawPath())                                     
                            .append(": ")                                     
                            .append(System.currentTimeMillis() - startTime)                                     
                            .append("ms");                             
                        if (config.isWithParams()) {                                 
                            sb.append(" params:").append(exchange.getRequest().getQueryParams());                             
                        }                             
                        log.info(sb.toString());                         
                    }                     
                })             
            );         
        };     
    }     
    public static class Config {         
        private boolean withParams;         
        public boolean isWithParams() {             
            return withParams;         
        }         
        public void setWithParams(boolean withParams) {             
            this.withParams = withParams;         
        }     
    } 
} 

```
注册Bean
```java
    @Bean     
    public RequestTimeGatewayFilterFactory elapsedGatewayFilterFactory() {         
        return new RequestTimeGatewayFilterFactory();     
    }
```
<a name="yowj9"></a>
#### global filter
Spring Cloud Gateway根据作用范围划分为GatewayFilter和GlobalFilter，二者区别如下：

- GatewayFilter : 需要通过spring.cloud.routes.filters 配置在具体路由下，只作用在当前路由上或通过spring.cloud.default-filters配置在全局，作用在所有路由上
- GlobalFilter : 全局过滤器，不需要在配置文件中配置，作用在所有的路由上，最终通过GatewayFilterAdapter包装成GatewayFilterChain可识别的过滤器，它为请求业务以及路由的URI转换为真实业务服务的请求地址的核心过滤器，不需要配置，系统初始化时加载，并作用在每个路由上。


<a name="Xo4R6"></a>
### gateway 限流
在高并发的系统中，往往需要在系统中做限流，一方面是为了防止大量的请求使服务器过载，导致服务不可用，另一方面是为了防止网络攻击。<br />常见的限流方式，比如Hystrix适用线程池隔离，超过线程池的负载，走熔断的逻辑。在一般应用服务器中，比如tomcat容器也是通过限制它的线程数来控制并发的；也有通过时间窗口的平均速度来控制流量。常见的限流纬度有比如通过Ip来限流、通过uri来限流、通过用户访问频次来限流。<br />一般限流都是在网关这一层做，比如Nginx、Openresty、kong、zuul、Spring Cloud Gateway等；也可以在应用层通过Aop这种方式去做限流。

<a name="A5AtW"></a>
### 本文参考
[https://www.fangzhipeng.com/springcloud/2018/12/21/sc-f-gatway3.html](https://www.fangzhipeng.com/springcloud/2018/12/21/sc-f-gatway3.html)





