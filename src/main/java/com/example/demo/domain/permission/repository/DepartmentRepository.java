package com.example.demo.domain.permission.repository;

import java.util.Set;

/**
 * 部门仓储接口
 * 用于部门层级查询（数据权限DEPT_TREE类型）
 */
public interface DepartmentRepository {

    /**
     * 获取部门的所有子部门ID（递归）
     */
    Set<Long> findAllSubDeptIds(Long deptId);

    /**
     * 获取部门层级路径
     */
    String getDeptPath(Long deptId);

    /**
     * 判断部门是否存在
     */
    boolean existsById(Long deptId);
}