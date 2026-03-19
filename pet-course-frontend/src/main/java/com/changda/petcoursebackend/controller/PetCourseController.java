package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.PetCourse;
import com.changda.petcoursebackend.entity.PetSkill;
import com.changda.petcoursebackend.service.PetCourseService;
import com.changda.petcoursebackend.mapper.PetBadgeMapper;
import com.changda.petcoursebackend.mapper.PetSkillMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/petCourse")
@CrossOrigin(origins = "http://localhost:5173")
public class PetCourseController {

    // 原有注入：PetCourseService
    @Autowired
    private PetCourseService petCourseService;

    // 新增注入：PetBadgeMapper（用于发放勋章）
    @Autowired
    private PetBadgeMapper petBadgeMapper;

    // 新增注入：PetSkillMapper（用于解锁/回收技能）
    @Autowired
    private PetSkillMapper petSkillMapper;

    // 选课接口（优化勋章发放逻辑：按数量动态匹配+升级覆盖 + 同步解锁技能）
    @PostMapping("/select")
    public String selectCourse(@RequestBody PetCourse petCourse) {
        try {
            // 1. 参数校验
            if (petCourse.getPetId() == null || petCourse.getCourseId() == null) {
                return "选课失败：宠物ID和课程ID不能为空";
            }

            // 2. 初始化默认值
            if (petCourse.getSelectTime() == null) {
                petCourse.setSelectTime(new Date());
            }
            if (petCourse.getProgress() == null) {
                petCourse.setProgress(0);
            }

            // 3. 原有逻辑：调用Service选课（加经验+升级）
            petCourseService.insert(petCourse);

            // 4. 新增逻辑：统计该宠物已选课数量
            int courseCount = petCourseService.countByPetId(petCourse.getPetId());

            // 5. 核心优化：根据选课数量动态匹配勋章ID（1-2门→初级、3门→进阶、4门+→大师）
            Integer targetBadgeId = getTargetBadgeId(courseCount);

            // 6. 关键优化：先删除该宠物原有勋章（避免重复/多枚勋章，实现升级覆盖）
            petBadgeMapper.deleteBadgeByPetId(petCourse.getPetId());

            // 7. 颁发最新勋章
            petBadgeMapper.addBadge(petCourse.getPetId(), targetBadgeId);

            // 8. 核心优化：选课成功后解锁对应技能（强制解锁基础训练根技能）
            Integer skillId = getSkillIdByCourseId(petCourse.getCourseId());
            if (skillId != null) {
                // ========== 方案三核心修改：强制解锁基础训练技能 ==========
                // 基础训练（skillId=1）是根技能，无论是否已解锁都确保存在
                if (skillId == 1) {
                    PetSkill rootSkill = new PetSkill();
                    rootSkill.setPetId(petCourse.getPetId());
                    rootSkill.setSkillId(1);
                    // 先删除原有记录（避免重复），再重新插入
                    petSkillMapper.deletePetSkill(petCourse.getPetId(), 1);
                    petSkillMapper.unlockSkill(rootSkill);
                    System.out.println("【调试】强制解锁基础训练技能：petId=" + petCourse.getPetId());
                } else {
                    // 其他技能：先校验是否已解锁，避免重复插入
                    Integer unlockedCount = petSkillMapper.checkSkillUnlocked(petCourse.getPetId(), skillId);
                    if (unlockedCount == null || unlockedCount == 0) {
                        PetSkill petSkill = new PetSkill();
                        petSkill.setPetId(petCourse.getPetId());
                        petSkill.setSkillId(skillId);
                        petSkillMapper.unlockSkill(petSkill);
                        System.out.println("【调试】解锁技能：petId=" + petCourse.getPetId() + ", skillId=" + skillId);
                    }
                }
            }

            // 9. 返回带勋章+技能的友好提示
            String badgeName = getBadgeName(targetBadgeId);
            String skillName = skillId != null ? getSkillName(skillId) : "无";
            return String.format("选课成功 +10经验，已选%d门课程，解锁「%s」勋章 + 「%s」技能",
                    courseCount, badgeName, skillName);
        } catch (Exception e) {
            e.printStackTrace();
            return "选课失败: " + e.getMessage();
        }
    }

