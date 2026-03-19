package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.entity.SelectCourse;
import com.changda.petcoursebackend.entity.Student;
import com.changda.petcoursebackend.mapper.CourseMapper;
import com.changda.petcoursebackend.mapper.SelectCourseMapper;
import com.changda.petcoursebackend.mapper.StudentMapper;
import com.changda.petcoursebackend.service.PetGrowthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/selectCourse")
@CrossOrigin
public class SelectCourseController {

    @Autowired
    private SelectCourseMapper selectCourseMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private PetGrowthService growthService;

    @PostMapping("/add")
    public String selectCourse(@RequestParam Integer sId, @RequestParam Integer cId) {
        try {
            Course course = courseMapper.selectById(cId);
            if (course == null) {
                return "课程不存在";
            }

            Student student = studentMapper.selectById(sId);
            if (student == null) {
                return "学生不存在";
            }

            SelectCourse exist = selectCourseMapper.selectBySC(sId, cId);
            if (exist != null) {
                return "已选该课程，无需重复选择";
            }

            SelectCourse selectCourse = new SelectCourse();
            selectCourse.setSId(sId);
            selectCourse.setCId(cId);
            selectCourseMapper.insert(selectCourse);

            String petName = student.getPetName();
            if (petName != null && !petName.isEmpty()) {
                growthService.addExp(petName, 20);
                return "选课成功！已为你的宠物增加20点成长经验";
            } else {
                return "选课成功，但未找到宠物名称，未增加经验";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "选课失败：" + e.getMessage();
        }
    }

    @GetMapping("/list/{sId}")
    public List<Course> getSelectedCourses(@PathVariable Integer sId) {
        return selectCourseMapper.selectCoursesByStudentId(sId);
    }

    @DeleteMapping("/delete")
    public String dropCourse(@RequestParam Integer sId, @RequestParam Integer cId) {
        try {
            SelectCourse selectCourse = selectCourseMapper.selectBySC(sId, cId);
            if (selectCourse == null) {
                return "未选择该课程，无法退课";
            }
            selectCourseMapper.deleteById(selectCourse.getId());
            return "退课成功";
        } catch (Exception e) {
            e.printStackTrace();
            return "退课失败：" + e.getMessage();
        }
    }
}