package com.ljm.swagger.config;

import io.swagger.models.auth.In;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket docketHi() {
        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
                .groupName("hi")
                // 配置扫描的接口
                .select()
                // 配置扫描指定包的接口
                //RequestHandlerSelectors配置扫描路径的一些方法
                // 扫描所有，项目中的所有接口都会被扫描到
                //any()
                // 不扫描接口
                //none()
                // 通过方法上的注解扫描，如withMethodAnnotation(GetMapping.class)只扫描get请求
                //withMethodAnnotation(final Class<? extends Annotation> annotation)
                // 通过类上的注解扫描，如.withClassAnnotation(Controller.class)只扫描有controller注解的类中的接口
                // withClassAnnotation(final Class<? extends Annotation> annotation)
                // 根据包路径扫描接口
                //basePackage(final String basePackage)
                .apis(RequestHandlerSelectors.basePackage("com.ljm.swagger.controller"))

                // 过滤请求，只扫描请求以自定义开头的接口
                //any() 任何请求都扫描
                //none() 任何请求都不扫描
                //regex(final String pathRegex) 通过正则表达式扫描
                //ant(final String antPattern) 通过ant()指定请求扫描
                .paths(PathSelectors.ant("/hi/**"))
                .build()

                // 设置是否启动Swagger，默认为true（不写即可），关闭后Swagger就不生效了
                .enable(true)
                ;
    }

    @Bean
    public Docket docketOrder() {

        RequestParameter parameter = new RequestParameterBuilder()
                //参数名称
                .name("token")
                //描述
                .description("authorize with token")
                //放在header中
                .in(ParameterType.HEADER)
                //是否必传
                .required(true)
                .build();
        //构建一个请求参数集合
        List<RequestParameter> parameters = Collections.singletonList(parameter);

        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo())
                .groupName("order")
                // 配置扫描的接口
                .select()
                // 配置扫描指定包的接口
                //RequestHandlerSelectors配置扫描路径的一些方法
                // 扫描所有，项目中的所有接口都会被扫描到
                //any()
                // 不扫描接口
                //none()
                // 通过方法上的注解扫描，如withMethodAnnotation(GetMapping.class)只扫描get请求
                //withMethodAnnotation(final Class<? extends Annotation> annotation)
                // 通过类上的注解扫描，如.withClassAnnotation(Controller.class)只扫描有controller注解的类中的接口
                // withClassAnnotation(final Class<? extends Annotation> annotation)
                // 根据包路径扫描接口
                //basePackage(final String basePackage)
                .apis(RequestHandlerSelectors.basePackage("com.ljm.swagger.controller"))

                // 过滤请求，只扫描请求以自定义开头的接口
                //any() 任何请求都扫描
                //none() 任何请求都不扫描
                //regex(final String pathRegex) 通过正则表达式扫描
                //ant(final String antPattern) 通过ant()指定请求扫描
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                //.globalRequestParameters(parameters)
                ;
    }

    private ApiInfo apiInfo() {

        return new ApiInfoBuilder()
                .title("swagger项目接口文档") // 文档标题
                .description("基本的一些接口说明") // 文档基本描述
                .contact(new Contact("jayce", "https://www.baidu.com", "xxxx@qq.com")) // 联系人信息
                .termsOfServiceUrl("http://terms.service.url/组织链接") // 组织链接
                .version("1.0") // 版本
                .license("Apache 2.0 许可") // 许可
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0") // 许可链接
                .extensions(new ArrayList<>()) // 拓展
                .build();
    }


    /**
     * 设置授权信息
     */
    private List<SecurityScheme> securitySchemes() {
        ApiKey apiKey = new ApiKey("token", "token", In.HEADER.toValue());
        return Collections.singletonList(apiKey);
    }

    /**
     * 授权信息全局应用
     */
    private List<SecurityContext> securityContexts() {
        return Collections.singletonList(
                SecurityContext.builder()
                        .securityReferences(Collections.singletonList(new SecurityReference("token", new AuthorizationScope[]{new AuthorizationScope("global", "")})))
                        .build()
        );

    }

}
