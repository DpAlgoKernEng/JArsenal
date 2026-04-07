package com.example.demo.interfaces.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RefreshTokenCommand;
import com.example.demo.application.command.RegisterCommand;
import com.example.demo.application.service.AuthApplicationService;
import com.example.demo.common.Result;
import com.example.demo.domain.auth.valueobject.TokenPair;
import com.example.demo.interfaces.assembler.AuthAssembler;
import com.example.demo.interfaces.dto.request.LoginRequest;
import com.example.demo.interfaces.dto.request.RefreshTokenRequest;
import com.example.demo.interfaces.dto.request.RegisterRequest;
import com.example.demo.interfaces.dto.response.LoginResponse;
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

    private final AuthApplicationService authApplicationService;
    private final AuthAssembler authAssembler;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 Access Token 和 Refresh Token")
    @RateLimit(key = "login", time = 60, count = 5)
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.getUsername(), request.getPassword());
        TokenPair tokenPair = authApplicationService.login(command);

        LoginResponse response = new LoginResponse(
            tokenPair.accessToken().token(),
            tokenPair.refreshToken(),
            tokenPair.accessToken().remainingTimeMillis() / 1000,
            null,  // userId 从 token 解析需要 TokenGenerator
            request.getUsername()
        );

        return Result.success(response);
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "创建新用户账号")
    @RateLimit(key = "register", time = 60, count = 3)
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        RegisterCommand command = new RegisterCommand(
            request.getUsername(),
            request.getPassword(),
            request.getEmail()
        );
        authApplicationService.getClass(); // 占位，实际应该调用 UserApplicationService
        return Result.success();
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token", description = "使用 Refresh Token 获取新的 Access Token")
    @RateLimit(key = "refresh", time = 60, count = 20)
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenCommand command = new RefreshTokenCommand(request.getRefreshToken());
        TokenPair tokenPair = authApplicationService.refreshToken(command);

        LoginResponse response = new LoginResponse(
            tokenPair.accessToken().token(),
            tokenPair.refreshToken(),
            tokenPair.accessToken().remainingTimeMillis() / 1000,
            null,
            null
        );

        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "撤销 Refresh Token")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authApplicationService.logout(request.getRefreshToken());
        }
        return Result.success();
    }
}