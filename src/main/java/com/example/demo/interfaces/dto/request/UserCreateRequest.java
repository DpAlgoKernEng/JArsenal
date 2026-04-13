package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户创建请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20字符之间")
    private String username;

    @Size(min = 6, max = 100, message = "密码长度必须在6-100字符之间")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 0, message = "状态值必须大于等于0")
    private Integer status = 1;
}