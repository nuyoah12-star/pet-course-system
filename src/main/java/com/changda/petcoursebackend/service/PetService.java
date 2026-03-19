package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.entity.Pet;
import com.changda.petcoursebackend.mapper.PetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 宠物服务层
 * 处理宠物相关的业务逻辑
 */
@Service
public class PetService {

    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    @Autowired
    private PetMapper petMapper;

    /**
     * 查询所有宠物
     * @return 宠物列表
     */
    public List<Pet> getAllPets() {
        return petMapper.findAll();
    }

    /**
     * 根据ID查询单个宠物（方案一核心方法，已存在）
     * @param petId 宠物ID
     * @return 宠物实体
     */
    public Pet getPetById(Integer petId) {
        return petMapper.findById(petId);
    }

    /**
     * 获取宠物排行榜（按经验降序取前10名）
     * @return 排名前10的宠物列表
     */
    public List<Pet> getPetRankList() {
        return petMapper.rankList();
    }

    /**
     * 新增宠物
     * @param pet 宠物实体
     */
    public void addPet(Pet pet) {
        petMapper.insert(pet);
    }

    // ========== 核心新增：更新宠物经验方法 ==========
    /**
     * 给宠物增加经验值（TaskController调用的核心方法）
     * @param petId 宠物ID
     * @param exp 增加的经验值
     */
    public void addExpToPet(Integer petId, Integer exp) {
        // 1. 参数校验
        if (petId == null || petId <= 0) {
            logger.error("宠物ID非法：petId={}", petId);
            throw new IllegalArgumentException("宠物ID必须为正整数");
        }
        if (exp == null || exp <= 0) {
            logger.error("经验值非法：exp={}", exp);
            throw new IllegalArgumentException("经验值必须为正整数");
        }

        // 2. 查询宠物是否存在
        Pet pet = petMapper.findById(petId);
        if (pet == null) {
            logger.error("未找到宠物：petId={}", petId);
            throw new RuntimeException("未找到ID为" + petId + "的宠物");
        }

        // 3. 计算新经验值
        Integer currentExp = pet.getExp() == null ? 0 : pet.getExp();
        Integer newExp = currentExp + exp;
        logger.info("宠物{}（ID:{}）经验更新：{} + {} = {}", pet.getName(), petId, currentExp, exp, newExp);

        // 4. 更新宠物经验（关键：调用mapper的更新方法）
        pet.setExp(newExp);
        // 可选：自动升级逻辑（如果需要）
        updatePetLevel(pet);

        // 5. 执行数据库更新
        int updateCount = petMapper.updateById(pet);
        if (updateCount == 0) {
            logger.error("宠物经验更新失败：petId={}", petId);
            throw new RuntimeException("更新宠物经验失败，数据库未生效");
        }
        logger.info("宠物{}（ID:{}）经验更新成功，当前经验：{}", pet.getName(), petId, newExp);
    }

    // 可选：宠物自动升级逻辑（根据经验值升级）
    private void updatePetLevel(Pet pet) {
        Integer exp = pet.getExp() == null ? 0 : pet.getExp();
        // 示例：每100经验升1级
        Integer newLevel = exp / 100 + 1;
        if (!newLevel.equals(pet.getLevel())) {
            pet.setLevel(newLevel);
            logger.info("宠物{}（ID:{}）升级：Lv{} → Lv{}", pet.getName(), pet.getId(), pet.getLevel(), newLevel);
        }
    }
}