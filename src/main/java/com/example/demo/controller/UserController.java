package com.example.demo.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.common.Result;
import com.example.demo.dto.PageResult;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserQueryRequest;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户增删改查接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "支持按用户名和状态筛选")
    @ApiResponse(responseCode = "200", description = "查询成功")
    @RateLimit(key = "userList", time = 60, count = 100)
    @GetMapping
    public Result<PageResult<User>> listUsers(UserQueryRequest request) {
        PageResult<User> result = userService.listUsers(request);
        return Result.success(result);
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "根据ID查询用户", description = "获取单个用户详细信息")
    @GetMapping("/{id}")
    public Result<User> getUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "新增一个用户")
    @RateLimit(key = "userCreate", time = 60, count = 10)
    @PostMapping
    public Result<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        User created = userService.createUser(user);
        return Result.success(created);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "修改用户信息")
    @RateLimit(key = "userUpdate", time = 60, count = 20)
    @PutMapping("/{id}")
    public Result<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        User user = new User();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        User updated = userService.updateUser(user);
        return Result.success(updated);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @RateLimit(key = "userDelete", time = 60, count = 10)
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}