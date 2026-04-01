package com.example.demo.enums;

/**
 * 模块类型枚举
 */
public enum ModuleType {
    AUTH("认证模块"),
    USER("用户模块");

    private final String description;

    ModuleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}