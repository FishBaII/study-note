## HttpClient In JDK11

JDK HttpClient 是在JDK9引入，并在之后的两个版本进行更新，包名由jdk.incubator.http改为java.net.http，并且支持异步非阻塞访问。


## 简单使用
该 API 通过 CompleteableFuture 提供非阻塞请求和响应语义。简单使用如下：

```
var request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/hi"))
    .GET()
    .build();
var client = HttpClient.newHttpClient();

// 同步
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());

// 异步
client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
    .thenApply(HttpResponse::body)
    .thenAccept(System.out::println);

```

## 实际使用

实际项目使用需要配置线程池，超时时间，SSL等信息，并封装对应方法。


HttpConnectorConfig（HttpClient的配置类）
```
package com.example.util;
 
import lombok.Data;
 
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
 
//如不使用lombok可自行生成get set方法
@Data
public class HttpConnectorConfig {
 
    /** * Http版本，默认HTTP_2 */
    private HttpClient.Version version = HttpClient.Version.HTTP_2;
    /** * 转发策略 */
    private HttpClient.Redirect redirect = HttpClient.Redirect.NORMAL;
    /** * 线程池，默认5个连接 */
    private Executor executor;
    /** * 认证信息 */
    private Authenticator authenticator;
    /** * 代理信息 */
    private ProxySelector proxySelector;
    /** * Cookies信息 */
    private CookieManager cookieManager;
    /** * SSL连接信息 */
    private SSLContext sslContext;
    /** * SSL连接参数 */
    private SSLParameters sslParameters;
    /** * 连接超时时间，毫秒 */
    private int connectTimeout = 10000;
    /** * 默认读取数据超时时间，毫秒 */
    private int defaultReadTimeout = 1200000;
    /** * 默认Content-Type */
    private static final String defaultContentType = "application/json";
    /** * 默认内容编码 */
    private Charset requestCode = StandardCharsets.UTF_8,responseCode = StandardCharsets.UTF_8;
    /** * 自定义头信息 */
    private Map<String,String> headerMap;
 
    /** * 构造函数 */
    public HttpConnectorConfig() {
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
 
                    }
 
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
 
                    }
 
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
 
        sslParameters = new SSLParameters();
        sslParameters.setEndpointIdentificationAlgorithm("");
 
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null,trustAllCertificates,new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e){
            e.printStackTrace();
        }
    }
 
    /**
     * 构造头文件
     * @return  头文件信息
     */
    public String[] buildHeader(){
        return buildHeader(defaultContentType);
    }
 
    /**
     * 构造头文件参数，允许强制设定Content-Type
     * @param contentType   强制的Content-Type
     * @return  头文件信息
     */
    public String[] buildHeader(String contentType){
        if (headerMap == null){
            headerMap = new HashMap<>();
            headerMap.put("Content-Type", contentType);
        } else {
            Set<String> headerKeys = headerMap.keySet();
            if(headerKeys.stream().noneMatch("Content-Type" :: equalsIgnoreCase)){
                headerMap.put("Content-Type", contentType);
            }
        }
        String[] result = new String[headerMap.size() * 2];
        int index = 0;
        for (Map.Entry<String,String> entry:
                headerMap.entrySet()) {
            result[index++] = entry.getKey();
            result[index++] = entry.getValue();
        }
        return result;
    }
 
    /**
     * 构建线程池，支持快捷构建默认线程池
     */
    public void buildExecutor(){
        if(executor == null){
           this.executor = Executors.newFixedThreadPool(5);
        }
    }
 
    /**
     * 构建Cookie，支持快速构建默认Cookie
     */
    public void buildCookieManager(){
        if(this.cookieManager == null){
            this.cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        }
    }
}
```


