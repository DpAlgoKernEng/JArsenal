package com.jguard.domain.user.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import java.util.regex.Pattern;

/**
 * 邮箱值对象
 * 包含邮箱格式验证逻辑
 */
public class Email {

    private final String value;

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"
    );

    public Email(String value) {
        if (value == null || value.isEmpty()) {
            throw new DomainException("邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainException("邮箱格式不正确: " + value);
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
        Email other = (Email) obj;
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