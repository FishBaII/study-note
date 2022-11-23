<a name="Q2UZo"></a>
## 简介

- 基于ORM思想实现的框架都称为JPA框架
- JPA 是一个基于O/R映射的标准规范,只定义标准规则（如注解、接口），不提供实现。
- JPA 的主要实现有Hibernate、EclipseLink 和OpenJPA 等

<a name="iuA6X"></a>
## 引入及配置
<dependency><br />    <groupId>org.springframework.boot</groupId><br />    <artifactId>spring-boot-starter-data-jpa</artifactId><br /></dependency>


spring:<br />  jpa:<br />    hibernate:<br />      ddl-auto: none<br />    show-sql: true  

ddl-auto:create----每次运行该程序，没有表格会新建表格，表内有数据会清空<br />ddl-auto:create-drop----每次程序结束的时候会清空表<br />ddl-auto:update----每次运行程序，没有表格会新建表格，表内有数据不会清空，只会更新<br />ddl-auto:validate----运行程序会校验数据与数据库的字段类型是否相同，不同会报错<br />[

](https://blog.csdn.net/zhangtongpeng/article/details/79609942)

<a name="LTCjB"></a>
## 实体映射类
@Entity<br />@Table(name = "TEST")<br />public class Test implements Serializable {

    private static final long _serialVersionUID _= 1L;

    @Column(name = "USER_ID")<br />    @GeneratedValue(strategy = GenerationType.AUTO)<br />    @Id<br />    private Integer id;<br /> <br />    @Column(name = "Name")<br />    private String uerName;<br /> <br />    @Column(name = "Email", nullable = true, length = 128)<br />    private String email;<br /> <br />    @Column(name = "Age")<br />    private int age;<br /> <br />    @Column(name = "Remark", columnDefinition = "text")<br />    private String remark;<br /> <br />    @Column(name = "Salary1", columnDefinition = "decimal(5,2)")<br />    private double salary1;<br /> <br />    @Column(name = "Salary2", precision = 5, scale = 2)<br />    private double salary2;<br /> <br />    @Column(name = "Salary3", columnDefinition = "decimal(5,2)")<br />    private BigDecimal salary3;<br /> <br />    @Column(name = "Salary4", precision = 5, scale = 2)<br />    private BigDecimal salary4;

}



<a name="v5Ixg"></a>
### 自定义主键增长序列
@Id<br />@GeneratedValue(strategy = GenerationType._SEQUENCE_, generator = "generated")<br />@SequenceGenerator(name = "generated", sequenceName = "SEQ_TYC_TABLE", allocationSize = 1)<br />@Column<br />private Integer id;



<a name="yHvlI"></a>
## Repository层

<a name="hZuJw"></a>
### DQM（定义查询方法）

- 直接通过方法名实现
- 通过@Query手动在方法上定义
- EntityManager创建自定义Query

①<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1654940423950-a39d3560-f253-4544-a8b1-4deba2b02e69.png#clientId=ueeb1e584-bebc-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=uab8cf2cf&margin=%5Bobject%20Object%5D&name=image.png&originHeight=815&originWidth=801&originalType=url&ratio=1&rotation=0&showTitle=false&size=451693&status=done&style=none&taskId=u570fe4d6-4b0d-42f0-8f3e-08025a313d8&title=)<br />②


```java
@Repository
public interface UserRepository extends JpaRepository<JayceUser, Long> {

    @Modifying
    @Transactional
    //可直接使用映射类作为查询结果映射
    @Query(value = "update Test t set t.lastName = ?1, t.email = ?2, t.gender = ?3, t.departmentId = ?4, t.date = ?5 where t.id = ?6")
    void modifyEmployee(String lastName, String email, String gender, Integer departmentId, Date date, Integer id);

    //直接写入sql
    //传入Pageable自动返回分页结果
    @Query(value = "select id, last_name, email, gender, department_id, date from jayce_employee", nativeQuery = true)
    List<JayceEmployee> listInPage(Pageable pageable);

    //根据映射类构造方法，自定义返回查询结果
    @Query(value = "select  new org.ljm.test(t.id, t.lastName, t.gender ) from Test t")
    List<Test> list();
   
}
```


③
<a name="iC7Aw"></a>
##  EntityManager动态SQL
> 可以自定义拼接sql语句


```java
@Autowired
LocalContainerEntityManagerFactoryBean entityManagerFactory;

//hql
//如果遇到数据库内置函数，例如pg：ST_Intersects(‘参数1’，‘参数2’)
//直接写原生语句会报错，需要借助function函数：
//function('ST_Intersects','参数1','参数2')
EntityManager manager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager();
StringBuilder sql = new StringBuilder(300);
sql.append("select new com.ljm.demo.entity.userDTO(u.id, u.name, u.age)  from userDTO u");
List<userDTO> list = manager.createQuery(sql.toString()).getResultList();

//sql
EntityManager manager = entityManagerFactory.getNativeEntityManagerFactory().createEntityManager();
StringBuilder sql = new StringBuilder(300);
sql.append("select id, name, age from user where 1= 1 ");
sql.append("and name = ?1");

//传入参数Test.class使结果转换为对应实体类
Query dataQuery = manager.createNativeQuery(sql.toString(), Test.class);
dataQuery.setParameter(1, "tom");
List<Test> list = dataQuery.getResultList();

//如果不指定实体转换类，可手动解析Object数组
Query dataQuery = manager.createNativeQuery(sql.toString());
List<Object[]> vlist = query.getResultList(); 
DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); 
List<Test> list = new ArrayList<>();
TestRulst result = null;
    for(Object[] v : vlist){  
        //数组顺序以查询字段排序
        result = new TestRulst();  
        result.setId(v[0].toString());  
        
        try {  
            result.setTradedate(df.parse(v[1].toString()));  
        } catch (ParseException e) {  
            e.printStackTrace();  
        }  
        list.add(result);  
    }  

```

```java
使用entityManager
适用于动态sql查询
@Service
@Transactional
public class IncomeService{
 
    /**
     * 实体管理对象
     */
    @PersistenceContext
    EntityManager entityManager;
 
    public Page<IncomeDaily> findIncomeDailysByPage(PageParam pageParam, String cpId, String appId, Date start, Date end, String sp) {
        StringBuilder countSelectSql = new StringBuilder();
        countSelectSql.append("select count(*) from IncomeDaily po where 1=1 ");
 
        StringBuilder selectSql = new StringBuilder();
        selectSql.append("from IncomeDaily po where 1=1 ");
 
        Map<String,Object> params = new HashMap<>();
        StringBuilder whereSql = new StringBuilder();
        if(StringUtils.isNotBlank(cpId)){
            whereSql.append(" and cpId=:cpId ");
            params.put("cpId",cpId);
        }
        if(StringUtils.isNotBlank(appId)){
            whereSql.append(" and appId=:appId ");
            params.put("appId",appId);
        }
        if(StringUtils.isNotBlank(sp)){
            whereSql.append(" and sp=:sp ");
            params.put("sp",sp);
        }
        if (start == null)
        {
            start = DateUtil.getStartOfDate(new Date());
        }
        whereSql.append(" and po.bizDate >= :startTime");
        params.put("startTime", start);
 
        if (end != null)
        {
            whereSql.append(" and po.bizDate <= :endTime");
            params.put("endTime", end);
        }
 
        String countSql = new StringBuilder().append(countSelectSql).append(whereSql).toString();
        Query countQuery = this.entityManager.createQuery(countSql,Long.class);
        this.setParameters(countQuery,params);
        Long count = (Long) countQuery.getSingleResult();
 
        String querySql = new StringBuilder().append(selectSql).append(whereSql).toString();
        Query query = this.entityManager.createQuery(querySql,IncomeDaily.class);
        this.setParameters(query,params);
        if(pageParam != null){ //分页
            query.setFirstResult(pageParam.getStart());
            query.setMaxResults(pageParam.getLength());
        }
 
        List<IncomeDaily> incomeDailyList = query.getResultList();
      if(pageParam != null) { //分页
            Pageable pageable = new PageRequest(pageParam.getPage(), pageParam.getLength());
            Page<IncomeDaily> incomeDailyPage = new PageImpl<IncomeDaily>(incomeDailyList, pageable, count);
            return incomeDailyPage;
        }else{ //不分页
            return new PageImpl<IncomeDaily>(incomeDailyList);
        }
    }
```


<a name="GFrt7"></a>
## 更改JPA表名命名策略
> JPA默认策略是统一将表名改为小写操作，可手动改为自定义配置


<a name="Rs7Wu"></a>
### 方法一：改为JPA其他策略
```properties
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

```
<a name="U99qL"></a>
### 方法二：自定义策略
①创建自定义策略类
```java

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
 
/**
 * 重写 hibernate 对于命名策略中改表名大写为小写的方法
 */
public class MySQLUpperCaseStrategy extends PhysicalNamingStrategyStandardImpl {
 
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
 
        String tableName = name.getText().toUpperCase();
        return name.toIdentifier(tableName);
    }
}
```
②配置引用自定义的策略类
```properties
spring.jpa.hibernate.naming.physical-strategy=org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
```


<a name="NuTSw"></a>
## 多表关联查询

<a name="guxx8"></a>
### 自定义Query
可通过指定构造方法来指定自定义映射类,部分JPA版本不支持此写法，可使用注解法或者NativeQuery查询
```java
 //UserCarResult构造方法
public UserCarResult(Long id, String userName, String name) {

        this.id= id;
        this.userName = userName;
        this.carName = name;
  }   


//Repository层Query,entityManagerFactory同理  
@Query("select new org.example.core.dto.UserCarResult(u.id, u.userName,  c.name) from User u right join UserCar c on c.userId = u.id")
public List<UserCarResult> findWithCar();
```

<a name="cTuJL"></a>
### 映射类注解法
//todo
<a name="ZBz8n"></a>
### <br />
