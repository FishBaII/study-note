<a name="hTOnv"></a>
## 简介
dynamic-datasource-spring-boot-starter 是一个基于springboot的快速集成多数据源的启动器。<br />其支持 **Jdk 1.7+, SpringBoot 1.4.x 1.5.x 2.x.x**。

git地址：[https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter](https://gitee.com/baomidou/dynamic-datasource-spring-boot-starter)

原理：继承**DataSource，**维护一个存储多个数据源连接的Map，即可根据业务所需数据源key来访问对应的数据源，因为**DynamicDataSource**只负责数据源连接，并无crud功能，所以对数据库类型并无限制（DataSource支持连接即可）


<a name="ijr5F"></a>
## 使用
有两种配置方式，一是采用固定配置在配置文件中，项目启动自动构建多数据源连接；二是使用dynamic-datasource的API根据需求动态增删数据源（无需重启）；

> maven引入（mybatis-plus,dynamic datasource）


```

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
            <version>3.0.0</version>
        </dependency>
```
<a name="RjwB2"></a>
### ① YML固定配置多数据源连接
> 配置主数据源master和从数据源slave_1


```
spring:
  datasource:
    dynamic:
      primary: master #设置默认的数据源或者数据源组,默认值即为master
      datasource:
        master:
          username: root
          password: root
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://192.000.000.000:3306/onemall?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
#        slave_1:
#          username: root
#          password: root
#          driver-class-name: com.mysql.cj.jdbc.Driver
#          url: jdbc:mysql://192.000.000.001:3306/onemall_slave?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

<a name="iprXV"></a>
### ② API动态增删数据源
> 数据源DTO构建
> DTO的所有属性必须按照此写法（如userName为错误），不然属性无法匹配，创建数据源


```java
public class DataSourceDTO {
    
    //连接池名称，如slave_1
    private String pollName;
    //驱动名称
    private String driverClassName;
    //url
    private String url;
    //sql账户名
    private String username;
    //sql账户密码
    private String password;
    
}
```

> 动态增删数据源API



```java
@Autowired
private DataSource dataSource;

@Autowired
private DataSourceCreator dataSourceCreator;

@GetMapping
@ApiOperation("获取当前所有数据源")
public Set<String> now() {
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    return ds.getCurrentDataSources().keySet();
}

@PostMapping("/add")
@ApiOperation("通用添加数据源（推荐）")
public Set<String> add(@Validated @RequestBody DataSourceDTO dto) {
    DataSourceProperty dataSourceProperty = new DataSourceProperty();
    BeanUtils.copyProperties(dto, dataSourceProperty);
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    DataSource dataSource = dataSourceCreator.createDataSource(dataSourceProperty);
    ds.addDataSource(dto.getPollName(), dataSource);
    return ds.getCurrentDataSources().keySet();
}
@DeleteMapping
@ApiOperation("删除数据源")
public String remove(String name) {
    DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
    ds.removeDataSource(name);
    return "删除成功";
}
//于session设置数据源pollName，使@DS("#session.pollName")从session获取数据源
@GetMapping("user2-list")
public List<User> getSlaveList(HttpServletRequest request){
    request.getSession().setAttribute("pollName", "slave_1");
    return userMapper.selectList(null);
}
```


<a name="BVu0P"></a>
## 切换数据源
> 使用@DS 使用切换数据源，@DS 可以注解在方法上或类上，同时存在就近原则 方法上注解 优先于 类上注解。如无此注解则默认使用master数据源
> 提供使用 **spel动态参数** 解析数据源方案。内置spel，session，header，支持自定义。


```java
@Service
//将当前类的数据源切换至slave数据源
@DS("slave")
public class UserServiceImpl implements UserService {

  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  @Override
  //将当前方法的数据源切换至slave数据源
  @DS("slave_1")
  public List selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }
    
  @Override
   //session{"pollName":"master"}
   //获取当前session中key为pollName的值,即切换数据源至master
  @DS(value = "#session.pollName")
  public List selectByCondition() {
    return  jdbcTemplate.queryForList("select * from user where age >10");
  }
    
  
  
}
```

<a name="L798d"></a>
### 事务
由于多数据源的关系，如有事务方面的需求，必须采用分布式事务解决，可使用dynamic-datasource本身支持的Seata服务进行处理。

