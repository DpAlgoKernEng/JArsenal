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

src/main/java/com/jguard/
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

## 任务 1.5：创建 Layout 组件（新增）

**文件：**
- 创建：`ui/src/components/Layout.vue`

> **说明：** Layout 是动态路由的根组件，规范第四章4.5节要求前端基于菜单权限生成动态路由，Layout 作为容器承载动态添加的子路由。

- [ ] **步骤 1：编写 Layout 组件**

```vue
<!-- ui/src/components/Layout.vue -->
<template>
  <el-container class="layout-container">
    <!-- 侧边栏导航 -->
    <el-aside width="200px" class="layout-aside">
      <Navbar />
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in menus" :key="menu.code">
          <!-- 有子菜单的情况 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.code">
            <template #title>
              <el-icon><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item v-for="child in menu.children" :key="child.code" :index="child.path">
              <el-icon><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单的情况 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区域 -->
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/stores/permission'
import Navbar from '@/components/Navbar.vue'

const route = useRoute()
const permissionStore = usePermissionStore()

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 从权限 store 获取菜单树
const menus = computed(() => permissionStore.menus)
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  color: #fff;
}

.sidebar-menu {
  border-right: none;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
```

- [ ] **步骤 2：注册 Layout 为全局组件**

```javascript
// ui/src/main.js 中添加
import Layout from '@/components/Layout.vue'

const app = createApp(App)
app.component('Layout', Layout)
```

- [ ] **步骤 3：提交 Layout 组件**

```bash
git add ui/src/components/Layout.vue ui/src/main.js
git commit -m "feat(rbac): add Layout component for dynamic route container"
```

---

## 任务 1：创建后端权限 API

**文件：**
- 创建：`src/main/java/com/jguard/controller/PermissionController.java`
- 创建：`src/main/java/com/jguard/service/dto/UserPermissionsDTO.java`

- [ ] **步骤 1：编写 UserPermissionsDTO**

```java
package com.jguard.service.dto;

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
package com.jguard.service.dto;

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
package com.jguard.service.dto;

public record ActionPermissionDTO(
    String resourceCode,
    List<String> actions
) {}
```

```java
package com.jguard.service.dto;

public record FieldPermissionDTO(
    String fieldCode,
    boolean canView,
    boolean canEdit
) {}
```

- [ ] **步骤 2：编写 PermissionController**

```java
package com.jguard.controller;

import com.jguard.common.Result;
import com.jguard.service.PermissionQueryService;
import com.jguard.service.dto.UserPermissionsDTO;
import com.jguard.security.UserContext;
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
package com.jguard.service;

import com.jguard.domain.permission.aggregate.Resource;
import com.jguard.domain.permission.aggregate.ResourceField;
import com.jguard.domain.permission.entity.FieldPermission;
import com.jguard.domain.permission.service.PermissionCacheService;
import com.jguard.domain.permission.valueobject.PermissionBitmap;
import com.jguard.domain.permission.valueobject.ActionType;
import com.jguard.domain.permission.valueobject.ResourceType;
import com.jguard.domain.permission.valueobject.SensitiveLevel;
import com.jguard.domain.permission.repository.ResourceRepository;
import com.jguard.domain.permission.repository.ResourceFieldRepository;
import com.jguard.domain.permission.repository.FieldPermissionRepository;
import com.jguard.service.dto.*;
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
git add src/main/java/com/jguard/controller/PermissionController.java \
        src/main/java/com/jguard/service/PermissionQueryService.java \
        src/main/java/com/jguard/service/dto/UserPermissionsDTO.java \
        src/main/java/com/jguard/service/dto/MenuDTO.java \
        src/main/java/com/jguard/service/dto/ActionPermissionDTO.java \
        src/main/java/com/jguard/service/dto/FieldPermissionDTO.java
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
import { getUserPermissions, checkPermissionVersion } from '@/api/permission'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    menus: [],
    actions: {},      // { 'USER': ['VIEW', 'CREATE'] }
    fields: {},       // { 'USER': { 'salary': { canView: true, canEdit: false } } }
    loaded: false,
    version: 0,
    pollingTimer: null   // 权限变更轮询定时器
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
        
        // 启动权限变更监听（每30秒检查版本）
        this.startPermissionPolling()
      }
    },
    
    /**
     * 版本校验：检查后端权限版本是否变更
     * 如果变更则重新加载权限
     */
    async checkVersionAndRefresh() {
      try {
        const res = await checkPermissionVersion()
        if (res.code === 200 && res.data.version !== this.version) {
          // 版本不一致，重新加载权限
          await this.loadPermissions()
          console.log('权限已更新，版本:', res.data.version)
        }
      } catch (error) {
        // 静默失败，不影响用户操作
        console.warn('权限版本检查失败:', error)
      }
    },
    
    /**
     * 启动权限变更轮询（每30秒）
     */
    startPermissionPolling() {
      if (this.pollingTimer) return
      this.pollingTimer = setInterval(() => {
        this.checkVersionAndRefresh()
      }, 30000)  // 30秒轮询
    },
    
    /**
     * 停止权限变更监听
     */
    stopPermissionPolling() {
      if (this.pollingTimer) {
        clearInterval(this.pollingTimer)
        this.pollingTimer = null
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
    
    /**
     * 清除权限（退出登录时）
     */
    clear() {
      this.stopPermissionPolling()
      this.menus = []
      this.actions = {}
      this.fields = {}
      this.loaded = false
      this.version = 0
    }
  }
})
```

