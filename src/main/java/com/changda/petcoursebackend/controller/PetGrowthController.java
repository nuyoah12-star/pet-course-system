package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.PetGrowth;
import com.changda.petcoursebackend.service.PetGrowthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物成长控制器
 * 兼容旧接口（按名称） + 新增petId接口（解决前端数据同步问题）
 */
@RestController
@RequestMapping("/growth") // 保留原有前缀，避免跨域/路径问题
@CrossOrigin // 保留跨域注解，前端无需修改跨域配置
public class PetGrowthController {

    @Autowired
    private PetGrowthService service;

    // ========== 原有接口（保留，兼容历史逻辑） ==========
    @GetMapping("/{petName}")
    public PetGrowth get(@PathVariable String petName) {
        return service.get(petName);
    }

    @PostMapping("/add")
    public String add(@RequestParam String petName) {
        service.addExp(petName, 20);
        return "exp added";
    }

    // ========== 新增核心接口（前端PetGrowth.vue专用） ==========
    /**
     * 按petId查询宠物成长数据（解决前端404+数据同步问题）
     * @param petId 宠物ID（1=可乐，2=糯米，3=旺财）
     * @return 成长数据（含level、exp、petName）
     */
    @GetMapping("/petId/{petId}")
    public PetGrowth getByPetId(@PathVariable Integer petId) {
        // 调用Service层按petId查询的方法，petName传null即可（Service会自动查）
        return service.getByPetId(petId, null);
    }
}