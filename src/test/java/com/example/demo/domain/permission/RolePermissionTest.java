package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.RolePermission;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionEffect;
import com.example.demo.domain.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("角色权限值对象测试")
class RolePermissionTest {

    @Test
    @DisplayName("有效权限应该创建成功")
    void validPermission_shouldCreate() {
        RolePermission perm = new RolePermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE), PermissionEffect.ALLOW);
        assertEquals(1L, perm.getResourceId());
        assertTrue(perm.hasAction(ActionType.VIEW));
        assertTrue(perm.hasAction(ActionType.CREATE));
    }

    @Test
    @DisplayName("null资源ID应该抛出异常")
    void nullResourceId_shouldThrow() {
        assertThrows(DomainException.class,
            () -> new RolePermission(null, Set.of(ActionType.VIEW), PermissionEffect.ALLOW));
    }

    @Test
    @DisplayName("空操作集合应该抛出异常")
    void emptyActions_shouldThrow() {
        assertThrows(DomainException.class,
            () -> new RolePermission(1L, Set.of(), PermissionEffect.ALLOW));
        assertThrows(DomainException.class,
            () -> new RolePermission(1L, null, PermissionEffect.ALLOW));
    }

    @Test
    @DisplayName("操作集合应该是不可变的")
    void actionsShouldBeImmutable() {
        Set<ActionType> mutableActions = new HashSet<>();
        mutableActions.add(ActionType.VIEW);

        RolePermission perm = new RolePermission(1L, mutableActions, PermissionEffect.ALLOW);

        // 尝试修改原始集合
        mutableActions.add(ActionType.DELETE);

        // 权限不应受影响
        assertFalse(perm.hasAction(ActionType.DELETE));

        // 尝试修改返回的集合
        assertThrows(UnsupportedOperationException.class,
            () -> perm.getActions().add(ActionType.DELETE));
    }

    @Test
    @DisplayName("null效果应默认为ALLOW")
    void nullEffect_shouldDefaultToAllow() {
        RolePermission perm = new RolePermission(1L, Set.of(ActionType.VIEW), null);
        assertEquals(PermissionEffect.ALLOW, perm.getEffect());
    }
}