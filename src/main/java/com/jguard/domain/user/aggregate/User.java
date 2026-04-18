package com.jguard.domain.user.aggregate;

import com.jguard.domain.shared.common.BaseEntity;
import com.jguard.domain.shared.event.DomainEvent;
import com.jguard.domain.shared.exception.DomainException;
import com.jguard.domain.user.event.UserRegistered;
import com.jguard.domain.user.event.UserStatusChanged;
import com.jguard.domain.user.event.UserUpdated;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户聚合根
 * 封装用户相关的所有业务规则和行为
 */
public class User extends BaseEntity<UserId> {

    private Username username;
    private Email email;
    private EncryptedPassword password;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 工厂方法：注册新用户
     * ID 由数据库生成，创建时为 null
     */
    public static User register(Username username, Email email, EncryptedPassword password) {
        User user = new User();
        // ID 不设置，由数据库自增生成
        user.username = username;
        user.email = email;
        user.password = password;
        user.status = UserStatus.ENABLED;
        user.createTime = LocalDateTime.now();
        user.updateTime = LocalDateTime.now();
        // 注册事件（ID 在持久化后设置）
        user.registerEvent(new UserRegistered(null, user.getUsername()));
        return user;
    }

    /**
     * 工厂方法：重建已存在的用户（从数据库加载）
     */
    public static User rebuild(UserId id, Username username, Email email,
                               EncryptedPassword password, UserStatus status,
                               LocalDateTime createTime, LocalDateTime updateTime) {
        User user = new User();
        user.setId(id);
        user.username = username;
        user.email = email;
        user.password = password;
        user.status = status;
        user.createTime = createTime;
        user.updateTime = updateTime;
        return user;
    }

    /**
     * 设置 ID（持久化后由 Repository 调用）
     */
    public void setIdAfterPersist(Long id) {
        this.setId(new UserId(id));
        // 更新待发布事件的 ID
        this.events().forEach(event -> {
            if (event instanceof UserRegistered) {
                // 事件已发布，无需更新
            }
        });
    }

    /**
     * 业务行为：更新用户资料
     */
    public void updateProfile(Username username, Email email) {
        this.username = username;
        this.email = email;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserUpdated(this.getId()));
    }

    /**
     * 业务行为：启用账号
     */
    public void enable() {
        if (this.status == UserStatus.ENABLED) {
            throw new DomainException("账号已处于启用状态");
        }
        this.status = UserStatus.ENABLED;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserStatusChanged(this.getId(), UserStatus.ENABLED));
    }

    /**
     * 业务行为：禁用账号
     */
    public void disable() {
        if (this.status == UserStatus.DISABLED) {
            throw new DomainException("账号已处于禁用状态");
        }
        this.status = UserStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
        this.registerEvent(new UserStatusChanged(this.getId(), UserStatus.DISABLED));
    }

    /**
     * 业务行为：验证密码
     */
    public boolean validatePassword(org.springframework.security.crypto.password.PasswordEncoder encoder,
                                     String rawPassword) {
        return encoder.matches(rawPassword, this.password.value());
    }

    /**
     * 业务行为：检查是否可登录
     */
    public void validateCanLogin() {
        if (this.status != UserStatus.ENABLED) {
            throw new DomainException("账号已被禁用");
        }
    }

    /**
     * 业务行为：修改密码
     */
    public void changePassword(EncryptedPassword newPassword) {
        this.password = newPassword;
        this.updateTime = LocalDateTime.now();
    }

    // Getters

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public EncryptedPassword getPassword() {
        return password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 获取所有待发布的领域事件
     */
    public List<DomainEvent> pendingEvents() {
        return this.events();
    }

    /**
     * 清除已发布的领域事件
     */
    public void clearPendingEvents() {
        this.clearEvents();
    }
}