package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.User;
import com.changda.petcoursebackend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 核心优化：删除字段注入，改用构造器注入；移除@RequestMapping的冗余括号
@RestController
@RequestMapping("/user") // 修正：删除多余的()
public class UserController {

    // 1. 定义final字段，确保依赖不可变
    private final UserService userService;

    // 2. 构造器注入（Spring自动识别，无需@Autowired）
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public User login(String username) {
        return userService.login(username);
    }
}


