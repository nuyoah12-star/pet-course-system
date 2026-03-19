package com.changda.petcoursebackend.entity;

import lombok.Data;

@Data
public class SelectCourse {
    private Integer id;
    private Integer sId;  // 学生ID
    private Integer cId;  // 课程ID
}
