# SpringBoot SPI
Java SPI 主要是应用于厂商自定义组件或插件中。在java.util.ServiceLoader的文档里有比较详细的介绍。  
简单的总结下java SPI机制的思想：我们系统里抽象的各个模块，往往有很多不同的实现方案，比如日志模块、xml解析模块、jdbc模块等方案。面向的对象的设计里，我们一般推荐模块之间基于接口编程，模块之间不对实现类进行硬编码。一旦代码里涉及具体的实现类，就违反了可拔插的原则，如果需要替换一种实现，就需要修改代码。为了实现在模块装配的时候能不在程序里动态指明，这就需要一种服务发现机制。  
Java SPI就是提供这样的一个机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要。


## 初始化

1. **创建一个SpringBoot项目名为third，实现三个功能：获取properties的key值，查询指定Table的数据，一个任意的静态工具方法**

pom.xml：
```
	<!-- 声明打包格式为jar 其余配置略-->
	<packaging>jar</packaging>
    

    <dependencies>
        
		<!-- 引入JPA（orm框架），H2驱动（DB driver）其余配置略-->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

application.properties
```
spring.application.name=third

key.name=third


spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

spring.datasource.username=sa
spring.datasource.password=sa
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.url=jdbc:h2:tcp://localhost/~/test
```

TestServiceImpl：
```
@Service
public class TestServiceImpl implements ITestService {

    private static Logger log = LogManager.getLogger(TestServiceImpl.class);


    @Value("${key.name}")
    private String keyName;

    @Autowired
    LocalContainerEntityManagerFactoryBean entityManagerFactory;


    @Override
    public String getConfig() {

        log.info("This is getConfig");
        log.info("keyName:" + keyName);


        //Get from
        return keyName;
    }


    @Override
    public List<String> getNameListFromDB() {
        log.info("This is getNameListFromDB");
        EntityManager manager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager();
        Query dataQuery = manager.createNativeQuery("select * from TEST");
        List<Object[]> resultList = dataQuery.getResultList();
        List<String> nameList = new ArrayList<>();
        for(Object[] name: resultList){
            log.info("name:" + name[1].toString());
            nameList.add(name[1].toString());
        }

        return nameList;
    }
}
```

TestUtil:
```
public class TestUtil {
	public static String getStr(){
        return "something!";
    }
}
```

2. 项目结构如下

third  
├── com.example.third  
│   ├── util  
│   │   └── TestUtil  
│   ├── service  
│   │   ├── ITestService  
│   │   └── TestServiceImpl  
│   └── ThirdApplication  

3. 启动测试

```
    @Autowired
    private ITestService testService;
	
    @Test
    void test(){
		log.info("Get str in test:" + TestUtil.getStr());
        log.info("Get key name in test:" + testService.getConfig());
        log.info("Get name list in test:" + testService.getNameListFromDB());
    }
```

> 作为SPI如果自身没有独立运行的需求，可以删除启动类和properties配置文件。

## 直接使用third.jar

> 问题：此时工程直接使用third.jar会怎么样呢？
1. 使用mvn install将third工程打包上传到本地仓库（远程仓库则使用deploy命令进行上传）
2. 创建新的SpringBoot工程parent，引用third项目依赖，增加properties配置（参考third配置文件）
```
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>third</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
3. 调用third包里的方法

静态方法正常调用
```
    @Test
    void contextLoads() {
        log.info("Get str in parent:" + TestUtil.getStr());

    }
```

使用third的Bean，报错对应bean找不到
```
    @Autowired
    private ITestService testService;

    @Test
    void contextLoads() {
        log.info("Get str in parent:" + TestUtil.getStr());
        log.info("Get key name in parent:" + testService.getConfig());
        log.info("Get name list in parent:" + testService.getNameListFromDB());

    }
```

> parent工程并没有管理third工程的bean，bean需要扫描注册到parent工程才能使用  
> parent工程中，@SpringBootApplication默认只扫描工程自身的@Bean，@Component，并不会扫描third的这些注解。

## 第三方Bean的注册管理

可以使用如下方法来使third注册bean到parent spring容器

1. Spring Factories机制解法（推荐）

> Spring Factories机制提供了一种解耦容器注入的方式，帮助外部包（独立于spring-boot项目）注册Bean到spring boot项目容器中。

于third工程resources资源处创建META-INF->spring.factories配置文件
```
#parent工程会将EnableAutoConfiguration值注册到spring容器中（多个值以逗号分隔）
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.example.third.service.TestServiceImpl
```

启动单元测试成功
```
    @Autowired
    private ITestService testService;

    @Test
    void contextLoads() {
        log.info("Get str in parent:" + TestUtil.getStr());
        log.info("Get key name in parent:" + testService.getConfig());
        log.info("Get name list in parent:" + testService.getNameListFromDB());

    }
```

> 使用Spring Factories方法，对使用者工程代码无侵入，耦合度低  

2. @ComponentScan注解指定扫描路径  
在parent工程启动类加上注解并指定扫描路径 @ComponentScan(value = "com.example.third")

> 对组件开发者方便（无需额外代码），但对组件使用者繁琐，需要在使用者工程启动类添加注解，有一定耦合度

3. 使用者通过注解方式引入
与第二种方法雷同，也是使用者手动配置扫描路径  

于组件使用者创建配置类作为组件bean入口
```
@Configuration
@ComponentScan("com.example.third.**")
public class ThirdBeanConfig {
}
```

建立一个注解类,使用者通过注解才能发现该配置类
```
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ThirdBeanConfig.class})
public @interface EnableThirdBean {
}
```

当使用者使用我们组件，需要在能被Spring发现到的 Configuration 上使用注解
```
@EnableLogRecordClient
@Configuration
public class TestConfig {
}
```

> SpringBoot的启动注解默认扫描启动类所在包的所有注解，所以只创建ThirdBeanConfig配置类（不需创建注解类）也可实现这个效果










