package com.ljm.swagger.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.ljm.swagger.entity.SystemUser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtil {

    /** token秘钥，请勿泄露，请勿随便修改 backups:JKKLJOoasdlfj */
    public static final String SECRET = "JKKLJOoasdlfj";
    /** token 过期时间: 1天 */
    public static final int calendarField = Calendar.DATE;
    public static final int calendarInterval = 1;

    public static String createToken(SystemUser user) throws Exception {
        Date iatDate = new Date();
        // expire time
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(calendarField, calendarInterval);
        Date expiresDate = nowTime.getTime();
        Long id=user.getId();
        String password=user.getPassword();

        // header Map
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        // build token
        // param backups {iss:Service, aud:APP}
        String token = JWT.create().withHeader(map)
                .withAudience(id.toString())// header
                .withClaim("iss", "Service") // payload
                .withClaim("id", null == id ? null : id.toString())
                //.withClaim("aud", "APP")

                //todo 存储roles
                .withIssuedAt(iatDate) // sign time
                .withExpiresAt(expiresDate) // expire time
                //密钥改为密码password
                .sign(Algorithm.HMAC256(password)); // signature

        return token;
    }

}
