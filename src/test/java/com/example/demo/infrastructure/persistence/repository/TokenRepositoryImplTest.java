package com.example.demo.infrastructure.persistence.repository;

import com.example.demo.domain.auth.entity.RefreshToken;
import com.example.demo.domain.user.valueobject.UserId;
import com.example.demo.infrastructure.persistence.converter.RefreshTokenConverter;
import com.example.demo.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.example.demo.infrastructure.persistence.po.RefreshTokenPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Token 仓库实现测试
 */
@ExtendWith(MockitoExtension.class)
class TokenRepositoryImplTest {

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @Mock
    private RefreshTokenConverter refreshTokenConverter;

    private TokenRepositoryImpl tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository = new TokenRepositoryImpl(refreshTokenMapper, refreshTokenConverter);
    }

    @Test
    @DisplayName("根据Token查询 - 找到")
    void findByToken_found_shouldReturnToken() {
        // given
        String tokenValue = "test-refresh-token";
        RefreshTokenPO po = new RefreshTokenPO();
        po.setId(1L);
        po.setToken(tokenValue);
        po.setUserId(1L);

        RefreshToken domainToken = mock(RefreshToken.class);

        when(refreshTokenMapper.selectByToken(tokenValue)).thenReturn(po);
        when(refreshTokenConverter.toDomain(po)).thenReturn(domainToken);

        // when
        RefreshToken result = tokenRepository.findByToken(tokenValue);

        // then
        assertNotNull(result);
        verify(refreshTokenMapper).selectByToken(tokenValue);
        verify(refreshTokenConverter).toDomain(po);
    }

    @Test
    @DisplayName("根据Token查询 - 未找到")
    void findByToken_notFound_shouldReturnNull() {
        // given
        String tokenValue = "nonexistent-token";
        when(refreshTokenMapper.selectByToken(tokenValue)).thenReturn(null);
        when(refreshTokenConverter.toDomain(null)).thenReturn(null);

        // when
        RefreshToken result = tokenRepository.findByToken(tokenValue);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("保存Token")
    void save_shouldInsertToken() {
        // given
        RefreshToken token = mock(RefreshToken.class);
        RefreshTokenPO po = new RefreshTokenPO();
        po.setToken("new-token");

        when(refreshTokenConverter.toPO(token)).thenReturn(po);
        when(refreshTokenMapper.insert(po)).thenReturn(1);

        // when
        tokenRepository.save(token);

        // then
        verify(refreshTokenConverter).toPO(token);
        verify(refreshTokenMapper).insert(po);
    }

    @Test
    @DisplayName("撤销Token")
    void revoke_shouldCallRevokeByToken() {
        // given
        RefreshToken token = mock(RefreshToken.class);
        when(token.getToken()).thenReturn("token-to-revoke");
        when(refreshTokenMapper.revokeByToken("token-to-revoke")).thenReturn(1);

        // when
        tokenRepository.revoke(token);

        // then
        verify(refreshTokenMapper).revokeByToken("token-to-revoke");
    }

    @Test
    @DisplayName("根据用户ID撤销所有Token")
    void revokeByUserId_shouldCallRevokeByUserId() {
        // given
        UserId userId = new UserId(1L);
        when(refreshTokenMapper.revokeByUserId(1L)).thenReturn(2);

        // when
        tokenRepository.revokeByUserId(userId);

        // then
        verify(refreshTokenMapper).revokeByUserId(1L);
    }

    @Test
    @DisplayName("删除过期Token")
    void deleteExpired_shouldCallDeleteExpired() {
        // given
        when(refreshTokenMapper.deleteExpired()).thenReturn(5);

        // when
        tokenRepository.deleteExpired();

        // then
        verify(refreshTokenMapper).deleteExpired();
    }
}