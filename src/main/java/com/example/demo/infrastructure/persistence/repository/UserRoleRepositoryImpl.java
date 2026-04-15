package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.permission.repository.UserRoleRepository;
import com.example.demo.infrastructure.persistence.mapper.UserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * User Role Repository Implementation
 * Implements UserRoleRepository using MyBatis mapper
 */
@Repository
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleMapper userRoleMapper;

    public UserRoleRepositoryImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        if (userRoleMapper.existsByUserIdAndRoleId(userId, roleId) == 0) {
            userRoleMapper.insert(userId, roleId);
        }
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        userRoleMapper.delete(userId, roleId);
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return userRoleMapper.findRoleIdsByUserId(userId);
    }

    @Override
    public List<Long> findUserIdsByRoleId(Long roleId) {
        return userRoleMapper.findUserIdsByRoleId(roleId);
    }

    @Override
    public Set<Long> findUserRoleIds(Long userId) {
        return userRoleMapper.findUserRoleIds(userId);
    }
}