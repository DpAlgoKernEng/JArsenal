package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Permission cache service implementation
 * Provides L1 (Caffeine) local cache for user permission bitmaps
 */
@Service
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private final PermissionDomainService permissionDomainService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    private final Cache<Long, PermissionBitmap> localCache;

    private static final long LOCAL_EXPIRE_SECONDS = 300;

    public PermissionCacheServiceImpl(PermissionDomainService permissionDomainService,
                                       UserRoleRepository userRoleRepository,
                                       RoleRepository roleRepository) {
        this.permissionDomainService = permissionDomainService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;

        this.localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(LOCAL_EXPIRE_SECONDS, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public PermissionBitmap getPermissionBitmap(Long userId) {
        // Input validation
        if (userId == null || userId <= 0) {
            return PermissionBitmap.empty(System.currentTimeMillis());
        }

        // L1: Local cache
        PermissionBitmap cached = localCache.getIfPresent(userId);
        if (cached != null && !cached.isExpired()) {
            return cached;
        }

        // Compute fresh bitmap
        PermissionBitmap fresh = permissionDomainService.computeUserPermissionBitmap(userId);
        localCache.put(userId, fresh);

        return fresh;
    }

    @Override
    public void clearUserPermissions(Long userId) {
        localCache.invalidate(userId);
    }

    @Override
    public void clearRoleRelatedPermissions(Long roleId) {
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        userIds.forEach(this::clearUserPermissions);
        roleRepository.incrementVersion(roleId);
    }
}