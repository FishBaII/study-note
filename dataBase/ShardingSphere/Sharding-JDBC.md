## 简介
Sharding-JDBC是ShardingSphere的第一个产品，也是ShardingSphere的前身。 它定位为轻量级Java框架，在Java的JDBC层提供的额外服务。它使用客户端直连数据库，以jar包形式提供服务，无需额外部署和依赖，可理解为增强版的JDBC驱动，完全兼容JDBC和各种ORM框架。

* 适用于任何基于Java的ORM框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template或直接使用JDBC。
* 基于任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP等。
* 支持任意实现JDBC规范的数据库。目前支持MySQL，Oracle，SQLServer和PostgreSQL。

![](./img/Sharding-JDBC-info.png)

官网链接：[https://shardingsphere.apache.org/document/current/en/overview/](https://shardingsphere.apache.org/document/current/en/overview/)

核心功能如下：

* 数据分片
* 读写分离
* 强制路由
* 数据治理
* 分布式事务

--- 
### 数据分片

传统的将数据集中存储至单一数据节点的解决方案，在性能、可用性和运维成本这三方面已经难于满足互联网的海量数据场景。

从性能方面来说，由于关系型数据库大多采用B+树类型的索引，在数据量超过阈值的情况下，索引深度的增加也将使得磁盘访问的IO次数增加，进而导致查询性能的下降；同时，高并发访问请求也使得集中式数据库成为系统的最大瓶颈。

从可用性的方面来讲，服务化的无状态型，能够达到较小成本的随意扩容，这必然导致系统的最终压力都落在数据库之上。而单一的数据节点，或者简单的主从架构，已经越来越难以承担。数据库的可用性，已成为整个系统的关键。

从运维成本方面考虑，当一个数据库实例中的数据达到阈值以上，对于DBA的运维压力就会增大。数据备份和恢复的时间成本都将随着数据量的大小而愈发不可控。一般来讲，单一数据库实例的数据的阈值在1TB之内，是比较合理的范围。

在传统的关系型数据库无法满足互联网场景需要的情况下，将数据存储至原生支持分布式的NoSQL的尝试越来越多。 但NoSQL对SQL的不兼容性以及生态圈的不完善，使得它们在与关系型数据库的博弈中始终无法完成致命一击，而关系型数据库的地位却依然不可撼动。

数据分片指按照某个维度将存放在单一数据库中的数据分散地存放至多个数据库或表中以达到提升性能瓶颈以及可用性的效果。 数据分片的有效手段是对关系型数据库进行分库和分表。分库和分表均可以有效的避免由数据量超过可承受阈值而产生的查询瓶颈。 除此之外，分库还能够用于有效的分散对数据库单点的访问量；分表虽然无法缓解数据库压力，但却能够提供尽量将分布式事务转化为本地事务的可能，一旦涉及到跨库的更新操作，分布式事务往往会使问题变得复杂。 使用多主多从的分片方式，可以有效的避免数据单点，从而提升数据架构的可用性。

通过分库和分表进行数据的拆分来使得各个表的数据量保持在阈值以下，以及对流量进行疏导应对高访问量，是应对高并发和海量数据系统的有效手段。 数据分片的拆分方式又分为垂直分片和水平分片。

垂直拆分：按照业务拆分的方式称为垂直分片，又称为纵向拆分，它的核心理念是专库专用。 在拆分之前，一个数据库由多个数据表构成，每个表对应着不同的业务。而拆分之后，则是按照业务将表进行归类，分布到不同的数据库中，从而将压力分散至不同的数据库。 下图展示了根据业务需要，将用户表和订单表垂直分片到不同的数据库的方案。

![](./img/Sharding-JDBC-vertical.png)

水平拆分：水平分片又称为横向拆分。 相对于垂直分片，它不再将数据根据业务逻辑分类，而是通过某个字段（或某几个字段），根据某种规则将数据分散至多个库或表中，每个分片仅包含数据的一部分。 例如：根据主键分片，偶数主键的记录放入0库（或表），奇数主键的记录放入1库（或表），如下图所示。

![](./img/Sharding-JDBC-Horizontal.png)

### 读写分离

面对日益增加的系统访问量，数据库的吞吐量面临着巨大瓶颈。 对于同一时刻有大量并发读操作和较少写操作类型的应用系统来说，将数据库拆分为主库和从库，主库负责处理事务性的增删改操作，从库负责处理查询操作，能够有效的避免由数据更新导致的行锁，使得整个系统的查询性能得到极大的改善。

通过一主多从的配置方式，可以将查询请求均匀的分散到多个数据副本，能够进一步的提升系统的处理能力。 使用多主多从的方式，不但能够提升系统的吞吐量，还能够提升系统的可用性，可以达到在任何一个数据库宕机，甚至磁盘物理损坏的情况下仍然不影响系统的正常运行。

与将数据根据分片键打散至各个数据节点的水平分片不同，读写分离则是根据SQL语义的分析，将读操作和写操作分别路由至主库与从库。

![](./img/Sharding-JDBC-Read-Write-info.png)

### 数据治理

提供注册中心、配置动态化、数据库熔断禁用、调用链路等治理能力。

* 配置集中化：越来越多的运行时实例，使得散落的配置难于管理，配置不同步导致的问题十分严重。将配置集中于配置中心，可以更加有效进行管理。

* 配置动态化：配置修改后的分发，是配置中心可以提供的另一个重要能力。它可支持数据源、表与分片及读写分离策略的动态切换。

### 分布式事务

数据库事务需要满足ACID（原子性、一致性、隔离性、持久性）四个特性。

* 原子性（Atomicity）指事务作为整体来执行，要么全部执行，要么全不执行。
* 一致性（Consistency）指事务应确保数据从一个一致的状态转变为另一个一致的状态。
* 隔离性（Isolation）指多个事务并发执行时，一个事务的执行不应影响其他事务的执行。
* 持久性（Durability）指已提交的事务修改数据会被持久保存。
在单一数据节点中，事务仅限于对单一数据库资源的访问控制，称之为本地事务。几乎所有的成熟的关系型数据库都提供了对本地事务的原生支持。 但是在基于微服务的分布式应用环境下，越来越多的应用场景要求对多个服务的访问及其相对应的多个数据库资源能纳入到同一个事务当中，分布式事务应运而生。

关系型数据库虽然对本地事务提供了完美的ACID原生支持。 但在分布式的场景下，它却成为系统性能的桎梏。如何让数据库在分布式场景下满足ACID的特性或找寻相应的替代方案，是分布式事务的重点工作。

#### 本地事务

在不开启任何分布式事务管理器的前提下，让每个数据节点各自管理自己的事务。 
它们之间没有协调以及通信的能力，也并不互相知晓其他数据节点事务的成功与否。 
本地事务在性能方面无任何损耗，但在强一致性以及最终一致性方面则力不从心。

#### XA强一致事务

XA协议最早的分布式事务模型是由X/Open国际联盟提出的X/Open Distributed Transaction Processing（DTP）模型，简称XA协议。

基于XA协议实现的分布式事务对业务侵入很小。 它最大的优势就是对使用方透明，用户可以像使用本地事务一样使用基于XA协议的分布式事务。 XA协议能够严格保障事务ACID特性。

严格保障事务ACID特性是一把双刃剑。 事务执行在过程中需要将所需资源全部锁定，它更加适用于执行时间确定的短事务。 对于长事务来说，整个事务进行期间对数据的独占，将导致对热点数据依赖的业务系统并发性能衰退明显。 因此，在高并发的性能至上场景中，基于XA协议的分布式事务并不是最佳选择。

#### 柔性事务
如果将实现了ACID的事务要素的事务称为刚性事务的话，那么基于BASE事务要素的事务则称为柔性事务。 BASE是基本可用、柔性状态和最终一致性这三个要素的缩写。

* 基本可用（Basically Available）保证分布式事务参与方不一定同时在线。
* 柔性状态（Soft state）则允许系统状态更新有一定的延时，这个延时对客户来说不一定能够察觉。
* 而最终一致性（Eventually consistent）通常是通过消息可达的方式保证系统的最终一致性。

在ACID事务中对隔离性的要求很高，在事务执行过程中，必须将所有的资源锁定。 柔性事务的理念则是通过业务逻辑将互斥锁操作从资源层面上移至业务层面。通过放宽对强一致性要求，来换取系统吞吐量的提升。
基于ACID的强一致性事务和基于BASE的最终一致性事务都不是银弹，只有在最适合的场景中才能发挥它们的最大长处。可通过下表详细对比它们之间的区别，以帮助开发者进行技术选型。

---
## 项目使用

### 开发环境与依赖引入

| Framework  | Version |
|------------|---------|
| JDK        | 17      |
| SpringBoot | 3.0.0   |
| maven      | 3.6     |

**shardingsphere-JDBC版本选择**

_旧版本shardingsphere-JDBC V4.1.1_ 支持Java，YAML，Spring命名空间和Spring Boot Starter（SpringBoot3.x以下）四种方式配置，开发者可根据场景选择其中一种适合的配置方式
```xml

<dependencies>
    <!-- for yaml-->
<!--    <dependency>-->
<!--        <groupId>org.apache.shardingsphere</groupId>-->
<!--        <artifactId>sharding-jdbc-core</artifactId>-->
<!--        <version>4.1.1</version>-->
<!--    </dependency>-->
    <!-- for spring boot 2.x-->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
        <version>4.1.1</version>
    </dependency>
</dependency>

```


_shardingsphere-JDBC V5.2.1_, 这是shardingsphere-JDBC最后一个支持SpringBoot starter配置的版本，同时也支持SpringBoot3.0.0，但后续版本因为spring更新频繁，维护shardingsphere-JDBC-spring-boot-starter工作量大等原因而放弃此分支
```xml
<dependencies>
    <!-- for yaml-->
<!--    <dependency>-->
<!--        <groupId>org.apache.shardingsphere</groupId>-->
<!--        <artifactId>shardingsphere-jdbc-core</artifactId>-->
<!--        <version>5.2.1</version>-->
<!--    </dependency>-->
    <!-- for spring boot 3.0.0-->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
        <version>5.2.1</version>
    </dependency>
</dependency>
```
_shardingsphere-JDBC V5.4.1_, 这是当前最新版本，仅支持yaml配置(与springboot-starter配置差别不大)，也是本文采用的版本号，如需旧版本lib配置方法，可以参考官网旧版本文档

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core</artifactId>
        <version>5.4.1</version>
    </dependency>
    
    <dependency>
        <groupId>org.glassfish.jaxb</groupId>
        <artifactId>jaxb-runtime</artifactId>
        <version>2.3.8</version>
    </dependency>
</dependency>
```

> 引入jaxb-runtime的原因：JDK9对package进行了修改，将javax包重命名为jakarta并删除了一些API，所以在本文环境JDK17下直接运行会提示异常*javax.xml.bind.JAXBException: Implementation of JAXB-API has not been found on module path or classpath.* 
> 
> issue: [https://github.com/apache/shardingsphere/issues/26041](https://github.com/apache/shardingsphere/issues/26041) 
> 
> 需要注意的是，因为在SpringBoot3.2.x中，默认使用snakeyaml2.2（详情可参考上面issue讨论），因而此版本仍不支持SpringBoot3.2.x以上，建议等待shardingsphere-jdbc更新或者降低SpringBoot版本为3.0.x




### 数据分片



配置信息

```properties
sharding.jdbc.datasource.names=ds0,ds1

sharding.jdbc.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
sharding.jdbc.datasource.ds0.username=root
sharding.jdbc.datasource.ds0.password=

sharding.jdbc.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
sharding.jdbc.datasource.ds1.username=root
sharding.jdbc.datasource.ds1.password=

sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression=ds$->{user_id % 2}

sharding.jdbc.config.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..1}
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order.table-strategy.inline.algorithm-expression=t_order$->{order_id % 2}

