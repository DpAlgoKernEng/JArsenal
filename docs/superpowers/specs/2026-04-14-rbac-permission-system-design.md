# RBAC 权限系统实现设计文档

## 文档信息

- **日期**: 2026-04-14
- **状态**: 已批准
- **参考文档**: docs/RBAC_PERMISSION_PLAN.md
- **工期**: 13 天（7 个阶段）

---

## 1. 架构概览与分层设计

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                                   │
│  Vue 3 + Element Plus                                                │
│  - 菜单动态渲染（基于用户权限）                                        │
│  - 按钮 v-permission 指令控制                                         │
│  - 角色管理、权限管理、部门管理页面                                    │
└─────────────────────────────────────────────────────────────────────┘
                                    │ HTTP/REST
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     Interfaces Layer                                 │
│  Controller + Request/Response DTO                                  │
│  - @RequirePermission, @RequireRole, @DataScope 注解                 │
│  - RoleController, PermissionController, DepartmentController       │
│  - UserPermissionController (用户角色分配)                           │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Application Layer                                 │
│  ApplicationService + Command + DTO                                 │
│  - PermissionApplicationService                                     │
│  - RoleApplicationService                                           │
│  - DepartmentApplicationService                                     │
│  - UserPermissionApplicationService                                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Domain Layer                                    │
│  聚合根 + 值对象 + 领域服务 + 仓储接口                                │
│  Aggregate: Permission, Role, Department                            │
│  ValueObject: PermissionCode, RoleCode, DeptPath                    │
│  DomainService: PermissionChecker (权限继承检查)                     │
│  Repository Interface: PermissionRepository, RoleRepository         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                               │
│  仓储实现 + Redis缓存 + 权限切面                                     │
│  - PermissionRepositoryImpl, RoleRepositoryImpl                     │
│  - RedisPermissionCache (继承权限缓存 + 版本号机制)                   │
│  - PermissionAspect (混合模式权限检查)                               │
│  - DataScopeAspect (部门级数据权限SQL改写)                           │
│  - AuditLogAspect (审计日志记录)                                     │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      MySQL + Redis                                   │
│  MySQL: role, permission, user_role, role_permission,               │
│         menu, department, user_department, audit_log                │
│  Redis: 权限缓存、版本号、Pub/Sub广播                                │
└─────────────────────────────────────────────────────────────────────┘
```

### 核心设计决策

| 决策项 | 方案 | 说明 |
|--------|------|------|
| **权限检查模式** | 混合模式 | 关键权限实时 Redis 查询，普通权限 JWT 版本号校验 |
| **权限继承** | 父权限继承子权限 | 用户拥有 `user` 权限自动拥有 `user:list/create/delete` 等 |
| **数据权限** | 四级隔离 | SELF/DEPT/DEPT_AND_SUB/ALL，MyBatis拦截器改写SQL |
| **缓存一致性** | 版本号 + Pub/Sub | 权限变更时更新版本号并广播清除缓存 |
| **Token 结构** | roles + perm_version | JWT 仅携带角色和权限版本号，不携带权限列表 |

---

## 2. 数据库设计

### 核心表结构（8张表）

```
┌─────────────── RBAC 核心 ────────────────┐
│                                           │
│  ┌─────────┐      ┌─────────┐            │
│  │  role   │◀────▶│permission│            │
│  │(角色表) │ N:M  │(权限表) │            │
│  └────┬────┘      └────┬────┘            │
│       │                │                  │
│       │ N:M            │ N:M              │
│       ▼                ▼                  │
│  ┌─────────┐      ┌─────────┐            │
│  │user_role│      │role_perm│            │
│  │(用户角色)│      │(角色权限)│            │
│  └────┬────┘      └────┬────┘            │
│       │                                  │
│       │ 关联 user 表                      │
│       ▼                                  │
│  ┌─────────┐                             │
│  │  user   │ (已存在)                     │
│  └────┬────┘                             │
│                                           │
└───────────────────────────────────────────┘

┌─────────────── 部门架构 ────────────────┐
│                                           │
│  ┌───────────┐      ┌───────────┐        │
│  │department │◀────▶│user_dept  │        │
│  │ (部门表)  │      │(用户部门) │        │
│  │ parent_id │      │ is_primary│        │
│  │ path字段  │      └───────────┘        │
│  └───────────┘                           │
│                                           │
└───────────────────────────────────────────┘

┌─────────────── 前端菜单 ────────────────┐
│                                           │
│  ┌─────────┐                             │
│  │  menu   │ (菜单表，树形结构)            │
│  │parent_id│                             │
│  │ path    │                             │
│  │component│                             │
│  │permission│ (关联权限编码)              │
│  └─────────┘                             │
│                                           │
└───────────────────────────────────────────┘

