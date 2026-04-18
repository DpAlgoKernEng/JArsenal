package com.jguard.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置
 * BCrypt strength 设置为 12 以应对现代硬件攻击
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt 密码加密器
     * strength=12 表示 2^12 次迭代，约需 250ms 计算
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}