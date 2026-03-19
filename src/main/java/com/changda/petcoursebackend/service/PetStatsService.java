package com.changda.petcoursebackend.service;

import com.changda.petcoursebackend.mapper.CourseRecordMapper;
import com.changda.petcoursebackend.mapper.ExpRecordMapper;
import com.changda.petcoursebackend.mapper.TaskRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * 宠物统计服务层：处理课程、经验、任务的统计逻辑
 */
@Service
public class PetStatsService {

    // 注入Mapper层（确保Mapper已添加@Mapper注解或被@MapperScan扫描）
    @Autowired
    private CourseRecordMapper courseRecordMapper;

    @Autowired
    private ExpRecordMapper expRecordMapper;

    @Autowired
    private TaskRecordMapper taskRecordMapper;

    /**
     * 按宠物ID统计4门课程的学习次数
     * @param petId 宠物ID
     * @return 课程名称 -> 学习次数的映射（次数为null时返回0）
     */
    public Map<String, Integer> getCourseCountByPetId(Integer petId) {
        // 防御性编程：petId为空时返回空数据
        if (petId == null) {
            Map<String, Integer> emptyMap = new HashMap<>();
            emptyMap.put("坐下训练", 0);
            emptyMap.put("握手训练", 0);
            emptyMap.put("接飞盘", 0);
            emptyMap.put("障碍赛", 0);
            return emptyMap;
        }

        Map<String, Integer> result = new HashMap<>();
        // 查询并处理空值：数据库返回null时，默认显示0次
        Integer sitCount = courseRecordMapper.countByPetIdAndCourseName(petId, "坐下训练");
        result.put("坐下训练", sitCount == null ? 0 : sitCount);

        Integer shakeCount = courseRecordMapper.countByPetIdAndCourseName(petId, "握手训练");
        result.put("握手训练", shakeCount == null ? 0 : shakeCount);

        Integer frisbeeCount = courseRecordMapper.countByPetIdAndCourseName(petId, "接飞盘");
        result.put("接飞盘", frisbeeCount == null ? 0 : frisbeeCount);

        Integer obstacleCount = courseRecordMapper.countByPetIdAndCourseName(petId, "障碍赛");
        result.put("障碍赛", obstacleCount == null ? 0 : obstacleCount);

        return result;
    }

    /**
     * 按宠物ID统计每周经验变化量
     * @param petId 宠物ID
     * @return 周数 -> 经验变化量的映射（无数据时返回0）
     */
    public Map<String, Integer> getExpGrowthByPetId(Integer petId) {
        if (petId == null) {
            Map<String, Integer> emptyMap = new HashMap<>();
            emptyMap.put("第1周", 0);
            emptyMap.put("第2周", 0);
            emptyMap.put("第3周", 0);
            emptyMap.put("第4周", 0);
            return emptyMap;
        }

        Map<String, Integer> result = new HashMap<>();
        // 统计每周经验，空值默认0
        Integer week1Exp = expRecordMapper.sumExpByPetIdAndWeek(petId, 1);
        result.put("第1周", week1Exp == null ? 0 : week1Exp);

        Integer week2Exp = expRecordMapper.sumExpByPetIdAndWeek(petId, 2);
        result.put("第2周", week2Exp == null ? 0 : week2Exp);

        Integer week3Exp = expRecordMapper.sumExpByPetIdAndWeek(petId, 3);
        result.put("第3周", week3Exp == null ? 0 : week3Exp);

        Integer week4Exp = expRecordMapper.sumExpByPetIdAndWeek(petId, 4);
        result.put("第4周", week4Exp == null ? 0 : week4Exp);

        return result;
    }

    /**
     * 按宠物ID统计4类任务的完成次数
     * @param petId 宠物ID
     * @return 任务名称 -> 完成次数的映射（无数据时返回0）
     */
    public Map<String, Integer> getTaskCountByPetId(Integer petId) {
        if (petId == null) {
            Map<String, Integer> emptyMap = new HashMap<>();
            emptyMap.put("坐下任务", 0);
            emptyMap.put("握手任务", 0);
            emptyMap.put("接飞盘任务", 0);
            emptyMap.put("障碍赛任务", 0);
            return emptyMap;
        }

        Map<String, Integer> result = new HashMap<>();
        // 查询任务次数，空值默认0
        Integer sitTaskCount = taskRecordMapper.countByPetIdAndTaskName(petId, "坐下任务");
        result.put("坐下任务", sitTaskCount == null ? 0 : sitTaskCount);

        Integer shakeTaskCount = taskRecordMapper.countByPetIdAndTaskName(petId, "握手任务");
        result.put("握手任务", shakeTaskCount == null ? 0 : shakeTaskCount);

        Integer frisbeeTaskCount = taskRecordMapper.countByPetIdAndTaskName(petId, "接飞盘任务");
        result.put("接飞盘任务", frisbeeTaskCount == null ? 0 : frisbeeTaskCount);

        Integer obstacleTaskCount = taskRecordMapper.countByPetIdAndTaskName(petId, "障碍赛任务");
        result.put("障碍赛任务", obstacleTaskCount == null ? 0 : obstacleTaskCount);

        return result;
    }

    /**
     * 统计所有宠物的课程汇总数据（用于前端"全部宠物"选项）
     * @return 课程名称 -> 所有宠物学习次数总和
     */
    public Map<String, Integer> getAllPetCourseCount() {
        Map<String, Integer> result = new HashMap<>();
        // 处理空值：COUNT(*)返回0，无需判空
        result.put("坐下训练", courseRecordMapper.countAllByCourseName("坐下训练"));
        result.put("握手训练", courseRecordMapper.countAllByCourseName("握手训练"));
        result.put("接飞盘", courseRecordMapper.countAllByCourseName("接飞盘"));
        result.put("障碍赛", courseRecordMapper.countAllByCourseName("障碍赛"));
        return result;
    }

    /**
     * 统计所有宠物的经验汇总数据
     */
    public Map<String, Integer> getAllPetExpGrowth() {
        Map<String, Integer> result = new HashMap<>();
        result.put("第1周", expRecordMapper.sumAllExpByWeek(1));
        result.put("第2周", expRecordMapper.sumAllExpByWeek(2));
        result.put("第3周", expRecordMapper.sumAllExpByWeek(3));
        result.put("第4周", expRecordMapper.sumAllExpByWeek(4));
        return result;
    }

    /**
     * 统计所有宠物的任务汇总数据
     */
    public Map<String, Integer> getAllPetTaskCount() {
        Map<String, Integer> result = new HashMap<>();
        result.put("坐下任务", taskRecordMapper.countAllByTaskName("坐下任务"));
        result.put("握手任务", taskRecordMapper.countAllByTaskName("握手任务"));
        result.put("接飞盘任务", taskRecordMapper.countAllByTaskName("接飞盘任务"));
        result.put("障碍赛任务", taskRecordMapper.countAllByTaskName("障碍赛任务"));
        return result;
    }
}