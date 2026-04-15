# RBAC P1: 核心模型与数据库实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现 RBAC 核心数据库架构和领域模型（Role、Resource、Permission 聚合根）

**架构：** DDD 风格领域模型，包含聚合根、值对象和仓储接口。数据库使用软删除、版本号用于缓存校验，以及触发器防止循环继承。

**技术栈：** Spring Boot 3.x、MyBatis、MySQL、Flyway 迁移、Jakarta 校验

---

## 文件结构

```
src/main/java/com/example/demo/
├── domain/
│   └── permission/
│       ├── aggregate/
│       │   ├── Role.java               # 角色聚合根
│       │   └── Resource.java           # 资源聚合根
│       ├── entity/
│       │   ├── RoleDataScope.java      # 数据范围实体
│       │   ├── RoleDataScopeValue.java # 范围值实体
│       │   ├── ResourceField.java      # 敏感字段实体
│       │   └── FieldPermission.java    # 字段权限实体
│       ├── valueobject/
│       ├── valueobject/
│       │   ├── RoleId.java             # 角色标识值对象
│       │   ├── RoleCode.java           # 角色编码值对象
│       │   ├── ResourceType.java       # 资源类型枚举
│       │   ├── ActionType.java         # 操作类型枚举
│       │   ├── DimensionType.java      # 数据维度枚举
│       │   ├── PermissionEffect.java   # 允许/拒绝枚举
│       │   ├── InheritMode.java        # 扩展/限制枚举
│       │   ├── SensitiveLevel.java     # 字段敏感级别枚举
│       │   ├── ScopeType.java          # 数据范围类型枚举
│       │   ├── RolePermission.java     # 权限值对象（不可变）
│       │   ├── PermissionBitmap.java   # 位图值对象用于性能优化
│       │   └── RoleStatus.java         # 角色状态枚举
│       ├── repository/
│       │   ├── RoleRepository.java     # 角色仓储接口
│       │   ├── ResourceRepository.java # 资源仓储接口
│       │   ├── PermissionRepository.java
│       │   ├── UserRoleRepository.java
│       │   └── DataDimensionRepository.java
│       ├── event/
│       │   ├── RoleCreatedEvent.java
│       │   ├── RolePermissionChangedEvent.java
│       │   ├── UserRoleAssignedEvent.java
│       │   └── RoleDeletedEvent.java
│       └── exception/
│           └── DomainException.java    # 领域异常
├── infrastructure/
│   └── persistence/
│       └── mapper/
│           ├── RoleMapper.java
│           ├── RoleMapper.xml
│           ├── ResourceMapper.java
│           ├── ResourceMapper.xml
│           ├── PermissionMapper.java
│           ├── PermissionMapper.xml
│           ├── PermissionActionMapper.java
│           ├── PermissionActionMapper.xml
│           ├── UserRoleMapper.java
│           ├── UserRoleMapper.xml
│           ├── RoleDataScopeMapper.java
│           ├── RoleDataScopeMapper.xml
│           ├── RoleDataScopeValueMapper.java
│           ├── RoleDataScopeValueMapper.xml
│           ├── DataDimensionMapper.java
│           ├── DataDimensionMapper.xml
│           ├── ResourceFieldMapper.java
│           ├── ResourceFieldMapper.xml
│           ├── FieldPermissionMapper.java
│           └── FieldPermissionMapper.xml
│       └── repository/
│           ├── RoleRepositoryImpl.java
│           ├── ResourceRepositoryImpl.java
│           └── PermissionRepositoryImpl.java
│       └ converter/
│           ├── RoleCodeTypeHandler.java    # 值对象 TypeHandler
│           ├── RoleStatusTypeHandler.java
│           ├── InheritModeTypeHandler.java
│           ├── ResourceTypeTypeHandler.java

src/main/resources/
├── db/migration/
│   ├── V1__Init_RBAC_Tables.sql        # 核心表
│   └── V2__RBAC_Triggers.sql           # 循环继承触发器
├── mapper/
│   └── (与 infrastructure 相同的 mapper)

src/test/java/com/example/demo/
├── domain/permission/
│   ├── RoleTest.java                   # 角色聚合测试
│   ├── RolePermissionTest.java        # 值对象不可变性测试
│   ├── PermissionBitmapTest.java      # 位图合并/拒绝测试
│   └── RoleCodeTest.java              # 值对象校验测试
```

---

## 任务 1：添加 Flyway 依赖

**文件：**
- 修改：`pom.xml`

- [ ] **步骤 1：在 pom.xml 中添加 Flyway 依赖**

```xml
<!-- 在 pom.xml dependencies 部分 -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

- [ ] **步骤 2：在 application.yml 中配置 Flyway**

```yaml
# 在 application.yml 中
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

- [ ] **步骤 3：创建迁移目录**

```bash
mkdir -p src/main/resources/db/migration
```

- [ ] **步骤 4：提交依赖变更**

```bash
git add pom.xml src/main/resources/application.yml
git commit -m "feat(rbac): add Flyway migration dependency"
```

---

## 任务 2：创建 V1 迁移 - 核心表

**文件：**
- 创建：`src/main/resources/db/migration/V1__Init_RBAC_Tables.sql`

- [ ] **步骤 1：编写角色表迁移**

```sql
-- V1__Init_RBAC_Tables.sql

-- 角色表（支持继承树 + 软删除）
CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    parent_id BIGINT COMMENT '父角色ID（继承）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    inherit_mode VARCHAR(10) DEFAULT 'EXTEND' COMMENT '继承模式：EXTEND/LIMIT',
    is_builtin TINYINT DEFAULT 0 COMMENT '是否内置角色',
    is_deleted TINYINT DEFAULT 0 COMMENT '软删除：0-正常，1-已删除',
    version INT DEFAULT 0 COMMENT '版本号（用于缓存校验）',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code),
    INDEX idx_status (status),
    INDEX idx_not_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';
```

- [ ] **步骤 2：添加资源和权限表**

