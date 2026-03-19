package com.changda.petcoursebackend.entity;

import lombok.Data;
import java.util.Date;

/**
 * 宠物选课实体类，对应数据库pet_course表
 */
@Data
public class PetCourse {
    private Integer id;          // 选课记录ID（主键）
    private Integer petId;       // 宠物ID
    private Integer courseId;    // 课程ID
    private Date selectTime;     // 选课时间
    private Integer progress;    // 课程进度（0-100）
}