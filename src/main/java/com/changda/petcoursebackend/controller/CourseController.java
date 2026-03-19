package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.mapper.CourseMapper;
import com.changda.petcoursebackend.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 课程控制器
 * 优化：适配数据库新增的trainer/avgScore/difficulty等字段，返回完整课程数据
 * 修复：删除 @CrossOrigin(origins = "*") 避免和全局 CorsConfig 冲突
 */
@RestController
@RequestMapping("/courses")
// 关键：保留全局CorsConfig，删除局部跨域注解
public class CourseController {

    // 构造器注入（原有Mapper + 新增Service）
    private final CourseMapper courseMapper;
    private final CourseService courseService;

    // 注入两个依赖，保证原有接口和新接口都可用
    public CourseController(CourseMapper courseMapper, CourseService courseService) {
        this.courseMapper = courseMapper;
        this.courseService = courseService;
    }

    // ---------------------- 原有接口（完全保留，不修改，自动返回新增字段） ----------------------

    /**
     * 获取所有课程列表
     * 优化：现在返回的Course对象包含trainer/avgScore/difficulty等新增字段
     */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseMapper.findAll();
    }

    /**
     * 根据ID获取课程详情（训练任务页面用）
     * 优化：返回完整字段（包含trainer/avgScore等）
     */
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Integer id) {
        return courseMapper.selectById(id);
    }

    /**
     * 原有推荐接口：仅根据宠物类型推荐（兼容旧调用）
     * @param type 宠物类型（如"狗"/"猫"）
     * @return 推荐的课程列表（最多5条），包含trainer等新增字段
     */
    @GetMapping("/recommend/{type}")
    public List<Course> recommendCoursesByType(@PathVariable String type) {
        return courseMapper.recommend(type);
    }

    // ---------------------- 新增推荐核心接口（类型+年龄+热门） ----------------------

    /**
     * 增强版推荐接口：根据宠物类型+年龄+动态热门推荐
     * @param petType 宠物类型（狗/猫，默认狗）
     * @param petAge 宠物年龄（默认3岁）
     * @return 精准推荐的课程列表（热门优先），包含trainer/avgScore等完整字段
     */
    @GetMapping("/recommend")
    public List<Course> recommendCourses(
            @RequestParam(defaultValue = "狗") String petType,
            @RequestParam(defaultValue = "3") Integer petAge
    ) {
        return courseService.recommendCourses(petType, petAge);
    }

    /**
     * 获取stats模块的热门课程TOP3（供前端打热门标签）
     * @return 热门课程列表（course_name/learn_count），可扩展包含trainer字段
     */
    @GetMapping("/hot-top3")
    public List<Map<String, Object>> getHotCoursesTop3() {
        return courseService.getHotCoursesTop3();
    }
}