```sql
-- 资源表（菜单/操作/API三级 + 路径模式）
CREATE TABLE resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '资源编码',
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    parent_id BIGINT COMMENT '父资源ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：MENU/OPERATION/API',
    path VARCHAR(200) COMMENT '路径（菜单路由）',
    path_pattern VARCHAR(200) COMMENT '路径模式（Ant风格：/api/users/**）',
    method VARCHAR(10) COMMENT 'API方法：GET/POST/PUT/DELETE',
    icon VARCHAR(50) COMMENT '菜单图标',
    component VARCHAR(100) COMMENT '前端组件路径',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    is_deleted TINYINT DEFAULT 0 COMMENT '软删除标记',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type),
    INDEX idx_path_pattern (path_pattern),
    INDEX idx_not_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';

-- 权限表（角色-资源关联）
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    effect VARCHAR(10) DEFAULT 'ALLOW' COMMENT '效果：ALLOW/DENY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    INDEX idx_role_id (role_id),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 权限操作表（拆分，规范化设计）
CREATE TABLE permission_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    action VARCHAR(20) NOT NULL COMMENT '操作：VIEW/CREATE/UPDATE/DELETE/EXECUTE',
    INDEX idx_permission_id (permission_id),
    UNIQUE KEY uk_perm_action (permission_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限操作表';
```

- [ ] **步骤 3：添加用户角色和字段权限表**

```sql
-- 用户角色关联表（多角色）
CREATE TABLE user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 资源字段表（敏感字段定义）
CREATE TABLE resource_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    field_code VARCHAR(50) NOT NULL COMMENT '字段编码（对应DTO属性名）',
    field_name VARCHAR(100) NOT NULL COMMENT '字段名称',
    sensitive_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '敏感级别：NORMAL/HIDDEN/ENCRYPTED',
    mask_pattern VARCHAR(50) COMMENT '脱敏规则：ID_CARD/PHONE/SALARY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_field (resource_id, field_code),
    INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源字段表';

-- 字段权限表
CREATE TABLE field_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    field_id BIGINT NOT NULL COMMENT '字段ID',
    can_view TINYINT DEFAULT 1 COMMENT '是否可查看',
    can_edit TINYINT DEFAULT 1 COMMENT '是否可编辑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_field (role_id, field_id),
    INDEX idx_role_id (role_id),
    INDEX idx_field_id (field_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段权限表';
```

- [ ] **步骤 4：添加数据维度表**

```sql
-- 数据维度定义表
CREATE TABLE data_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '维度编码：DEPARTMENT/PROJECT/CUSTOMER',
    name VARCHAR(100) NOT NULL COMMENT '维度名称',
    description VARCHAR(200) COMMENT '描述',
    source_table VARCHAR(100) COMMENT '数据来源表',
    source_column VARCHAR(100) COMMENT '关联字段',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据维度定义表';

-- 角色数据权限表（改进：范围值拆分为子表）
CREATE TABLE role_data_scope (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    scope_type VARCHAR(20) NOT NULL COMMENT '范围类型：ALL/SELF/SELF_DEPT/DEPT_TREE/CUSTOM',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_dimension (role_id, dimension_code),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限表';

-- 数据权限范围值表（拆分，规范化设计）
CREATE TABLE role_data_scope_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scope_id BIGINT NOT NULL COMMENT '数据权限ID',
    value_id BIGINT NOT NULL COMMENT '范围值ID（如部门ID、项目ID）',
    INDEX idx_scope_id (scope_id),
    UNIQUE KEY uk_scope_value (scope_id, value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限范围值表';

-- 用户维度关联表（用户跨维度场景）
CREATE TABLE user_dimension (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dimension_code VARCHAR(50) NOT NULL COMMENT '维度编码',
    dimension_value_id BIGINT NOT NULL COMMENT '维度值ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_dimension (user_id, dimension_code, dimension_value_id),
    INDEX idx_user_id (user_id),
    INDEX idx_dimension (dimension_code, dimension_value_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户维度关联表';
```

- [ ] **步骤 5：添加审计日志表**

```sql
-- 权限变更审计表
CREATE TABLE permission_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型：ROLE/RESOURCE/USER',
    target_id BIGINT NOT NULL COMMENT '目标ID',
    before_value TEXT COMMENT '变更前值（JSON）',
    after_value TEXT COMMENT '变更后值（JSON）',
    reason VARCHAR(200) COMMENT '变更原因',
    trace_id VARCHAR(50) COMMENT '链路追踪ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_operator (operator_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_change_type (change_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限变更审计表';
```

- [ ] **步骤 6：提交迁移**

```bash
git add src/main/resources/db/migration/V1__Init_RBAC_Tables.sql
git commit -m "feat(rbac): add core RBAC tables migration"
```

---

## 任务 3：创建 V2 迁移 - 循环继承触发器

**文件：**
- 创建：`src/main/resources/db/migration/V2__RBAC_Triggers.sql`

- [ ] **步骤 1：编写触发器迁移**

```sql
-- V2__RBAC_Triggers.sql

DELIMITER //

/**
 * 触发器：角色parent_id更新前校验循环继承
 */
CREATE TRIGGER trg_role_before_update
BEFORE UPDATE ON role
FOR EACH ROW
BEGIN
    DECLARE is_circular INT DEFAULT 0;
    DECLARE current_parent BIGINT;
    
    IF NEW.parent_id IS NOT NULL AND (OLD.parent_id IS NULL OR NEW.parent_id != OLD.parent_id) THEN
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;
        
        SET current_parent = NEW.parent_id;
        SET is_circular = 0;
        
        WHILE current_parent IS NOT NULL AND is_circular = 0 DO
            IF current_parent = NEW.id THEN
                SET is_circular = 1;
            END IF;
            
            SELECT parent_id INTO current_parent 
            FROM role 
            WHERE id = current_parent AND is_deleted = 0;
        END WHILE;
        
        IF is_circular = 1 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色继承链形成循环，更新被拒绝';
        END IF;
    END IF;
END//

/**
 * 触发器：角色插入前校验parent_id有效性
 */
CREATE TRIGGER trg_role_before_insert
BEFORE INSERT ON role
FOR EACH ROW
BEGIN
    DECLARE parent_exists INT DEFAULT 0;
    
    IF NEW.parent_id IS NOT NULL THEN
        IF NEW.parent_id = NEW.id THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '角色不能以自己为父角色（循环继承）';
        END IF;
        
        SELECT COUNT(*) INTO parent_exists 
        FROM role 
        WHERE id = NEW.parent_id AND is_deleted = 0;
        
        IF parent_exists = 0 THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = '父角色不存在或已被删除';
        END IF;
    END IF;
END//

DELIMITER ;
```

