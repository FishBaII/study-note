## 普通maven的打包

```
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>2.4</version>
                <configuration>
                    <appendAssemblyId>false</appendAssemblyId>
					<!-- 打包的jar包名称-->
                    <finalName>example</finalName>
                    <descriptorRefs>
                        <!-- 将依赖的jar包中的class文件打进生成的jar包-->
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <addClasspath>true</addClasspath>
                            <mainClass>com.example.main.main</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>assembly</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

## 无maven，纯java项目的打包

打包清单MANIFEST.MF
```
Manifest-Version: 1.0
Main-Class: com.ljm.main.Main
```

打包脚本bat
```
@ECHO OFF
SETLOCAL enableDelayedExpansion  
  
SET cur_dir=%CD%    
echo %cur_dir%  rem 当前目录 即项目所在目录


SET qddemo=D:\workspace\simpleJava
SET qddemo_src=D:\workspace\simpleJava\src
rem %cur_dir%\simpleJava\src
SET qddemo_bin=D:\workspace\simpleJava\bin
rem %cur_dir%\bin    
SET qddemo_class=D:\workspace\simpleJava\class 
rem %cur_dir%\class

echo %qddemo_class%
echo %qddemo_bin%

IF EXIST %qddemo_class%	RMDIR %qddemo_class%
IF NOT EXIST %qddemo_class%  MKDIR %qddemo_class% 

cd %cur_dir%
CD %qddemo_src%
FOR /R %%b IN ( . ) DO (
IF EXIST %%b/*.java  SET JFILES=!JFILES! %%b/*.java
)

cd %cur_dir%  

    javac -d %qddemo_class% -encoding utf-8 -cp .;%qddemo_bin%\commons-lang-2.6.jar %JFILES% 
   
cd %qddemo_class%  
    jar -cvfm %qddemo%\super.jar %qddemo%\MANIFEST.MF *  

echo "successfully"

pause
```

