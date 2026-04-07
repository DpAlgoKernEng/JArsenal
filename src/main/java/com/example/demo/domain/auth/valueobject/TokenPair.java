package com.example.demo.domain.auth.valueobject;

/**
 * Token 对值对象
 * 包含 Access Token 和 Refresh Token
 */
public class TokenPair {

    private final AccessToken accessToken;
    private final String refreshToken;

    public TokenPair(AccessToken accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public AccessToken accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }
}