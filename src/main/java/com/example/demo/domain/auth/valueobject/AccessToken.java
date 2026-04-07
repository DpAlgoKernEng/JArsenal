package com.example.demo.domain.auth.valueobject;

import com.example.demo.domain.shared.exception.DomainException;
import java.time.LocalDateTime;

/**
 * Access Token 值对象
 */
public class AccessToken {

    private final String token;
    private final LocalDateTime expiresAt;

    public AccessToken(String token, LocalDateTime expiresAt) {
        if (token == null || token.isEmpty()) {
            throw new DomainException("Access Token 不能为空");
        }
        if (expiresAt == null) {
            throw new DomainException("过期时间不能为空");
        }
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String token() {
        return token;
    }

    public LocalDateTime expiresAt() {
        return expiresAt;
    }

    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 获取剩余有效时间（毫秒）
     */
    public long remainingTimeMillis() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMillis();
    }
}