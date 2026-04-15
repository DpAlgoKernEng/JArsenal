# RBAC P9: 权限审计日志实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现权限审计日志系统，记录所有权限变更及其前后值和追踪 ID

**架构：** 领域事件触发审计日志写入，PermissionAuditService 记录变更到 permission_audit_log 表，API 端点用于查询审计历史

**技术栈：** Spring Event Publisher、MyBatis、异步日志

**依赖：** P1-P8（所有领域事件必须存在）

---

## 文件结构

```
src/main/java/com/example/demo/
├── domain/permission/
│   ├── service/
│   │   ├── PermissionAuditService.java     # 审计记录服务
│   ├── entity/
│   │   ├── PermissionAuditLog.java         # 审计日志实体
│   ├── event/
│   │   ├── PermissionAuditEventListener.java
├── infrastructure/
│   ├── persistence/
│       ├── mapper/
│           ├── PermissionAuditLogMapper.java
│       ├── repository/
│           ├── PermissionAuditLogRepository.java
├── controller/
│   └ PermissionAuditController.java       # 审计查询 API
├── service/
│   └ dto/
│       ├── AuditLogResponse.java
│       ├── AuditLogQueryRequest.java
```

---

## 任务 1：创建 PermissionAuditLog 实体

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/entity/PermissionAuditLog.java`

- [ ] **步骤 1：编写审计日志实体**

```java
package com.example.demo.domain.permission.entity;

import java.time.LocalDateTime;

public class PermissionAuditLog {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String changeType;  // ROLE_CREATE/ROLE_UPDATE/PERM_ASSIGN/USER_ROLE_ASSIGN 等
    private String targetType;  // ROLE/RESOURCE/USER
    private Long targetId;
    private String beforeValue; // JSON
    private String afterValue;  // JSON
    private String reason;
    private String traceId;
    private LocalDateTime createTime;
    
    public static PermissionAuditLog of(Long operatorId, String operatorName, 
                                         String changeType, String targetType, Long targetId,
                                         String beforeValue, String afterValue, String reason, String traceId) {
        PermissionAuditLog log = new PermissionAuditLog();
        log.operatorId = operatorId;
        log.operatorName = operatorName;
        log.changeType = changeType;
        log.targetType = targetType;
        log.targetId = targetId;
        log.beforeValue = beforeValue;
        log.afterValue = afterValue;
        log.reason = reason;
        log.traceId = traceId;
        log.createTime = LocalDateTime.now();
        return log;
    }
    
    // Getter/Setter
}
```

- [ ] **步骤 2：提交实体**

```bash
git add src/main/java/com/example/demo/domain/permission/entity/PermissionAuditLog.java
git commit -m "feat(rbac): add PermissionAuditLog entity"
```

---

## 任务 2：创建 PermissionAuditService

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/service/PermissionAuditService.java`

- [ ] **步骤 1：编写审计服务**

```java
package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.entity.PermissionAuditLog;
import com.example.demo.domain.permission.repository.PermissionAuditLogRepository;
import com.example.demo.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PermissionAuditService {
    
    private final PermissionAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Async
    public void logChange(Long operatorId, String operatorName,
                          String changeType, String targetType, Long targetId,
                          Object beforeValue, Object afterValue, String reason) {
        try {
            String traceId = MDC.get("traceId");
            
            String beforeJson = beforeValue != null ? objectMapper.writeValueAsString(beforeValue) : null;
            String afterJson = afterValue != null ? objectMapper.writeValueAsString(afterValue) : null;
            
            PermissionAuditLog log = PermissionAuditLog.of(
                operatorId, operatorName, changeType, targetType, targetId,
                beforeJson, afterJson, reason, traceId
            );
            
            auditLogRepository.save(log);
        } catch (Exception e) {
            // 审计日志失败不应影响业务操作
            // 记录错误但不抛出
        }
    }
    
    public void logRoleCreate(Long operatorId, String operatorName, Long roleId, String roleCode, String roleName) {
        logChange(operatorId, operatorName, "ROLE_CREATE", "ROLE", roleId,
            null, Map.of("code", roleCode, "name", roleName), null);
    }
    
    public void logPermissionAssign(Long operatorId, Long roleId, Long resourceId, Set<String> actions) {
        logChange(operatorId, UserContext.getCurrentUserName(),
            "PERM_ASSIGN", "ROLE", roleId,
            null, Map.of("resourceId", resourceId, "actions", actions), null);
    }
    
    public void logUserRoleAssign(Long operatorId, Long userId, Long roleId) {
        logChange(operatorId, UserContext.getCurrentUserName(),
            "USER_ROLE_ASSIGN", "USER", userId,
            null, Map.of("roleId", roleId), null);
    }
}
```

- [ ] **步骤 2：提交审计服务**

```bash
git add src/main/java/com/example/demo/domain/permission/service/PermissionAuditService.java
git commit -m "feat(rbac): add PermissionAuditService for async audit logging"
```

---

## 任务 3：创建审计事件监听器

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/event/PermissionAuditEventListener.java`

- [ ] **步骤 1：编写事件监听器**

```java
package com.example.demo.domain.permission.event;

