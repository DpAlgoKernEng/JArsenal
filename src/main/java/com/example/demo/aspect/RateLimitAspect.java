package com.example.demo.aspect;

import com.example.demo.annotation.RateLimit;
import com.example.demo.exception.BusinessException;
import com.example.demo.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;

/**
 * 限流切面 - 基于 Redis + Lua 实现原子性限流
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/rate_limit.lua")));
        this.rateLimitScript.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = buildKey(joinPoint, rateLimit);
        boolean allowed = executeRateLimit(key, rateLimit.count(), rateLimit.time());

        if (!allowed) {
            log.warn("限流触发: key={}, limit={}/{}s", key, rateLimit.count(), rateLimit.time());
            throw new BusinessException(429, "请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    /**
     * 构建限流 Key
     */
    private String buildKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String baseKey = rateLimit.key().isEmpty() ? methodName : rateLimit.key();

        switch (rateLimit.limitType()) {
            case IP:
                HttpServletRequest request = getCurrentRequest();
                String ip = getClientIp(request);
                return "rate_limit:" + baseKey + ":ip:" + ip;
            case USER:
                Long userId = UserContext.getCurrentUserId();
                return "rate_limit:" + baseKey + ":user:" + (userId != null ? userId : "anonymous");
            default:
                return "rate_limit:" + baseKey + ":global";
        }
    }

    /**
     * 执行限流检查
     */
    private boolean executeRateLimit(String key, int limit, int window) {
        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(limit),
                    String.valueOf(window),
                    String.valueOf(System.currentTimeMillis())
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("Redis限流执行失败，降级为放行: {}", e.getMessage());
            // Redis 异常时降级为放行，避免影响业务
            return true;
        }
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端 IP
     */
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