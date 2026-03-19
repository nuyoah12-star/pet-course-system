package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.PetSkill;
import com.changda.petcoursebackend.entity.Skill; // 关键导入：解决找不到Skill类的问题
import com.changda.petcoursebackend.mapper.PetSkillMapper;
import com.changda.petcoursebackend.mapper.SkillMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pet-skill")
@CrossOrigin
public class PetSkillController {

    private final PetSkillMapper petSkillMapper;
    private final SkillMapper skillMapper;

    // 构造注入
    public PetSkillController(PetSkillMapper petSkillMapper, SkillMapper skillMapper) {
        this.petSkillMapper = petSkillMapper;
        this.skillMapper = skillMapper;
    }

    /**
     * 解锁宠物技能（含前置校验+重复校验）
     * @param petSkill 宠物-技能关联对象
     * @return 操作结果
     */
    @PostMapping("/unlock")
    public Map<String, Object> unlockPetSkill(@RequestBody PetSkill petSkill) {
        Map<String, Object> result = new HashMap<>();

        // 1. 基础参数校验
        if (petSkill.getPetId() == null || petSkill.getSkillId() == null) {
            result.put("success", false);
            result.put("message", "宠物ID和技能ID不能为空");
            return result;
        }

        // 2. 校验是否已解锁该技能
        int unlockedCount = petSkillMapper.checkSkillUnlocked(petSkill.getPetId(), petSkill.getSkillId());
        if (unlockedCount > 0) {
            result.put("success", false);
            result.put("message", "该技能已解锁，无需重复操作");
            return result;
        }

        // 3. 校验前置技能（非根技能需校验）
        Skill targetSkill = skillMapper.findById(petSkill.getSkillId());
        if (targetSkill != null && targetSkill.getParentId() != 0) {
            int parentUnlocked = petSkillMapper.checkSkillUnlocked(petSkill.getPetId(), targetSkill.getParentId());
            if (parentUnlocked == 0) {
                Skill parentSkill = skillMapper.findById(targetSkill.getParentId());
                result.put("success", false);
                result.put("message", "需先解锁前置技能：" + parentSkill.getName());
                return result;
            }
        }

        // 4. 执行解锁操作
        int affectedRows = petSkillMapper.unlockSkill(petSkill);
        if (affectedRows > 0) {
            result.put("success", true);
            result.put("message", "技能解锁成功");
        } else {
            result.put("success", false);
            result.put("message", "技能解锁失败");
        }

        return result;
    }

    /**
     * 查询宠物已解锁的所有技能ID
     * @param petId 宠物ID
     * @return 技能ID列表
     */
    @GetMapping("/{petId}")
    public Map<String, Object> getUnlockedSkills(@PathVariable Integer petId) {
        Map<String, Object> result = new HashMap<>();
        result.put("skills", petSkillMapper.findSkillIdsByPetId(petId));
        return result;
    }
}