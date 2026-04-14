# JArsenal RBAC 权限系统重构开发计划

## 一、项目概述

### 1.1 项目背景

当前 JArsenal 项目只有基础的用户状态管理（启用/禁用），缺乏完整的权限控制体系。为满足企业级应用的安全性和可管理性需求，需要重构并实现完整的 RBAC（基于角色的访问控制）权限模型。

### 1.2 项目目标

- ✅ 实现完整的 RBAC 权限模型（用户-角色-权限）
- ✅ 支持细粒度的 API 级别权限控制
- ✅ 支持数据权限隔离（用户只能操作自己的数据）
- ✅ 权限变更实时生效（无需重新登录）
- ✅ 完整的权限审计日志
- ✅ 管理后台权限管理界面
- ✅ 高可用设计（Redis 缓存 + 数据库持久化）

### 1.3 技术选型

| 组件 | 技术 | 说明 |
|------|------|------|
| 权限框架 | 自研 + Spring AOP | 轻量级，无 Spring Security 依赖 |
| 缓存 | Redis | 权限缓存，提高性能 |
| 持久化 | MySQL + MyBatis | 权限数据存储 |
| Token | JWT | 无状态认证，携带角色/权限信息 |

---

## 二、系统架构设计

### 2.1 RBAC 模型

```
┌─────────────────────────────────────────────────────────────┐
│                        用户 (User)                           │
│  一个用户可以拥有多个角色                                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ N:M
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        角色 (Role)                           │
│  角色是权限的集合，便于批量授权                               │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ N:M
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                       权限 (Permission)                      │
│  最小权限单元，对应具体的资源和操作                           │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 权限检查流程

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐
│  请求    │───▶│ 认证拦截器   │───▶│ 权限切面     │───▶│ Controller │
└──────────┘    └──────────────┘    └──────────────┘    └────────────┘
                      │                     │
                      ▼                     ▼
                ┌──────────────┐    ┌──────────────┐
                │ 验证 JWT     │    │ 获取用户权限 │
                │ 解析用户信息  │    │ Redis 缓存  │
                └──────────────┘    │ 权限匹配检查 │
                                    └──────────────┘
```

### 2.3 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                     Interfaces Layer                         │
│  PermissionController, RoleController, UserPermissionCtrl   │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                         │
│  PermissionApplicationService, RoleApplicationService       │
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
│  Aggregate: User, Role, Permission                          │
│  ValueObject: PermissionCode, RoleCode                      │
│  DomainService: PermissionChecker, PermissionCacheManager   │
│  DomainEvent: PermissionGranted, RoleAssigned, etc.         │
├─────────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                       │
│  Repository: PermissionRepository, RoleRepository           │
│  Cache: RedisPermissionCache                                │
│  Aspect: PermissionAspect, DataScopeAspect                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 三、数据库设计

### 3.1 表结构设计

#### 3.1.1 角色表 (role)

```sql
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色编码: ROLE_ADMIN',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统内置角色: 1-是, 0-否',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    UNIQUE KEY uk_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

#### 3.1.2 权限表 (permission)

```sql
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL COMMENT '权限编码: user:create',
    resource VARCHAR(200) COMMENT '资源路径: /api/users',
    http_method VARCHAR(10) COMMENT 'HTTP方法: GET,POST,PUT,DELETE',
    menu_id BIGINT COMMENT '关联菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_type VARCHAR(20) DEFAULT 'api' COMMENT '权限类型: api-接口, menu-菜单, button-按钮',
    description VARCHAR(200) COMMENT '权限描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';
```

#### 3.1.3 用户角色关联表 (user_role)

```sql
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '授权人ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';
```

#### 3.1.4 角色权限关联表 (role_permission)

```sql
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '授权人ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

#### 3.1.5 菜单表 (menu) - **必需**，用于前端菜单权限

