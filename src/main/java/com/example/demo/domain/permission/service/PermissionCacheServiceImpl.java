package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.UserRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Permission cache service implementation
 * Provides L1 (Caffeine) + L2 (Redis) two-level cache for user permission bitmaps
 * Features:
 * - SHA256 hashed cache keys for security (prevents enumeration attacks)
 * - Cache penetration protection (caches empty permissions with short TTL)
 * - Version validation for cache consistency
 * - Metrics monitoring (hit rate, load time)
 */
@Service
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private static final Logger log = LoggerFactory.getLogger(PermissionCacheServiceImpl.class);

    private final PermissionDomainService permissionDomainService;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheMetricsService metricsService;

    private final Cache<Long, PermissionBitmap> localCache;

    private static final long LOCAL_EXPIRE_SECONDS = 300;  // 5 minutes
    private static final long REDIS_EXPIRE_SECONDS = 3600; // 1 hour
    private static final long EMPTY_PERMISSION_TTL = 60;    // 1 minute (short TTL for penetration protection)
    private static final String CACHE_KEY_PREFIX = "perm:bitmap:";
    private static final String CACHE_SALT = "perm_salt_2026";

    public PermissionCacheServiceImpl(PermissionDomainService permissionDomainService,
                                       UserRoleRepository userRoleRepository,
                                       RoleRepository roleRepository,
                                       StringRedisTemplate stringRedisTemplate,
                                       ObjectMapper objectMapper,
                                       CacheMetricsService metricsService) {
        this.permissionDomainService = permissionDomainService;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.metricsService = metricsService;

        this.localCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(LOCAL_EXPIRE_SECONDS, TimeUnit.SECONDS)
            .recordStats() // Enable stats recording
            .build();
    }

    /**
     * Generate SHA256 hashed cache key for security
     * Prevents enumeration attacks where attackers could guess sequential userId keys
     */
    private String safeKey(Long userId) {
        String raw = userId + ":" + CACHE_SALT;
        String hash = DigestUtils.sha256Hex(raw);
        return CACHE_KEY_PREFIX + hash.substring(0, 16);
    }

    @Override
    public PermissionBitmap getPermissionBitmap(Long userId) {
        // Input validation - cache empty for invalid userId (penetration protection)
        if (userId == null || userId <= 0) {
            return cacheEmptyPermission(0L);
        }

        // L1: Local cache check
        PermissionBitmap cached = localCache.getIfPresent(userId);
        if (cached != null && !cached.isExpired()) {
            metricsService.recordL1Hit();
            return cached;
        }
        metricsService.recordL1Miss();

        // L2: Redis cache check
        String key = safeKey(userId);
        PermissionBitmap redisCached = null;
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                redisCached = objectMapper.readValue(json, PermissionBitmap.class);
            }
        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            // Graceful degradation - proceed without L2 cache
        }

        if (redisCached != null) {
            // Check if it's empty permission cache (penetration protection)
            if (redisCached.getActionBits().isEmpty()) {
                metricsService.recordL2Hit();
                return redisCached;
            }

            // Validate version consistency
            if (validateVersion(redisCached.getVersion(), userId)) {
                metricsService.recordL2Hit();
                localCache.put(userId, redisCached);
                return redisCached;
            }
            // Version mismatch - invalidate stale cache
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("Redis delete failed for key {}: {}", key, e.getMessage());
            }
        }
        metricsService.recordL2Miss();

        // Compute fresh bitmap
        long startTime = System.currentTimeMillis();
        PermissionBitmap fresh = permissionDomainService.computeUserPermissionBitmap(userId);
        long loadTime = System.currentTimeMillis() - startTime;
        metricsService.recordComputation(loadTime);

        // Cache the result (even empty permissions to prevent penetration)
        try {
            long ttl = fresh.getActionBits().isEmpty() ? EMPTY_PERMISSION_TTL : REDIS_EXPIRE_SECONDS;
            String json = objectMapper.writeValueAsString(fresh);
            stringRedisTemplate.opsForValue().set(key, json, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
        localCache.put(userId, fresh);

        return fresh;
    }

    /**
     * Cache empty permission to prevent cache penetration
     * Called when userId is invalid or user has no roles
     */
    @Override
    public PermissionBitmap cacheEmptyPermission(Long userId) {
        PermissionBitmap empty = PermissionBitmap.empty(System.currentTimeMillis());
        String key = safeKey(userId);

        // Use short TTL for empty permissions
        try {
            String json = objectMapper.writeValueAsString(empty);
            stringRedisTemplate.opsForValue().set(key, json, EMPTY_PERMISSION_TTL, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed for empty permission key {}: {}", key, e.getMessage());
        }
        localCache.put(userId, empty);

        return empty;
    }

    /**
     * Validate version consistency between cached bitmap and current role versions
     */
    private boolean validateVersion(long cachedVersion, Long userId) {
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return true; // No roles = empty bitmap is valid
        }

        long currentVersion = 0;
        for (Long roleId : roleIds) {
            var role = roleRepository.findById(roleId);
            if (role.isPresent()) {
                currentVersion += role.get().getVersion();
            }
        }

        return cachedVersion == currentVersion;
    }

    @Override
    public void clearUserPermissions(Long userId) {
        localCache.invalidate(userId);
        try {
            stringRedisTemplate.delete(safeKey(userId));
        } catch (Exception e) {
            log.warn("Redis delete failed during cache clear: {}", e.getMessage());
        }
    }

    @Override
    public void clearRoleRelatedPermissions(Long roleId) {
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        userIds.forEach(this::clearUserPermissions);
        roleRepository.incrementVersion(roleId);
    }

    /**
     * Get current cache metrics
     */
    public CacheMetricsService.CacheMetrics getMetrics() {
        return metricsService.getMetrics(localCache.estimatedSize());
    }

    /**
     * Reset cache metrics counters
     */
    public void resetMetrics() {
        metricsService.reset();
    }

    /**
     * Get local cache size estimate
     */
    public long getLocalCacheSize() {
        return localCache.estimatedSize();
    }
}