package com.example.demo.config;

import com.example.demo.interceptor.AuthInterceptor;
import com.example.demo.interceptor.RequestLogInterceptor;
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
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/public/**",
                        "/api-docs/**",
                        "/swagger-ui/**"
                )
                .order(2);
    }
}