```sql
CREATE TABLE menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见: 1-是, 0-否',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    permission VARCHAR(100) COMMENT '权限标识',
    menu_type VARCHAR(20) COMMENT '类型: directory-目录, menu-菜单, button-按钮',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

#### 3.1.6 部门表 (department) - **必需**，用于数据权限隔离

```sql
CREATE TABLE department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    level INT DEFAULT 1 COMMENT '层级: 1-一级, 2-二级...',
    path VARCHAR(200) COMMENT '层级路径: /1/2/3，用于快速查询子部门',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    leader_id BIGINT COMMENT '部门负责人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_path (path),
    INDEX idx_leader_id (leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';
```

#### 3.1.7 用户部门关联表 (user_department) - **必需**，用于数据权限隔离

```sql
CREATE TABLE user_department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    is_primary TINYINT DEFAULT 1 COMMENT '是否主部门: 1-是, 0-否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_dept (user_id, dept_id),
    INDEX idx_user_id (user_id),
    INDEX idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户部门关联表';
```

#### 3.1.8 审计日志表 (audit_log) - **必需**，用于权限操作审计

```sql
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型: ASSIGN_ROLE, REVOKE_ROLE, GRANT_PERMISSION, REVOKE_PERMISSION',
    target_type VARCHAR(50) NOT NULL COMMENT '目标类型: USER, ROLE, PERMISSION',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(100) COMMENT '目标名称',
    before_value VARCHAR(500) COMMENT '操作前值(JSON)',
    after_value VARCHAR(500) COMMENT '操作后值(JSON)',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    request_id VARCHAR(50) COMMENT '请求ID(链路追踪)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_target (target_type, target_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';
```

### 3.2 初始数据

#### 3.2.1 预置角色

```sql
INSERT INTO role (id, name, code, description, is_system, sort_order) VALUES
(1, '超级管理员', 'ROLE_SUPER_ADMIN', '系统最高权限，拥有所有权限', 1, 1),
(2, '系统管理员', 'ROLE_ADMIN', '系统管理，用户管理，角色管理', 1, 2),
(3, '用户管理员', 'ROLE_USER_MANAGER', '用户管理相关权限', 1, 3),
(4, '普通用户', 'ROLE_USER', '基础用户权限', 1, 4);
```

#### 3.2.2 预置权限

```sql
-- 用户管理权限
INSERT INTO permission (id, name, code, resource, http_method, permission_type, parent_id, sort_order) VALUES
(1, '用户管理', 'user', NULL, NULL, 'menu', 0, 1),
(2, '用户列表', 'user:list', '/api/users', 'GET', 'api', 1, 1),
(3, '用户查询', 'user:view', '/api/users/*', 'GET', 'api', 1, 2),
(4, '用户新增', 'user:create', '/api/users', 'POST', 'api', 1, 3),
(5, '用户修改', 'user:update', '/api/users/*', 'PUT', 'api', 1, 4),
(6, '用户删除', 'user:delete', '/api/users/*', 'DELETE', 'api', 1, 5),
(7, '用户启用', 'user:enable', '/api/users/*/enable', 'PUT', 'api', 1, 6),
(8, '用户禁用', 'user:disable', '/api/users/*/disable', 'PUT', 'api', 1, 7),
(9, '密码重置', 'user:reset-password', '/api/users/*/password/reset', 'POST', 'api', 1, 8),
(10, '分配角色', 'user:assign-role', '/api/users/*/roles', 'POST', 'api', 1, 9),

-- 角色管理权限
(11, '角色管理', 'role', NULL, NULL, 'menu', 0, 2),
(12, '角色列表', 'role:list', '/api/roles', 'GET', 'api', 11, 1),
(13, '角色查询', 'role:view', '/api/roles/*', 'GET', 'api', 11, 2),
(14, '角色新增', 'role:create', '/api/roles', 'POST', 'api', 11, 3),
(15, '角色修改', 'role:update', '/api/roles/*', 'PUT', 'api', 11, 4),
(16, '角色删除', 'role:delete', '/api/roles/*', 'DELETE', 'api', 11, 5),
(17, '分配权限', 'role:assign-permission', '/api/roles/*/permissions', 'POST', 'api', 11, 6),

-- 权限管理
(18, '权限管理', 'permission', NULL, NULL, 'menu', 0, 3),
(19, '权限列表', 'permission:list', '/api/permissions', 'GET', 'api', 18, 1),

-- 审计日志
(20, '审计日志', 'audit', NULL, NULL, 'menu', 0, 4),
(21, '日志查询', 'audit:log', '/api/audit-logs', 'GET', 'api', 20, 1);
```

#### 3.2.3 角色权限关联

```sql
-- 超级管理员拥有所有权限
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission WHERE deleted = 0;

-- 系统管理员权限
INSERT INTO role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
(2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16), (2, 17),
(2, 18), (2, 19), (2, 20), (2, 21);

-- 用户管理员权限
INSERT INTO role_permission (role_id, permission_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 7), (3, 8), (3, 9);

-- 普通用户权限 (基础查看)
INSERT INTO role_permission (role_id, permission_id) VALUES
(4, 2), (4, 3);
```

---

## 四、代码实现计划

### 4.1 Domain 层

#### 4.1.1 领域对象

| 文件 | 说明 |
|------|------|
| `domain/permission/aggregate/Permission.java` | 权限聚合根 |
| `domain/role/aggregate/Role.java` | 角色聚合根 |
| `domain/permission/valueobject/PermissionCode.java` | 权限编码值对象 |
| `domain/role/valueobject/RoleCode.java` | 角色编码值对象 |
| `domain/permission/entity/Menu.java` | 菜单实体 |

#### 4.1.2 领域服务

| 文件 | 说明 |
|------|------|
| `domain/permission/service/PermissionChecker.java` | 权限检查领域服务 |
| `domain/permission/service/PermissionCacheManager.java` | 权限缓存管理 |

#### 4.1.3 领域事件

| 文件 | 说明 |
|------|------|
| `domain/permission/event/PermissionGranted.java` | 权限授予事件 |
| `domain/permission/event/PermissionRevoked.java` | 权限撤销事件 |
| `domain/role/event/RoleAssigned.java` | 角色分配事件 |
| `domain/role/event/RoleRevoked.java` | 角色撤销事件 |

#### 4.1.4 仓储接口

| 文件 | 说明 |
|------|------|
| `domain/permission/repository/PermissionRepository.java` | 权限仓储接口 |
| `domain/role/repository/RoleRepository.java` | 角色仓储接口 |
| `domain/permission/repository/UserPermissionRepository.java` | 用户权限仓储接口 |

### 4.2 Infrastructure 层

#### 4.2.1 仓储实现

| 文件 | 说明 |
|------|------|
| `infrastructure/persistence/repository/PermissionRepositoryImpl.java` | 权限仓储实现 |
| `infrastructure/persistence/repository/RoleRepositoryImpl.java` | 角色仓储实现 |
| `infrastructure/persistence/repository/UserPermissionRepositoryImpl.java` | 用户权限仓储实现 |

#### 4.2.2 缓存实现

| 文件 | 说明 |
|------|------|
| `infrastructure/cache/RedisPermissionCache.java` | Redis 权限缓存实现 |
| `infrastructure/cache/PermissionCacheProperties.java` | 缓存配置 |

#### 4.2.3 AOP 实现

| 文件 | 说明 |
|------|------|
| `infrastructure/security/annotation/RequirePermission.java` | 权限注解 |
| `infrastructure/security/annotation/RequireRole.java` | 角色注解 |
| `infrastructure/security/annotation/DataScope.java` | 数据权限注解 |
| `infrastructure/security/aspect/PermissionAspect.java` | 权限切面 |
| `infrastructure/security/aspect/DataScopeAspect.java` | 数据权限切面 |

### 4.3 Application 层

| 文件 | 说明 |
|------|------|
| `application/service/PermissionApplicationService.java` | 权限应用服务 |
| `application/service/RoleApplicationService.java` | 角色应用服务 |
| `application/service/UserPermissionApplicationService.java` | 用户权限应用服务 |
| `application/command/AssignRoleCommand.java` | 分配角色命令 |
| `application/command/AssignPermissionCommand.java` | 分配权限命令 |
| `application/dto/PermissionDTO.java` | 权限 DTO |
| `application/dto/RoleDTO.java` | 角色 DTO |
| `application/dto/UserPermissionDTO.java` | 用户权限 DTO |

### 4.4 Interfaces 层

| 文件 | 说明 |
|------|------|
| `interfaces/controller/RoleController.java` | 角色管理接口 |
| `interfaces/controller/PermissionController.java` | 权限管理接口 |
| `interfaces/controller/UserPermissionController.java` | 用户权限接口 |
| `interfaces/dto/request/RoleCreateRequest.java` | 角色创建请求 |
| `interfaces/dto/request/RoleUpdateRequest.java` | 角色更新请求 |
| `interfaces/dto/request/AssignRoleRequest.java` | 分配角色请求 |
| `interfaces/dto/request/AssignPermissionRequest.java` | 分配权限请求 |
| `interfaces/dto/response/RoleResponse.java` | 角色响应 |
| `interfaces/dto/response/PermissionResponse.java` | 权限响应 |
| `interfaces/dto/response/MenuResponse.java` | 菜单响应 |

### 4.5 前端实现

| 文件 | 说明 |
|------|------|
| `ui/src/views/RoleList.vue` | 角色管理页面 |
| `ui/src/views/PermissionList.vue` | 权限管理页面 |
| `ui/src/views/UserRole.vue` | 用户角色分配页面 |
| `ui/src/components/PermissionTree.vue` | 权限树组件 |
| `ui/src/components/RoleSelect.vue` | 角色选择组件 |
| `ui/src/api/role.js` | 角色 API |
| `ui/src/api/permission.js` | 权限 API |

---

## 五、核心功能实现

### 5.1 权限注解

```java
/**
 * 权限注解 - 用于方法级别的权限控制
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * 需要的权限编码
     */
    String[] value();

    /**
     * 逻辑关系: AND-需要全部权限, OR-需要任一权限
     */
    Logical logical() default Logical.AND;
}

