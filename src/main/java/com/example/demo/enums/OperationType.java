package com.example.demo.enums;

/**
 * 操作类型枚举
 */
public enum OperationType {
    LOGIN("登录"),
    LOGOUT("登出"),
    REGISTER("注册"),
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    VIEW("查看");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}