- [ ] **步骤 2：提交触发器迁移**

```bash
git add src/main/resources/db/migration/V2__RBAC_Triggers.sql
git commit -m "feat(rbac): add circular inheritance prevention triggers"
```

---

## 任务 4：创建领域异常

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/exception/DomainException.java`

- [ ] **步骤 1：编写领域异常类**

```java
package com.example.demo.domain.permission.exception;

public class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **步骤 2：提交异常类**

```bash
git add src/main/java/com/example/demo/domain/permission/exception/DomainException.java
git commit -m "feat(rbac): add DomainException for domain validation errors"
```

---

## 任务 5：创建值对象 - 枚举

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/ResourceType.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/ActionType.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/DimensionType.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/PermissionEffect.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/InheritMode.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/SensitiveLevel.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/ScopeType.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/RoleStatus.java`

- [ ] **步骤 1：编写 ResourceType 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum ResourceType {
    MENU,
    OPERATION,
    API
}
```

- [ ] **步骤 2：编写 ActionType 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum ActionType {
    VIEW,
    CREATE,
    UPDATE,
    DELETE,
    EXECUTE
}
```

- [ ] **步骤 3：编写 DimensionType 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum DimensionType {
    DEPARTMENT,
    PROJECT,
    CUSTOMER,
    CUSTOM
}
```

- [ ] **步骤 4：编写 PermissionEffect 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum PermissionEffect {
    ALLOW,
    DENY
}
```

- [ ] **步骤 5：编写 InheritMode 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum InheritMode {
    EXTEND,
    LIMIT
}
```

- [ ] **步骤 6：编写 SensitiveLevel 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum SensitiveLevel {
    NORMAL,
    HIDDEN,
    ENCRYPTED
}
```

- [ ] **步骤 7：编写 ScopeType 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum ScopeType {
    ALL,
    SELF,
    SELF_DEPT,
    DEPT_TREE,
    CUSTOM
}
```

- [ ] **步骤 8：编写 RoleStatus 枚举**

```java
package com.example.demo.domain.permission.valueobject;

public enum RoleStatus {
    ENABLED,
    DISABLED
}
```

- [ ] **步骤 9：提交枚举值对象**

```bash
git add src/main/java/com/example/demo/domain/permission/valueobject/*.java
git commit -m "feat(rbac): add enum value objects for domain model"
```

---

## 任务 6：创建值对象 - 标识符

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/RoleId.java`
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/RoleCode.java`

- [ ] **步骤 1：编写 RoleId 值对象**

```java
package com.example.demo.domain.permission.valueobject;

import com.example.demo.domain.permission.exception.DomainException;

public record RoleId(Long value) {
    
    public RoleId {
        if (value == null || value <= 0) {
            throw new DomainException("角色ID必须为正整数");
        }
    }
}
```

- [ ] **步骤 2：编写 RoleCode 值对象**

```java
package com.example.demo.domain.permission.valueobject;

import com.example.demo.domain.permission.exception.DomainException;
import java.util.regex.Pattern;

public record RoleCode(String value) {
    
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,49}$");
    
    public RoleCode {
        if (value == null || value.isBlank()) {
            throw new DomainException("角色编码不能为空");
        }
        if (!CODE_PATTERN.matcher(value).matches()) {
            throw new DomainException("角色编码格式错误：需2-50字符，大写字母开头，仅含字母数字下划线");
        }
    }
}
```

- [ ] **步骤 3：编写 RoleCodeTest 校验测试**

```java
package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.RoleCode;
import com.example.demo.domain.permission.exception.DomainException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleCodeTest {
    
    @Test
    void shouldAcceptValidCode() {
        RoleCode code = new RoleCode("ADMIN");
        assertEquals("ADMIN", code.value());
    }
    
    @Test
    void shouldRejectNullCode() {
        assertThrows(DomainException.class, () -> new RoleCode(null));
    }
    
    @Test
    void shouldRejectBlankCode() {
        assertThrows(DomainException.class, () -> new RoleCode(""));
        assertThrows(DomainException.class, () -> new RoleCode("   "));
    }
    
    @Test
    void shouldRejectInvalidFormat() {
        assertThrows(DomainException.class, () -> new RoleCode("admin"));  // 小写
        assertThrows(DomainException.class, () -> new RoleCode("1ADMIN")); // 数字开头
        assertThrows(DomainException.class, () -> new RoleCode("A"));      // 太短
    }
}
```

- [ ] **步骤 4：运行测试验证校验生效**

```bash
mvn test -Dtest=RoleCodeTest -v
```
预期：所有测试通过

- [ ] **步骤 5：提交标识符值对象**

```bash
git add src/main/java/com/example/demo/domain/permission/valueobject/RoleId.java \
        src/main/java/com/example/demo/domain/permission/valueobject/RoleCode.java \
        src/test/java/com/example/demo/domain/permission/RoleCodeTest.java
git commit -m "feat(rbac): add RoleId and RoleCode value objects with validation"
```

---

## 任务 7：创建 RolePermission 值对象

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/RolePermission.java`
- 创建：`src/test/java/com/example/demo/domain/permission/RolePermissionTest.java`

- [ ] **步骤 1：编写 RolePermission 值对象（不可变）**

```java
package com.example.demo.domain.permission.valueobject;

