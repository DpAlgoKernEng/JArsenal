package com.jguard.infrastructure.persistence.converter;

import com.jguard.domain.user.aggregate.User;
import com.jguard.domain.user.valueobject.Email;
import com.jguard.domain.user.valueobject.EncryptedPassword;
import com.jguard.domain.user.valueobject.UserId;
import com.jguard.domain.user.valueobject.Username;
import com.jguard.domain.user.valueobject.UserStatus;
import com.jguard.infrastructure.persistence.po.UserPO;
import org.springframework.stereotype.Component;

/**
 * User 领域对象与持久化对象转换器
 */
@Component
public class UserConverter {

    /**
     * PO 转 领域对象
     */
    public User toDomain(UserPO po) {
        if (po == null) {
            return null;
        }
        return User.rebuild(
            new UserId(po.getId()),
            new Username(po.getUsername()),
            new Email(po.getEmail()),
            new EncryptedPassword(po.getPassword()),
            UserStatus.fromCode(po.getStatus()),
            po.getCreateTime(),
            po.getUpdateTime()
        );
    }

    /**
     * 领域对象 转 PO
     */
    public UserPO toPO(User user) {
        if (user == null) {
            return null;
        }
        UserPO po = new UserPO();
        if (user.getId() != null) {
            po.setId(user.getId().value());
        }
        po.setUsername(user.getUsername().value());
        po.setEmail(user.getEmail().value());
        po.setPassword(user.getPassword().value());
        po.setStatus(user.getStatus().code());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }
}