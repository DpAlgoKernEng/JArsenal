package com.jguard.domain.user.service;

import com.jguard.domain.shared.exception.DomainException;
import com.jguard.domain.user.repository.UserRepository;
import com.jguard.domain.user.valueobject.Username;
import org.springframework.stereotype.Service;

/**
 * 用户领域服务
 * 处理跨聚合根的业务逻辑（如唯一性校验）
 */
@Service
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 确保用户名唯一
     * @param username 用户名
     * @throws DomainException 如果用户名已存在
     */
    public void ensureUsernameUnique(Username username) {
        if (userRepository.existsByUsername(username)) {
            throw new DomainException("用户名已存在: " + username.value());
        }
    }

    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 是否可用
     */
    public boolean isUsernameAvailable(Username username) {
        return !userRepository.existsByUsername(username);
    }
}