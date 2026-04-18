package com.jguard.domain.permission.service;

import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.valueobject.*;
import com.jguard.domain.permission.repository.RoleRepository;
import com.jguard.domain.permission.repository.PermissionRepository;
import com.jguard.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 权限领域服务
 * 负责权限位图计算，优化性能从O(n^2)到O(n)
 */
@Service
public class PermissionDomainService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public PermissionDomainService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * 计算用户权限位图（预计算优化）
     * 性能从O(n^2)优化到O(n)
     */
    public PermissionBitmap computeUserPermissionBitmap(Long userId) {
        // 使用带权限的角色查询
        List<Role> roles = roleRepository.findRolesWithPermissionsByUserId(userId);

        if (roles.isEmpty()) {
            return PermissionBitmap.empty(System.currentTimeMillis());
        }

        long version = computeVersion(roles);

        PermissionBitmap result = PermissionBitmap.empty(version);
        PermissionBitmap denyBitmap = PermissionBitmap.empty(version);

        for (Role role : roles) {
            if (role.getStatus() != RoleStatus.ENABLED) {
                continue;
            }

            PermissionBitmap roleBitmap = computeRolePermissionBitmap(role);

            if (role.hasDenyPermissions()) {
                denyBitmap = denyBitmap.merge(role.getDenyBitmap());
            }
            result = result.merge(roleBitmap);
        }

        result = result.applyDeny(denyBitmap);

        return result;
    }

    /**
     * 计算角色权限位图（含继承链）
     */
    private PermissionBitmap computeRolePermissionBitmap(Role role) {
        PermissionBitmap bitmap = PermissionBitmap.empty();

        // 递归获取父角色权限位图
        if (role.getParentId() != null) {
            validateNoCircularInheritance(role);

            Role parent = roleRepository.findById(role.getParentId()).orElse(null);
            if (parent != null && parent.getStatus() == RoleStatus.ENABLED) {
                PermissionBitmap parentBitmap = computeRolePermissionBitmap(parent);

                bitmap = bitmap.merge(parentBitmap);

                if (role.getInheritMode() == InheritMode.LIMIT) {
                    bitmap = bitmap.applyDeny(role.getDenyBitmap());
                }
            }
        }

        // 加上角色自身权限
        for (RolePermission perm : role.getOwnPermissions()) {
            if (perm.getEffect() == PermissionEffect.ALLOW) {
                bitmap = bitmap.addPermission(perm.getResourceId(), perm.getActions());
            }
        }

        return bitmap;
    }

    /**
     * 验证无循环继承
     */
    private void validateNoCircularInheritance(Role role) {
        Set<Long> visited = new HashSet<>();
        Long current = role.getParentId();

        while (current != null) {
            if (visited.contains(current)) {
                throw new DomainException("角色继承存在循环引用: " + role.getCode().value());
            }
            if (current.equals(role.getId())) {
                throw new DomainException("角色继承存在循环引用: " + role.getCode().value());
            }
            visited.add(current);

            Role parent = roleRepository.findById(current).orElse(null);
            if (parent == null) break;
            current = parent.getParentId();
        }
    }

    /**
     * 计算用户权限版本（角色版本之和）
     */
    private long computeVersion(List<Role> roles) {
        return roles.stream()
            .mapToLong(Role::getVersion)
            .sum();
    }
}