package com.jguard.domain.permission.service;

import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.entity.FieldAccessor;
import com.jguard.domain.permission.entity.FieldPermission;
import com.jguard.domain.permission.entity.ResourceField;
import com.jguard.domain.permission.repository.FieldPermissionRepository;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.RoleRepository;
import com.jguard.domain.permission.valueobject.InheritMode;
import com.jguard.domain.permission.valueobject.RoleCode;
import com.jguard.domain.permission.valueobject.RoleStatus;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldPermissionDomainServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceFieldRepository resourceFieldRepository;

    @Mock
    private FieldPermissionRepository fieldPermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    private FieldPermissionDomainService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new FieldPermissionDomainService(
                resourceRepository,
                resourceFieldRepository,
                fieldPermissionRepository,
                roleRepository
        );
    }

    @Test
    void shouldInheritFieldPermissionsFromParentRole() {
        // 父角色：有字段权限
        Role parentRole = Role.create(new RoleCode("MANAGER"), "管理员", null, InheritMode.EXTEND);
        parentRole.setId(1L);
        parentRole.setStatus(RoleStatus.ENABLED);

        FieldPermission salaryPerm = FieldPermission.create(1L, 100L, true, false); // 可查看薪资
        FieldPermission phonePerm = FieldPermission.create(1L, 101L, true, true);   // 可查看和编辑手机号

        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        when(fieldPermissionRepository.findByRoleId(1L)).thenReturn(List.of(salaryPerm, phonePerm));

        // 子角色：继承父角色
        Role childRole = Role.create(new RoleCode("USER"), "普通用户", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.setStatus(RoleStatus.ENABLED);

        // 子角色 own 权限：仅可查看手机号，不可编辑
        FieldPermission childPhonePerm = FieldPermission.create(2L, 101L, true, false);
        when(fieldPermissionRepository.findByRoleId(2L)).thenReturn(List.of(childPhonePerm));

        // 计算继承后的字段权限
        Map<Long, FieldPermission> inheritedPerms = service.computeFieldPermissions(childRole);

        // 验证继承结果
        // 薪资：继承父角色权限（可查看，不可编辑）
        assertTrue(inheritedPerms.containsKey(100L));
        assertTrue(inheritedPerms.get(100L).canView());
        assertFalse(inheritedPerms.get(100L).canEdit());

        // 手机号：子角色 own 权限覆盖继承（可查看，不可编辑）
        assertTrue(inheritedPerms.containsKey(101L));
        assertTrue(inheritedPerms.get(101L).canView());
        assertFalse(inheritedPerms.get(101L).canEdit()); // 被子角色 own 权限覆盖
    }

    @Test
    void shouldMergeFieldPermissionsFromMultipleRoles() {
        // 用户拥有两个角色
        Role role1 = Role.create(new RoleCode("ROLE1"), "角色1", null, null);
        role1.setId(1L);
        role1.setStatus(RoleStatus.ENABLED);
        FieldPermission perm1 = FieldPermission.create(1L, 100L, true, false); // 可查看
        when(fieldPermissionRepository.findByRoleId(1L)).thenReturn(List.of(perm1));

        Role role2 = Role.create(new RoleCode("ROLE2"), "角色2", null, null);
        role2.setId(2L);
        role2.setStatus(RoleStatus.ENABLED);
        FieldPermission perm2 = FieldPermission.create(2L, 100L, false, true); // 可编辑
        when(fieldPermissionRepository.findByRoleId(2L)).thenReturn(List.of(perm2));

        when(roleRepository.findRolesByUserId(10L)).thenReturn(List.of(role1, role2));

        // 计算用户字段权限（多角色宽松合并）
        Map<Long, FieldPermission> mergedPerms = service.computeUserFieldPermissions(10L, 1L);

        // 验收标准：多角色权限取宽松策略（任意角色有权限则合并后有权限）
        assertTrue(mergedPerms.containsKey(100L));
        assertTrue(mergedPerms.get(100L).canView());  // role1 有查看权限
        assertTrue(mergedPerms.get(100L).canEdit());  // role2 有编辑权限
    }

    @Test
    void shouldNotInheritFromDisabledParentRole() {
        // 父角色已禁用
        Role disabledParent = Role.create(new RoleCode("DISABLED"), "已禁用", null, null);
        disabledParent.setId(1L);
        disabledParent.setStatus(RoleStatus.DISABLED);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(disabledParent));
        when(fieldPermissionRepository.findByRoleId(1L)).thenReturn(List.of());

        // 子角色
        Role childRole = Role.create(new RoleCode("CHILD"), "子角色", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.setStatus(RoleStatus.ENABLED);
        when(fieldPermissionRepository.findByRoleId(2L)).thenReturn(List.of());

        Map<Long, FieldPermission> inheritedPerms = service.computeFieldPermissions(childRole);

        // 验收标准：禁用角色的权限不应被继承
        assertTrue(inheritedPerms.isEmpty());
    }

    @Test
    void shouldReturnEmptyPermissionsForNoRoles() {
        when(roleRepository.findRolesByUserId(1L)).thenReturn(List.of());

        Map<Long, FieldPermission> perms = service.computeUserFieldPermissions(1L, 1L);

        assertTrue(perms.isEmpty());
    }

    @Test
    void shouldSkipDisabledRoles() {
        Role disabledRole = Role.create(new RoleCode("DISABLED"), "禁用", null, null);
        disabledRole.setId(1L);
        disabledRole.setStatus(RoleStatus.DISABLED);
        FieldPermission perm = FieldPermission.create(1L, 100L, true, true);
        when(fieldPermissionRepository.findByRoleId(1L)).thenReturn(List.of(perm));

        Role enabledRole = Role.create(new RoleCode("ENABLED"), "启用", null, null);
        enabledRole.setId(2L);
        enabledRole.setStatus(RoleStatus.ENABLED);
        FieldPermission perm2 = FieldPermission.create(2L, 200L, true, false);
        when(fieldPermissionRepository.findByRoleId(2L)).thenReturn(List.of(perm2));

        when(roleRepository.findRolesByUserId(10L)).thenReturn(List.of(disabledRole, enabledRole));

        Map<Long, FieldPermission> perms = service.computeUserFieldPermissions(10L, 1L);

        // 禁用角色的权限不应被包含
        assertFalse(perms.containsKey(100L));
        assertTrue(perms.containsKey(200L));
    }
}