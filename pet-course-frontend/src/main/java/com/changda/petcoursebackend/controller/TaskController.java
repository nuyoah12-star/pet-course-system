package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.entity.Task;
import com.changda.petcoursebackend.mapper.TaskMapper;
import com.changda.petcoursebackend.mapper.TaskRecordMapper;
import com.changda.petcoursebackend.service.PetGrowthService;
import com.changda.petcoursebackend.entity.Pet;
import com.changda.petcoursebackend.mapper.PetMapper;
import com.changda.petcoursebackend.service.PetService;
import com.changda.petcoursebackend.entity.PetSkill; // 新增：引入宠物技能实体
import com.changda.petcoursebackend.mapper.PetSkillMapper; // 新增：引入宠物技能Mapper
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private PetService petService;

    @Autowired
    private PetGrowthService petGrowthService;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private TaskRecordMapper taskRecordMapper;

    // ========== 核心新增：注入PetSkillMapper（技能解锁用） ==========
    @Autowired
    private PetSkillMapper petSkillMapper;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskMapper.selectAll();
    }

    @GetMapping("/list")
    public List<Task> getTasksByCourseId(@RequestParam(required = false) Integer courseId) {
        if (courseId != null) {
            return taskMapper.selectByCourseId(courseId);
        } else {
            return taskMapper.selectAll();
        }
    }

    // ========== 核心新增：任务与技能的映射关系 ==========
    /**
     * 根据任务名称获取对应的技能ID
     * @param taskName 任务名称
     * @return 技能ID（1=基础训练，2=坐下，3=握手，4=接飞盘，5=障碍赛）
     */
    private Integer getSkillIdByTaskName(String taskName) {
        if (taskName == null) return null;
        taskName = taskName.toLowerCase();
        if (taskName.contains("基础训练")) {
            return 1;
        } else if (taskName.contains("坐下")) {
            return 2;
        } else if (taskName.contains("握手")) {
            return 3;
        } else if (taskName.contains("接飞盘")) {
            return 4;
        } else if (taskName.contains("障碍赛")) {
            return 5;
        } else {
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/complete")
    public String completeTask(@RequestBody Map<String, Object> params) {
        try {
            // 1. 安全解析参数
            Integer taskId = null;
            Integer courseId = null;
            Integer petId = null;
            try {
                Object taskIdObj = params.get("taskId");
                Object courseIdObj = params.get("courseId");
                Object petIdObj = params.get("petId");

                if (taskIdObj == null || courseIdObj == null || petIdObj == null) {
                    return "完成任务失败：taskId、courseId、petId 不能为空";
                }

                taskId = Integer.parseInt(taskIdObj.toString());
                courseId = Integer.parseInt(courseIdObj.toString());
                petId = Integer.parseInt(petIdObj.toString());
            } catch (NumberFormatException e) {
                logger.error("参数类型转换失败", e);
                return "完成任务失败：参数必须为数字格式";
            } catch (Exception e) {
                logger.error("参数解析异常", e);
                return "完成任务失败：参数解析出错 - " + e.getMessage();
            }

            logger.info("开始处理任务完成请求：taskId={}, courseId={}, petId={}", taskId, courseId, petId);
            System.out.println("\n===== 【调试】TaskController 接收参数 =====");
            System.out.println("taskId: " + taskId + ", courseId: " + courseId + ", petId: " + petId);

            // 2. 严格参数校验
            if (taskId <= 0 || courseId <= 0 || petId <= 0) {
                logger.error("参数非法（非正整数）：taskId={}, courseId={}, petId={}", taskId, courseId, petId);
                return "完成任务失败：taskId、courseId、petId 必须为正整数";
            }

            // 3. 查询任务
            Task task = taskMapper.selectById(taskId);
            if (task == null) {
                logger.error("未找到任务：taskId={}", taskId);
                return "完成任务失败：未找到ID为" + taskId + "的任务";
            }
            Integer expGained = task.getExp();
            if (expGained == null || expGained <= 0) {
                logger.error("任务无有效经验：taskId={}, exp={}", taskId, expGained);
                return "完成任务失败：该任务无有效经验奖励（exp=" + expGained + "）";
            }
            System.out.println("【调试】查询到任务：taskId=" + taskId + ", 奖励经验=" + expGained);

            // 4. 校验宠物是否存在
            Pet pet = petMapper.selectById(petId);
            if (pet == null) {
                logger.error("未找到宠物：petId={}", petId);
                return "完成任务失败：未找到ID为" + petId + "的宠物";
            }
            String petName = pet.getName();
            if (petName == null || petName.trim().isEmpty()) {
                petName = "宠物" + petId;
            }
            logger.info("找到宠物：petId={}, petName={}", petId, petName);
            System.out.println("【调试】查询到宠物：petId=" + petId + ", petName=" + petName);

            // 5. 更新pet表经验
            try {
                logger.info("开始更新宠物经验：petId={}, 奖励经验={}", petId, expGained);
                petService.addExpToPet(petId, expGained);
                logger.info("宠物经验更新完成：petId={}", petId);
                System.out.println("【调试】宠物经验更新完成：petId=" + petId);
            } catch (Exception e) {
                logger.error("更新宠物经验失败：petId={}", petId, e);
                e.printStackTrace();
                return "完成任务失败：更新宠物经验失败 - " + e.getMessage();
            }

            // 6. 更新pet_growth表
            try {
                logger.info("开始更新宠物成长数据：petId={}, petName={}, 奖励经验={}", petId, petName, expGained);
                System.out.println("\n===== 【调试】调用 addExpByPetId =====");
                System.out.println("petId: " + petId + ", petName: " + petName + ", exp: " + expGained);

                petGrowthService.addExpByPetId(petId, petName, expGained);

                System.out.println("===== 【调试】addExpByPetId 执行完成 =====\n");
                logger.info("宠物成长数据更新完成：petId={}", petId);
            } catch (Exception e) {
                logger.error("更新宠物成长数据失败：petId={}", petId, e);
                e.printStackTrace();
                return "完成任务失败：更新宠物成长数据失败 - " + e.getMessage();
            }

            // ========== 核心新增1：写入task_record表 ==========
            try {
                // 获取任务名称（根据task表的name字段，适配统计页的任务名）
                String taskName = task.getName();
                // 统一任务名格式
                if (taskName.contains("坐下")) {
                    taskName = "坐下任务";
                } else if (taskName.contains("握手")) {
                    taskName = "握手任务";
                } else if (taskName.contains("接飞盘")) {
                    taskName = "接飞盘任务";
                } else if (taskName.contains("障碍赛")) {
                    taskName = "障碍赛任务";
                }

                // 写入任务完成记录到task_record表
                taskRecordMapper.insertTaskRecord(petId, taskName, new Date());
                logger.info("任务记录写入成功：petId={}, taskName={}", petId, taskName);
                System.out.println("【调试】任务记录写入成功：petId=" + petId + ", taskName=" + taskName);
            } catch (Exception e) {
                logger.error("写入task_record表失败：petId={}", petId, e);
                e.printStackTrace();
                logger.warn("任务核心流程完成，但任务记录写入失败，不影响经验获取");
            }

            // ========== 核心新增2：自动解锁对应技能 ==========
            try {
                // 根据任务名称获取技能ID
                Integer skillId = getSkillIdByTaskName(task.getName());
                if (skillId != null) {
                    // 先检查是否已解锁该技能，避免重复插入
                    Integer count = petSkillMapper.checkSkillUnlocked(petId, skillId);
                    if (count == null || count == 0) {
                        // 未解锁则插入解锁记录
                        PetSkill petSkill = new PetSkill();
                        petSkill.setPetId(petId);
                        petSkill.setSkillId(skillId);
                        petSkillMapper.unlockSkill(petSkill);
                        logger.info("技能解锁成功：petId={}, skillId={}, 技能名称={}", petId, skillId, task.getName());
                        System.out.println("【调试】技能解锁成功：petId=" + petId + ", skillId=" + skillId);
                    } else {
                        logger.info("技能已解锁，无需重复操作：petId={}, skillId={}", petId, skillId);
                        System.out.println("【调试】技能已解锁：petId=" + petId + ", skillId=" + skillId);
                    }
                } else {
                    logger.info("该任务无对应技能可解锁：taskName={}", task.getName());
                }
            } catch (Exception e) {
                logger.error("技能解锁失败：petId={}, taskName={}", petId, task.getName(), e);
                e.printStackTrace();
                logger.warn("任务核心流程完成，但技能解锁失败，不影响经验获取");
            }

            // 7. 成功返回
            logger.info("任务完成：petId={}, 宠物{}获得{}经验", petId, petName, expGained);
            return "✅ 任务完成！宠物「" + petName + "」获得了 " + expGained + " 点经验！";
        } catch (Exception e) {
            logger.error("任务完成处理全局异常", e);
            e.printStackTrace();
            return "❌ 完成任务失败：系统异常 - " + e.getMessage();
        }
    }
}