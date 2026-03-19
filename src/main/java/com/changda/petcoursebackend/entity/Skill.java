package com.changda.petcoursebackend.entity;

/**
 * 宠物技能实体类
 * 对应数据库 skill 表
 */
public class Skill {
    private Integer id;         // 技能ID
    private String name;       // 技能名称
    private String description;// 技能描述
    private Integer parentId;  // 前置技能ID（对应数据库parent_id）

    // Getter & Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }
}
