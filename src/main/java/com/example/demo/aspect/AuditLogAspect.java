package com.example.demo.aspect;

import com.example.demo.annotation.AuditLog;
import com.example.demo.common.Result;
import com.example.demo.dto.LoginResponse;
import com.example.demo.entity.AuditLogEntity;
import com.example.demo.security.UserContext;
import com.example.demo.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 审计日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Around("@annotation(com.example.demo.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

        // 构建审计日志
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setOperation(auditLogAnnotation.operation().name());
        auditLog.setModule(auditLogAnnotation.module().name());
        auditLog.setDescription(auditLogAnnotation.description());

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            auditLog.setIp(getClientIp(request));
        }

        // 获取 traceId
        String traceId = MDC.get("traceId");
        auditLog.setTraceId(traceId);

        // 尝试获取目标 ID（如果有 id 参数）
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long id) {
                auditLog.setTargetId(id);
                break;
            }
        }

        try {
            // 执行方法
            Object result = joinPoint.proceed();

            // 方法执行后获取用户信息（优先从 UserContext，其次从返回值）
            fillUserInfo(auditLog, result);

            // 记录成功
            auditLog.setStatus(1);
            auditLog.setDuration(System.currentTimeMillis() - startTime);

            return result;
        } catch (Throwable e) {
            // 失败时也尝试获取用户信息
            fillUserInfoFromContext(auditLog);

            // 记录失败
            auditLog.setStatus(0);
            auditLog.setErrorMsg(e.getMessage());
            auditLog.setDuration(System.currentTimeMillis() - startTime);
            throw e;
        } finally {
            // 异步保存审计日志
            auditLogService.saveAsync(auditLog);
        }
    }

    /**
     * 填充用户信息
     */
    private void fillUserInfo(AuditLogEntity auditLog, Object result) {
        // 先从 UserContext 获取
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

        auditLog.setUserId(userId);
        auditLog.setUsername(username);
    }

    /**
     * 从 UserContext 获取用户信息
     */
    private void fillUserInfoFromContext(AuditLogEntity auditLog) {
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
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
}