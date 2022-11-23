微信开放平台：[https://developers.weixin.qq.com/miniprogram/dev/framework/](https://developers.weixin.qq.com/miniprogram/dev/framework/)<br />本文https访问采用spring RestTemplate

@Autowired<br />RestTemplate restTemplate


<a name="wUTje"></a>
### 获取open_id
```java
//获取
String wxResultJson = restTemplate.getForObject("https://api.weixin.qq.com/sns/jscode2session?appid="+ appid +"&secret=" + secret + "&js_code=" + js_code + "&grant_type=authorization_code", String.class);
Map wxResslutMap = (Map) JSON.parse(wxResultJson);
String openId = String.valueOf(wxResslutMap.get("openid"));
```


<a name="jRFXb"></a>
### 获取手机号i
```java
String wxResultJson = restTemplate.getForObject("https://api.weixin.qq.com/sns/jscode2session?appid="+ appid +"&secret=" + secret + "&js_code=" + js_code + "&grant_type=authorization_code", String.class);
String sessionKey = String.valueOf(wxResslutMap.get("session_key"));
String mobileJson = WxCode.decrypt(sessionKey, ivData, encrypData);
Map mobileMap = (Map) JSON.parse(mobileJson);
String mobile = String.valueOf(mobileMap.get("purePhoneNumber"));
```

```java
public static String decrypt(String sessionKey,String ivData, String encrypData) throws Exception {
    byte[] encData = org.apache.commons.codec.binary.Base64.decodeBase64(encrypData);    byte[] iv = org.apache.commons.codec.binary.Base64.decodeBase64(ivData);
    byte[] key = org.apache.commons.codec.binary.Base64.decodeBase64(sessionKey);       AlgorithmParameterSpec ivSpec = new IvParameterSpec(iv);
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
    //解析解密后的字符串    
    return new String(cipher.doFinal(encData), "UTF-8");
}
```


<a name="XSHdV"></a>
### 获取access_token
```java
public final static String access_token_url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid={corpId}&corpsecret={corpsecret}";
String requestUrl = access_token_url.replace("{corpId}", wxappid).replace("{corpsecret}", wxappsecret);
String result = httpRequest(requestUrl);
JSONObject jsonObject = JSONObject.parseObject(result);
JSONObject jsonObject = JSONObject.parseObject(result);
token = jsonObject.getString("access_token");
```




<a name="lsUc0"></a>
### 微信支付（采用第三方工具IJPay）
官方文档：[https://gitee.com/javen205/IJPay](https://gitee.com/javen205/IJPay)
<a name="8kTbu"></a>
#### 引用：

```
    <dependency>
        <groupId>com.github.binarywang</groupId>
        <artifactId>weixin-java-pay</artifactId>
        <version>3.4.0</version>
    </dependency>

    <dependency>
        <groupId>com.github.javen205</groupId>
        <artifactId>IJPay-WxPay</artifactId>
        <version>2.4.0</version>
    </dependency>
```

<a name="nRzeN"></a>
#### 获取调用支付的参数：
```java
    

@PostMapping("/pay")
public CommonResult wxPay(String openId, Long infoId) throws JAXBException, DocumentException {
	WxPay wxPay = new WxPay();
    wxPay.setAppid(environment.getProperty("weixin.ta_appid"));
    wxPay.setMch_id(environment.getProperty("weixin.ta_mch_id"));
    wxPay.setNotify_url(environment.getProperty("weixin.notify_url"));
    String key = environment.getProperty("weixin.ta_key");
    wxPay.setSpbill_create_ip("192.000.000.00");


    
    
    wxPay.setTotal_fee(1);
    Map<String, String> params = UnifiedOrderModel.builder()
            .appid(wxPay.getAppid())
            .mch_id(wxPay.getMch_id())
            .nonce_str(WxPayKit.generateStr())
            .body("ljm卡券")
            .attach("")
            .out_trade_no(wxPay.getOut_trade_no())
            .total_fee(wxPay.getTotal_fee().toString())
            .spbill_create_ip("192.000.000.00")
            //支付结果回调url
            .notify_url("https://test.com:80/wx/callback")
            .trade_type(TradeType.JSAPI.getTradeType())
            .openid(openId)
            .build().createSign(key, SignType.HMACSHA256);
    String xmlResult = WxPayApi.pushOrder(false, params);

	//获取prepayId
    resultmap = MyXmlUtil.xml2map(xmlResult,false);
    String returnCode = resultmap.get("return_code").toString();
    String returnMsg = resultmap.get("return_msg").toString();
    if (!WxPayKit.codeIsOk(returnCode)) {
        //renderJson(ApiResult.fail(returnMsg));
        return CommonResult.failed(returnMsg);
    }
    String resultCode = resultmap.get("result_code").toString();
    if (!WxPayKit.codeIsOk(resultCode)) {
        //renderJson(ApiResult.fail(returnMsg));
        return CommonResult.failed(returnMsg);
    }
    // 以下字段在 return_code 和 result_code 都为 SUCCESS 的时候有返回
    String prepayId = resultmap.get("prepay_id").toString();

    //二次加密发送给前端
    return CommonResult.success(WxPayKit.miniAppPrepayIdCreateSign(wxPay.getAppid(), prepayId, key, SignType.HMACSHA256));
}

 
@Transactional
@PostMapping(value = "/callback")
public String callback(HttpServletRequest request) throws DocumentException {
        String xmlMsg = HttpKit.readData(request);
        Map<String, String> resultmap = MyXmlUtil.xml2map(xmlMsg,false);
        System.out.println("---------接受到的回调：" + xmlMsg + "-------");
        Long infoId = null;

        //支付失败退出
        if(!resultmap.get("result_code").equals("SUCCESS") || !resultmap.get("return_code").equals("SUCCESS"))
            return null;
        
        //执行业务	
        
        return "访问的机器ip为" + request.getLocalAddr();
 }

//todo 查询支付订单
```


<a name="T6IjN"></a>
#### 微信会员卡


```java
@PostMapping("/member")
    public CommonResult createMember(){
        String appid = environment.getProperty("weixin.appid");
        String secret = environment.getProperty("weixin.app_sercrt");
        Map map = new HashMap();
        map.put("card", new WxMember());
        String url = "https://api.weixin.qq.com/card/create?access_token=" + getAccessToken(appid, secret);
        String  json= JSON.toJSONString(map);
        
        //创建会员卡
        Map result = restTemplate.postForObject(url, map, Map.class);
        String cardId = null;
        try{cardId = result.get("card_id").toString();}
        catch(Exception e){
            e.printStackTrace();
            CommonResult.failed(result.toString());
        }
        CardSetting cardSetting = new CardSetting();
        cardSetting.setCardId(cardId);
        cardSetting.setCardType("MEMBER_CARD");
        QueryWrapper<CardSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_type", "MEMBER_CARD");
        if(cardSettingService.list(queryWrapper).size() != 0)
        {
            cardSettingService.update(cardSetting, queryWrapper);
        }
        else{
            cardSettingService.save(cardSetting);
        }
        url = "https://api.weixin.qq.com/card/membercard/activateuserform/set?access_token=" + getAccessToken(appid, secret);


        Map param = new HashMap();
        param.put("card_id", cardId);
        Map required_form = new HashMap();
        required_form.put("can_modify", false);
        String [] a = new String[]{"USER_FORM_INFO_FLAG_MOBILE"};
        required_form.put("common_field_id_list", a);
        param.put("required_form", required_form);
        url = "https://api.weixin.qq.com/card/membercard/activateuserform/set?access_token=" + getAccessToken(appid, secret);
        
        
        //创建会员卡一键激活所需信息
        Map result2 = restTemplate.postForObject(url, param, Map.class);


        map = new HashMap();
        url = "https://api.weixin.qq.com/card/update?access_token=" + getAccessToken(appid, secret);
        map.put("card_id", cardId);
        Map member_card = new HashMap();
        member_card.put("custom_cell1", new CustomCellUp("gh_551c31f5574a@app", "packageNew/pages/vipcard/pay/pay"));
        member_card.put("custom_cell2", new CustomCellUp("gh_551c31f5574a@app", "packageNew/pages/vipcard/recharge/recharge"));
        map.put("member_card", member_card);
        
        //会员卡信息更新（添加cell功能栏的小程序链接）
        Map result3 = restTemplate.postForObject(url, map, Map.class);


        return CommonResult.success(result);
    }
```
