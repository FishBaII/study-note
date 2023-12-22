### ApplicationRunner  

* 启动类参数处理
```java
@SpringBootApplication
public class ThirdApplication implements ApplicationRunner {

    private static Logger log = LogManager.getLogger(ThirdApplication.class);

    @Autowired(required = false)
    private ITestService testService;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            log.error("启动参数不存在。");
//            args = new String[]{"para1", "--spring.profiles.active=dev"};
            System.exit(0);

        }

        log.info("========================================================================");
        log.info("启动参数为：{}。", Arrays.toString(args));
        log.info("========================================================================");

        if (args.length < 2) {
            log.error("启动参数不合法。");
            System.exit(0);
        }
        System.out.println("start project");
        SpringApplication.run(ThirdApplication.class, args);
        System.out.println("end project");
    }

    @Override
    public void run(ApplicationArguments args) {

        String[] as = args.getSourceArgs();
        // 正常任务
        log.info("启动参数11为：{}。", Arrays.toString(as));
        if (testService == null) {
            log.warn("未找到{}服务。", as[0]);
            System.exit(0);
        }
        log.info("Get key name in main:" + testService.getConfig());
        log.info("Get name list in main:" + testService.getNameListFromDB());
        System.exit(1);
    }
}
```

* 根据bean id获取bean

```java
@Service
public class AppService {

    private final ApplicationContext applicationContext;

    public AppService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void outputAny(){
        System.out.println("any!!!");
        //获取id为enc_service的bean，其中id可通过注解声明，@Component("enc_service")
        if(applicationContext.containsBean("enc_service")){
            EncService encService = applicationContext.getBean("enc_service", EncService.class);
            encService.enc();
        }

    }
}
```

* 获取resource配置文件

```
//获取resources下的文件，开发及服务器环境适用
Properties pro = new Properties();
InputStream in = this.getClass().getResourceAsStream("/myConfig/mine.properties")
pro.load(in);
in.close();

//直接通过静态方法获取配置文件的值filePath（对应配置中的jwt.filePath: xxx）
@Component
@ConfigurationProperties(prefix = "jwt)
public class SysConfig{
private static String filePath;
private static String dbPassword;

public void setFilePath(String filePath){
    SysConfig.filePath = filePath;
}

public static String getFilePath(){
    return filePath;
}
//...
}
```