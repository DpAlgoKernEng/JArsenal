package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.infrastructure.persistence.mapper.RoleMapper;
import com.example.demo.infrastructure.persistence.mapper.UserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository Implementation
 * Implements RoleRepository using MyBatis mappers
 */
@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    public RoleRepositoryImpl(RoleMapper roleMapper, UserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            roleMapper.insert(role);
        } else {
            roleMapper.update(role);
        }
        return role;
    }

    @Override
    public Optional<Role> findById(Long id) {
        Role role = roleMapper.findById(id);
        return Optional.ofNullable(role);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        Role role = roleMapper.findByCode(code);
        return Optional.ofNullable(role);
    }

    @Override
    public List<Role> findAll() {
        return roleMapper.findAllNotDeleted();
    }

    @Override
    public List<Role> findAllNotDeleted() {
        return roleMapper.findAllNotDeleted();
    }

    @Override
    public List<Role> findByParentId(Long parentId) {
        return roleMapper.findByParentId(parentId);
    }

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return roleMapper.findRolesByUserId(userId);
    }

    @Override
    public List<Role> findRolesWithPermissionsByUserId(Long userId) {
        return roleMapper.findRolesWithPermissionsByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        roleMapper.softDelete(id);
    }

    @Override
    public void incrementVersion(Long roleId) {
        roleMapper.incrementVersion(roleId);
    }
}