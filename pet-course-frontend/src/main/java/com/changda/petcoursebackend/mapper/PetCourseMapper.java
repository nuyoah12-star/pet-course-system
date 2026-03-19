package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.PetCourse;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper // 关键：必须加
public interface PetCourseMapper {

    // 选课插入
    @Insert("INSERT INTO pet_course(pet_id,course_id,select_time,progress) VALUES(#{petId},#{courseId},#{selectTime},#{progress})")
    void selectCourse(PetCourse petCourse);

    // 取消选课
    @Delete("DELETE FROM pet_course WHERE pet_id=#{petId} AND course_id=#{courseId}")
    void cancelCourse(@Param("petId") Integer petId, @Param("courseId") Integer courseId);

    // 查询宠物选课记录
    @Select("SELECT * FROM pet_course WHERE pet_id = #{petId}")
    List<PetCourse> findByPetId(Integer petId);

    // 查询宠物选课详情（含课程信息）
    @Select("SELECT c.id, c.name, c.trainer, c.description, pc.select_time as selectTime, pc.progress " +
            "FROM course c JOIN pet_course pc ON c.id = pc.course_id WHERE pc.pet_id = #{petId}")
    List<Map<String, Object>> findCoursesWithEnrollInfoByPetId(Integer petId);

    // 原有新增：统计宠物已选课数量（适配勋章逻辑）
    @Select("SELECT COUNT(*) FROM pet_course WHERE pet_id = #{petId}")
    int countByPetId(Integer petId);

    // ========== 新增核心方法：兼容参数注解（避免MyBatis绑定失败） ==========
    /**
     * 统计宠物已选课程总数（带@Param注解，适配Controller调用）
     * 解决：直接调用countByPetId时可能因参数名不匹配导致的报错
     */
    @Select("SELECT COUNT(*) FROM pet_course WHERE pet_id = #{petId}")
    int countCoursesByPetId(@Param("petId") Integer petId);

    // 🔥 新增：检查宠物是否选了某课程（解决Service中调用报错的核心方法）
    @Select("SELECT COUNT(*) FROM pet_course WHERE pet_id = #{petId} AND course_id = #{courseId}")
    int checkPetHasCourse(@Param("petId") Integer petId, @Param("courseId") Integer courseId);
}