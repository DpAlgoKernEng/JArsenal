package com.jguard.interfaces.controller;

import com.jguard.application.dto.AuditLogResponse;
import com.jguard.application.service.PermissionAuditQueryService;
import com.jguard.infrastructure.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 权限审计日志查询控制器
 */
@Tag(name = "权限审计日志", description = "权限变更审计日志查询接口")
@RestController
@RequestMapping("/api/v1/permission-audit-logs")
@RequiredArgsConstructor
public class PermissionAuditQueryController {

    private final PermissionAuditQueryService auditQueryService;

    /**
     * 根据目标查询审计日志
     */
    @Operation(summary = "按目标查询审计日志", description = "查询指定角色或用户的权限变更审计日志")
    @GetMapping("/target/{targetType}/{targetId}")
    public Result<List<AuditLogResponse>> getByTarget(@PathVariable String targetType,
                                                       @PathVariable Long targetId,
                                                       @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditQueryService.getLogsByTarget(targetType, targetId, limit);
        return Result.success(logs);
    }

    /**
     * 根据操作人查询审计日志
     */
    @Operation(summary = "按操作人查询审计日志", description = "查询指定操作人执行的权限变更记录")
    @GetMapping("/operator/{operatorId}")
    public Result<List<AuditLogResponse>> getByOperator(@PathVariable Long operatorId,
                                                         @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditQueryService.getLogsByOperator(operatorId, limit);
        return Result.success(logs);
    }

    /**
     * 搜索审计日志
     */
    @Operation(summary = "搜索审计日志", description = "按变更类型和时间范围搜索权限变更记录")
    @GetMapping("/search")
    public Result<List<AuditLogResponse>> search(@RequestParam String changeType,
                                                  @RequestParam String startTime,
                                                  @RequestParam String endTime) {
        List<AuditLogResponse> logs = auditQueryService.searchLogs(changeType, startTime, endTime);
        return Result.success(logs);
    }
}