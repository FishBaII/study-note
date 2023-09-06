

### 测试环境
SpringBoot 2.7.12
JDK8
Junit5


### @SpringBootTest
此注解创建测试中使用的ApplicationContext，可以使用 @SpringBootTest 的 webEnvironment 属性进一步优化测试的运行方式：

* MOCK（默认）：加载 web ApplicationContext 并提供模拟web环境。使用此注解时，嵌入式服务器未启动。如果类路径上没有可用的web环境，则此模式会透明地回退到创建常规的非web ApplicationContext。它可以与 @AutoConfigureMockMvc 或 @AutoConfigureWebTestClient 结合使用，对web应用程序进行基于模拟的测试。
* RANDOM_PORT：加载 WebServerApplicationContext 并提供真正的web环境。嵌入式服务器启动并在随机端口上监听。
* DEFINED_PORT：加载 WebServerApplicationContext 并提供真正的web环境。嵌入式服务器将启动并在定义的端口（从 application.properties）或默认端口8080上监听。
* NONE：使用 SpringApplication 加载 ApplicationContext，但不提供任何web环境（mock或其他）。

### @MockBean

使用此注解注入的类，表明类中的所有方法都使用自定义返回的值，这样在测试的时候就不会真的去调用远程接口，而是返回一个我们预设的值  
默认返回null


```
@MockBean
protected UserService userService;

@Test
public void addComsumptions() {
	
	when(userService.getUserById(anyString())).thenReturn(new User());
	assertNotNull(userService.getUserById("1"));
	assertNull(userService.getUserByName("ljm"));
	
}

```


### @SpyBean

使用此注解注入的类，表明类中的某一个方法使用使用自定义返回的值，在测试时，如果使用到了多个方法，那么只是遵循@SpyBean写法的方法会返回我们自定义的值
默认返回其真实的值


```
@MockBean
protected UserService userService;

@Test
public void addComsumptions() {
	
	when(userService.getUserById(anyString())).thenReturn(null);
	assertNull(userService.getUserById("1"));
	assertNotNull(userService.getUserByName("ljm"));
	
}

```


### mock静态方法

使用Mockito.mockStatic可以对静态static方法进行mock,但必须引入mockito-inline,否则运行时抛出异常org.mockito.exceptions.base.MockitoException并提示你引入mockito-inline
```
		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-inline</artifactId>
			<version>3.8.0</version>
		</dependency>
```

```
	@Test
	void staticMoc(){
		
		DateFormat dateFormat = SimpleDateFormat.getInstance();
		assertEquals("Asia/Shanghai", dateFormat.getTimeZone().getID());
		
		MockedStatic<DateFormat> dateFormatMockedStatic = mockStatic(DateFormat.class);
		dateFormatMockedStatic.when(() -> DateFormat.getInstance()).thenReturn(null);
		DateFormat dateFormatMock = SimpleDateFormat.getInstance();
		assertNull(dateFormatMock);
		dateFormatMockedStatic.close();
		
	}
```

>- 除了可以Mock自己创建的静态方法，还可以Mock jdk自带的工具类静态方法，但部分工具类强制不能mock或者会由于java重载出现java.lang.StackOverflowError错误（System, Objects, Arrays等）
>- 可以使用thenThrow(Class<? extends Throwable> var1)指定抛出异常,但必须在此静态方法里事先声明，否则会抛出MockitoException异常并提示你Checked exception is invalid for this method!


### @SpringBootTest
替代了spring-test中的@ContextConfiguration注解，目的是加载ApplicationContext，启动spring容器
可只指定目标bean加载
@SpringBootTest(class = {SystemConfig.class})
//......
@Autowired SystemConfig config;


### @ContextConfiguration

加载配置文件
@ContextConfiguration(classes = SystemConfig.class) 来指定加载哪个 Spring @Configuration
@ContextConfiguration(locations = {"classpath*:/spring.xml" }) 来指定加载哪个 Spring xml


### OutputCapture
OutputCapture 是一个JUnit扩展，可用于捕获 System.out 和 System.err 输出。  
添加 @ExtendWith(OutputCaptureExtension.class) 并将 CapturedOutput 作为参数注入测试类构造函数或测试方法来使用

```
@ExtendWith(OutputCaptureExtension.class)
public class OutputCaptureTests {

  @Test
  void test(CapturedOutput output) {
    System.out.print("hello world!");
    assertThat(output).isEqualTo("hello world!");
    assertTrue(output.getOut().contains("hello world!"));
  }
}
```

### Test Property
设置自定义的配置信息Property

```
@SpringBootTest()
//优先于bean创建
@TestPropertySource(properties = {
		"env.headerKey=value"
})
class PropertiesTests {
	@Autowired private ConfigurableEnvironment environment;

	@Test
	void envTest(){
		TestPropertyValues.of("env.methodKey=value").applyTo(environment);
		assertEquals("value", environment.getProperty("env.ymlKey"));
		assertEquals("value", environment.getProperty("env.headerKey"));
		assertEquals("value", environment.getProperty("env.methodKey"));
	}
}	
```


### 测试web接口

