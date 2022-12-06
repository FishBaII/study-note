Feign是一个声明式的伪Http客户端，它使得写Http客户端变得更简单。使用Feign，只需要创建一个接口并注解。它具有可插拔的注解特性，可使用Feign 注解和JAX-RS注解。Feign支持可插拔的编码器和解码器。Feign默认集成了Ribbon，并和Eureka结合，默认实现了负载均衡的效果。<br />简而言之：

- Feign 采用的是基于接口的注解
- Feign 整合了ribbon，具有负载均衡的能力
- 整合了Hystrix，具有熔断的能力

<a name="yH6yR"></a>
### 依赖引入
```xml
<dependency>             
  <groupId>org.springframework.cloud</groupId>             
  <artifactId>spring-cloud-starter-openfeign</artifactId>         
</dependency>
```
<a name="PTSne"></a>
### feign使用
消费者启动类加上@EnableFeignClients注解开启Feign的功能
```java
@SpringBootApplication 
@EnableFeignClients 
public class ServiceFeignApplication {     
    //skip
}
```

提供者代码如下：
```yaml
spring:
  application:
    name: user-feign

#skip
```
```java
@RestContorller
@RequestMapping('user')
public class UserController{
	
	@PostMapping
    public Result saveUserf(@RequestBody User user){
        //skip
    }

    @GetMapping
    public Result getUserByName(@RequestParam(value = "name") String name){
        //skip
    }
    
}
```

消费者创建调用接口，使用@FeignClient(value = "xxx")，来指定调用哪个服务。
```java
@FeignClient(value = "user-service", contextId = "userService1") 
public interface UserService {  
    //相当于访问 user-service/user 返回
    @PostMapping("/user")     
    Result saveUserInFeign(@RequestBody User user); 

    //相当于访问 user-service/user?name={name}
    @GetMapping("/user")
    Result getUserByNameInFeign(@RequestParam(value = "name") String name);
}
```
> 当2个或以上feign接口调用同一个服务，即@FeignClient(value = "xxx")相同，会提示bean冲突错误，此时需要在yml配置添加spring.main.allow-bean-definition-overriding=true且注解添加 **contextId **以区分


<a name="oY99S"></a>
### FallBack
fallbackFactory（类似于断容器）与fallback方法。<br />为@FeignClient属性：<br />fallbackFactory 与 fallback 方法不能同时使用，这个两个方法其实都类似于 Hystrix 的功能，当网络不通时返回默认的配置数据。

fallbackFactory使用（其余代码与上述代码一致）：

配置文件开启hystrix
```yaml
feign:
  hystrix:
    enabled: true #在Feign中开启Hystrix
```

声明失败回调方法
```java
@FeignClient(value = "user-service", fallbackFactory = UserFallbackFactory.class)
```

```java
@Component
public class UserFallbackFactory implements FallbackFactory<UserService> {

    private static final Logger logger = LoggerFactory.getLogger(UserFallbackFactory.class);

    @Override
    public UserService create(Throwable cause) {
        //打印异常信息
        UserFallbackFactory.logger.info("fallback; exception was: {}", cause.toString());
        UserFallbackFactory.logger.info("fallback; reason was: {}", cause.getMessage());
        return new UserService() {
            @Override
            public Result saveUserInFeign(User user) {
                //自定义错误返回
                return null;
            }

            @Override
            public Result getUserByNameInFeign(String name) {
                //自定义错误返回
                return null;
            }
            
        };
    }
}
```

本文参考<br />[https://www.fangzhipeng.com/springcloud/2018/08/03/sc-f3-feign.html](https://www.fangzhipeng.com/springcloud/2018/08/03/sc-f3-feign.html)
