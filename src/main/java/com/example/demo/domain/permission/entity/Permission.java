package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.PermissionEffect;
import com.example.demo.domain.shared.common.BaseEntity;

/**
 * 权限实体 - 角色与资源的关联
 * 供PermissionRepository.findByRoleId返回使用
 */
public class Permission extends BaseEntity<Long> {

    private Long roleId;
    private Long resourceId;
    private PermissionEffect effect;

    /**
     * 创建权限
     */
    public static Permission create(Long roleId, Long resourceId, PermissionEffect effect) {
        Permission perm = new Permission();
        perm.roleId = roleId;
        perm.resourceId = resourceId;
        perm.effect = effect != null ? effect : PermissionEffect.ALLOW;
        return perm;
    }

    // Getter/Setter

    @Override
    public Long getId() {
        return super.getId();
    }

    public void setId(Long id) {
        super.setId(id);
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public PermissionEffect getEffect() {
        return effect;
    }

    public void setEffect(PermissionEffect effect) {
        this.effect = effect;
    }
}