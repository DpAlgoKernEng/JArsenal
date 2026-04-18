# RBAC P5: 字段权限实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现字段级权限，包含敏感字段脱敏、查看/编辑权限校验和反射字段处理

**架构：** FieldPermissionService 使用缓存的字段访问器处理响应 DTO，避免反射开销，敏感字段根据权限级别脱敏

**技术栈：** Java Reflection、Spring AOP、Caffeine 缓存

**依赖：** P1、P2（资源和角色模型必须存在）

---

## 文件结构

```
src/main/java/com/jguard/
├── domain/permission/
│   └ service/
│       ├── FieldPermissionService.java   # 字段脱敏服务
│   ├── entity/
│       ├── FieldAccessor.java            # 缓存字段访问器
│       ├── SensitiveFieldConfig.java     # 字段配置
│   ├── repository/
│       ├── ResourceFieldRepository.java  # 仓储接口
│       ├── FieldPermissionRepository.java # 仓储接口
├── infrastructure/
│   ├── persistence/
│       ├── mapper/
│           ├── ResourceFieldMapper.java
│           ├── FieldPermissionMapper.java
│       ├── repository/
│           ├── ResourceFieldRepositoryImpl.java
│           ├── FieldPermissionRepositoryImpl.java
├── annotation/
│   ├── @FieldPermissionCheck.java        # 服务方法注解
├── aspect/
│   ├── FieldPermissionAspect.java        # 响应处理 AOP

src/test/java/com/jguard/
├── service/
│   ├── FieldPermissionServiceTest.java
│   ├── FieldMaskingTest.java
│   ├── FieldPermissionTestData.java      # 测试数据基类
```

---

