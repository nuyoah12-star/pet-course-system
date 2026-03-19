package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.User;

// 核心：定义login方法，供Controller调用、ServiceImpl实现
public interface UserService {
    /**
     * 登录查询（根据用户名查用户）
     * @param username 用户名
     * @return 用户实体对象
     */
    User login(String username);
}