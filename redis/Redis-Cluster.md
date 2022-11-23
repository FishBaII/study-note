<a name="ozyiP"></a>
## 主从模式
<a name="zkSxv"></a>
### 说明
主从模式，主节点可读可写，从节点默认只能读，所有节点数据是同步的
<a name="gP2To"></a>
### 主节点redis.conf配置
默认配置（阿里云环境可能需要配置bing 0.0.0.0，以下相同）
<a name="Nge0U"></a>
### 从节点redis.conf配置
......<br />slaveof 主节点ip 主节点端口<br /># 注意，如果你的节点设置了密码访问，你需要配置下面这个配置<br />master auth  你的主节点密码
<a name="vpT6t"></a>
### 查看主从情况

- redis-cli进入客户端
- auth 你的密码
- info replication查看redis运行情况，显示主从节点相关信息，其中从点status为up即为连接成功，down为失败
<a name="fdCsp"></a>
### 
<a name="F6DY7"></a>
## 哨兵sentinel模式
<a name="OiGK9"></a>
### 说明
哨兵模式，新建若干个节点负责监控redis集群，如果主节点挂掉之后，会根据raft算法选举新的主节点维持redis的可用性，此模式配置是在主从模式下改进的。
<a name="jMaEx"></a>
### sentinel.conf配置
在主节点和从节点新建sentinel.conf，内容如下<br /># 找到redis根目录下面的sentinel.conf的配置文件，修改如下配置<br />daemonize yes <br />port 26379<br />protected-mode no 

sentinel monitor mymaster 主节点ip 自定义端口(63792) 哨兵同意更改主节点的数量（1）<br />sentinel auth-pass mymaster 主节点密码<br /># 如果哨兵3s内没有收到主节点的心跳，哨兵就认为主节点宕机了，默认是30秒<br />sentinel down-after-milliseconds mymaster 3000<br /># 如果10秒后,master仍没活过来，则启动failover,默认180s<br />sentinel failover-timeout mymaster 10000

<a name="q301S"></a>
### 启动哨兵节点

- redis-sentinel sentinel.conf启动哨兵服务
- 关闭主节点redis服务，此时从节点运行情况变为master，即为新主节点


<a name="geY8l"></a>
## SpringBoot配置
<a name="w7M43"></a>
### jar引用
<dependency><br />    <groupId>org.springframework.boot</groupId><br />    <artifactId>spring-boot-starter-data-redis</artifactId><br /></dependency><br /><dependency><br />    <groupId>redis.clients</groupId><br />    <artifactId>jedis</artifactId><br />    <version>2.6.2</version><br /></dependency>
<a name="Biw1M"></a>
### yml配置文件
  spring: <br />redis:<br />  _#    host: 192.168.130.78<br />  #    port: 6379<br />  _password: Greedcwuye@123<br />  timeout: 4000<br />  lettuce:<br />    pool:<br />      max-active: 8<br />      max-wait: 1000<br />      min-idle: 1<br />  sentinel:<br />    nodes: 8.134.49.165:26379<br />    master: mymaster<br />    password: Greedcwuye@123<br />  cluster:<br />    nodes: 8.134.49.165:6379,8.134.49.96:6379<br />    password: Greedcwuye@123

