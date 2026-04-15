# RBAC P3: 前端权限实现计划

> **对于智能体执行者：** 必须使用子技能：推荐使用 superpowers:subagent-driven-development 或 superpowers:executing-plans 按任务执行此计划。步骤使用复选框 (`- [ ]`) 语法进行跟踪。

**目标：** 实现前端权限系统，包含 Pinia store、动态路由、v-permission 指令和权限 API 端点

**架构：** 登录时加载权限数据，基于菜单权限生成动态路由，v-permission 指令控制按钮，路由守卫保护导航

**技术栈：** Vue 3、Pinia、Vue Router、Element Plus

**依赖：** P1、P2（后端权限校验必须工作）

---

## 文件结构

```
ui/src/
├── stores/
│   └ permission.js               # Pinia 权限 store
├── api/
│   └ permission.js               # 权限 API 调用
├── router/
│   ├── index.js                  # 添加动态路由（修改）
│   └ dynamicRoutes.js            # 动态路由生成
├── directives/
│   └ permission.js               # v-permission 指令
├── views/
│   ├── Login.vue                 # 登录时加载权限（修改）
│   ├── 403.vue                   # 禁止访问页面
├── composables/
│   └ usePermission.js            # 权限检查组合式函数

src/main/java/com/example/demo/
├── controller/
│   └ PermissionController.java   # 权限查询 API
├── service/
│   └ PermissionQueryService.java # 权限查询服务（完整实现）
│   └ dto/
│       ├── UserPermissionsDTO.java
│       ├── MenuDTO.java
│       ├── ActionPermissionDTO.java
│       ├── FieldPermissionDTO.java
```

---

## 任务 1：创建后端权限 API

**文件：**
- 创建：`src/main/java/com/example/demo/controller/PermissionController.java`
- 创建：`src/main/java/com/example/demo/service/dto/UserPermissionsDTO.java`

- [ ] **步骤 1：编写 UserPermissionsDTO**

```java
package com.example.demo.service.dto;

import java.util.List;
import java.util.Map;

public record UserPermissionsDTO(
    List<MenuDTO> menus,
    Map<String, List<String>> actions,
    Map<String, Map<String, FieldPermissionDTO>> fields,
    long version
) {}
```

```java
package com.example.demo.service.dto;

public record MenuDTO(
    Long id,
    String code,
    String name,
    String path,
    String icon,
    String component,
    Long parentId,
    List<MenuDTO> children
) {}
```

```java
package com.example.demo.service.dto;

public record ActionPermissionDTO(
    String resourceCode,
    List<String> actions
) {}
```

```java
package com.example.demo.service.dto;

public record FieldPermissionDTO(
    String fieldCode,
    boolean canView,
    boolean canEdit
) {}
```

- [ ] **步骤 2：编写 PermissionController**

```java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.PermissionQueryService;
import com.example.demo.service.dto.UserPermissionsDTO;
import com.example.demo.security.UserContext;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class PermissionController {
    
    private final PermissionQueryService permissionQueryService;
    
    public PermissionController(PermissionQueryService permissionQueryService) {
        this.permissionQueryService = permissionQueryService;
    }
    
    @GetMapping("/permissions")
    public Result<UserPermissionsDTO> getUserPermissions() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        UserPermissionsDTO permissions = permissionQueryService.getUserPermissions(userId);
        return Result.success(permissions);
    }
    
    @GetMapping("/menus")
    public Result<List<MenuDTO>> getUserMenus() {
        Long userId = UserContext.getCurrentUserId();
        List<MenuDTO> menus = permissionQueryService.getUserMenus(userId);
        return Result.success(menus);
    }
    
    @GetMapping("/actions")
    public Result<Map<String, List<String>>> getUserActions() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, List<String>> actions = permissionQueryService.getUserActions(userId);
        return Result.success(actions);
    }
    
    @GetMapping("/fields")
    public Result<Map<String, Map<String, FieldPermissionDTO>>> getUserFields() {
        Long userId = UserContext.getCurrentUserId();
        Map<String, Map<String, FieldPermissionDTO>> fields = permissionQueryService.getUserFields(userId);
        return Result.success(fields);
    }
}
```

- [ ] **步骤 3：编写 PermissionQueryService**

