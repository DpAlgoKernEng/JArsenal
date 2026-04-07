package com.example.demo.application.service;

import com.example.demo.application.command.LoginCommand;
import com.example.demo.application.command.RefreshTokenCommand;
import com.example.demo.domain.auth.aggregate.Session;
import com.example.demo.domain.auth.entity.RefreshToken;
import com.example.demo.domain.auth.repository.TokenRepository;
import com.example.demo.domain.auth.service.TokenGenerator;
import com.example.demo.domain.auth.valueobject.TokenPair;
import com.example.demo.domain.shared.exception.DomainException;
import com.example.demo.domain.user.aggregate.User;
import com.example.demo.domain.user.repository.UserRepository;
import com.example.demo.domain.user.valueobject.Username;
import com.example.demo.infrastructure.outbox.OutboxDomainEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务
 */
@Service
@Transactional
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final TokenGenerator tokenGenerator;
    private final OutboxDomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public AuthApplicationService(UserRepository userRepository,
                                   TokenRepository tokenRepository,
                                   TokenGenerator tokenGenerator,
                                   OutboxDomainEventPublisher eventPublisher,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录
     */
    public TokenPair login(LoginCommand command) {
        User user = userRepository.findByUsername(new Username(command.getUsername()));

        if (user == null || !user.validatePassword(passwordEncoder, command.getPassword())) {
            throw new DomainException("用户名或密码错误");
        }

        user.validateCanLogin();

        Session session = Session.create(user.getId(), user.getUsername(), tokenGenerator);

        // 保存 Refresh Token
        tokenRepository.save(session.getRefreshToken());

        // 发布领域事件 (使用 Outbox)
        eventPublisher.setAggregateContext("Session", session.getId().value());
        eventPublisher.publishAll(session.pendingEvents());
        eventPublisher.clearAggregateContext();
        session.clearPendingEvents();

        return new TokenPair(session.getAccessToken(), session.getRefreshToken().getToken());
    }

    /**
     * 刷新 Token
     */
    public TokenPair refreshToken(RefreshTokenCommand command) {
        RefreshToken refreshToken = tokenRepository.findByToken(command.getRefreshToken());

        if (refreshToken == null) {
            throw new DomainException("无效的 Refresh Token");
        }

        refreshToken.validate();

        User user = userRepository.findById(refreshToken.getUserId());
        if (user == null) {
            throw new DomainException("用户不存在");
        }

        Session session = Session.create(user.getId(), user.getUsername(), tokenGenerator);

        // 撤销旧 Token
        tokenRepository.revoke(refreshToken);

        // 保存新 Token
        tokenRepository.save(session.getRefreshToken());

        // 发布领域事件
        eventPublisher.setAggregateContext("Session", session.getId().value());
        eventPublisher.publishAll(session.pendingEvents());
        eventPublisher.clearAggregateContext();
        session.clearPendingEvents();

        return new TokenPair(session.getAccessToken(), session.getRefreshToken().getToken());
    }

    /**
     * 用户登出
     */
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            RefreshToken token = tokenRepository.findByToken(refreshToken);
            if (token != null) {
                tokenRepository.revoke(token);
            }
        }
    }
}