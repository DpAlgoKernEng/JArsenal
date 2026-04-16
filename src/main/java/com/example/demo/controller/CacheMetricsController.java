package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.domain.permission.service.PermissionCacheServiceImpl;
import com.example.demo.domain.permission.service.CacheMetricsService.CacheMetrics;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Cache metrics monitoring controller
 * Provides endpoints for cache performance monitoring
 */
@RestController
@RequestMapping("/api/cache/metrics")
public class CacheMetricsController {

    private final PermissionCacheServiceImpl cacheService;

    public CacheMetricsController(PermissionCacheServiceImpl cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * Get cache hit rate metrics
     */
    @GetMapping("/hit-rate")
    public Result<Map<String, Object>> getCacheHitRate() {
        CacheMetrics metrics = cacheService.getMetrics();

        Map<String, Object> result = new HashMap<>();
        result.put("l1Hits", metrics.l1Hits());
        result.put("l1Misses", metrics.l1Misses());
        result.put("l2Hits", metrics.l2Hits());
        result.put("l2Misses", metrics.l2Misses());
        result.put("computations", metrics.computations());
        result.put("localCacheSize", metrics.localCacheSize());
        result.put("overallHitRate", metrics.hitRate());
        result.put("averageLoadTimeMs", metrics.averageLoadTimeMs());

        // Calculate layer-specific hit rates
        long l1Total = metrics.l1Hits() + metrics.l1Misses();
        long l2Total = metrics.l2Hits() + metrics.l2Misses();

        result.put("l1HitRate", l1Total > 0 ? (double) metrics.l1Hits() / l1Total : 0.0);
        result.put("l2HitRate", l2Total > 0 ? (double) metrics.l2Hits() / l2Total : 0.0);

        return Result.success(result);
    }

    /**
     * Reset cache metrics counters
     */
    @PostMapping("/reset")
    public Result<Void> resetMetrics() {
        cacheService.resetMetrics();
        return Result.success(null);
    }

    /**
     * Get cache health status
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> getCacheHealth() {
        CacheMetrics metrics = cacheService.getMetrics();

        Map<String, Object> health = new HashMap<>();

        // Health criteria: hit rate > 80%
        boolean healthy = metrics.hitRate() > 0.8;

        health.put("status", healthy ? "HEALTHY" : "WARNING");
        health.put("hitRate", metrics.hitRate());
        health.put("message", healthy ?
            "Cache working normally, hit rate is good" :
            "Hit rate is low, consider checking warmup strategy");

        return Result.success(health);
    }

    /**
     * Clear cache for specific user
     */
    @DeleteMapping("/user/{userId}")
    public Result<Void> clearUserCache(@PathVariable Long userId) {
        cacheService.clearUserPermissions(userId);
        return Result.success(null);
    }

    /**
     * Clear cache for role-related users
     */
    @DeleteMapping("/role/{roleId}")
    public Result<Void> clearRoleCache(@PathVariable Long roleId) {
        cacheService.clearRoleRelatedPermissions(roleId);
        return Result.success(null);
    }
}