package com.example.demo.infrastructure.config;

import com.example.demo.infrastructure.interceptor.AuthInterceptor;
import com.example.demo.infrastructure.interceptor.PermissionInterceptor;
import com.example.demo.infrastructure.interceptor.RequestLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置
 */
@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final RequestLogInterceptor requestLogInterceptor;
    private final AuthInterceptor authInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 请求日志拦截器
        registry.addInterceptor(requestLogInterceptor)
                .addPathPatterns("/**")
                .order(1);

        // 认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/register",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                        "/api/v1/public/**",
                        "/api-docs/**",
                        "/swagger-ui/**"
                )
                .order(2);

        // 权限拦截器 - 在认证拦截器之后执行
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/**",
                        "/api/v1/public/**",
                        "/api-docs/**",
                        "/swagger-ui/**"
                )
                .order(3);
    }
}