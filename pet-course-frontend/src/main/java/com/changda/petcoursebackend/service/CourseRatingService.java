package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.CourseRating;
import com.changda.petcoursebackend.mapper.CourseRatingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseRatingService {

    @Autowired
    private CourseRatingMapper mapper;

    public void addRating(CourseRating rating) {
        mapper.addRating(rating);
    }

    public List<CourseRating> getRatings(Integer courseId) {
        return mapper.getRatings(courseId);
    }
}