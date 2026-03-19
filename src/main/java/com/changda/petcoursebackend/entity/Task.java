package com.changda.petcoursebackend.entity;

import lombok.Data;

@Data
public class Task {
    private Integer id;
    private Integer courseId;
    private String name;
    private Integer exp;
}