package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 请求日志拦截器 - 支持 traceId 链路追踪
 */
@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final String TRACE_ID = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 生成或获取 traceId
        String traceId = getOrGenerateTraceId(request);

        // 设置 MDC traceId（用于日志输出）
        MDC.put(TRACE_ID, traceId);

        // 添加到响应 header（便于客户端追踪）
        response.setHeader(TRACE_ID_HEADER, traceId);

        request.setAttribute(START_TIME, System.currentTimeMillis());
        log.info("请求开始 - {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIp(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                 Object handler, Exception ex) {
        long startTime = (Long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        log.info("请求结束 - {} {} -> {} ({}ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration);

        // 慢请求告警（超过3秒）
        if (duration > 3000) {
            log.warn("慢请求告警 - {} {} 耗时 {}ms", request.getMethod(), request.getRequestURI(), duration);
        }

        // 清理 MDC，防止内存泄漏
        MDC.clear();
    }

    /**
     * 获取或生成 traceId
     * 优先从请求 header 获取（支持分布式追踪），否则生成新的
     */
    private String getOrGenerateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
        return traceId;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}