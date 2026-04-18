package com.jguard.application.service;

import com.jguard.application.command.RegisterCommand;
import com.jguard.application.command.UpdateUserCommand;
import com.jguard.domain.shared.exception.DomainException;
import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.repository.UserRepository;
import com.jguard.domain.user.service.UserDomainService;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import com.jguard.infrastructure.outbox.OutboxDomainEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户应用服务
 * 协调领域对象完成业务用例
 */
@Service
@Transactional
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final OutboxDomainEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    public UserApplicationService(UserRepository userRepository,
                                   UserDomainService userDomainService,
                                   OutboxDomainEventPublisher eventPublisher,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.eventPublisher = eventPublisher;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 注册新用户
     */
    public Long register(RegisterCommand command) {
        Username username = new Username(command.getUsername());
        Email email = new Email(command.getEmail());

        // 领域服务校验唯一性（先校验，避免不必要的密码加密）
        userDomainService.ensureUsernameUnique(username);

        // 加密密码
        EncryptedPassword password = new EncryptedPassword(
            passwordEncoder.encode(command.getPassword())
        );

        // 创建用户聚合根
        User user = User.register(username, email, password);

        // 持久化
        userRepository.save(user);

        // 发布领域事件 (使用 Outbox 保证原子性)
        eventPublisher.setAggregateContext("User", user.getId().asString());
        eventPublisher.publishAll(user.pendingEvents());
        eventPublisher.clearAggregateContext();
        user.clearPendingEvents();

        return user.getId().value();
    }

    /**
     * 更新用户信息
     */
    public void updateUser(UpdateUserCommand command) {
        User user = userRepository.findById(new UserId(command.getUserId()));
        if (user == null) {
            throw new DomainException("用户不存在");
        }

        if (command.getUsername() != null) {
            user.updateProfile(new Username(command.getUsername()), new Email(command.getEmail()));
        }

        if (command.getStatus() != null) {
            UserStatus status = UserStatus.fromCode(command.getStatus());
            if (status == UserStatus.ENABLED) {
                user.enable();
            } else {
                user.disable();
            }
        }

        userRepository.save(user);

        // 发布领域事件
        eventPublisher.setAggregateContext("User", user.getId().asString());
        eventPublisher.publishAll(user.pendingEvents());
        eventPublisher.clearAggregateContext();
        user.clearPendingEvents();
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userRepository.delete(new UserId(userId));
    }

    /**
     * 根据ID查询用户
     */
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(new UserId(userId));
    }

    /**
     * 分页查询用户列表
     */
    @Transactional(readOnly = true)
    public List<User> listUsers(String username, Integer status, int pageNum, int pageSize) {
        return userRepository.findAll(username, status, pageNum, pageSize);
    }

    /**
     * 统计用户总数
     */
    @Transactional(readOnly = true)
    public long countUsers(String username, Integer status) {
        return userRepository.count(username, status);
    }
}