sharding.jdbc.config.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..1}
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.sharding-column=order_id
sharding.jdbc.config.sharding.tables.t_order_item.table-strategy.inline.algorithm-expression=t_order_item$->{order_id % 2}
```

```properties
sharding.jdbc.datasource.names= #数据源名称，多数据源以逗号分隔

sharding.jdbc.datasource.<data-source-name>.type= #数据库连接池类名称
sharding.jdbc.datasource.<data-source-name>.driver-class-name= #数据库驱动类名
sharding.jdbc.datasource.<data-source-name>.url= #数据库url连接
sharding.jdbc.datasource.<data-source-name>.username= #数据库用户名
sharding.jdbc.datasource.<data-source-name>.password= #数据库密码
sharding.jdbc.datasource.<data-source-name>.xxx= #数据库连接池的其它属性

sharding.jdbc.config.sharding.tables.<logic-table-name>.actual-data-nodes= #由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况

#分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一

#用于单分片键的标准分片场景
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.sharding-column= #分片列名称
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.precise-algorithm-class-name= #精确分片算法类名称，用于=和IN。该类需实现PreciseShardingAlgorithm接口并提供无参数的构造器
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.standard.range-algorithm-class-name= #范围分片算法类名称，用于BETWEEN，可选。该类需实现RangeShardingAlgorithm接口并提供无参数的构造器

