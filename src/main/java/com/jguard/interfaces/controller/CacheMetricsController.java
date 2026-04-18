package com.jguard.interfaces.controller;

import com.jguard.infrastructure.common.Result;
import com.jguard.domain.permission.service.PermissionCacheServiceImpl;
import com.jguard.domain.permission.service.CacheMetricsService.CacheMetrics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * 缓存监控控制器
 */
@Tag(name = "缓存监控", description = "权限缓存性能监控与管理接口")
@RestController
@RequestMapping("/api/v1/cache/metrics")
@RequiredArgsConstructor
public class CacheMetricsController {

    private final PermissionCacheServiceImpl cacheService;

    /**
     * Get cache hit rate metrics
     */
    @Operation(summary = "获取缓存命中率", description = "查询L1/L2缓存命中率、加载时间等性能指标")
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
    @Operation(summary = "重置缓存指标", description = "清零缓存命中率计数器，重新开始统计")
    @PostMapping("/reset")
    public Result<Void> resetMetrics() {
        cacheService.resetMetrics();
        return Result.success(null);
    }

    /**
     * Get cache health status
     */
    @Operation(summary = "获取缓存健康状态", description = "检查缓存运行状态，命中率低于80%返回警告")
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
    @Operation(summary = "清除用户缓存", description = "清除指定用户的权限缓存，下次请求重新加载")
    @DeleteMapping("/user/{userId}")
    public Result<Void> clearUserCache(@PathVariable Long userId) {
        cacheService.clearUserPermissions(userId);
        return Result.success(null);
    }

    /**
     * Clear cache for role-related users
     */
    @Operation(summary = "清除角色相关缓存", description = "清除拥有指定角色的所有用户的权限缓存")
    @DeleteMapping("/role/{roleId}")
    public Result<Void> clearRoleCache(@PathVariable Long roleId) {
        cacheService.clearRoleRelatedPermissions(roleId);
        return Result.success(null);
    }
}