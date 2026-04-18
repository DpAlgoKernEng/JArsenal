package com.jguard.domain.permission;

import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.valueobject.*;
import com.jguard.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("角色聚合根测试")
class RoleTest {

    @Test
    @DisplayName("应该创建角色成功")
    void shouldCreateRole() {
        Role role = Role.create(new RoleCode("MANAGER"), "部门管理员", null, InheritMode.EXTEND);

        assertEquals("MANAGER", role.getCode().value());
        assertEquals("部门管理员", role.getName());
        assertEquals(InheritMode.EXTEND, role.getInheritMode());
        assertEquals(RoleStatus.ENABLED, role.getStatus());
        assertFalse(role.isDeleted());
    }

    @Test
    @DisplayName("应该分配权限成功")
    void shouldAssignPermission() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);

        Set<RolePermission> perms = role.getOwnPermissions();
        assertEquals(1, perms.size());

        RolePermission perm = perms.iterator().next();
        assertEquals(1L, perm.getResourceId());
        assertTrue(perm.hasAction(ActionType.VIEW));
    }

    @Test
    @DisplayName("应该移除权限成功")
    void shouldRemovePermission() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);
        role.assignPermission(2L, Set.of(ActionType.CREATE), PermissionEffect.ALLOW);

        role.removePermission(1L);

        Set<RolePermission> perms = role.getOwnPermissions();
        assertEquals(1, perms.size());
        assertEquals(2L, perms.iterator().next().getResourceId());
    }

    @Test
    @DisplayName("应该启用/禁用角色成功")
    void shouldEnableDisableRole() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.disable();
        assertEquals(RoleStatus.DISABLED, role.getStatus());

        role.enable();
        assertEquals(RoleStatus.ENABLED, role.getStatus());
    }

    @Test
    @DisplayName("内置角色不能被禁用")
    void shouldRejectDisableBuiltinRole() {
        Role role = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        role.setBuiltin(true);

        assertThrows(DomainException.class, () -> role.disable());
    }

    @Test
    @DisplayName("内置角色不能被删除")
    void shouldRejectDeleteBuiltinRole() {
        Role role = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        role.setBuiltin(true);

        assertThrows(DomainException.class, () -> role.softDelete());
    }

    @Test
    @DisplayName("应该软删除非内置角色")
    void shouldSoftDeleteNonBuiltinRole() {
        Role role = Role.create(new RoleCode("CUSTOM"), "自定义角色", null, null);
        role.softDelete();

        assertTrue(role.isDeleted());
    }

    @Test
    @DisplayName("应该递增版本号")
    void shouldIncrementVersion() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        assertEquals(0, role.getVersion());

        role.incrementVersion();
        assertEquals(1, role.getVersion());
    }

    @Test
    @DisplayName("应该检测DENY权限")
    void shouldDetectDenyPermissions() {
        Role role = Role.create(new RoleCode("MANAGER"), "管理员", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);
        role.assignPermission(2L, Set.of(ActionType.DELETE), PermissionEffect.DENY);

        assertTrue(role.hasDenyPermissions());
    }
}