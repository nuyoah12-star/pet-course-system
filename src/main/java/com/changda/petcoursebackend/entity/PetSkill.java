package com.changda.petcoursebackend.entity;

/**
 * 宠物-技能关联实体类
 * 对应数据库 pet_skill 表
 */
public class PetSkill {
    private Integer id;       // 关联记录ID
    private Integer petId;    // 宠物ID
    private Integer skillId;  // 技能ID

    // Getter & Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }
}
