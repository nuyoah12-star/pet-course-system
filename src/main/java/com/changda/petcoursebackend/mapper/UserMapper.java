package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

// 核心：添加@Mapper注解，让MyBatis识别；定义selectByUsername方法
@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户实体对象
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);
}

