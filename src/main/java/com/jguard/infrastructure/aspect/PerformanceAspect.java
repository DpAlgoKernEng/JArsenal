package com.jguard.infrastructure.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 接口性能监控切面
 */
@Slf4j
@Aspect
@Component
public class PerformanceAspect {

    private static final long SLOW_REQUEST_THRESHOLD = 3000; // 3秒

    /**
     * 切点：所有 Controller 方法
     */
    @Pointcut("execution(* com.jguard.interfaces.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        log.debug("执行开始: {}.{} 参数: {}", className, methodName, Arrays.toString(args));

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > SLOW_REQUEST_THRESHOLD) {
                log.warn("慢接口告警: {}.{} 耗时 {}ms", className, methodName, duration);
            } else {
                log.debug("执行结束: {}.{} 耗时 {}ms", className, methodName, duration);
            }

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("执行异常: {}.{} 耗时 {}ms 异常: {}", className, methodName, duration, e.getMessage());
            throw e;
        }
    }
}