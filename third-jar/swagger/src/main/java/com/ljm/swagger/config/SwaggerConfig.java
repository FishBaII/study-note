package com.ljm.swagger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ListVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableOpenApi
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.OAS_30).apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {

        List<String> list = new ArrayList<>();
        list.add("test1");
        list.add("test2");
        list.add("test3");

        VendorExtension vendorExtension = new ListVendorExtension("test", list);
        List<VendorExtension> vendorExtensions = new ArrayList<>();
        vendorExtensions.add(vendorExtension);



        return new ApiInfoBuilder()
                .title("swagger项目接口文档") // 文档标题
                .description("基本的一些接口说明") // 文档基本描述
                .contact(new Contact("jayce", "https://www.baidu.com", "xxxx@qq.com")) // 联系人信息
                .termsOfServiceUrl("http://terms.service.url/组织链接") // 组织链接
                .version("1.0") // 版本
                .license("Apache 2.0 许可") // 许可
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0") // 许可链接
                .extensions(vendorExtensions) // 拓展
                .build();
    }

}
