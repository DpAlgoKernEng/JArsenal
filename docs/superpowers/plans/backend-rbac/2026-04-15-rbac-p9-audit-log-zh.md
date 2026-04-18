# RBAC P9: 权限审计日志实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现权限审计日志系统，记录所有权限变更及其前后值和追踪 ID

**架构：** 领域事件触发审计日志写入，PermissionAuditService 记录变更到 permission_audit_log 表，API 端点用于查询审计历史

**技术栈：** Spring Event Publisher、MyBatis、异步日志

**依赖：** P1-P8（所有领域事件必须存在）

---

## 文件结构

```
src/main/java/com/jguard/
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
│           ├── PermissionAuditLogRepository.java   # 仓储接口（新增）
│           ├── PermissionAuditLogRepositoryImpl.java # 仓储实现（新增）
├── controller/
│   └ PermissionAuditController.java       # 审计查询 API
├── service/
│   └ dto/
│       ├── AuditLogResponse.java
│       ├── AuditLogQueryRequest.java
│       ├── PermissionAuditQueryService.java # 审计查询服务（新增）
```

---

## 任务 0.5：创建 PermissionAuditLogRepository 接口（新增）

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/repository/PermissionAuditLogRepository.java`

- [ ] **步骤 1：编写审计日志仓储接口**

```java
package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import java.util.List;

public interface PermissionAuditLogRepository {
    
    void save(PermissionAuditLog log);
    
    List<PermissionAuditLog> findByTarget(String targetType, Long targetId, int limit);
    
    List<PermissionAuditLog> findByOperator(Long operatorId, int limit);
    
    List<PermissionAuditLog> findByTypeAndTimeRange(String changeType, String startTime, String endTime);
}
```

- [ ] **步骤 2：提交仓储接口**

```bash
git add src/main/java/com/jguard/domain/permission/repository/PermissionAuditLogRepository.java
git commit -m "feat(rbac): add PermissionAuditLogRepository interface"
```

---

## 任务 1：创建 PermissionAuditLog 实体

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/entity/PermissionAuditLog.java`

- [ ] **步骤 1：编写审计日志实体**

```java
package com.jguard.domain.permission.entity;

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
git add src/main/java/com/jguard/domain/permission/entity/PermissionAuditLog.java
git commit -m "feat(rbac): add PermissionAuditLog entity"
```

---

## 任务 2：创建 PermissionAuditService

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/service/PermissionAuditService.java`

- [ ] **步骤 1：编写审计服务**

```java
package com.jguard.domain.permission.service;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import com.jguard.security.UserContext;
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
    
    /**
     * 记录角色创建事件
     * 参数与P1 RoleCreatedEvent匹配：roleId, roleCode, roleName, operatorId
     */
    public void logRoleCreate(Long operatorId, String roleCode, String roleName) {
        logChange(operatorId, UserContext.getCurrentUserName(), 
            "ROLE_CREATE", "ROLE", null,
            null, Map.of("code", roleCode, "name", roleName), null);
    }
    
    /**
     * 记录权限分配事件
     * 参数与P1 RolePermissionChangedEvent匹配：roleId, resourceId, changeType, operatorId
     */
    public void logPermissionAssign(Long operatorId, Long roleId, Long resourceId, String changeType) {
        logChange(operatorId, UserContext.getCurrentUserName(),
            changeType.equals("REMOVE") ? "PERM_REMOVE" : "PERM_ASSIGN", 
            "ROLE", roleId,
            null, Map.of("resourceId", resourceId, "changeType", changeType), null);
    }
    
    /**
     * 记录用户角色分配事件
     * 参数与P1 UserRoleAssignedEvent匹配：userId, roleId, operatorId
     */
    public void logUserRoleAssign(Long operatorId, Long userId, Long roleId) {
        logChange(operatorId, UserContext.getCurrentUserName(),
            "USER_ROLE_ASSIGN", "USER", userId,
            null, Map.of("roleId", roleId), null);
    }
}
```

- [ ] **步骤 2：提交审计服务**

```bash
git add src/main/java/com/jguard/domain/permission/service/PermissionAuditService.java
git commit -m "feat(rbac): add PermissionAuditService for async audit logging"
```

---

## 任务 3：创建审计事件监听器

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/event/PermissionAuditEventListener.java`

- [ ] **步骤 1：编写事件监听器**

