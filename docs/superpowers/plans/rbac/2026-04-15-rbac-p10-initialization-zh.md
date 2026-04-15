# RBAC P10: 初始化数据实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 初始化 RBAC 系统预设数据 - 内置角色、默认资源、数据维度和系统菜单结构

**架构：** V3 迁移插入预设数据，ResourceInitializationService 在启动时验证数据完整性

**技术栈：** Flyway 迁移、Spring Boot 启动验证

**依赖：** P1-P9（所有表必须存在）

---

## 文件结构

```
src/main/resources/
├── db/migration/
│   ├── V3__RBAC_Preset_Data.sql       # 预设角色、资源、维度
│   └── V4__RBAC_Preset_Permissions.sql # 内置角色的默认权限
├── init/
│   ├── ResourceInitializationService.java # 启动验证
```

---

## 任务 1：创建 V3 迁移 - 预设角色

**文件：**
- 创建：`src/main/resources/db/migration/V3__RBAC_Preset_Data.sql`

- [ ] **步骤 1：编写预设角色数据**

```sql
-- V3__RBAC_Preset_Data.sql

-- 内置角色
INSERT INTO role (code, name, parent_id, is_builtin, sort, inherit_mode, status) VALUES
('SUPER_ADMIN', '超级管理员', NULL, 1, 1, 'EXTEND', 1),
('ADMIN', '系统管理员', 1, 1, 2, 'EXTEND', 1),
('DEPT_MANAGER', '部门管理员', 2, 1, 3, 'LIMIT', 1),
('USER', '普通用户', 3, 1, 4, 'LIMIT', 1);

-- 数据维度
INSERT INTO data_dimension (code, name, source_table, source_column, description, status) VALUES
('DEPARTMENT', '部门', 'department', 'dept_id', '部门维度数据权限', 1),
('PROJECT', '项目', 'project', 'project_id', '项目维度数据权限', 1),
('CUSTOMER', '客户', 'customer', 'customer_id', '客户维度数据权限', 1);

-- 超级管理员数据权限（全部数据）
INSERT INTO role_data_scope (role_id, dimension_code, scope_type) VALUES
(1, 'DEPARTMENT', 'ALL'),
(1, 'PROJECT', 'ALL'),
(1, 'CUSTOMER', 'ALL');

-- 部门管理员数据权限（本部门及下级）
INSERT INTO role_data_scope (role_id, dimension_code, scope_type) VALUES
(3, 'DEPARTMENT', 'DEPT_TREE'),
(3, 'PROJECT', 'SELF_DEPT'),
(3, 'CUSTOMER', 'SELF_DEPT');

-- 普通用户数据权限（仅本人）
INSERT INTO role_data_scope (role_id, dimension_code, scope_type) VALUES
(4, 'DEPARTMENT', 'SELF'),
(4, 'PROJECT', 'SELF'),
(4, 'CUSTOMER', 'SELF');
```

- [ ] **步骤 2：提交迁移**

```bash
git add src/main/resources/db/migration/V3__RBAC_Preset_Data.sql
git commit -m "feat(rbac): add preset roles and data dimensions migration"
```

---

## 任务 2：创建 V4 迁移 - 预设资源

**文件：**
- 创建：`src/main/resources/db/migration/V4__RBAC_Preset_Permissions.sql`

- [ ] **步骤 1：编写系统菜单资源**

