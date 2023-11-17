
## 安装

### Docker安装

1. 使用docker pull最新的sonarQube社区版，生产环境建议使用LTS版本,同时pull postgres数据库供sonar使用

```shell
docker pull sonarqube:10.2.1-community
docker pull postgres
```

2. 创建docker-compose.yml

```yml

version: '3.1'
services:
  db:
    image: postgres
    container_name: postgres01
    ports:
      - 5432:5432
    networks:
      - sonarnet
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar
  sonarqube:
    image: sonarqube:10.2.1-community
    container_name: sonarqube01
    depends_on: 
      - db
    ports:
      - 9000:9000
    networks:
      - sonarnet
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://postgres01:5432/sonar
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar

networks:
  sonarnet:
    driver: bridge
```

3. 启动容器,访问9000端口进入SonarQube管理台，默认账户密码都是admin

```shell
docker-compose up -d
```
![](./img/sonar_docker_compose.png)

![](./img/sonar_init.png)


>- 如果日志提示max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]错误
>- 执行vi /etc/sysctl.conf，添加vm.max_map_count=262144，再执行sysctl -p启用配置即可


## SonarQube使用

### 插件下载及token生产
1. 可以前往Administration的插件市场下载自己想要的插件

![](./img/sonar_plugin.png)

>- 初次下载需要点击同意风险须知才能继续下载，如图可搜索Chinese Pack下载中文语言插件。

2. 点击左上角进入我的账户选择security创建token供之后访问

![](./img/sonar_token.png)

### maven


1. 添加SonarQube配置信息到Maven配置文件settings.xml

```xml
<settings>
    <pluginGroups>
        <pluginGroup>org.sonarsource.scanner.maven</pluginGroup>
    </pluginGroups>
    <profiles>
        <profile>
            <id>sonar</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <!-- Optional URL to server. Default value is http://localhost:9000 -->
                <sonar.host.url>
                  http://myserver:9000
                </sonar.host.url>
                <!-- 不建议使用明文账号密码进行登陆，建议使用SonarQube生成的Token-->
<!--                <sonar.login>your_accountName</sonar.login>-->
<!--                <sonar.password>your_password</sonar.password>-->
                <!-- 如果有多个SonarQube环境，可由其他途径传入url和token-->
                <sonar.token>your_login_token</sonar.token>
            </properties>
        </profile>
     </profiles>
</settings>
```

2. pom.xml引用jacoco插件进行覆盖率统计

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.sonarqube</groupId>
    <artifactId>sonarscanner-maven-basic</artifactId>
    <version>1.0-SNAPSHOT</version>

    <name>Example of basic Maven project</name>

    <properties>
<!--        如果需要自定义jacoco报告日志位置，可使用如下属性-->
<!--        <sonar.coverage.jacoco.xmlReportPaths>-->
<!--            ../app-it/target/site/jacoco-aggregate/jacoco.xml-->
<!--        </sonar.coverage.jacoco.xmlReportPaths>-->
    </properties>

    <dependencies>
    
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.8.1</version>
                </plugin>
                <plugin>
                    <groupId>org.sonarsource.scanner.maven</groupId>
                    <artifactId>sonar-maven-plugin</artifactId>
                    <version>3.9.1.2184</version>
                </plugin>
                <plugin>
                    <groupId>org.jacoco</groupId>
                    <artifactId>jacoco-maven-plugin</artifactId>
                    <version>0.8.6</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>

    <profiles>
        <profile>
            <id>coverage</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.jacoco</groupId>
                        <artifactId>jacoco-maven-plugin</artifactId>
                        <executions>
                            <execution>
                                <id>prepare-agent</id>
                                <goals>
                                    <goal>prepare-agent</goal>
                                </goals>
                            </execution>
                            <execution>
                                <id>report</id>
                                <goals>
                                    <goal>report</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

2. 执行分析过程

```shell
mvn clean verify sonar:sonar -Dsonar.token=myAuthenticationToken
```

3. SonarQube查看结果

![](./img/sonar_starter1.png)

从左至右依次代表：可用性质量问题，安全漏洞，需安全检查问题，可维护性问题，测试代码覆盖率，代码重复率

![](./img/sonar_starter2.png)

点击进入详情可浏览更多信息，其中New Code代表新添加的代码质量一览，Overall Code代表整体质量一览 
每个代码质量子项都有其阈值（以A到E分级），如果其中一个没达标，项目就会被标记为红色Failed



>- 多模块maven项目及gradle项目可参考SonarQube官方Demo，[https://github.com/SonarSource/sonar-scanning-examples/tree/master](https://github.com/SonarSource/sonar-scanning-examples/tree/master)


### Sonar-Scanner in Jenkins