#用于多分片键的复合分片场景
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.sharding-columns= #分片列名称，多个列以逗号分隔
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.complex.algorithm-class-name= #复合分片算法类名称。该类需实现ComplexKeysShardingAlgorithm接口并提供无参数的构造器

#行表达式分片策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.sharding-column= #分片列名称
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.inline.algorithm-expression= #分片算法行表达式，需符合groovy语法

#Hint分片策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.database-strategy.hint.algorithm-class-name= #Hint分片算法类名称。该类需实现HintShardingAlgorithm接口并提供无参数的构造器

#分表策略，同分库策略
sharding.jdbc.config.sharding.tables.<logic-table-name>.table-strategy.xxx= #省略

sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-column-name= #自增列名称，缺省表示不使用自增主键生成器
sharding.jdbc.config.sharding.tables.<logic-table-name>.key-generator-class-name= #自增列值生成器类名称，缺省表示使用默认自增列值生成器。该类需提供无参数的构造器

sharding.jdbc.config.sharding.tables.<logic-table-name>.logic-index= #逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表

sharding.jdbc.config.sharding.binding-tables[0]= #绑定表规则列表
sharding.jdbc.config.sharding.binding-tables[1]= #绑定表规则列表
sharding.jdbc.config.sharding.binding-tables[x]= #绑定表规则列表