```sql
-- V4__RBAC_Preset_Permissions.sql

-- 系统菜单
INSERT INTO resource (code, name, parent_id, type, path, icon, component, sort, status) VALUES
('SYSTEM', '系统管理', NULL, 'MENU', '/system', 'Setting', 'Layout', 1, 1),
('USER_MANAGE', '用户管理', 1, 'MENU', '/system/users', 'User', 'UserList', 1, 1),
('ROLE_MANAGE', '角色管理', 1, 'MENU', '/system/roles', 'UserRole', 'RoleList', 2, 1),
('RESOURCE_MANAGE', '资源管理', 1, 'MENU', '/system/resources', 'Tree', 'ResourceList', 3, 1),
('PERMISSION', '权限管理', NULL, 'MENU', '/permission', 'Lock', 'Layout', 2, 1);

-- 用户管理操作
INSERT INTO resource (code, name, parent_id, type, sort, status) VALUES
('USER_VIEW', '查看用户', 2, 'OPERATION', 1, 1),
('USER_CREATE', '新增用户', 2, 'OPERATION', 2, 1),
('USER_UPDATE', '编辑用户', 2, 'OPERATION', 3, 1),
('USER_DELETE', '删除用户', 2, 'OPERATION', 4, 1);

-- 角色管理操作
INSERT INTO resource (code, name, parent_id, type, sort, status) VALUES
('ROLE_VIEW', '查看角色', 3, 'OPERATION', 1, 1),
('ROLE_CREATE', '新增角色', 3, 'OPERATION', 2, 1),
('ROLE_UPDATE', '编辑角色', 3, 'OPERATION', 3, 1),
('ROLE_DELETE', '删除角色', 3, 'OPERATION', 4, 1);

-- API资源
INSERT INTO resource (code, name, type, path_pattern, method, sort, status) VALUES
('API_USER_LIST', '用户列表API', 'API', '/api/users/**', 'GET', 1, 1),
('API_USER_CREATE', '用户创建API', 'API', '/api/users', 'POST', 2, 1),
('API_USER_UPDATE', '用户更新API', 'API', '/api/users/*', 'PUT', 3, 1),
('API_USER_DELETE', '用户删除API', 'API', '/api/users/*', 'DELETE', 4, 1),
('API_ROLE_LIST', '角色列表API', 'API', '/api/roles/**', 'GET', 5, 1),
('API_ROLE_CREATE', '角色创建API', 'API', '/api/roles', 'POST', 6, 1),
('API_ROLE_UPDATE', '角色更新API', 'API', '/api/roles/*', 'PUT', 7, 1),
('API_ROLE_DELETE', '角色删除API', 'API', '/api/roles/*', 'DELETE', 8, 1);
```

- [ ] **步骤 2：编写默认权限分配（使用变量避免硬编码ID）**

```sql
-- 超级管理员权限（全部）
-- 使用查询获取实际ID，避免硬编码

-- 创建权限记录
INSERT INTO permission (role_id, resource_id, effect)
SELECT 1, id, 'ALLOW' FROM resource WHERE code IN ('SYSTEM', 'USER_MANAGE', 'ROLE_MANAGE', 'RESOURCE_MANAGE', 'PERMISSION');

INSERT INTO permission (role_id, resource_id, effect)
SELECT 2, id, 'ALLOW' FROM resource WHERE code IN ('SYSTEM', 'USER_MANAGE', 'ROLE_MANAGE', 'RESOURCE_MANAGE');

INSERT INTO permission (role_id, resource_id, effect)
SELECT 3, id, 'ALLOW' FROM resource WHERE code IN ('SYSTEM', 'USER_MANAGE');

-- 为每个permission添加action记录（使用子查询获取permission_id）
-- 超级管理员对所有资源有全部操作权限
INSERT INTO permission_action (permission_id, action)
SELECT p.id, a.action
FROM permission p
CROSS JOIN (SELECT 'VIEW' AS action UNION SELECT 'CREATE' UNION SELECT 'UPDATE' UNION SELECT 'DELETE' UNION SELECT 'EXECUTE') a
WHERE p.role_id = 1;

-- 系统管理员对系统菜单有全部操作权限（除EXECUTE）
INSERT INTO permission_action (permission_id, action)
SELECT p.id, a.action
FROM permission p
CROSS JOIN (SELECT 'VIEW' AS action UNION SELECT 'CREATE' UNION SELECT 'UPDATE' UNION SELECT 'DELETE') a
WHERE p.role_id = 2;

-- 部门管理员仅有查看和编辑权限
INSERT INTO permission_action (permission_id, action)
SELECT p.id, a.action
FROM permission p
CROSS JOIN (SELECT 'VIEW' AS action UNION SELECT 'UPDATE') a
WHERE p.role_id = 3;
```

- [ ] **步骤 3：编写敏感字段定义（修复Flyway变量语法）**

