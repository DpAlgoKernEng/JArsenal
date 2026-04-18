package com.jguard.domain.auth.service;

import com.jguard.domain.auth.valueobject.AccessToken;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import java.time.LocalDateTime;

/**
 * Token 生成器接口
 * 定义在领域层，实现在基础设施层
 */
public interface TokenGenerator {

    /**
     * 生成 Access Token
     * @param userId 用户ID
     * @param username 用户名
     * @return Access Token 值对象
     */
    AccessToken generateAccessToken(UserId userId, Username username);

    /**
     * 生成 Refresh Token 字串
     * @param userId 用户ID
     * @return Refresh Token 字串值
     */
    String generateRefreshToken(UserId userId);

    /**
     * 获取 Access Token 过期时间
     */
    LocalDateTime accessTokenExpiration();

    /**
     * 获取 Refresh Token 过期时间
     */
    LocalDateTime refreshTokenExpiration();

    /**
     * 解析并验证 Access Token
     * @param token Token 字串
     * @return 解析出的用户ID
     */
    UserId parseAccessToken(String token);

    /**
     * 解析并验证 Refresh Token
     * @param token Token 字串
     * @return 解析出的用户ID
     */
    UserId parseRefreshToken(String token);
}