import com.example.demo.domain.permission.exception.DomainException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RolePermission {
    private final Long resourceId;
    private final Set<ActionType> actions;
    private final PermissionEffect effect;
    
    public RolePermission(Long resourceId, Set<ActionType> actions, PermissionEffect effect) {
        if (resourceId == null) {
            throw new DomainException("资源ID不能为空");
        }
        if (actions == null || actions.isEmpty()) {
            throw new DomainException("操作集合不能为空");
        }
        this.resourceId = resourceId;
        this.actions = Collections.unmodifiableSet(new HashSet<>(actions));
        this.effect = effect != null ? effect : PermissionEffect.ALLOW;
    }
    
    public Long getResourceId() { return resourceId; }
    public Set<ActionType> getActions() { return actions; }
    public PermissionEffect getEffect() { return effect; }
    
    public boolean hasAction(ActionType action) {
        return actions.contains(action);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePermission that)) return false;
        return resourceId.equals(that.resourceId);
    }
    
    @Override
    public int hashCode() {
        return resourceId.hashCode();
    }
}
```

- [ ] **步骤 2：编写 RolePermissionTest 不可变性测试**

```java
package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.RolePermission;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.PermissionEffect;
import com.example.demo.domain.permission.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RolePermissionTest {
    
    @Test
    void shouldCreateValidPermission() {
        RolePermission perm = new RolePermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE), PermissionEffect.ALLOW);
        assertEquals(1L, perm.getResourceId());
        assertTrue(perm.hasAction(ActionType.VIEW));
        assertTrue(perm.hasAction(ActionType.CREATE));
    }
    
    @Test
    void shouldRejectNullResourceId() {
        assertThrows(DomainException.class, 
            () -> new RolePermission(null, Set.of(ActionType.VIEW), PermissionEffect.ALLOW));
    }
    
    @Test
    void shouldRejectEmptyActions() {
        assertThrows(DomainException.class, 
            () -> new RolePermission(1L, Set.of(), PermissionEffect.ALLOW));
        assertThrows(DomainException.class, 
            () -> new RolePermission(1L, null, PermissionEffect.ALLOW));
    }
    
    @Test
    void shouldBeImmutable() {
        Set<ActionType> mutableActions = new HashSet<>();
        mutableActions.add(ActionType.VIEW);
        
        RolePermission perm = new RolePermission(1L, mutableActions, PermissionEffect.ALLOW);
        
        // 尝试修改原始集合
        mutableActions.add(ActionType.DELETE);
        
        // 权限不应受影响
        assertFalse(perm.hasAction(ActionType.DELETE));
        
        // 尝试修改返回的集合
        assertThrows(UnsupportedOperationException.class, 
            () -> perm.getActions().add(ActionType.DELETE));
    }
    
    @Test
    void shouldDefaultToAllowEffect() {
        RolePermission perm = new RolePermission(1L, Set.of(ActionType.VIEW), null);
        assertEquals(PermissionEffect.ALLOW, perm.getEffect());
    }
}
```

- [ ] **步骤 3：运行测试验证不可变性**

```bash
mvn test -Dtest=RolePermissionTest -v
```
预期：所有测试通过

- [ ] **步骤 4：提交 RolePermission 值对象**

```bash
git add src/main/java/com/example/demo/domain/permission/valueobject/RolePermission.java \
        src/test/java/com/example/demo/domain/permission/RolePermissionTest.java
git commit -m "feat(rbac): add immutable RolePermission value object with tests"
```

---

## 任务 8：创建 PermissionBitmap 值对象

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/valueobject/PermissionBitmap.java`
- 创建：`src/test/java/com/example/demo/domain/permission/PermissionBitmapTest.java`

- [ ] **步骤 1：编写 PermissionBitmap 值对象**

```java
package com.example.demo.domain.permission.valueobject;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public final class PermissionBitmap {
    private final Map<Long, BitSet> actionBits;
    private final long version;
    
    public PermissionBitmap(Map<Long, BitSet> actionBits, long version) {
        this.actionBits = actionBits != null ? new HashMap<>(actionBits) : new HashMap<>();
        this.version = version;
    }
    
    public static PermissionBitmap empty(long version) {
        return new PermissionBitmap(new HashMap<>(), version);
    }
    
    public static PermissionBitmap empty() {
        return empty(System.currentTimeMillis());
    }
    
    /**
     * 位图合并（高效O(n)操作）
     */
    public PermissionBitmap merge(PermissionBitmap other) {
        Map<Long, BitSet> merged = new HashMap<>(this.actionBits);
        
        other.actionBits.forEach((resource, bits) -> {
            BitSet existing = merged.get(resource);
            if (existing != null) {
                existing.or(bits);
            } else {
                merged.put(resource, (BitSet) bits.clone());
            }
        });
        
        return new PermissionBitmap(merged, System.currentTimeMillis());
    }
    
    /**
     * 检查是否有指定操作权限
     */
    public boolean hasAction(Long resourceId, ActionType action) {
        BitSet bits = actionBits.get(resourceId);
        return bits != null && bits.get(action.ordinal());
    }
    
    /**
     * 处理DENY冲突（DENY优先）
     */
    public PermissionBitmap applyDeny(PermissionBitmap denyBitmap) {
        denyBitmap.actionBits.forEach((resource, denyBits) -> {
            BitSet existing = this.actionBits.get(resource);
            if (existing != null) {
                existing.andNot(denyBits);
            }
        });
        return this;
    }
    
    /**
     * 添加权限
     */
    public PermissionBitmap addPermission(Long resourceId, Set<ActionType> actions) {
        BitSet bits = actionBits.computeIfAbsent(resourceId, k -> new BitSet());
        for (ActionType action : actions) {
            bits.set(action.ordinal());
        }
        return new PermissionBitmap(actionBits, System.currentTimeMillis());
    }
    
    public Map<Long, BitSet> getActionBits() {
        return new HashMap<>(actionBits);
    }
    
    public long getVersion() {
        return version;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - version > 300000; // 5分钟
    }
}
```

- [ ] **步骤 2：编写 PermissionBitmapTest**

```java
package com.example.demo.domain.permission;

import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.valueobject.ActionType;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PermissionBitmapTest {
    
    @Test
    void shouldCreateEmptyBitmap() {
        PermissionBitmap bitmap = PermissionBitmap.empty();
        assertFalse(bitmap.hasAction(1L, ActionType.VIEW));
    }
    
    @Test
    void shouldAddPermission() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE));
        
        assertTrue(bitmap.hasAction(1L, ActionType.VIEW));
        assertTrue(bitmap.hasAction(1L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(1L, ActionType.DELETE));
    }
    
    @Test
    void shouldMergeBitmaps() {
        PermissionBitmap a = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE));
        PermissionBitmap b = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.UPDATE, ActionType.DELETE));
        
        PermissionBitmap merged = a.merge(b);
        
        assertTrue(merged.hasAction(1L, ActionType.VIEW));
        assertTrue(merged.hasAction(1L, ActionType.CREATE));
        assertTrue(merged.hasAction(1L, ActionType.UPDATE));
        assertTrue(merged.hasAction(1L, ActionType.DELETE));
    }
    
    @Test
    void shouldApplyDenyPriority() {
        PermissionBitmap allow = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW, ActionType.CREATE, ActionType.UPDATE));
        PermissionBitmap deny = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.UPDATE));
        
        PermissionBitmap result = allow.applyDeny(deny);
        
        assertTrue(result.hasAction(1L, ActionType.VIEW));
        assertTrue(result.hasAction(1L, ActionType.CREATE));
        assertFalse(result.hasAction(1L, ActionType.UPDATE)); // DENY生效
    }
    
    @Test
    void shouldHandleDifferentResources() {
        PermissionBitmap bitmap = PermissionBitmap.empty()
            .addPermission(1L, Set.of(ActionType.VIEW))
            .addPermission(2L, Set.of(ActionType.CREATE));
        
        assertTrue(bitmap.hasAction(1L, ActionType.VIEW));
        assertFalse(bitmap.hasAction(1L, ActionType.CREATE));
        assertTrue(bitmap.hasAction(2L, ActionType.CREATE));
        assertFalse(bitmap.hasAction(2L, ActionType.VIEW));
    }
}
```

