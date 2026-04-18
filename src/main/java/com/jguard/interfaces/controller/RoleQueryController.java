package com.jguard.interfaces.controller;

import com.jguard.infrastructure.annotation.RateLimit;
import com.jguard.application.service.RoleApplicationService;
import com.jguard.infrastructure.common.Result;
import com.jguard.domain.permission.aggregate.Role;
import com.jguard.interfaces.assembler.RoleAssembler;
import com.jguard.interfaces.dto.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色查询控制器 (CQRS - 读操作)
 */
@Tag(name = "角色管理", description = "角色查询接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleQueryController {

    private final RoleApplicationService roleApplicationService;
    private final RoleAssembler roleAssembler;

    /**
     * 查询角色列表（树结构）
     */
    @Operation(summary = "查询角色树", description = "返回所有角色的树形结构")
    @RateLimit(key = "roleTree", time = 60, count = 100)
    @GetMapping
    public Result<List<RoleResponse>> getRoleTree() {
        List<Role> roles = roleApplicationService.getRoleTree();
        List<RoleResponse> tree = roleAssembler.buildRoleTree(roles);
        return Result.success(tree);
    }

    /**
     * 根据ID查询角色详情
     */
    @Operation(summary = "根据ID查询角色", description = "获取单个角色详细信息")
    @RateLimit(key = "roleDetail", time = 60, count = 100)
    @GetMapping("/{id}")
    public Result<RoleResponse> getRole(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        Role role = roleApplicationService.getRoleById(id);
        if (role == null) {
            return Result.error(404, "角色不存在");
        }
        return Result.success(roleAssembler.toResponse(role));
    }

    /**
     * 查询用户的角色列表
     */
    @Operation(summary = "查询用户角色", description = "获取指定用户拥有的角色列表")
    @RateLimit(key = "userRoles", time = 60, count = 100)
    @GetMapping("/user/{userId}")
    public Result<List<RoleResponse>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<Role> roles = roleApplicationService.getRolesByUserId(userId);
        return Result.success(roleAssembler.toResponseList(roles));
    }
}