┌─────────────── 审计日志 ────────────────┐
│                                           │
│  ┌─────────┐                             │
│  │audit_log│                             │
│  │operation│ (ASSIGN_ROLE/GRANT_PERM等)  │
│  │before   │                             │
│  │after    │                             │
│  └─────────┘                             │
│                                           │
└───────────────────────────────────────────┘
```

### 关键字段设计

| 表名 | 关键字段 | 设计要点 |
|------|---------|---------|
| **role** | `code`, `is_system`, `status` | 系统内置角色不可删除，code 唯一 |
| **permission** | `code`, `parent_id`, `permission_type`, `resource` | 权限树结构，支持 menu/api/button 三种类型 |
| **department** | `parent_id`, `level`, `path` | path 字段如 `/1/5/12` 用于快速查询子部门 |
| **menu** | `parent_id`, `permission`, `menu_type` | permission 字段关联权限编码，控制菜单可见性 |
| **audit_log** | `operation`, `before_value`, `after_value` | JSON 格式记录操作前后对比 |

### 预置数据

**角色（4个系统内置）**:
- `ROLE_SUPER_ADMIN` → 所有权限
- `ROLE_ADMIN` → 用户+角色+权限管理
- `ROLE_USER_MANAGER` → 用户管理（不含删除）
- `ROLE_USER` → 基础查看权限

**权限树（约21个）**:
```
user (父权限)
├── user:list/view/create/update/delete
├── user:enable/disable/reset-password
└── user:assign-role
role (父权限)
├── role:list/view/create/update/delete
└── role:assign-permission
permission (父权限)
└── permission:list
audit (父权限)
└── audit:log
```

**部门（测试数据）**:
```
总公司 (id=1, path=/1)
├── 研发部 (id=2, path=/1/2)
│   ├── 研发一组 (id=3, path=/1/2/3)
│   └── 研发二组 (id=4, path=/1/2/4)
├── 市场部 (id=5, path=/1/5)
└── 财务部 (id=6, path=/1/6)
```

---

## 3. Domain 层设计

### 聚合根定义

**Permission (权限聚合根)**:
- 属性: PermissionId, PermissionCode, PermissionName, PermissionType, PermissionId(parentId), Resource, HttpMethod, PermissionStatus
- 行为: isParentOf(), isChildOf(), enable(), disable(), assignToRole()

**Role (角色聚合根)**:
- 属性: RoleId, RoleCode, RoleName, RoleDescription, IsSystem, RoleStatus
- 行为: assignPermission(), revokePermission(), assignToUser(), canDelete()

**Department (部门聚合根)**:
- 属性: DeptId, DeptName, DeptId(parentId), DeptLevel, DeptPath, UserId(leaderId)
- 行为: isParentOf(), containsUser(), getAllDescendants()

### 值对象定义

| 值对象 | 验证规则 | 用途 |
|--------|---------|------|
| **PermissionCode** | 正则 `^[a-z]+:[a-z-]+$` | 权限编码格式校验 |
| **RoleCode** | 正则 `^ROLE_[A-Z_]+$` | 角色编码格式校验 |
| **DeptPath** | 正则 `^/\d+(/\d+)*$` | 部门路径格式校验 |
| **PermissionType** | 枚举 API/MENU/BUTTON | 权限类型分类 |
| **DataScopeType** | 枚举 SELF/DEPT/DEPT_AND_SUB/ALL | 数据权限范围 |

### 领域服务：PermissionChecker

核心职责：权限继承检查逻辑

```java
public boolean hasPermission(UserId userId, PermissionCode permCode) {
    Set<PermissionCode> effectivePerms = getEffectivePermissions(userId);
    return effectivePerms.contains(permCode);
}

public Set<PermissionCode> computeEffectivePermissions(Set<PermissionCode> directPerms) {
    Set<PermissionCode> allPerms = new HashSet<>(directPerms);
    for (PermissionCode code : directPerms) {
        Permission perm = permissionRepository.findByCode(code);
        if (perm != null) {
            allPerms.addAll(findAllDescendants(perm.getId()));
        }
    }
    return allPerms;
}