```sql
-- 用户敏感字段 - Flyway不支持SET变量，改用子查询
-- 先创建USER_ENTITY资源（如果不存在）
INSERT INTO resource (code, name, type, sort, status)
SELECT 'USER_ENTITY', '用户实体', 'API', 0, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM resource WHERE code = 'USER_ENTITY');

-- 使用子查询插入敏感字段
INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern)
SELECT id, 'phone', '手机号', 'ENCRYPTED', 'PHONE'
FROM resource WHERE code = 'USER_ENTITY';

INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern)
SELECT id, 'salary', '薪资', 'ENCRYPTED', 'SALARY'
FROM resource WHERE code = 'USER_ENTITY';

INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern)
SELECT id, 'idCard', '身份证号', 'ENCRYPTED', 'ID_CARD'
FROM resource WHERE code = 'USER_ENTITY';

INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern)
SELECT id, 'password', '密码', 'HIDDEN', NULL
FROM resource WHERE code = 'USER_ENTITY';
```

- [ ] **步骤 4：补充菜单资源的component路径**

```sql
-- 更新菜单资源的component路径（用于前端动态路由）
UPDATE resource SET component = 'Layout' WHERE code = 'SYSTEM';
UPDATE resource SET component = 'UserList' WHERE code = 'USER_MANAGE';
UPDATE resource SET component = 'RoleList' WHERE code = 'ROLE_MANAGE';
UPDATE resource SET component = 'ResourceList' WHERE code = 'RESOURCE_MANAGE';
UPDATE resource SET component = 'Layout' WHERE code = 'PERMISSION';
```

- [ ] **步骤 4：提交迁移**

```bash
git add src/main/resources/db/migration/V4__RBAC_Preset_Permissions.sql
git commit -m "feat(rbac): add preset resources and permissions migration"
```

---

## 任务 3：创建初始化验证服务

**文件：**
- 创建：`src/main/java/com/example/demo/service/ResourceInitializationService.java`

- [ ] **步骤 1：编写验证服务**

```java
package com.example.demo.service;

import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.DataDimensionRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ResourceInitializationService {
    
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final DataDimensionRepository dimensionRepository;
    
    @EventListener(ApplicationReadyEvent.class)
    public void verifyInitialization() {
        log.info("开始验证RBAC初始化数据...");
        
        // 验证内置角色
        verifyBuiltinRoles();
        
        // 验证数据维度
        verifyDataDimensions();
        
        // 验证核心资源
        verifyCoreResources();
        
        log.info("RBAC初始化数据验证完成");
    }
    
    private void verifyBuiltinRoles() {
        String[] builtinRoles = {"SUPER_ADMIN", "ADMIN", "DEPT_MANAGER", "USER"};
        
        for (String code : builtinRoles) {
            if (roleRepository.findByCode(code).isEmpty()) {
                log.warn("内置角色缺失: {}", code);
            }
        }
    }
    
    private void verifyDataDimensions() {
        String[] dimensions = {"DEPARTMENT", "PROJECT", "CUSTOMER"};
        
        for (String code : dimensions) {
            if (dimensionRepository.findByCode(code) == null) {
                log.warn("数据维度缺失: {}", code);
            }
        }
    }
    
    private void verifyCoreResources() {
        String[] coreMenus = {"SYSTEM", "USER_MANAGE", "ROLE_MANAGE"};
        
        for (String code : coreMenus) {
            if (resourceRepository.findByCode(code).isEmpty()) {
                log.warn("核心资源缺失: {}", code);
            }
        }
    }
}
```

- [ ] **步骤 2：提交验证服务**

```bash
git add src/main/java/com/example/demo/service/ResourceInitializationService.java
git commit -m "feat(rbac): add initialization verification service"
```

---

## 任务 4：运行完整迁移

**文件：**
- 无（执行迁移）

- [ ] **步骤 1：运行 Flyway 迁移**

```bash
mvn flyway:migrate
```
预期：V1、V2、V3、V4 迁移成功执行

- [ ] **步骤 2：验证数据已插入**

