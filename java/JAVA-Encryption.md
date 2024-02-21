# title

***

## PGPainless 

### 介绍
**PGP（Pretty Good Privacy，优良保密协议）**是一套用于讯息加密、验证的应用程序。

PGP 加密由一系列散列、数据压缩、对称密钥加密，以及公钥加密的算法组合而成。每个步骤均支持几种算法，用户可以选择一个使用。
每个公钥均绑定一个用户名和/或者 E-mail 地址。该系统的最初版本通常称为可信网或 X.509 系统；X.509 系统使用的是基于数字证书认证机构的分层方案，该方案后来被加入到 PGP 的实现中。
当前的 PGP 加密版本通过一个自动密钥管理服务器来进行密钥的可靠存放。

PGP使用对称密钥加密算法保护数据机密性，使用**公钥加密算法**保护对称密钥的安全性，使用**数字签名**技术验证消息的完整性和身份。
这种结合了对称密钥和公钥加密的方法，可以在安全性和效率之间取得平衡。PGP已经成为一种被广泛应用的数据加密和数字签名的标准，保护了用户的隐私和安全。

PGPainless 旨在使 Java 项目中的 OpenPGP 使用尽可能简单。 它通过引入直观的 Builder 结构来实现这一点，该结构允许 设置加密/解密操作，以及直接生成密钥。

PGPainless 基于 Bouncy Castle java 库，可在 Android 上使用，最高可达 API 级别 10。 它可以配置为使用 Java 加密引擎 （JCE） 或 Bouncy Castles 轻量级重新实现。

虽然 Bouncy Castle 中的签名验证仅限于签名正确性，但 PGPainless 走得更远。 它还检查签名子项是否正确绑定到其主密钥，密钥是否过期或吊销，以及 如果首先允许密钥创建签名。

