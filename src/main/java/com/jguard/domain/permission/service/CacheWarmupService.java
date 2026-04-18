package com.jguard.domain.permission.service;

import com.jguard.domain.permission.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Cache warmup service
 * Pre-loads frequently accessed user permissions on application startup
 */
@Service
public class CacheWarmupService {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmupService.class);
    private static final int WARMUP_USER_LIMIT = 100;

    private final PermissionCacheService cacheService;
    private final UserRoleRepository userRoleRepository;

    public CacheWarmupService(PermissionCacheService cacheService,
                              UserRoleRepository userRoleRepository) {
        this.cacheService = cacheService;
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Warmup cache on application startup
     * Pre-loads top N most active users' permission bitmaps
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCache() {
        log.info("Starting permission cache warmup...");

        try {
            List<Long> activeUserIds = userRoleRepository.findActiveUserIds(WARMUP_USER_LIMIT);

            if (activeUserIds.isEmpty()) {
                log.info("No active users found for cache warmup");
                return;
            }

            log.info("Pre-loading permissions for {} active users", activeUserIds.size());

            // Parallel warmup for faster startup
            activeUserIds.parallelStream().forEach(userId -> {
                try {
                    cacheService.getPermissionBitmap(userId);
                } catch (Exception e) {
                    // Ignore warmup failures - cache will be loaded on demand
                    log.debug("Cache warmup failed for user {}: {}", userId, e.getMessage());
                }
            });

            log.info("Permission cache warmup completed for {} users", activeUserIds.size());
        } catch (Exception e) {
            log.warn("Cache warmup failed, will load permissions on demand: {}", e.getMessage());
        }
    }
}