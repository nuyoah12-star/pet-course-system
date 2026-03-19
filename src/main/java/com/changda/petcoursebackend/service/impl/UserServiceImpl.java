package com.changda.petcoursebackend.service.impl;

import com.changda.petcoursebackend.entity.User;
import com.changda.petcoursebackend.mapper.UserMapper;
import com.changda.petcoursebackend.service.UserService;
import org.springframework.stereotype.Service;

// 核心：改用构造器注入（消除警告），实现login方法并调用Mapper
@Service
public class UserServiceImpl implements UserService {

    // 定义final字段，确保依赖不可变
    private final UserMapper userMapper;

    // 构造器注入（Spring官方推荐方式）
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User login(String username) {
        // 调用Mapper层方法查询用户
        return userMapper.selectByUsername(username);
    }
}
