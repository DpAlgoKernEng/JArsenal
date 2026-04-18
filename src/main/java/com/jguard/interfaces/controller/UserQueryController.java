package com.jguard.interfaces.controller;

import com.jguard.infrastructure.annotation.RateLimit;
import com.jguard.application.dto.UserPermissionsDTO;
import com.jguard.application.service.PermissionQueryService;
import com.jguard.application.service.UserApplicationService;
import com.jguard.infrastructure.common.Result;
import com.jguard.domain.user.aggregate.User;
import com.jguard.interfaces.assembler.UserAssembler;
import com.jguard.interfaces.dto.request.UserQueryRequest;
import com.jguard.interfaces.dto.response.PageResult;
import com.jguard.interfaces.dto.response.UserResponse;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserApplicationService userApplicationService;
    private final UserAssembler userAssembler;
    private final PermissionQueryService permissionQueryService;

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

    /**
     * 查询用户权限详情
     */
    @Operation(summary = "查询用户权限", description = "获取指定用户的菜单、操作和字段权限")
    @RateLimit(key = "userPermissionsQuery", time = 60, count = 50)
    @GetMapping("/{id}/permissions")
    public Result<UserPermissionsDTO> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        // 验证用户存在
        User user = userApplicationService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        UserPermissionsDTO permissions = permissionQueryService.getUserPermissionsByUserId(id);
        return Result.success(permissions);
    }

    /**
     * 查询用户角色列表
     */
    @Operation(summary = "查询用户角色", description = "获取指定用户的角色编码列表")
    @RateLimit(key = "userRolesQuery", time = 60, count = 50)
    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        // 验证用户存在
        User user = userApplicationService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        List<String> roles = permissionQueryService.getUserRoles(id);
        return Result.success(roles);
    }
}