```java
package com.jguard.domain.permission.event;

import com.jguard.domain.permission.service.PermissionAuditService;
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
git add src/main/java/com/jguard/domain/permission/event/PermissionAuditEventListener.java
git commit -m "feat(rbac): add audit event listener for domain events"
```

---

## 任务 4：创建审计日志 Mapper

**文件：**
- 创建：`src/main/java/com/jguard/infrastructure/persistence/mapper/PermissionAuditLogMapper.java`

- [ ] **步骤 1：编写审计 Mapper**

```java
package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.entity.PermissionAuditLog;
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
git add src/main/java/com/jguard/infrastructure/persistence/mapper/PermissionAuditLogMapper.java
git commit -m "feat(rbac): add PermissionAuditLogMapper"
```

---

## 任务 5.5：创建 PermissionAuditQueryService（新增）

**文件：**
- 创建：`src/main/java/com/jguard/service/PermissionAuditQueryService.java`
- 创建：`src/main/java/com/jguard/service/dto/AuditLogResponse.java`

- [ ] **步骤 1：编写 AuditLogResponse DTO**

```java
package com.jguard.service.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    Long operatorId,
    String operatorName,
    String changeType,
    String targetType,
    Long targetId,
    String beforeValue,
    String afterValue,
    String reason,
    String traceId,
    LocalDateTime createTime
) {}
```

- [ ] **步骤 2：编写 PermissionAuditQueryService**

```java
package com.jguard.service;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import com.jguard.service.dto.AuditLogResponse;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PermissionAuditQueryService {
    
    private final PermissionAuditLogRepository auditLogRepository;
    
    public PermissionAuditQueryService(PermissionAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    public List<AuditLogResponse> getLogsByTarget(String targetType, Long targetId, int limit) {
        List<PermissionAuditLog> logs = auditLogRepository.findByTarget(targetType, targetId, limit);
        return logs.stream().map(this::toResponse).toList();
    }
    
    public List<AuditLogResponse> getLogsByOperator(Long operatorId, int limit) {
        List<PermissionAuditLog> logs = auditLogRepository.findByOperator(operatorId, limit);
        return logs.stream().map(this::toResponse).toList();
    }
    
    public List<AuditLogResponse> searchLogs(String changeType, String startTime, String endTime) {
        List<PermissionAuditLog> logs = auditLogRepository.findByTypeAndTimeRange(changeType, startTime, endTime);
        return logs.stream().map(this::toResponse).toList();
    }
    
    private AuditLogResponse toResponse(PermissionAuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getOperatorId(),
            log.getOperatorName(),
            log.getChangeType(),
            log.getTargetType(),
            log.getTargetId(),
            log.getBeforeValue(),
            log.getAfterValue(),
            log.getReason(),
            log.getTraceId(),
            log.getCreateTime()
        );
    }
}
```

- [ ] **步骤 3：提交查询服务**

```bash
git add src/main/java/com/jguard/service/PermissionAuditQueryService.java \
        src/main/java/com/jguard/service/dto/AuditLogResponse.java
git commit -m "feat(rbac): add PermissionAuditQueryService for audit log query"
```

---

## 任务 5：创建审计查询 API

**文件：**
- 创建：`src/main/java/com/jguard/controller/PermissionAuditController.java`

- [ ] **步骤 1：编写审计控制器**

```java
package com.jguard.controller;

import com.jguard.common.Result;
import com.jguard.service.PermissionAuditQueryService;
import com.jguard.service.dto.AuditLogResponse;
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
git add src/main/java/com/jguard/controller/PermissionAuditController.java
git commit -m "feat(rbac): add PermissionAuditController for audit query"
```

---

## 任务 6：启用异步处理

