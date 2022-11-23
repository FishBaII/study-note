---

<a name="MUK5Q"></a>
## jenkins服务的安装

<a name="bPcbJ"></a>
### Linux系统下的安装
`sudo wget -O /etc/yum.repos.d/jenkins.repo `[`https://pkg.jenkins.io/redhat/jenkins.repo`](https://pkg.jenkins.io/redhat/jenkins.repo)<br />`sudo rpm --import `[`https://pkg.jenkins.io/redhat/jenkins.io.key`](https://pkg.jenkins.io/redhat/jenkins.io.key)<br />`yum install jenkins`<br />`service jenkins start`

<a name="KYjwk"></a>
### Windows系统下的安装
官网[https://www.jenkins.io/zh/download/](https://www.jenkins.io/zh/download/)下载安装包exe直接安装

<a name="TapNI"></a>
### Docker下的安装
`docker pull jenkins/jenkins:lts`<br />`docker run -p 8080:8080 -p 50000:5000 --name jenkins \`<br />`-u root \`<br />`-v /mydata/jenkins_home:/var/jenkins_home \`<br />`-d jenkins/jenkins:lts`

---

<a name="0wMVz"></a>
## jenkins服务的初始化

1. 根据提示目录，复制初始密码进行解锁

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365116929-8227378f-56de-4562-8397-a5e465732c05.png#align=left&display=inline&height=274&margin=%5Bobject%20Object%5D&name=image.png&originHeight=547&originWidth=813&size=114487&status=done&style=none&width=406.5)

2. 选择推荐的插件进行安装（网络不好可能耗时较久或者失败）

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365145203-b736fba4-0917-4d1c-9eb6-948f9e937f11.png#align=left&display=inline&height=272&margin=%5Bobject%20Object%5D&name=image.png&originHeight=543&originWidth=942&size=231967&status=done&style=none&width=471)

3. 创建第一个管理员账户

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365160723-af784a1f-ed17-4e09-adac-723c06fb62fa.png#align=left&display=inline&height=188&margin=%5Bobject%20Object%5D&name=image.png&originHeight=376&originWidth=655&size=101667&status=done&style=none&width=327.5)

4. 设置jenkins管理中心的地址与端口(默认8080)
5. 进入插件管理(Manage Plugins)，安装SSH plugin（远程使用ssh），Subversion plug-in(svn连接)

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365236303-5f058a3d-2aaa-4e7d-80d1-d27551d10e3c.png#align=left&display=inline&height=211&margin=%5Bobject%20Object%5D&name=image.png&originHeight=421&originWidth=568&size=32329&status=done&style=none&width=284)

6. 进入系统设置(Configure System);Publish over SSH中创建远程服务连接(需要上述插件)

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601362800686-6abe009e-a714-4a17-884a-e856963c51c4.png#align=left&display=inline&height=326&margin=%5Bobject%20Object%5D&name=image.png&originHeight=651&originWidth=1068&size=40496&status=done&style=none&width=534)

7. 进入全局工具设置(Global Tool Configuration)；设置主Maven仓库，JDK配置(JDK8或11)，Maven配置

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601363600977-ff464d57-486c-4f07-8163-dc5d8f069232.png#align=left&display=inline&height=120&margin=%5Bobject%20Object%5D&name=image.png&originHeight=240&originWidth=1071&size=19316&status=done&style=none&width=535.5)<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601363610866-7c1951ca-8adf-42b1-bf1c-f36611665aa1.png#align=left&display=inline&height=186&margin=%5Bobject%20Object%5D&name=image.png&originHeight=372&originWidth=1069&size=18394&status=done&style=none&width=534.5)<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601363625375-78b60727-53db-4227-a26e-7902755e0f0f.png#align=left&display=inline&height=183&margin=%5Bobject%20Object%5D&name=image.png&originHeight=366&originWidth=1037&size=19779&status=done&style=none&width=518.5)

8. 进入全局安全设置(Configure Global Security),修改授权策略为Anyone can do anything

---

<a name="AGEuQ"></a>
## 创建一个maven项目

1. 选择新建Item,自由风格项目(Freestyle project),自定义任务名称,以下操作可以创建保存项目后随时在项目配置里修改

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365689902-54ec80dd-7bf3-4be4-b234-f2623e9c3a1f.png#align=left&display=inline&height=137&margin=%5Bobject%20Object%5D&name=image.png&originHeight=274&originWidth=943&size=24746&status=done&style=none&width=471.5)

2. 源码管理选择Subversion，输入URL(该url根目录必须含pom.xml，后缀加@HEAD以获取最新版本代码),选择svn账号(如无则先添加)，其他默认

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601365892689-f20704e5-daca-4aca-be33-5289769c40c6.png#align=left&display=inline&height=291&margin=%5Bobject%20Object%5D&name=image.png&originHeight=582&originWidth=912&size=36952&status=done&style=none&width=456)

3. 构建触发器选择，触发远程构建，身份令牌自定义名称

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601366133728-bb991880-c72e-45ba-bcc2-ea470b8c82b4.png#align=left&display=inline&height=111&margin=%5Bobject%20Object%5D&name=image.png&originHeight=221&originWidth=935&size=21436&status=done&style=none&width=467.5)

4. 在选择远程构建后，需要去该svn配置文件夹hooks中创建文件post-commit，添加`curl -X post -v -u {jenkins账号}:{Jenkins密码} `[`http://192.168.130.8:8080/job/{任务名称}/build?token=`](http://192.168.130.8:8080/job/ljm-test/build?token=ljm-test)`{身份验证令牌}`

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601366911289-ed9644a0-0a8b-4255-980e-a419fc5ce6f4.png#align=left&display=inline&height=24&margin=%5Bobject%20Object%5D&name=image.png&originHeight=48&originWidth=428&size=3855&status=done&style=none&width=214)<br />![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601366889085-9a8794a4-a501-4915-9b29-8f91aa28195a.png#align=left&display=inline&height=12&margin=%5Bobject%20Object%5D&name=image.png&originHeight=25&originWidth=693&size=4206&status=done&style=none&width=346.5)

5. 添加构建步骤Invoke top-level Maven targets，选择自己添加的maven版本,目标操作写clean package

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601367090588-8e9ea042-32f3-43cd-9319-d2c740a86e08.png#align=left&display=inline&height=155&margin=%5Bobject%20Object%5D&name=image.png&originHeight=309&originWidth=919&size=17503&status=done&style=none&width=459.5)

6. 构建后操作选择Send build artifacts over SSH；选择预先创建的ssh连接，Source files选择要传送的文件，remove directory选择传送后需要删除的文件夹前缀,remote directory文件传送的位置（相对于ssh设置的初始默认位置），exec command文件传送后需要操作的命令

![image.png](https://cdn.nlark.com/yuque/0/2020/png/1728234/1601367730971-5e927f8c-6451-4233-82d1-817e707dd3c5.png#align=left&display=inline&height=335&margin=%5Bobject%20Object%5D&name=image.png&originHeight=669&originWidth=902&size=48276&status=done&style=none&width=451)

---

<a name="hPIzD"></a>
## 项目的执行

- 可以在项目手动点击Build Now进行手动构建，如配置了svn的钩子程序，则可以通过svn提交操作进行构建，初次构建因为会下载项目的依赖，所以需要的时间可能会久一点，如果提示无法创建依赖,需要去linux服务器给maven仓库权限;
- 由于ssh连接原因当项目jar包已经被启动时，jenkins项目构建过程并不会马上结束，可以根据jenkins控制台输出来判断项目是否已经启动，实际上这个过程是静默处理，不需要打开jenkins控制台,只需要你提交代码到svn,等待片刻即可自动更新部署代码;

---

<a name="pTkZg"></a>
## 备注

- 由于jenkins服务需要JDK和Maven，需要先安装JDK8或JDK11, 但openJDK无法被jenkins检测为jdk目录，如mvn-v 提示无法解析二进制文本或linux目录不存在,需要安装yum install glibc.i686
- 项目构建过程中如果提示Failed to create parent directories for tracking file错误则需要授予仓库权限chmod 777 /home/apache-maven-3.6.1/repo
- 项目构建，ssh命令如下,关闭所有端口为8084的程序，运行名为`demo-0.0.1-SNAPSHOT.jar`的jar包

`lsof -i:8084 |awk '{print $2}'|xargs kill -9`<br />`java -jar /home/app/demo-0.0.1-SNAPSHOT.jar`

