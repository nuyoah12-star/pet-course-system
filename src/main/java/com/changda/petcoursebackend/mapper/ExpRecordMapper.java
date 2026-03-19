package com.changda.petcoursebackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 经验记录Mapper接口
 * 负责宠物经验记录的数据库查询操作
 * 从真实业务表（pet_course/task_record）实时统计经验，无需专用exp_record表
 */
@Mapper // MyBatis扫描注解，确保接口被Spring管理
public interface ExpRecordMapper {

    /**
     * 按宠物ID和周数统计经验变化量
     * 经验规则：选1门课+10经验，完成1个任务+5经验，按选课/完成时间自动分组到对应周
     * 适配pet_course表真实时间字段：select_time（而非create_time）
     *
     * @param petId 宠物ID
     * @param week  周数（1/2/3/4）
     * @return 该宠物该周的经验变化总量（无记录返回0）
     */
    @Select("""
        SELECT COALESCE(
            -- 选课经验：每选1门课+10经验（使用pet_course真实时间字段select_time）
            (SELECT COALESCE(SUM(10), 0) FROM pet_course WHERE pet_id = #{petId} AND WEEK(select_time) = #{week}) +
            -- 任务经验：每完成1个任务+5经验
            (SELECT COALESCE(SUM(5), 0) FROM task_record WHERE pet_id = #{petId} AND WEEK(complete_time) = #{week}),
            0
        ) AS total_exp
    """)
    Integer sumExpByPetIdAndWeek(@Param("petId") Integer petId, @Param("week") Integer week);

    /**
     * 统计所有宠物某周的总经验变化量
     *
     * @param week 周数（1/2/3/4）
     * @return 所有宠物该周的经验总变化量（无记录返回0）
     */
    @Select("""
        SELECT COALESCE(
            -- 所有宠物选课经验（使用select_time）
            (SELECT COALESCE(SUM(10), 0) FROM pet_course WHERE WEEK(select_time) = #{week}) +
            -- 所有宠物任务经验
            (SELECT COALESCE(SUM(5), 0) FROM task_record WHERE WEEK(complete_time) = #{week}),
            0
        ) AS total_exp
    """)
    Integer sumAllExpByWeek(@Param("week") Integer week);
}