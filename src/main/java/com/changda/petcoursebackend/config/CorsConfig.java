package com.changda.petcoursebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类（使用CorsFilter方式，优先级更高、更稳定）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 允许前端地址访问（精准匹配你的Vue前端端口）
        config.addAllowedOriginPattern("http://localhost:5173");

        // 允许携带凭证（Cookie/Token等，必须和前端withCredentials:true对应）
        config.setAllowCredentials(true);

        // 允许所有HTTP请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 2. 创建配置源，对所有接口路径生效
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回CORS过滤器
        return new CorsFilter(source);
    }
}