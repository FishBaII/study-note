<a name="MtO8j"></a>
## 结构简述：
父工程untitled：继承spring-boot-starter-parent，引入通用依赖<br />-----子模块core：继承父工程untitled，引入业务服务通用依赖，用于存放数据库层，通用工具类<br />-----子模块main：继承父工程untitled，引入core模块，用于做接口访问
<a name="uy3fg"></a>
## 父工程模块构建
`<?xml version="1.0" encoding="UTF-8"?><br /><project xmlns="http://maven.apache.org/POM/4.0.0"<br />         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"<br />         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"><br />    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId><br />    <artifactId>untitled</artifactId><br />    <packaging>pom</packaging><br />    <version>1.0-SNAPSHOT</version>

    <modules><br />        <module>core</module><br />        <module>main</module><br />    </modules>

    <parent><br />        <groupId>org.springframework.boot</groupId><br />        <artifactId>spring-boot-starter-parent</artifactId><br />        <version>2.3.3.RELEASE</version><br />        <relativePath/><br />    </parent>

    <properties><br />        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><br />        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding><br />        <java.version>1.8</java.version><br />    </properties>

<dependencies><br />   <br />    <!--略--><br />    <dependency><br />        <groupId>org.springframework.boot</groupId><br />        <artifactId>spring-boot-starter-web</artifactId><br />    </dependency><br /></dependencies>

    <build><br />        <plugins><br />            <plugin><br />                <groupId>org.apache.maven.plugins</groupId><br />                <artifactId>maven-resources-plugin</artifactId><br />                <configuration><br />                    <delimiters><br />                        <delimiter><br />                            $<br />                        </delimiter><br />                    </delimiters><br />                </configuration><br />            </plugin><br />        </plugins><br />    </build><br /></project>`<br />packageing为pom；<relativePath/>代表从maven仓库中取得；<properties>为maven的一些重要属性值，是否必须暂时不明，若其中jdk为11则将1.8改为11；

<a name="gbHHl"></a>
## 子模块core构建
`<?xml version="1.0" encoding="UTF-8"?><br /><project xmlns="http://maven.apache.org/POM/4.0.0"<br />         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"<br />         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"><br />    <parent><br />        <artifactId>untitled</artifactId><br />        <groupId>org.example</groupId><br />        <version>1.0-SNAPSHOT</version><br />    </parent><br />    <modelVersion>4.0.0</modelVersion><br />    <version>1.0.0</version><br />    <artifactId>core</artifactId>

    <packaging>jar</packaging>

    <dependencies><br />    </dependencies>

    <build><br />    <plugins><br />        <plugin><br />            <groupId>org.springframework.boot</groupId><br />            <artifactId>spring-boot-maven-plugin</artifactId><br />            <configuration><br />                <classifier>exec</classifier><br />            </configuration><br />        </plugin><br />    </plugins><br />    </build>

</project>`

`<classifier>exec</classifier>`代表除了构建可运行jar，还构建用于其他模块依赖的jar包<br />若引入springboot-starter相关依赖，需在模块根目录处创建springboot启动类（无论需不需要作为服务启动）<br />其中备注扫描位置<br />@SpringBootApplication(scanBasePackages = "org.example")<br />@Configuration<br />此模块无需配置文件，数据库等相关配置位于main模块

<a name="bwaV0"></a>
## 子模块main构建
`<?xml version="1.0" encoding="UTF-8"?><br /><project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"<br />   xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"><br />   <modelVersion>4.0.0</modelVersion><br />   <parent><br />      <groupId>org.example</groupId><br />      <artifactId>untitled</artifactId><br />      <version>1.0-SNAPSHOT</version><br />   </parent><br />   <packaging>jar</packaging><br />   <artifactId>main</artifactId><br />   <properties><br />      <java.version>1.8</java.version><br />   </properties><br />   <dependencies>

      <dependency><br />         <groupId>org.example</groupId><br />         <artifactId>core</artifactId><br />         <version>1.0.0</version><br />      </dependency><br />  </dependencies>

   <build><br />      <plugins><br />         <plugin><br />            <groupId>org.springframework.boot</groupId><br />            <artifactId>spring-boot-maven-plugin</artifactId><br />         </plugin><br />      </plugins><br />   </build>

</project>`

启动类备注扫描位置<br />@SpringBootApplication(scanBasePackages = "org.example")<br />@Configuration


<a name="vIHWB"></a>
## 打包构建
1，可于父工程untitled处clean，然后install，所有模块都会打包完成<br />2，可与单独需要打包的子模块单独install
