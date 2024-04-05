package com.nhjclxc.generator.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @author LuoXianchao
 */
@Configuration
@ComponentScan("com.nhjclxc.generator.controller")
public class InterceptorConfig extends WebMvcConfigurationSupport {

    private final MyInterceptor myInterceptor;

    public InterceptorConfig(MyInterceptor myInterceptor) {
        this.myInterceptor = myInterceptor;
    }

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        ////把刚刚写的拦截器类对象传进去
        registry.addInterceptor(myInterceptor)
                .addPathPatterns("/**")//拦截所有请求
        .excludePathPatterns("/gen/connect");//不拦截的请求
    }

  	// 如果项目中有配置swagger，必须添加如下代码
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(
                "classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations(
                "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                "classpath:/META-INF/resources/webjars/");
        super.addResourceHandlers(registry);
    }
}
