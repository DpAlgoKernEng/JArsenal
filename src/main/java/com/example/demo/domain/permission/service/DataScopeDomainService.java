package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.entity.DataScope;
import com.example.demo.domain.permission.entity.RoleDataScope;
import com.example.demo.domain.permission.entity.UserDataScope;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.RoleDataScopeRepository;
import com.example.demo.domain.permission.valueobject.RoleStatus;
import com.example.demo.domain.permission.valueobject.ScopeType;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 数据范围领域服务
 * 负责计算用户的数据权限范围
 */
@Service
public class DataScopeDomainService {

    private final RoleRepository roleRepository;
    private final RoleDataScopeRepository roleDataScopeRepository;

    public DataScopeDomainService(RoleRepository roleRepository,
                                   RoleDataScopeRepository roleDataScopeRepository) {
        this.roleRepository = roleRepository;
        this.roleDataScopeRepository = roleDataScopeRepository;
    }

    /**
     * 计算用户的数据权限范围
     * 多角色数据权限合并策略：取最大范围
     */
    public UserDataScope getUserDataScope(Long userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);

        if (roles.isEmpty()) {
            return new UserDataScope(userId, new HashMap<>());
        }

        Map<String, DataScope> mergedScopes = new HashMap<>();

        for (Role role : roles) {
            if (role.getStatus() != RoleStatus.ENABLED) {
                continue;
            }

            Map<String, DataScope> roleScopes = computeRoleDataScope(role);

            // 多角色数据权限合并策略：取最大范围
            for (Map.Entry<String, DataScope> entry : roleScopes.entrySet()) {
                String dimension = entry.getKey();
                DataScope existing = mergedScopes.get(dimension);

                if (existing == null) {
                    mergedScopes.put(dimension, entry.getValue());
                } else {
                    mergedScopes.put(dimension, mergeDataScope(existing, entry.getValue()));
                }
            }
        }

        return new UserDataScope(userId, mergedScopes);
    }

    /**
     * 计算角色的数据权限范围（含继承链）
     */
    private Map<String, DataScope> computeRoleDataScope(Role role) {
        Map<String, DataScope> scopes = new HashMap<>();

        // 继承父角色数据权限
        if (role.getParentId() != null) {
            Role parent = roleRepository.findById(role.getParentId()).orElse(null);
            if (parent != null && parent.getStatus() == RoleStatus.ENABLED) {
                scopes.putAll(computeRoleDataScope(parent));
            }
        }

        // 加上角色自身数据权限（覆盖继承）
        for (RoleDataScope rs : role.getOwnDataScopes()) {
            Set<Long> values = roleDataScopeRepository.findScopeValues(rs.getId());
            ScopeType scopeType = parseScopeType(rs.getScopeExpression());
            scopes.put(rs.getDimensionCode(),
                       new DataScope(rs.getDimensionCode(), scopeType, values));
        }

        return scopes;
    }

    /**
     * 数据权限合并策略：ALL > CUSTOM > DEPT_TREE > SELF_DEPT > SELF
     */
    private DataScope mergeDataScope(DataScope a, DataScope b) {
        if (a.getScopeType() == ScopeType.ALL || b.getScopeType() == ScopeType.ALL) {
            return DataScope.all(a.getDimensionCode());
        }

        if (a.getScopeType() == ScopeType.CUSTOM && b.getScopeType() == ScopeType.CUSTOM) {
            Set<Long> merged = new HashSet<>(a.getScopeValues());
            merged.addAll(b.getScopeValues());
            return DataScope.custom(a.getDimensionCode(), merged);
        }

        // 其他情况取范围更大的
        int priorityA = getScopePriority(a.getScopeType());
        int priorityB = getScopePriority(b.getScopeType());

        return priorityA >= priorityB ? a : b;
    }

    /**
     * 获取范围类型优先级（数值越大范围越广）
     */
    private int getScopePriority(ScopeType type) {
        return switch (type) {
            case ALL -> 5;
            case CUSTOM -> 4;
            case DEPT_TREE -> 3;
            case SELF_DEPT -> 2;
            case SELF -> 1;
        };
    }

    /**
     * 解析范围类型表达式
     */
    private ScopeType parseScopeType(String expression) {
        if (expression == null || expression.isEmpty()) {
            return ScopeType.SELF;
        }
        try {
            return ScopeType.valueOf(expression.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ScopeType.SELF;
        }
    }

    /**
     * 检查用户是否有访问特定数据的权限
     */
    public boolean hasDataAccess(Long userId, String dimensionCode, Long targetId) {
        UserDataScope userScope = getUserDataScope(userId);
        DataScope scope = userScope.getScope(dimensionCode);

        if (scope == null) {
            return false;
        }

        switch (scope.getScopeType()) {
            case ALL:
                return true;
            case CUSTOM:
                return scope.getScopeValues().contains(targetId);
            default:
                // SELF, SELF_DEPT, DEPT_TREE 需要结合用户上下文判断
                return false;
        }
    }
}