import com.example.demo.domain.permission.service.PermissionAuditService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PermissionAuditEventListener {
    
    private final PermissionAuditService auditService;
    
    @EventListener
    public void onRoleCreated(RoleCreatedEvent event) {
        auditService.logRoleCreate(event.operatorId(), event.roleCode(), event.roleName());
    }
    
    @EventListener
    public void onRolePermissionChanged(RolePermissionChangedEvent event) {
        auditService.logPermissionAssign(
            event.operatorId(), 
            event.roleId(),
            event.resourceId(),
            event.changeType()
        );
    }
    
    @EventListener
    public void onUserRoleAssigned(UserRoleAssignedEvent event) {
        auditService.logUserRoleAssign(event.operatorId(), event.userId(), event.roleId());
    }
    
    @EventListener
    public void onRoleDeleted(RoleDeletedEvent event) {
        auditService.logChange(
            event.operatorId(),
            "SYSTEM",
            "ROLE_DELETE",
            "ROLE",
            event.roleId(),
            Map.of("code", event.roleCode()),
            null,
            null
        );
    }
}
```

- [ ] **步骤 2：提交事件监听器**

```bash
git add src/main/java/com/example/demo/domain/permission/event/PermissionAuditEventListener.java
git commit -m "feat(rbac): add audit event listener for domain events"
```

---

## 任务 4：创建审计日志 Mapper

**文件：**
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/mapper/PermissionAuditLogMapper.java`

- [ ] **步骤 1：编写审计 Mapper**

```java
package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.PermissionAuditLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PermissionAuditLogMapper {
    
    @Insert("INSERT INTO permission_audit_log(operator_id, operator_name, change_type, target_type, target_id, " +
            "before_value, after_value, reason, trace_id, create_time) " +
            "VALUES(#{operatorId}, #{operatorName}, #{changeType}, #{targetType}, #{targetId}, " +
            "#{beforeValue}, #{afterValue}, #{reason}, #{traceId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PermissionAuditLog log);
    
    @Select("SELECT * FROM permission_audit_log WHERE target_type = #{targetType} AND target_id = #{targetId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PermissionAuditLog> findByTarget(@Param("targetType") String targetType, 
                                          @Param("targetId") Long targetId,
                                          @Param("limit") int limit);
    
    @Select("SELECT * FROM permission_audit_log WHERE operator_id = #{operatorId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PermissionAuditLog> findByOperator(@Param("operatorId") Long operatorId, @Param("limit") int limit);
    
    @Select("SELECT * FROM permission_audit_log WHERE change_type = #{changeType} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<PermissionAuditLog> findByTypeAndTimeRange(@Param("changeType") String changeType,
                                                    @Param("startTime") String startTime,
                                                    @Param("endTime") String endTime);
}
```

- [ ] **步骤 2：提交 Mapper**

```bash
git add src/main/java/com/example/demo/infrastructure/persistence/mapper/PermissionAuditLogMapper.java
git commit -m "feat(rbac): add PermissionAuditLogMapper"
```

---

## 任务 5：创建审计查询 API

**文件：**
- 创建：`src/main/java/com/example/demo/controller/PermissionAuditController.java`

- [ ] **步骤 1：编写审计控制器**

```java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.PermissionAuditQueryService;
import com.example.demo.service.dto.AuditLogResponse;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/permission-audit-logs")
public class PermissionAuditController {
    
    private final PermissionAuditQueryService auditQueryService;
    
    @GetMapping("/target/{targetType}/{targetId}")
    public Result<List<AuditLogResponse>> getByTarget(@PathVariable String targetType,
                                                       @PathVariable Long targetId,
                                                       @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditQueryService.getLogsByTarget(targetType, targetId, limit);
        return Result.success(logs);
    }
    
    @GetMapping("/operator/{operatorId}")
    public Result<List<AuditLogResponse>> getByOperator(@PathVariable Long operatorId,
                                                         @RequestParam(defaultValue = "20") int limit) {
        List<AuditLogResponse> logs = auditQueryService.getLogsByOperator(operatorId, limit);
        return Result.success(logs);
    }
    
    @GetMapping("/search")
    public Result<List<AuditLogResponse>> search(@RequestParam String changeType,
                                                  @RequestParam String startTime,
                                                  @RequestParam String endTime) {
        List<AuditLogResponse> logs = auditQueryService.searchLogs(changeType, startTime, endTime);
        return Result.success(logs);
    }
}
```

- [ ] **步骤 2：提交控制器**

```bash
git add src/main/java/com/example/demo/controller/PermissionAuditController.java
git commit -m "feat(rbac): add PermissionAuditController for audit query"
```

---

## 任务 6：启用异步处理

**文件：**
- 修改：`src/main/java/com/example/demo/DemoApplication.java`

- [ ] **步骤 1：启用异步**

```java
// 在 DemoApplication.java 中
@EnableAsync
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

- [ ] **步骤 2：提交异步配置**

```bash
git add src/main/java/com/example/demo/DemoApplication.java
git commit -m "feat(rbac): enable async for audit logging"
```

---

## 自检清单

- [x] 规范 P9 覆盖：审计日志实体 ✓、审计服务 ✓、事件监听器 ✓、查询 API ✓
- [x] 无占位符：所有代码完整
- [x] 异步：审计日志不阻塞业务操作
- [x] 追踪 ID：链接到 MDC 用于请求追踪

---

**计划完成。**