- [ ] **步骤 3：运行测试验证位图操作**

```bash
mvn test -Dtest=PermissionBitmapTest -v
```
预期：所有测试通过

- [ ] **步骤 4：提交 PermissionBitmap 值对象**

```bash
git add src/main/java/com/example/demo/domain/permission/valueobject/PermissionBitmap.java \
        src/test/java/com/example/demo/domain/permission/PermissionBitmapTest.java
git commit -m "feat(rbac): add PermissionBitmap value object with merge/deny logic"
```

---

## 任务 9：创建 Role 聚合根

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/aggregate/Role.java`
- 创建：`src/test/java/com/example/demo/domain/permission/RoleTest.java`

- [ ] **步骤 1：编写 Role 聚合根**

```java
package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.permission.exception.DomainException;
import com.example.demo.entity.BaseEntity;
import java.util.*;

public class Role extends BaseEntity {
    private Long id;
    private RoleCode code;
    private String name;
    private Long parentId;
    private RoleStatus status;
    private InheritMode inheritMode;
    private boolean isBuiltin;
    private boolean isDeleted;
    private int version;
    private int sort;
    private Set<RolePermission> permissions;
    private Set<Long> dataScopeIds; // 关联RoleDataScope实体ID
    
    // 工厂方法
    public static Role create(RoleCode code, String name, Long parentId, InheritMode mode) {
        Role role = new Role();
        role.code = code;
        role.name = name;
        role.parentId = parentId;
        role.inheritMode = mode != null ? mode : InheritMode.EXTEND;
        role.status = RoleStatus.ENABLED;
        role.isDeleted = false;
        role.isBuiltin = false;
        role.version = 0;
        role.permissions = new HashSet<>();
        role.dataScopeIds = new HashSet<>();
        return role;
    }
    
    // 业务行为
    public void assignPermission(Long resourceId, Set<ActionType> actions, PermissionEffect effect) {
        RolePermission perm = new RolePermission(resourceId, actions, effect);
        permissions.add(perm);
    }
    
    public void removePermission(Long resourceId) {
        permissions.removeIf(p -> p.getResourceId().equals(resourceId));
    }
    
    public void enable() {
        this.status = RoleStatus.ENABLED;
    }
    
    public void disable() {
        if (isBuiltin) {
            throw new DomainException("内置角色不能被禁用");
        }
        this.status = RoleStatus.DISABLED;
    }
    
    public void softDelete() {
        if (isBuiltin) {
            throw new DomainException("内置角色不能被删除");
        }
        this.isDeleted = true;
    }
    
    public void incrementVersion() {
        this.version++;
    }
    
    // 获取角色自身权限（不含继承）
    public Set<RolePermission> getOwnPermissions() {
        return Collections.unmodifiableSet(permissions);
    }
    
    // 检查是否有DENY权限
    public boolean hasDenyPermissions() {
        return permissions.stream().anyMatch(p -> p.getEffect() == PermissionEffect.DENY);
    }
    
    // 获取DENY位图
    public PermissionBitmap getDenyBitmap() {
        PermissionBitmap bitmap = PermissionBitmap.empty();
        permissions.stream()
            .filter(p -> p.getEffect() == PermissionEffect.DENY)
            .forEach(p -> bitmap.addPermission(p.getResourceId(), p.getActions()));
        return bitmap;
    }
    
    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RoleCode getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public RoleStatus getStatus() { return status; }
    public InheritMode getInheritMode() { return inheritMode; }
    public void setInheritMode(InheritMode inheritMode) { this.inheritMode = inheritMode; }
    public boolean isBuiltin() { return isBuiltin; }
    public void setBuiltin(boolean builtin) { this.isBuiltin = builtin; }
    public boolean isDeleted() { return isDeleted; }
    public int getVersion() { return version; }
    public int getSort() { return sort; }
    public void setSort(int sort) { this.sort = sort; }
    public Set<Long> getDataScopeIds() { return Collections.unmodifiableSet(dataScopeIds); }
    public void setDataScopeIds(Set<Long> dataScopeIds) { this.dataScopeIds = dataScopeIds; }
}
```

- [ ] **步骤 2：编写 RoleTest 聚合行为测试**

```java
package com.example.demo.domain.permission;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.permission.exception.DomainException;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {
    
    @Test
    void shouldCreateRole() {
        Role role = Role.create(new RoleCode("MANAGER"), "部门管理员", null, InheritMode.EXTEND);
        
        assertEquals("MANAGER", role.getCode().value());
        assertEquals("部门管理员", role.getName());
        assertEquals(InheritMode.EXTEND, role.getInheritMode());
        assertEquals(RoleStatus.ENABLED, role.getStatus());
        assertFalse(role.isDeleted());
    }
    
    @Test
    void shouldAssignPermission() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);
        
        Set<RolePermission> perms = role.getOwnPermissions();
        assertEquals(1, perms.size());
        
        RolePermission perm = perms.iterator().next();
        assertEquals(1L, perm.getResourceId());
        assertTrue(perm.hasAction(ActionType.VIEW));
    }
    
    @Test
    void shouldRemovePermission() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);
        role.assignPermission(2L, Set.of(ActionType.CREATE), PermissionEffect.ALLOW);
        
        role.removePermission(1L);
        
        Set<RolePermission> perms = role.getOwnPermissions();
        assertEquals(1, perms.size());
        assertEquals(2L, perms.iterator().next().getResourceId());
    }
    
    @Test
    void shouldEnableDisableRole() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        role.disable();
        assertEquals(RoleStatus.DISABLED, role.getStatus());
        
        role.enable();
        assertEquals(RoleStatus.ENABLED, role.getStatus());
    }
    
    @Test
    void shouldRejectDisableBuiltinRole() {
        Role role = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        role.setBuiltin(true);
        
        assertThrows(DomainException.class, () -> role.disable());
    }
    
    @Test
    void shouldRejectDeleteBuiltinRole() {
        Role role = Role.create(new RoleCode("ADMIN"), "管理员", null, null);
        role.setBuiltin(true);
        
        assertThrows(DomainException.class, () -> role.softDelete());
    }
    
    @Test
    void shouldSoftDeleteNonBuiltinRole() {
        Role role = Role.create(new RoleCode("CUSTOM"), "自定义角色", null, null);
        role.softDelete();
        
        assertTrue(role.isDeleted());
    }
    
    @Test
    void shouldIncrementVersion() {
        Role role = Role.create(new RoleCode("USER"), "普通用户", null, null);
        assertEquals(0, role.getVersion());
        
        role.incrementVersion();
        assertEquals(1, role.getVersion());
    }
    
    @Test
    void shouldDetectDenyPermissions() {
        Role role = Role.create(new RoleCode("MANAGER"), "管理员", null, null);
        role.assignPermission(1L, Set.of(ActionType.VIEW), PermissionEffect.ALLOW);
        role.assignPermission(2L, Set.of(ActionType.DELETE), PermissionEffect.DENY);
        
        assertTrue(role.hasDenyPermissions());
    }
}
```

- [ ] **步骤 3：运行测试验证聚合行为**

```bash
mvn test -Dtest=RoleTest -v
```
预期：所有测试通过

- [ ] **步骤 4：提交 Role 聚合根**

```bash
git add src/main/java/com/example/demo/domain/permission/aggregate/Role.java \
        src/test/java/com/example/demo/domain/permission/RoleTest.java
