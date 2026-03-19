package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Pet;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 宠物Mapper（注解版，无XML）
 * 处理宠物相关的数据库操作
 * 新增：头像字段（avatar）的查询/更新方法
 * 优化：自动升级逻辑 + 经验更新方法
 */
@Mapper
public interface PetMapper {

    /**
     * 查询所有宠物（包含头像字段）
     * ✨ 优化：补充所有实体字段，避免前端缺失type/age/weight等信息
     * @return 宠物列表
     */
    @Select("SELECT id, name, type, breed, age, weight, owner_id AS ownerId, exp, level, avatar FROM pet")
    List<Pet> findAll();

    /**
     * 插入新宠物（包含头像字段，默认null）
     * @param pet 宠物实体
     */
    @Insert("INSERT INTO pet(name, type, breed, age, weight, owner_id, exp, level, avatar) " +
            "VALUES(#{name}, #{type}, #{breed}, #{age}, #{weight}, #{ownerId}, #{exp}, #{level}, #{avatar})")
    void insert(Pet pet);

    /**
     * 选课成功后固定增加经验（+10）
     * @param petId 宠物ID
     */
    @Update("UPDATE pet SET exp = exp + 10 WHERE id = #{petId}")
    void addExp(Integer petId);

    /**
     * 核心修改：经验达标自动升级（经验≥1000升1级）
     * @param petId 宠物ID
     */
    @Update("UPDATE pet SET level = level + 1 WHERE exp >= 1000 AND id = #{petId}")
    void levelUp(Integer petId);

    /**
     * 新增：按指定经验值增加（适配任务完成场景）
     * @param petId 宠物ID
     * @param exp 要增加的经验值（任务奖励）
     */
    @Update("UPDATE pet SET exp = exp + #{exp} WHERE id = #{petId}")
    void addExpByExp(@Param("petId") Integer petId, @Param("exp") Integer exp);

    /**
     * 原有：根据ID查询单个宠物（包含头像字段）
     * ✨ 优化：补充所有实体字段，保持和findAll一致
     * @param petId 宠物ID
     * @return 宠物详情
     */
    @Select("SELECT id, name, type, breed, age, weight, owner_id AS ownerId, exp, level, avatar FROM pet WHERE id = #{petId}")
    Pet findById(Integer petId);

    /**
     * 新增：兼容selectById方法（和findById逻辑一致，适配TaskController）
     * ✨ 优化：补充所有实体字段
     * @param id 宠物ID
     * @return 宠物详情
     */
    @Select("SELECT id, name, type, breed, age, weight, owner_id AS ownerId, exp, level, avatar FROM pet WHERE id = #{id}")
    Pet selectById(Integer id);

    /**
     * 宠物排行榜：按经验降序取前10名（包含头像字段）
     * ✨ 优化：补充所有实体字段
     * @return 排名前10的宠物列表
     */
    @Select("SELECT id, name, type, breed, age, weight, owner_id AS ownerId, exp, level, avatar FROM pet order by exp desc limit 10")
    List<Pet> rankList();

    // ========== 核心新增：根据ID更新宠物（PetService依赖） ==========
    /**
     * 根据ID更新宠物信息（支持经验/等级/名称/头像等字段更新）
     * @param pet 宠物实体（必须包含id字段）
     * @return 受影响的行数（0=更新失败，1=更新成功）
     */
    @Update("UPDATE pet SET " +
            "name = #{name}, " +
            "type = #{type}, " +
            "breed = #{breed}, " +
            "age = #{age}, " +
            "weight = #{weight}, " +
            "owner_id = #{ownerId}, " +
            "exp = #{exp}, " +
            "level = #{level}, " +
            "avatar = #{avatar} " + // 新增：头像字段更新
            "WHERE id = #{id}")
    int updateById(Pet pet);

    // ========== 优化：自动升级逻辑（修复原levelUp的漏洞） ==========
    /**
     * 智能升级：根据当前经验自动计算等级（每100经验升1级，而非仅≥1000）
     * @param petId 宠物ID
     * @param newLevel 计算后的新等级
     */
    @Update("UPDATE pet SET level = #{newLevel} WHERE id = #{petId}")
    void updateLevel(@Param("petId") Integer petId, @Param("newLevel") Integer newLevel);

    // ========== 核心调整：更新宠物头像方法（参数名和PetController对齐） ==========
    /**
     * 更新宠物头像地址（参数名改为petId，和Controller保持一致，避免绑定错误）
     * @param petId 宠物ID
     * @param avatar 头像文件名/访问路径
     */
    @Update("UPDATE pet SET avatar = #{avatar} WHERE id = #{petId}")
    void updateAvatar(@Param("petId") Integer petId, @Param("avatar") String avatar);
}