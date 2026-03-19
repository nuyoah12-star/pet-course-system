package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseMapper {

    /**
     * 原有方法：查询所有课程
     * 修复：显式指定字段 + 映射trainer（兼容数据库字段）
     */
    @Select("""
        SELECT 
            id, 
            name, 
            description, 
            difficulty, 
            trainer, 
            avg_score, 
            cover, 
            pet_type, 
            min_age, 
            max_age 
        FROM course
    """)
    List<Course> findAll();

    /**
     * 原有方法：根据ID查询课程
     * 修复：显式指定字段 + 映射trainer
     */
    @Select("""
        SELECT 
            id, 
            name, 
            description, 
            difficulty, 
            trainer, 
            avg_score, 
            cover, 
            pet_type, 
            min_age, 
            max_age 
        FROM course WHERE id = #{id}
    """)
    Course selectById(Integer id);

    /**
     * 原有方法：根据宠物类型推荐课程（最多返回5条）
     * 修复：显式指定字段 + 映射trainer
     * @param type 宠物类型（如"狗"/"猫"）
     * @return 推荐的课程列表
     */
    @Select("""
        SELECT 
            id, 
            name, 
            description, 
            difficulty, 
            trainer, 
            avg_score, 
            cover, 
            pet_type, 
            min_age, 
            max_age 
        FROM course WHERE pet_type = #{type} LIMIT 5
    """)
    List<Course> recommend(String type);

    // ---------------------- 新增推荐核心方法（适配类型+年龄+热门） ----------------------

    /**
     * 1. 根据宠物类型+年龄查询基础课程（核心推荐规则）
     * 修复：添加trainer字段 + 难度/评分/封面等完整字段
     * @param petType 宠物类型（狗/猫）
     * @param petAge 宠物年龄
     * @return 匹配的课程列表
     */
    @Select("""
        SELECT 
            id, 
            name, 
            description, 
            difficulty, 
            trainer, 
            avg_score, 
            cover, 
            pet_type, 
            min_age, 
            max_age 
        FROM course 
        WHERE pet_type = #{petType} 
          AND #{petAge} BETWEEN min_age AND max_age
    """)
    List<Course> getCoursesByTypeAndAge(
            @Param("petType") String petType,
            @Param("petAge") Integer petAge
    );

    /**
     * 2. 动态获取stats模块的热门课程TOP3（返回Map，无需新增实体类）
     * @return 热门课程列表（course_name/learn_count）
     */
    @Select("""
        SELECT course_name, learn_count 
        FROM course_learn_stats 
        ORDER BY learn_count DESC 
        LIMIT 3
    """)
    List<Map<String, Object>> getHotCoursesTop3();

    /**
     * 3. 根据课程名+宠物类型匹配课程详情
     * 修复：添加trainer字段 + 完整字段
     * @param courseName 课程名称
     * @param petType 宠物类型
     * @return 匹配的课程
     */
    @Select("""
        SELECT 
            id, 
            name, 
            description, 
            difficulty, 
            trainer, 
            avg_score, 
            cover, 
            pet_type, 
            min_age, 
            max_age 
        FROM course 
        WHERE name = #{courseName} 
          AND pet_type = #{petType}
    """)
    Course getCourseByNameAndType(
            @Param("courseName") String courseName,
            @Param("petType") String petType
    );

    // 其他原有方法保留
}