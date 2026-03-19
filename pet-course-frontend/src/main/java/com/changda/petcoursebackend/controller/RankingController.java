package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ranking")
@CrossOrigin
public class RankingController {

    @Autowired
    private CourseService service;

    @GetMapping("/courses")
    public List<Course> ranking() {
        return service.listByScoreDesc();
    }
}