package com.example.demo.controller;

import com.example.demo.annotation.AuditLog;
import com.example.demo.annotation.RateLimit;
import com.example.demo.common.Result;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.enums.ModuleType;
import com.example.demo.enums.OperationType;
import com.example.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录注册接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 Access Token 和 Refresh Token")
    @RateLimit(key = "login", time = 60, count = 5)
    @AuditLog(operation = OperationType.LOGIN, module = ModuleType.AUTH, description = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @RateLimit(key = "register", time = 60, count = 3)
    @AuditLog(operation = OperationType.REGISTER, module = ModuleType.AUTH, description = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token", description = "使用 Refresh Token 获取新的 Access Token")
    @RateLimit(key = "refresh", time = 60, count = 20)
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "撤销 Refresh Token")
    @AuditLog(operation = OperationType.LOGOUT, module = ModuleType.AUTH, description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return Result.success();
    }
}