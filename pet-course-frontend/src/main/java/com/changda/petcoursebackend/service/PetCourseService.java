package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.entity.PetCourse;
import com.changda.petcoursebackend.mapper.CourseMapper;
import com.changda.petcoursebackend.mapper.PetCourseMapper;
import com.changda.petcoursebackend.mapper.PetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PetCourseService {

    @Autowired
    private PetCourseMapper petCourseMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private PetMapper petMapper;

    // 原有：选课成功后固定加经验（+10）
    public void addExpAndLevelUp(Integer petId) {
        // 参数校验：避免空指针
        if (petId == null) {
            return;
        }
        petMapper.addExp(petId);
        petMapper.levelUp(petId);
    }

    // 新增：适配任务完成的方法（按任务经验值增加）
    public void addExpToPet(Integer petId, Integer exp) {
        // 参数校验：避免空指针/负数经验
        if (petId == null || exp == null || exp < 0) {
            return;
        }
        petMapper.addExpByExp(petId, exp);
        petMapper.levelUp(petId);
    }

    // 原有选课插入逻辑
    public void insert(PetCourse petCourse) {
        // 参数校验：避免空指针
        if (petCourse == null || petCourse.getPetId() == null) {
            return;
        }
        petCourseMapper.selectCourse(petCourse);
        addExpAndLevelUp(petCourse.getPetId());
    }

    // 原有取消选课
    public void cancelCourse(Integer petId, Integer courseId) {
        // 参数校验：避免空指针
        if (petId == null || courseId == null) {
            return;
        }
        petCourseMapper.cancelCourse(petId, courseId);
    }

    // 原有查询宠物已报名课程
    public List<Map<String, Object>> findCoursesWithEnrollInfoByPetId(Integer petId) {
        // 参数校验：避免空指针
        if (petId == null) {
            return new ArrayList<>();
        }

        List<PetCourse> petCourses = petCourseMapper.findByPetId(petId);
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (PetCourse pc : petCourses) {
            Course course = courseMapper.selectById(pc.getCourseId());
            if (course != null) {
                Map<String, Object> courseMap = new HashMap<>();
                courseMap.put("id", course.getId());
                courseMap.put("name", course.getName());
                courseMap.put("trainer", course.getTrainer());
                courseMap.put("description", course.getDescription());
                courseMap.put("selectTime", pc.getSelectTime());
                courseMap.put("progress", pc.getProgress());
                resultList.add(courseMap);
            }
        }
        return resultList;
    }

    // 新增：统计宠物已选课数量（用于勋章发放）
    public int countByPetId(Integer petId) {
        // 参数校验：避免空指针
        if (petId == null) {
            return 0;
        }
        return petCourseMapper.countByPetId(petId);
    }

    // 新增核心方法：检查宠物是否选了某课程（用于取消选课时校验技能回收）
    public boolean checkPetHasCourse(Integer petId, Integer courseId) {
        // 参数校验：避免空指针
        if (petId == null || courseId == null) {
            return false;
        }
        // 调用Mapper查询宠物是否选了该课程
        int count = petCourseMapper.checkPetHasCourse(petId, courseId);
        return count > 0;
    }
}