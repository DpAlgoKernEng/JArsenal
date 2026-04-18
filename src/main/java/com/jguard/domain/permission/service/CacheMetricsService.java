package com.jguard.domain.permission.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache metrics service
 * Monitors cache performance: hit rates, load times, cache sizes
 */
@Service
public class CacheMetricsService {

    private final AtomicLong l1Hits = new AtomicLong(0);
    private final AtomicLong l1Misses = new AtomicLong(0);
    private final AtomicLong l2Hits = new AtomicLong(0);
    private final AtomicLong l2Misses = new AtomicLong(0);
    private final AtomicLong computations = new AtomicLong(0);
    private final AtomicLong totalLoadTimeMs = new AtomicLong(0);

    public void recordL1Hit() {
        l1Hits.incrementAndGet();
    }

    public void recordL1Miss() {
        l1Misses.incrementAndGet();
    }

    public void recordL2Hit() {
        l2Hits.incrementAndGet();
    }

    public void recordL2Miss() {
        l2Misses.incrementAndGet();
    }

    public void recordComputation(long loadTimeMs) {
        computations.incrementAndGet();
        totalLoadTimeMs.addAndGet(loadTimeMs);
    }

    public CacheMetrics getMetrics(long localCacheSize) {
        return new CacheMetrics(
            l1Hits.get(),
            l1Misses.get(),
            l2Hits.get(),
            l2Misses.get(),
            computations.get(),
            localCacheSize,
            calculateHitRate(),
            calculateAverageLoadTime()
        );
    }

    private double calculateHitRate() {
        long totalHits = l1Hits.get() + l2Hits.get();
        long totalRequests = totalHits + computations.get();
        return totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
    }

    private double calculateAverageLoadTime() {
        long totalComputations = computations.get();
        return totalComputations > 0 ? (double) totalLoadTimeMs.get() / totalComputations : 0.0;
    }

    public void reset() {
        l1Hits.set(0);
        l1Misses.set(0);
        l2Hits.set(0);
        l2Misses.set(0);
        computations.set(0);
        totalLoadTimeMs.set(0);
    }

    /**
     * Cache metrics data record
     */
    public record CacheMetrics(
        long l1Hits,
        long l1Misses,
        long l2Hits,
        long l2Misses,
        long computations,
        long localCacheSize,
        double hitRate,
        double averageLoadTimeMs
    ) {}
}