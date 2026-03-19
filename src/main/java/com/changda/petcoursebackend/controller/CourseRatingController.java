package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.CourseRating;
import com.changda.petcoursebackend.service.CourseRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rating")
@CrossOrigin
public class CourseRatingController {

    @Autowired
    private CourseRatingService service;

    @PostMapping("/add")
    public String add(@RequestBody CourseRating rating) {
        service.addRating(rating);
        return "success";
    }

    @GetMapping("/list/{courseId}")
    public List<CourseRating> list(@PathVariable Integer courseId) {
        return service.getRatings(courseId);
    }
}