package com.jguard.infrastructure.persistence.repository;

import com.jguard.domain.permission.entity.Permission;
import com.jguard.domain.permission.repository.PermissionRepository;
import com.jguard.infrastructure.persistence.mapper.PermissionMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionMapper permissionMapper;

    public PermissionRepositoryImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Permission save(Permission permission) {
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    public List<Permission> findByRoleId(Long roleId) {
        return permissionMapper.findByRoleId(roleId);
    }

    @Override
    public Permission findByRoleAndResource(Long roleId, Long resourceId) {
        return permissionMapper.findByRoleAndResource(roleId, resourceId);
    }

    @Override
    public void deleteByRoleAndResource(Long roleId, Long resourceId) {
        permissionMapper.deleteByRoleAndResource(roleId, resourceId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        permissionMapper.deleteByRoleId(roleId);
    }
}