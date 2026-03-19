package com.changda.petcoursebackend.entity;

import lombok.Data;

@Data
public class CourseRating {
    private Integer id;
    private Integer courseId;
    private String petName;
    private Integer score;
    private String comment;
}
