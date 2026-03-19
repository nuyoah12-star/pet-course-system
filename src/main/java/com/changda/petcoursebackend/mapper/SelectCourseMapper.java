package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Course;
import com.changda.petcoursebackend.entity.SelectCourse;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SelectCourseMapper {

    @Select("SELECT * FROM select_course WHERE s_id = #{sId} AND c_id = #{cId}")
    SelectCourse selectBySC(@Param("sId") Integer sId, @Param("cId") Integer cId);

    @Insert("INSERT INTO select_course(s_id, c_id) VALUES(#{sId}, #{cId})")
    int insert(SelectCourse selectCourse);

    @Select("SELECT c.* FROM course c " +
            "INNER JOIN select_course sc ON c.id = sc.c_id " +
            "WHERE sc.s_id = #{sId}")
    List<Course> selectCoursesByStudentId(@Param("sId") Integer sId);

    @Delete("DELETE FROM select_course WHERE id = #{id}")
    int deleteById(Integer id);
}