public boolean isCriticalPermission(PermissionCode code) {
    return code.matches("user:enable", "user:disable", 
                       "user:delete", "role:delete",
                       "role:assign-permission", "user:assign-role");
}
```

### 领域事件

| 事件 | 触发时机 | 处理逻辑 |
|------|---------|---------|
| **PermissionGranted** | 给角色分配权限 | 更新 Redis 缓存、记录审计日志 |
| **PermissionRevoked** | 撤销角色权限 | 清除 Redis 缓存、记录审计日志 |
| **RoleAssigned** | 给用户分配角色 | 更新用户权限版本号、记录审计日志 |
| **RoleRevoked** | 撤销用户角色 | 清除权限缓存、记录审计日志 |

### 仓储接口

- `PermissionRepository`: findByCode(), findByParentId(), findPermissionCodesByUserId(), findSubPermissionCodes(), save()
- `RoleRepository`: findById(), findByCode(), findRoleCodesByUserId(), save()
- `DepartmentRepository`: findById(), findByParentId(), findDescendantsByPath(), findDeptIdsByUserId()

---

## 4. Infrastructure 层设计

### 权限缓存设计 (RedisPermissionCache)

**缓存 Key 设计**:
- `auth:user:roles:{userId}` → Set<RoleCode>
- `auth:user:direct_perms:{userId}` → Set<PermissionCode>
- `auth:user:effective_perms:{userId}` → Set<PermissionCode> (含继承)
- `auth:user:perm_version:{userId}` → Long (版本号)
- `auth:user:dept_ids:{userId}` → Set<DeptId>
- `auth:critical_permissions` → Set<PermissionCode>

**TTL**: 2 小时

**缓存更新策略**:
1. 更新版本号 (INCREMENT)
2. 清除缓存 (DELETE keys)
3. Pub/Sub 广播发布消息 `"userId:newVersion"`

### 权限检查切面 (PermissionAspect)

**混合模式逻辑**:

```java
@Around("@annotation(requirePermission)")
public Object checkPermission(ProceedingJoinPoint jp, RequirePermission perm) {
    Long userId = UserContext.getCurrentUserId();
    String[] requiredPerms = perm.value();
    
    boolean isCritical = Arrays.stream(requiredPerms)
        .anyMatch(p -> criticalPermCache.contains(p));
    
    Set<String> effectivePerms;
    if (isCritical) {
        // 关键权限：实时从 Redis 查询（确保实时生效）
        effectivePerms = redisPermCache.getEffectivePermissions(userId);
    } else {
        // 普通权限：优先使用本地缓存，版本号不一致时回源 Redis
        UserInfo user = UserContext.getCurrentUser();
        Long currentVersion = redisPermCache.getPermVersion(userId);

        if (user.getPermVersion() == currentVersion) {
            // 版本一致，使用本地缓存（避免频繁访问 Redis）
            effectivePerms = localPermCache.get(userId);
            if (effectivePerms == null) {
                // 本地缓存未命中，回源 Redis
                effectivePerms = redisPermCache.getEffectivePermissions(userId);
                localPermCache.put(userId, effectivePerms);
            }
        } else {
            // 版本不一致，权限已变更，强制回源 Redis 并更新本地缓存
            effectivePerms = redisPermCache.getEffectivePermissions(userId);
            localPermCache.put(userId, effectivePerms);
        }
    }
    
    boolean hasPerm = Arrays.stream(requiredPerms).allMatch(effectivePerms::contains);
    if (!hasPerm) {
        throw new BusinessException(403, "权限不足");
    }
    
    return jp.proceed();
}
```

### 数据权限切面 (DataScopeAspect)

**流程**:
1. 解析 @DataScope 注解
2. 获取用户数据权限范围 (SELF/DEPT/DEPT_AND_SUB/ALL)
3. 存入 ThreadLocal (DataScopeContext)
4. MyBatis 拦截器读取并改写 SQL
5. 执行查询
6. 清理 ThreadLocal

**SQL 改写示例**:
- 原 SQL: `SELECT * FROM user WHERE status = 1`
- SELF 改写: `SELECT * FROM user WHERE status = 1 AND user_id = 123`
- DEPT_AND_SUB 改写: `SELECT * FROM user WHERE status = 1 AND dept_id IN (SELECT id FROM department WHERE path LIKE '/1/2%')`

### Redis Pub/Sub 配置

监听 `auth:perm_change` 通道，收到消息后清除本地缓存标记。

---

## 5. Application & Interfaces 层设计

### Application 服务层

**RoleApplicationService**:
- createRole(), updateRole(), deleteRole(), assignPermission(), revokePermission()
- getRole(), listRoles(), getRolePermissions()

**PermissionApplicationService**:
- getPermissionTree(), listPermissions(), getUserPermissions()
- createPermission(), updatePermission(), deletePermission()

**UserPermissionApplicationService**:
- assignRole(), revokeRole()
- getUserRoles(), getCurrentUserPermissions(), getCurrentUserMenus()

**DepartmentApplicationService**:
- createDept(), updateDept(), deleteDept(), assignUserDept()
- getDeptTree(), listDepts(), getUserDepts()

### Controller API 设计

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/roles` | 角色列表（分页） | `role:list` |
| POST | `/api/roles` | 创建角色 | `role:create` |
| POST | `/api/roles/{id}/permissions` | 分配权限 | `role:assign-permission` |
| GET | `/api/permissions/tree` | 权限树 | `permission:list` |
| GET | `/api/users/{id}/roles` | 用户角色列表 | `user:view` |
| POST | `/api/users/{id}/roles` | 分配角色 | `user:assign-role` |
| GET | `/api/me/permissions` | 当前用户权限 | 无需权限 |
| GET | `/api/me/menus` | 当前用户菜单树 | 无需权限 |
| GET | `/api/departments/tree` | 部门树 | `dept:list` |

