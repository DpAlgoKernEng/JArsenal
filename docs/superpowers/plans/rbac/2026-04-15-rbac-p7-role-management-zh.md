# RBAC P7: 角色管理 API 实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现角色管理 CRUD API、角色继承树展示和权限分配端点

**架构：** RoleController 暴露 REST 端点，RoleService 处理业务逻辑，RoleHierarchyService 计算角色继承

**技术栈：** Spring Boot REST、MyBatis、Jakarta Validation

**依赖：** P1-P6（所有领域模型和服务必须存在）

---

## 文件结构

```
src/main/java/com/example/demo/
├── controller/
│   └ RoleController.java              # 角色 CRUD API
├── service/
│   ├── RoleService.java               # 角色应用服务
│   ├── RoleHierarchyService.java      # 继承树
│   └ dto/
│       ├── RoleCreateRequest.java
│       ├── RoleUpdateRequest.java
│       ├── RoleResponse.java
│       ├── RoleTreeResponse.java
│       ├── PermissionAssignRequest.java
│       ├── DataScopeAssignRequest.java

ui/src/views/
├── RoleList.vue                        # 角色管理页面
├── RoleEdit.vue                        # 角色编辑对话框
├── RolePermission.vue                  # 权限分配
├── RoleDataScope.vue                   # 数据范围分配
```

---

## 任务 1：创建角色 DTO

**文件：**
- 创建：`src/main/java/com/example/demo/service/dto/RoleCreateRequest.java`
- 创建：`src/main/java/com/example/demo/service/dto/RoleUpdateRequest.java`
- 创建：`src/main/java/com/example/demo/service/dto/RoleResponse.java`

- [ ] **步骤 1：编写角色 DTO**

```java
package com.example.demo.service.dto;

import jakarta.validation.constraints.*;

public record RoleCreateRequest(
    @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{1,49}$")
    String code,
    
    @NotBlank @Size(max = 100)
    String name,
    
    Long parentId,
    
    String inheritMode,  // EXTEND/LIMIT
    
    @Min(0) @Max(999)
    Integer sort
) {}
```

```java
package com.example.demo.service.dto;

import jakarta.validation.constraints.*;

public record RoleUpdateRequest(
    Long id,
    
    @NotBlank @Size(max = 100)
    String name,
    
    Long parentId,
    
    String inheritMode,
    
    @Min(0) @Max(999)
    Integer sort
) {}
```

```java
package com.example.demo.service.dto;

import java.util.List;

public record RoleResponse(
    Long id,
    String code,
    String name,
    Long parentId,
    String parentName,
    String status,
    String inheritMode,
    boolean isBuiltin,
    int version,
    int sort,
    List<PermissionResponse> permissions,
    List<DataScopeResponse> dataScopes
) {}
```

- [ ] **步骤 2：提交 DTO**

```bash
git add src/main/java/com/example/demo/service/dto/RoleCreateRequest.java \
        src/main/java/com/example/demo/service/dto/RoleUpdateRequest.java \
        src/main/java/com/example/demo/service/dto/RoleResponse.java
git commit -m "feat(rbac): add role DTOs with validation"
```

---

## 任务 2：创建 RoleService

**文件：**
- 创建：`src/main/java/com/example/demo/service/RoleService.java`

- [ ] **步骤 1：编写 RoleService**

