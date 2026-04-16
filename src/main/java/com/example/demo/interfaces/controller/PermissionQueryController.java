package com.example.demo.interfaces.controller;

import com.example.demo.infrastructure.annotation.RateLimit;
import com.example.demo.application.dto.ActionPermissionDTO;
import com.example.demo.application.dto.FieldPermissionDTO;
import com.example.demo.application.dto.MenuDTO;
import com.example.demo.application.dto.UserPermissionsDTO;
import com.example.demo.application.service.PermissionQueryService;
import com.example.demo.infrastructure.common.Result;
import com.example.demo.infrastructure.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限查询控制器
 * 用于前端权限系统
 */
@Tag(name = "权限查询", description = "前端权限查询接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PermissionQueryController {

    private final PermissionQueryService permissionQueryService;

    /**
     * 获取用户完整权限信息
     */
    @Operation(summary = "获取用户权限", description = "返回用户的菜单、操作权限、字段权限和版本号")
    @RateLimit(key = "userPermissions", time = 60, count = 100)
    @GetMapping("/permissions")
    public Result<UserPermissionsDTO> getUserPermissions() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        UserPermissionsDTO permissions = permissionQueryService.getUserPermissions();
        return Result.success(permissions);
    }

    /**
     * 获取用户菜单（树结构）
     */
    @Operation(summary = "获取用户菜单", description = "返回用户可访问的菜单树结构")
    @RateLimit(key = "userMenus", time = 60, count = 100)
    @GetMapping("/menus")
    public Result<List<MenuDTO>> getUserMenus() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<MenuDTO> menus = permissionQueryService.getUserMenus();
        return Result.success(menus);
    }

    /**
     * 获取用户操作权限
     */
    @Operation(summary = "获取用户操作权限", description = "返回用户在各资源上的操作权限")
    @RateLimit(key = "userActions", time = 60, count = 100)
    @GetMapping("/actions")
    public Result<List<ActionPermissionDTO>> getUserActions() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<ActionPermissionDTO> actions = permissionQueryService.getUserActions();
        return Result.success(actions);
    }

    /**
     * 获取用户字段权限
     */
    @Operation(summary = "获取用户字段权限", description = "返回用户在各资源字段上的查看/编辑权限")
    @RateLimit(key = "userFields", time = 60, count = 100)
    @GetMapping("/fields")
    public Result<List<FieldPermissionDTO>> getUserFields() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        List<FieldPermissionDTO> fields = permissionQueryService.getUserFields();
        return Result.success(fields);
    }

    /**
     * 获取权限版本号
     */
    @Operation(summary = "获取权限版本", description = "返回用户权限的版本号，用于前端轮询判断是否需要刷新")
    @RateLimit(key = "permissionVersion", time = 60, count = 1000)
    @GetMapping("/permissions/version")
    public Result<Long> getPermissionVersion() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        long version = permissionQueryService.getPermissionVersion();
        return Result.success(version);
    }
}