    // 取消选课接口（新增：取消后重新计算勋章 + 回收对应技能）
    @GetMapping("/cancel")
    public String cancel(@RequestParam Integer petId, @RequestParam Integer courseId) {
        try {
            // 1. 参数校验
            if (petId == null || courseId == null) {
                return "取消失败：宠物ID和课程ID不能为空";
            }

            // 2. 原有取消选课逻辑
            petCourseService.cancelCourse(petId, courseId);

            // 3. 取消后重新统计选课数量，更新勋章（避免取消后勋章不降级）
            int courseCount = petCourseService.countByPetId(petId);
            Integer targetBadgeId = getTargetBadgeId(courseCount);

            // 4. 删除原有勋章，重新颁发（无课程则删除后不颁发）
            petBadgeMapper.deleteBadgeByPetId(petId);
            if (courseCount > 0) { // 有选课才颁发勋章
                petBadgeMapper.addBadge(petId, targetBadgeId);
            }

            // 5. 核心优化：取消选课回收对应技能（保留基础训练根技能）
            Integer skillId = getSkillIdByCourseId(courseId);
            if (skillId != null) {
                // ========== 方案三核心修改：取消选课时保留基础训练技能 ==========
                if (skillId != 1) { // 基础训练技能不回收
                    // 方案2：取消后检查是否还有其他课程解锁该技能，无则回收
                    boolean hasOtherCourse = checkHasOtherCourseUnlockSkill(petId, skillId);
                    if (!hasOtherCourse) {
                        petSkillMapper.deletePetSkill(petId, skillId);
                        System.out.println("【调试】回收技能：petId=" + petId + ", skillId=" + skillId);
                    }
                } else {
                    System.out.println("【调试】保留基础训练技能，不回收：petId=" + petId);
                }
            }

            // 6. 返回友好提示
            String badgeName = courseCount > 0 ? getBadgeName(targetBadgeId) : "无";
            String skillName = skillId != null ? getSkillName(skillId) : "无";
            return String.format("取消成功，剩余%d门课程，当前勋章：%s，已回收「%s」技能",
                    courseCount, badgeName, skillName);
        } catch (Exception e) {
            e.printStackTrace();
            return "取消失败: " + e.getMessage();
        }
    }

    // 查询我的课程接口（保留原有逻辑）
    @GetMapping("/myCourses")
    public List<Map<String, Object>> myCourses(@RequestParam Integer petId) {
        if (petId == null) {
            return List.of(); // 参数为空返回空列表，避免空指针
        }
        return petCourseService.findCoursesWithEnrollInfoByPetId(petId);
    }

    // 辅助方法：根据选课数量匹配勋章ID（核心规则）
    private Integer getTargetBadgeId(int courseCount) {
        if (courseCount >= 4) {
            return 3; // 4门及以上 → 训练大师
        } else if (courseCount == 3) {
            return 2; // 3门 → 进阶训练师
        } else {
            return 1; // 1-2门 → 初级训练师（0门返回1，但取消时会过滤）
        }
    }

    // 辅助方法：根据勋章ID获取勋章名称
    private String getBadgeName(Integer badgeId) {
        return switch (badgeId) {
            case 1 -> "初级训练师";
            case 2 -> "进阶训练师";
            case 3 -> "训练大师";
            default -> "未知勋章";
        };
    }

    // 核心辅助方法：根据课程ID匹配对应技能ID（可根据业务扩展）
    private Integer getSkillIdByCourseId(Integer courseId) {
        return switch (courseId) {
            case 1 -> 1;   // 基础训练课程 → 解锁技能1（基础训练）
            case 2 -> 2;   // 坐下课程 → 解锁技能2（坐下）
            case 3 -> 3;   // 握手课程 → 解锁技能3（握手）
            case 4 -> 4;   // 接飞盘课程 → 解锁技能4（接飞盘）
            case 5 -> 5;   // 障碍赛课程 → 解锁技能5（障碍赛）
            default -> null;
        };
    }

    // 辅助方法：根据技能ID获取技能名称
    private String getSkillName(Integer skillId) {
        return switch (skillId) {
            case 1 -> "基础训练";
            case 2 -> "坐下";
            case 3 -> "握手";
            case 4 -> "接飞盘";
            case 5 -> "障碍赛";
            default -> "未知技能";
        };
    }

    // 辅助方法：检查宠物是否有其他课程解锁该技能（避免误删）
    private boolean checkHasOtherCourseUnlockSkill(Integer petId, Integer skillId) {
        // 此处需根据你的业务逻辑实现：
        // 示例逻辑：查询宠物已选课程中，是否有其他课程能解锁该技能
        Integer otherCourseId = getCourseIdBySkillId(skillId);
        if (otherCourseId == null) {
            return false;
        }
        // 检查是否选了该其他课程
        return petCourseService.checkPetHasCourse(petId, otherCourseId);
    }

    // 辅助方法：根据技能ID反向匹配课程ID（用于取消选课时校验）
    private Integer getCourseIdBySkillId(Integer skillId) {
        return switch (skillId) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 4;
            case 5 -> 5;
            default -> null;
        };
    }
}