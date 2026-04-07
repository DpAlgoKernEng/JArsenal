package com.example.demo.domain.auth.repository;

import com.example.demo.domain.auth.entity.RefreshToken;
import com.example.demo.domain.user.valueobject.UserId;

/**
 * Token 仓库接口
 */
public interface TokenRepository {

    /**
     * 根据 Token 字串查找
     */
    RefreshToken findByToken(String token);

    /**
     * 保存 Refresh Token
     */
    void save(RefreshToken token);

    /**
     * 撤销指定 Token
     */
    void revoke(RefreshToken token);

    /**
     * 撤销用户的所有 Token
     */
    void revokeByUserId(UserId userId);

    /**
     * 删除过期的 Token
     */
    void deleteExpired();
}