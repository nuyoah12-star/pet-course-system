package com.changda.petcoursebackend.controller;

import com.changda.petcoursebackend.service.PetStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 宠物学习统计控制器
 * 提供课程学习次数、任务完成次数、经验成长趋势的统计接口
 * 支持按宠物ID精准查询，或查询所有宠物汇总数据
 *
 * @author 开发者名称
 * @date 2026-03-11
 */
@RestController
@RequestMapping("/stats")
@CrossOrigin(
        origins = {"http://localhost:5173", "http://localhost:8081"},
        allowCredentials = "true",
        methods = {RequestMethod.GET},
        allowedHeaders = "*"
)
public class StatsController {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);

    // 注入统计服务层（核心依赖）
    @Autowired
    private PetStatsService petStatsService;

    /**
     * 课程学习数量统计接口
     *
     * @param petId 宠物ID（可选，1=可乐，2=糯米，3=旺财，null=全部宠物）
     * @return Map<课程名称, 学习次数> 统计结果，异常时返回空数据
     */
    @GetMapping("/courseCount")
    public Map<String, Integer> courseCount(@RequestParam(required = false) Integer petId) {
        try {
            logger.info("查询课程学习次数，宠物ID：{}", petId);
            // 分情况查询：null=全部宠物，非null=指定宠物
            if (petId == null) {
                return petStatsService.getAllPetCourseCount();
            } else {
                return petStatsService.getCourseCountByPetId(petId);
            }
        } catch (Exception e) {
            logger.error("查询课程学习次数失败，宠物ID：{}，异常信息：{}", petId, e.getMessage(), e);
            // 异常时返回空数据，避免前端报错
            Map<String, Integer> emptyData = new HashMap<>();
            emptyData.put("坐下训练", 0);
            emptyData.put("握手训练", 0);
            emptyData.put("接飞盘", 0);
            emptyData.put("障碍赛", 0);
            return emptyData;
        }
    }

    /**
     * 任务完成数量统计接口
     *
     * @param petId 宠物ID（可选）
     * @return Map<任务名称, 完成次数> 统计结果，异常时返回空数据
     */
    @GetMapping("/taskCount")
    public Map<String, Integer> taskCount(@RequestParam(required = false) Integer petId) {
        try {
            logger.info("查询任务完成次数，宠物ID：{}", petId);
            if (petId == null) {
                return petStatsService.getAllPetTaskCount();
            } else {
                return petStatsService.getTaskCountByPetId(petId);
            }
        } catch (Exception e) {
            logger.error("查询任务完成次数失败，宠物ID：{}，异常信息：{}", petId, e.getMessage(), e);
            Map<String, Integer> emptyData = new HashMap<>();
            emptyData.put("坐下任务", 0);
            emptyData.put("握手任务", 0);
            emptyData.put("接飞盘任务", 0);
            emptyData.put("障碍赛任务", 0);
            return emptyData;
        }
    }

    /**
     * 宠物经验成长数据接口（周维度）
     *
     * @param petId 宠物ID（可选）
     * @return Map<周数, 经验变化量> 统计结果，异常时返回空数据
     */
    @GetMapping("/expGrowth")
    public Map<String, Integer> expGrowth(@RequestParam(required = false) Integer petId) {
        try {
            logger.info("查询经验成长趋势，宠物ID：{}", petId);
            if (petId == null) {
                return petStatsService.getAllPetExpGrowth();
            } else {
                return petStatsService.getExpGrowthByPetId(petId);
            }
        } catch (Exception e) {
            logger.error("查询经验成长趋势失败，宠物ID：{}，异常信息：{}", petId, e.getMessage(), e);
            Map<String, Integer> emptyData = new HashMap<>();
            emptyData.put("第1周", 0);
            emptyData.put("第2周", 0);
            emptyData.put("第3周", 0);
            emptyData.put("第4周", 0);
            return emptyData;
        }
    }

    /**
     * 兜底接口：防止404，返回友好提示
     */
    @GetMapping("/**")
    public Map<String, String> fallback() {
        Map<String, String> result = new HashMap<>();
        result.put("code", "404");
        result.put("msg", "接口不存在，请检查路径是否正确，支持的接口：/courseCount, /taskCount, /expGrowth");
        return result;
    }
}