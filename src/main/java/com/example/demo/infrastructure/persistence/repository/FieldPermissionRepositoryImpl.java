package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.repository.FieldPermissionRepository;
import com.example.demo.infrastructure.persistence.mapper.FieldPermissionMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Field Permission Repository Implementation
 */
@Repository
public class FieldPermissionRepositoryImpl implements FieldPermissionRepository {

    private final FieldPermissionMapper fieldPermissionMapper;

    public FieldPermissionRepositoryImpl(FieldPermissionMapper fieldPermissionMapper) {
        this.fieldPermissionMapper = fieldPermissionMapper;
    }

    @Override
    public FieldPermission save(FieldPermission permission) {
        if (permission.getId() == null) {
            fieldPermissionMapper.insert(permission);
        } else {
            fieldPermissionMapper.update(permission);
        }
        return permission;
    }

    @Override
    public List<FieldPermission> findByUserIdAndResourceId(Long userId, Long resourceId) {
        return fieldPermissionMapper.findByUserIdAndResourceId(userId, resourceId);
    }

    @Override
    public List<FieldPermission> findByRoleId(Long roleId) {
        return fieldPermissionMapper.findByRoleId(roleId);
    }

    @Override
    public List<FieldPermission> findByFieldId(Long fieldId) {
        return fieldPermissionMapper.findByFieldId(fieldId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        fieldPermissionMapper.deleteByRoleId(roleId);
    }

    @Override
    public void deleteByFieldId(Long fieldId) {
        fieldPermissionMapper.deleteByFieldId(fieldId);
    }
}