```java
package com.example.demo.service;

import com.example.demo.domain.permission.aggregate.Resource;
import com.example.demo.domain.permission.aggregate.ResourceField;
import com.example.demo.domain.permission.entity.FieldPermission;
import com.example.demo.domain.permission.service.PermissionCacheService;
import com.example.demo.domain.permission.valueobject.PermissionBitmap;
import com.example.demo.domain.permission.valueobject.ActionType;
import com.example.demo.domain.permission.valueobject.ResourceType;
import com.example.demo.domain.permission.valueobject.SensitiveLevel;
import com.example.demo.domain.permission.repository.ResourceRepository;
import com.example.demo.domain.permission.repository.ResourceFieldRepository;
import com.example.demo.domain.permission.repository.FieldPermissionRepository;
import com.example.demo.service.dto.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PermissionQueryService {
    
    private final PermissionCacheService permissionCache;
    private final ResourceRepository resourceRepository;
    private final ResourceFieldRepository resourceFieldRepository;
    private final FieldPermissionRepository fieldPermissionRepository;
    
    public PermissionQueryService(PermissionCacheService permissionCache,
                                   ResourceRepository resourceRepository,
                                   ResourceFieldRepository resourceFieldRepository,
                                   FieldPermissionRepository fieldPermissionRepository) {
        this.permissionCache = permissionCache;
        this.resourceRepository = resourceRepository;
        this.resourceFieldRepository = resourceFieldRepository;
        this.fieldPermissionRepository = fieldPermissionRepository;
    }
    
    public UserPermissionsDTO getUserPermissions(Long userId) {
        List<MenuDTO> menus = getUserMenus(userId);
        Map<String, List<String>> actions = getUserActions(userId);
        Map<String, Map<String, FieldPermissionDTO>> fields = getUserFields(userId);
        
        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);
        
        return new UserPermissionsDTO(menus, actions, fields, bitmap.getVersion());
    }
    
    public List<MenuDTO> getUserMenus(Long userId) {
        List<Resource> menuResources = resourceRepository.findByType(ResourceType.MENU);
        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);
        
        List<MenuDTO> menus = menuResources.stream()
            .filter(r -> bitmap.hasAction(r.getId(), ActionType.VIEW))
            .map(this::toMenuDTO)
            .toList();
        
        return buildMenuTree(menus);
    }
    
    private MenuDTO toMenuDTO(Resource r) {
        return new MenuDTO(r.getId(), r.getCode(), r.getName(), r.getPath(), 
                           r.getIcon(), r.getComponent(), r.getParentId(), new ArrayList<>());
    }
    
    private List<MenuDTO> buildMenuTree(List<MenuDTO> menus) {
        Map<Long, MenuDTO> menuMap = new HashMap<>();
        menus.forEach(m -> menuMap.put(m.id(), m));
        
        List<MenuDTO> rootMenus = new ArrayList<>();
        for (MenuDTO menu : menus) {
            if (menu.parentId() == null) {
                rootMenus.add(menu);
            } else {
                MenuDTO parent = menuMap.get(menu.parentId());
                if (parent != null) {
                    parent.children().add(menu);
                }
            }
        }
        return rootMenus;
    }
    
    public Map<String, List<String>> getUserActions(Long userId) {
        PermissionBitmap bitmap = permissionCache.getPermissionBitmap(userId);
        List<Resource> operationResources = resourceRepository.findByType(ResourceType.OPERATION);
        
        Map<String, List<String>> actionsMap = new HashMap<>();
        
        for (Resource resource : operationResources) {
            List<String> allowedActions = new ArrayList<>();
            
            for (ActionType action : ActionType.values()) {
                if (bitmap.hasAction(resource.getId(), action)) {
                    allowedActions.add(action.name());
                }
            }
            
            if (!allowedActions.isEmpty()) {
                // 查找父资源（菜单）的编码作为 key
                String resourceCode = resource.getCode();
                if (resource.getParentId() != null) {
                    Resource parent = resourceRepository.findById(resource.getParentId()).orElse(null);
                    if (parent != null) {
                        resourceCode = parent.getCode();  // 使用菜单编码
                    }
                }
                
                actionsMap.computeIfAbsent(resourceCode, k -> new ArrayList<>())
                    .addAll(allowedActions);
            }
        }
        
        return actionsMap;
    }
    
    public Map<String, Map<String, FieldPermissionDTO>> getUserFields(Long userId) {
        List<Resource> resources = resourceRepository.findAll();
        Map<String, Map<String, FieldPermissionDTO>> fieldsMap = new HashMap<>();
        
        for (Resource resource : resources) {
            List<ResourceField> sensitiveFields = resourceFieldRepository.findByResourceId(resource.getId());
            
            if (sensitiveFields.isEmpty()) continue;
            
            Map<String, FieldPermissionDTO> fieldPerms = new HashMap<>();
            
            for (ResourceField field : sensitiveFields) {
                List<FieldPermission> perms = fieldPermissionRepository.findByUserIdAndResourceId(userId, resource.getId());
                
                FieldPermission userPerm = perms.stream()
                    .filter(p -> p.getFieldId().equals(field.getId()))
                    .findFirst()
                    .orElse(null);
                
                boolean canView = userPerm != null ? userPerm.canView() : 
                    field.getSensitiveLevel() == SensitiveLevel.NORMAL;
                boolean canEdit = userPerm != null ? userPerm.canEdit() : false;
                
                fieldPerms.put(field.getFieldCode(), new FieldPermissionDTO(
                    field.getFieldCode(),
                    canView,
                    canEdit
                ));
            }
            
            fieldsMap.put(resource.getCode(), fieldPerms);
        }
        
        return fieldsMap;
    }
}
```

