package com.jguard.infrastructure.persistence.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门 Mapper
 * 用于部门层级查询（数据权限DEPT_TREE类型）
 */
@Mapper
public interface DepartmentMapper {

    /**
     * 递归查询部门的所有子部门ID
     * 使用 MySQL 8.0 WITH RECURSIVE 语法
     */
    @Select("""
        WITH RECURSIVE dept_tree AS (
            SELECT id FROM department WHERE id = #{deptId} AND is_deleted = 0
            UNION ALL
            SELECT d.id FROM department d
            INNER JOIN dept_tree dt ON d.parent_id = dt.id
            WHERE d.is_deleted = 0
        )
        SELECT id FROM dept_tree WHERE id != #{deptId}
        """)
    Set<Long> findAllSubDeptIds(@Param("deptId") Long deptId);

    /**
     * 查询部门路径（用于层级展示）
     */
    @Select("""
        WITH RECURSIVE dept_path AS (
            SELECT id, parent_id, name, 1 as level FROM department WHERE id = #{deptId}
            UNION ALL
            SELECT d.id, d.parent_id, d.name, dp.level + 1
            FROM department d
            INNER JOIN dept_path dp ON d.id = dp.parent_id
            WHERE d.is_deleted = 0
        )
        SELECT * FROM dept_path ORDER BY level DESC
        """)
    List<Map<String, Object>> getDeptPath(@Param("deptId") Long deptId);

    /**
     * 查询部门基本信息
     */
    @Select("SELECT id, name, parent_id FROM department WHERE id = #{deptId} AND is_deleted = 0")
    Map<String, Object> findById(@Param("deptId") Long deptId);

    /**
     * 查询用户所属部门ID
     */
    @Select("SELECT dept_id FROM user WHERE id = #{userId} AND is_deleted = 0")
    Long findUserDeptId(@Param("userId") Long userId);

    /**
     * 判断部门是否存在
     */
    @Select("SELECT COUNT(*) FROM department WHERE id = #{deptId} AND is_deleted = 0")
    int existsById(@Param("deptId") Long deptId);
}