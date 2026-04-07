package com.example.demo.interfaces.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户更新请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20字符之间")
    private String username;

    private String email;

    @Min(value = 0, message = "状态值必须大于等于0")
    private Integer status;
}