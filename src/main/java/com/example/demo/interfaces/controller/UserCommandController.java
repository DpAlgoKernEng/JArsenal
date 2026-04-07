package com.example.demo.interfaces.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.application.command.UpdateUserCommand;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.common.Result;
import com.example.demo.domain.user.aggregate.User;
import com.example.demo.interfaces.assembler.UserAssembler;
import com.example.demo.interfaces.dto.request.UserCreateRequest;
import com.example.demo.interfaces.dto.request.UserUpdateRequest;
import com.example.demo.interfaces.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户命令控制器 (CQRS - 写操作)
 */
@Tag(name = "用户管理", description = "用户增删改接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserApplicationService userApplicationService;
    private final UserAssembler userAssembler;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "新增一个用户")
    @RateLimit(key = "userCreate", time = 60, count = 10)
    @PostMapping
    public Result<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        // 使用注册逻辑创建用户
        Long userId = userApplicationService.register(
            new com.example.demo.application.command.RegisterCommand(
                request.getUsername(),
                request.getPassword(),
                request.getEmail()
            )
        );

        User user = userApplicationService.getUserById(userId);
        return Result.success(userAssembler.toResponse(user));
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "修改用户信息")
    @RateLimit(key = "userUpdate", time = 60, count = 20)
    @PutMapping("/{id}")
    public Result<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        UpdateUserCommand command = new UpdateUserCommand(
            id,
            request.getUsername(),
            request.getEmail(),
            request.getStatus()
        );

        userApplicationService.updateUser(command);

        User user = userApplicationService.getUserById(id);
        return Result.success(userAssembler.toResponse(user));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @RateLimit(key = "userDelete", time = 60, count = 10)
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userApplicationService.deleteUser(id);
        return Result.success();
    }
}