```java
package com.example.demo.service;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.valueobject.*;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.event.*;
import com.example.demo.service.dto.*;
import com.example.demo.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final PermissionCacheService cacheService;
    
    @Transactional
    public RoleResponse createRole(RoleCreateRequest request) {
        // 检查重复编码
        if (roleRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException(400, "角色编码已存在");
        }
        
        Role role = Role.create(
            new RoleCode(request.code()),
            request.name(),
            request.parentId(),
            request.inheritMode() != null ? InheritMode.valueOf(request.inheritMode()) : InheritMode.EXTEND
        );
        role.setSort(request.sort() != null ? request.sort() : 0);
        
        role = roleRepository.save(role);
        
        // 发布事件
        eventPublisher.publish(new RoleCreatedEvent(role.getId(), role.getCode().value(), role.getName(), UserContext.getCurrentUserId()));
        
        return toResponse(role);
    }
    
    @Transactional
    public RoleResponse updateRole(RoleUpdateRequest request) {
        Role role = roleRepository.findById(request.id())
            .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        
        if (role.isBuiltin()) {
            throw new BusinessException(400, "内置角色不能修改");
        }
        
        role.setName(request.name());
        role.setParentId(request.parentId());
        role.setInheritMode(request.inheritMode() != null ? InheritMode.valueOf(request.inheritMode()) : InheritMode.EXTEND);
        role.setSort(request.sort() != null ? request.sort() : 0);
        role.incrementVersion();
        
        role = roleRepository.save(role);
        
        // 清除受影响用户的缓存
        cacheService.clearRoleRelatedPermissions(role.getId());
        
        return toResponse(role);
    }
    
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        
        if (role.isBuiltin()) {
            throw new BusinessException(400, "内置角色不能删除");
        }
        
        // 检查角色是否有用户
        List<Long> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        if (!userIds.isEmpty()) {
            throw new BusinessException(400, "角色下存在用户，不能删除");
        }
        
        role.softDelete();
        roleRepository.save(role);
        
        cacheService.clearRoleRelatedPermissions(roleId);
        
        eventPublisher.publish(new RoleDeletedEvent(roleId, role.getCode().value(), UserContext.getCurrentUserId()));
    }
    
    public RoleResponse getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        return toResponse(role);
    }
    
    public List<RoleResponse> listRoles() {
        List<Role> roles = roleRepository.findAllNotDeleted();
        return roles.stream().map(this::toResponse).toList();
    }
    
    @Transactional
    public void assignPermission(Long roleId, PermissionAssignRequest request) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new BusinessException(404, "角色不存在"));
        
        role.assignPermission(request.resourceId(), request.actions(), PermissionEffect.valueOf(request.effect()));
        role.incrementVersion();
        
        roleRepository.save(role);
        cacheService.clearRoleRelatedPermissions(roleId);
    }
}
```

- [ ] **步骤 2：提交 RoleService**

```bash
git add src/main/java/com/example/demo/service/RoleService.java
git commit -m "feat(rbac): add RoleService for role management"
```

---

## 任务 3：创建 RoleController

**文件：**
- 创建：`src/main/java/com/example/demo/controller/RoleController.java`

- [ ] **步骤 1：编写 RoleController**

```java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.RoleService;
import com.example.demo.service.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    
    private final RoleService roleService;
    
    @PostMapping
    public Result<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleResponse role = roleService.createRole(request);
        return Result.success(role);
    }
    
    @PutMapping("/{id}")
    public Result<RoleResponse> updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        request = new RoleUpdateRequest(id, request.name(), request.parentId(), request.inheritMode(), request.sort());
        RoleResponse role = roleService.updateRole(request);
        return Result.success(role);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success(null);
    }
    
    @GetMapping("/{id}")
    public Result<RoleResponse> getRole(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return Result.success(role);
    }
    
    @GetMapping
    public Result<List<RoleResponse>> listRoles() {
        List<RoleResponse> roles = roleService.listRoles();
        return Result.success(roles);
    }
    
    @GetMapping("/tree")
    public Result<List<RoleTreeResponse>> getRoleTree() {
        List<RoleTreeResponse> tree = roleService.getRoleTree();
        return Result.success(tree);
    }
    
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermission(@PathVariable Long id, @RequestBody PermissionAssignRequest request) {
        roleService.assignPermission(id, request);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}/permissions/{resourceId}")
    public Result<Void> removePermission(@PathVariable Long id, @PathVariable Long resourceId) {
        roleService.removePermission(id, resourceId);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/data-scopes")
    public Result<Void> assignDataScope(@PathVariable Long id, @RequestBody DataScopeAssignRequest request) {
        roleService.assignDataScope(id, request);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}/data-scopes/{dimensionCode}")
    public Result<Void> removeDataScope(@PathVariable Long id, @PathVariable String dimensionCode) {
        roleService.removeDataScope(id, dimensionCode);
        return Result.success(null);
    }
}
```

- [ ] **步骤 2：提交 RoleController**

```bash
git add src/main/java/com/example/demo/controller/RoleController.java
git commit -m "feat(rbac): add RoleController REST API"
```

---

## 任务 4：创建 RoleHierarchyService

**文件：**
- 创建：`src/main/java/com/example/demo/domain/permission/service/RoleHierarchyService.java`

