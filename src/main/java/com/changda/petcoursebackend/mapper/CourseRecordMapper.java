package com.changda.petcoursebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 课程学习记录Mapper接口
 * 负责课程学习记录的数据库查询操作
 * 对应数据库表：pet_course（真实宠物选课业务表）+ course（课程信息表）
 */
@Mapper // MyBatis扫描注解，与启动类@MapperScan二选一（建议保留）
public interface CourseRecordMapper {

    /**
     * 按宠物ID和课程名统计学习（选课）次数
     * 联表查询：通过pet_course的course_id关联course表获取课程名，解决pet_course无course_name字段问题
     * 适配course表真实字段：name（而非course_name）
     *
     * @param petId      宠物ID
     * @param courseName 课程名称（坐下训练/握手训练/接飞盘/障碍赛）
     * @return 学习次数（无记录返回0）
     */
    @Select("""
        SELECT COALESCE(COUNT(*), 0) 
        FROM pet_course pc
        JOIN course c ON pc.course_id = c.id
        WHERE pc.pet_id = #{petId} AND c.name = #{courseName}
    """)
    Integer countByPetIdAndCourseName(@Param("petId") Integer petId, @Param("courseName") String courseName);

    /**
     * 统计所有宠物某门课程的总学习（选课）次数
     *
     * @param courseName 课程名称
     * @return 总学习次数（无记录返回0）
     */
    @Select("""
        SELECT COALESCE(COUNT(*), 0) 
        FROM pet_course pc
        JOIN course c ON pc.course_id = c.id
        WHERE c.name = #{courseName}
    """)
    Integer countAllByCourseName(@Param("courseName") String courseName);
}