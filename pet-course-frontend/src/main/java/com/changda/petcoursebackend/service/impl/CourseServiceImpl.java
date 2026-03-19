package com.changda.petcoursebackend.service.impl;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.entity.CourseScoreVO;
import com.changda.petcoursebackend.mapper.CourseMapper;
import com.changda.petcoursebackend.mapper.CourseRatingMapper;
import com.changda.petcoursebackend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseRatingMapper ratingMapper;

    // 原有方法：查询所有课程
    @Override
    public List<Course> list() {
        return courseMapper.findAll();
    }

    // 修复后的按评分排序方法（方法名大小写与实体类一致）
    @Override
    public List<Course> listByScoreDesc() {
        // 1. 获取所有课程
        List<Course> courseList = courseMapper.findAll();
        if (courseList == null || courseList.isEmpty()) { // 增加null判断
            return new ArrayList<>(); // 返回空列表而非null
        }

        // 2. 统计每个课程的平均评分
        List<CourseScoreVO> scoreVOList = ratingMapper.getCourseAvgScore();
        // 处理scoreVOList为空的情况
        Map<Integer, Double> courseScoreMap = scoreVOList == null ?
                Map.of() :
                scoreVOList.stream()
                        .collect(Collectors.toMap(CourseScoreVO::getCourseId,
                                CourseScoreVO::getAvgScore,
                                (v1, v2) -> v1)); // 解决主键重复问题

        // 3. 给课程设置平均评分（修复：使用正确的 setAvgScore 大写 S）
        courseList.forEach(course -> {
            course.setAvgScore(courseScoreMap.getOrDefault(course.getId(), 0.0));
        });

        // 4. 按评分降序排序（修复：使用正确的 getAvgScore 大写 S）
        courseList.sort(Comparator.comparing(Course::getAvgScore)
                .reversed()
                .thenComparing(Course::getId));

        return courseList;
    }

    // ---------------------- 新增推荐功能核心实现（修复500错误） ----------------------

    /**
     * 核心推荐方法：类型+年龄 + 动态热门TOP3（防空指针）
     */
    @Override
    public List<Course> recommendCourses(String petType, Integer petAge) {
        // 入参校验：防止petType/petAge为空
        if (petType == null || petType.trim().isEmpty() || petAge == null || petAge < 0) {
            return new ArrayList<>();
        }

        // 步骤1：按类型+年龄查询基础课程（增加null判断）
        List<Course> baseCourses = courseMapper.getCoursesByTypeAndAge(petType, petAge);
        if (baseCourses == null) {
            baseCourses = new ArrayList<>();
        }

        // 步骤2：动态获取stats模块的热门课程TOP3（增加null判断）
        List<Map<String, Object>> hotStats = courseMapper.getHotCoursesTop3();
        List<Course> hotCourses = new ArrayList<>();

        // 仅当hotStats非空时才遍历
        if (hotStats != null && !hotStats.isEmpty()) {
            for (Map<String, Object> stat : hotStats) {
                // 跳过空的stat对象
                if (stat == null || !stat.containsKey("course_name")) {
                    continue;
                }
                String courseName = (String) stat.get("course_name");
                // 跳过空的课程名
                if (courseName == null || courseName.trim().isEmpty()) {
                    continue;
                }
                Course course = courseMapper.getCourseByNameAndType(courseName, petType);
                if (course != null) {
                    hotCourses.add(course);
                }
            }
        }

        // 步骤4：合并去重（优化：用Set快速去重，避免空指针）
        Set<String> courseNameSet = new HashSet<>();
        List<Course> finalCourses = new ArrayList<>();

        // 先加热门课程
        for (Course hotCourse : hotCourses) {
            if (hotCourse != null && hotCourse.getName() != null) {
                if (!courseNameSet.contains(hotCourse.getName())) {
                    finalCourses.add(hotCourse);
                    courseNameSet.add(hotCourse.getName());
                }
            }
        }

        // 再加基础课程（去重）
        for (Course baseCourse : baseCourses) {
            if (baseCourse != null && baseCourse.getName() != null) {
                if (!courseNameSet.contains(baseCourse.getName())) {
                    finalCourses.add(baseCourse);
                    courseNameSet.add(baseCourse.getName());
                }
            }
        }

        return finalCourses;
    }

    /**
     * 获取stats模块的热门课程TOP3（返回Map，无需新增实体类）
     */
    @Override
    public List<Map<String, Object>> getHotCoursesTop3() {
        // 防止返回null
        List<Map<String, Object>> hotStats = courseMapper.getHotCoursesTop3();
        return hotStats == null ? new ArrayList<>() : hotStats;
    }
}