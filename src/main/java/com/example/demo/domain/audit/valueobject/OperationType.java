package com.example.demo.domain.audit.valueobject;

/**
 * 操作类型枚举
 */
public enum OperationType {
    LOGIN("LOGIN", "登录"),
    LOGOUT("LOGOUT", "登出"),
    REGISTER("REGISTER", "注册"),
    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除");

    private final String code;
    private final String description;

    OperationType(String code, String description) {
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