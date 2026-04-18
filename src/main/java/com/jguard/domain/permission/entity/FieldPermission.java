package com.jguard.domain.permission.entity;

import com.jguard.domain.shared.common.BaseEntity;

/**
 * 字段权限实体
 * 用于定义角色对特定字段的访问权限
 * 对应数据库表 field_permission (role_id, field_id, can_view, can_edit)
 */
public class FieldPermission extends BaseEntity<Long> {

    private Long roleId;
    private Long fieldId;  // 关联 resource_field.id
    private boolean canView;
    private boolean canEdit;

    public FieldPermission() {
    }

    /**
     * 工厂方法：创建字段权限
     */
    public static FieldPermission create(Long roleId, Long fieldId, boolean canView, boolean canEdit) {
        FieldPermission perm = new FieldPermission();
        perm.roleId = roleId;
        perm.fieldId = fieldId;
        perm.canView = canView;
        perm.canEdit = canEdit;
        return perm;
    }

    /**
     * 是否可查看字段
     */
    public boolean canView() {
        return canView;
    }

    /**
     * 是否可编辑字段
     */
    public boolean canEdit() {
        return canEdit;
    }

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

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public boolean isCanView() {
        return canView;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}