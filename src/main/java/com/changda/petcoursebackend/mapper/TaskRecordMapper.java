package com.changda.petcoursebackend.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Date;

/**
 * 任务完成记录Mapper接口
 * 负责宠物任务完成记录的数据库查询+插入操作
 * 对应数据库表：task_record（真实宠物任务完成业务表）
 */
@Mapper // MyBatis扫描注解，确保接口被Spring管理
public interface TaskRecordMapper {

    /**
     * 插入任务完成记录（核心：任务完成后自动写入数据库）
     * @param petId 宠物ID
     * @param taskName 任务名称（坐下任务/握手任务/接飞盘任务/障碍赛任务）
     * @param completeTime 任务完成时间
     * @return 插入成功的行数
     */
    @Insert("""
        INSERT INTO task_record (pet_id, task_name, complete_time) 
        VALUES (#{petId}, #{taskName}, #{completeTime})
    """)
    int insertTaskRecord(
            @Param("petId") Integer petId,
            @Param("taskName") String taskName,
            @Param("completeTime") Date completeTime
    );

    /**
     * 按宠物ID和任务名统计完成次数
     * 直接查询task_record表，支持实时统计（任务写入后立即查到）
     *
     * @param petId     宠物ID
     * @param taskName  任务名称（坐下任务/握手任务/接飞盘任务/障碍赛任务）
     * @return 该宠物完成该任务的次数（无记录返回0）
     */
    @Select("SELECT COALESCE(COUNT(*), 0) FROM task_record WHERE pet_id = #{petId} AND task_name = #{taskName}")
    Integer countByPetIdAndTaskName(@Param("petId") Integer petId, @Param("taskName") String taskName);

    /**
     * 统计所有宠物某类任务的总完成次数
     *
     * @param taskName 任务名称
     * @return 所有宠物完成该任务的总次数（无记录返回0）
     */
    @Select("SELECT COALESCE(COUNT(*), 0) FROM task_record WHERE task_name = #{taskName}")
    Integer countAllByTaskName(@Param("taskName") String taskName);
}