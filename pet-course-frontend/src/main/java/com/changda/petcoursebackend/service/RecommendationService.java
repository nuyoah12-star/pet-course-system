package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.Course;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    public List<Course> recommend(List<Course> courses, String petType) {
        if (courses == null || petType == null) {
            return List.of();
        }
        return courses.stream()
                .filter(c -> c.getDescription() != null && c.getDescription().contains(petType))
                .collect(Collectors.toList());
    }
}