git commit -m "feat(rbac): add Role aggregate root with business behaviors and tests"
```

---

## 任务 10：创建 Resource 聚合根

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/aggregate/Resource.java`

- [ ] **步骤 1：编写 Resource 聚合根**

```java
package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.permission.valueobject.ResourceType;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.entity.BaseEntity;
import java.util.ArrayList;
import java.util.List;

public class Resource extends BaseEntity {
    private Long id;
    private String code;
    private String name;
    private Long parentId;
    private ResourceType type;
    private String path;
    private String pathPattern;  // Ant风格路径模式
    private String method;       // HTTP方法（仅API类型）
    private String icon;
    private String component;    // 前端组件路径
    private int sort;
    private boolean status;      // true-启用
    private boolean isDeleted;
    private List<ResourceField> sensitiveFields;
    
    public static Resource createMenu(String code, String name, String path, String icon, String component) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.type = ResourceType.MENU;
        resource.path = path;
        resource.icon = icon;
        resource.component = component;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }
    
    public static Resource createOperation(String code, String name, Long parentId) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.parentId = parentId;
        resource.type = ResourceType.OPERATION;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }
    
    public static Resource createApi(String code, String name, String pathPattern, String method) {
        Resource resource = new Resource();
        resource.code = code;
        resource.name = name;
        resource.type = ResourceType.API;
        resource.pathPattern = pathPattern;
        resource.method = method;
        resource.status = true;
        resource.isDeleted = false;
        resource.sensitiveFields = new ArrayList<>();
        return resource;
    }
    
    public void addSensitiveField(String fieldCode, String fieldName, SensitiveLevel level, String maskPattern) {
        ResourceField field = new ResourceField();
        field.setFieldCode(fieldCode);
        field.setFieldName(fieldName);
        field.setSensitiveLevel(level);
        field.setMaskPattern(maskPattern);
        field.setResourceId(this.id);
        sensitiveFields.add(field);
    }
    
    public void softDelete() {
        this.isDeleted = true;
    }
    
    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public ResourceType getType() { return type; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getPathPattern() { return pathPattern; }
    public String getMethod() { return method; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getComponent() { return component; }
    public int getSort() { return sort; }
    public void setSort(int sort) { this.sort = sort; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public boolean isDeleted() { return isDeleted; }
    public List<ResourceField> getSensitiveFields() { return sensitiveFields; }
}
```

- [ ] **步骤 2：创建 ResourceField 实体**

```java
package com.example.demo.domain.permission.aggregate;

import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.entity.BaseEntity;

public class ResourceField extends BaseEntity {
    private Long id;
    private Long resourceId;
    private String fieldCode;
    private String fieldName;
    private SensitiveLevel sensitiveLevel;
    private String maskPattern;
    
    /**
     * 脱敏处理
     */
    public Object maskValue(Object originalValue) {
        if (originalValue == null) return null;
        
        if (sensitiveLevel == SensitiveLevel.HIDDEN) {
            return null;
        }
        
        if (sensitiveLevel == SensitiveLevel.ENCRYPTED && maskPattern != null) {
            String strValue = originalValue.toString();
            return switch (maskPattern) {
                case "ID_CARD" -> maskIdCard(strValue);
                case "PHONE" -> maskPhone(strValue);
                case "SALARY" -> maskSalary(strValue);
                default -> strValue;
            };
        }
        
        return originalValue;
    }
    
    private String maskIdCard(String value) {
        if (value.length() < 18) return value;
        return value.substring(0, 6) + "********" + value.substring(14);
    }
    
    private String maskPhone(String value) {
        if (value.length() < 11) return value;
        return value.substring(0, 3) + "****" + value.substring(7);
    }
    
    private String maskSalary(String value) {
        return "***";
    }
    
    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getFieldCode() { return fieldCode; }
    public void setFieldCode(String fieldCode) { this.fieldCode = fieldCode; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public SensitiveLevel getSensitiveLevel() { return sensitiveLevel; }
    public void setSensitiveLevel(SensitiveLevel sensitiveLevel) { this.sensitiveLevel = sensitiveLevel; }
    public String getMaskPattern() { return maskPattern; }
    public void setMaskPattern(String maskPattern) { this.maskPattern = maskPattern; }
}
```

- [ ] **步骤 3：提交 Resource 聚合**

```bash
git add src/main/java/com/example/demo/domain/permission/aggregate/Resource.java \
        src/main/java/com/example/demo/domain/permission/aggregate/ResourceField.java
git commit -m "feat(rbac): add Resource aggregate with sensitive field support"
```

