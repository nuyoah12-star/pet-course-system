package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.PetGrowth;
import com.changda.petcoursebackend.mapper.PetGrowthMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 宠物成长服务层
 * 修复：TooManyResultsException（查询加LIMIT 1）
 * 完善：参数校验 + 空值容错 + 详细日志
 * 保留：原有所有方法（兼容历史逻辑）
 */
@Service
public class PetGrowthService {

    private static final Logger logger = LoggerFactory.getLogger(PetGrowthService.class);

    @Autowired
    private PetGrowthMapper mapper;

    // ==================== 原有方法（保留 + 增加容错） ====================
    public PetGrowth get(String petName) {
        // 参数校验
        if (petName == null || petName.trim().isEmpty()) {
            throw new IllegalArgumentException("宠物名称不能为空");
        }
        try {
            PetGrowth g = mapper.findByPet(petName);
            if (g == null) {
                mapper.create(petName);
                g = mapper.findByPet(petName);
            }
            return g;
        } catch (Exception e) {
            logger.error("按名称查询/创建宠物成长记录失败：petName={}", petName, e);
            throw new RuntimeException("查询宠物成长记录失败：" + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void addExp(String petName, int exp) {
        // 参数校验
        if (petName == null || petName.trim().isEmpty()) {
            throw new IllegalArgumentException("宠物名称不能为空");
        }
        if (exp <= 0) {
            throw new IllegalArgumentException("经验值必须为正整数");
        }

        try {
            PetGrowth g = mapper.findByPet(petName);
            if (g == null) {
                mapper.create(petName);
                g = mapper.findByPet(petName);
            }

            int total = g.getExp() + exp;
            int level = g.getLevel();
            while (total >= 1000) {
                level++;
                total -= 1000;
            }

            g.setLevel(level);
            g.setExp(total);
            mapper.update(g);
        } catch (Exception e) {
            logger.error("按名称增加宠物经验失败：petName={}, exp={}", petName, exp, e);
            throw new RuntimeException("增加宠物经验失败：" + e.getMessage());
        }
    }

    // ==================== 按petId方法（核心修复 + 完善日志） ====================
    /**
     * 按petId查询成长记录（增加容错 + 详细日志）
     */
    public PetGrowth getByPetId(Integer petId, String petName) {
        logger.info("开始查询宠物成长记录：petId={}, petName={}", petId, petName);
        System.out.println("\n===== 【调试】PetGrowthService.getByPetId =====");
        System.out.println("查询 petId=" + petId + " 的成长记录");

        // 参数校验
        if (petId == null || petId <= 0) {
            logger.error("宠物ID非法：petId={}", petId);
            throw new IllegalArgumentException("宠物ID必须为正整数");
        }
        if (petName == null || petName.trim().isEmpty()) {
            petName = "宠物" + petId; // 兜底名称
            logger.warn("宠物名称为空，自动兜底：{}", petName);
        }

        try {
            PetGrowth g = mapper.findByPetId(petId);
            if (g == null) {
                logger.info("未找到petId={}的成长记录，创建新记录", petId);
                System.out.println("未找到记录，创建新记录：petId=" + petId + ", petName=" + petName);
                mapper.createByPetId(petId, petName);
                g = mapper.findByPetId(petId);
            }

            if (g == null) {
                logger.error("创建petId={}的成长记录后仍未查询到", petId);
                throw new RuntimeException("创建宠物成长记录失败");
            }

            System.out.println("查询结果：id=" + g.getId() + ", petId=" + g.getPetId() + ", petName=" + g.getPetName() +
                    ", level=" + g.getLevel() + ", exp=" + g.getExp());
            System.out.println("===========================================\n");
            return g;
        } catch (Exception e) {
            logger.error("按petId查询/创建成长记录失败：petId={}", petId, e);
            throw new RuntimeException("查询宠物成长记录失败：" + e.getMessage());
        }
    }

    /**
     * 核心：按petId累加经验（含完整调试日志 + 异常捕获）
     */
    @Transactional(rollbackFor = Exception.class)
    public void addExpByPetId(Integer petId, String petName, int exp) {
        logger.info("开始给宠物增加成长经验：petId={}, petName={}, exp={}", petId, petName, exp);
        System.out.println("\n===== 【调试】PetGrowthService.addExpByPetId 开始 =====");
        System.out.println("处理 petId=" + petId + ", 经验+" + exp);

        // 参数校验
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("宠物ID必须为正整数");
        }
        if (exp <= 0) {
            throw new IllegalArgumentException("经验值必须为正整数");
        }
        if (petName == null || petName.trim().isEmpty()) {
            petName = "宠物" + petId;
        }

        try {
            // 1. 查询记录
            PetGrowth g = getByPetId(petId, petName);
            System.out.println("当前状态：level=" + g.getLevel() + ", exp=" + g.getExp());

            // 2. 计算新经验和等级
            int currentExp = g.getExp() == null ? 0 : g.getExp();
            int total = currentExp + exp;
            int level = g.getLevel() == null ? 1 : g.getLevel();
            System.out.println("累加后总经验：" + total);

            while (total >= 1000) {
                level++;
                total -= 1000;
                System.out.println("升级！新等级=" + level + ", 剩余经验=" + total);
                logger.info("宠物{}（ID:{}）升级：Lv{} → Lv{}", petName, petId, level-1, level);
            }

            // 3. 更新对象
            g.setLevel(level);
            g.setExp(total);
            System.out.println("准备更新：id=" + g.getId() + ", level=" + level + ", exp=" + total);

            // 4. 执行更新
            mapper.update(g);
            System.out.println("mapper.update 执行完成");

            // 5. 立即查询数据库，验证结果
            PetGrowth afterUpdate = mapper.findByPetId(petId);
            System.out.println("\n===== 【调试】数据库验证结果 =====");
            System.out.println("petId=" + petId + " 最新数据：");
            System.out.println("id=" + afterUpdate.getId() + ", petId=" + afterUpdate.getPetId());
            System.out.println("level=" + afterUpdate.getLevel() + ", exp=" + afterUpdate.getExp());
            System.out.println("====================================\n");

            logger.info("宠物{}（ID:{}）成长经验更新完成：当前等级Lv{}，经验{}", petName, petId, level, total);
            System.out.println("===== 【调试】PetGrowthService.addExpByPetId 结束 =====\n");
        } catch (Exception e) {
            logger.error("按petId增加成长经验失败：petId={}, exp={}", petId, exp, e);
            throw new RuntimeException("增加宠物成长经验失败：" + e.getMessage());
        }
    }
}