/**
 * 角色注解 - 用于方法级别的角色控制
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();
    Logical logical() default Logical.AND;
}

/**
 * 数据权限注解 - 用于数据隔离
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 数据权限类型
     */
    DataScopeType value() default DataScopeType.SELF;

    /**
     * 关联的用户ID字段
     */
    String userIdField() default "userId";
}

public enum DataScopeType {
    ALL,        // 全部数据
    DEPT,       // 部门数据
    DEPT_AND_SUB, // 部门及子部门
    SELF        // 仅自己
}
```

### 5.2 权限切面

```java
@Aspect
@Component
@Slf4j
public class PermissionAspect {

    private final PermissionChecker permissionChecker;

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint,
                                  RequirePermission requirePermission) throws Throwable {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }

        String[] requiredPermissions = requirePermission.value();
        Set<String> userPermissions = permissionChecker.getUserPermissions(userId);

        boolean hasPermission;
        if (requirePermission.logical() == Logical.AND) {
            hasPermission = Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);
        } else {
            hasPermission = Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("权限不足: userId={}, required={}, actual={}",
                userId, requiredPermissions, userPermissions);
            throw new BusinessException(403, "权限不足");
        }

        return joinPoint.proceed();
    }
}
```

### 5.3 权限缓存策略（含继承缓存）

```java
@Component
public class RedisPermissionCache {

