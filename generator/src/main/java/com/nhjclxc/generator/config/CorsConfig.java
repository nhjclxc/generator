package com.nhjclxc.generator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 允许跨域CORS
 * 
 * @author LuoXianchao
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        /*是否允许请求带有验证信息*/
        corsConfiguration.setAllowCredentials(true);
        /*允许访问的客户端域名*/
        // corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedOriginPattern("*");//allowedOriginPatterns
        /*允许服务端访问的客户端请求头*/
        corsConfiguration.addAllowedHeader("*");
        /*允许访问的方法名,GET POST等*/
        corsConfiguration.addAllowedMethod("*");
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}

 //        response.setHeader("token", token);
 //        // 跨域获取不到请求头设置 在登录的请求头里面加上如下的语句
 //        response.addHeader("Access-Control-Allow-Headers", "Content-Type,token");
 //        response.addHeader("Access-Control-Expose-Headers","Content-Type,token"); 