package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionDomainServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    private PermissionDomainService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new PermissionDomainService(roleRepository, permissionRepository);
    }

    @Test
    void shouldComputeEmptyBitmapForNoRoles() {
        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of());

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertFalse(bitmap.hasAction(1L, ActionType.VIEW));
    }

    @Test
    void shouldComputeSingleRoleBitmap() {
        Role role = Role.create(new RoleCode("USER"), "用户", null, null);
        role.setId(1L);
        role.assignPermission(100L, Set.of(ActionType.VIEW, ActionType.CREATE), PermissionEffect.ALLOW);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(role));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(100L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(100L, ActionType.DELETE));
    }

    @Test
    void shouldMergeMultipleRoles() {
        Role role1 = Role.create(new RoleCode("USER"), "用户", null, null);
        role1.setId(1L);
        role1.assignPermission(100L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);

        Role role2 = Role.create(new RoleCode("MANAGER"), "管理员", null, null);
        role2.setId(2L);
        role2.assignPermission(100L, Set.of(ActionType.CREATE, ActionType.DELETE), PermissionEffect.ALLOW);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(role1, role2));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(100L, ActionType.CREATE));
        assertTrue(bitmap.hasAction(100L, ActionType.DELETE));
    }

    @Test
    void shouldApplyDenyFromMultipleRoles() {
        // Role 1 has ALLOW permissions on resource 100
        Role allowRole = Role.create(new RoleCode("USER"), "用户", null, null);
        allowRole.setId(1L);
        allowRole.assignPermission(100L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.DELETE), PermissionEffect.ALLOW);

        // Role 2 has DENY permission on the same resource 100
        Role denyRole = Role.create(new RoleCode("LIMITED"), "受限角色", null, null);
        denyRole.setId(2L);
        denyRole.assignPermission(100L, Set.of(ActionType.DELETE), PermissionEffect.DENY);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(allowRole, denyRole));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(100L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(100L, ActionType.DELETE)); // DENY from Role 2 removes DELETE
    }

    @Test
    void shouldSkipDisabledRoles() {
        Role enabledRole = Role.create(new RoleCode("USER"), "用户", null, null);
        enabledRole.setId(1L);
        enabledRole.assignPermission(100L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);

        Role disabledRole = Role.create(new RoleCode("GUEST"), "访客", null, null);
        disabledRole.setId(2L);
        disabledRole.disable();
        disabledRole.assignPermission(100L, Set.of(ActionType.DELETE), PermissionEffect.ALLOW);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(enabledRole, disabledRole));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertFalse(bitmap.hasAction(100L, ActionType.DELETE));
    }

    @Test
    void shouldHandleRoleInheritance() {
        Role parentRole = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        parentRole.setId(1L);
        parentRole.assignPermission(100L, Set.of(ActionType.VIEW, ActionType.CREATE), PermissionEffect.ALLOW);

        Role childRole = Role.create(new RoleCode("SUPER_ADMIN"), "超级管理员", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.assignPermission(200L, Set.of(ActionType.DELETE), PermissionEffect.ALLOW);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(childRole));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(100L, ActionType.CREATE));
        assertTrue(bitmap.hasAction(200L, ActionType.DELETE));
    }

    @Test
    void shouldHandleLimitInheritMode() {
        Role parentRole = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        parentRole.setId(1L);
        parentRole.assignPermission(100L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.DELETE), PermissionEffect.ALLOW);

        Role childRole = Role.create(new RoleCode("LIMITED_ADMIN"), "受限管理员", 1L, InheritMode.LIMIT);
        childRole.setId(2L);
        // DENY父角色的DELETE权限
        childRole.assignPermission(100L, Set.of(ActionType.DELETE), PermissionEffect.DENY);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(childRole));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertTrue(bitmap.hasAction(100L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(100L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(100L, ActionType.DELETE)); // DENY生效
    }

    @Test
    void shouldSkipDisabledParentInInheritance() {
        Role parentRole = Role.create(new RoleCode("DISABLED_ADMIN"), "禁用管理员", null, null);
        parentRole.setId(1L);
        parentRole.disable();
        parentRole.assignPermission(100L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);

        Role childRole = Role.create(new RoleCode("CHILD"), "子角色", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.assignPermission(200L, Set.of(ActionType.CREATE), PermissionEffect.ALLOW);

        when(roleRepository.findRolesWithPermissionsByUserId(1L)).thenReturn(List.of(childRole));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));

        PermissionBitmap bitmap = service.computeUserPermissionBitmap(1L);

        assertFalse(bitmap.hasAction(100L, ActionType.VIEW)); // 禁用父角色权限不继承
        assertTrue(bitmap.hasAction(200L, ActionType.CREATE));
    }
}