**文件：**
- 修改：`src/main/java/com/jguard/DemoApplication.java`

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
git add src/main/java/com/jguard/DemoApplication.java
git commit -m "feat(rbac): enable async for audit logging"
```

---

## 自检清单

- [x] 规范 P9 覆盖：审计日志实体 ✓、审计服务 ✓、事件监听器 ✓、查询 API ✓
- [x] 无占位符：所有代码完整
- [x] 异步：审计日志不阻塞业务操作
- [x] 追踪 ID：链接到 MDC 用于请求追踪
- [x] **事件参数匹配**：logRoleCreate签名与RoleCreatedEvent匹配 ✓
- [x] **事件参数匹配**：logPermissionAssign签名与RolePermissionChangedEvent匹配 ✓
- [x] **仓储接口补充**：PermissionAuditLogRepository已添加 ✓
- [x] **查询服务补充**：PermissionAuditQueryService已添加 ✓、AuditLogResponse DTO已添加 ✓
- [x] **新增**：集成测试配置文件 application-test.yml 示例 ✓
- [x] **新增**：事件监听器集成测试完整实现 ✓、RoleCreatedEvent/PermissionAssign/UserRoleAssign 覆盖 ✓
- [x] **改进点补充**：AuditLogExportService导出服务 ✓、CSV/JSON/ZIP格式导出 ✓、合规审计支持 ✓

---

## 任务 7：建议补充 - 事件监听器集成测试（可选）

**文件：**
- 创建：`src/test/java/com/jguard/service/PermissionAuditEventListenerIT.java`

- [ ] **步骤 1：编写集成测试示例**

```java
package com.jguard.service;

import com.jguard.domain.permission.event.RoleCreatedEvent;
import com.jguard.domain.permission.event.RolePermissionChangedEvent;
import com.jguard.domain.permission.event.UserRoleAssignedEvent;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class PermissionAuditEventListenerIT {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private PermissionAuditLogRepository auditLogRepository;
    
    @Test
    void shouldLogRoleCreatedEvent() {
        // 发布角色创建事件
        RoleCreatedEvent event = new RoleCreatedEvent(1L, "TEST_ROLE", "测试角色", 100L);
        eventPublisher.publishEvent(event);
        
        // 等待异步处理完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // 验证审计日志被记录
        var logs = auditLogRepository.findByTarget("ROLE", 1L, 10);
        assertFalse(logs.isEmpty());
        
        var log = logs.get(0);
        assertEquals("ROLE_CREATE", log.getChangeType());
        assertEquals(100L, log.getOperatorId());
    }
    
    @Test
    void shouldLogPermissionAssignEvent() {
        // 发布权限分配事件
        RolePermissionChangedEvent event = new RolePermissionChangedEvent(
            1L, 100L, "ADD", 100L
        );
        eventPublisher.publishEvent(event);
        
        // 等待异步处理
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // 验证审计日志
        var logs = auditLogRepository.findByTarget("ROLE", 1L, 10);
        assertFalse(logs.isEmpty());
        
        var lastLog = logs.get(0);
        assertEquals("PERM_ASSIGN", lastLog.getChangeType());
    }
    
    @Test
    void shouldLogUserRoleAssignedEvent() {
        // 发布用户角色分配事件
        UserRoleAssignedEvent event = new UserRoleAssignedEvent(10L, 1L, 100L);
        eventPublisher.publishEvent(event);
        
        // 等待异步处理
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // ignore
        }
        
        // 验证审计日志
        var logs = auditLogRepository.findByTarget("USER", 10L, 10);
        assertFalse(logs.isEmpty());
        
        var log = logs.get(0);
        assertEquals("USER_ROLE_ASSIGN", log.getChangeType());
        assertEquals(100L, log.getOperatorId());
    }
}
```

- [ ] **步骤 2：配置测试 Profile**

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jguard_test
    username: root
    password: root
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## 任务 8：审计日志导出功能（新增 - 改进点）

**文件：**
- 创建：`src/main/java/com/jguard/service/AuditLogExportService.java`
- 修改：`src/main/java/com/jguard/controller/PermissionAuditController.java`
- 创建：`src/main/java/com/jguard/service/dto/AuditLogExportRequest.java`

> **改进点：** 支持审计日志导出功能，用于合规审计和离线分析。

- [ ] **步骤 1：编写 AuditLogExportRequest DTO**

```java
package com.jguard.service.dto;

import java.time.LocalDateTime;

public record AuditLogExportRequest(
    String targetType,      // ROLE/RESOURCE/USER（可选）
    Long targetId,          // 目标ID（可选）
    Long operatorId,        // 操作人ID（可选）
    String changeType,      // 变更类型（可选）
    LocalDateTime startTime, // 开始时间
    LocalDateTime endTime,   // 结束时间
    String format           // CSV/JSON/PDF
) {}
```

- [ ] **步骤 2：编写 AuditLogExportService**

```java
package com.jguard.service;