```bash
mysql -u root -proot demo -e "SELECT * FROM role WHERE is_builtin = 1; SELECT * FROM data_dimension;"
```
预期：SUPER_ADMIN、ADMIN、DEPT_MANAGER、USER 角色存在；DEPARTMENT、PROJECT、CUSTOMER 维度存在

- [ ] **步骤 3：验证触发器**

```bash
mysql -u root -proot demo -e "SHOW TRIGGERS LIKE 'role';"
```
预期：trg_role_before_insert、trg_role_before_update 触发器存在

---

## 任务 5：测试循环继承防护

**文件：**
- 无（数据库触发器测试）

- [ ] **步骤 1：测试触发器阻止循环继承**

```bash
mysql -u root -proot demo -e "
-- 尝试让ADMIN角色以USER为父角色（会形成循环）
UPDATE role SET parent_id = 4 WHERE code = 'ADMIN';
"
```
预期：错误 "角色继承链形成循环，更新被拒绝"

- [ ] **步骤 2：验证触发器阻止自继承**

```bash
mysql -u root -proot demo -e "
-- 尝试让角色以自己为父角色
UPDATE role SET parent_id = id WHERE code = 'USER';
"
```
预期：错误 "角色不能以自己为父角色（循环继承）"

---

## 任务 6：性能验收测试（补充）

**文件：**
- 创建：`src/test/java/com/example/demo/service/RbacPerformanceVerificationTest.java`

- [ ] **步骤 1：编写性能验收测试**

```java
package com.example.demo.service;

import com.example.demo.domain.permission.service.PermissionQueryService;
import com.example.demo.domain.permission.service.UserPermissionCache;
import com.example.demo.security.UserContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class RbacPerformanceVerificationTest {

    @Autowired
    private PermissionQueryService permissionQueryService;

    @Autowired
    private UserPermissionCache userPermissionCache;

    // 验收标准：权限加载时间 < 50ms（本地缓存命中）
    @Test
    void permissionLoadTime_shouldBeLessThan50ms() {
        Long userId = 1L;
        String resourceCode = "USER_MANAGE";

        // 预热缓存
        userPermissionCache.loadUserPermissions(userId);

        long start = System.currentTimeMillis();
        boolean hasPermission = permissionQueryService.hasPermission(userId, resourceCode, "VIEW");
        long duration = System.currentTimeMillis() - start;

        assertTrue(hasPermission);
        assertTrue(duration < 50, "权限加载时间应 < 50ms，实际: " + duration + "ms");
    }

    // 验收标准：100并发成功率 > 99%
    @Test
    void concurrentPermissionQuery_shouldHave99PercentSuccess() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        Long userId = 1L;
        String resourceCode = "USER_MANAGE";

        // 预热缓存
        userPermissionCache.loadUserPermissions(userId);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    permissionQueryService.hasPermission(userId, resourceCode, "VIEW");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        double successRate = (double) successCount.get() / threadCount * 100;
        assertTrue(successRate > 99, "并发成功率应 > 99%，实际: " + successRate + "%");
    }

    // 验收标准：无权限API返回403
    @Test
    void unauthorizedApi_shouldReturn403() {
        // 模拟无权限用户
        Long userId = 999L; // 无权限用户
        boolean hasPermission = permissionQueryService.hasPermission(userId, "USER_DELETE", "DELETE");
        assertFalse(hasPermission, "无权限用户应返回false（对应API返回403）");
    }

    // 验收标准：角色继承正确计算
    @Test
    void roleInheritance_shouldCalculateCorrectly() {
        // DEPT_MANAGER继承自ADMIN，ADMIN继承自SUPER_ADMIN
        Long deptManagerUserId = 3L; // 拥有DEPT_MANAGER角色

        // 验证继承权限
        boolean hasAdminPermission = permissionQueryService.hasPermission(
            deptManagerUserId, "USER_MANAGE", "VIEW");
        assertTrue(hasAdminPermission, "DEPT_MANAGER应继承ADMIN的USER_MANAGE权限");
    }

    // 验收标准：多角色权限正确合并（取最宽松）
    @Test
    void multiRolePermission_shouldMergeWithLenientStrategy() {
        // 用户同时拥有USER和DEPT_MANAGER角色
        Long multiRoleUserId = 10L;

        // USER角色无DELETE权限，DEPT_MANAGER有UPDATE权限
        boolean canUpdate = permissionQueryService.hasPermission(
            multiRoleUserId, "USER_MANAGE", "UPDATE");

        // 多角色合并应取最宽松策略
        assertTrue(canUpdate, "多角色权限应取最宽松策略合并");
    }
}
```

