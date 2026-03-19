package com.changda.petcoursebackend.entity;

import lombok.Data;

/**
 * 课程实体类，与数据库course表字段对应
 * 字段说明：覆盖课程基础信息、难度、训练师、评分、封面、适配宠物等核心属性
 */
@Data // Lombok注解，自动生成getter/setter/toString/hashCode/equals等方法
public class Course {
    private Integer id;         // 课程ID（主键，自增）
    private String name;        // 课程名称（如：坐下训练、握手训练）
    private String description; // 课程描述（课程详细介绍）
    private Integer difficulty; // 课程难度（1-简单，2-中等，3-困难）
    private String trainer;     // 训练师名称（如：训练师A、训练师B）
    private Double avgScore;    // 课程平均分（核心字段：统计用户评分的平均值）
    private String cover;       // 课程封面图片URL（如：http://localhost:8080/uploads/41.jpg）
    private String petType;     // 课程适合的宠物类型（如："狗"/"猫"/"通用"）
    private Integer minAge;     // 课程适合的宠物最小年龄（单位：月）
    private Integer maxAge;     // 课程适合的宠物最大年龄（单位：月）
}