sharding.jdbc.config.sharding.broadcast-tables[0]= #广播表规则列表
sharding.jdbc.config.sharding.broadcast-tables[1]= #广播表规则列表
sharding.jdbc.config.sharding.broadcast-tables[x]= #广播表规则列表

sharding.jdbc.config.sharding.default-data-source-name= #未配置分片规则的表将通过默认数据源定位
sharding.jdbc.config.sharding.default-database-strategy.xxx= #默认数据库分片策略，同分库策略
sharding.jdbc.config.sharding.default-table-strategy.xxx= #默认表分片策略，同分表策略
sharding.jdbc.config.sharding.default-key-generator-class-name= #默认自增列值生成器类名称，缺省使用io.shardingsphere.core.keygen.DefaultKeyGenerator。该类需实现KeyGenerator接口并提供无参数的构造器

sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.master-data-source-name= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[0]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[1]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.slave-data-source-names[x]= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-class-name= #详见读写分离部分
sharding.jdbc.config.sharding.master-slave-rules.<master-slave-data-source-name>.load-balance-algorithm-type= #详见读写分离部分
sharding.jdbc.config.config.map.key1= #详见读写分离部分
sharding.jdbc.config.config.map.key2= #详见读写分离部分
sharding.jdbc.config.config.map.keyx= #详见读写分离部分

sharding.jdbc.config.props.sql.show= #是否开启SQL显示，默认值: false
sharding.jdbc.config.props.executor.size= #工作线程数量，默认值: CPU核数

sharding.jdbc.config.config.map.key1= #用户自定义配置
sharding.jdbc.config.config.map.key2= #用户自定义配置
sharding.jdbc.config.config.map.keyx= #用户自定义配置
```