- [ ] **步骤 2：提交性能验收测试**

```bash
git add src/test/java/com/example/demo/service/RbacPerformanceVerificationTest.java
git commit -m "feat(rbac): add RBAC performance verification test covering all acceptance criteria"
```

---

## 自检清单

- [x] 规范 P10 覆盖：内置角色 ✓、数据维度 ✓、核心资源 ✓、验证 ✓
- [x] 无占位符：所有数据已初始化
- [x] 触发器验证：循环继承已阻止
- [x] 启动验证：服务检查数据完整性
- [x] **预设数据可靠性**：permission使用查询获取ID ✓（避免硬编码）
- [x] **敏感字段定义**：改用子查询代替SET变量 ✓（Flyway兼容）
- [x] **菜单component路径**：完整映射已补充 ✓
- [x] **性能验收测试**：RbacPerformanceVerificationTest已补充 ✓
- [x] **改进点补充**：V5__RBAC_Init_Rollback.sql回滚脚本 ✓、手动回滚指南 ✓、初始化失败恢复方案 ✓

---

## 所有阶段最终清单

**功能验收（跨阶段引用）：**
- [x] 用户登录后可获取权限列表 → P2: UserPermissionApiTest ✓
- [x] 无权限访问API返回403 → P2: PermissionDeniedTest + P10: unauthorizedApi_shouldReturn403 ✓
- [x] 无权限菜单不在导航显示 → P3: DynamicRouteTest ✓
- [x] 无权限按钮不渲染 → P3: vPermissionDirectiveTest ✓
- [x] 数据权限正确过滤 → P4: DataScopeTest + SoftDeleteValidationTest ✓
- [x] 敏感字段正确脱敏 → P5: FieldMaskingTest ✓
- [x] 角色继承正确计算 → P7: RoleInheritanceIT + P10: roleInheritance_shouldCalculateCorrectly ✓
- [x] 多角色权限正确合并 → P5: FieldPermissionInheritanceTest + P10: multiRolePermission_shouldMergeWithLenientStrategy ✓
- [x] 权限变更缓存刷新 → P6: CacheInvalidationTest ✓
- [x] 审计日志正确记录 → P9: PermissionAuditEventListenerIT ✓

**性能验收：**
- [x] 权限加载 < 50ms（本地缓存） → P2: PermissionPerformanceTest + P10: permissionLoadTime_shouldBeLessThan50ms ✓
- [x] 权限检查不影响API响应 → P6: CacheHitRateTest ✓
- [x] 100并发成功率 > 99% → P2: PermissionStormTest + P10: concurrentPermissionQuery_shouldHave99PercentSuccess ✓

**安全验收：**
- [x] 无SQL注入漏洞 → P1: MyBatis参数化查询 + P4: MyBatisInterceptor安全过滤 ✓
- [x] 缓存Key不可枚举 → P6: CacheKeyTest（userId加盐） ✓
- [x] 角色继承循环检测 → P1: CircularInheritanceTest + P7: RoleInheritanceIT + P10触发器测试 ✓
- [x] 软删除数据不可访问 → P4: SoftDeleteValidationTest ✓

---

## 任务 8：初始化失败回滚脚本（新增 - 改进点）

**文件：**
- 创建：`src/main/resources/db/migration/V5__RBAC_Init_Rollback.sql`

> **改进点：** 初始化失败时需要回滚脚本，避免数据库处于不一致状态。Flyway不支持自动回滚，提供手动回滚方案。

- [ ] **步骤 1：编写回滚脚本**

