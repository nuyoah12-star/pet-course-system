package com.changda.petcoursebackend.entity;

import lombok.Data;

/**
 * 课程评分统计VO，接收Mapper分组统计结果
 */
@Data
public class CourseScoreVO {
    private Integer courseId; // 课程ID
    private Double avgScore;  // 平均评分
}
