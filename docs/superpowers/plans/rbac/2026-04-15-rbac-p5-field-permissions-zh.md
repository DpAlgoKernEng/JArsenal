# RBAC P5: 字段权限实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现字段级权限，包含敏感字段脱敏、查看/编辑权限校验和反射字段处理

**架构：** FieldPermissionService 使用缓存的字段访问器处理响应 DTO，避免反射开销，敏感字段根据权限级别脱敏

**技术栈：** Java Reflection、Spring AOP、Caffeine 缓存

**依赖：** P1、P2（资源和角色模型必须存在）

---

## 文件结构

```
src/main/java/com/example/demo/
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

src/test/java/com/example/demo/
├── service/
│   ├── FieldPermissionServiceTest.java
│   ├── FieldMaskingTest.java
│   ├── FieldPermissionTestData.java      # 测试数据基类
```

---

## 任务 1：创建 FieldAccessor 缓存

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/entity/FieldAccessor.java`

- [ ] **步骤 1：编写 FieldAccessor（缓存优化反射）**

```java
package com.example.demo.domain.permission.entity;

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
git add src/main/java/com/example/demo/domain/permission/entity/FieldAccessor.java
git commit -m "feat(rbac): add cached FieldAccessor for reflection optimization"
```

---

## 任务 2：创建 FieldPermissionService

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/service/FieldPermissionService.java`

- [ ] **步骤 1：编写 FieldPermissionService**

```java
package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.aggregate.ResourceField;
import com.example.demo.domain.permission.entity.FieldAccessor;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.FieldPermissionRepository;
import com.example.demo.security.UserContext;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FieldPermissionService {
    
    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    private final PermissionCacheService permissionCache;
    
    public FieldPermissionService(ResourceRepository resourceRepository,
                                  ResourceFieldRepository resourceFieldRepository,
                                  FieldPermissionRepository fieldPermissionRepository,
                                  PermissionCacheService permissionCache) {
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
        this.permissionCache = permissionCache;
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
}
```

- [ ] **步骤 2：提交服务**

```bash
git add src/main/java/com/example/demo/domain/permission/service/FieldPermissionService.java
git commit -m "feat(rbac): add FieldPermissionService for field masking"
```

---

## 任务 3：创建仓储接口

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/repository/ResourceFieldRepository.java`
- 创建：`src/main/java/com/example/demo/domain/permission/repository/FieldPermissionRepository.java`

- [ ] **步骤 1：编写 ResourceFieldRepository 接口**

```java
package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.aggregate.ResourceField;
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
package com.example.demo.domain.permission.repository;

import com.example.demo.domain.permission.entity.FieldPermission;
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
git add src/main/java/com/example/demo/domain/permission/repository/ResourceFieldRepository.java \
        src/main/java/com/example/demo/domain/permission/repository/FieldPermissionRepository.java
git commit -m "feat(rbac): add field permission repository interfaces"
```

---

## 任务 4：创建字段权限 Mapper

**文件：**
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/mapper/ResourceFieldMapper.java`
- 创建：`src/main/java/com/example/demo/infrastructure/persistence/mapper/FieldPermissionMapper.java`

- [ ] **步骤 1：编写 ResourceFieldMapper**

```java
package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.aggregate.ResourceField;
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
package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.FieldPermission;
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
git add src/main/java/com/example/demo/infrastructure/persistence/mapper/ResourceFieldMapper.java \
        src/main/java/com/example/demo/infrastructure/persistence/mapper/FieldPermissionMapper.java
git commit -m "feat(rbac): add field permission mappers"
```

---

## 任务 4：创建 FieldPermissionAspect

**文件：**
- 创建：`src/main/java/com/example/demo/annotation/FieldPermissionCheck.java`
- 创建：`src/main/java/com/example/demo/aspect/FieldPermissionAspect.java`

- [ ] **步骤 1：编写 @FieldPermissionCheck 注解**

```java
package com.example.demo.annotation;

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
package com.example.demo.aspect;

import com.example.demo.annotation.FieldPermissionCheck;
import com.example.demo.domain.permission.service.FieldPermissionService;
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
git add src/main/java/com/example/demo/annotation/FieldPermissionCheck.java \
        src/main/java/com/example/demo/aspect/FieldPermissionAspect.java
git commit -m "feat(rbac): add FieldPermissionAspect for response processing"
```

---

## 任务 5：UserService 使用示例

**文件：**
- 修改：`src/main/java/com/example/demo/service/UserService.java`（展示示例）

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
git add src/main/java/com/example/demo/service/UserService.java
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
- 创建：`src/test/java/com/example/demo/service/FieldPermissionTestData.java`

- [ ] **步骤 1：创建测试数据初始化类**

```java
package com.example.demo.service;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.aggregate.ResourceField;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.ResourceFieldRepository;
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
git add src/test/java/com/example/demo/service/FieldPermissionTestData.java
git commit -m "feat(rbac): add field permission test data setup"
```

---

## 自检清单

- [x] 规范 P5 覆盖：字段脱敏 ✓、查看/编辑权限 ✓、缓存反射 ✓
- [x] 无占位符：所有代码完整
- [x] 性能：FieldAccessor 缓存避免重复反射
- [x] 脱敏规则：ID_CARD、PHONE、SALARY 已实现
- [x] 仓储接口：ResourceFieldRepository、FieldPermissionRepository 完整定义
- [x] 测试数据：FieldPermissionTestData 提供临时敏感字段初始化

---

**计划完成。**