package com.example.demo.infrastructure.aspect;

import com.example.demo.infrastructure.common.Result;
import com.example.demo.domain.audit.repository.AuditLogRepository;
import com.example.demo.domain.audit.valueobject.ModuleType;
import com.example.demo.domain.audit.valueobject.OperationType;
import com.example.demo.domain.audit.valueobject.TraceInfo;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.interfaces.dto.response.LoginResponse;
import com.example.demo.infrastructure.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计日志切面
 * 使用 DDD 领域模型记录审计日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(com.example.demo.infrastructure.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        com.example.demo.infrastructure.annotation.AuditLog auditLogAnnotation = method.getAnnotation(com.example.demo.infrastructure.annotation.AuditLog.class);

        // 获取操作类型和模块
        OperationType operationType = auditLogAnnotation.operation();
        ModuleType moduleType = auditLogAnnotation.module();
        String description = auditLogAnnotation.description();

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String ip = request != null ? getClientIp(request) : "unknown";
        String traceId = MDC.get("traceId");

        // 尝试获取目标 ID
        Long targetId = extractTargetId(joinPoint.getArgs());

        try {
            // 执行方法
            Object result = joinPoint.proceed();

            // 获取用户信息
            UserInfo userInfo = extractUserInfo(result);

            // 记录成功日志
            long duration = System.currentTimeMillis() - startTime;
            saveAuditLogAsync(userInfo.userId, userInfo.username, operationType, moduleType,
                description, targetId, ip, traceId, true, null, duration);

            return result;
        } catch (Throwable e) {
            // 获取用户信息
            Long userId = UserContext.getCurrentUserId();
            String username = UserContext.getCurrentUsername();

            // 记录失败日志
            long duration = System.currentTimeMillis() - startTime;
            saveAuditLogAsync(userId, username, operationType, moduleType,
                description, targetId, ip, traceId, false, e.getMessage(), duration);

            throw e;
        }
    }

    /**
     * 异步保存审计日志
     */
    @Async
    public void saveAuditLogAsync(Long userId, String username, OperationType operation,
                                   ModuleType module, String description, Long targetId,
                                   String ip, String traceId, boolean success,
                                   String errorMsg, long duration) {
        try {
            com.example.demo.domain.audit.aggregate.AuditLog auditLog;
            if (success) {
                auditLog = com.example.demo.domain.audit.aggregate.AuditLog.success(
                    userId != null ? new UserId(userId) : null,
                    username != null ? new Username(username) : null,
                    operation, module, description, targetId,
                    TraceInfo.of(ip, traceId), duration
                );
            } else {
                auditLog = com.example.demo.domain.audit.aggregate.AuditLog.failure(
                    userId != null ? new UserId(userId) : null,
                    username != null ? new Username(username) : null,
                    operation, module, description, targetId,
                    TraceInfo.of(ip, traceId), errorMsg, duration
                );
            }
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    /**
     * 提取用户信息
     */
    private UserInfo extractUserInfo(Object result) {
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();

        // 如果 UserContext 为空，尝试从返回值获取（登录场景）
        if (userId == null && result instanceof Result<?> apiResult) {
            Object data = apiResult.getData();
            if (data instanceof LoginResponse loginResponse) {
                userId = loginResponse.getUserId();
                username = loginResponse.getUsername();
            }
        }

        return new UserInfo(userId, username);
    }

    /**
     * 提取目标 ID
     */
    private Long extractTargetId(Object[] args) {
        if (args == null) return null;
        for (Object arg : args) {
            if (arg instanceof Long id) {
                return id;
            }
        }
        return null;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
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

    private record UserInfo(Long userId, String username) {}
}