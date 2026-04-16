package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.aggregate.Role;
import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口
 * 定义角色持久化操作契约
 */
public interface RoleRepository {

    /**
     * 保存角色
     */
    Role save(Role role);

    /**
     * 根据ID查找角色
     */
    Optional<Role> findById(Long id);

    /**
     * 根据编码查找角色
     */
    Optional<Role> findByCode(String code);

    /**
     * 查找所有角色
     */
    List<Role> findAll();

    /**
     * 查找所有未删除的角色
     */
    List<Role> findAllNotDeleted();

    /**
     * 根据父角色ID查找子角色
     */
    List<Role> findByParentId(Long parentId);

    /**
     * 根据用户ID查找其拥有的角色
     */
    List<Role> findRolesByUserId(Long userId);

    /**
     * 根据用户ID查找其拥有的角色（包含权限）
     * 用于权限位图计算
     */
    List<Role> findRolesWithPermissionsByUserId(Long userId);

    /**
     * 根据用户ID查找其拥有的角色编码列表
     */
    List<String> findRoleCodesByUserId(Long userId);

    /**
     * 删除角色
     */
    void deleteById(Long id);

    /**
     * 递增版本号
     */
    void incrementVersion(Long roleId);
}