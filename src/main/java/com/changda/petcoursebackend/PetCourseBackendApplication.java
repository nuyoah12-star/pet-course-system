package com.changda.petcoursebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 宠物课程管理后端启动类
 * 核心配置：扫描Mapper接口、启动Spring Boot应用
 */
@SpringBootApplication
// 关键：扫描所有Mapper接口，替代每个Mapper的@Mapper注解（双重保障）
@MapperScan("com.changda.petcoursebackend.mapper")
public class PetCourseBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetCourseBackendApplication.class, args);
    }

}