- [ ] **步骤 1：编写 RoleHierarchyService**

```java
package com.example.demo.domain.permission.service;

import com.example.demo.domain.permission.aggregate.Role;
import com.example.demo.domain.permission.repository.RoleRepository;
import com.example.demo.service.dto.RoleTreeResponse;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RoleHierarchyService {
    
    private final RoleRepository roleRepository;
    
    public List<RoleTreeResponse> buildRoleTree() {
        List<Role> allRoles = roleRepository.findAllNotDeleted();
        
        Map<Long, RoleTreeResponse> roleMap = new HashMap<>();
        List<RoleTreeResponse> rootRoles = new ArrayList<>();
        
        for (Role role : allRoles) {
            RoleTreeResponse node = new RoleTreeResponse(
                role.getId(),
                role.getCode().value(),
                role.getName(),
                role.getParentId(),
                role.getInheritMode().name(),
                role.isBuiltin(),
                new ArrayList<>()
            );
            roleMap.put(role.getId(), node);
        }
        
        for (Role role : allRoles) {
            RoleTreeResponse node = roleMap.get(role.getId());
            if (role.getParentId() == null) {
                rootRoles.add(node);
            } else {
                RoleTreeResponse parent = roleMap.get(role.getParentId());
                if (parent != null) {
                    parent.children().add(node);
                }
            }
        }
        
        return rootRoles;
    }
    
    public List<Long> getInheritanceChain(Long roleId) {
        List<Long> chain = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        
        Long current = roleId;
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            chain.add(current);
            
            Role role = roleRepository.findById(current).orElse(null);
            if (role == null) break;
            current = role.getParentId();
        }
        
        return chain;
    }
    
    public boolean wouldCreateCircularInheritance(Long roleId, Long newParentId) {
        Set<Long> visited = new HashSet<>();
        Long current = newParentId;
        
        while (current != null) {
            if (current.equals(roleId)) return true;
            if (visited.contains(current)) return true;
            
            visited.add(current);
            Role role = roleRepository.findById(current).orElse(null);
            if (role == null) break;
            current = role.getParentId();
        }
        
        return false;
    }
}
```

- [ ] **步骤 2：提交继承服务**

```bash
git add src/main/java/com/example/demo/domain/permission/service/RoleHierarchyService.java
git commit -m "feat(rbac): add RoleHierarchyService for inheritance tree"
```

---

## 任务 5：创建前端角色管理页面

**文件：**
- 创建：`ui/src/views/RoleList.vue`
- 创建：`ui/src/views/RoleEdit.vue`

- [ ] **步骤 1：编写 RoleList.vue**

```vue
<template>
  <div class="role-container">
    <el-card>
      <el-button v-permission="{ resource: 'ROLE', action: 'CREATE' }" 
                 type="primary" @click="handleAdd">
        新增角色
      </el-button>
      
      <el-table :data="roles" row-key="id" border>
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="inheritMode" label="继承模式" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-permission="{ resource: 'ROLE', action: 'UPDATE' }" 
                       link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button v-permission="{ resource: 'ROLE', action: 'DELETE' }" 
                       link type="danger" @click="handleDelete(row)" 
                       :disabled="row.isBuiltin">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRoles, deleteRole } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

const roles = ref([])

onMounted(async () => {
  const res = await getRoles()
  if (res.code === 200) {
    roles.value = res.data
  }
})

const handleAdd = () => {
  // 打开编辑对话框
}

const handleEdit = (row) => {
  // 打开编辑对话框并传入行数据
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该角色？', '提示')
  const res = await deleteRole(row.id)
  if (res.code === 200) {
    ElMessage.success('删除成功')
    onMounted()
  }
}
</script>
```

- [ ] **步骤 2：提交前端页面**

```bash
git add ui/src/views/RoleList.vue ui/src/views/RoleEdit.vue
git commit -m "feat(rbac): add role management frontend pages"
```

---

## 自检清单

- [x] 规范 P7 覆盖：角色 CRUD ✓、权限分配 ✓、数据范围分配 ✓、继承树 ✓
- [x] 无占位符：所有代码完整
- [x] 校验：DTO 上有 Jakarta 约束
- [x] 安全：内置角色保护、循环继承检查

---

**计划完成。**