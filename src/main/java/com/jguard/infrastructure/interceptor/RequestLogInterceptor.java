package com.jguard.infrastructure.interceptor;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器 - 与 OpenTelemetry 集成的链路追踪
 * OTel 自动管理 traceId/spanId，此拦截器仅记录请求日志
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final String LEGACY_TRACE_ID_HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public RequestLogInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        // 支持遗留的 X-Trace-Id header（向后兼容）
        String legacyTraceId = request.getHeader(LEGACY_TRACE_ID_HEADER);
        if (legacyTraceId != null && !legacyTraceId.isEmpty()) {
            MDC.put("legacyTraceId", legacyTraceId);
            response.setHeader(LEGACY_TRACE_ID_HEADER, legacyTraceId);
        }

        // OTel traceId/spanId 已由 Micrometer Tracing 自动注入 MDC
        var currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            // 添加 W3C traceparent 格式到响应 header
            String traceId = currentSpan.context().traceId();
            response.setHeader("traceparent", formatTraceParent(traceId));
        }

        log.info("请求开始 - {} {} from {} [traceId={}]",
                request.getMethod(),
                request.getRequestURI(),
                getClientIp(request),
                MDC.get("traceId"));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        log.info("请求结束 - {} {} -> {} ({}ms) [traceId={}]",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration,
                MDC.get("traceId"));

        if (duration > 3000) {
            log.warn("慢请求告警 - {} {} 耗时 {}ms [traceId={}]",
                    request.getMethod(), request.getRequestURI(), duration, MDC.get("traceId"));
        }

        MDC.remove("legacyTraceId");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 格式化 W3C traceparent header
     * 格式: 00-{traceId}-{spanId}-{flags}
     */
    private String formatTraceParent(String traceId) {
        var span = tracer.currentSpan();
        if (span != null) {
            return "00-" + traceId + "-" + span.context().spanId() + "-01";
        }
        return "00-" + traceId + "-00-01";
    }
}