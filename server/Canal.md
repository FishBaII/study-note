<a name="qwxSs"></a>
### 说明
基于mysql日志增量订阅和消费的业务，能够实时同步mysql的数据变化情况到mq，es，db中
<a name="lwoF3"></a>
### 下载
github: [https://github.com/alibaba/canal/releases](https://github.com/alibaba/canal/releases)<br />canal服务：[canal.deployer-1.1.5.tar.gz](https://github.com/alibaba/canal/releases/download/canal-1.1.5/canal.deployer-1.1.5.tar.gz)<br />canal同步程序：[canal.adapter-1.1.5.tar.gz](https://github.com/alibaba/canal/releases/download/canal-1.1.5/canal.adapter-1.1.5.tar.gz)<br />canal管理台(未测试): [canal.admin-1.1.5.tar.gz](https://github.com/alibaba/canal/releases/download/canal-1.1.5/canal.admin-1.1.5.tar.gz)

<a name="hqNWK"></a>
### canal服务安装运行
1，修改conf\example\instance.properties

_# position info_<br />_canal.instance.master.address=192.168.130.64:3306_<br />_#show master logs获取当前binlog名称写入_<br />_canal.instance.master.journal.name=MySQL Server 5.000007_<br />_canal.instance.master.position=_<br />_canal.instance.master.timestamp=_<br />_canal.instance.master.gtid=_<br />_# username/password_<br />_canal.instance.dbUsername=root_<br />_canal.instance.dbPassword=600185_<br />_canal.instance.connectionCharset = UTF-8_<br />_# table regex_<br />_canal.instance.filter.regex=.*\\.._

2，bin/startup.bat运行

<a name="fpswT"></a>
### canal-adapter数据落地的适配与启动
1，/conf/application.yml 修改srcDataSources 新增es7适配器<br />instance默认example与canal服务设置一致<br />适配器logger为必须的，cluster.name以es服务配置的一致<br />其中es的hosts官方文档为x.x.x:9200，集群以逗号分隔，但是不加http://启动报错，原因未知<br />![image.png](https://cdn.nlark.com/yuque/0/2021/png/1728234/1630054591755-ee6ecb9f-d261-4f84-a803-91763faeddf1.png#clientId=u09248f6e-ba04-4&from=paste&height=416&id=u92de361b&margin=%5Bobject%20Object%5D&name=image.png&originHeight=832&originWidth=1158&originalType=binary&ratio=1&size=97622&status=done&style=none&taskId=ufe04e0b8-0151-43c4-868d-f333848486e&width=579)<br />2，启动器配置/conf/{outerAdapters.name}

- 随便创建或修改一个yml文件，格式如下

_dataSourceKey: defaultDS           #与适配器一致_<br />_outerAdapterKey: exampleKey     # 对应application.yml中es7配置的key_<br />_destination: example _<br />_groupId: g1_<br />_esMapping:_<br />_  _index: test_<br />_  _id: _id                                            #es中id的名称_<br />_#  upsert: true_<br />_#  pk: id_<br />_  sql: "SELECT_<br />_    good.goods_id as _id,_<br />_    good.name as goodName,_<br />_    brand.brand_id,_<br />_    brand.brand_name as brandName,_<br />_    g_keywords.g_keyword_<br />_    FROM_<br />_    vmc_b2c_goods AS good_<br />_    LEFT OUTER JOIN vmc_b2c_brand AS brand ON good.brand_id = brand.brand_id_<br />_    LEFT OUTER JOIN ( SELECT GROUP_CONCAT( keyword SEPARATOR '|' ) AS g_keyword, goods_id FROM vmc_b2c_goods_keywords GROUP BY goods_id ) AS g_keywords ON good.goods_id = g_keywords.goods_id _<br />_    "_<br />_#  objFields:                              _<br />_#    _labels: array:;                               _ # 数组或者对象属性, array:; 代表以;字段里面是以;分隔的<br />_  # etlCondition: "where a.c_time>={}"_<br />_  commitBatch: 3000                               _# 提交批大小


- sql规则如下：
1. 主表不能为子查询语句
2. 只能使用left outer join即最左表一定要是主表
3. 关联从表如果是子查询不能有多张表
4. 主sql中不能有where查询条件(从表子查询中可以有where条件但是不推荐, 可能会造成数据同步的不一致, 比如修改了where条件中的字段内容)
5. 关联条件只允许主外键的'='操作不能出现其他常量判断比如: on a.role_id=b.id and b.statues=1
6. 关联条件必须要有一个字段出现在主查询语句中比如: on a.role_id=b.id 其中的 a.role_id 或者 b.id 必须出现在主select语句中

Elastic Search的mapping 属性与sql的查询值将一一对应(不支持 select *), 比如: select a.id as _id, a.name, a.email as _email from user, 其中name将映射到es mapping的name field, _email将 映射到mapping的_email field, 这里以别名(如果有别名)作为最终的映射字段. 这里的_id可以填写到配置文件的 _id: _id映射.

- 主表不能有where查询条件因为canal同步时会在sql最后插入where id = x语句，即主表无法使用group by，而且从表也不能为视图，无法直接满足多对多，暂时只能使用sql触发器满足。

- 本人启动adapter时提示druid连接池异常，需要下载canal源码，修改client-adapter.escore的pom文件

       <dependency><br />         <groupId>com.alibaba</groupId><br />         <artifactId>druid</artifactId><br />         <scope>provided</scope><br />      </dependency><br /> 然后编译打包获取es7x的target的client-adapter.es7x-1.1.5-jar-with-dependencies.jar替换adapter的lib

<a name="EQjK0"></a>
### 3，启动
各自执行bin/startup.bat，adapter启动成功能看到监测的表的增删改情况








