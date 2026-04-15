package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.shared.common.BaseEntity;

/**
 * 字段权限实体
 * 用于定义角色对特定字段的访问权限
 */
public class FieldPermission extends BaseEntity<Long> {

    private Long roleId;
    private Long resourceId;
    private String fieldCode;
    private SensitiveLevel viewLevel;
    private boolean canEdit;

    public FieldPermission() {
    }

    public FieldPermission(Long roleId, Long resourceId, String fieldCode, SensitiveLevel viewLevel) {
        this.roleId = roleId;
        this.resourceId = resourceId;
        this.fieldCode = fieldCode;
        this.viewLevel = viewLevel;
        this.canEdit = false;
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

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public SensitiveLevel getViewLevel() {
        return viewLevel;
    }

    public void setViewLevel(SensitiveLevel viewLevel) {
        this.viewLevel = viewLevel;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
}