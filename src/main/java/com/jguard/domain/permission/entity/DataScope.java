package com.jguard.domain.permission.entity;

import com.jguard.domain.permission.valueobject.ScopeType;
import java.util.Collections;
import java.util.Set;

/**
 * 数据范围值对象
 * 用于定义数据权限的范围类型和具体值
 */
public class DataScope {

    private final String dimensionCode;
    private final ScopeType scopeType;
    private final Set<Long> scopeValues;

    public DataScope(String dimensionCode, ScopeType scopeType, Set<Long> scopeValues) {
        this.dimensionCode = dimensionCode;
        this.scopeType = scopeType;
        this.scopeValues = scopeValues != null ? Set.copyOf(scopeValues) : Set.of();
    }

    /**
     * 创建全部数据范围
     */
    public static DataScope all(String dimensionCode) {
        return new DataScope(dimensionCode, ScopeType.ALL, Set.of());
    }

    /**
     * 创建仅本人数据范围
     */
    public static DataScope self(String dimensionCode) {
        return new DataScope(dimensionCode, ScopeType.SELF, Set.of());
    }

    /**
     * 创建本部门数据范围
     */
    public static DataScope selfDept(String dimensionCode) {
        return new DataScope(dimensionCode, ScopeType.SELF_DEPT, Set.of());
    }

    /**
     * 创建部门及子部门数据范围
     */
    public static DataScope deptTree(String dimensionCode, Set<Long> deptIds) {
        return new DataScope(dimensionCode, ScopeType.DEPT_TREE, deptIds);
    }

    /**
     * 创建自定义数据范围
     */
    public static DataScope custom(String dimensionCode, Set<Long> values) {
        return new DataScope(dimensionCode, ScopeType.CUSTOM, values);
    }

    /**
     * 判断是否有权限范围定义
     */
    public boolean hasScopeValues() {
        return scopeValues != null && !scopeValues.isEmpty();
    }

    public String getDimensionCode() {
        return dimensionCode;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public Set<Long> getScopeValues() {
        return Collections.unmodifiableSet(scopeValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataScope that)) return false;
        return dimensionCode.equals(that.dimensionCode) && scopeType == that.scopeType;
    }

    @Override
    public int hashCode() {
        return dimensionCode.hashCode() + scopeType.hashCode();
    }
}