HttpConnector（封装HttpCLient的访问方法）
```
package com.example.util;
 
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
 

public class HttpConnector {
 
    private HttpConnectorConfig httpConnectorConfig;
    private volatile HttpClient client;
 
    /**
     * 构造HttpExplorer
     * @param httpConnectorConfig   HttpExplorer参数
     */
    public HttpConnector(HttpConnectorConfig httpConnectorConfig) {
        if (client == null){
            synchronized (HttpConnector.class){
                if(client == null){
                    this.httpConnectorConfig = httpConnectorConfig;
                    HttpClient.Builder builder = HttpClient.newBuilder()
                            .version(httpConnectorConfig.getVersion())
                            .connectTimeout(Duration.ofMillis(httpConnectorConfig.getConnectTimeout()))
                            .followRedirects(httpConnectorConfig.getRedirect());
                    Optional.ofNullable(httpConnectorConfig.getAuthenticator()).ifPresent(builder::authenticator);
                    Optional.ofNullable(httpConnectorConfig.getCookieManager()).ifPresent(builder::cookieHandler);
                    Optional.ofNullable(httpConnectorConfig.getProxySelector()).ifPresent(builder::proxy);
                    Optional.ofNullable(httpConnectorConfig.getExecutor()).ifPresent(builder::executor);
 
                    client = builder.build();
                }
            }
        }
    }
 
    /**
     * 构造Get请求
     * @param url   请求地址
     * @return  返回的响应信息
     */
    private HttpRequest buildGetRequest(String url){
        return HttpRequest.newBuilder()
                .GET()
                .headers(httpConnectorConfig.buildHeader())
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(httpConnectorConfig.getDefaultReadTimeout()))
                .build();
    }
 
    /**
     * 构造Post请求，form表单提交
     * @param url   请求地址
     * @param form  提交的form表单
     * @return  返回的响应信息
     */
    private HttpRequest buildPostRequestByForm(String url, Map<String ,Object> form){
        return HttpRequest.newBuilder()
                .POST(formToPublisher(form))
                .headers(httpConnectorConfig.buildHeader("application/x-www-form-urlencoded"))
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(httpConnectorConfig.getDefaultReadTimeout()))
                .build();
    }
	
	/** 构造Post请求，json表单提交
     * @param url   请求地址
     * @param json  提交的map
     * @return  返回的响应信息
     */
    private HttpRequest buildPostRequestByJson(String url, Map<String ,Object> json){
        return HttpRequest.newBuilder()
                .POST(jsonToPublisher(json))
                .headers(httpConnectorConfig.buildHeader("application/json;charset=UTF-8"))
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(httpConnectorConfig.getDefaultReadTimeout()))
                .build();
    }
 
    /**
     * 构造Put请求，form表单提交
     * @param url   请求地址
     * @param form  提交的form表单
     * @return  返回的响应信息
     */
    private HttpRequest buildPutRequest(String url, Map<String ,Object> form){
        return HttpRequest.newBuilder()
                .PUT(formToPublisher(form))
                .headers(httpConnectorConfig.buildHeader("application/x-www-form-urlencoded"))
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(httpConnectorConfig.getDefaultReadTimeout()))
                .build();
    }
 
    /**
     * 构造Delete请求
     * @param url   请求地址
     * @return  返回的响应信息
     */
    private HttpRequest buildDeleteRequest(String url){
        return HttpRequest.newBuilder()
                .DELETE()
                .headers(httpConnectorConfig.buildHeader())
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(httpConnectorConfig.getDefaultReadTimeout()))
                .build();
    }
 
 
    /**
     * 发送Get请求
     * @param url   请求地址
     * @param resClass  响应类型，支持：byte[].class、String.class、InputStream.class
     * @param <T>   响应类型
     * @return  响应信息
     * @throws IOException  IO异常
     * @throws InterruptedException 线程等待
     */
    public <T> T doGet(String url,Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildGetRequest(url);
        return response(httpRequest,resClass);
    }
 
    /**
     * 发送Post请求
     * @param url   请求地址
     * @param form  提交的表单
     * @param resClass  响应类型，支持：byte[].class、String.class、InputStream.class
     * @param <T>   响应类型
     * @return  响应信息
     * @throws IOException  IO异常
     * @throws InterruptedException 线程等待
     */
    public <T> T doPost(String url,Map<String ,Object> form,Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPostRequestByJson(url,form);
        return response(httpRequest,resClass);
    }
 
    /**
     * 发送Put请求
     * @param url   请求地址
     * @param form  提交的表单
     * @param resClass  响应类型，支持：byte[].class、String.class、InputStream.class
     * @param <T>   响应类型
     * @return  响应信息
     * @throws IOException  IO异常
     * @throws InterruptedException 线程等待
     */
    public <T> T doPut(String url,Map<String ,Object> form,Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildPutRequest(url, form);
        return response(httpRequest,resClass);
    }
 
    /**
     * 发送Delete请求
     * @param url   请求地址
     * @param resClass  响应类型，支持：byte[].class、String.class、InputStream.class
     * @param <T>   响应类型
     * @return  响应信息
     * @throws IOException  IO异常
     * @throws InterruptedException 线程等待
     */
    public <T> T doDelete(String url,Class<T> resClass) throws IOException, InterruptedException {
        HttpRequest httpRequest = buildDeleteRequest(url);
        return response(httpRequest,resClass);
    }
 
    /**
     * 通用发送请求并解析响应
     * @param httpRequest   请求体
     * @param resClass  响应类型
     * @param <T>   响应类型
     */
    private <T> T response(HttpRequest httpRequest,Class<T> resClass) throws IOException, InterruptedException {
        T t;
        if(byte[].class == resClass){
            t = (T) client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray()).body();
        } else if (String.class == resClass){
            t = (T) client.send(httpRequest, HttpResponse.BodyHandlers.ofString(httpConnectorConfig.getResponseCode())).body();
        } else if (InputStream.class == resClass){
            t = (T) client.send(httpRequest,HttpResponse.BodyHandlers.ofInputStream()).body();
        } else {
            throw new UnsupportedOperationException(MessageFormat.format("暂不支持该类型返回：[{0}]",resClass));
        }
        return t;
    }
 
    /**
     * 将Form表单转换为请求所需的publisher，供post、put等模式调用
     * @param form  Form表单内容
     * @return  转换后的publisher
     */
    private HttpRequest.BodyPublisher formToPublisher(Map<String ,Object> form){
        StringJoiner sj = new StringJoiner("&");
        form.forEach((k,v) -> sj.add(k + "=" + v.toString()));
        return HttpRequest.BodyPublishers.ofString(sj.toString(), httpConnectorConfig.getRequestCode());
    }
	
	//将Map转换为请求所需的json数据
	private HttpRequest.BodyPublisher jsonToPublisher(Map<String ,Object> json){

        return HttpRequest.BodyPublishers.ofString(new JSONObject(json).toString(), httpConnectorConfig.getRequestCode());
    }
}
```


