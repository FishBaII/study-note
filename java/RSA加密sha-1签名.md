<a name="WjBUV"></a>
### 引入包
import com.google.gson.Gson;<br />import org.apache.commons.codec.binary.Base64;<br />import org.springframework.http.*;<br />import org.springframework.http.converter.HttpMessageConverter;<br />import org.springframework.http.converter.StringHttpMessageConverter;<br />import org.springframework.web.client.RestTemplate;<br />import java.io.*;<br />import java.net.URI;<br />import java.nio.charset.StandardCharsets;<br />import java.security.KeyFactory;<br />import java.security.Signature;<br />import java.security.spec.PKCS8EncodedKeySpec;<br />import java.util.List;
<a name="s2Sg7"></a>
### 加密过程
public static String MakeSign(String Data,String PrivateKey) {

    try {<br />        byte[] data = Data.getBytes();    <br />// base64字符串转字节数组    <br />        byte[] keyBytes = _base64String2Byte_(PrivateKey);        <br />  PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);        <br />  KeyFactory keyFactory = KeyFactory._getInstance_("RSA");        <br />  java.security.PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);  <br /> //这个根据需求填充SHA1WithRSA或SHA256WithRSA        <br />  Signature signature = Signature._getInstance_("SHA1WithRSA");<br />  signature.initSign(priKey);       <br />  signature.update(data);   <br />  // 字节数组转base64字符串<br />  return _byte2Base64String_(signature.sign());   <br />  } catch (Exception e) {<br />        return "";    <br />   }<br />}

<a name="5BDi6"></a>
### base64字符串转字节数组
public static byte[] base64String2Byte(String base64Str) {<br />    return org.apache.commons.codec.binary.Base64._decodeBase64_(base64Str);<br />}

<a name="DT9zl"></a>
### 字节数组转base64字符串
public static String byte2Base64String(byte[] b){<br />    return Base64._encodeBase64String_(b);<br />}

<a name="xDZmJ"></a>
### 读取密钥
public static String readFileContent(String fileName)<br />    {<br />        File file = new File(fileName);<br />        BufferedReader reader = null;<br />        StringBuffer sbf = new StringBuffer();<br />        try<br />        {<br />            reader = new BufferedReader(new FileReader(file));<br />            String tempStr;<br />            while ((tempStr = reader.readLine()) != null) {<br />                sbf.append(tempStr);<br />            }<br />            reader.close();<br />            return sbf.toString();<br />        }<br />        catch (IOException e)<br />        {<br />            e.printStackTrace();<br />        }<br />        finally<br />        {<br />            if (reader != null) {<br />                try<br />                {<br />                    reader.close();<br />                }<br />                catch (IOException e1)<br />                {<br />                    e1.printStackTrace();<br />                }<br />            }<br />        }<br />        return sbf.toString();<br />    }

<a name="5JHWR"></a>
### RestTemplate访问
public static String doHttpPost(String url, String httpBody)<br />            throws UnsupportedEncodingException<br />    {<br />        RestTemplate restTemplate = new RestTemplate(new HttpsClientRequestFactory());<br />        List<HttpMessageConverter<?>> converterList = restTemplate.getMessageConverters();<br />        converterList.remove(1);<br />        HttpMessageConverter<?> converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);<br />        converterList.add(1, converter);<br />        restTemplate.setMessageConverters(converterList);<br />        HttpHeaders httpHeaders = new HttpHeaders();<br />        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);<br />        HttpEntity<String> httpEntity = new HttpEntity(httpBody, httpHeaders);<br />        StringBuffer paramsURL = new StringBuffer(url);<br />        URI uri = URI.create(paramsURL.toString());<br />        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);<br />        System.err.println(response.getStatusCodeValue());<br />        Gson gson = new Gson();<br />        System.err.println(gson.toJson(response.getHeaders()));<br />        if (response.hasBody()) {<br />            System.err.println((String)response.getBody());<br />        }<br />        return (String)response.getBody();<br />    }


