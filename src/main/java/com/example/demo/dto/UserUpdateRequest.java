package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(min = 2, max = 20, message = "用户名长度必须在2-20之间")
    private String username;

    @jakarta.validation.constraints.Email(message = "邮箱格式不正确")
    private String email;

    @Min(value = 0, message = "状态值必须大于等于0")
    private Integer status;
}