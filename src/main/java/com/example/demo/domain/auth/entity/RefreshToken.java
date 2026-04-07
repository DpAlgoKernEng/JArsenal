package com.example.demo.domain.auth.entity;

import com.example.demo.domain.auth.valueobject.TokenId;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.domain.user.valueobject.UserId;
import java.time.LocalDateTime;

/**
 * Refresh Token 实体
 * 属于 Session Aggregate 的内部实体
 */
public class RefreshToken {

    private TokenId id;
    private UserId userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean revoked;

    /**
     * 工厂方法：创建新的 Refresh Token
     */
    public static RefreshToken create(UserId userId, String tokenValue, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.id = TokenId.generate();
        refreshToken.userId = userId;
        refreshToken.token = tokenValue;
        refreshToken.expiresAt = expiresAt;
        refreshToken.createdAt = LocalDateTime.now();
        refreshToken.revoked = false;
        return refreshToken;
    }

    /**
     * 重建已存在的 Refresh Token（从数据库加载）
     */
    public static RefreshToken rebuild(TokenId id, UserId userId, String token,
                                       LocalDateTime expiresAt, LocalDateTime createdAt, boolean revoked) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.id = id;
        refreshToken.userId = userId;
        refreshToken.token = token;
        refreshToken.expiresAt = expiresAt;
        refreshToken.createdAt = createdAt;
        refreshToken.revoked = revoked;
        return refreshToken;
    }

    /**
     * 撤销 Token
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * 验证 Token 有效性
     * @throws DomainException 如果 Token 无效
     */
    public void validate() {
        if (this.revoked) {
            throw new DomainException("Token 已被撤销");
        }
        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new DomainException("Token 已过期");
        }
    }

    /**
     * 检查是否有效
     */
    public boolean isValid() {
        return !revoked && expiresAt.isAfter(LocalDateTime.now());
    }

    // Getters

    public TokenId getId() {
        return id;
    }

    public UserId getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}