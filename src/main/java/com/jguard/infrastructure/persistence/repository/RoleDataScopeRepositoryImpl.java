package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.entity.RoleDataScope;
import com.jguard.domain.permission.repository.RoleDataScopeRepository;
import com.jguard.infrastructure.persistence.mapper.RoleDataScopeMapper;
import com.jguard.infrastructure.persistence.mapper.RoleDataScopeValueMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 角色数据范围仓储实现
 */
@Repository
public class RoleDataScopeRepositoryImpl implements RoleDataScopeRepository {

    private final RoleDataScopeMapper scopeMapper;
    private final RoleDataScopeValueMapper valueMapper;

    public RoleDataScopeRepositoryImpl(RoleDataScopeMapper scopeMapper,
                                        RoleDataScopeValueMapper valueMapper) {
        this.scopeMapper = scopeMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public RoleDataScope save(RoleDataScope scope) {
        if (scope.getId() == null) {
            scopeMapper.insert(scope);
        } else {
            // 更新时先删除旧值再插入新值
            scopeMapper.deleteByRoleAndDimension(scope.getRoleId(), scope.getDimensionCode());
            scopeMapper.insert(scope);
        }
        return scope;
    }

    @Override
    public Optional<RoleDataScope> findById(Long scopeId) {
        RoleDataScope scope = scopeMapper.findById(scopeId);
        return Optional.ofNullable(scope);
    }

    @Override
    public List<RoleDataScope> findByRoleId(Long roleId) {
        return scopeMapper.findByRoleId(roleId);
    }

    @Override
    public Set<Long> findScopeValues(Long scopeId) {
        return valueMapper.findByScopeId(scopeId);
    }

    @Override
    public void deleteByRoleAndDimension(Long roleId, String dimensionCode) {
        // 先删除范围值
        List<RoleDataScope> scopes = scopeMapper.findByRoleId(roleId);
        for (RoleDataScope scope : scopes) {
            if (scope.getDimensionCode().equals(dimensionCode)) {
                valueMapper.deleteByScopeId(scope.getId());
            }
        }
        scopeMapper.deleteByRoleAndDimension(roleId, dimensionCode);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        // 先删除所有范围值
        List<RoleDataScope> scopes = scopeMapper.findByRoleId(roleId);
        for (RoleDataScope scope : scopes) {
            valueMapper.deleteByScopeId(scope.getId());
        }
        scopeMapper.deleteByRoleId(roleId);
    }
}