- [ ] **步骤 4：提交后端权限 API**

```bash
git add src/main/java/com/example/demo/controller/PermissionController.java \
        src/main/java/com/example/demo/service/PermissionQueryService.java \
        src/main/java/com/example/demo/service/dto/UserPermissionsDTO.java \
        src/main/java/com/example/demo/service/dto/MenuDTO.java \
        src/main/java/com/example/demo/service/dto/ActionPermissionDTO.java \
        src/main/java/com/example/demo/service/dto/FieldPermissionDTO.java
git commit -m "feat(rbac): add PermissionController for frontend permission query"
```

---

## 任务 2：创建前端权限 Store

**文件：**
- 创建：`ui/src/stores/permission.js`
- 创建：`ui/src/api/permission.js`

- [ ] **步骤 1：编写权限 API 模块**

```javascript
// ui/src/api/permission.js
import request from './index'

export function getUserPermissions() {
  return request.get('/api/auth/permissions')
}

export function getUserMenus() {
  return request.get('/api/auth/menus')
}

export function getUserActions() {
  return request.get('/api/auth/actions')
}

export function checkPermission(resourceCode, action) {
  return request.post('/api/auth/check', { resourceCode, actions: [action] })
}
```

- [ ] **步骤 2：编写 Pinia 权限 store**

```javascript
// ui/src/stores/permission.js
import { defineStore } from 'pinia'
import { getUserPermissions } from '@/api/permission'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    menus: [],
    actions: {},      // { 'USER': ['VIEW', 'CREATE'] }
    fields: {},       // { 'USER': { 'salary': { canView: true, canEdit: false } } }
    loaded: false,
    version: 0
  }),
  
  actions: {
    async loadPermissions() {
      const res = await getUserPermissions()
      if (res.code === 200) {
        this.menus = res.data.menus
        this.actions = res.data.actions
        this.fields = res.data.fields
        this.version = res.data.version
        this.loaded = true
      }
    },
    
    hasMenu(menuCode) {
      return this.menus.some(m => m.code === menuCode) ||
             this.menus.some(m => m.children?.some(c => c.code === menuCode))
    },
    
    hasAction(resourceCode, action) {
      const actions = this.actions[resourceCode] || []
      return actions.includes(action)
    },
    
    canViewField(resourceCode, fieldCode) {
      const fields = this.fields[resourceCode] || {}
      return fields[fieldCode]?.canView !== false
    },
    
    canEditField(resourceCode, fieldCode) {
      const fields = this.fields[resourceCode] || {}
      return fields[fieldCode]?.canEdit !== false
    },
    
    clear() {
      this.menus = []
      this.actions = {}
      this.fields = {}
      this.loaded = false
      this.version = 0
    }
  }
})
```

- [ ] **步骤 3：提交权限 store**

