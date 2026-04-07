package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.auth.entity.RefreshToken;
import com.example.demo.domain.auth.repository.TokenRepository;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.infrastructure.persistence.converter.RefreshTokenConverter;
import com.example.demo.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.example.demo.infrastructure.persistence.po.RefreshTokenPO;
import org.springframework.stereotype.Repository;

/**
 * Token 仓库实现
 */
@Repository
public class TokenRepositoryImpl implements TokenRepository {

    private final RefreshTokenMapper refreshTokenMapper;
    private final RefreshTokenConverter refreshTokenConverter;

    public TokenRepositoryImpl(RefreshTokenMapper refreshTokenMapper,
                               RefreshTokenConverter refreshTokenConverter) {
        this.refreshTokenMapper = refreshTokenMapper;
        this.refreshTokenConverter = refreshTokenConverter;
    }

    @Override
    public RefreshToken findByToken(String token) {
        RefreshTokenPO po = refreshTokenMapper.selectByToken(token);
        return refreshTokenConverter.toDomain(po);
    }

    @Override
    public void save(RefreshToken token) {
        RefreshTokenPO po = refreshTokenConverter.toPO(token);
        refreshTokenMapper.insert(po);
    }

    @Override
    public void revoke(RefreshToken token) {
        refreshTokenMapper.revokeByToken(token.getToken());
    }

    @Override
    public void revokeByUserId(UserId userId) {
        refreshTokenMapper.revokeByUserId(userId.value());
    }

    @Override
    public void deleteExpired() {
        refreshTokenMapper.deleteExpired();
    }
}