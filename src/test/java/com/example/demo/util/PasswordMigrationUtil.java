package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码迁移工具
 * 用于生成前端SHA256加密后的BCrypt哈希
 */
public class PasswordMigrationUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // 原始密码: 123456
        String originalPassword = "123456";

        // 前端SHA256哈希后的值
        String sha256Hash = sha256(originalPassword);

        // 后端BCrypt加密后的值（存储到数据库）
        String bcryptHash = encoder.encode(sha256Hash);

        System.out.println("原始密码: " + originalPassword);
        System.out.println("SHA256哈希: " + sha256Hash);
        System.out.println("BCrypt哈希(存储): " + bcryptHash);
        System.out.println();

        // 验证
        boolean matches = encoder.matches(sha256Hash, bcryptHash);
        System.out.println("验证结果: " + matches);
    }

    // SHA-256 哈希（与前端js-sha256一致）
    private static String sha256(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}