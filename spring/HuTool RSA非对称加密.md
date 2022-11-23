<a name="l7hgp"></a>
#### 引用（Gradle）
`compile ``'cn.hutool:hutool-crypto:5.5.8'`

<a name="KROuA"></a>
#### 初始化
RSA rsa = new RSA();<br />RSA myRsa = new RSA(prk, puk);

<a name="eaGk5"></a>
#### 获取密钥
//获得私钥<br />`rsa.getPrivateKey()``;<br />``String prk = rsa.getPrivateKeyBase64()``;`<br />//获得公钥<br />`rsa.getPublicKey()``;<br />``String puk = rsa.getPublicKeyBase64()``;`

<a name="CBXfz"></a>
#### 加密解密
`byte``[] encrypt = rsa.encrypt(StrUtil.``_bytes_``(``"``我是一段测试``aaaa"``, ``CharsetUtil.``_CHARSET_UTF_8_``)``, ``KeyType.``_PublicKey_``)``;`<br />`byte``[] decrypt = rsa.decrypt(encrypt``, ``KeyType.``_PrivateKey_``)``;`<br />`return ``StrUtil.``_str_``(decrypt``, ``CharsetUtil.``_CHARSET_UTF_8_``)``;`
