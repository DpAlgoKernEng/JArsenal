package com.example.demo.domain.permission.entity;

import com.example.demo.domain.shared.common.BaseEntity;

/**
 * 角色数据范围实体
 * 用于定义角色的数据权限范围
 */
public class RoleDataScope extends BaseEntity<Long> {

    private Long roleId;
    private String dimensionCode;
    private String dimensionValue;
    private String scopeExpression;

    public RoleDataScope() {
    }

    public RoleDataScope(Long roleId, String dimensionCode, String dimensionValue) {
        this.roleId = roleId;
        this.dimensionCode = dimensionCode;
        this.dimensionValue = dimensionValue;
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

    public String getDimensionCode() {
        return dimensionCode;
    }

    public void setDimensionCode(String dimensionCode) {
        this.dimensionCode = dimensionCode;
    }

    public String getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(String dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public String getScopeExpression() {
        return scopeExpression;
    }

    public void setScopeExpression(String scopeExpression) {
        this.scopeExpression = scopeExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleDataScope that)) return false;
        return dimensionCode != null && dimensionCode.equals(that.dimensionCode);
    }

    @Override
    public int hashCode() {
        return dimensionCode != null ? dimensionCode.hashCode() : 0;
    }
}