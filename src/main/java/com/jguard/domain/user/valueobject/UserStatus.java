package com.jguard.domain.user.valueobject;

import com.jguard.domain.shared.exception.DomainException;
import java.util.Arrays;

/**
 * 用户状态值对象（枚举）
 */
public enum UserStatus {

    ENABLED(1, "正常"),
    DISABLED(0, "禁用");

    private final int code;
    private final String description;

    UserStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    /**
     * 根据状态码转换为枚举
     */
    public static UserStatus fromCode(int code) {
        return Arrays.stream(values())
            .filter(status -> status.code == code)
            .findFirst()
            .orElseThrow(() -> new DomainException("无效的用户状态码: " + code));
    }

    /**
     * 判断是否为启用状态
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }

    /**
     * 判断是否为禁用状态
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}