@AutoConfigureMockMvc: 注入MockMvc 类的 Bean

```
@SpringBootTest
@AutoConfigureMockMvc
class HiControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void getTest() throws Exception {
        mvc.perform(get("/hi")).andExpect(status().isOk()).andExpect(content().string("hi"));
    }
    
    @Test
    void postTest() throws Exception {

        Account account = new Account();
        account.setId(9L);
        account.setNumber("S-000-0");
        MvcResult result = mvc.perform(
                        MockMvcRequestBuilders.request(HttpMethod.POST, "/login")
                                .contentType("application/json").content(JSON.toJSONString(account)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                //.andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                //.andExpect(MockMvcResultMatchers.jsonPath("$.data.reader[0].age").value(18))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(9))
                .andDo(print())
                .andReturn();

    }
}
```

如果需要启动完全运行的服务器，建议使用随机端口。  
如果使用 @SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)，则每次运行测试时都会随机选择一个可用端口。

```
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RandomPortTestRestTemplateTests {
  @Test
  public void test(@Autowired TestRestTemplate restTemplate) {
    String body = restTemplate.getForObject("/", String.class);
    assertThat(body).isEqualTo(ConstantUtil.body);
  }
}
```

#### WebTestClient

WebTestClient 除了可以调用API，还支持流式断言，更加直观和方便输入

```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-webflux</artifactId>
  <scope>test</scope>
</dependency>
```

```
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.Mockito.when;

@WebFluxTest(GreetingController.class)
//@SpringBootTest(properties = "spring.main.web-application-type=reactive")
//@AutoConfigureWebTestClient
public class WebFluxWebMockTest { 
    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private GreetingService service;
    @Test
    @DisplayName("greeting should return message from service")
    void greetingShouldReturnMessageFromService() throws Exception { 
        // mock here
        when(service.greet()).thenReturn("Hello, Mock");
        this.webTestClient
                .get()
                .uri("/greeting")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("Hello, Mock");
    }
}
```

### @WebMvcTest

要测试Spring MVC controllers 是否按预期工作，使用 @WebMvcTest 注解。  
@WebMvcTest 自动配置Spring MVC基础结构，并将扫描的bean限制为@Controller、@ControllerAdvice、@JsonComponent、Converter、GenericConverter、Filter、HandlerInterceptor、WebMVCConfiguer和HandlerMethodArgumentResolver。  
使用此注解时，不扫描常规@Component bean。  

```
@WebMvcTest(TestController.class)
public class WebMvcTests {
  @Autowired private MockMvc mvc;
  @MockBean private TestService testService;

  @Test
  public void test() throws Exception {
    given(this.testService.hello()).willReturn("哈哈");
    this.mvc
        .perform(get("/hello").accept(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk())
        .andExpect(content().string("哈哈"));
  }
}
```

### TestRestTemplate
```
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class RestTemplateTests {

  @Test
  void test() {
    TestRestTemplate template = new TestRestTemplate();
    String responseBody = template.getForObject("http://127.0.0.1:8080/", String.class);
    assertThat(responseBody).isEqualTo("Hello World!");
  }
}
```

### @DataJpaTest
它扫描@Entity类并配置Spring Data JPA repositories。
如果类路径上有可用的嵌入式数据库，它也会配置一个。常规@Component bean不会加载到ApplicationContext中。  
如果希望对真实数据库运行测试，则可以使用 @AutoConfigureTestDatabase(replace=Replace.NONE)
```
    <!-- 内存嵌入式数据库 -->
    <dependency>
      <groupId>org.apache.derby</groupId>
      <artifactId>derby</artifactId>
    </dependency>
    
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

```

```
@DataJpaTest
public class DataJpaTests {
  @Autowired private TestEntityManager entityManager;
  @Autowired private UserRepository userRepository;

  @Test
  public void find() throws Exception {
    this.entityManager.persist(new UserInfo(1, "张", "三"));
    UserInfo user = this.userRepository.findByFirstName("张");
    assertThat(user.getLastName()).isEqualTo("三");
  }
}
```

### @DataRedisTest
可以使用 @DataRedisTest 来测试Redis应用程序。默认情况下，它扫描 @RedisHash 类并配置Spring Data Redis repositories。  
常规@Component bean不会加载到ApplicationContext中。

```
@DataRedisTest
public class DataRedisTests {
  @Autowired StringRedisTemplate stringRedisTemplate;

  @Test
  void test() {
    stringRedisTemplate.opsForValue().set("sex", "girl");
  }

  @Test
  void valueHasSet() {
    assertThat(stringRedisTemplate.opsForValue().get("sex")).isEqualTo("girl");
  }
}
```

### Bean自选加载

* @SpringBootTest(classes = BeanConfig.class) 加载对应Bean
* @ImportAutoConfiguration 添加自动配置
```
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(BeanConfig.class)
public class BeanConfigTest {
    @Autowired
    BeanInBeanConfig bean;
}
```

* @ContextConfiguration(classes=xxx) 来指定加载那个 Spring @Configuration。


### 参考引用
[https://juejin.cn/post/6844904039549779981](https://juejin.cn/post/6844904039549779981)