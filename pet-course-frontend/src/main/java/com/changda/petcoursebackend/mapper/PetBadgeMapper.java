package com.changda.petcoursebackend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PetBadgeMapper {
    // 原有方法：保留（用于颁发勋章）
    @Insert("insert into pet_badge(pet_id, badge_id) values(#{petId}, #{badgeId})")
    void addBadge(@Param("petId") Integer petId, @Param("badgeId") Integer badgeId);

    // 新增核心方法：根据宠物ID查询该宠物已获得的所有勋章详情
    @Select("""
        SELECT b.id, b.name, b.description
        FROM pet_badge pb
        JOIN badge b ON pb.badge_id = b.id
        WHERE pb.pet_id = #{petId}
    """)
    List<Map<String, Object>> findBadgesByPetId(@Param("petId") Integer petId);

    // ========== 新增核心方法：删除宠物原有所有勋章（适配升级/降级逻辑） ==========
    /**
     * 删除指定宠物的所有勋章记录
     * 作用：避免一个宠物有多枚勋章，实现勋章升级/降级时的覆盖效果
     */
    @Delete("DELETE FROM pet_badge WHERE pet_id = #{petId}")
    void deleteBadgeByPetId(@Param("petId") Integer petId);
}