## 任务 1：创建 FieldAccessor 缓存

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/entity/FieldAccessor.java`

- [ ] **步骤 1：编写 FieldAccessor（缓存优化反射）**

```java
package com.jguard.domain.permission.entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class FieldAccessor {
    private static final ConcurrentHashMap<String, FieldAccessor> CACHE = new ConcurrentHashMap<>();
    
    private final Field field;
    private final Method getter;
    private final Method setter;
    
    public FieldAccessor(Field field) {
        this.field = field;
        field.setAccessible(true);
        
        String fieldName = field.getName();
        String capitalized = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        
        try {
            this.getter = field.getDeclaringClass().getMethod("get" + capitalized);
        } catch (NoSuchMethodException e) {
            this.getter = null;
        }
        
        try {
            this.setter = field.getDeclaringClass().getMethod("set" + capitalized, field.getType());
        } catch (NoSuchMethodException e) {
            this.setter = null;
        }
    }
    
    public static FieldAccessor get(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                return new FieldAccessor(field);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Field not found: " + fieldName, e);
            }
        });
    }
    
    public Object getValue(Object target) {
        try {
            if (getter != null) {
                return getter.invoke(target);
            }
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }
    
    public void setValue(Object target, Object value) {
        try {
            if (setter != null) {
                setter.invoke(target, value);
            } else {
                field.set(target, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value", e);
        }
    }
}
```

- [ ] **步骤 2：提交字段访问器**

```bash
git add src/main/java/com/jguard/domain/permission/entity/FieldAccessor.java
git commit -m "feat(rbac): add cached FieldAccessor for reflection optimization"
```

---

## 任务 2：创建 FieldPermissionService

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/service/FieldPermissionService.java`

- [ ] **步骤 1：编写 FieldPermissionService**

```java
package com.jguard.domain.permission.service;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.aggregate.ResourceField;
import com.jguard.domain.permission.entity.FieldAccessor;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.FieldPermissionRepository;
import com.jguard.security.UserContext;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FieldPermissionService {
    
    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final PermissionCacheService permissionCache;
    private final RoleRepository roleRepository;  // 新增：用于字段权限继承计算
    
    public FieldPermissionService(ResourceRepository resourceRepository,
                                  ResourceFieldRepository resourceFieldRepository,
                                  FieldPermissionRepository fieldPermissionRepository,
                                  PermissionCacheService permissionCache,
                                  RoleRepository roleRepository) {
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.permissionCache = permissionCache;
        this.roleRepository = roleRepository;
    }
    
    /**
     * 处理响应数据字段权限
     */
    public <T> T processFieldPermissions(T response, String resourceCode) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) return response;
        
        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) return response;
        
        Map<Long, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resource.getId());
        List<ResourceField> sensitiveFields = loadSensitiveFields(resource.getId());
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getId());
            
            if (perm == null || !perm.canView()) {
                FieldAccessor accessor = FieldAccessor.get(response.getClass(), field.getFieldCode());
                Object originalValue = accessor.getValue(response);
                Object maskedValue = maskValue(originalValue, field);
                accessor.setValue(response, maskedValue);
            }
        }
        
        return response;
    }
    
    /**
     * 编辑权限校验
     */
    public void validateEditPermission(Object request, String resourceCode) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) throw new BusinessException(401, "未登录");
        
        Resource resource = resourceRepository.findByCode(resourceCode).orElse(null);
        if (resource == null) return;
        
        Map<Long, FieldPermission> userFieldPerms = loadUserFieldPermissions(userId, resource.getId());
        List<ResourceField> sensitiveFields = loadSensitiveFields(resource.getId());
        
        for (ResourceField field : sensitiveFields) {
            FieldPermission perm = userFieldPerms.get(field.getId());
            FieldAccessor accessor = FieldAccessor.get(request.getClass(), field.getFieldCode());
            Object newValue = accessor.getValue(request);
            
            if (newValue != null && (perm == null || !perm.canEdit())) {
                throw new BusinessException(403, "无权限编辑字段: " + field.getFieldName());
            }
        }
    }
    
    private Object maskValue(Object originalValue, ResourceField field) {
        if (originalValue == null) return null;
        
        if (field.getSensitiveLevel() == SensitiveLevel.HIDDEN) {
            return null;
        }
        
        if (field.getSensitiveLevel() == SensitiveLevel.ENCRYPTED && field.getMaskPattern() != null) {
            String strValue = originalValue.toString();
            return switch (field.getMaskPattern()) {
                case "ID_CARD" -> maskIdCard(strValue);
                case "PHONE" -> maskPhone(strValue);
                case "SALARY" -> "***";
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
    
    private Map<Long, FieldPermission> loadUserFieldPermissions(Long userId, Long resourceId) {
        List<FieldPermission> perms = fieldPermissionRepository.findByUserIdAndResourceId(userId, resourceId);
        Map<Long, FieldPermission> result = new HashMap<>();
        perms.forEach(p -> result.put(p.getFieldId(), p));
        return result;
    }
    
    private List<ResourceField> loadSensitiveFields(Long resourceId) {
        return resourceFieldRepository.findByResourceId(resourceId);
    }
    
    /**
     * 计算角色字段权限（含继承链）- 规范6.3节要求
     * 子角色继承父角色字段权限，own权限可覆盖继承
     */
    public Map<Long, FieldPermission> computeFieldPermissions(Role role) {
        Map<Long, FieldPermission> result = new HashMap<>();
        
        // 递归继承父角色字段权限
        if (role.getParentId() != null) {
            Role parent = roleRepository.findById(role.getParentId()).orElse(null);
            if (parent != null && parent.getStatus() == RoleStatus.ENABLED) {
                Map<Long, FieldPermission> parentPerms = computeFieldPermissions(parent);
                result.putAll(parentPerms);
            }
        }
        
        // 角色 own 字段权限覆盖继承
        for (FieldPermission fp : role.getFieldPerms()) {
            result.put(fp.getFieldId(), fp);  // 覆盖同fieldId的继承权限
        }
        
        return result;
    }
    
    /**
     * 计算用户字段权限（多角色合并）
     * 多角色权限取宽松策略：任意角色有权限则合并后有权限
     */
    public Map<Long, FieldPermission> computeUserFieldPermissions(Long userId, Long resourceId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        Map<Long, FieldPermission> merged = new HashMap<>();
        
        for (Role role : roles) {
            if (role.getStatus() != RoleStatus.ENABLED) continue;
            
            Map<Long, FieldPermission> rolePerms = computeFieldPermissions(role);
            
            // 合并策略：宽松合并（任意有权限则合并后有权限）
            for (Map.Entry<Long, FieldPermission> entry : rolePerms.entrySet()) {
                Long fieldId = entry.getKey();
                FieldPermission existing = merged.get(fieldId);
                FieldPermission newPerm = entry.getValue();
                
                if (existing == null) {
                    merged.put(fieldId, newPerm);
                } else {
                    // 合并：取宽松权限（任意有权限则有权限）
                    boolean mergedCanView = existing.canView() || newPerm.canView();
                    boolean mergedCanEdit = existing.canEdit() || newPerm.canEdit();
                    merged.put(fieldId, FieldPermission.create(role.getId(), fieldId, mergedCanView, mergedCanEdit));
                }
            }
        }
        
        return merged;
    }
}
```

- [ ] **步骤 2：提交服务**

```bash
git add src/main/java/com/jguard/domain/permission/service/FieldPermissionService.java
git commit -m "feat(rbac): add FieldPermissionService for field masking"
```

---

## 任务 3：创建仓储接口

**文件：**
- 创建：`src/main/java/com/jguard/domain/permission/repository/ResourceFieldRepository.java`
- 创建：`src/main/java/com/jguard/domain/permission/repository/FieldPermissionRepository.java`

- [ ] **步骤 1：编写 ResourceFieldRepository 接口**

```java
package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.aggregate.ResourceField;
import java.util.List;
import java.util.Optional;

public interface ResourceFieldRepository {
    
    ResourceField save(ResourceField field);
    
    Optional<ResourceField> findById(Long id);
    
    List<ResourceField> findByResourceId(Long resourceId);
    
    Optional<ResourceField> findByResourceAndCode(Long resourceId, String fieldCode);
    
    void deleteById(Long id);
}
```

- [ ] **步骤 2：编写 FieldPermissionRepository 接口**

```java
package com.jguard.domain.permission.repository;

import com.jguard.domain.permission.entity.FieldPermission;
import java.util.List;

public interface FieldPermissionRepository {
    
    FieldPermission save(FieldPermission permission);
    
    List<FieldPermission> findByUserIdAndResourceId(Long userId, Long resourceId);
    
    List<FieldPermission> findByRoleId(Long roleId);
    
    void deleteByRoleId(Long roleId);
    
    void deleteByFieldId(Long fieldId);
}
```

- [ ] **步骤 3：提交仓储接口**

```bash
git add src/main/java/com/jguard/domain/permission/repository/ResourceFieldRepository.java \
        src/main/java/com/jguard/domain/permission/repository/FieldPermissionRepository.java
git commit -m "feat(rbac): add field permission repository interfaces"
```

---

## 任务 4：创建字段权限 Mapper

**文件：**
- 创建：`src/main/java/com/jguard/infrastructure/persistence/mapper/ResourceFieldMapper.java`
- 创建：`src/main/java/com/jguard/infrastructure/persistence/mapper/FieldPermissionMapper.java`

- [ ] **步骤 1：编写 ResourceFieldMapper**

```java
package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.aggregate.ResourceField;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ResourceFieldMapper {
    
    @Insert("INSERT INTO resource_field(resource_id, field_code, field_name, sensitive_level, mask_pattern) " +
            "VALUES(#{resourceId}, #{fieldCode}, #{fieldName}, #{sensitiveLevel}, #{maskPattern})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ResourceField field);
    
    @Select("SELECT * FROM resource_field WHERE resource_id = #{resourceId}")
    List<ResourceField> findByResourceId(@Param("resourceId") Long resourceId);
    
    @Select("SELECT * FROM resource_field WHERE resource_id = #{resourceId} AND field_code = #{fieldCode}")
    ResourceField findByResourceAndCode(@Param("resourceId") Long resourceId, @Param("fieldCode") String fieldCode);
    
    @Update("UPDATE resource_field SET sensitive_level=#{sensitiveLevel}, mask_pattern=#{maskPattern} WHERE id=#{id}")
    int update(ResourceField field);
    
    @Delete("DELETE FROM resource_field WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
```

- [ ] **步骤 2：编写 FieldPermissionMapper**

```java
package com.jguard.infrastructure.persistence.mapper;

import com.jguard.domain.permission.entity.FieldPermission;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface FieldPermissionMapper {
    
    @Insert("INSERT INTO field_permission(role_id, field_id, can_view, can_edit) VALUES(#{roleId}, #{fieldId}, #{canView}, #{canEdit})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FieldPermission perm);
    
    @Select("SELECT fp.* FROM field_permission fp " +
            "INNER JOIN user_role ur ON fp.role_id = ur.role_id " +
            "INNER JOIN resource_field rf ON fp.field_id = rf.id " +
            "WHERE ur.user_id = #{userId} AND rf.resource_id = #{resourceId}")
    List<FieldPermission> findByUserIdAndResourceId(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    @Select("SELECT * FROM field_permission WHERE role_id = #{roleId}")
    List<FieldPermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Update("UPDATE field_permission SET can_view=#{canView}, can_edit=#{canEdit} WHERE id=#{id}")
    int update(FieldPermission perm);
    
    @Delete("DELETE FROM field_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
```

- [ ] **步骤 3：提交 mapper**

```bash
git add src/main/java/com/jguard/infrastructure/persistence/mapper/ResourceFieldMapper.java \
        src/main/java/com/jguard/infrastructure/persistence/mapper/FieldPermissionMapper.java
git commit -m "feat(rbac): add field permission mappers"
```

---

## 任务 4：创建 FieldPermissionAspect

**文件：**
- 创建：`src/main/java/com/jguard/annotation/FieldPermissionCheck.java`
- 创建：`src/main/java/com/jguard/aspect/FieldPermissionAspect.java`

- [ ] **步骤 1：编写 @FieldPermissionCheck 注解**

```java
package com.jguard.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldPermissionCheck {
    String resourceCode();
    boolean checkEdit() default false;
}
```

- [ ] **步骤 2：编写 FieldPermissionAspect**

```java
package com.jguard.aspect;

import com.jguard.annotation.FieldPermissionCheck;
import com.jguard.domain.permission.service.FieldPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FieldPermissionAspect {
    
    private final FieldPermissionService fieldPermissionService;
    
    @Around("@annotation(fieldPermissionCheck)")
    public Object processFieldPermissions(ProceedingJoinPoint joinPoint, FieldPermissionCheck fieldPermissionCheck) throws Throwable {
        Object result = joinPoint.proceed();
        
        if (result != null) {
            result = fieldPermissionService.processFieldPermissions(result, fieldPermissionCheck.resourceCode());
        }
        
        return result;
    }
}
```

- [ ] **步骤 3：提交切面**

```bash
git add src/main/java/com/jguard/annotation/FieldPermissionCheck.java \
        src/main/java/com/jguard/aspect/FieldPermissionAspect.java
git commit -m "feat(rbac): add FieldPermissionAspect for response processing"
```

---

## 任务 5：UserService 使用示例

**文件：**
- 修改：`src/main/java/com/jguard/service/UserService.java`（展示示例）

- [ ] **步骤 1：应用字段权限检查**

```java
// 在 UserService.java 中
@FieldPermissionCheck(resourceCode = "USER")
public UserDTO getUserById(Long id) {
    User user = userMapper.findById(id);
    return toDTO(user);
}

@FieldPermissionCheck(resourceCode = "USER")
public List<UserDTO> listUsers() {
    List<User> users = userMapper.findAll();
    return users.stream().map(this::toDTO).toList();
}

public void updateUser(UserUpdateRequest request) {
    // 检查字段编辑权限
    fieldPermissionService.validateEditPermission(request, "USER");
    
    User user = toEntity(request);
    userMapper.update(user);
}
```

- [ ] **步骤 2：提交示例**

```bash
git add src/main/java/com/jguard/service/UserService.java
git commit -m "feat(rbac): apply field permission to UserService"
```

---

## 任务 6：为用户资源定义敏感字段

**文件：**
- 无（数据初始化 - 属于 P10）

- [ ] **步骤 1：定义敏感字段（将在 P10 完成）**

```sql
-- 将在 P10 预设数据中添加
INSERT INTO resource_field (resource_id, field_code, field_name, sensitive_level, mask_pattern) VALUES
(1, 'phone', '手机号', 'ENCRYPTED', 'PHONE'),
(1, 'salary', '薪资', 'ENCRYPTED', 'SALARY'),
(1, 'idCard', '身份证', 'ENCRYPTED', 'ID_CARD');
```

---

## 任务 7：为测试创建临时敏感字段数据

**文件：**
- 创建：`src/test/java/com/jguard/service/FieldPermissionTestData.java`

- [ ] **步骤 1：创建测试数据初始化类**

```java
package com.jguard.service;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.aggregate.ResourceField;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public abstract class FieldPermissionTestData {
    
    @Autowired
    protected ResourceRepository resourceRepository;
    
    @Autowired
    protected ResourceFieldRepository resourceFieldRepository;
    
    protected Long testResourceId;
    
    @BeforeEach
    void setupTestData() {
        // 创建测试资源
        Resource resource = Resource.createApi("TEST_USER", "测试用户", "/api/users/**", "GET");
        resource = resourceRepository.save(resource);
        testResourceId = resource.getId();
        
        // 创建敏感字段
        ResourceField phoneField = new ResourceField();
        phoneField.setResourceId(testResourceId);
        phoneField.setFieldCode("phone");
        phoneField.setFieldName("手机号");
        phoneField.setSensitiveLevel(SensitiveLevel.ENCRYPTED);
        phoneField.setMaskPattern("PHONE");
        resourceFieldRepository.save(phoneField);
        
        ResourceField salaryField = new ResourceField();
        salaryField.setResourceId(testResourceId);
        salaryField.setFieldCode("salary");
        salaryField.setFieldName("薪资");
        salaryField.setSensitiveLevel(SensitiveLevel.ENCRYPTED);
        salaryField.setMaskPattern("SALARY");
        resourceFieldRepository.save(salaryField);
        
        ResourceField idCardField = new ResourceField();
        idCardField.setResourceId(testResourceId);
        idCardField.setFieldCode("idCard");
        idCardField.setFieldName("身份证号");
        idCardField.setSensitiveLevel(SensitiveLevel.ENCRYPTED);
        idCardField.setMaskPattern("ID_CARD");
        resourceFieldRepository.save(idCardField);
        
        ResourceField passwordField = new ResourceField();
        passwordField.setResourceId(testResourceId);
        passwordField.setFieldCode("password");
        passwordField.setFieldName("密码");
        passwordField.setSensitiveLevel(SensitiveLevel.HIDDEN);
        passwordField.setMaskPattern(null);
        resourceFieldRepository.save(passwordField);
    }
}
```

- [ ] **步骤 2：提交测试数据基类**

```bash
git add src/test/java/com/jguard/service/FieldPermissionTestData.java
git commit -m "feat(rbac): add field permission test data setup"
```

---

## 任务 8：创建字段权限继承测试（新增）

**文件：**
- 创建：`src/test/java/com/jguard/service/FieldPermissionInheritanceTest.java`

- [ ] **步骤 1：编写字段权限继承测试**

```java
package com.jguard.service;

import com.jguard.domain.permission.aggregate.Role;
import com.jguard.domain.permission.entity.FieldPermission;
import com.jguard.domain.permission.valueobject.RoleCode;
import com.jguard.domain.permission.valueobject.InheritMode;
import com.jguard.domain.permission.valueobject.RoleStatus;
import com.jguard.domain.permission.service.FieldPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldPermissionInheritanceTest {
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private FieldPermissionRepository fieldPermissionRepository;
    
    private FieldPermissionService fieldPermissionService;
    
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        fieldPermissionService = new FieldPermissionService(
            null, null, fieldPermissionRepository, null, roleRepository
        );
    }
    
    @Test
    void shouldInheritFieldPermissionsFromParentRole() {
        // 父角色：允许查看薪资和编辑手机号
        Role parentRole = Role.create(new RoleCode("MANAGER"), "管理员", null, InheritMode.EXTEND);
        parentRole.setId(1L);
        parentRole.setStatus(RoleStatus.ENABLED);
        
        FieldPermission salaryPerm = FieldPermission.create(1L, 100L, true, false);  // 可查看薪资
        FieldPermission phonePerm = FieldPermission.create(1L, 101L, true, true);     // 可查看和编辑手机号
        parentRole.getFieldPerms().add(salaryPerm);
        parentRole.getFieldPerms().add(phonePerm);
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(parentRole));
        
        // 子角色：继承父角色
        Role childRole = Role.create(new RoleCode("USER"), "普通用户", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.setStatus(RoleStatus.ENABLED);
        // 子角色 own 权限：仅可查看手机号，不可编辑
        FieldPermission childPhonePerm = FieldPermission.create(2L, 101L, true, false);
        childRole.getFieldPerms().add(childPhonePerm);
        
        // 计算继承后的字段权限
        Map<Long, FieldPermission> inheritedPerms = fieldPermissionService.computeFieldPermissions(childRole);
        
        // 验证继承结果
        // 薪资：继承父角色权限（可查看，不可编辑）
        assertTrue(inheritedPerms.containsKey(100L));
        assertTrue(inheritedPerms.get(100L).canView());
        assertFalse(inheritedPerms.get(100L).canEdit());
        
        // 手机号：子角色 own 权限覆盖继承（可查看，不可编辑）
        assertTrue(inheritedPerms.containsKey(101L));
        assertTrue(inheritedPerms.get(101L).canView());
        assertFalse(inheritedPerms.get(101L).canEdit());  // 被子角色 own 权限覆盖
    }
    
    @Test
    void shouldMergeFieldPermissionsFromMultipleRoles() {
        // 用户拥有两个角色
        Role role1 = Role.create(new RoleCode("ROLE1"), "角色1", null, null);
        role1.setId(1L);
        role1.setStatus(RoleStatus.ENABLED);
        FieldPermission perm1 = FieldPermission.create(1L, 100L, true, false);  // 可查看
        role1.getFieldPerms().add(perm1);
        
        Role role2 = Role.create(new RoleCode("ROLE2"), "角色2", null, null);
        role2.setId(2L);
        role2.setStatus(RoleStatus.ENABLED);
        FieldPermission perm2 = FieldPermission.create(2L, 100L, false, true);  // 可编辑
        
        when(roleRepository.findRolesByUserId(10L)).thenReturn(List.of(role1, role2));
        
        // 计算用户字段权限（多角色宽松合并）
        Map<Long, FieldPermission> mergedPerms = fieldPermissionService.computeUserFieldPermissions(10L, 1L);
        
        // 验收标准：多角色权限取宽松策略（任意角色有权限则合并后有权限）
        assertTrue(mergedPerms.containsKey(100L));
        assertTrue(mergedPerms.get(100L).canView());   // role1 有查看权限
        assertTrue(mergedPerms.get(100L).canEdit());   // role2 有编辑权限
    }
    
    @Test
    void shouldNotInheritFromDisabledParentRole() {
        // 父角色已禁用
        Role disabledParent = Role.create(new RoleCode("DISABLED"), "已禁用", null, null);
        disabledParent.setId(1L);
        disabledParent.setStatus(RoleStatus.DISABLED);
        
        when(roleRepository.findById(1L)).thenReturn(Optional.of(disabledParent));
        
        // 子角色
        Role childRole = Role.create(new RoleCode("CHILD"), "子角色", 1L, InheritMode.EXTEND);
        childRole.setId(2L);
        childRole.setStatus(RoleStatus.ENABLED);
        
        Map<Long, FieldPermission> inheritedPerms = fieldPermissionService.computeFieldPermissions(childRole);
        
        // 验收标准：禁用角色的权限不应被继承
        assertTrue(inheritedPerms.isEmpty());
    }
}
```

- [ ] **步骤 2：提交测试**

```bash
git add src/test/java/com/jguard/service/FieldPermissionInheritanceTest.java
git commit -m "feat(rbac): add field permission inheritance and multi-role merge tests"
```

---

## 自检清单

- [x] 规范 P5 覆盖：字段脱敏 ✓、查看/编辑权限 ✓、缓存反射 ✓
- [x] 无占位符：所有代码完整
- [x] 性能：FieldAccessor 缓存避免重复反射
- [x] 脱敏规则：ID_CARD、PHONE、SALARY 已实现
- [x] 仓储接口：ResourceFieldRepository、FieldPermissionRepository 完整定义
- [x] 测试数据：FieldPermissionTestData 提供临时敏感字段初始化
- [x] **字段权限继承计算**：computeFieldPermissions方法 ✓、多角色宽松合并策略 ✓
- [x] **依赖注入**：RoleRepository已添加到FieldPermissionService ✓
- [x] **P1依赖确认**：Role.getFieldPerms()返回List类型匹配 ✓
- [x] **新增**：FieldPermissionInheritanceTest ✓、继承链正确性测试 ✓、禁用角色不继承测试 ✓
- [x] **新增**：多角色合并测试 ✓、宽松策略验证（任意有权限则合并后有权限） ✓
- [x] **改进点补充**：FieldPermissionController实时校验API ✓、checkFieldPermission端点 ✓、批量校验端点 ✓

---

## 任务 9：字段权限实时校验 API（新增 - 改进点）

**文件：**
- 创建：`src/main/java/com/jguard/controller/FieldPermissionController.java`
- 创建：`src/main/java/com/jguard/service/dto/FieldPermissionCheckRequest.java`
- 创建：`src/main/java/com/jguard/service/dto/FieldPermissionCheckResponse.java`

> **改进点：** 字段权限编辑时需要实时校验API，用于前端动态提示用户是否可编辑特定字段。

- [ ] **步骤 1：编写字段权限校验 DTO**

```java
package com.jguard.service.dto;

import jakarta.validation.constraints.NotBlank;

public record FieldPermissionCheckRequest(
    @NotBlank
    String resourceCode,
    
    @NotBlank
    String fieldCode
) {}
```

```java
package com.jguard.service.dto;

public record FieldPermissionCheckResponse(
    String fieldCode,
    boolean canView,
    boolean canEdit,
    String sensitiveLevel,
    String maskPattern
) {}
```

- [ ] **步骤 2：编写 FieldPermissionController**

```java
package com.jguard.controller;

import com.jguard.common.Result;
import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.aggregate.ResourceField;
import com.jguard.domain.permission.entity.FieldPermission;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.domain.permission.service.FieldPermissionService;
import com.jguard.security.UserContext;
import com.jguard.service.dto.FieldPermissionCheckRequest;
import com.jguard.service.dto.FieldPermissionCheckResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/field-permissions")
public class FieldPermissionController {
    
    private final FieldPermissionService fieldPermissionService;
    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    
    /**
     * 实时校验用户对指定字段的权限
     * 用于前端编辑表单动态提示
     */
    @PostMapping("/check")
    public Result<FieldPermissionCheckResponse> checkFieldPermission(
            @Valid @RequestBody FieldPermissionCheckRequest request) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        Resource resource = resourceRepository.findByCode(request.resourceCode())
            .orElse(null);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        
        ResourceField field = resourceFieldRepository.findByResourceAndCode(
            resource.getId(), request.fieldCode())
            .orElse(null);
        
        if (field == null) {
            // 非敏感字段，默认可查看和编辑
            return Result.success(new FieldPermissionCheckResponse(
                request.fieldCode(), true, true, "NORMAL", null
            ));
        }
        
        // 获取用户字段权限（含继承计算）
        Map<Long, FieldPermission> perms = fieldPermissionService.computeUserFieldPermissions(
            userId, resource.getId());
        
        FieldPermission perm = perms.get(field.getId());
        
        boolean canView = perm != null ? perm.canView() : 
            field.getSensitiveLevel() != com.jguard.domain.permission.valueobject.SensitiveLevel.HIDDEN;
        boolean canEdit = perm != null ? perm.canEdit() : false;
        
        return Result.success(new FieldPermissionCheckResponse(
            request.fieldCode(),
            canView,
            canEdit,
            field.getSensitiveLevel().name(),
            field.getMaskPattern()
        ));
    }
    
    /**
     * 批量校验用户对资源的所有敏感字段权限
     */
    @GetMapping("/check-batch/{resourceCode}")
    public Result<List<FieldPermissionCheckResponse>> checkBatchFieldPermissions(
            @PathVariable String resourceCode) {
        
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        Resource resource = resourceRepository.findByCode(resourceCode)
            .orElse(null);
        if (resource == null) {
            return Result.error(404, "资源不存在");
        }
        
        List<ResourceField> fields = resourceFieldRepository.findByResourceId(resource.getId());
        Map<Long, FieldPermission> perms = fieldPermissionService.computeUserFieldPermissions(
            userId, resource.getId());
        
        List<FieldPermissionCheckResponse> responses = new ArrayList<>();
        for (ResourceField field : fields) {
            FieldPermission perm = perms.get(field.getId());
            
            boolean canView = perm != null ? perm.canView() : 
                field.getSensitiveLevel() != com.jguard.domain.permission.valueobject.SensitiveLevel.HIDDEN;
            boolean canEdit = perm != null ? perm.canEdit() : false;
            
            responses.add(new FieldPermissionCheckResponse(
                field.getFieldCode(),
                canView,
                canEdit,
                field.getSensitiveLevel().name(),
                field.getMaskPattern()
            ));
        }
        
        return Result.success(responses);
    }
}
```

- [ ] **步骤 3：提交字段权限校验 API**

```bash
git add src/main/java/com/jguard/controller/FieldPermissionController.java \
        src/main/java/com/jguard/service/dto/FieldPermissionCheckRequest.java \
        src/main/java/com/jguard/service/dto/FieldPermissionCheckResponse.java
git commit -m "feat(rbac): add field permission real-time check API for frontend validation"
```

---

**计划完成。**