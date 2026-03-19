package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.CourseRating;
import com.changda.petcoursebackend.entity.CourseScoreVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseRatingMapper {

    @Insert("INSERT INTO course_rating (course_id, score, comment) VALUES (#{courseId}, #{score}, #{comment})")
    void addRating(CourseRating rating);

    @Select("SELECT * FROM course_rating WHERE course_id = #{courseId}")
    List<CourseRating> getRatings(Integer courseId);

    // 新增：统计每个课程的平均评分
    @Select("SELECT course_id, AVG(score) AS avgScore FROM course_rating GROUP BY course_id")
    List<CourseScoreVO> getCourseAvgScore();
}