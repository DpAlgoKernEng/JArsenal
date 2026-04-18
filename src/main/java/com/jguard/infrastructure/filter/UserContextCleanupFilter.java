package com.jguard.infrastructure.filter;

import com.jguard.infrastructure.security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * UserContext 清理过滤器
 * 作为 AuthInterceptor 的安全网，确保请求结束后 ThreadLocal 被清理
 * 使用最高优先级(order=Ordered.HIGHEST_PRECEDENCE+100)确保在所有过滤器之后执行清理
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class UserContextCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 继续处理请求
            filterChain.doFilter(request, response);
        } finally {
            // 无论请求成功或失败，都清理 ThreadLocal
            // 这比 Interceptor.afterCompletion() 更可靠，因为 Filter 先于 Interceptor 执行
            UserContext.clear();
            log.debug("UserContext 已清理: uri={}", request.getRequestURI());
        }
    }
}