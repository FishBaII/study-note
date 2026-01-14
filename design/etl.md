# ETL

## 介绍

旨在一个ETL（Extract-Transform-Load）处理系统，根据批次号从一个库查询并insert到另一个库（不同的mysql服务）

>- 如果是即时增量迁移，可以参考Canal中间件（基于mysql日志增量订阅和消费的业务，能够实时同步mysql的数据变化情况到mq，es，db中）

## 设计思路

项目工作流程大致分为3步：查询，映射，插入；主要需求以及解决方案如下：
1. 由于数据量比较大（一批次可能达到几十万条），必须采用多线程的方式
2. 为了保障幂等性，采用upsert（mysql使用ON DUPLICATE KEY UPDATE关键字）
3. 需要记录并返回当前批次的迁移情况（插入成功数量，更新成功数量，失败数量），设置变量（localThread）记录
4. 部分迁移涉及多表关联以及多目标表

采用**Spring Batch**进行批量操作，多Chunk模式可以轻松集成线程池和异常重试操作，使用batch listener对执行情况进行监听和统计, 自带日志表，多step解决多表迁移问题；使用JDBCTemplate拼接批量upsert sql语句，最大化提升性能



### 方案一

Spring Batch job采用多chunk模式，1000条数据为1个chunk，每个chunk分为3步reader，processor，writer对应数据迁移的查询，映射和插入；

```java

@Configuration
public class EarningsHistoricalJobConfig {

    private final JdbcTemplate jdbcTemplate;

    public EarningsHistoricalJobConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public Job earningsHistoricalJob(JobRepository jobRepository, @Qualifier("earningsHistoricalStep") Step earningsHistoricalStep) {
        return new JobBuilder("earningsHistoricalJob", jobRepository)
                //这里是单step，如果多step可以使用.next(step)添加
                .start(earningsHistoricalStep)
                .build();
    }

    @Bean
    public Step earningsHistoricalStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             RepositoryItemReader<RawTbEarningsHistorical> earningsHistoricalReader,
                             ItemWriter<TbEarningsHistorical> earningsHistoricalItemWriter,
                             EarningsHistoricalProcessor earningsHistoricalProcessor,
                             @Qualifier("MIGRATION_EXECUTOR") TaskExecutor migrationExecutor,
                             AjioTbEarningsHistoricalRepository repository ) {
        return new StepBuilder("earningsHistoricalStep", jobRepository)
                //设置chunk大小
                .<RawTbEarningsHistorical, TbEarningsHistorical>chunk(1000, transactionManager)
                //设置reader
                .reader(earningsHistoricalReader)
                //设置processor
                .processor(earningsHistoricalProcessor)
                //设置writer
                .writer(earningsHistoricalItemWriter)
                //设置listener，用于统计迁移结果
                .listener(new GenericCountStepListener(repository))
                //线程池参数
                .taskExecutor(migrationExecutor)
                .faultTolerant()
                //遇到Exception异常会至多重试3次
                .retryLimit(3)
                .retry(Exception.class)
                //Exception异常被抛出超过5次会终止当前job
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }

    //通过batchId查询源表数据，因为使用多chunk，所以查询方法必须为分页查询
    @Bean
    @StepScope
    public RepositoryItemReader<RawTbEarningsHistorical> earningsHistoricalReader(
            RawTbEarningsHistoricalRepository rawTbEarningsHistoricalRepository,
            @Value("#{jobParameters['batchId']}") Long batchId) {
        return new RepositoryItemReaderBuilder<RawTbEarningsHistorical>()
                .name("earningsHistoricalReader")
                .repository(rawTbEarningsHistoricalRepository)
                .methodName("findByIdBatchId")
                .arguments(Collections.singletonList(batchId))
                .pageSize(1000)
                .sorts(new LinkedHashMap<>() {{
                    put("id", Sort.Direction.ASC);
                }})
                .build();
    }

    //将源数据对象映射目标数据对象
    @Bean
    public ItemProcessor<RawTbEarningsHistorical, TbEarningsHistorical> earningsHistoricalProcessor(EarningsHistoricalMapper earningsHistoricalMapper) {
        return earningsHistoricalMapper::toAjioTbEarningsHistorical;
    }

    //拼接sql，进行批量upsert
    @Bean
    public ItemWriter<TbEarningsHistorical> earningsHistoricalItemWriter(JdbcTemplate jdbcTemplate) {
        return new CustomGenericItemWriter<>(
                jdbcTemplate,
                //包含sql语句以及values占位符
                SqlConstants.INSERT_TB_EARNINGS_HISTORICAL,
                //占位符替换方法
                TbEarningsHistorical::extractEarningsHistoricalValues
        );
    }
} 

```