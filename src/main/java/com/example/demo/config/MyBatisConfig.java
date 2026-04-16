package com.example.demo.config;

import com.example.demo.infrastructure.persistence.interceptor.DataScopeInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置
 * 注册数据权限拦截器
 */
@Configuration
public class MyBatisConfig {

    /**
     * 注册数据权限拦截器到 SqlSessionFactory
     * 拦截器会在 Spring 容器初始化后自动注入
     */
    @Bean
    public DataScopeInterceptor dataScopeInterceptor() {
        // 拦截器由 Spring 容器管理，依赖自动注入
        return new DataScopeInterceptor(null, null, null, null, null);
    }
}