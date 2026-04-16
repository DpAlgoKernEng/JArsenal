package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;

/**
 * Permission cache service interface
 * Provides L1/L2 cache for user permission bitmaps
 */
public interface PermissionCacheService {

    /**
     * Get user permission bitmap from cache
     * Falls back to computation if not cached
     */
    PermissionBitmap getPermissionBitmap(Long userId);

    /**
     * Clear user permission cache
     */
    void clearUserPermissions(Long userId);

    /**
     * Clear role-related user caches
     */
    void clearRoleRelatedPermissions(Long roleId);

    /**
     * Cache empty permission to prevent cache penetration
     * Used when userId is invalid or user has no roles
     */
    PermissionBitmap cacheEmptyPermission(Long userId);
}