package com.example.demo.service.impl;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.RefreshTokenMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AuthService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 3. 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 4. 生成双 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 5. 存储 Refresh Token
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUserId(user.getId());
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpiration() * 1_000_000));
        refreshTokenMapper.insert(tokenEntity);

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtUtil.getAccessExpiration(),
                user.getId(),
                user.getUsername()
        );
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // 1. 检查用户名是否存在
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 2. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 1. 验证 Refresh Token 格式
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessException(401, "无效的 Refresh Token");
        }

        // 2. 从 Token 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 3. 检查数据库中的 Refresh Token
        RefreshToken storedToken = refreshTokenMapper.selectByToken(refreshToken);
        if (storedToken == null) {
            throw new BusinessException(401, "Refresh Token 不存在");
        }
        if (storedToken.getRevoked() == 1) {
            throw new BusinessException(401, "Refresh Token 已被撤销");
        }
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(401, "Refresh Token 已过期");
        }

        // 4. 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(401, "用户不存在或已禁用");
        }

        // 5. 撤销旧的 Refresh Token
        refreshTokenMapper.revokeByToken(refreshToken);

        // 6. 生成新的双 Token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 7. 存储新的 Refresh Token
        RefreshToken newTokenEntity = new RefreshToken();
        newTokenEntity.setUserId(user.getId());
        newTokenEntity.setToken(newRefreshToken);
        newTokenEntity.setExpiresAt(LocalDateTime.now().plusNanos(jwtUtil.getRefreshExpiration() * 1_000_000));
        refreshTokenMapper.insert(newTokenEntity);

        log.info("Token刷新成功: userId={}", userId);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                jwtUtil.getAccessExpiration(),
                user.getId(),
                user.getUsername()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenMapper.revokeByToken(refreshToken);
            log.info("用户登出成功: token={}", refreshToken.substring(0, Math.min(10, refreshToken.length())) + "...");
        }
    }
}