package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Badge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BadgeMapper {
    @Select("select * from badge")
    List<Badge> findAll();
}
