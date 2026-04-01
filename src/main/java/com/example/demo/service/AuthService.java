package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 刷新 Access Token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * 登出
     */
    void logout(String refreshToken);
}