package com.jguard.domain.permission;

import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.domain.permission.valueobject.ActionType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissionBitmapTest {

    @Test
    void shouldCreateEmptyBitmap() {
        PermissionBitmap bitmap = PermissionBitmap.empty();
        assertFalse(bitmap.hasAction(1L, ActionType.VIEW));
    }

    @Test
    void shouldAddPermission() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE));

        assertTrue(bitmap.hasAction(1L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(1L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(1L, ActionType.DELETE));
    }

    @Test
    void shouldMergeBitmaps() {
        PermissionBitmap a = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE));
        PermissionBitmap b = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.UPDATE, ActionType.DELETE));

        PermissionBitmap merged = a.merge(b);

        assertTrue(merged.hasAction(1L, ActionType.VIEW));
        assertTrue(merged.hasAction(1L, ActionType.CREATE));
        assertTrue(merged.hasAction(1L, ActionType.UPDATE));
        assertTrue(merged.hasAction(1L, ActionType.DELETE));
    }

    @Test
    void shouldApplyDenyPriority() {
        PermissionBitmap allow = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.UPDATE));
        PermissionBitmap deny = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.UPDATE));

        PermissionBitmap result = allow.applyDeny(deny);

        assertTrue(result.hasAction(1L, ActionType.VIEW));
        assertTrue(result.hasAction(1L, ActionType.CREATE));
        assertFalse(result.hasAction(1L, ActionType.UPDATE)); // DENY生效
    }

    @Test
    void shouldHandleDifferentResources() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW))
            .addPermission(2L, Set.of(ActionType.CREATE));

        assertTrue(bitmap.hasAction(1L, ActionType.VIEW));
        assertFalse(bitmap.hasAction(1L, ActionType.CREATE));
        assertTrue(bitmap.hasAction(2L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(2L, ActionType.VIEW));
    }
}