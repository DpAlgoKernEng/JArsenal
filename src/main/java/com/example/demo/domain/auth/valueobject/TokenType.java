package com.example.demo.domain.auth.valueobject;

/**
 * Token 类型枚举
 */
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}