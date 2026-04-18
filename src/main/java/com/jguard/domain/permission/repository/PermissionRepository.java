package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.entity.Permission;
import java.util.List;

/**
 * 权限仓储接口
 * 定义权限持久化操作契约
 */
public interface PermissionRepository {

    /**
     * 保存权限
     */
    Permission save(Permission permission);

    /**
     * 根据角色ID查找权限列表
     */
    List<Permission> findByRoleId(Long roleId);

    /**
     * 根据角色和资源查找权限
     */
    Permission findByRoleAndResource(Long roleId, Long resourceId);

    /**
     * 删除指定角色和资源的权限
     */
    void deleteByRoleAndResource(Long roleId, Long resourceId);

    /**
     * 删除角色的所有权限
     */
    void deleteByRoleId(Long roleId);
}