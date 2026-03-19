package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.PetSkill;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 宠物-技能关联数据访问接口
 */
@Mapper
public interface PetSkillMapper {

    /**
     * 解锁宠物技能（插入关联记录）
     * @param petSkill 宠物-技能关联对象
     * @return 受影响行数
     */
    @Insert("INSERT INTO pet_skill(pet_id, skill_id) VALUES(#{petId}, #{skillId})")
    int unlockSkill(PetSkill petSkill);

    /**
     * 查询宠物已解锁的所有技能ID
     * @param petId 宠物ID
     * @return 技能ID列表
     */
    @Select("SELECT skill_id FROM pet_skill WHERE pet_id = #{petId}")
    List<Integer> findSkillIdsByPetId(Integer petId);

    /**
     * 校验宠物是否已解锁指定技能（避免重复解锁）
     * @param petId 宠物ID
     * @param skillId 技能ID
     * @return 1=已解锁，0=未解锁（修改返回值为Integer，兼容空值判断）
     */
    @Select("SELECT COUNT(*) FROM pet_skill WHERE pet_id = #{petId} AND skill_id = #{skillId}")
    Integer checkSkillUnlocked(@Param("petId") Integer petId, @Param("skillId") Integer skillId);

    /**
     * 回收宠物技能（删除关联记录）
     * 取消选课时调用，删除宠物对应的指定技能
     * @param petId 宠物ID
     * @param skillId 技能ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM pet_skill WHERE pet_id = #{petId} AND skill_id = #{skillId}")
    int deletePetSkill(@Param("petId") Integer petId, @Param("skillId") Integer skillId);

    /**
     * 批量删除宠物所有技能（可选扩展：宠物删除时调用）
     * @param petId 宠物ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM pet_skill WHERE pet_id = #{petId}")
    int deleteAllSkillsByPetId(Integer petId);
}