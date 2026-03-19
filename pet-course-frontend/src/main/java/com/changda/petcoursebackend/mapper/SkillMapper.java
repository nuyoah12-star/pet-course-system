package com.changda.petcoursebackend.mapper;

import com.changda.petcoursebackend.entity.Skill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 技能数据访问接口
 * 基于MyBatis实现数据库操作
 */
@Mapper
public interface SkillMapper {

    /**
     * 查询所有技能（含层级关系）
     * @return 技能列表
     */
    @Select("SELECT id, name, description, parent_id AS parentId FROM skill ORDER BY id ASC")
    List<Skill> findAll();

    /**
     * 根据ID查询单个技能
     * @param id 技能ID
     * @return 技能详情
     */
    @Select("SELECT id, name, description, parent_id AS parentId FROM skill WHERE id = #{id}")
    Skill findById(Integer id);

    /**
     * 查询指定前置技能下的子技能（可选扩展）
     * @param parentId 前置技能ID
     * @return 子技能列表
     */
    @Select("SELECT id, name, description, parent_id AS parentId FROM skill WHERE parent_id = #{parentId}")
    List<Skill> findByParentId(Integer parentId);
}
