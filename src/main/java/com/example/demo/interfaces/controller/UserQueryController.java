package com.example.demo.interfaces.controller;

import com.example.demo.annotation.RateLimit;
import com.example.demo.application.service.UserApplicationService;
import com.example.demo.common.Result;
import com.example.demo.domain.user.aggregate.User;
import com.example.demo.interfaces.assembler.UserAssembler;
import com.example.demo.interfaces.dto.request.UserQueryRequest;
import com.example.demo.interfaces.dto.response.PageResult;
import com.example.demo.interfaces.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户查询控制器 (CQRS - 读操作)
 */
@Tag(name = "用户管理", description = "用户查询接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserApplicationService userApplicationService;
    private final UserAssembler userAssembler;

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "支持按用户名和状态筛选")
    @RateLimit(key = "userList", time = 60, count = 100)
    @GetMapping
    public Result<PageResult<UserResponse>> listUsers(UserQueryRequest request) {
        List<User> users = userApplicationService.listUsers(
            request.getUsername(),
            request.getStatus(),
            request.getPageNum(),
            request.getPageSize()
        );

        long total = userApplicationService.countUsers(request.getUsername(), request.getStatus());

        PageResult<UserResponse> result = new PageResult<>(
            userAssembler.toResponseList(users),
            total,
            request.getPageNum(),
            request.getPageSize()
        );

        return Result.success(result);
    }

    /**
     * 根据ID查询用户
     */
    @Operation(summary = "根据ID查询用户", description = "获取单个用户详细信息")
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        User user = userApplicationService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(userAssembler.toResponse(user));
    }
}