    // 缓存 Key 设计
    private static final String USER_PERMISSIONS_KEY = "auth:user:permissions:";       // 直接权限
    private static final String USER_EFFECTIVE_PERMS_KEY = "auth:user:effective_perms:"; // 继承后的所有权限
    private static final String USER_ROLES_KEY = "auth:user:roles:";
    private static final String USER_PERM_VERSION_KEY = "auth:user:perm_version:";    // 权限版本号
    private static final String CRITICAL_PERMS_KEY = "auth:critical_permissions";     // 关键权限列表
    private static final Duration CACHE_TTL = Duration.ofHours(2);

    /**
     * 获取用户有效权限（包含继承后的所有权限）
     * 权限继承规则：拥有父权限自动拥有所有子权限
     */
    public Set<String> getEffectivePermissions(Long userId) {
        String key = USER_EFFECTIVE_PERMS_KEY + userId;

        // 1. 先查缓存
        Set<String> effectivePerms = redisTemplate.opsForSet().members(key);
        if (effectivePerms != null && !effectivePerms.isEmpty()) {
            return effectivePerms;
        }

        // 2. 查数据库获取直接权限
        Set<String> directPerms = permissionRepository.findPermissionCodesByUserId(userId);

        // 3. 计算继承后的所有权限（递归查询子权限）
        Set<String> allPerms = new HashSet<>(directPerms);
        for (String permCode : directPerms) {
            // 查询该权限的所有子权限
            allPerms.addAll(permissionRepository.findSubPermissionCodes(permCode));
        }

        // 4. 写入缓存
        if (!allPerms.isEmpty()) {
            redisTemplate.opsForSet().add(key, allPerms.toArray(new String[0]));
            redisTemplate.expire(key, CACHE_TTL);
        }

        return allPerms;
    }

    /**
     * 检查权限（含继承逻辑）
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        Set<String> effectivePerms = getEffectivePermissions(userId);
        return effectivePerms.contains(permissionCode);
    }

    /**
     * 权限变更时清除缓存（含版本号更新）
     */
    public void onPermissionChange(Long userId) {
        // 1. 更新版本号（用于 Token 校验）
        Long newVersion = redisTemplate.opsForValue().increment(USER_PERM_VERSION_KEY + userId);

        // 2. 清除缓存
        redisTemplate.delete(USER_PERMISSIONS_KEY + userId);
        redisTemplate.delete(USER_EFFECTIVE_PERMS_KEY + userId);
        redisTemplate.delete(USER_ROLES_KEY + userId);

        // 3. 发布变更消息（供其他节点同步）
        redisTemplate.convertAndSend("auth:perm_change", userId + ":" + newVersion);
    }

    /**
     * 角色权限变更时，清除所有拥有该角色的用户缓存
     */
    public void onRolePermissionChange(Long roleId) {
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            onPermissionChange(userId);
        }
    }

    /**
     * 获取当前权限版本号（用于 Token 校验）
     */
    public Long getCurrentPermVersion(Long userId) {
        String version = redisTemplate.opsForValue().get(USER_PERM_VERSION_KEY + userId);
        return version != null ? Long.parseLong(version) : 0L;
    }
}
```

### 5.4 混合模式 Token 方案

**设计目标**: 关键权限实时生效，普通权限允许延迟生效

```java
/**
 * JWT Token Payload 结构
 */
{
  "sub": "userId",
  "username": "testuser",
  "roles": ["ROLE_ADMIN", "ROLE_USER"],           // 角色列表（延迟生效）
  "perm_version": 123,                             // 权限版本号（用于校验）
  "iat": 1234567890,
  "exp": 1234570890
}
```

**关键权限定义**（实时校验）:
- `user:enable`, `user:disable` - 用户启禁用
- `user:delete`, `role:delete` - 删除操作
- `role:assign-permission`, `user:assign-role` - 权限分配操作
- `role:create`, `permission:create` - 创建操作

```java
// 登录时生成 Token（仅携带角色，不携带权限）
public LoginResponse login(LoginCommand command) {
    // ... 验证逻辑 ...

    // 获取用户角色
    Set<String> roles = roleRepository.findRoleCodesByUserId(user.getId().value());

    // 获取当前权限版本号
    Long permVersion = permissionCache.getCurrentPermVersion(user.getId().value());

    // 生成 Token（携带角色 + 权限版本号）
    String accessToken = jwtTokenGenerator.generateAccessToken(
        user.getId().value(),
        user.getUsername().value(),
        roles,
        permVersion
    );

    // 权限缓存预热（计算继承后的所有权限）
    Set<String> effectivePerms = permissionRepository.findEffectivePermissions(user.getId().value());
    permissionCache.cacheEffectivePermissions(user.getId().value(), effectivePerms);

    return new LoginResponse(accessToken, refreshToken, ...);
}
```

**权限检查切面（混合模式）**:

```java
@Around("@annotation(requirePermission)")
public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) {
    Long userId = UserContext.getCurrentUserId();
    UserInfo user = UserContext.getCurrentUser();

    String[] requiredPerms = requirePermission.value();

    // 判断是否关键权限
    boolean isCritical = Arrays.stream(requiredPerms)
        .anyMatch(this::isCriticalPermission);

    if (isCritical) {
        // 关键权限：实时从 Redis 查询（含继承）
        Set<String> effectivePerms = permissionCache.getEffectivePermissions(userId);
        boolean hasPerm = Arrays.stream(requiredPerms).allMatch(effectivePerms::contains);
        if (!hasPerm) {
            throw new BusinessException(403, "权限不足");
        }
    } else {
        // 普通权限：先校验版本号，不一致则回源 Redis
        Long currentVersion = permissionCache.getCurrentPermVersion(userId);
        if (user.getPermVersion() != currentVersion) {
            // 版本不一致，从 Redis 重新获取
            Set<String> effectivePerms = permissionCache.getEffectivePermissions(userId);
            boolean hasPerm = Arrays.stream(requiredPerms).allMatch(effectivePerms::contains);
            if (!hasPerm) {
                throw new BusinessException(403, "权限不足");
            }
        } else {
            // 版本一致，使用 Token 中的角色判断（简化逻辑）
            // 或仍然从缓存查询，确保继承逻辑正确
            Set<String> effectivePerms = permissionCache.getEffectivePermissions(userId);
            boolean hasPerm = Arrays.stream(requiredPerms).allMatch(effectivePerms::contains);
            if (!hasPerm) {
                throw new BusinessException(403, "权限不足");
            }
        }
    }

    return joinPoint.proceed();
}

