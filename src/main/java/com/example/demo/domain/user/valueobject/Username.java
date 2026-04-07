package com.example.demo.domain.user.valueobject;

import com.example.demo.domain.shared.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户名值对象
 * 包含验证逻辑：长度 2-50 字符
 */
public class Username {

    private final String value;

    @JsonCreator
    public Username(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("用户名不能为空");
        }
        if (value.length() < 2) {
            throw new DomainException("用户名长度不能少于2个字符");
        }
        if (value.length() > 50) {
            throw new DomainException("用户名长度不能超过50个字符");
        }
        this.value = value;
    }

    @JsonValue
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
        Username other = (Username) obj;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}