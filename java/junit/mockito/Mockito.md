

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

