package com.changda.petcoursebackend.entity;

import lombok.Data;

/**
 * 宠物实体类，对应数据库pet表
 */
@Data
public class Pet {
    // 原有字段
    private Integer id;      // 宠物ID（主键）
    private String name;     // 宠物名称
    private String type;     // 宠物类型（猫/狗等）
    private String breed;    // 宠物品种
    private Integer age;     // 宠物年龄
    private Double weight;   // 宠物体重
    private Integer ownerId; // 主人ID

    // 新增：经验值和等级字段
    private Integer exp;     // 宠物经验值
    private Integer level;   // 宠物等级

    // 核心新增：头像文件名字段（必须加）
    private String avatar;   // 宠物头像文件名
}