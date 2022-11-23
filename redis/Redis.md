<a name="ZVAIj"></a>
## 简介
Redis 是一个使用 C 语言开发的数据库，不过与传统数据库不同的是 **Redis 的数据是存在内存中的** ，也就是它是内存数据库，所以读写速度非常快，因此 Redis 被广泛应用于缓存方向。<br />另外，Redis 除了做缓存之外，也经常用来做分布式锁，甚至是消息队列。<br />Redis 提供了多种数据类型来支持不同的业务场景。Redis 还支持事务 、持久化、Lua 脚本、多种集群方案。

> **GitHub地址：**[https://github.com/redis/redis](https://github.com/redis/redis)
> **官网下载地址**：[https://redis.io/download/](https://redis.io/download/)
> **windows版本下载地址：**[https://github.com/tporadowski/redis/releases/tag/v5.0.14.1](https://github.com/tporadowski/redis/releases/tag/v5.0.14.1)
> **可视化客户端QuickRedis：**[https://gitee.com/quick123official/quick_redis_blog/](https://gitee.com/quick123official/quick_redis_blog/)




<a name="skFrG"></a>
## 安装

<a name="f0xcI"></a>
### Windows版本

<a name="Lx7bD"></a>
#### 下载windows版本Redis并解压

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656341086767-0f7a26e9-bdf7-430d-be5f-37379b4e6f04.png#clientId=u6642f5cb-844f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=524&id=ue30a26d4&margin=%5Bobject%20Object%5D&name=image.png&originHeight=524&originWidth=645&originalType=binary&ratio=1&rotation=0&showTitle=false&size=53927&status=done&style=none&taskId=ue8de1c03-2d90-4eb7-919b-dc1e6416fd2&title=&width=645)

<a name="yXKiE"></a>
#### 修改配置（可选）
> 商用必须根据实际需求进行配置修改或者集群部署，个人学习可跳过这一步。

redis目录打开redis.windows.conf

删除 # 符号，在requirepass后输入即可设置redis登录密码（不设置则默认无密码）<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656341350288-be3262dc-22f8-43c1-b217-f8d348bd8a71.png#clientId=u6642f5cb-844f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=493&id=u48263bb3&margin=%5Bobject%20Object%5D&name=image.png&originHeight=493&originWidth=344&originalType=binary&ratio=1&rotation=0&showTitle=false&size=27282&status=done&style=none&taskId=u521eb9f9-b606-4f8b-97c6-2e05b3b39ed&title=&width=344)

默认端口6379<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656341633133-bfbd036e-5ec7-40a7-9043-43a660e19328.png#clientId=u6642f5cb-844f-4&crop=0.0379&crop=0.0507&crop=1&crop=1&from=paste&height=266&id=ue09061c0&margin=%5Bobject%20Object%5D&name=image.png&originHeight=276&originWidth=369&originalType=binary&ratio=1&rotation=0&showTitle=false&size=16486&status=done&style=none&taskId=u2b34df3b-e898-4669-a973-ebd29b0c8e8&title=&width=355)<br />其余配置解释：<br />1）daemonize：是否以守护进程的方式运行，默认为no，可以通过修改为yes启动守护进程<br />2）当daemonize为yes时，redis默认会将pid写入/var/run/redis.pid文件，可以通过pidfile指定<br />pidfile /var/run/redis.pin<br />3）redis默认监听端口为6379，可以通过port指定<br />port 6379<br />4）绑定的主机地址<br />bind 127.0.0.1<br />5）设置当客户端限制多长时间后关闭连接，如果指定为0，表示关闭该功能<br />timeout 300<br />6）指定日志记录级别：redis共支持4个级别：debug，verbose，notice，warning 默认为notice<br />loglevel verbose<br />7）日志记录方式，默认为标准输出。如果配置redis以守护进程方式运行，而这里有配置为日志记录方式为标准输出，则日志将会发送给/dev/null<br />logfile stdout<br />8）设置数据库的数量，默认是0，可以通过select dbid 命令在连接上zh数据库id<br />database 16<br />9）指定在多长时间内，有多少次更新操作就将数据同步到数据文件，可多个条件配合<br />save <seconds> <changes><br />redis默认配置中提供了三个条件：<br />save 900 1<br />save 300 10<br />save 60 10000<br />分别表示900秒内一个更改，300秒内10个更改，60秒内10000个更改<br />10）指定存储到本地数据库时，是否压缩数据，默认为yes。redis采用LZF压缩，如果为了节省CPU时间，可以关闭该压缩，但是会导致数据库文件变得巨大。<br />rdbcompression yes<br />11）指定本地数据库文件名，默认为dump.rdb<br />dbfilename dump.rdb<br />12）指定本地数据库存放目录<br />dir ./<br />13）设置当本机为slav服务时，设置master服务的IP地址及端口，在redis启动时，它会自动从master进行数据同步<br />slaveof <masterip> <masterport><br />14）当master服务设置了密码保护时，slav服务连接master的密码<br />masterauth <master-password><br />15）设置连接密码。如果设置了密码，客户端在连接redis时，需要通过auth <password> 命令提供密码，默认关闭。<br />requirepass foobared<br />16）设置同一时间客户端最大连接数，默认无限制。如果设置maxclients为0，表示不限制。<br />maxclients 128<br />17）指定redis最大内存限制，redis在启动时，会将数据加载到内存中，达到最大内存后，redis会先尝试清除已到期或即将到期的key，清除后，若内存还是不足，将无法再写入，但仍可读取。<br />redis新的vm机制，会把key放在内存，value放在swap区<br />maxmemory <bytes><br />18）指定是否在每次更新操作后进行日志记录，默认为no。redis默认是异步写入磁盘，如果未开启，可能会导致断点时部分一段时间内的数据丢失，因为redis本身同步数据是按照上面save条件来同步的，所有数据会在一段时间内只存在于内存中。<br />appendonly no<br />19）指定更新日志文件名，默认为appendonly.aof<br />appendfilename appendonly.aof<br />20）指定更新日志条件，共三个可选值：<br />no：表示等操作系统进行数据缓存同步到磁盘（快）<br />always：表示每次更新操作后，手动调用fsync()将数据写入磁盘（慢，但安全）<br />everysec：表示美秒同步一次（折中，默认值）<br />appendfsync everysec<br />21）指定是否启用虚拟机内存，默认为no。<br />vm机制将数据分页存放，由redis将访问量较少的页即冷数据swap到磁盘上，访问多的页由磁盘自动换出到内存中。<br />vm-enabled no<br />22）虚拟内存文件路径，默认值为/tmp/redis.swap，不可多个redis实例共享<br />vm-swap-file /tmp/redis.swap<br />23）将所有大于vm-max-memory的数据存入虚拟内存，无论vm-max-memory设置的多小，所有索引数据都是内存存储的（redis的索引数据，就是keys）。<br />也就是说，当vm-max-memory设置为0时，其实所有value都存在于磁盘，默认值为0<br />vm-max-memory 0<br />24）redis swap文件分成了很多的page，一个对象可以保存在多个page上面，但一个page不能被多个对象共享，vm-page-size是要根据存储的数据大小来设定的。<br />如果存储很多小对象，page大小最好设置为32或者64bytes，如果存储很大的对象就可以使用更大的page，如果不确定，可以直接使用默认值即可<br />vm-page-size 32<br />25）设置swap文件中的page的数量，由于页表（一种表示页面空闲或使用的bitmap）是放在内存中的，在磁盘中每8个page将消耗1byte的内存。<br />vm-pages 134217728<br />26）设置访问swap文件的线程数。最好不要超过机器的核数，如果设置为0，那么所有对swap的操作都是串行的。可能会造成比较长时间的延迟。默认值为4<br />vm-max-threads 4<br />27）设置在向客户端应答时，是否把较小的包合并为一个包发送，默认开启<br />glueoutputbuf yes<br />28）指定在超过一定数量或者最大元素超过某一临界值时，采用一种特殊的hash算法<br />hash-max-zipmap-entries 64<br />hash-max-zipmap-value 512<br />29）指定是否激活重置hash，默认为开启。<br />activerehashing yes<br />30）指定包含其他的配置文件，可以在同一主机上，多个redis实例之间使用同一份配置文件。而同时各个实例又拥有各自特定的配置文件。<br />include /path/to/local.conf

<a name="lM8R8"></a>
#### 启动Redis服务

- **常驻服务安装**

安装服务：redis-server.exe --service-install redis.windows.conf --service-name  [服务名]<br />卸载服务：redis-server.exe --service-uninstall --service-name [服务名]

使用cmd进入redis解压目录，输入安装服务命令（可选择指定的配置文件与自定义服务名）：redis-server.exe --service-install redis.windows.conf --service-name redis-server<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656344220326-9b070cf8-7fc5-40a8-a500-0432e532dbaf.png#clientId=u6642f5cb-844f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=208&id=ub2e7b627&margin=%5Bobject%20Object%5D&name=image.png&originHeight=208&originWidth=742&originalType=binary&ratio=1&rotation=0&showTitle=false&size=19564&status=done&style=none&taskId=uc9973f70-fdf3-4ede-949c-c90f18fc270&title=&width=742)

- **窗口服务安装**

使用cmd进入redis解压目录，输入服务启动命令：redis-server.exe redis.windows.conf
> 窗口关闭随即会关闭服务

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656424378604-d26a0209-28a8-4f6b-9d24-d8473b215516.png#clientId=ua26c6438-8573-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=418&id=u4662e00a&margin=%5Bobject%20Object%5D&name=image.png&originHeight=418&originWidth=918&originalType=binary&ratio=1&rotation=0&showTitle=false&size=16122&status=done&style=none&taskId=uf6d45008-7ce5-4c7f-847e-1bce046998c&title=&width=918)

<a name="x2M3F"></a>
#### Redis客户端启动
> Redis服务端启动后，无法直接对Redis库进行操作，必须通过客户端进行操作，如使用Redis支持的语言进行客户端创建（如下图），或者直接使用Redis内置的客户端进行访问与操作

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656424698907-29401cb0-7703-45c7-bb4d-bc700431b3fc.png#clientId=ua26c6438-8573-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=356&id=u8c9b82e8&margin=%5Bobject%20Object%5D&name=image.png&originHeight=356&originWidth=1041&originalType=binary&ratio=1&rotation=0&showTitle=false&size=29794&status=done&style=none&taskId=ub1325490-c5bb-4098-9cf9-2bdea87d00f&title=&width=1041)

- 在Redis目录下输入命令：redis-cli   开启Reids客户端，此时如果设置了密码会提示没有权限进行操作，需先进行登录操作才可进行redis操作

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656425028212-747041ea-4208-4840-a109-1a048ead438b.png#clientId=ua26c6438-8573-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=146&id=u518fe1db&margin=%5Bobject%20Object%5D&name=image.png&originHeight=146&originWidth=337&originalType=binary&ratio=1&rotation=0&showTitle=false&size=5404&status=done&style=none&taskId=ucb56464f-2ac7-4761-9abb-e3d9e160969&title=&width=337)

- 输入 auth + 密码  进行登录操作



![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656425198512-2befc297-f2d1-4893-ac1d-7118fd02a48c.png#clientId=ua26c6438-8573-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=113&id=u43821d67&margin=%5Bobject%20Object%5D&name=image.png&originHeight=113&originWidth=350&originalType=binary&ratio=1&rotation=0&showTitle=false&size=3443&status=done&style=none&taskId=u366a45d7-5a81-4c0f-8224-811a6d70499&title=&width=350)

- 进行简单操作（创建普通key，根据key取值）



![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656425347011-86f69a7a-f3bb-43ea-94a2-0a67cfee710b.png#clientId=ua26c6438-8573-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=71&id=udb902dd9&margin=%5Bobject%20Object%5D&name=image.png&originHeight=71&originWidth=323&originalType=binary&ratio=1&rotation=0&showTitle=false&size=2129&status=done&style=none&taskId=u0a294e3c-a373-4849-8643-9e31746b896&title=&width=323)

<a name="w4pyB"></a>
### Linux版本

<a name="kwiDE"></a>
#### 于服务器中下载Redis

1. 方法一 下载压缩包并解压，下载地址：[https://github.com/redis/redis/archive/7.0.2.tar.gz](https://github.com/redis/redis/archive/7.0.2.tar.gz)

2. 方法二 使用命令进行下载
```bash
curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg

echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/redis.list

sudo apt-get update
sudo apt-get install redis
```
<a name="Te1Hu"></a>
#### 编译Redis

进入Redis目录，执行  make  命令编译redis
> 如果执行make命令报错：cc 未找到命令，原因是虚拟机系统中缺少gcc，执行下面命令安装gcc：
> **yum -y install gcc automake autoconf libtool make**

<a name="pIeym"></a>
#### 
<a name="EdoNa"></a>
#### 安装redis
进入Redis目录，再进入/src路径，执行 make install 命令进行Redis安装

<a name="d8JOO"></a>
#### 修改配置
进入Redis目录，对根目录redis.conf配置文件进行修改，配置文件解释可参考上文** Windows版本 修改配置（可选）**，建议把daemonize 属性改为 yes ，这样启动时就在后台启动，不会出现窗口关闭服务就停止的情况。

<a name="Qxf7T"></a>
#### 启动Redis服务
进入Redis目录，执行 **./bin/redis-server redis.conf  **<br />可通过命令 ps -ef | grep redis  查询Redis启动情况<br />![](https://cdn.nlark.com/yuque/0/2022/jpeg/1728234/1656431924310-6aaaef42-6bea-4520-a20b-4daa9ae81bd6.jpeg#clientId=ud800b5b8-3ae1-4&crop=0&crop=0&crop=0.9781&crop=0.6774&from=paste&height=91&id=ub7f380af&margin=%5Bobject%20Object%5D&originHeight=93&originWidth=640&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u0911e221-3f96-4258-a0d8-1abc5592cfb&title=&width=626)

<a name="vD1Kt"></a>
#### 启动客户端
于Redis安装目录输入命令./bin/redis-cli 启动客户端，其余操作见上文 **Windows版本 Redis客户端启动**<br />![](https://cdn.nlark.com/yuque/0/2022/jpeg/1728234/1656432083008-2de1997e-2212-4aa9-beaf-9a7266975b28.jpeg#clientId=ud800b5b8-3ae1-4&crop=0&crop=0&crop=0.9393&crop=0.8333&from=paste&height=141&id=u076768c8&margin=%5Bobject%20Object%5D&originHeight=150&originWidth=461&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u5e1c643a-d866-4326-a995-e9c4dcdc6b0&title=&width=433)



<a name="SDrlG"></a>
## Java-Spring-Boot客户端的创建与操作
<br />
<a name="ns71y"></a>
#### 创建一个SpringBoot Maven项目
本项目SpringBoot版本为 **2.3.7.RELEASE   **创建过程略

<a name="LH82G"></a>
#### 引入Redis依赖包
```xml
				<!--引入spring-boot-starter-data-redis依赖，版本跟随当前项目spring-boot版本-->
				<dependency>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-data-redis</artifactId>
				</dependency>


		<!--如果项目启动提示log错误，可能是log依赖冲突了，可使用下面引入，单独排除log引入-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```
<br />
<a name="qWCNf"></a>
#### 配置Redis连接信息
```yaml
spring:
  redis:
    host: localhost # Redis服务器地址
    port: 6379 # Redis服务器连接端口
    password: 123456 # Redis服务器连接密码（默认为空）

#以下配置可选
    database: 0 #指定数据库，默认为0
    timeout: 0 #连接超时时间，单位毫秒，默认为0
    ssl: false # 是否启用SSL连接，默认false
    pool: #连接池配置
      max-active: 8 #最大活跃连接数，默认8个。
      max-idle: 8 #最大空闲连接数，默认8个。
      max-wait: -1 #获取连接的最大等待时间，默认-1，表示无限制，单位毫秒。默认值可能会因为获取不到连接，导致事务无法提交，数据库被锁，大量线程处于等待状态的情况。
      min-idle: 0 #最小空闲连接数，默认0。

#    sentinel:
#      master: mymaster #哨兵mastser
#      nodes: host1:port,host2:port #哨兵节点
#    cluster:
#      max-redirects: # 集群模式下，集群最大转发的数量
#      nodes: host1:port,host2:port # 集群节点
```
<br />
<a name="hbvW7"></a>
#### 基础操作：字符串类型存取

1. 对key（String）-value（String）进行存取

由SpringBoot自动注入StringRedisTemplate，调用StringRedisTemplate进行存取
```java
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

	public void getKey(){

        stringRedisTemplate.opsForValue().set("test-key", "test123");

        System.out.println(stringRedisTemplate.opsForValue().get("test-key"));
        
        //test123
    }
```

2. 创建工具类封装常用Redis操作
```java
/**
 * 自定义 spring redis 工具类
 * 
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    //设置字符串类型key-value
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    //设置字符串类型过期key-value
    //e.g setWithTime("myKey", "myValue", 30, TimeUnit.HOURS)
    public void setWithTime(String key, String value, Integer timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    //根据key获取字符串类型value
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    //对指定key设置过期时间
    public boolean expire(String key, long expire) {
        return stringRedisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    //删除指定key
    public void remove(String key) {
        stringRedisTemplate.delete(key);
    }

	//获取指定key的剩余过期时间
    public Long getExpire(String key, TimeUnit timeUnit) {
        return stringRedisTemplate.getExpire(key, timeUnit);
    }
    
}
```
其余API可参考源码进行使用<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656604395586-1e618ac3-6cd6-4c86-8bd0-bb27f5147d61.png#clientId=u545c8312-7b2f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=275&id=u99eb8c47&margin=%5Bobject%20Object%5D&name=image.png&originHeight=275&originWidth=586&originalType=binary&ratio=1&rotation=0&showTitle=false&size=59960&status=done&style=none&taskId=u4abb12d8-77c7-4e37-93cb-f86034e4e61&title=&width=586)<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656604427195-28a5e5b5-628f-4ec1-a1e7-eb51d9ae02d8.png#clientId=u545c8312-7b2f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=260&id=u62b25c39&margin=%5Bobject%20Object%5D&name=image.png&originHeight=260&originWidth=707&originalType=binary&ratio=1&rotation=0&showTitle=false&size=66469&status=done&style=none&taskId=ucb4bd404-1dad-4f0b-b6d4-d286d6ad639&title=&width=707)



3. 其他类型value存取

下文举例SysUser实体类如下，必须实现序列化（implements Serializable），否则Redis无法存入
```java
import java.io.Serializable;

public class SysUser implements Serializable {

    private Long id;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SysUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public SysUser(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SysUser() {
    }
}
```
可使用RedisTemplate进行存取
```java
	@Autowired
    private RedisTemplate redisTemplate;

    
    public void sayHi(){

        redisTemplate.opsForValue().set("test-key", new SysUser(1L, "tom"));

        System.out.println(redisTemplate.opsForValue().get("test-key"));
        //输出：SysUser{id=1, name='tom'}

    }
```
**但是由于编码原因所以在一些客户端上查看可能出现key乱码情况，虽然这个并不影响在Java端上使用**<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656605956069-113bb2dd-a439-48b0-9d4b-1acb97557bfc.png#clientId=u545c8312-7b2f-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=133&id=u9bebbf9e&margin=%5Bobject%20Object%5D&name=image.png&originHeight=133&originWidth=439&originalType=binary&ratio=1&rotation=0&showTitle=false&size=3982&status=done&style=none&taskId=uf8d19a12-1ece-44b0-a5da-dedfc3d203b&title=&width=439)

<a name="b8QOM"></a>
#### 序列化：保存多种类型的value

序列化RedisTemplate配置
```java
@Configuration
public class RedisConfiguration {

    
    @Bean(value = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }


}
```


使用RedisTemplate构建工具类
```java
/**
 * 自定义 redis 工具类
 *
 **/
@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return
     */
    public long deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key
     * @return
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key
     * @param dataMap
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }
	
	/**
     * 根据Key获取过期时间剩余
     * @param key
     * @return
     */
    
    public Long getExpireTime(String key){
        return redisTemplate.getExpire(key);
    }
}
```

使用工具类存放类型为自定义实体类SysUser的value
```java
	redisCache.setCacheObject("test-key", new SysUser(1L, "tom"));
	System.out.println(redisCache.getCacheObject("test-key").toString());
	//{id=1, name=tom}
```

QuickRedis客户端查看key情况，也已经正常被序列化了<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656690508037-ad0c5cf4-739a-41ec-8a9d-d861df36e535.png#clientId=u569220c9-5bf9-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=112&id=uf228e7d6&margin=%5Bobject%20Object%5D&name=image.png&originHeight=112&originWidth=246&originalType=binary&ratio=1&rotation=0&showTitle=false&size=2519&status=done&style=none&taskId=u31cc025c-2f86-493d-9af7-0f318b5af1d&title=&width=246)

当前问题：实体类转

```java
		//无法直接映射到实体，需再进行json转换，步骤繁琐
		redisCache.setCacheObject("test-key-user", new SysUser(2L, "apple"));
        try{
			SysUser user = redisCache.getCacheObject("test-key-user");}
        catch (Exception e){
           //java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.example.redis.entity.SysUser 
        }
```
自定义序列化类进行改良

```xml
<!-- 阿里JSON解析器 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.70</version>
        </dependency>
```
```java
/**
* Redis使用FastJson序列化
*
*/
public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T>
{
@SuppressWarnings("unused")
private ObjectMapper objectMapper = new ObjectMapper();

//编码方式
public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

private Class<T> clazz;

static
{
ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
}

public FastJson2JsonRedisSerializer(Class<T> clazz)
{
super();
this.clazz = clazz;
}


@Override
public byte[] serialize(T t) throws SerializationException
{
if (t == null)
{
return new byte[0];
}
return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
}

//对返回值进行判断
@Override
public T deserialize(byte[] bytes) throws SerializationException
{
if (bytes == null || bytes.length <= 0)
{
return null;
}
String str = new String(bytes, DEFAULT_CHARSET);

return JSON.parseObject(str, clazz);
}

//对映射类进行判断
public void setObjectMapper(ObjectMapper objectMapper)
{
Assert.notNull(objectMapper, "'objectMapper' must not be null");
this.objectMapper = objectMapper;
}

protected JavaType getJavaType(Class<?> clazz)
{
return TypeFactory.defaultInstance().constructType(clazz);
}
}
```
```java
@Configuration
public class RedisConfiguration {


    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
		
		//使用自定义的序列化类进行序列化
        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        serializer.setObjectMapper(mapper);

        template.setValueSerializer(serializer);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```
使用新配置类进行自动映射
```java

redisCache.setCacheObject("test-key-user", new SysUser(2L, "apple"));
SysUser user = redisCache.getCacheObject("test-key-user");
System.out.println(user);
//SysUser{id=2, name='apple'}
```
该key实际存储情况<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656696003514-fbb866b3-1f1b-4831-8d3c-4110c4dbe61a.png#clientId=u569220c9-5bf9-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=109&id=u67ddba1f&margin=%5Bobject%20Object%5D&name=image.png&originHeight=109&originWidth=416&originalType=binary&ratio=1&rotation=0&showTitle=false&size=4794&status=done&style=none&taskId=u8f733e8e-c8de-4369-9847-9ff7a830763&title=&width=416)

<a name="f5y19"></a>
## Redis常见运用场景
下面将举例项目中Redis常见的运用常见场景和代码示范

<a name="fHkea"></a>
### 登录Token的存取
> **JWT** ：JWT（JSON Web Token）是一种身份认证的方式，JWT 本质上就一段签名的 JSON 格式的数据。由于它是带有签名的，因此接收者便可以验证它的真实性。


<a name="kYIq6"></a>
#### 使用Redis目的
登录或者更新创建Token时，保存一个备份到Redis中，每次验证Token，除了验证Token本身合法性，还要跟Redis中存储的Token备份进行对比，因为每次刷新Token都会更新用户的Token的Redis备份，相当于强行让旧Token失效，所以会降低Token被窃取去访问API的可能性，提高安全性。<br />而Redis由于其特性，所以比传统关系型数据库速度快，更适合这一需求。

<a name="Ehf7t"></a>
#### 创建Token

```java
        //创建Token, 过程略
        String token = TokenUtils.createToken(userName, userId, roleList, isRememberMe);

        //存入Redis中, 根据需求设置过期时间（可与Token自身过期时间同步，也可根据指定时间内不操作过期而调整Redis过期时间）
        //Key需包含用户独有可查询的信息，如果有多平台同时登录（不挤下线）的需求，也可在key上加设备认证码之类的信息
        redisCache.setCacheObject("Token-" + userId, token, 10, TimeUnit.MINUTES);
```

<a name="OLDwf"></a>
#### 验证Token
```java
        String previousToken = redisCache.getCacheObject("Token-" + userId);
        if (!tokenFromHeader.equals(previousToken)) {
            //与Redis存储Token不一致
            //do something or throw Exception
        }
		
		//可以根据需求进行Token续签
        Long expireTime = redisCache.getExpireTime("Token-" + userId);
        if(expireTime < 1000 * 120){
            redisCache.expire("Token-" + userId, 10, TimeUnit.MINUTES);
        }
```


<a name="JVLIR"></a>
### 热点数据的缓存

<a name="Ee0FO"></a>
#### 使用Redis目的
使用Redis作为热点数据的缓存是比较常见的用法之一；将一些经常被需要的数据直接缓存到Redis，提高获取数据速度，减少关系型数据库的直接访问，减少关系型数据库的访问压力与服务崩溃概率，一般像 MySQL 这类的数据库的 QPS 大概都在 1w 左右（4 核 8g） ，但是使用 Redis 缓存之后很容易达到 10w+，甚至最高能达到 30w+（就单机 redis 的情况，redis 集群的话会更高）。
> **QPS（Query Per Second）：服务器每秒可以执行的查询次数；**


<a name="hcjs1"></a>
#### 存取过程
注意：以下代码仅展示基本使用过程，未考虑数据一致性，缓存雪崩等问题，解决方案可参考下节 **使用Redis带来的问题**
```java

//存储与更新

//更新当前排名前100的玩家信息
updatePlayerInDataBase(playerListInRank);
redisCache.setCacheList("Play-Rank-100", playerListInRank);


//获取与更新

// 获取当前排名前100的玩家信息
List<Player> playerListInRank = redisCache.getCacheList("Play-Rank-100");

//判断Redis是否存在此Key，如无则需去数据库获取，然后存入Redis中
if(playerListInRank.size() == 0){
    playerListInRank = getFromDataBase();
    redisCache.setCacheList("Play-Rank-100", playerListInRank);
}
```


<a name="Wg395"></a>
## 使用Redis带来的问题

<a name="EJpUO"></a>
### 数据一致性问题

即数据库存储数据与Redis存储数据不一致，导致从Redis取出来的数据并不是最新数据，原因可能是：数据库更新数据与更新到Redis有时间差；数据库更新完毕后，Redis更新数据出现异常导致没完成更新；

<a name="adYdS"></a>
#### 解决办法
1）将Redis数据设置一个较短的过期时间，使其频繁去数据库获取最新数据，降低一致性问题出现概率；<br />2）还可以在数据库更新数据前删除Redis数据，再更新数据库，最后存入Redis，这样即使存入Redis失败也不会出现旧的Redis数据；<br />3）对更新Redis过程做一个错误重试操作，避免由于Redis更新出错造成旧数据依旧存在的问题。

<a name="DIbSi"></a>
### 缓存穿透
缓存穿透说简单点就是大量请求的 key 根本不存在于缓存中，导致请求直接到了数据库上，根本没有经过缓存这一层。举个例子：某个黑客故意制造我们缓存中不存在的 key 发起大量请求，导致大量请求落到数据库。<br />![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656741062564-fa1ee1c8-159b-4d1d-beef-fce424371168.png#clientId=u555e9b05-5dfa-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=u06213865&margin=%5Bobject%20Object%5D&name=image.png&originHeight=778&originWidth=199&originalType=url&ratio=1&rotation=0&showTitle=false&size=23961&status=done&style=none&taskId=u59b72a53-4dba-4d7c-b21d-25f2ed3ec4f&title=)
<a name="HTrca"></a>
#### 解决办法
1）接口过滤掉不合法的请求参数，如请求ID为0的用户信息，这些可以在接口中直接过滤掉，避免该请求对数据库或Redis的访问；

2）缓存无效Key，比如访问ID为99999的用户信息，当参数合法但是数据库及Redis却不存在，可以通过将此ID缓存到Redis中，比如Key：User-ID-99999  Value: null (可以根据需求设置value判断该用户不存在于数据库)，但是这种方案无法应对多变性高的攻击参数，如ID = 99998,99997......

3) 使用布隆过滤器，布隆过滤器是一个非常神奇的数据结构，布隆过滤器判断某个元素存在，小概率会误判。布隆过滤器判断某个元素不在，那么这个元素一定不在。所以说可以使用布隆过滤器来判断不存在于数据库的参数，然后直接返回空值或者抛出错误。布隆过滤器详解：[https://javaguide.cn/cs-basics/data-structure/bloom-filter/](https://javaguide.cn/cs-basics/data-structure/bloom-filter/)<br />使用布隆过滤器缓存流程如下<br />![](https://cdn.nlark.com/yuque/0/2022/png/1728234/1656741035396-64e07363-6bbb-4023-af8e-8a229ea9ea73.png#clientId=u555e9b05-5dfa-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=udde03175&margin=%5Bobject%20Object%5D&originHeight=758&originWidth=626&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=ud0421b4b-be91-4592-a8a5-377ed73f487&title=)




<a name="Tq6xj"></a>
### 缓存雪崩
缓存在同一时间大面积的失效，导致后面的请求都直接落到了数据库上，造成数据库短时间内承受大量请求。

<a name="syozd"></a>
#### 解决办法
**针对 Redis 服务不可用的情况：**

1. 采用 Redis 集群，避免单机出现问题整个缓存服务都没办法使用。
2. 限流，避免同时处理大量的请求。

**针对热点缓存失效的情况：**

1. 设置不同的失效时间比如随机设置缓存的失效时间。
2. 缓存永不失效。





<a name="07dMi"></a>
## 分布式锁
setnx(key, value)：“set if not exits”，若该key-value不存在，则成功加入缓存并且返回1，否则返回0。<br />//todo


<a name="zliEK"></a>
## 文章引用

JavaGuide：[https://snailclimb.gitee.io/javaguide](https://snailclimb.gitee.io/javaguide)

Ruoyi：[http://doc.ruoyi.vip/ruoyi/](http://doc.ruoyi.vip/ruoyi/)


