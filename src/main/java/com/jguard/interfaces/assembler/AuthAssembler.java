package com.jguard.interfaces.assembler;

import com.jguard.domain.auth.valueobject.AccessToken;
import com.jguard.domain.auth.valueobject.TokenPair;
import com.jguard.interfaces.dto.response.LoginResponse;
import org.springframework.stereotype.Component;

/**
 * Auth 转换器
 */
@Component
public class AuthAssembler {

    /**
     * Token Pair 转登录响应
     */
    public LoginResponse toResponse(TokenPair tokenPair, Long userId, String username) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(tokenPair.accessToken().token());
        response.setRefreshToken(tokenPair.refreshToken());
        response.setAccessTokenExpiresIn(tokenPair.accessToken().remainingTimeMillis() / 1000);
        response.setUserId(userId);
        response.setUsername(username);
        return response;
    }
}