package com.example.demo.domain.user.valueobject;

import com.example.demo.domain.shared.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户标识值对象
 * 使用 Long 类型适配数据库自增 ID
 */
public class UserId {

    private final Long value;

    @JsonCreator
    public UserId(Long value) {
        if (value == null) {
            throw new DomainException("用户ID不能为空");
        }
        if (value <= 0) {
            throw new DomainException("用户ID必须大于0");
        }
        this.value = value;
    }

    /**
     * 从字符串创建（用于 API 参数）
     */
    public static UserId fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("用户ID不能为空");
        }
        try {
            return new UserId(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new DomainException("用户ID格式无效: " + value);
        }
    }

    @JsonValue
    public Long value() {
        return this.value;
    }

    public String asString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UserId other = (UserId) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}