private boolean isCriticalPermission(String permCode) {
    Set<String> criticalPerms = redisTemplate.opsForSet().members(CRITICAL_PERMS_KEY);
    return criticalPerms != null && criticalPerms.contains(permCode);
}
```

---

## 六、测试计划

### 6.1 单元测试

| 测试文件 | 测试内容 |
|----------|----------|
| `PermissionTest.java` | 权限聚合根测试 |
| `RoleTest.java` | 角色聚合根测试 |
| `PermissionCheckerTest.java` | 权限检查逻辑测试 |
| `PermissionAspectTest.java` | 权限切面测试 |
| `RedisPermissionCacheTest.java` | 权限缓存测试 |

### 6.2 集成测试

| 测试文件 | 测试内容 |
|----------|----------|
| `RoleControllerTest.java` | 角色管理接口测试 |
| `PermissionControllerTest.java` | 权限管理接口测试 |
| `UserPermissionControllerTest.java` | 用户权限接口测试 |
| `PermissionIntegrationTest.java` | 权限系统端到端测试 |

### 6.3 测试场景

1. **权限检查测试**
   - 有权限用户访问受保护资源 → 成功
   - 无权限用户访问受保护资源 → 403
   - 未登录用户访问受保护资源 → 401

2. **权限变更测试**
   - 分配角色后权限立即生效
   - 撤销角色后权限立即失效
   - 权限缓存正确更新

3. **数据权限测试**
   - 用户只能查看自己的数据
   - 管理员可以查看所有数据

---

## 七、实施计划

### 7.1 阶段划分（调整后）

| 阶段 | 内容 | 工期 | 产出 |
|------|------|------|------|
| **Phase 1** | 数据库设计与基础设施 | 1天 | 表结构（含部门表）、PO/Mapper/Converter |
| **Phase 2** | Domain 层实现（含继承逻辑） | 2天 | 聚合根、值对象、权限继承检查服务 |
| **Phase 3** | Infrastructure 层实现 | 2天 | 仓储实现、继承缓存、权限注解/切面 |
| **Phase 4** | **数据权限切面实现** | 2天 | 部门级 DataScopeAspect、SQL改写 |
| **Phase 5** | Application/Interfaces 层 | 2天 | 应用服务、Controller、混合Token方案 |
| **Phase 6** | 前端实现（含部门管理） | 2天 | 权限管理、部门管理页面 |
| **Phase 7** | 测试与文档 | 2天 | 单元测试、集成测试、压测、API文档 |
| **总计** | | **13天** | 原10天 + 部门权限新增3天 |

### 7.2 详细任务清单

#### Phase 1: 数据库设计与基础设施 (Day 1)

- [ ] 创建数据库表 (role, permission, user_role, role_permission, menu, **department, user_department, audit_log**)
- [ ] 插入初始数据 (预置角色、权限、角色权限关联、**预置部门**)
- [ ] 创建 PO 类 (RolePO, PermissionPO, UserRolePO, RolePermissionPO, MenuPO, **DepartmentPO, UserDepartmentPO, AuditLogPO**)
- [ ] 创建 MyBatis Mapper (RoleMapper, PermissionMapper, UserRoleMapper, RolePermissionMapper, MenuMapper, **DepartmentMapper, UserDepartmentMapper, AuditLogMapper**)
- [ ] 创建 Converter (RoleConverter, PermissionConverter, **DepartmentConverter**)

#### Phase 2: Domain 层实现 (Day 2-3)

- [ ] 创建权限聚合根 Permission.java
- [ ] 创建角色聚合根 Role.java
- [ ] **创建部门聚合根 Department.java**
- [ ] 创建值对象 PermissionCode.java, RoleCode.java
- [ ] 创建领域服务 PermissionChecker.java **（含权限继承检查逻辑）**
- [ ] 创建领域服务 PermissionCacheManager.java **（含继承缓存设计）**
- [ ] 创建领域事件 (PermissionGranted, RoleAssigned, **PermissionInherited**)
- [ ] 创建仓储接口 (PermissionRepository, RoleRepository, **DepartmentRepository**)

#### Phase 3: Infrastructure 层实现 (Day 4-5)

- [ ] 实现 PermissionRepositoryImpl
- [ ] 实现 RoleRepositoryImpl
- [ ] 实现 UserPermissionRepositoryImpl
- [ ] **实现 DepartmentRepositoryImpl**
- [ ] 实现 RedisPermissionCache **（含继承权限缓存、版本号机制、Pub/Sub广播）**
- [ ] 创建权限注解 (@RequirePermission, @RequireRole, @DataScope)
- [ ] 实现 PermissionAspect **（混合模式：关键权限实时校验）**
- [ ] 配置 Redis 缓存 + Pub/Sub 监听

#### Phase 4: 数据权限切面实现 (Day 6-7)

- [ ] 实现 DataScopeAspect **（部门级数据权限）**
- [ ] 实现 SQL 改写逻辑（MyBatis 拦截器或参数注入）
- [ ] 实现部门层级查询（path 字段优化）
- [ ] 实现用户部门关联查询
- [ ] 测试数据权限隔离（SELF/DEPT/DEPT_AND_SUB/ALL）

#### Phase 5: Application/Interfaces 层实现 (Day 8-9)

- [ ] 实现 PermissionApplicationService
- [ ] 实现 RoleApplicationService
- [ ] 实现 UserPermissionApplicationService
- [ ] **实现 DepartmentApplicationService**
- [ ] 创建 Command 类 (AssignRoleCommand, AssignPermissionCommand, **AssignDeptCommand**)
- [ ] 创建 DTO 类
- [ ] 实现 RoleController
- [ ] 实现 PermissionController
- [ ] 实现 UserPermissionController
- [ ] **实现 DepartmentController**
- [ ] 实现审计日志切面 AuditLogAspect
- [ ] 更新 JwtUtil **（混合Token方案：携带角色+版本号）**
- [ ] 更新现有 Controller 添加权限注解
- [ ] 更新 Swagger 文档

#### Phase 6: 前端实现 (Day 10-11)

- [ ] 创建角色管理页面 RoleList.vue
- [ ] 创建权限管理页面 PermissionList.vue
- [ ] 创建用户角色分配页面 UserRole.vue
- [ ] **创建部门管理页面 DepartmentList.vue**
- [ ] 创建权限树组件 PermissionTree.vue
- [ ] 创建角色选择组件 RoleSelect.vue
- [ ] **创建部门选择组件 DeptSelect.vue**
- [ ] 实现 API 接口 (role.js, permission.js, **department.js**)
- [ ] 更新路由和导航
- [ ] 前端权限指令 v-permission
- [ ] 实现 /api/me/menus 菜单树返回

#### Phase 7: 测试与文档 (Day 12-13)

- [ ] 编写单元测试（PermissionChecker、**权限继承逻辑**）
- [ ] 编写集成测试（RoleController, PermissionController）
- [ ] **编写数据权限隔离测试（DataScopeAspect）**
- [ ] **编写性能压测（权限检查 QPS）**
- [ ] 更新 API 文档
- [ ] 编写用户手册
- [ ] 代码审查与优化

- [ ] 实现 PermissionApplicationService
- [ ] 实现 RoleApplicationService
- [ ] 实现 UserPermissionApplicationService
- [ ] 创建 Command 类
- [ ] 创建 DTO 类

#### Phase 5: Interfaces 层实现 (Day 7)

- [ ] 实现 RoleController
- [ ] 实现 PermissionController
- [ ] 实现 UserPermissionController
- [ ] 创建请求/响应对象
- [ ] 更新现有 Controller 添加权限注解
- [ ] 更新 Swagger 文档

#### Phase 6: 前端实现 (Day 8-9)

- [ ] 创建角色管理页面 RoleList.vue
- [ ] 创建权限管理页面 PermissionList.vue
- [ ] 创建用户角色分配页面 UserRole.vue
- [ ] 创建权限树组件 PermissionTree.vue
- [ ] 创建角色选择组件 RoleSelect.vue
- [ ] 实现 API 接口
- [ ] 更新路由和导航
- [ ] 前端权限指令 v-permission

#### Phase 7: 测试与文档 (Day 10)

- [ ] 编写单元测试
- [ ] 编写集成测试
- [ ] 更新 API 文档
- [ ] 编写用户手册
- [ ] 代码审查与优化

---

## 八、API 接口设计

### 8.1 角色管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/roles` | 角色列表 | role:list |
| GET | `/api/roles/{id}` | 角色详情 | role:view |
| POST | `/api/roles` | 创建角色 | role:create |
| PUT | `/api/roles/{id}` | 更新角色 | role:update |
| DELETE | `/api/roles/{id}` | 删除角色 | role:delete |
| POST | `/api/roles/{id}/permissions` | 分配权限 | role:assign-permission |
| GET | `/api/roles/{id}/permissions` | 角色权限列表 | role:view |

