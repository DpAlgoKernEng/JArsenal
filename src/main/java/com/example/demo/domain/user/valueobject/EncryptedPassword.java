package com.example.demo.domain.user.valueobject;

import com.example.demo.domain.shared.exception.DomainException;

/**
 * 加密密码值对象
 * 存储经过 BCrypt 加密的密码
 */
public class EncryptedPassword {

    private final String value;

    public EncryptedPassword(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("密码不能为空");
        }
        // BCrypt 密码长度为 60
        if (value.length() != 60 && !value.startsWith("$2a$")) {
            throw new DomainException("密码格式不正确，需要 BCrypt 加密");
        }
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EncryptedPassword other = (EncryptedPassword) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "***"; // 安全考虑，不暴露密码
    }
}