//package com.nhjclxc.generator.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.service.Contact;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
///**
// * Swagger2配置类
// * 在与spring boot集成时，放在与Application.java同级的目录下。
// * 通过@Configuration注解，让Spring来加载该类配置。
// * 再通过@EnableSwagger2注解来启用Swagger2。
// */
//@Configuration
//@EnableSwagger2
//public class Swagger2 implements WebMvcConfigurer {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations(
//                "classpath:/static/");
//        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
//                "classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations(
//                "classpath:/META-INF/resources/webjars/");
//        WebMvcConfigurer.super.addResourceHandlers(registry);
//    }
//
//    /**
//     * 创建API应用
//     * apiInfo() 增加API相关信息
//     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
//     * 本例采用指定扫描的包路径来定义指定要建立API的目录。
//     *
//     * @return
//     */
//    @Bean
//    public Docket createRestApi() {
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(apiInfo())
//                .select()
//                //扫描controller层所在的位置
//                .apis(RequestHandlerSelectors.basePackage("com.nhjclxc.generator.controller"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//
//    /**
//     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
//     * 访问地址：http://项目实际地址/swagger-ui.html
//     * @return
//     */
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("根据后端表结构，生成后端SpringBoot和前端Vue组件相关代码")
//                .description("根据后端表结构，生成后端SpringBoot和前端Vue组件相关代码")
//                // .termsOfServiceUrl("团队的链接联系地址，例如https://github.com/nhjclxc/generator 也可以不写不是必须的")
//                .contact(new Contact("访问原始仓库","https://github.com/nhjclxc/generator","nhjclxc@163.com"))
//                .version("1.0")
//                .build();
//    }
//}