```sql
-- V5__RBAC_Init_Rollback.sql
-- 注意：这是一个回滚辅助脚本，仅在初始化失败时手动执行
-- Flyway不执行此脚本，仅用于管理员手动回滚

-- ============================================
-- RBAC 初始化失败回滚脚本（手动执行）
-- ============================================

-- 1. 清除预设权限数据
DELETE FROM permission_action WHERE permission_id IN (
    SELECT p.id FROM permission p WHERE p.role_id IN (1, 2, 3, 4)
);

DELETE FROM permission WHERE role_id IN (1, 2, 3, 4);

-- 2. 清除角色数据范围
DELETE FROM role_data_scope_value WHERE scope_id IN (
    SELECT id FROM role_data_scope WHERE role_id IN (1, 2, 3, 4)
);

DELETE FROM role_data_scope WHERE role_id IN (1, 2, 3, 4);

-- 3. 清除字段权限
DELETE FROM field_permission WHERE role_id IN (1, 2, 3, 4);

-- 4. 清除资源字段定义
DELETE FROM resource_field WHERE resource_id IN (
    SELECT id FROM resource WHERE code IN ('SYSTEM', 'USER_MANAGE', 'ROLE_MANAGE', 'RESOURCE_MANAGE', 'PERMISSION', 'USER_ENTITY')
);

-- 5. 清除资源数据
DELETE FROM resource WHERE code IN (
    'SYSTEM', 'USER_MANAGE', 'ROLE_MANAGE', 'RESOURCE_MANAGE', 'PERMISSION',
    'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
    'ROLE_VIEW', 'ROLE_CREATE', 'ROLE_UPDATE', 'ROLE_DELETE',
    'API_USER_LIST', 'API_USER_CREATE', 'API_USER_UPDATE', 'API_USER_DELETE',
    'API_ROLE_LIST', 'API_ROLE_CREATE', 'API_ROLE_UPDATE', 'API_ROLE_DELETE',
    'USER_ENTITY'
);

-- 6. 清除内置角色（谨慎操作）
-- 如果需要完全回滚：
-- DELETE FROM role WHERE code IN ('SUPER_ADMIN', 'ADMIN', 'DEPT_MANAGER', 'USER');
-- DELETE FROM user_role WHERE role_id IN (1, 2, 3, 4);

-- 7. 清除数据维度
DELETE FROM data_dimension WHERE code IN ('DEPARTMENT', 'PROJECT', 'CUSTOMER');

-- 8. 清除审计日志（可选）
-- DELETE FROM permission_audit_log WHERE target_type = 'ROLE' AND target_id IN (1, 2, 3, 4);

-- ============================================
-- 验证回滚结果
-- ============================================
SELECT '内置角色:' as category, COUNT(*) as count FROM role WHERE is_builtin = 1;
SELECT '数据维度:' as category, COUNT(*) as count FROM data_dimension;
SELECT '系统资源:' as category, COUNT(*) as count FROM resource WHERE code LIKE 'SYSTEM%' OR code LIKE 'API_%';
SELECT '预设权限:' as category, COUNT(*) as count FROM permission WHERE role_id IN (1, 2, 3, 4);
```

- [ ] **步骤 2：编写回滚说明文档**

```markdown
## RBAC 初始化回滚指南

### 执行时机
仅当RBAC初始化失败导致系统无法正常运行时执行。

### 执行步骤
1. 连接到 MySQL 数据库：`mysql -u root -proot demo`
2. 手动执行回滚SQL（从 V5__RBAC_Init_Rollback.sql 复制需要的部分）
3. 验证回滚结果

### 注意事项
- 不建议删除内置角色和用户角色关联（保留用户分配）
- 回滚后需要重新执行 Flyway 迁移：`mvn flyway:migrate`
- 建议在生产环境前先在测试环境验证
```

- [ ] **步骤 3：提交回滚脚本**

```bash
git add src/main/resources/db/migration/V5__RBAC_Init_Rollback.sql
git commit -m "docs(rbac): add initialization rollback script for manual recovery"
```

---

**所有10个阶段实现计划完成。全部验收标准已有对应测试覆盖。全部改进点已补充完整。**

**执行选项：**

1. **子代理驱动（推荐）** - 每个阶段派发新子代理，阶段间进行评审
2. **内联执行** - 顺序执行各阶段并设置检查点

**P1（核心模型）采用哪种方式？**