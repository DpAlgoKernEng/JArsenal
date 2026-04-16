package com.example.demo.interfaces.controller;

import com.example.demo.infrastructure.annotation.RateLimit;
import com.example.demo.application.command.AssignPermissionCommand;
import com.example.demo.application.command.AssignRoleCommand;
import com.example.demo.application.command.CreateRoleCommand;
import com.example.demo.application.command.UpdateRoleCommand;
import com.example.demo.application.service.RoleApplicationService;
import com.example.demo.infrastructure.common.Result;
import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.interfaces.assembler.RoleAssembler;
import com.example.demo.interfaces.dto.request.AssignPermissionRequest;
import com.example.demo.interfaces.dto.request.AssignRoleRequest;
import com.example.demo.interfaces.dto.request.RoleCreateRequest;
import com.example.demo.interfaces.dto.request.RoleUpdateRequest;
import com.example.demo.interfaces.dto.response.RoleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色命令控制器 (CQRS - 写操作)
 */
@Tag(name = "角色管理", description = "角色增删改接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleCommandController {

    private final RoleApplicationService roleApplicationService;
    private final RoleAssembler roleAssembler;

    /**
     * 创建角色
     */
    @Operation(summary = "创建角色", description = "新增一个角色")
    @RateLimit(key = "roleCreate", time = 60, count = 10)
    @PostMapping
    public Result<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        CreateRoleCommand command = new CreateRoleCommand(
            request.getCode(),
            request.getName(),
            request.getParentId(),
            request.getInheritMode(),
            request.getSort()
        );

        Long roleId = roleApplicationService.createRole(command);

        Role role = roleApplicationService.getRoleById(roleId);
        return Result.success(roleAssembler.toResponse(role));
    }

    /**
     * 更新角色
     */
    @Operation(summary = "更新角色", description = "修改角色信息")
    @RateLimit(key = "roleUpdate", time = 60, count = 20)
    @PutMapping("/{id}")
    public Result<RoleResponse> updateRole(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        UpdateRoleCommand command = new UpdateRoleCommand(
            id,
            request.getName(),
            request.getParentId(),
            request.getInheritMode(),
            request.getStatus(),
            request.getSort()
        );

        roleApplicationService.updateRole(command);

        Role role = roleApplicationService.getRoleById(id);
        return Result.success(roleAssembler.toResponse(role));
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色", description = "软删除角色")
    @RateLimit(key = "roleDelete", time = 60, count = 10)
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long id) {
        roleApplicationService.deleteRole(id);
        return Result.success();
    }

    /**
     * 分配权限给角色
     */
    @Operation(summary = "分配权限", description = "为角色分配资源权限")
    @RateLimit(key = "assignPermission", time = 60, count = 20)
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermission(
            @Parameter(description = "角色ID") @PathVariable Long id,
            @Valid @RequestBody AssignPermissionRequest request) {

        AssignPermissionCommand command = new AssignPermissionCommand(
            id,
            request.getResourceId(),
            request.getActions(),
            request.getEffect()
        );

        roleApplicationService.assignPermission(command);
        return Result.success();
    }

    /**
     * 分配角色给用户
     */
    @Operation(summary = "分配角色给用户", description = "为用户分配多个角色")
    @RateLimit(key = "assignRole", time = 60, count = 20)
    @PostMapping("/user/{userId}/roles")
    public Result<Void> assignRolesToUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {

        AssignRoleCommand command = new AssignRoleCommand(userId, request.getRoleIds());
        roleApplicationService.assignRolesToUser(command);
        return Result.success();
    }

    /**
     * 移除用户的角色
     */
    @Operation(summary = "移除用户角色", description = "移除用户的某个角色")
    @RateLimit(key = "removeRole", time = 60, count = 20)
    @DeleteMapping("/user/{userId}/roles/{roleId}")
    public Result<Void> removeRoleFromUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "角色ID") @PathVariable Long roleId) {

        roleApplicationService.removeRoleFromUser(userId, roleId);
        return Result.success();
    }
}