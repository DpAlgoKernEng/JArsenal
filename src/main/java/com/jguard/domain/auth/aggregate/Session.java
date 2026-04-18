package com.jguard.domain.auth.aggregate;

import com.jguard.domain.auth.entity.RefreshToken;
import com.jguard.domain.auth.event.TokenRefreshed;
import com.jguard.domain.auth.event.UserLoggedIn;
import com.jguard.domain.auth.event.UserLoggedOut;
import com.jguard.domain.auth.service.TokenGenerator;
import com.jguard.domain.auth.valueobject.AccessToken;
import com.jguard.domain.auth.valueobject.SessionId;
import com.jguard.domain.shared.common.BaseEntity;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Session 聚合根
 * 管理用户会话和 Token
 */
public class Session extends BaseEntity<SessionId> {

    private UserId userId;
    private Username username;
    private AccessToken accessToken;
    private RefreshToken refreshToken;
    private LocalDateTime createdAt;

    /**
     * 工厂方法：创建新会话
     */
    public static Session create(UserId userId, Username username, TokenGenerator generator) {
        Session session = new Session();
        session.setId(SessionId.generate());
        session.userId = userId;
        session.username = username;
        session.accessToken = generator.generateAccessToken(userId, username);
        session.refreshToken = RefreshToken.create(
            userId,
            generator.generateRefreshToken(userId),
            generator.refreshTokenExpiration()
        );
        session.createdAt = LocalDateTime.now();
        session.registerEvent(new UserLoggedIn(userId, username));
        return session;
    }

    /**
     * 重建已存在的会话（从数据库加载）
     */
    public static Session rebuild(SessionId id, UserId userId, Username username,
                                  AccessToken accessToken, RefreshToken refreshToken,
                                  LocalDateTime createdAt) {
        Session session = new Session();
        session.setId(id);
        session.userId = userId;
        session.username = username;
        session.accessToken = accessToken;
        session.refreshToken = refreshToken;
        session.createdAt = createdAt;
        return session;
    }

    /**
     * 业务行为：刷新会话（生成新的 Token）
     */
    public Session refresh(TokenGenerator generator) {
        // 撤销旧的 Refresh Token
        this.refreshToken.revoke();

        // 创建新的 Session
        Session newSession = new Session();
        newSession.setId(SessionId.generate());
        newSession.userId = this.userId;
        newSession.username = this.username;
        newSession.accessToken = generator.generateAccessToken(this.userId, this.username);
        newSession.refreshToken = RefreshToken.create(
            this.userId,
            generator.generateRefreshToken(this.userId),
            generator.refreshTokenExpiration()
        );
        newSession.createdAt = LocalDateTime.now();
        newSession.registerEvent(new TokenRefreshed(this.userId));
        return newSession;
    }

    /**
     * 业务行为：登出
     */
    public void logout() {
        this.refreshToken.revoke();
        this.registerEvent(new UserLoggedOut(this.userId));
    }

    // Getters

    public UserId getUserId() {
        return userId;
    }

    public Username getUsername() {
        return username;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取所有待发布的领域事件
     */
    public List<com.jguard.domain.shared.event.DomainEvent> pendingEvents() {
        return this.events();
    }

    /**
     * 清除已发布的领域事件
     */
    public void clearPendingEvents() {
        this.clearEvents();
    }
}