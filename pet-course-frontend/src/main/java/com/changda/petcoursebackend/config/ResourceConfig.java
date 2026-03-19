package com.changda.petcoursebackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(ResourceConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的uploads绝对路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        // 注册映射：前端访问 /uploads/** 映射到本地uploads目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(0); // 禁用缓存，立即显示新头像
        logger.info("静态资源映射成功：/uploads/** -> {}", uploadPath);
    }
}