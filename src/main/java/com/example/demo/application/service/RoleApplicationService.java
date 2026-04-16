package com.example.demo.application.service;

import com.example.demo.application.command.AssignPermissionCommand;
import com.example.demo.application.command.AssignRoleCommand;
import com.example.demo.application.command.CreateRoleCommand;
import com.example.demo.application.command.UpdateRoleCommand;
import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.entity.Permission;
import com.example.demo.domain.permission.repository.PermissionRepository;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.UserRoleRepository;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.infrastructure.persistence.mapper.PermissionActionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色应用服务
 * 协调领域对象完成角色管理业务用例
 */
@Service
@Transactional
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionActionMapper permissionActionMapper;

    public RoleApplicationService(RoleRepository roleRepository,
                                   PermissionRepository permissionRepository,
                                   UserRoleRepository userRoleRepository,
                                   PermissionActionMapper permissionActionMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.permissionActionMapper = permissionActionMapper;
    }

    /**
     * 创建角色
     */
    public Long createRole(CreateRoleCommand command) {
        RoleCode code = new RoleCode(command.getCode());
        InheritMode mode = command.getInheritMode() != null
            ? InheritMode.valueOf(command.getInheritMode())
            : InheritMode.EXTEND;

        // 检查编码是否已存在
        if (roleRepository.findByCode(command.getCode()).isPresent()) {
            throw new DomainException("角色编码已存在: " + command.getCode());
        }

        // 检查父角色是否存在（如果指定了父角色）
        if (command.getParentId() != null && command.getParentId() > 0) {
            Optional<Role> parentRole = roleRepository.findById(command.getParentId());
            if (parentRole.isEmpty()) {
                throw new DomainException("父角色不存在: " + command.getParentId());
            }
        }

        // 创建角色聚合根
        Role role = Role.create(code, command.getName(), command.getParentId(), mode);
        role.setSort(command.getSort() != null ? command.getSort() : 0);

        // 持久化
        roleRepository.save(role);

        return role.getId();
    }

    /**
     * 更新角色
     */
    public void updateRole(UpdateRoleCommand command) {
        Role role = roleRepository.findById(command.getRoleId())
            .orElseThrow(() -> new DomainException("角色不存在: " + command.getRoleId()));

        // 更新基本信息
        if (command.getName() != null) {
            role.setName(command.getName());
        }

        if (command.getParentId() != null) {
            // 检查是否会造成循环继承
            validateNoCircularInheritance(role.getId(), command.getParentId());
            role.setParentId(command.getParentId());
        }

        if (command.getInheritMode() != null) {
            role.setInheritMode(InheritMode.valueOf(command.getInheritMode()));
        }

        if (command.getStatus() != null) {
            RoleStatus status = RoleStatus.valueOf(command.getStatus());
            if (status == RoleStatus.ENABLED) {
                role.enable();
            } else {
                role.disable();
            }
        }

        if (command.getSort() != null) {
            role.setSort(command.getSort());
        }

        role.incrementVersion();
        roleRepository.save(role);
    }

    /**
     * 验证不会造成循环继承
     */
    private void validateNoCircularInheritance(Long roleId, Long newParentId) {
        if (newParentId == null || newParentId == 0L) {
            return;
        }

        // 不能将自己设为自己的父角色
        if (roleId.equals(newParentId)) {
            throw new DomainException("不能将角色设为自己的父角色");
        }

        // 检查新父角色的祖先链中是否包含当前角色
        Role parentRole = roleRepository.findById(newParentId)
            .orElseThrow(() -> new DomainException("父角色不存在: " + newParentId));

        Long currentParentId = parentRole.getParentId();
        while (currentParentId != null && currentParentId > 0L) {
            if (currentParentId.equals(roleId)) {
                throw new DomainException("会造成循环继承: 角色 " + roleId + " 不能作为角色 " + newParentId + " 的子角色");
            }
            Role ancestor = roleRepository.findById(currentParentId).orElse(null);
            if (ancestor == null) {
                break;
            }
            currentParentId = ancestor.getParentId();
        }
    }

    /**
     * 软删除角色
     */
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new DomainException("角色不存在: " + roleId));

        // 检查是否有用户使用此角色
        List<Long> users = userRoleRepository.findUserIdsByRoleId(roleId);
        if (!users.isEmpty()) {
            throw new DomainException("角色正在被使用，无法删除。关联用户数: " + users.size());
        }

        // 检查是否有子角色
        List<Role> children = roleRepository.findByParentId(roleId);
        if (!children.isEmpty()) {
            throw new DomainException("角色存在子角色，无法删除。子角色数: " + children.size());
        }

        role.softDelete();
        roleRepository.save(role);
    }

    /**
     * 根据ID查询角色
     */
    @Transactional(readOnly = true)
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId).orElse(null);
    }

    /**
     * 查询所有角色列表
     */
    @Transactional(readOnly = true)
    public List<Role> listAllRoles() {
        return roleRepository.findAllNotDeleted();
    }

    /**
     * 查询角色树结构
     */
    @Transactional(readOnly = true)
    public List<Role> getRoleTree() {
        return roleRepository.findAllNotDeleted();
    }

    /**
     * 根据用户ID查询角色列表
     */
    @Transactional(readOnly = true)
    public List<Role> getRolesByUserId(Long userId) {
        return roleRepository.findRolesByUserId(userId);
    }

    /**
     * 分配权限给角色
     */
    public void assignPermission(AssignPermissionCommand command) {
        Role role = roleRepository.findById(command.getRoleId())
            .orElseThrow(() -> new DomainException("角色不存在: " + command.getRoleId()));

        // 解析操作类型
        Set<ActionType> actions = command.getActions().stream()
            .map(ActionType::valueOf)
            .collect(Collectors.toSet());

        // 解析权限效果
        PermissionEffect effect = PermissionEffect.valueOf(command.getEffect());

        // 检查是否已有该资源的权限记录
        Permission existing = permissionRepository.findByRoleAndResource(role.getId(), command.getResourceId());

        if (existing != null) {
            // 更新现有权限：删除旧的action记录，插入新的
            permissionActionMapper.deleteByPermissionId(existing.getId());
            permissionActionMapper.batchInsert(existing.getId(), new ArrayList<>(actions));
            existing.setEffect(effect);
            permissionRepository.save(existing);
        } else {
            // 创建新权限
            Permission permission = Permission.create(role.getId(), command.getResourceId(), effect);
            permissionRepository.save(permission);
            permissionActionMapper.batchInsert(permission.getId(), new ArrayList<>(actions));
        }

        // 更新角色聚合（用于事件发布）
        role.assignPermission(command.getResourceId(), actions, effect);
        role.incrementVersion();
        roleRepository.save(role);
    }

    /**
     * 分配角色给用户
     */
    public void assignRolesToUser(AssignRoleCommand command) {
        // 验证角色是否存在且启用
        for (Long roleId : command.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new DomainException("角色不存在: " + roleId));

            if (role.getStatus() != RoleStatus.ENABLED) {
                throw new DomainException("角色已禁用: " + role.getName());
            }
        }

        // 获取用户当前的角色
        List<Long> currentRoleIds = userRoleRepository.findRoleIdsByUserId(command.getUserId());

        // 计算需要添加和移除的角色
        Set<Long> newRoleIds = Set.copyOf(command.getRoleIds());
        Set<Long> currentRoleSet = Set.copyOf(currentRoleIds);

        // 移除不在新列表中的角色
        for (Long roleId : currentRoleIds) {
            if (!newRoleIds.contains(roleId)) {
                userRoleRepository.removeRole(command.getUserId(), roleId);
            }
        }

        // 添加新角色
        for (Long roleId : command.getRoleIds()) {
            if (!currentRoleSet.contains(roleId)) {
                userRoleRepository.assignRole(command.getUserId(), roleId);
            }
        }
    }

    /**
     * 移除用户的某个角色
     */
    public void removeRoleFromUser(Long userId, Long roleId) {
        userRoleRepository.removeRole(userId, roleId);
    }

    /**
     * 获取用户的角色ID列表
     */
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleRepository.findRoleIdsByUserId(userId);
    }
}