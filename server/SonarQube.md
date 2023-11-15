
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