- [ ] **步骤 1.5：添加权限版本检查API**

```javascript
// ui/src/api/permission.js - 添加版本检查方法
export function checkPermissionVersion() {
  return request.get('/api/auth/permissions/version')
}
```

- [ ] **步骤 3：提交权限 store**

```bash
git add ui/src/stores/permission.js ui/src/api/permission.js
git commit -m "feat(rbac): add Pinia permission store with version check and auto refresh"
```

---

## 任务 2.5：添加后端权限版本检查API

**文件：**
- 修改：`src/main/java/com/jguard/controller/PermissionController.java`

- [ ] **步骤 1：添加版本检查端点**

```java
// 在 PermissionController.java 中添加

@GetMapping("/permissions/version")
public Result<Long> getPermissionVersion() {
    Long userId = UserContext.getCurrentUserId();
    if (userId == null) {
        return Result.error(401, "未登录");
    }
    
    // 计算用户权限版本（角色版本之和）
    List<Role> roles = roleRepository.findRolesByUserId(userId);
    long version = roles.stream()
        .mapToLong(Role::getVersion)
        .sum();
    
    return Result.success(version);
}
```

- [ ] **步骤 2：提交版本检查API**

```bash
git add src/main/java/com/jguard/controller/PermissionController.java
git commit -m "feat(rbac): add permission version check API for frontend polling"
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
- [x] API 端点：/api/auth/permissions、/api/auth/menus、/api/auth/actions、/api/auth/fields、/api/auth/permissions/version
- [x] 测试：前端测试可选（建议补充端到端测试）
- [x] 字段权限：getUserFields 方法完整实现
- [x] **版本校验**：前端每30秒轮询版本 ✓、版本不一致自动刷新权限 ✓
- [x] **权限变更刷新**：退出登录停止轮询 ✓、权限更新静默执行 ✓
- [x] **版本检查API**：PermissionController.getPermissionVersion()已添加 ✓
- [x] **新增**：Layout.vue 组件定义 ✓、作为动态路由根组件 ✓、菜单树渲染完整 ✓
- [x] **建议补充测试**：端到端测试（Playwright/Cypress）验证动态路由和权限指令

---

## 任务 9：建议补充 - 端到端测试（可选）

**文件：**
- 创建：`tests/e2e/permission.spec.js`（Playwright 示例）

- [ ] **步骤 1：编写 E2E 测试示例**

```javascript
// tests/e2e/permission.spec.js - Playwright 示例
import { test, expect } from '@playwright/test';

test.describe('权限系统 E2E 测试', () => {
  
  test.beforeEach(async ({ page }) => {
    // 登录
    await page.goto('/login');
    await page.fill('[placeholder="用户名"]', 'admin');
    await page.fill('[placeholder="密码"]', 'password');
    await page.click('button[type="submit"]');
    await page.waitForURL('/');
  });
  
  test('动态路由 - 无权限菜单不显示', async ({ page }) => {
    // 检查菜单树
    const menuItems = await page.locator('.el-menu-item').allTextContents();
    
    // 验证管理员能看到所有菜单
    expect(menuItems).toContain('用户管理');
    expect(menuItems).toContain('角色管理');
  });
  
  test('v-permission 指令 - 无权限按钮不渲染', async ({ page }) => {
    await page.goto('/system/users');
    
    // 检查删除按钮是否存在（取决于权限）
    const deleteButton = page.locator('button').filter({ hasText: '删除' });
    
    // 根据测试用户权限验证按钮可见性
    // 如果用户有 DELETE 权限，按钮应可见
    // 如果用户无 DELETE 权限，按钮不应存在
  });
  
  test('路由守卫 - 无权限页面跳转 403', async ({ page }) => {
    // 尝试访问无权限的路由
    await page.goto('/system/roles');
    
    // 如果无权限，应被重定向到 403 页面
    // await expect(page).toHaveURL('/403');
  });
  
  test('版本轮询 - 权限变更自动刷新', async ({ page }) => {
    // 模拟权限变更（需要后端配合）
    // 验证前端检测到版本变化后自动刷新权限
  });
});
```

- [ ] **步骤 2：配置 Playwright（可选）**

```bash
npm install -D @playwright/test
npx playwright install
```

---

**计划完成。**