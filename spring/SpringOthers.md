### ApplicationRunner  
```
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