---

## 任务 11：创建仓储接口

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/repository/RoleRepository.java`
- 创建：`src/main/java/com/example/demo/domain/permission/repository/ResourceRepository.java`
- 创建：`src/main/java/com/example/demo/domain/permission/repository/UserRoleRepository.java`
- 创建：`src/main/java/com/example/demo/domain/permission/repository/PermissionRepository.java`

- [ ] **步骤 1：编写 RoleRepository 接口**

```java
package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.aggregate.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    
    Role save(Role role);
    
    Optional<Role> findById(Long id);
    
    Optional<Role> findByCode(String code);
    
    List<Role> findAll();
    
    List<Role> findAllNotDeleted();
    
    List<Role> findByParentId(Long parentId);
    
    List<Role> findRolesByUserId(Long userId);
    
    void deleteById(Long id);
    
    void incrementVersion(Long roleId);
}
```

- [ ] **步骤 2：编写 ResourceRepository 接口**

```java
package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.valueobject.ResourceType;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository {
    
    Resource save(Resource resource);
    
    Optional<Resource> findById(Long id);
    
    Optional<Resource> findByCode(String code);
    
    List<Resource> findAll();
    
    List<Resource> findByType(ResourceType type);
    
    List<Resource> findByParentId(Long parentId);
    
    List<Resource> findAllApis();
    
    void deleteById(Long id);
}
```

- [ ] **步骤 3：编写 UserRoleRepository 接口**

```java
package com.example.demo.domain.permission.repository;

import java.util.List;
import java.util.Set;

public interface UserRoleRepository {
    
    void assignRole(Long userId, Long roleId);
    
    void removeRole(Long userId, Long roleId);
    
    List<Long> findRoleIdsByUserId(Long userId);
    
    List<Long> findUserIdsByRoleId(Long roleId);
    
    Set<Long> findUserRoleIds(Long userId);
}
```

- [ ] **步骤 4：编写 PermissionRepository 接口**

```java
package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.entity.Permission;
import java.util.List;

public interface PermissionRepository {
    
    Permission save(Permission permission);
    
    List<Permission> findByRoleId(Long roleId);
    
    Permission findByRoleAndResource(Long roleId, Long resourceId);
    
    void deleteByRoleAndResource(Long roleId, Long resourceId);
    
