package com.example.demo.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Repository 配置类
 */
@Configuration
@MapperScan("com.example.demo.infrastructure.persistence.mapper")
public class RepositoryConfig {
    // Mapper 扫描配置
}