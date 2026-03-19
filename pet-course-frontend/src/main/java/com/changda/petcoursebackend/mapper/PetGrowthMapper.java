package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.PetGrowth;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 宠物成长Mapper（注解版，无XML）
 * 修复：TooManyResultsException（查询加LIMIT 1）
 * 新增：按petId查询/创建方法（核心解决名称匹配问题）
 * 保留：原有按名称操作的方法（兼容历史逻辑）
 */
@Mapper
public interface PetGrowthMapper {

    // ==================== 原有方法（保留，兼容历史逻辑） ====================
    /**
     * 按宠物名查询成长记录（加LIMIT 1，避免TooManyResultsException）
     * @param petName 宠物名（如"旺财"）
     * @return 成长记录
     */
    @Select("SELECT id, pet_name, level, exp FROM pet_growth WHERE pet_name = #{petName} LIMIT 1")
    PetGrowth findByPet(String petName);

    /**
     * 创建宠物成长记录（初始等级1，经验0）
     * @param petName 宠物名
     */
    @Insert("INSERT INTO pet_growth(pet_name, level, exp) VALUES(#{petName}, 1, 0)")
    void create(String petName);

    /**
     * 核心修复：按ID更新成长记录（替代按宠物名更新）
     * 避免宠物名匹配失败导致更新无效
     * @param petGrowth 成长记录（必须包含id、level、exp）
     */
    @Update("UPDATE pet_growth SET level = #{level}, exp = #{exp} WHERE id = #{id}")
    void update(PetGrowth petGrowth);

    // ==================== 新增方法（核心解决名称匹配问题 + 修复异常） ====================
    /**
     * 按宠物ID查询成长记录（加LIMIT 1，避免TooManyResultsException）
     * @param petId 宠物唯一ID（如1=可乐，2=糯米，3=旺财）
     * @return 成长记录
     */
    @Select("SELECT id, pet_id, pet_name, level, exp FROM pet_growth WHERE pet_id = #{petId} LIMIT 1")
    PetGrowth findByPetId(Integer petId);

    /**
     * 按宠物ID创建成长记录（初始化时关联petId，避免名称问题）
     * @param petId 宠物唯一ID
     * @param petName 宠物名称（仅用于显示）
     */
    @Insert("INSERT INTO pet_growth(pet_id, pet_name, level, exp) VALUES(#{petId}, #{petName}, 1, 0)")
    void createByPetId(@Param("petId") Integer petId, @Param("petName") String petName);
}