```bash
git add ui/src/stores/permission.js ui/src/api/permission.js
git commit -m "feat(rbac): add Pinia permission store and API"
```

---

## 任务 3：创建 v-permission 指令

**文件：**
- 创建：`ui/src/directives/permission.js`

- [ ] **步骤 1：编写权限指令**

```javascript
// ui/src/directives/permission.js
import { usePermissionStore } from '@/stores/permission'

export const permissionDirective = {
  mounted(el, binding) {
    const { resource, action } = binding.value
    const permissionStore = usePermissionStore()
    
    if (!permissionStore.hasAction(resource, action)) {
      el.parentNode?.removeChild(el)
    }
  },
  
  updated(el, binding) {
    const { resource, action } = binding.value
    const permissionStore = usePermissionStore()
    
    if (!permissionStore.hasAction(resource, action)) {
      if (el.parentNode) {
        el.parentNode.removeChild(el)
      }
    }
  }
}

export function setupPermissionDirective(app) {
  app.directive('permission', permissionDirective)
}
```

- [ ] **步骤 2：在 main.js 中注册指令**

```javascript
// ui/src/main.js - 添加此导入和调用
import { setupPermissionDirective } from '@/directives/permission'

const app = createApp(App)
setupPermissionDirective(app)
```

- [ ] **步骤 3：提交指令**

```bash
git add ui/src/directives/permission.js ui/src/main.js
git commit -m "feat(rbac): add v-permission directive for button control"
```

---

## 任务 4：创建动态路由

**文件：**
- 创建：`ui/src/router/dynamicRoutes.js`
- 修改：`ui/src/router/index.js`

- [ ] **步骤 1：编写动态路由生成**

```javascript
// ui/src/router/dynamicRoutes.js
import router from './index'
import Layout from '@/components/Layout.vue'

const modules = import.meta.glob('../views/**/*.vue')

export function addDynamicRoutes(menus) {
  const routes = menus.flatMap(menu => {
    const route = buildRoute(menu)
    if (menu.children?.length > 0) {
      route.children = menu.children.map(buildRoute)
    }
    return route
  })
  
  router.addRoute({
    path: '/',
    component: Layout,
    children: routes
  })
}

function buildRoute(menu) {
  const componentPath = `../views/${menu.component}.vue`
  
  return {
    path: menu.path,
    name: menu.code,
    component: modules[componentPath],
    meta: {
      title: menu.name,
      icon: menu.icon,
      resource: menu.code
    }
  }
}

export function removeDynamicRoutes() {
  router.getRoutes().forEach(route => {
    if (route.meta?.resource) {
      router.removeRoute(route.name)
    }
  })
}
```

- [ ] **步骤 2：添加路由守卫**

```javascript
// ui/src/router/index.js - 修改现有 router
import { usePermissionStore } from '@/stores/permission'
import { addDynamicRoutes } from './dynamicRoutes'

router.beforeEach(async (to, from, next) => {
  const permissionStore = usePermissionStore()
  const userStore = useUserStore()
  
  if (!userStore.token) {
    if (to.path === '/login' || to.path === '/register') {
      next()
    } else {
      next('/login')
    }
    return
  }
  
  // 首屏加载权限
  if (!permissionStore.loaded) {
    await permissionStore.loadPermissions()
    addDynamicRoutes(permissionStore.menus)
    next({ ...to, replace: true })
    return
  }
  
  // 检查菜单权限
  if (to.meta?.resource) {
    if (!permissionStore.hasMenu(to.meta.resource)) {
      next('/403')
      return
    }
  }
  
  next()
})
```

- [ ] **步骤 3：提交动态路由**

```bash
git add ui/src/router/dynamicRoutes.js ui/src/router/index.js
git commit -m "feat(rbac): add dynamic route generation based on menu permissions"
```

---

## 任务 5：创建 403 禁止访问页面

**文件：**
- 创建：`ui/src/views/403.vue`

- [ ] **步骤 1：编写 403 页面**

```vue
<!-- ui/src/views/403.vue -->
<template>
  <div class="forbidden-container">
    <el-result
      icon="error"
      title="403"
      sub-title="您没有权限访问此页面"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.forbidden-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
```

- [ ] **步骤 2：添加 403 路由**