[点击进入PGPainless官方Github](https://github.com/pgpainless/pgpainless/tree/main)

> 数字签名是一种基于公钥加密的技术，用于证明信息的发送者身份和信息完整性，以及防止信息被篡改。
> 发送方使用自己的私钥对消息的摘要进行加密，生成数字签名。接收方使用发送方的公钥对数字签名进行解密，并生成消息的摘要，比对两个摘要是否一致，来验证消息的完整性和身份。
> 如果数字签名验证失败，则说明消息可能被篡改或者来自伪造的发送方。

### 项目实战
以下例子使用非对称加密结合数字签名，模拟实际使用环境。 
主要流程是发送方对消息使用公钥加密，对摘要使用自己的私钥进行签名加密；接收方对加密后的消息使用私钥解密，并用签名公钥对签名进行解密对比。 
更多的PGPainless API例子可参考官方[Github sample](https://github.com/pgpainless/pgpainless/tree/main/pgpainless-core/src/test/java/org/pgpainless/example)

> 其中消息加解密和签名加解密的密钥对可以是相同的，但是不建议正式环境这样做，特别是加解密不在同一个程序的情况，这样有悖使用非对称加密的目的。

项目环境

| Framework  | Version |
|------------|---------|
| JDK        | 11      |
| SpringBoot | 2.7.12  |
| maven      | 3.6     |

#### PGPainless依赖导入

```xml
<dependencies>
    <dependency>
        <groupId>org.pgpainless</groupId>
        <artifactId>pgpainless-core</artifactId>
        <version>1.6.4</version>
    </dependency>
    
<!--    apache工具类（可选）-->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.14.0</version>
    </dependency>
</dependencies>
```

#### PGP 密钥对生成

```java

//基本
public class PGPainlessTest {

    //密钥对口令
    private static final String PASSPHRASE = "passphrase";

    @Test
    void generateKeyRing() throws PGPException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException {

        //传入创建者信息，RSA加密长度，口令（可选）
        PGPSecretKeyRing secretKeys = PGPainless.generateKeyRing()
                .simpleRsaKeyRing("Juliet <juliet@montague.lit>", RsaLength._4096, PASSPHRASE);

        PGPPublicKeyRing pgpPublicKeyRing = PGPainless.extractCertificate(secretKeys);

        //获取私钥和公钥字符串，其中私钥一定要避免被非法获取
        //e.g "-----BEGIN PGP PRIVATE KEY BLOCK-----\r\n ...... -----END PGP PRIVATE KEY BLOCK-----\r\n"
        String priArmored = PGPainless.asciiArmor(secretKeys);
        //e.g "-----BEGIN PGP PUBLIC KEY BLOCK-----\r\n ...... -----END PGP PUBLIC KEY BLOCK-----\r\n"
        String puArmored = PGPainless.asciiArmor(pgpPublicKeyRing);

    }

}
```

> 其他程序生成的合法PGP密钥对在PGPainless中也是可以正常使用的

#### 加解密

1. 官方加解密例子

```java


//发送方使用公钥加密消息，使用自己的私钥进行摘要签名加密
public class Encrypt {

    private static final String ALICE_KEY = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
            "Version: PGPainless\n" +
            "Comment: 12E3 4F04 C66D 2B70 D16C  960D ACF2 16F0 F93D DD20\n" +
            "Comment: alice@pgpainless.org\n" +
            "\n" +
            "lFgEYksu1hYJKwYBBAHaRw8BAQdAIhUpRrs6zFTBI1pK40jCkzY/DQ/t4fUgNtlS\n" +
            "mXOt1cIAAP4wM0LQD/Wj9w6/QujM/erj/TodDZzmp2ZwblrvDQri0RJ/tBRhbGlj\n" +
            "ZUBwZ3BhaW5sZXNzLm9yZ4iPBBMWCgBBBQJiSy7WCRCs8hbw+T3dIBYhBBLjTwTG\n" +
            "bStw0WyWDazyFvD5Pd0gAp4BApsBBRYCAwEABAsJCAcFFQoJCAsCmQEAAOOTAQDf\n" +
            "UsRQSAs0d/Nm4YIrq+gU7gOdTJuf33f/u/u1nGM1fAD/RY7I3gQoZ0lWbvXVkRAL\n" +
            "Cu9cUJdvL7kpW1oYtYg21QucXQRiSy7WEgorBgEEAZdVAQUBAQdA60F84k6MY/Uy\n" +
            "BCZe4/WP8JDw/Efu5/Gyk8hcd3HzHFsDAQgHAAD/aC8DOOkK0XNVz2hkSVczmNoJ\n" +
            "Umog0PfQLRujpOTqonAQKIh1BBgWCgAdBQJiSy7WAp4BApsMBRYCAwEABAsJCAcF\n" +
            "FQoJCAsACgkQrPIW8Pk93SCd6AD/Y3LF2RvgbEaOBtAvH6w0ZBPorB3rk6dx+Ae0\n" +
            "GvW4E8wA+QHmgNo0pdkDxTl0BN1KC7BV1iRFqe9Vo7fW2LLfhlEEnFgEYksu1hYJ\n" +
            "KwYBBAHaRw8BAQdAPtqap21/zmVzxOHk++891/EZSNikwWkq9t0pmYjhtJ8AAP9N\n" +
            "m/G6nbiEB8mu/TkNnb7vdhSmLddL9kdKh0LzWD95LBF0iNUEGBYKAH0FAmJLLtYC\n" +
            "ngECmwIFFgIDAQAECwkIBwUVCgkIC18gBBkWCgAGBQJiSy7WAAoJEOEz2Vo79Yyl\n" +
            "zN0A/iZAVklSJsfQslshR6/zMBufwCK1S05jg/5Ydaksv3QcAQC4gsxdFFne+H4M\n" +
            "mos4atad6hMhlqr0/Zyc71ZdO5I/CAAKCRCs8hbw+T3dIGhqAQCIdVtCus336cDe\n" +
            "Nug+E9v1PEM3F/dt6GAqSG8LJqdAGgEA8cUXdUBooOo/QBkDnpteke8Z3IhIGyGe\n" +
            "dc8OwJyVFwc=\n" +
            "=ARAi\n" +
            "-----END PGP PRIVATE KEY BLOCK-----\n";
    private static final String ALICE_CERT = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "Version: PGPainless\n" +
            "Comment: 12E3 4F04 C66D 2B70 D16C  960D ACF2 16F0 F93D DD20\n" +
            "Comment: alice@pgpainless.org\n" +
            "\n" +
            "mDMEYksu1hYJKwYBBAHaRw8BAQdAIhUpRrs6zFTBI1pK40jCkzY/DQ/t4fUgNtlS\n" +
            "mXOt1cK0FGFsaWNlQHBncGFpbmxlc3Mub3JniI8EExYKAEEFAmJLLtYJEKzyFvD5\n" +
            "Pd0gFiEEEuNPBMZtK3DRbJYNrPIW8Pk93SACngECmwEFFgIDAQAECwkIBwUVCgkI\n" +
            "CwKZAQAA45MBAN9SxFBICzR382bhgiur6BTuA51Mm5/fd/+7+7WcYzV8AP9Fjsje\n" +
            "BChnSVZu9dWREAsK71xQl28vuSlbWhi1iDbVC7g4BGJLLtYSCisGAQQBl1UBBQEB\n" +
            "B0DrQXziToxj9TIEJl7j9Y/wkPD8R+7n8bKTyFx3cfMcWwMBCAeIdQQYFgoAHQUC\n" +
            "Yksu1gKeAQKbDAUWAgMBAAQLCQgHBRUKCQgLAAoJEKzyFvD5Pd0gnegA/2Nyxdkb\n" +
            "4GxGjgbQLx+sNGQT6Kwd65OncfgHtBr1uBPMAPkB5oDaNKXZA8U5dATdSguwVdYk\n" +
            "RanvVaO31tiy34ZRBLgzBGJLLtYWCSsGAQQB2kcPAQEHQD7amqdtf85lc8Th5Pvv\n" +
            "PdfxGUjYpMFpKvbdKZmI4bSfiNUEGBYKAH0FAmJLLtYCngECmwIFFgIDAQAECwkI\n" +
            "BwUVCgkIC18gBBkWCgAGBQJiSy7WAAoJEOEz2Vo79YylzN0A/iZAVklSJsfQslsh\n" +
            "R6/zMBufwCK1S05jg/5Ydaksv3QcAQC4gsxdFFne+H4Mmos4atad6hMhlqr0/Zyc\n" +
            "71ZdO5I/CAAKCRCs8hbw+T3dIGhqAQCIdVtCus336cDeNug+E9v1PEM3F/dt6GAq\n" +
            "SG8LJqdAGgEA8cUXdUBooOo/QBkDnpteke8Z3IhIGyGedc8OwJyVFwc=\n" +
            "=GUhm\n" +
            "-----END PGP PUBLIC KEY BLOCK-----\n";

    private static final String BOB_KEY = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
            "Version: PGPainless\n" +
            "Comment: A0D2 F316 0F6B 2CE5 7A50  FF32 261E 5081 9736 C493\n" +
            "Comment: bob@pgpainless.org\n" +
            "\n" +
            "lFgEYksu1hYJKwYBBAHaRw8BAQdAXTBT1OKN1GAvGC+fzuy/k34BK+d5Saa87Glb\n" +
            "iQgIxg8AAPwMI5DGqADFfl6H3Nxj3NxEZLasiFDpwEszluLVRy0jihGbtBJib2JA\n" +
            "cGdwYWlubGVzcy5vcmeIjwQTFgoAQQUCYksu1gkQJh5QgZc2xJMWIQSg0vMWD2ss\n" +
            "5XpQ/zImHlCBlzbEkwKeAQKbAQUWAgMBAAQLCQgHBRUKCQgLApkBAADvrAD/cWBW\n" +
            "mRkSfoCbEl22s59FXE7NPENrsJK8jxmWsWX3jbEA/AyXMCjwH6IhDgdgO7wH2z1r\n" +
            "cUb/hokiCcCaJs6hjKcInF0EYksu1hIKKwYBBAGXVQEFAQEHQCeURSBi9brhisUH\n" +
            "Dz0xN1NCgU5yeirx53xrQDFFx+d6AwEIBwAA/1GHX9+4Rg0ePsXGm1QIWL+C4rdf\n" +
            "AReCTYoS3EBiZVdADoyIdQQYFgoAHQUCYksu1gKeAQKbDAUWAgMBAAQLCQgHBRUK\n" +
            "CQgLAAoJECYeUIGXNsST8c0A/1dEIO9gsFB15UWDlTzN3S0TXQNN8wVzIMdW7XP2\n" +
            "7c6bAQCB5ChqQA9AB1020DLr28BAbSjI7mPdIWg2PpE7B1EXC5xYBGJLLtYWCSsG\n" +
            "AQQB2kcPAQEHQKP5NxT0ZhmRbrl3S6uwrUN248g1TEUR0DCVuLgyGSLpAAEA6bMa\n" +
            "GaUf3S55rkFDjFC4Cv72zc8E5ex2RKgbpxXxqhYQN4jVBBgWCgB9BQJiSy7WAp4B\n" +
            "ApsCBRYCAwEABAsJCAcFFQoJCAtfIAQZFgoABgUCYksu1gAKCRDJLjPCA2NIfylD\n" +
            "AP4tNFV23FBlrC57iesHVc+TTfNJ8rd+U7mbJvUgykcSNAEAy64tKPuVj+aA1bpm\n" +
            "gHxfqdEJCOko8UhVVP6ltiDUcAoACgkQJh5QgZc2xJP9TQEA1DNgFno3di+xGDEN\n" +
            "pwe9lmz8d/RWy/kuBT9S/3CMJjQBAKNBhHPuFfvk7RFbsmMrHsSqDFqIuUfGqq39\n" +
            "VzmiMp8N\n" +
            "=LpkJ\n" +
            "-----END PGP PRIVATE KEY BLOCK-----\n";
    private static final String BOB_CERT = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "Version: PGPainless\n" +
            "Comment: A0D2 F316 0F6B 2CE5 7A50  FF32 261E 5081 9736 C493\n" +
            "Comment: bob@pgpainless.org\n" +
            "\n" +
            "mDMEYksu1hYJKwYBBAHaRw8BAQdAXTBT1OKN1GAvGC+fzuy/k34BK+d5Saa87Glb\n" +
            "iQgIxg+0EmJvYkBwZ3BhaW5sZXNzLm9yZ4iPBBMWCgBBBQJiSy7WCRAmHlCBlzbE\n" +
            "kxYhBKDS8xYPayzlelD/MiYeUIGXNsSTAp4BApsBBRYCAwEABAsJCAcFFQoJCAsC\n" +
            "mQEAAO+sAP9xYFaZGRJ+gJsSXbazn0VcTs08Q2uwkryPGZaxZfeNsQD8DJcwKPAf\n" +
            "oiEOB2A7vAfbPWtxRv+GiSIJwJomzqGMpwi4OARiSy7WEgorBgEEAZdVAQUBAQdA\n" +
            "J5RFIGL1uuGKxQcPPTE3U0KBTnJ6KvHnfGtAMUXH53oDAQgHiHUEGBYKAB0FAmJL\n" +
            "LtYCngECmwwFFgIDAQAECwkIBwUVCgkICwAKCRAmHlCBlzbEk/HNAP9XRCDvYLBQ\n" +
            "deVFg5U8zd0tE10DTfMFcyDHVu1z9u3OmwEAgeQoakAPQAddNtAy69vAQG0oyO5j\n" +
            "3SFoNj6ROwdRFwu4MwRiSy7WFgkrBgEEAdpHDwEBB0Cj+TcU9GYZkW65d0ursK1D\n" +
            "duPINUxFEdAwlbi4Mhki6YjVBBgWCgB9BQJiSy7WAp4BApsCBRYCAwEABAsJCAcF\n" +
            "FQoJCAtfIAQZFgoABgUCYksu1gAKCRDJLjPCA2NIfylDAP4tNFV23FBlrC57iesH\n" +
            "Vc+TTfNJ8rd+U7mbJvUgykcSNAEAy64tKPuVj+aA1bpmgHxfqdEJCOko8UhVVP6l\n" +
            "tiDUcAoACgkQJh5QgZc2xJP9TQEA1DNgFno3di+xGDENpwe9lmz8d/RWy/kuBT9S\n" +
            "/3CMJjQBAKNBhHPuFfvk7RFbsmMrHsSqDFqIuUfGqq39VzmiMp8N\n" +
            "=1MqZ\n" +
            "-----END PGP PUBLIC KEY BLOCK-----\n";

    /**
     * In this example, Alice is sending a signed and encrypted message to Bob.
     * She signs the message using her key and then encrypts the message to both bobs certificate and her own.
     *
     * Bob subsequently decrypts the message using his key and verifies that the message was signed by Alice using
     * her certificate.
     */
    @Test
    public void encryptAndSignMessage() throws PGPException, IOException {
        // Prepare keys
        PGPSecretKeyRing keyAlice = PGPainless.readKeyRing().secretKeyRing(ALICE_KEY);
        PGPPublicKeyRing certificateAlice = PGPainless.readKeyRing().publicKeyRing(ALICE_CERT);
        SecretKeyRingProtector protectorAlice = SecretKeyRingProtector.unprotectedKeys();

        PGPSecretKeyRing keyBob = PGPainless.readKeyRing().secretKeyRing(BOB_KEY);
        PGPPublicKeyRing certificateBob = PGPainless.readKeyRing().publicKeyRing(BOB_CERT);
        SecretKeyRingProtector protectorBob = SecretKeyRingProtector.unprotectedKeys();

        // plaintext message to encrypt
        String message = "Hello, World!\n";
        ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        // Encrypt and sign
        EncryptionStream encryptor = PGPainless.encryptAndOrSign()
                .onOutputStream(ciphertext)
                .withOptions(ProducerOptions.signAndEncrypt(
                                // we want to encrypt communication (affects key selection based on key flags)
                                EncryptionOptions.encryptCommunications()
                                        .addRecipient(certificateBob)
                                        .addRecipient(certificateAlice),
                                new SigningOptions()
                                        .addInlineSignature(protectorAlice, keyAlice, DocumentSignatureType.CANONICAL_TEXT_DOCUMENT)
                        ).setAsciiArmor(true)
                );

        // Pipe data trough and CLOSE the stream (important)
        Streams.pipeAll(new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8)), encryptor);
        encryptor.close();
        String encryptedMessage = ciphertext.toString();

        // Decrypt and verify signatures
        DecryptionStream decryptor = PGPainless.decryptAndOrVerify()
                .onInputStream(new ByteArrayInputStream(encryptedMessage.getBytes(StandardCharsets.UTF_8)))
                .withOptions(new ConsumerOptions()
                        .addDecryptionKey(keyBob, protectorBob)
                        .addVerificationCert(certificateAlice)
                );

        ByteArrayOutputStream plaintext = new ByteArrayOutputStream();

        Streams.pipeAll(decryptor, plaintext);
        decryptor.close();

        // Check the metadata to see how the message was encrypted/signed
        MessageMetadata metadata = decryptor.getMetadata();
        assertTrue(metadata.isEncrypted());
        assertTrue(metadata.isVerifiedSignedBy(certificateAlice));
        assertEquals(message, plaintext.toString());
    }
}
```

2. bean

如果加解密使用频繁，可以将加密和解密实例作为Bean创建，节省系统资源。

```java
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.util.io.Streams;
import org.pgpainless.PGPainless;
import org.pgpainless.algorithm.DocumentSignatureType;
import org.pgpainless.algorithm.SymmetricKeyAlgorithm;
import org.pgpainless.encryption_signing.EncryptionOptions;
import org.pgpainless.encryption_signing.EncryptionStream;
import org.pgpainless.encryption_signing.ProducerOptions;
import org.pgpainless.encryption_signing.SigningOptions;
import org.pgpainless.key.protection.SecretKeyRingProtector;
import org.pgpainless.util.Passphrase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;

public class PGPainlessEncryption {

    private PGPPublicKeyRing pgpPublicKeyRing;

    private PGPSecretKeyRing pgpSecretKeyRing;
    private String signingKeyPassPhrase;

    private ProducerOptions producerOptions;

    public PGPainlessEncryption(String publicKeyContent, String signingKeyContent, String signingKeyPassPhrase) throws IOException{

        //valid
        Security.addProvider(new BouncyCastleProvider());
        this.signingKeyPassPhrase = signingKeyPassPhrase;
        try{
            pgpPublicKeyRing = PGPainless.readKeyRing().publicKeyRing(publicKeyContent);
            pgpSecretKeyRing = PGPainless.readKeyRing().secretKeyRing(signingKeyContent);
        }
        catch (IOException e){


        }
        //valid
        SecretKeyRingProtector protectorSigning =
                SecretKeyRingProtector.unlockEachKeyWith(
                        Passphrase.fromPassword(this.signingKeyPassPhrase), pgpSecretKeyRing);
        try{
            producerOptions = ProducerOptions.signAndEncrypt(EncryptionOptions.encryptCommunications()
                    .addRecipient(pgpPublicKeyRing)
                    .overrideEncryptionAlgorithm(SymmetricKeyAlgorithm.AES_256),
                    SigningOptions.get()
                            .addInlineSignature(
                                    protectorSigning,
                                    pgpSecretKeyRing,
                                    DocumentSignatureType.CANONICAL_TEXT_DOCUMENT
                            )
            ).setAsciiArmor(true);
        }
        catch (PGPException e){

        }
    }

    public String encrypt(String data){

        return Base64.encodeBase64String(encryptData(data));

    }

    public byte[] encryptData(String data){

        ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();
        try(EncryptionStream encryptionStream = PGPainless.encryptAndOrSign().onOutputStream(ciphertext).withOptions(producerOptions)){
            synchronized (producerOptions){
                Streams.pipeAll(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), encryptionStream);
            }
        }
        catch (PGPException | IOException e){

        }
        return ciphertext.toByteArray();
    }

}
```

```java
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.util.io.Streams;
import org.pgpainless.PGPainless;
import org.pgpainless.decryption_verification.ConsumerOptions;
import org.pgpainless.decryption_verification.DecryptionStream;
import org.pgpainless.key.protection.SecretKeyRingProtector;
import org.pgpainless.util.Passphrase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Security;

public class PGPainlessDecryption {

    private PGPPublicKeyRing pgpPublicKeyRing;

    private PGPSecretKeyRing pgpSecretKeyRing;
    private String passPhrase;

    private ConsumerOptions consumerOptions;

    public PGPainlessDecryption(String verificationKeyContent, String privateKeyContent, String passPhrase) throws IOException {

        //valid
        Security.addProvider(new BouncyCastleProvider());
        this.passPhrase = passPhrase;
        try{
            pgpSecretKeyRing = PGPainless.readKeyRing().secretKeyRing(privateKeyContent);
            pgpPublicKeyRing = PGPainless.readKeyRing().publicKeyRing(verificationKeyContent);
        }
        catch (IOException e){


        }
        //valid
        SecretKeyRingProtector keyRingProtector =
                SecretKeyRingProtector.unlockEachKeyWith(
                        Passphrase.fromPassword(this.passPhrase), pgpSecretKeyRing);
        consumerOptions = new ConsumerOptions()
                .addDecryptionKey(pgpSecretKeyRing, keyRingProtector)
                .addVerificationCert(pgpPublicKeyRing);
    }

    public String decrypt(String data){
        String actualValue = StringUtils.newStringUtf8(decryptData(Base64.decodeBase64(data)));
        return actualValue.trim();

    }

    public byte[] decryptData(byte[] encryptData){

        ByteArrayOutputStream plaintext = new ByteArrayOutputStream();
        DecryptionStream decryptionStream;
        try{
            synchronized (consumerOptions){
                decryptionStream = PGPainless.decryptAndOrVerify()
                        .onInputStream(new ByteArrayInputStream(encryptData))
                        .withOptions(consumerOptions);
            }
            Streams.pipeAll(decryptionStream, plaintext);
            decryptionStream.close();
        }
        catch (PGPException | IOException e){

        }
        return plaintext.toByteArray();
    }

}
```
---

## Jasypt

### 介绍
Jasypt（Java Simplified Encryption）是一个专注于简化Java加密操作的工具。它提供了一种简单而强大的方式来处理数据的加密和解密，使开发者能够轻松地保护应用程序中的敏感信息，如数据库密码、API密钥等。

Jasypt的设计理念是简化加密操作，使其对开发者更加友好。它采用密码学强度的加密算法，支持多种加密算法，从而平衡了性能和安全性。其中，Jasypt的核心思想之一是基于密码的加密（Password Based Encryption，PBE），通过用户提供的密码生成加密密钥，然后使用该密钥对数据进行加密和解密。

该工具还引入了盐（Salt）的概念，通过添加随机生成的盐值，提高了加密的安全性，防止相同的原始数据在不同的加密过程中产生相同的结果，有效抵御彩虹表攻击。

官网： [http://www.jasypt.org/](http://www.jasypt.org/)


### 项目实战

#### SpringBoot3.x

项目环境

| Framework  | Version |
|------------|---------|
| JDK        | 17      |
| SpringBoot | 3.2.1   |
| maven      | 3.6     |

1. 引入Jasypt依赖, 如果是SpringBoot2.x版本请引入旧的jasypt2.x依赖

```xml
<dependency>
  <groupId>com.github.ulisesbocchio</groupId>
  <artifactId>jasypt-spring-boot</artifactId>
  <version>3.0.5</version>
</dependency>
```

2. 配置文件

常用配置及默认值如下表

| Key	                                       | Required	 | Default Value                       |
|--------------------------------------------|-----------|-------------------------------------|
| jasypt.encryptor.password	                 | True	     | -                                   |
| jasypt.encryptor.algorithm	                | False	    | PBEWITHHMACSHA512ANDAES_256         |
| jasypt.encryptor.key-obtention-iterations	 | False	    | 1000                                |
| jasypt.encryptor.pool-size	                | False	    | 1                                   |
| jasypt.encryptor.provider-name	            | False	    | SunJCE                              |
| jasypt.encryptor.provider-class-name	      | False	    | null                                |
| jasypt.encryptor.salt-generator-classname	 | False	    | org.jasypt.salt.RandomSaltGenerator |
| jasypt.encryptor.iv-generator-classname	   | False	    | org.jasypt.iv.RandomIvGenerator     |
| jasypt.encryptor.string-output-type	       | False	    | base64                              |
| jasypt.encryptor.proxy-property-sources	   | False	    | false                               |
| jasypt.encryptor.skip-property-sources	    | False	    | empty list                          |

```yaml
jasypt:
  encryptor:
    # jasypt密钥
    password: thisIsKey
    #声明加密算法，默认为PBEWITHHMACSHA512ANDAES_256
    algorithm: PBEWithMD5AndDES

# 自定义密文配置的前后缀，默认为ENC()
#    property:
#      prefix: ENC@[
#      suffix: ]

# 使用方法如下，项目启动时jasypt bean会自动创建并自动解密所有符合指定前后缀的密文，解密失败会启动失败（密文产生方法参考下一步）
datasource:
  password: ENC(r/ojbvIxrOVJGv8t3HKsnFJJB9EGZPUW3WU0Nru7lrI=)
```
> 注意， 生产环境jasypt密钥不能直接写死在配置文件中，必须通过其他安全途径获取，如启动命令设置配置变量**java -Djasypt.encryptor.password=thisIsKey ...**

4. jasypt工具方法

此方法用于测试jasypt加解密，使用后应删除，如果需要多次加解密，请将加密和解密过程独立为bean方法进行调用。
```
        StandardPBEStringEncryptor standardPBEStringEncryptor =new StandardPBEStringEncryptor();
        /*配置文件中配置如下的算法*/
        standardPBEStringEncryptor.setAlgorithm("PBEWithMD5AndDES");
        /*配置文件中配置的password*/
        standardPBEStringEncryptor.setPassword("thisIsKey");
        /*配置文件中配置的IvGenerator，如果不填默认为NoIvGenerator，需要在配置文件声明jasypt.encryptor.iv-generator-classname值*/
        standardPBEStringEncryptor.setIvGenerator(new RandomIvGenerator());
        //加密
        String jasyptPasswordEN =standardPBEStringEncryptor.encrypt("thisIsPassword");
        //解密
        String jasyptPasswordDE =standardPBEStringEncryptor.decrypt(jasyptPasswordEN);
        System.out.println("加密后密码："+jasyptPasswordEN);
        System.out.println("解密后密码："+jasyptPasswordDE);
```

5. 项目启动

自动解密所有在配置文件中符合指定前后缀的密文，如数据库密码等，在项目启动时进行解密并用明文进行连接。

```java
@SpringBootTest
public class JasyptBootTest {


    @Autowired
    private Environment environment;

    @Test
    void test(){

        System.out.println(environment.getProperty("datasource.password"));
        //自动解密，输出密码原文
        //thisIsPassword
    }
}
```







