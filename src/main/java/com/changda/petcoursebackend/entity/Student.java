package com.changda.petcoursebackend.entity;

import lombok.Data;

@Data
public class Student {
    private Integer id;
    private String studentName;
    private String petName; // 宠物名称，用于关联成长系统
}
