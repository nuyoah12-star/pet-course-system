package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.Course;
import java.util.List;
import java.util.Map;

/**
 * 课程服务接口（适配推荐功能，保留原有方法）
 */
public interface CourseService {
    // 原有方法：查询所有课程
    List<Course> list();

    // 原有方法：按评分从高到低排序查询课程
    List<Course> listByScoreDesc();

    // ---------------------- 新增推荐功能核心方法 ----------------------

    /**
     * 核心推荐方法：根据宠物类型+年龄+动态热门TOP3推荐课程
     * @param petType 宠物类型（狗/猫）
     * @param petAge 宠物年龄
     * @return 推荐课程列表（热门优先，去重）
     */
    List<Course> recommendCourses(String petType, Integer petAge);

    /**
     * 获取stats模块的热门课程TOP3（返回Map，无需新增实体类）
     * @return 热门课程列表（course_name/learn_count）
     */
    List<Map<String, Object>> getHotCoursesTop3();

    /**
     * 原有推荐方法兼容（可选，保留向下兼容）
     * @param type 宠物类型
     * @return 基础推荐课程列表
     */
    default List<Course> recommend(String type) {
        // 默认适配原有逻辑：按类型推荐，年龄默认3岁
        return recommendCourses(type, 3);
    }
}