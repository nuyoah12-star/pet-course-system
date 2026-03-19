package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Skill;
import com.changda.petcoursebackend.mapper.SkillMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能模块接口控制器
 * 提供技能查询相关API
 */
@RestController
@RequestMapping("/skill")
@CrossOrigin // 允许跨域请求（适配前端Vue项目）
public class SkillController {

    // 构造注入SkillMapper（Spring推荐方式）
    private final SkillMapper skillMapper;

    public SkillController(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    /**
     * 查询所有技能（含层级关系）
     * @return 技能列表
     */
    @GetMapping("/list")
    public List<Skill> getAllSkills() {
        return skillMapper.findAll();
    }

    /**
     * 根据ID查询单个技能详情
     * @param id 技能ID
     * @return 技能详情
     */
    @GetMapping("/{id}")
    public Skill getSkillById(@PathVariable Integer id) {
        return skillMapper.findById(id);
    }

    /**
     * 查询指定前置技能下的子技能
     * @param parentId 前置技能ID
     * @return 子技能列表
     */
    @GetMapping("/children/{parentId}")
    public List<Skill> getChildSkills(@PathVariable Integer parentId) {
        return skillMapper.findByParentId(parentId);
    }
}
