package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.auth.entity.RefreshToken;
import com.example.demo.domain.auth.valueobject.TokenId;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.infrastructure.persistence.po.RefreshTokenPO;
import org.springframework.stereotype.Component;

/**
 * RefreshToken 领域对象与持久化对象转换器
 */
@Component
public class RefreshTokenConverter {

    /**
     * PO 转 领域对象
     */
    public RefreshToken toDomain(RefreshTokenPO po) {
        if (po == null) {
            return null;
        }
        return RefreshToken.rebuild(
            new TokenId(String.valueOf(po.getId())),
            new UserId(po.getUserId()),
            po.getToken(),
            po.getExpiresAt(),
            po.getCreateTime(),
            po.getRevoked() == 1
        );
    }

    /**
     * 领域对象 转 PO
     */
    public RefreshTokenPO toPO(RefreshToken token) {
        if (token == null) {
            return null;
        }
        RefreshTokenPO po = new RefreshTokenPO();
        // 不设置 ID，由数据库自增生成
        po.setUserId(token.getUserId().value());
        po.setToken(token.getToken());
        po.setExpiresAt(token.getExpiresAt());
        po.setCreateTime(token.getCreatedAt());
        po.setRevoked(token.isRevoked() ? 1 : 0);
        return po;
    }
}