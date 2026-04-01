package com.example.demo.security;

/**
 * 用户上下文 - 基于 ThreadLocal 存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(Long userId, String username) {
        CONTEXT.set(new UserInfo(userId, username));
    }

    /**
     * 获取当前用户信息
     */
    public static UserInfo getCurrentUser() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserInfo user = CONTEXT.get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserInfo user = CONTEXT.get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 清理上下文（防止内存泄漏）
     */
    public static void clear() {
        CONTEXT.remove();
    }
}