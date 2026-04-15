package com.example.demo.domain.permission.repository;

import java.util.List;
import java.util.Set;

/**
 * 用户角色仓储接口
 * 定义用户与角色关联关系持久化操作契约
 */
public interface UserRoleRepository {

    /**
     * 为用户分配角色
     */
    void assignRole(Long userId, Long roleId);

    /**
     * 移除用户的角色
     */
    void removeRole(Long userId, Long roleId);

    /**
     * 根据用户ID查找其角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 根据角色ID查找拥有该角色的用户ID列表
     */
    List<Long> findUserIdsByRoleId(Long roleId);

    /**
     * 获取用户的所有角色ID集合
     */
    Set<Long> findUserRoleIds(Long userId);
}