### 8.2 权限管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/permissions` | 权限列表（树形） | permission:list |
| GET | `/api/permissions/tree` | 权限树 | permission:list |
| POST | `/api/permissions` | 创建权限 | permission:create |
| PUT | `/api/permissions/{id}` | 更新权限 | permission:update |
| DELETE | `/api/permissions/{id}` | 删除权限 | permission:delete |

### 8.3 用户权限

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/users/{id}/roles` | 用户角色列表 | user:view |
| POST | `/api/users/{id}/roles` | 分配角色 | user:assign-role |
| DELETE | `/api/users/{id}/roles/{roleId}` | 移除角色 | user:assign-role |
| GET | `/api/users/{id}/permissions` | 用户权限列表 | user:view |
| GET | `/api/me/permissions` | 当前用户权限 | - |
| GET | `/api/me/menus` | 当前用户菜单 | - |

---

## 九、风险评估与应对

### 9.1 技术风险

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 权限缓存不一致 | 中 | 使用 Redis 发布订阅，权限变更时广播清除缓存 |
| 性能问题 | 中 | 权限数据缓存 + JWT 携带核心权限 |
| 数据迁移 | 高 | 提供迁移脚本，平滑升级 |

### 9.2 业务风险

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 超级管理员误删 | 高 | 超级管理员不可删除，系统内置角色保护 |
| 权限配置错误 | 中 | 提供权限预览功能，操作前确认 |
| 历史数据兼容 | 中 | 新用户默认分配普通用户角色 |

---

## 十、验收标准

### 10.1 功能验收

- [ ] 用户可以登录并获取正确的权限
- [ ] 无权限用户访问受保护资源返回 403
- [ ] 权限变更后实时生效（无需重新登录）
- [ ] 角色管理功能完整可用
- [ ] 权限管理功能完整可用
- [ ] 用户角色分配功能完整可用
- [ ] 审计日志正确记录权限操作

### 10.2 性能验收

- [ ] 权限检查响应时间 < 10ms（缓存命中）
- [ ] 权限检查响应时间 < 50ms（缓存未命中）
- [ ] 支持 1000+ QPS 权限检查

### 10.3 安全验收

- [ ] 所有敏感接口都有权限控制
- [ ] SQL 注入防护
- [ ] 权限越权防护
- [ ] 敏感操作审计日志

---

## 十一、附录

### 11.1 参考资料

- Spring Security 官方文档
- RBAC 权限模型规范
- 企业级权限系统最佳实践

---

## 十二、补充设计（根据评估调整）

### 12.1 权限继承规则

**规则定义**：拥有父权限自动拥有所有子权限

```
权限树结构示例：
user (父权限，parent_id=0)
├── user:list (子权限，parent_id=1)
├── user:view (子权限，parent_id=1)
├── user:create (子权限，parent_id=1)
├── user:update (子权限，parent_id=1)
├── user:delete (子权限，parent_id=1)
│   ├── user:enable (孙权限，parent_id=6)
│   └── user:disable (孙权限，parent_id=6)
└── user:assign-role (子权限，parent_id=1)
```

**继承检查逻辑**：

```java
/**
 * 检查用户是否拥有某权限（含继承）
 * 如果用户拥有 "user" 权限，则自动拥有 "user:list", "user:view" 等所有子权限
 */
