package com.jguard.interfaces.controller;

import com.jguard.infrastructure.annotation.RateLimit;
import com.jguard.application.command.LoginCommand;
import com.jguard.application.command.RefreshTokenCommand;
import com.jguard.application.command.RegisterCommand;
import com.jguard.application.service.AuthApplicationService;
import com.jguard.application.service.UserApplicationService;
import com.jguard.infrastructure.common.Result;
import com.jguard.domain.auth.valueobject.TokenPair;
import com.jguard.interfaces.assembler.AuthAssembler;
import com.jguard.interfaces.dto.request.LoginRequest;
import com.jguard.interfaces.dto.request.RefreshTokenRequest;
import com.jguard.interfaces.dto.request.RegisterRequest;
import com.jguard.interfaces.dto.response.LoginResponse;
import com.jguard.infrastructure.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户登录注册接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;
    private final UserApplicationService userApplicationService;
    private final AuthAssembler authAssembler;
    private final JwtUtil jwtUtil;

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "使用用户名密码登录，返回 Access Token，Refresh Token 通过 HttpOnly Cookie 返回")
    @RateLimit(key = "login", time = 60, count = 5)
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                        HttpServletResponse response) {
        LoginCommand command = new LoginCommand(request.getUsername(), request.getPassword());
        TokenPair tokenPair = authApplicationService.login(command);

        // 设置 Refresh Token 为 HttpOnly Cookie
        setRefreshTokenCookie(response, tokenPair.refreshToken());

        // 只在响应体中返回 Access Token（不暴露 Refresh Token）
        LoginResponse loginResponse = new LoginResponse(
            tokenPair.accessToken().token(),
            null,  // Refresh Token 已通过 Cookie 返回，不在响应体中暴露
            tokenPair.accessToken().remainingTimeMillis() / 1000,
            null,
            request.getUsername()
        );

        return Result.success(loginResponse);
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
        userApplicationService.register(command);
        return Result.success();
    }

    /**
     * 刷新 Token
     */
    @Operation(summary = "刷新Token", description = "使用 Refresh Token (Cookie) 获取新的 Access Token")
    @RateLimit(key = "refresh", time = 60, count = 20)
    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(HttpServletRequest request,
                                               HttpServletResponse response) {
        // 从 Cookie 读取 Refresh Token
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return Result.error(401, "Refresh Token 不存在");
        }

        RefreshTokenCommand command = new RefreshTokenCommand(refreshToken);
        TokenPair tokenPair = authApplicationService.refreshToken(command);

        // 更新 Refresh Token Cookie
        setRefreshTokenCookie(response, tokenPair.refreshToken());

        LoginResponse loginResponse = new LoginResponse(
            tokenPair.accessToken().token(),
            null,  // Refresh Token 已通过 Cookie 返回
            tokenPair.accessToken().remainingTimeMillis() / 1000,
            null,
            null
        );

        return Result.success(loginResponse);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "撤销 Refresh Token 并清除 Cookie")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 从 Cookie 读取并撤销 Refresh Token
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            authApplicationService.logout(refreshToken);
        }

        // 清除 Refresh Token Cookie
        clearRefreshTokenCookie(response);

        return Result.success();
    }

    /**
     * 设置 Refresh Token Cookie (HttpOnly, Secure, SameSite=Strict)
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .sameSite("Strict")  // 防止 CSRF 攻击
            .maxAge(refreshExpiration / 1000)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * 从 Cookie 读取 Refresh Token
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    /**
     * 清除 Refresh Token Cookie (HttpOnly, Secure, SameSite=Strict)
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .sameSite("Strict")
            .maxAge(0)
            .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}