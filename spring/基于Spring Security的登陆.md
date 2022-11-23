参考Git：[https://github.com/Snailclimb/spring-security-jwt-guide](https://github.com/Snailclimb/spring-security-jwt-guide)<br />关键字：Spring Security；Redis；RSA；JWT；Role；

<a name="Xy12p"></a>
## 流程图例
> 流程一览

![](https://cdn.nlark.com/yuque/0/2022/jpeg/1728234/1652279742571-a6daecfd-2339-4427-a35f-32200b22fba7.jpeg#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=udba18901&margin=%5Bobject%20Object%5D&originHeight=179&originWidth=640&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u2448463f-5f47-4d83-95f4-f682120207a&title=)
> 认证流程

![](https://cdn.nlark.com/yuque/0/2022/jpeg/1728234/1652279806942-7643864d-1dc2-4069-9379-f66dfc0ec746.jpeg#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&id=uae237c45&margin=%5Bobject%20Object%5D&originHeight=882&originWidth=640&originalType=url&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u73b68577-d918-4ee4-8b83-75fb68893f9&title=)
<a name="yPeOe"></a>
## 依赖引入
| **名称** | **版本** |
| --- | --- |
| spring-boot-starter-security | 2.4.0 |
| spring-boot-starter-parent | 2.4.0 |
| spring-boot-starter-web | 2.4.0 |
| jjwt-api | 0.10.7 |
| jjwt-impl | 0.10.7 |
| jjwt-jackson | 0.10.7 |
| guava | 29.0-jre |
| spring-boot-starter-data-redis | 2.4.0 |

<a name="MbKyv"></a>
## 表结构
> User

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1652280738520-49ee23c8-2a7a-49eb-af41-2547add42396.png#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=182&id=u16304288&margin=%5Bobject%20Object%5D&name=image.png&originHeight=182&originWidth=156&originalType=binary&ratio=1&rotation=0&showTitle=false&size=4959&status=done&style=none&taskId=u534305b0-f0ba-4cf8-84cf-05454fef0e8&title=&width=156)
> Role

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1652280763141-0e1a6923-d60c-4179-b4fd-7afaed7dda45.png#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=143&id=ucbcb7270&margin=%5Bobject%20Object%5D&name=image.png&originHeight=143&originWidth=160&originalType=binary&ratio=1&rotation=0&showTitle=false&size=3912&status=done&style=none&taskId=u76ce4d59-fb59-49eb-aaef-47e40d6a099&title=&width=160)
> User_Role

![image.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1652280792324-6d656e14-8453-4922-a018-7b90b84f05fb.png#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=146&id=u1b367d56&margin=%5Bobject%20Object%5D&name=image.png&originHeight=146&originWidth=172&originalType=binary&ratio=1&rotation=0&showTitle=false&size=3994&status=done&style=none&taskId=uedaff6a6-65d7-4682-b444-b9c6b7e0c26&title=&width=172)

<a name="nacXt"></a>
## 项目详解
> 项目结构

![structure.png](https://cdn.nlark.com/yuque/0/2022/png/1728234/1652282017733-f5930088-edc9-4e5e-9523-eac6c0bdd05e.png#clientId=u0fb1db18-63af-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=1334&id=ua499a17e&margin=%5Bobject%20Object%5D&name=structure.png&originHeight=1334&originWidth=2878&originalType=binary&ratio=1&rotation=0&showTitle=false&size=465259&status=done&style=none&taskId=u54578db2-88bb-453c-b66b-f85652c7401&title=&width=2878)

> 登录对象类

_/**<br /> * _**_@description _**_用户登录请求DTO<br /> */<br />_@Data<br />@AllArgsConstructor<br />public class LoginRequest {<br />    private String username;<br />    private String password;<br />    private Boolean rememberMe;<br />}

> JWT对象类

public class JwtUser implements UserDetails {

    private Long id;<br />    private String username;<br />    private String password;<br />    private Boolean enabled;<br />    private Collection<? extends GrantedAuthority> authorities;

    public JwtUser() {<br />    }

    _/**<br />     * 通过 user 对象创建jwtUser<br />     */<br />    _public JwtUser(User user) {<br />        id = user.getId();<br />        username = user.getUserName();<br />        password = user.getPassword();<br />        enabled = user.getEnabled() == null ? true : user.getEnabled();<br />        authorities = user.getRoles();<br />    }<br /> //获取角色列表<br />    @Override<br />    public Collection<? extends GrantedAuthority> getAuthorities() {<br />        return authorities;<br />    }

    @Override<br />    public String getPassword() {<br />        return password;<br />    }

    @Override<br />    public String getUsername() {<br />        return username;<br />    }

    @Override<br />    public boolean isAccountNonExpired() {<br />        return true;<br />    }

    @Override<br />    public boolean isAccountNonLocked() {<br />        return true;<br />    }

    @Override<br />    public boolean isCredentialsNonExpired() {<br />        return true;<br />    }

    @Override<br />    public boolean isEnabled() {<br />        return this.enabled;<br />    }

}

> JWT工具类

_/**<br /> * _**_@author _**_shuang.kou<br /> * _**_@description _**_JWT工具类<br /> */<br />_public class JwtTokenUtils {

    //keytool -genkey -alias jwt -keyalg RSA -keystore jwt.jks

    _/**<br />     * 生成足够的安全随机密钥，以适合符合规范的签名<br />     */<br />    _private static final byte[] _API_KEY_SECRET_BYTES _= DatatypeConverter._parseBase64Binary_(SecurityConstants._JWT_SECRET_KEY_);<br />    private static final SecretKey _SECRET_KEY _= Keys._hmacShaKeyFor_(_API_KEY_SECRET_BYTES_);

    private static InputStream _inputStream _= Thread._currentThread_().getContextClassLoader().getResourceAsStream("jwt.jks"); // 寻找证书文件<br />    private static PrivateKey _privateKey _= null;<br />    private static PublicKey _publicKey _= null;

    static { // 将证书文件里边的私钥公钥拿出来<br />        try {<br />//            private static InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jwt.jks"); // 寻找证书文件<br />            String jksPath = "D:\\workspace\\spring-security-jwt-guide-master\\src\\main\\resources\\jwt.jks"; //jks file path<br />            String jksPassword = "123456"; // jks keyStore password<br />            String certAlias = "jwt"; // cert alias<br />            String certPassword = "123456"; // cert password<br />            KeyStore keyStore = KeyStore._getInstance_("JKS");<br />            keyStore.load(new FileInputStream(jksPath), jksPassword.toCharArray());<br />//取出其中的公钥私钥，实际上一般分开操作保证安全<br />            _privateKey _= (PrivateKey) keyStore.getKey(certAlias, certPassword.toCharArray());<br />            _publicKey _= keyStore.getCertificate(certAlias).getPublicKey();<br />        } catch (Exception e) {<br />            e.printStackTrace();<br />        }<br />    }

    public static String createToken(String username, String id, List<String> roles, boolean isRememberMe) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {<br />	<br />//根据是否记住账密判断过期时间<br />        long expiration = isRememberMe ? SecurityConstants._EXPIRATION_REMEMBER _: SecurityConstants._EXPIRATION_;<br />        final Date createdDate = new Date();<br />        final Date expirationDate = new Date(createdDate.getTime() + expiration * 1000);<br />        String tokenPrefix = Jwts._builder_()<br />                .setHeaderParam("type", SecurityConstants._TOKEN_TYPE_)<br />//私钥加密，设置加密类型（对称加密可选HS256）<br />                .signWith(_privateKey_, SignatureAlgorithm._RS256_)<br />//设置角色，以 '_ROLE_CLAIMS_' 为key<br />                .claim(SecurityConstants._ROLE_CLAIMS_, String._join_(",", roles))<br />                .setId(id)<br />                .setIssuer("SnailClimb")<br />                .setIssuedAt(createdDate)<br />                .setSubject(username)<br />//设置过期时间<br />                .setExpiration(expirationDate)<br />                .compact();<br />        return SecurityConstants._TOKEN_PREFIX _+ tokenPrefix; // 添加 token 前缀 "Bearer ";<br />    }

    public static String getId(String token) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {<br />        Claims claims = _getClaims_(token);<br />        return claims.getId();<br />    }

    public static UsernamePasswordAuthenticationToken getAuthentication(String token) throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {<br />        Claims claims = _getClaims_(token);<br />        List<SimpleGrantedAuthority> authorities = _getAuthorities_(claims);<br />        String userName = claims.getSubject();<br />        return new UsernamePasswordAuthenticationToken(userName, token, authorities);<br />    }<br />/**      * 获取用户所有角色      */<br />    private static List<SimpleGrantedAuthority> getAuthorities(Claims claims) {<br />        String role = (String) claims.get(SecurityConstants._ROLE_CLAIMS_);<br />        return Arrays._stream_(role.split(","))<br />                .map(SimpleGrantedAuthority::new)<br />                .collect(Collectors._toList_());<br />    }<br />//判断token是否过期<br />private boolean isTokenExpired(String token) {         <br />Date expiredDate = getClaims(token).getExpiration();         <br />return expiredDate.before(new Date());    <br /> }<br />//解密token<br />    private static Claims getClaims(String token){<br />        return Jwts._parser_()<br />                .setSigningKey(_privateKey_)<br />                .parseClaimsJws(token)<br />                .getBody();<br />    }

}

> Security配置类



_/**<br /> * _**_@version _**_1.1<br /> * _**_@date _**_2020.11.28 14:16<br /> * _**_@description _**_Spring Security配置类<br /> **/<br />_@EnableWebSecurity<br />@EnableGlobalMethodSecurity(prePostEnabled = true)<br />public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final StringRedisTemplate stringRedisTemplate;

    public SecurityConfiguration(StringRedisTemplate stringRedisTemplate) {<br />        this.stringRedisTemplate = stringRedisTemplate;<br />    }

    _/**<br />     * 密码编码器<br />     */<br />    _@Bean<br />    public BCryptPasswordEncoder bCryptPasswordEncoder() {<br />        return new BCryptPasswordEncoder();<br />    }

    @Override<br />    protected void configure(HttpSecurity http) throws Exception {<br />        http.cors(_withDefaults_())<br />                // 禁用 CSRF<br />                .csrf().disable()<br />                .authorizeRequests()<br />                // 指定的接口直接放行<br />                // swagger<br />                .antMatchers(SecurityConstants._SWAGGER_WHITELIST_).permitAll()<br />                .antMatchers(SecurityConstants._H2_CONSOLE_).permitAll()<br />                .antMatchers("/auth/test").permitAll()<br />                .antMatchers(HttpMethod._POST_, SecurityConstants._SYSTEM_WHITELIST_).permitAll()<br />                // 其他的接口都需要认证后才能请求<br />                .anyRequest().authenticated()<br />                .and()<br />                //添加自定义Filter，传入redis以供后续使用<br />                .addFilter(new JwtAuthorizationFilter(authenticationManager(), stringRedisTemplate))<br />                //.addFilter(new TestFilter(authenticationManager()))

                // 不需要session（不创建会话）<br />                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy._STATELESS_).and()<br />                // 授权异常处理<br />                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())<br />                .accessDeniedHandler(new JwtAccessDeniedHandler());<br />        // 防止H2 web 页面的Frame 被拦截<br />        http.headers().frameOptions().disable();<br />    }

    _/**<br />     * Cors配置优化<br />     **/<br />    _@Bean<br />    CorsConfigurationSource corsConfigurationSource() {<br />        org.springframework.web.cors.CorsConfiguration configuration = new CorsConfiguration();<br />        configuration.setAllowedOrigins(_singletonList_("*"));<br />        // configuration.setAllowedOriginPatterns(singletonList("*"));<br />        configuration.setAllowedHeaders(_singletonList_("*"));<br />        configuration.setAllowedMethods(Arrays._asList_("GET", "POST", "DELETE", "PUT", "OPTIONS"));<br />        configuration.setExposedHeaders(_singletonList_(SecurityConstants._TOKEN_HEADER_));<br />        configuration.setAllowCredentials(false);<br />        configuration.setMaxAge(3600L);<br />        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();<br />        source.registerCorsConfiguration("/**", configuration);<br />        return source;<br />    }

}

> 过滤器Filter

_/**<br /> * _**_@author _**_shuang.kou<br /> * _**_@description _**_过滤器处理所有HTTP请求，并检查是否存在带有正确令牌的Authorization标头。例如，如果令牌未过期或签名密钥正确。<br /> */<br />_@Slf4j<br />public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final StringRedisTemplate stringRedisTemplate;

//构造器<br />    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, StringRedisTemplate stringRedisTemplate) {<br />        super(authenticationManager);<br />        this.stringRedisTemplate = stringRedisTemplate;<br />    }

    @Override<br />    protected void doFilterInternal(HttpServletRequest request,<br />                                    HttpServletResponse response,<br />                                    FilterChain chain) throws IOException, ServletException {

        String token = request.getHeader(SecurityConstants._TOKEN_HEADER_);<br />//判断token是否存在<br />        if (token == null || !token.startsWith(SecurityConstants._TOKEN_PREFIX_)) {<br />            SecurityContextHolder._clearContext_();<br />            chain.doFilter(request, response);<br />            return;<br />        }<br />        String tokenValue = token.replace(SecurityConstants._TOKEN_PREFIX_, "");<br />        UsernamePasswordAuthenticationToken authentication = null;<br />        try {<br />//对比redis存储的token<br />            String previousToken = stringRedisTemplate.opsForValue().get(JwtTokenUtils._getId_(tokenValue));<br />            if (!token.equals(previousToken)) {<br />                SecurityContextHolder._clearContext_();<br />                chain.doFilter(request, response);<br />                return;<br />            }<br />            String userId = JwtTokenUtils._getId_(tokenValue);<br />            authentication = JwtTokenUtils._getAuthentication_(tokenValue);<br />        } catch (JwtException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {<br />            logger.error("Invalid jwt : " + e.getMessage());<br />        }<br />//设置角色权限至上下文<br />        SecurityContextHolder._getContext_().setAuthentication(authentication);<br />        chain.doFilter(request, response);<br />    }<br />}、


<a name="cPCV0"></a>
## 接口进行权限认证
> 默认角色权限会加上ROLE_前缀

@GetMapping<br />@PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")<br />@ApiOperation("获取所有用户的信息（分页）")<br />public ResponseEntity<Page<UserRepresentation>> getAllUser(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {<br />    Page<UserRepresentation> allUser = userService.getAll(pageNum, pageSize);<br />    return ResponseEntity._ok_().body(allUser);<br />}