### Controller 注解使用示例

```java
@RestController
@RequestMapping("/api/roles")
@RequireRole("ROLE_ADMIN")
public class RoleController {
    
    @GetMapping
    @RequirePermission("role:list")
    public Result<PageResult<RoleResponse>> listRoles(RoleQueryRequest request) { }
    
    @PostMapping
    @RequirePermission("role:create")
    @AuditLog(operation = "CREATE_ROLE", targetType = "ROLE")
    public Result<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) { }
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping
    @RequirePermission("user:list")
    @DataScope(DataScopeType.DEPARTMENT)
    public Result<PageResult<UserResponse>> listUsers(UserQueryRequest request) { }
}
```

---

## 6. 前端设计

### 页面结构

```
ui/src/views/
├── RoleList.vue          # 角色管理列表
├── RoleEdit.vue          # 角色创建/编辑
├── PermissionList.vue    # 权限管理（树形）
├── DepartmentList.vue    # 部门管理（树形）
├── UserRoleAssign.vue    # 用户角色分配

ui/src/components/
├── PermissionTree.vue    # 权限树选择组件
├── RoleSelect.vue        # 角色多选组件
├── DeptSelect.vue        # 部门树选择组件
```

### 权限控制机制

1. **登录后获取权限**: 调用 `/api/me/permissions` 和 `/api/me/menus`
2. **动态菜单渲染**: router.beforeEach 检查权限，动态添加路由
3. **按钮权限控制**: `v-permission="'user:create'"` 指令
4. **路由守卫**: 无权限访问路由时跳转 403

### Vue 权限指令

```javascript
export const permission = {
  mounted(el, binding) {
    const permissionStore = usePermissionStore()
    const requiredPerm = binding.value
    if (!permissionStore.hasPermission(requiredPerm)) {
      el.parentNode?.removeChild(el)
    }
  }
}
```

### Pinia 权限 Store

```javascript
export const usePermissionStore = defineStore('permission', {
  state: () => ({
    permissions: new Set(),
    menus: [],
    roles: []
  }),
  actions: {
    setPermissions(perms) { this.permissions = new Set(perms.map(p => p.code)) },
    hasPermission(code) { return this.permissions.has(code) }
  }
})
```

---

## 7. 测试与验收设计

### 测试金字塔

- **E2E 测试**: 2个场景（完整权限分配流程、数据权限隔离验证）
- **集成测试**: 8个场景（Controller → Service + Redis + MySQL）
- **单元测试**: 15+个（Domain层 + Infrastructure层）
- **性能压测**: 权限检查 QPS

### 关键测试场景

**权限继承测试**:
- 拥有父权限 `user` → 自动拥有子权限 `user:list`, `user:create`
- 仅拥有 `user:view` → 不包含 `user:create`

**数据权限测试**:
- SELF: 仅返回自己的数据
- DEPT: 返回部门内所有数据
- DEPT_AND_SUB: 返回部门及子部门数据

**缓存一致性测试**:
- 分配角色后权限立即生效
- 撤销角色后权限立即失效
- Pub/Sub 广播后其他节点缓存清除

### 验收标准

**功能验收**:
- 权限变更实时生效
- 权限继承正确工作
- 数据权限隔离正确
- 前端菜单/按钮动态控制

**性能验收**:
- 权限检查缓存命中 < 10ms
- 权限检查缓存未命中 < 50ms
- 支持 1000+ QPS

**安全验收**:
- 所有敏感接口有权限控制
- SQL 注入防护
- 权限越权防护
- 审计日志完整

---

## 实施计划

按 docs/RBAC_PERMISSION_PLAN.md 中的 7 个阶段顺序执行（13天）：

| Phase | 内容 | 工期 |
|-------|------|------|
| Phase 1 | 数据库设计（含部门表） | 1天 |
| Phase 2 | Domain 层（含继承逻辑） | 2天 |
| Phase 3 | Infrastructure 层 | 2天 |
| Phase 4 | 数据权限切面实现 | 2天 |
| Phase 5 | Application/Interfaces 层 | 2天 |
| Phase 6 | 前端实现 | 2天 |
| Phase 7 | 测试与文档 | 2天 |

---

## 附录

详细实现参考 `docs/RBAC_PERMISSION_PLAN.md`，该文档包含完整的：
- SQL 表结构脚本
- 预置数据脚本
- 代码文件路径规划
- MyBatis Mapper 设计
- 详细任务清单