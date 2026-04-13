package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 */
@Configuration
public class RedissonConfig {

    // Redisson Spring Boot Starter 会自动配置 RedissonClient
    // 只需要确保 application.yml 中有 spring.data.redis 配置即可

    @Bean
    public String outboxLockKey() {
        return "outbox:publish:lock";
    }
}