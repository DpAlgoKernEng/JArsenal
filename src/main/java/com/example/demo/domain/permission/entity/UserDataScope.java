package com.example.demo.domain.permission.entity;

import com.example.demo.domain.permission.valueobject.ScopeType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户组合数据范围
 * 汇聚用户所有角色的数据权限范围
 */
public class UserDataScope {

    private final Long userId;
    private final Map<String, DataScope> scopes;

    public UserDataScope(Long userId, Map<String, DataScope> scopes) {
        this.userId = userId;
        this.scopes = scopes != null ? new HashMap<>(scopes) : new HashMap<>();
    }

    /**
     * 获取指定维度的数据范围
     */
    public DataScope getScope(String dimensionCode) {
        return scopes.get(dimensionCode);
    }

    /**
     * 获取所有数据范围
     */
    public Map<String, DataScope> getAllScopes() {
        return Collections.unmodifiableMap(scopes);
    }

    /**
     * 判断是否有指定维度的数据权限
     */
    public boolean hasDimension(String dimensionCode) {
        return scopes.containsKey(dimensionCode);
    }

    /**
     * 判断是否有全部数据权限（任意维度为ALL）
     */
    public boolean hasAllScope() {
        return scopes.values().stream()
            .anyMatch(scope -> scope.getScopeType() == ScopeType.ALL);
    }

    public Long getUserId() {
        return userId;
    }
}