```javascript
// 在 ui/src/router/index.js 静态路由中
{
  path: '/403',
  component: () => import('@/views/403.vue'),
  meta: { title: '无权限' }
}
```

- [ ] **步骤 3：提交 403 页面**

```bash
git add ui/src/views/403.vue ui/src/router/index.js
git commit -m "feat(rbac): add 403 forbidden page"
```

---

## 任务 6：创建 usePermission 组合式函数

**文件：**
- 创建：`ui/src/composables/usePermission.js`

- [ ] **步骤 1：编写 usePermission 组合式函数**

```javascript
// ui/src/composables/usePermission.js
import { usePermissionStore } from '@/stores/permission'

export function usePermission() {
  const permissionStore = usePermissionStore()
  
  function checkAction(resourceCode, action) {
    return permissionStore.hasAction(resourceCode, action)
  }
  
  function checkViewField(resourceCode, fieldCode) {
    return permissionStore.canViewField(resourceCode, fieldCode)
  }
  
  function checkEditField(resourceCode, fieldCode) {
    return permissionStore.canEditField(resourceCode, fieldCode)
  }
  
  function checkMenu(menuCode) {
    return permissionStore.hasMenu(menuCode)
  }
  
  return {
    checkAction,
    checkViewField,
    checkEditField,
    checkMenu,
    hasPermission: checkAction
  }
}
```

- [ ] **步骤 2：提交组合式函数**

```bash
git add ui/src/composables/usePermission.js
git commit -m "feat(rbac): add usePermission composable for reusable checks"
```

---

## 任务 7：更新登录加载权限

**文件：**
- 修改：`ui/src/views/Login.vue`

- [ ] **步骤 1：登录后加载权限**

```javascript
// 在 Login.vue 登录处理中
import { usePermissionStore } from '@/stores/permission'
import { addDynamicRoutes } from '@/router/dynamicRoutes'

const permissionStore = usePermissionStore()

const handleLogin = async () => {
  try {
    const res = await login({ username, password })
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserId(res.data.userId)
      
      // 加载权限并生成动态路由
      await permissionStore.loadPermissions()
      addDynamicRoutes(permissionStore.menus)
      
      router.push('/')
    }
  } catch (error) {
    ElMessage.error(error.message)
  }
}
```

- [ ] **步骤 2：提交登录更新**

```bash
git add ui/src/views/Login.vue
git commit -m "feat(rbac): load permissions on login"
```

---

## 任务 8：UserList 使用示例

**文件：**
- 修改：`ui/src/views/UserList.vue`（展示权限指令用法）

- [ ] **步骤 1：在 UserList 中添加权限检查**

```vue
<!-- 在 UserList.vue 中示例 -->
<template>
  <el-button v-permission="{ resource: 'USER', action: 'CREATE' }" 
             type="primary" @click="handleAdd">
    新增用户
  </el-button>
  
  <el-button v-permission="{ resource: 'USER', action: 'DELETE' }" 
             type="danger" @click="handleBatchDelete">
    批量删除
  </el-button>
  
  <el-table-column label="操作">
    <template #default="{ row }">
      <el-button v-permission="{ resource: 'USER', action: 'UPDATE' }" 
                 link @click="handleEdit(row)">
        编辑
      </el-button>
      <el-button v-permission="{ resource: 'USER', action: 'DELETE' }" 
                 link type="danger" @click="handleDelete(row)">
        删除
      </el-button>
    </template>
  </el-table-column>
</template>

<script setup>
import { usePermission } from '@/composables/usePermission'

const { hasPermission } = usePermission()

// 字段权限示例
const canViewSalary = hasPermission('USER_SALARY', 'VIEW')
</script>
```

- [ ] **步骤 2：提交示例**

```bash
git add ui/src/views/UserList.vue
git commit -m "feat(rbac): apply permission directive to UserList"
```

---

## 自检清单

- [x] 规范 P3 覆盖：Pinia store ✓、动态路由 ✓、v-permission ✓、路由守卫 ✓
- [x] 无占位符：所有代码完整（PermissionQueryService 完整实现）
- [x] API 端点：/api/auth/permissions、/api/auth/menus、/api/auth/actions、/api/auth/fields
- [x] 测试：前端测试可选（建议补充端到端测试）
- [x] 字段权限：getUserFields 方法完整实现

---

**计划完成。**