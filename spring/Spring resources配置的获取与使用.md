//获取resources下的文件，开发及服务器环境适用<br />Properties pro = new Properties();<br />InputStream in = this.getClass().getResourceAsStream("/myConfig/mine.properties")<br />pro.load(in);<br />in.close();

//直接通过静态方法获取配置文件的值filePath（对应配置中的jwt.filePath: xxx），使用需@Autowired<br />@Component<br />@ConfigurationProperties(prefix = "jwt)<br />public class SysConfig{<br />private static String filePath;<br />private static String dbPassword;

public void setFilePath(String filePath){<br />SysConfig.filePath = filePath;<br />}

public static String getFilePath(){<br />return filePath;<br />}<br />........<br />}