public boolean checkPermissionWithInheritance(Long userId, String permissionCode) {
    // 1. 获取用户所有有效权限（已包含继承）
    Set<String> effectivePerms = getEffectivePermissions(userId);

    // 2. 直接匹配
    return effectivePerms.contains(permissionCode);
}

/**
 * 计算有效权限：直接权限 + 所有子权限
 */
public Set<String> computeEffectivePermissions(Set<String> directPerms) {
    Set<String> allPerms = new HashSet<>(directPerms);

    // 递归查询每个直接权限的所有子权限
    for (String permCode : directPerms) {
        Permission perm = permissionRepository.findByCode(permCode);
        if (perm != null) {
            // 查询该权限的所有后代权限
            allPerms.addAll(findAllDescendants(perm.getId()));
        }
    }

    return allPerms;
}

/**
 * 递归查询所有后代权限
 */
private Set<String> findAllDescendants(Long parentId) {
    List<Permission> children = permissionRepository.findByParentId(parentId);
    Set<String> descendants = new HashSet<>();

    for (Permission child : children) {
        descendants.add(child.getCode());
        descendants.addAll(findAllDescendants(child.getId()));
    }

    return descendants;
}
```

### 12.2 部门级数据权限隔离

**数据权限类型**：

| 类型 | 说明 | SQL 过滤条件 |
|------|------|-------------|
| ALL | 全部数据 | 无限制 |
| DEPT | 本部门数据 | `WHERE dept_id IN (用户所属部门)` |
| DEPT_AND_SUB | 本部门及子部门数据 | `WHERE dept_id IN (用户部门及其所有子部门)` |
| SELF | 仅本人数据 | `WHERE user_id = 当前用户ID` |

**实现方案**（MyBatis 拦截器 + 参数注入）：

```java
/**
 * 数据权限切面 - 在查询方法执行前注入数据权限条件
 */
