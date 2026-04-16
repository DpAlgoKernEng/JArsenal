package com.example.demo.controller;

import com.example.demo.application.dto.AuditLogResponse;
import com.example.demo.application.service.PermissionAuditQueryService;
import com.example.demo.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 权限审计控制器
 */
@RestController
@RequestMapping("/api/permission-audit-logs")
public class PermissionAuditController {

    private final PermissionAuditQueryService auditQueryService;

    public PermissionAuditController(PermissionAuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    /**
     * 根据目标查询审计日志
     */
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
    @GetMapping("/operator/{operatorId}")
    public Result<List<AuditLogResponse>> getByOperator(@PathVariable Long operatorId,
                                                         @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditQueryService.getLogsByOperator(operatorId, limit);
        return Result.success(logs);
    }

    /**
     * 搜索审计日志
     */
    @GetMapping("/search")
    public Result<List<AuditLogResponse>> search(@RequestParam String changeType,
                                                  @RequestParam String startTime,
                                                  @RequestParam String endTime) {
        List<AuditLogResponse> logs = auditQueryService.searchLogs(changeType, startTime, endTime);
        return Result.success(logs);
    }
}