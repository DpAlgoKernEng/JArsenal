package com.jguard.infrastructure.monitor;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 内存健康检查
 */
@Component
public class MemoryHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD = 0.9; // 90% 告警阈值

    @Override
    public Health health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        double usageRatio = (double) usedMemory / maxMemory;

        if (usageRatio < MEMORY_THRESHOLD) {
            return Health.up()
                    .withDetail("maxMemory", formatBytes(maxMemory))
                    .withDetail("usedMemory", formatBytes(usedMemory))
                    .withDetail("usageRatio", String.format("%.2f%%", usageRatio * 100))
                    .build();
        } else {
            return Health.status("WARNING")
                    .withDetail("maxMemory", formatBytes(maxMemory))
                    .withDetail("usedMemory", formatBytes(usedMemory))
                    .withDetail("usageRatio", String.format("%.2f%%", usageRatio * 100))
                    .withDetail("message", "内存使用率超过阈值")
                    .build();
        }
    }

    private String formatBytes(long bytes) {
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}