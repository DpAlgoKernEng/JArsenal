package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.entity.RoleDataScope;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色数据范围仓储接口
 */
public interface RoleDataScopeRepository {

    /**
     * 保存角色数据范围
     */
    RoleDataScope save(RoleDataScope scope);

    /**
     * 根据ID查找
     */
    Optional<RoleDataScope> findById(Long scopeId);

    /**
     * 根据角色ID查找所有数据范围
     */
    List<RoleDataScope> findByRoleId(Long roleId);

    /**
     * 查找数据范围的具体值
     */
    Set<Long> findScopeValues(Long scopeId);

    /**
     * 删除角色的指定维度数据范围
     */
    void deleteByRoleAndDimension(Long roleId, String dimensionCode);

    /**
     * 删除角色的所有数据范围
     */
    void deleteByRoleId(Long roleId);
}