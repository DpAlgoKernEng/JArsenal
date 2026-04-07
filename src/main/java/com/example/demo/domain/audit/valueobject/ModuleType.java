package com.example.demo.domain.audit.valueobject;

/**
 * 模块类型枚举
 */
public enum ModuleType {
    AUTH("AUTH", "认证模块"),
    USER("USER", "用户模块");

    private final String code;
    private final String description;

    ModuleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}