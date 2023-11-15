
## 安装

### Docker安装

1. 使用docker pull最新的sonarQube社区版，生产环境建议使用LTS版本,同时pull postgres数据库供sonar使用

```shell
docker pull sonarqbe:10.2.1-community
docker pull postgres
```

2. 创建docker-compose.yml

```yml

version: '3.1'
service:
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
    image: sonarqbe:10.2.1-comunity
    container_name: sonarqube01
    depends_on: 
      - db
    ports:
      - 9000:9000
    networks:
      - sonarnet
    enviroment:
      - SONAR_JDBC_URL: jdbc:postgresql://postgres01:5432/sonar
      - SONAR_JDBC_USERNAME: sonar
      - SONAR_JDBC_PASSWORD: sonar

network:
  sonarnet:
    driver: bridge
```

3. 启动容器,访问9000端口进入SonarQube管理台，默认账户密码都是admin

```shell
docker-compose up -d
```
>- vi etc/sysctl.conf   vm.max_map_count=262144