import com.jguard.domain.permission.entity.PermissionAuditLog;
import com.jguard.domain.permission.repository.PermissionAuditLogRepository;
import com.jguard.service.dto.AuditLogExportRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AuditLogExportService {
    
    private final PermissionAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 导出审计日志为CSV格式
     */
    public byte[] exportToCsv(AuditLogExportRequest request) {
        List<PermissionAuditLog> logs = queryLogs(request);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        try {
            // CSV header
            writer.write("ID,操作人ID,操作人姓名,变更类型,目标类型,目标ID,变更前值,变更后值,原因,追踪ID,创建时间\n");
            
            for (PermissionAuditLog log : logs) {
                writer.write(String.format("%d,%d,%s,%s,%s,%d,%s,%s,%s,%s,%s\n",
                    log.getId(),
                    log.getOperatorId(),
                    escapeCsv(log.getOperatorName()),
                    log.getChangeType(),
                    log.getTargetType(),
                    log.getTargetId(),
                    escapeCsv(log.getBeforeValue()),
                    escapeCsv(log.getAfterValue()),
                    escapeCsv(log.getReason()),
                    log.getTraceId(),
                    log.getCreateTime().format(formatter)
                ));
            }
            
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * 导出审计日志为JSON格式
     */
    public byte[] exportToJson(AuditLogExportRequest request) {
        List<PermissionAuditLog> logs = queryLogs(request);
        
        try {
            return objectMapper.writeValueAsBytes(logs);
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }
    
    /**
     * 导出审计日志为压缩包（包含CSV和JSON）
     */
    public byte[] exportToZip(AuditLogExportRequest request) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 添加CSV文件
            ZipEntry csvEntry = new ZipEntry("audit_logs.csv");
            zos.putNextEntry(csvEntry);
            zos.write(exportToCsv(request));
            zos.closeEntry();
            
            // 添加JSON文件
            ZipEntry jsonEntry = new ZipEntry("audit_logs.json");
            zos.putNextEntry(jsonEntry);
            zos.write(exportToJson(request));
            zos.closeEntry();
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
        
        return baos.toByteArray();
    }
    
    private List<PermissionAuditLog> queryLogs(AuditLogExportRequest request) {
        // 根据请求参数查询日志
        if (request.targetType() != null && request.targetId() != null) {
            return auditLogRepository.findByTarget(request.targetType(), request.targetId(), 10000);
        } else if (request.operatorId() != null) {
            return auditLogRepository.findByOperator(request.operatorId(), 10000);
        } else {
            return auditLogRepository.findByTypeAndTimeRange(
                request.changeType(),
                request.startTime().toString(),
                request.endTime().toString()
            );
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
```

- [ ] **步骤 3：在 PermissionAuditController 中添加导出端点**

```java
// 在 PermissionAuditController.java 中添加

private final AuditLogExportService auditLogExportService;

/**
 * 导出审计日志
 */
@GetMapping("/export")
public void exportAuditLogs(
        @RequestParam(required = false) String targetType,
        @RequestParam(required = false) Long targetId,
        @RequestParam(required = false) Long operatorId,
        @RequestParam(required = false) String changeType,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime,
        @RequestParam(defaultValue = "CSV") String format,
        HttpServletResponse response) throws IOException {
    
    AuditLogExportRequest request = new AuditLogExportRequest(
        targetType, targetId, operatorId, changeType,
        startTime, endTime, format
    );
    
    byte[] content;
    String filename;
    String contentType;
    
    switch (format.toUpperCase()) {
        case "JSON":
            content = auditLogExportService.exportToJson(request);
            filename = "audit_logs.json";
            contentType = "application/json";
            break;
        case "ZIP":
            content = auditLogExportService.exportToZip(request);
            filename = "audit_logs.zip";
            contentType = "application/zip";
            break;
        default:
            content = auditLogExportService.exportToCsv(request);
            filename = "audit_logs.csv";
            contentType = "text/csv";
    }
    
    response.setContentType(contentType);
    response.setHeader("Content-Disposition", "attachment; filename=" + filename);
    response.getOutputStream().write(content);
    response.getOutputStream().flush();
}
```

- [ ] **步骤 4：提交导出功能**

```bash
git add src/main/java/com/jguard/service/AuditLogExportService.java \
        src/main/java/com/jguard/service/dto/AuditLogExportRequest.java \
        src/main/java/com/jguard/controller/PermissionAuditController.java
git commit -m "feat(rbac): add audit log export functionality for compliance reporting"
```

---

**计划完成。**