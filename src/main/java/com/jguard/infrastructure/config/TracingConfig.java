package com.jguard.infrastructure.config;

import io.micrometer.tracing.Tracer;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry 追踪配置
 * 提供辅助方法获取当前 traceId/spanId
 */
@Configuration
public class TracingConfig {

    /**
     * 获取当前 traceId
     * 优先从 Tracer 获取，否则从 MDC 获取
     */
    public static String getCurrentTraceId(Tracer tracer) {
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().traceId();
        }
        return MDC.get("traceId");
    }

    /**
     * 获取当前 spanId
     */
    public static String getCurrentSpanId(Tracer tracer) {
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().spanId();
        }
        return MDC.get("spanId");
    }
}