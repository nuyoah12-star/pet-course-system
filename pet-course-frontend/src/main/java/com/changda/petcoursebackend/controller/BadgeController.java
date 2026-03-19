package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Badge;
import com.changda.petcoursebackend.mapper.BadgeMapper;
import com.changda.petcoursebackend.mapper.PetBadgeMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// 新增跨域注解，允许前端（5173端口）请求
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/badge")
public class BadgeController {
    private final BadgeMapper badgeMapper;
    // 新增：注入PetBadgeMapper，用于关联查询宠物勋章
    private final PetBadgeMapper petBadgeMapper;

    // 新增：构造器注入PetBadgeMapper（保持和原有注入风格一致）
    public BadgeController(BadgeMapper badgeMapper, PetBadgeMapper petBadgeMapper) {
        this.badgeMapper = badgeMapper;
        this.petBadgeMapper = petBadgeMapper;
    }

    // 原有接口：获取所有勋章模板（保留，兼容历史逻辑）
    @GetMapping("/list")
    public List<Badge> list() {
        return badgeMapper.findAll();
    }

    // 新增核心接口：根据宠物ID查询该宠物已获得的勋章
    @GetMapping("/my")
    public List<Map<String, Object>> getPetBadges(@RequestParam Integer petId) {
        // 调用PetBadgeMapper的关联查询方法，返回宠物专属勋章
        return petBadgeMapper.findBadgesByPetId(petId);
    }
}