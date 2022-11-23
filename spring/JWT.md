<a name="1acab1f6"></a>
## 引入maven

```
<dependency>
      <groupId>com.auth0</groupId>
      <artifactId>java-jwt</artifactId>
      <version>3.4.0</version>
</dependency>
```

<a name="83f03fb5"></a>
## 自定义两个注解

<a name="e9927748"></a>
### 用来跳过验证的  **_PassToken_**

```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PassToken {
    boolean required() default true;
}
```

<a name="1a5a6048"></a>
### 需要登录才能进行操作的注解  **_UserLoginToken_**

```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserLoginToken {
    boolean required() default true;
}
```

<a name="dd7571fd"></a>
## Token创建

```
/** token秘钥，请勿泄露，请勿随便修改 backups:JKKLJOoasdlfj */
    public static final String SECRET = "JKKLJOoasdlfj";
    /** token 过期时间: 1天 */
    public static final int calendarField = Calendar.DATE;
    public static final int calendarInterval = 1;
	
	public static String createToken(UserDTO user) throws Exception {
        Date iatDate = new Date();
        // expire time
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(calendarField, calendarInterval);
        Date expiresDate = nowTime.getTime();
        Integer id=user.getId();
        String password=user.getPassword();

        // header Map
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");

        // build token
        // param backups {iss:Service, aud:APP}
        String token = JWT.create().withHeader(map)
                .withAudience(Integer.toString(id))// header
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
```

<a name="f7ae864d"></a>
## 拦截器

```
/**
 * @description: token拦截器
 * @author: lijianming
 * @create: 2020-04-13 18:47
 **/

public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        String token = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        //检查有没有需要用户权限的注解
        if (method.isAnnotationPresent(UserLoginToken.class)) {
            UserLoginToken userLoginToken = method.getAnnotation(UserLoginToken.class);
            if (userLoginToken.required()) {
                // 执行认证
                if (token == null) {
                    throw new RuntimeException("无token，请重新登录");
                }
                // 获取 token 中的 user id
                String userId;

                try {
                    userId = JWT.decode(token).getAudience().get(0);
                } catch (JWTDecodeException j) {
                    throw new RuntimeException("401");
                }
                UserDTO user = userService.findUserById(Integer.parseInt(userId));
                if (user == null) {
                    throw new RuntimeException("用户不存在，请重新登录");
                }


                // 验证 token
                JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
                try {
                    jwtVerifier.verify(token);
                } catch (JWTVerificationException e) {
                    throw new RuntimeException("401");
                }

                Date overTime = JWT.decode(token).getExpiresAt();
                Date currentTime = new Date();
                long fireTime = overTime.getTime()-currentTime.getTime();
                long fireMinute = fireTime/(1000*60);
//                if(fireMinute < 5L){
//                    Cookie cookie = new Cookie("tokenStatus", "");
//                    cookie.setMaxAge(10);
//                    //System.out.println("you come here!");
//                    token = TokenUtil.createToken(Integer.parseInt(userId));
//                    httpServletResponse.setHeader("tokenStatus",token);
//                    httpServletResponse.addCookie(cookie);
//                }
                return true;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}
```

<a name="0947e10a"></a>
## 配置拦截器

```
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor())
                .addPathPatterns("/**");   
    }
    @Bean
    public AuthenticationInterceptor authenticationInterceptor() {
        return new AuthenticationInterceptor();
    }
}
```
