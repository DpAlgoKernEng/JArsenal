package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.entity.FieldPermission;
import java.util.List;

/**
 * 字段权限仓储接口
 * 定义字段权限持久化操作契约
 */
public interface FieldPermissionRepository {

    /**
     * 保存字段权限
     */
    FieldPermission save(FieldPermission permission);

    /**
     * 根据用户ID和资源ID查找字段权限
     * 通过 user_role -> role -> field_permission -> resource_field 关联查询
     */
    List<FieldPermission> findByUserIdAndResourceId(Long userId, Long resourceId);

    /**
     * 根据角色ID查找字段权限
     */
    List<FieldPermission> findByRoleId(Long roleId);

    /**
     * 根据字段ID查找字段权限
     */
    List<FieldPermission> findByFieldId(Long fieldId);

    /**
     * 删除角色的所有字段权限
     */
    void deleteByRoleId(Long roleId);

    /**
     * 删除字段的所有权限配置
     */
    void deleteByFieldId(Long fieldId);
}