@Aspect
@Component
public class DataScopeAspect {

    @Around("@annotation(dataScope)")
    public Object applyDataScope(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        Long userId = UserContext.getCurrentUserId();

        // 1. 获取用户的数据权限范围
        DataScopeType scope = getUserDataScope(userId);

        // 2. 将权限范围存入 ThreadLocal，供 MyBatis 拦截器读取
        DataScopeContext.setScope(scope);
        DataScopeContext.setUserId(userId);
        DataScopeContext.setDeptIds(getUserDeptIds(userId));

        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }

    /**
     * 获取用户部门及子部门 ID 列表
     */
    private Set<Long> getUserDeptAndSubDeptIds(Long userId) {
        // 1. 获取用户所属部门
        Set<Long> userDepts = userDeptRepository.findDeptIdsByUserId(userId);

        // 2. 查询所有子部门（使用 path 字段优化）
        Set<Long> allDepts = new HashSet<>(userDepts);
        for (Long deptId : userDepts) {
            Department dept = deptRepository.findById(deptId);
            // 使用 path LIKE '/deptId/%' 查询所有后代部门
            allDepts.addAll(deptRepository.findDescendantsByPath(dept.getPath()));
        }

        return allDepts;
    }
}

/**
 * MyBatis 拦截器 - 动态修改 SQL
 */
@Intercepts({@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        DataScopeType scope = DataScopeContext.getScope();
        if (scope == null || scope == DataScopeType.ALL) {
            return invocation.proceed();
        }

        // 动态修改 SQL，添加数据权限条件
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = ms.getBoundSql(parameter);

        String originalSql = boundSql.getSql();
        String dataScopeSql = addDataScopeCondition(originalSql, scope);

        // 使用反射设置新的 SQL
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, dataScopeSql);

        return invocation.proceed();
    }

    private String addDataScopeCondition(String sql, DataScopeType scope) {
        Long userId = DataScopeContext.getUserId();
        Set<Long> deptIds = DataScopeContext.getDeptIds();

        String condition;
        switch (scope) {
            case SELF:
                condition = "user_id = " + userId;
                break;
            case DEPT:
                condition = "dept_id IN (" + StringUtils.join(deptIds, ",") + ")";
                break;
            case DEPT_AND_SUB:
                condition = "dept_id IN (" + StringUtils.join(deptIds, ",") + ")";
                break;
            default:
                return sql;
        }

        // 添加 WHERE 条件
        if (sql.toUpperCase().contains("WHERE")) {
            return sql + " AND " + condition;
        } else {
            return sql + " WHERE " + condition;
        }
    }
}
```

### 12.3 分布式缓存一致性方案

**问题**：多节点部署时，权限变更后其他节点缓存未及时清除

**解决方案**：Redis Pub/Sub + 版本号机制

```java
/**
 * Redis Pub/Sub 配置
 */
@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(new PermissionChangeListener(), new ChannelTopic("auth:perm_change"));
        return container;
    }
}

/**
 * 权限变更消息监听器
 */
public class PermissionChangeListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = message.toString();
        String[] parts = payload.split(":");
        Long userId = Long.parseLong(parts[0]);
        Long version = Long.parseLong(parts[1]);

        // 清除本地缓存
        localPermissionCache.evict(userId);

        // 更新本地版本号
        localPermVersion.put(userId, version);

        log.info("权限变更消息已接收: userId={}, version={}", userId, version);
    }
}
```

### 12.4 审计日志实现

```java
/**
 * 审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Around("@annotation(auditLog)")
    public Object recordAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();
        String ip = getIpAddress();
        String requestId = MDC.get("traceId");

        // 记录操作前状态
        Object beforeValue = getBeforeValue(joinPoint);

        Object result = joinPoint.proceed();

        // 记录操作后状态
        Object afterValue = getAfterValue(joinPoint, result);

        // 保存审计日志
        AuditLogPO log = new AuditLogPO();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(auditLog.operation());
        log.setTargetType(auditLog.targetType());
        log.setBeforeValue(JsonUtils.toJson(beforeValue));
        log.setAfterValue(JsonUtils.toJson(afterValue));
        log.setIpAddress(ip);
        log.setRequestId(requestId);

        auditLogRepository.save(log);

        return result;
    }
}

/**
 * 审计日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
    String operation();     // 操作类型
    String targetType();    // 目标类型
}
```