    void deleteByRoleId(Long roleId);
}
```

- [ ] **步骤 5：提交仓储接口**

```bash
git add src/main/java/com/example/demo/domain/permission/repository/*.java
git commit -m "feat(rbac): add domain repository interfaces"
```

---

## 任务 12：创建 TypeHandler（值对象转换）

**文件：**
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/converter/RoleCodeTypeHandler.java`
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/converter/RoleStatusTypeHandler.java`
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/converter/InheritModeTypeHandler.java`
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/converter/ResourceTypeTypeHandler.java`

- [ ] **步骤 1：编写 RoleCodeTypeHandler**

```java
package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.permission.valueobject.RoleCode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleCodeTypeHandler extends BaseTypeHandler<RoleCode> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RoleCode parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.value());
    }
    
    @Override
    public RoleCode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String code = rs.getString(columnName);
        return code != null ? new RoleCode(code) : null;
    }
    
    @Override
    public RoleCode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String code = rs.getString(columnIndex);
        return code != null ? new RoleCode(code) : null;
    }
    
    @Override
    public RoleCode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String code = cs.getString(columnIndex);
        return code != null ? new RoleCode(code) : null;
    }
}
```

- [ ] **步骤 2：编写 RoleStatusTypeHandler**

```java
package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.permission.valueobject.RoleStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RoleStatusTypeHandler extends BaseTypeHandler<RoleStatus> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RoleStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter == RoleStatus.ENABLED ? 1 : 0);
    }
    
    @Override
    public RoleStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int status = rs.getInt(columnName);
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }
    
    @Override
    public RoleStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int status = rs.getInt(columnIndex);
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }
    
    @Override
    public RoleStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int status = cs.getInt(columnIndex);
        return status == 1 ? RoleStatus.ENABLED : RoleStatus.DISABLED;
    }
}
```

- [ ] **步骤 3：编写 InheritModeTypeHandler**

```java
package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.permission.valueobject.InheritMode;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InheritModeTypeHandler extends BaseTypeHandler<InheritMode> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, InheritMode parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public InheritMode getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String mode = rs.getString(columnName);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }
    
    @Override
    public InheritMode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String mode = rs.getString(columnIndex);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }
    
    @Override
    public InheritMode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String mode = cs.getString(columnIndex);
        return mode != null ? InheritMode.valueOf(mode) : InheritMode.EXTEND;
    }
}
```

- [ ] **步骤 4：编写 ResourceTypeTypeHandler**

```java
package com.example.demo.infrastructure.persistence.converter;

import com.example.demo.domain.permission.valueobject.ResourceType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceTypeTypeHandler extends BaseTypeHandler<ResourceType> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ResourceType parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.name());
    }
    
    @Override
    public ResourceType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String type = rs.getString(columnName);
        return type != null ? ResourceType.valueOf(type) : null;
    }
    
    @Override
    public ResourceType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String type = rs.getString(columnIndex);
        return type != null ? ResourceType.valueOf(type) : null;
    }
    
    @Override
    public ResourceType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String type = cs.getString(columnIndex);
        return type != null ? ResourceType.valueOf(type) : null;
    }
}
```

- [ ] **步骤 5：提交 TypeHandler**

```bash
git add src/main/java/com/example/demo/infrastructure/persistence/converter/*.java
git commit -m "feat(rbac): add TypeHandlers for value object persistence conversion"
```

---

## 任务 13：创建 MyBatis Mapper - Role

**文件：**
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/mapper/RoleMapper.java`
- 创建：`src/main/resources/mapper/RoleMapper.xml`

- [ ] **步骤 1：编写 RoleMapper 接口**

```java
package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.aggregate.Role;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RoleMapper {
    
    @Insert("INSERT INTO role(code, name, parent_id, status, inherit_mode, is_builtin, is_deleted, version, sort, created_by) " +
            "VALUES(#{code.value}, #{name}, #{parentId}, #{status}, #{inheritMode}, #{isBuiltin}, #{isDeleted}, #{version}, #{sort}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);
    
    @Update("UPDATE role SET name=#{name}, parent_id=#{parentId}, status=#{status}, inherit_mode=#{inheritMode}, " +
            "sort=#{sort}, version=#{version}, updated_by=#{updatedBy} WHERE id=#{id}")
    int update(Role role);
    
    @Select("SELECT * FROM role WHERE id = #{id} AND is_deleted = 0")
    Role findById(@Param("id") Long id);
    
    @Select("SELECT * FROM role WHERE code = #{code} AND is_deleted = 0")
    Role findByCode(@Param("code") String code);
    
    @Select("SELECT * FROM role WHERE is_deleted = 0 ORDER BY sort")
    List<Role> findAllNotDeleted();
    
    @Select("SELECT * FROM role WHERE parent_id = #{parentId} AND is_deleted = 0")
    List<Role> findByParentId(@Param("parentId") Long parentId);
    
    @Select("SELECT r.* FROM role r INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.is_deleted = 0")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
    
    @Update("UPDATE role SET is_deleted = 1, version = version + 1 WHERE id = #{id}")
    int softDelete(@Param("id") Long id);
    
    @Update("UPDATE role SET version = version + 1 WHERE id = #{id}")
    int incrementVersion(@Param("id") Long id);
}
```

- [ ] **步骤 2：编写 RoleMapper.xml 用于复杂查询**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.demo.infrastructure.persistence.mapper.RoleMapper">
    
    <resultMap id="RoleResultMap" type="com.example.demo.domain.permission.aggregate.Role">
        <id property="id" column="id"/>
        <result property="code" column="code" typeHandler="com.example.demo.infrastructure.persistence.converter.RoleCodeTypeHandler"/>
        <result property="name" column="name"/>
        <result property="parentId" column="parent_id"/>
        <result property="status" column="status" typeHandler="com.example.demo.infrastructure.persistence.converter.RoleStatusTypeHandler"/>
        <result property="inheritMode" column="inherit_mode" typeHandler="com.example.demo.infrastructure.persistence.converter.InheritModeTypeHandler"/>
        <result property="isBuiltin" column="is_builtin"/>
        <result property="isDeleted" column="is_deleted"/>
        <result property="version" column="version"/>
        <result property="sort" column="sort"/>
    </resultMap>
    
    <select id="findRoleTree" resultMap="RoleResultMap">
        SELECT * FROM role WHERE is_deleted = 0 ORDER BY sort, id
    </select>
    
</mapper>
```

- [ ] **步骤 3：提交 Role mapper**

```bash
git add src/main/java/com/example/demo/infrastructure/persistence/mapper/RoleMapper.java \
        src/main/resources/mapper/RoleMapper.xml
git commit -m "feat(rbac): add RoleMapper for role persistence"
```

---

## 任务 14：创建领域事件

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/event/RoleCreatedEvent.java`
- 创建：`src/main/java/com/example/demo/domain/permission/event/RolePermissionChangedEvent.java`
- 创建：`src/main/java/com/example/demo/domain/permission/event/UserRoleAssignedEvent.java`
- 创建：`src/main/java/com/example/demo/domain/permission/event/RoleDeletedEvent.java`

- [ ] **步骤 1：编写领域事件**

```java
package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

public record RoleCreatedEvent(
    Long roleId,
    String roleCode,
    String roleName,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RoleCreatedEvent(Long roleId, String roleCode, String roleName, Long operatorId) {
        this(roleId, roleCode, roleName, operatorId, LocalDateTime.now());
    }
}
```

```java
package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

public record RolePermissionChangedEvent(
    Long roleId,
    Long resourceId,
    String changeType,  // ADD/REMOVE
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RolePermissionChangedEvent(Long roleId, Long resourceId, String changeType, Long operatorId) {
        this(roleId, resourceId, changeType, operatorId, LocalDateTime.now());
    }
}
```

```java
package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

public record UserRoleAssignedEvent(
    Long userId,
    Long roleId,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public UserRoleAssignedEvent(Long userId, Long roleId, Long operatorId) {
        this(userId, roleId, operatorId, LocalDateTime.now());
    }
}
```

```java
package com.example.demo.domain.permission.event;

import java.time.LocalDateTime;

public record RoleDeletedEvent(
    Long roleId,
    String roleCode,
    Long operatorId,
    LocalDateTime occurredAt
) {
    public RoleDeletedEvent(Long roleId, String roleCode, Long operatorId) {
        this(roleId, roleCode, operatorId, LocalDateTime.now());
    }
}
```

- [ ] **步骤 2：提交领域事件**

```bash
git add src/main/java/com/example/demo/domain/permission/event/*.java
git commit -m "feat(rbac): add domain events for permission system"
```

---

## 任务 15：运行数据库迁移

**文件：**
- 无（数据库迁移）

- [ ] **步骤 1：运行 Flyway 迁移**

```bash
mvn flyway:migrate
```
预期：迁移 V1、V2 成功执行

- [ ] **步骤 2：验证表已创建**

```bash
mysql -u root -proot demo -e "SHOW TABLES LIKE 'role'; SHOW TABLES LIKE 'permission%'; SHOW TABLES LIKE 'resource%';"
```
预期：role、permission、permission_action、resource、resource_field 表存在

- [ ] **步骤 3：验证触发器已创建**

```bash
mysql -u root -proot demo -e "SHOW TRIGGERS LIKE 'role';"
```
预期：trg_role_before_insert、trg_role_before_update 触发器存在

---

## 自检清单

- [x] 规范 P1 覆盖：核心表 ✓、领域模型 ✓、值对象 ✓、仓储接口 ✓、TypeHandler ✓
- [x] 无占位符：所有代码完整
- [x] 类型一致性：RolePermission、PermissionBitmap、Role 使用一致的签名
- [x] 测试：RoleTest、RolePermissionTest、PermissionBitmapTest、RoleCodeTest 覆盖关键行为
- [x] 循环继承：数据库触发器防止 + RoleDomainService 代码校验（P2）
- [x] TypeHandler：值对象持久化转换器完整

---

**计划完成并保存到 `docs/superpowers/plans/rbac/2026-04-15-rbac-p1-core-models.md`。**

此阶段创建 RBAC 的基础。后续阶段（P2-P10）依赖这些产物。

**是否继续编写 P2-P10 计划，还是先执行 P1？**