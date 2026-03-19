package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务Mapper（注解版，无XML）
 * 处理任务相关的数据库操作
 */
@Mapper
public interface TaskMapper {

    /**
     * 查询所有任务
     * @return 任务列表
     */
    @Select("SELECT * FROM task")
    List<Task> selectAll();

    /**
     * 根据课程ID查询任务
     * @param courseId 课程ID
     * @return 对应课程的任务列表
     */
    @Select("SELECT * FROM task WHERE course_id = #{courseId}")
    List<Task> selectByCourseId(@Param("courseId") Integer courseId);

    /**
     * 根据任务ID查询单个任务（新增，适配TaskController）
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Select("SELECT * FROM task WHERE id = #{taskId}")
    Task selectById(@Param("taskId") Integer taskId);
}