package com.changda.petcoursebackend.entity;

import lombok.Data;

/**
 * 宠物成长实体（新增petId字段，关联宠物唯一ID）
 * 保留原有字段，仅新增petId，完全兼容历史代码
 */
@Data
public class PetGrowth {
    private Integer id;         // 主键ID（原有）
    private Integer petId;      // 新增：关联宠物的唯一ID（核心匹配字段）
    private String petName;     // 原有：宠物名称（仅用于显示）
    private Integer level;      // 原有：宠物等级
    private Integer exp;        // 原有：宠物经验值
}