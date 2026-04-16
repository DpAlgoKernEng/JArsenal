# P5: 权限管理页面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现权限管理页面，角色权限配置 + 用户权限查看两个Tab

**Architecture:** PermissionList.vue组合RolePermissionPanel.vue和UserPermissionView.vue，PermissionConfigDialog为配置弹窗

**Tech Stack:** Vue 3 + Element Plus + Pinia

**前置依赖:** 
- P1 API封装层 (`ui/src/api/role.js`, `ui/src/api/resource.js`)
- P2 角色Store (`ui/src/stores/role.js`)
- P3 角色管理页面组件 (`ui/src/components/role/RoleTree.vue`)

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/components/permission/PermissionConfigDialog.vue` | 创建 | 权限配置弹窗 |
| `ui/src/components/permission/RolePermissionPanel.vue` | 创建 | 角色权限配置面板 |
| `ui/src/components/permission/UserPermissionView.vue` | 创建 | 用户权限查看 |
| `ui/src/views/PermissionList.vue` | 创建 | 权限管理页面 |

---

### Task 1: 创建PermissionConfigDialog组件

**Files:**
- Create: `ui/src/components/permission/PermissionConfigDialog.vue`

- [ ] **Step 1: 创建组件目录**

```bash
mkdir -p ui/src/components/permission
```

- [ ] **Step 2: 创建 PermissionConfigDialog.vue**

```vue
<template>
  <el-dialog
    v-model="visible"
    title="配置权限"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-alert type="warning" :closable="false" class="warning-alert">
      配置将覆盖该资源的现有权限设置
    </el-alert>

    <div class="resource-info">
      <p><strong>资源名称：</strong>{{ resource?.name }}</p>
      <p><strong>资源编码：</strong>{{ resource?.code }}</p>
    </div>

    <el-form ref="formRef" :model="form" label-width="100px" class="config-form">
      <el-form-item label="操作权限">
        <el-checkbox-group v-model="form.actions">
          <el-checkbox label="VIEW">查看</el-checkbox>
          <el-checkbox label="CREATE">创建</el-checkbox>
          <el-checkbox label="UPDATE">更新</el-checkbox>
          <el-checkbox label="DELETE">删除</el-checkbox>
          <el-checkbox label="EXECUTE">执行</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item label="权限效果">
        <el-select v-model="form.effect" placeholder="请选择">
          <el-option label="允许" value="ALLOW" />
          <el-option label="拒绝" value="DENY" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button class="save-button" type="primary" @click="handleSave" :loading="loading">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { usePermissionStore } from '../../stores/permission'
import { roleApi } from '../../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  roleId: { type: Number, default: null },
  resource: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const permissionStore = usePermissionStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref()
const loading = ref(false)

const form = reactive({
  actions: ['VIEW'],
  effect: 'ALLOW'
})

// 监听打开，初始化默认值
watch(visible, (val) => {
  if (val) {
    form.actions = ['VIEW']
    form.effect = 'ALLOW'
  }
})

// 保存
const handleSave = async () => {
  if (!props.roleId || !props.resource) return
  
  try {
    loading.value = true
    await roleApi.assignPermission(props.roleId, {
      resourceId: props.resource.id,
      actions: form.actions,
      effect: form.effect
    })
    
    ElMessage.success('权限配置成功')
    
    // 刷新当前用户权限（如果影响到当前用户）
    await permissionStore.loadPermissions()
    
    emit('success')
    visible.value = false
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.warning-alert {
  margin-bottom: 16px;
}

.resource-info {
  padding: 12px;
  background: var(--color-bg-glass);
  border-radius: 8px;
  margin-bottom: 16px;

  p {
    margin: 4px 0;
    color: var(--color-text-primary);
  }
}

.config-form {
  .el-checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
}

.save-button {
  @include gradient-button;
}
</style>
```

- [ ] **Step 3: 验证创建成功**

Run: `ls -la ui/src/components/permission/PermissionConfigDialog.vue`
Expected: 文件存在

---

### Task 2: 创建RolePermissionPanel组件

**Files:**
- Create: `ui/src/components/permission/RolePermissionPanel.vue`

- [ ] **Step 1: 创建 RolePermissionPanel.vue**

```vue
<template>
  <div class="role-permission-panel">
    <!-- 左侧角色选择 -->
    <div class="left-panel">
      <RoleTree
        @select="handleSelectRole"
      />
    </div>

    <!-- 右侧资源权限配置 -->
    <div class="right-panel">
      <div class="panel-header">
        <h3 class="panel-title">
          {{ selectedRole ? `配置角色 "${selectedRole.name}" 的权限` : '请选择角色' }}
        </h3>
      </div>

      <el-table
        v-if="selectedRole"
        :data="flatResources"
        row-key="id"
        v-loading="resourceLoading"
        class="resource-table"
      >
        <el-table-column prop="name" label="资源名称" min-width="150" />
        <el-table-column prop="code" label="编码" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="canConfig.value"
              link
              size="small"
              @click="handleConfig(row)"
            >
              配置权限
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-else description="请从左侧选择一个角色" />
    </div>

    <!-- 权限配置弹窗 -->
    <PermissionConfigDialog
      v-model="configVisible"
      :role-id="selectedRole?.id"
      :resource="configResource"
      @success="handleConfigSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import RoleTree from '../role/RoleTree.vue'
import PermissionConfigDialog from './PermissionConfigDialog.vue'
import { useRoleStore } from '../../stores/role'
import { usePermission } from '../../composables/usePermission'
import { resourceApi } from '../../api'

const route = useRoute()
const roleStore = useRoleStore()
const { hasPermission } = usePermission()

const canConfig = hasPermission('PERMISSION', 'UPDATE')

const selectedRole = computed(() => roleStore.currentRole)
const resources = ref([])
const resourceLoading = ref(false)

// 扁平化资源树（用于表格显示）
const flatResources = computed(() => {
  return flattenTree(resources.value)
})

const flattenTree = (tree, result = []) => {
  for (const node of tree) {
    result.push(node)
    if (node.children && node.children.length > 0) {
      flattenTree(node.children, result)
    }
  }
  return result
}

const configVisible = ref(false)
const configResource = ref(null)

// 加载资源树
const loadResources = async () => {
  resourceLoading.value = true
  try {
    resources.value = await resourceApi.treeV2()
  } finally {
    resourceLoading.value = false
  }
}

// 选择角色
const handleSelectRole = (role) => {
  // roleStore 已更新 currentRole
}

// 打开配置弹窗
const handleConfig = (row) => {
  configResource.value = row
  configVisible.value = true
}

// 配置成功
const handleConfigSuccess = () => {
  // PermissionConfigDialog 已刷新 permissionStore
}

// 监听路由参数，自动选择角色
watch(() => route.query.roleId, (roleId) => {
  if (roleId && roleStore.loaded) {
    const role = roleStore.findRoleById(Number(roleId))
    if (role) {
      roleStore.selectRole(role)
    }
  }
}, { immediate: true })

onMounted(() => {
  loadResources()
})
</script>

<style lang="scss" scoped>
.role-permission-panel {
  display: flex;
  height: calc(100vh - 140px);

  .left-panel {
    width: 300px;
    border-right: 1px solid var(--color-border);
    background: var(--color-bg-elevated);
    overflow-y: auto;
  }

  .right-panel {
    flex: 1;
    padding: 24px;
    overflow-y: auto;

    .panel-header {
      margin-bottom: 16px;

      .panel-title {
        @include gradient-text;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .resource-table {
      border-radius: 8px;

      :deep(.el-table__header-wrapper th) {
        background: var(--color-bg-elevated);
      }
    }
  }
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/components/permission/RolePermissionPanel.vue`
Expected: 文件存在

---

### Task 3: 创建UserPermissionView组件

**Files:**
- Create: `ui/src/components/permission/UserPermissionView.vue`

- [ ] **Step 1: 创建 UserPermissionView.vue**

```vue
<template>
  <div class="user-permission-view">
    <!-- 用户搜索 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名搜索"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button class="search-button" @click="handleSearch">
        搜索
      </el-button>
    </div>

    <!-- 搜索结果 -->
    <div class="search-result" v-if="searchResults.length > 0">
      <el-table :data="searchResults" highlight-current-row @row-click="handleSelectUser">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 选中用户详情 -->
    <div class="user-detail" v-if="selectedUser">
      <div class="info-card">
        <h3 class="card-title">用户信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ selectedUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 关联角色 -->
      <div class="roles-card">
        <div class="card-header">
          <h3 class="card-title">关联角色</h3>
          <el-button
            v-if="canAssignRole.value"
            size="small"
            @click="showAddRoleDialog"
          >
            添加角色
          </el-button>
        </div>

        <el-table :data="userRoles" v-loading="rolesLoading">
          <el-table-column prop="name" label="角色名称" />
          <el-table-column prop="code" label="编码" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" effect="plain" size="small">
                {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                v-if="canAssignRole.value"
                link
                size="small"
                class="remove-btn"
                @click="handleRemoveRole(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加角色弹窗 -->
    <el-dialog v-model="addRoleVisible" title="添加角色" width="400px">
      <el-tree
        ref="roleTreeRef"
        :data="availableRoles"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        show-checkbox
        check-strictly
      />
      <template #footer>
        <el-button @click="addRoleVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRoles" :loading="addLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { usePermission } from '../../composables/usePermission'
import { useRoleStore } from '../../stores/role'
import { userApi, roleApi } from '../../api'

const { hasPermission } = usePermission()
const canAssignRole = hasPermission('ROLE_MANAGE', 'UPDATE')

const roleStore = useRoleStore()

const searchKeyword = ref('')
const searchResults = ref([])
const selectedUser = ref(null)
const userRoles = ref([])
const rolesLoading = ref(false)

const addRoleVisible = ref(false)
const roleTreeRef = ref()
const addLoading = ref(false)

// 可选角色
const availableRoles = computed(() => roleStore.roles)

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return
  try {
    const data = await userApi.list({ username: searchKeyword.value, pageNum: 1, pageSize: 10 })
    searchResults.value = data.list || []
  } catch (error) {
    // error handled
  }
}

// 选择用户
const handleSelectUser = async (row) => {
  selectedUser.value = row
  await loadUserRoles()
}

// 加载用户角色
const loadUserRoles = async () => {
  if (!selectedUser.value) return
  rolesLoading.value = true
  try {
    userRoles.value = await roleApi.getUserRoles(selectedUser.value.id)
  } finally {
    rolesLoading.value = false
  }
}

// 移除角色
const handleRemoveRole = async (role) => {
  try {
    await ElMessageBox.confirm(
      `确定移除角色 "${role.name}"？`,
      '提示',
      { type: 'warning' }
    )
    await roleApi.removeFromUser(selectedUser.value.id, role.id)
    ElMessage.success('移除成功')
    await loadUserRoles()
  } catch (error) {
    if (error !== 'cancel') {
      // error handled
    }
  }
}

// 显示添加角色弹窗
const showAddRoleDialog = () => {
  addRoleVisible.value = true
}

// 添加角色
const handleAddRoles = async () => {
  const checkedIds = roleTreeRef.value?.getCheckedKeys() || []
  if (checkedIds.length === 0) {
    ElMessage.warning('请选择角色')
    return
  }

  // 合并现有角色
  const currentIds = userRoles.value.map(r => r.id)
  const newIds = [...currentIds, ...checkedIds]

  addLoading.value = true
  try {
    await roleApi.assignToUser(selectedUser.value.id, newIds)
    ElMessage.success('添加成功')
    addRoleVisible.value = false
    await loadUserRoles()
  } catch (error) {
    // error handled
  } finally {
    addLoading.value = false
  }
}

onMounted(() => {
  if (!roleStore.loaded) {
    roleStore.loadRoles()
  }
})
</script>

<style lang="scss" scoped>
.user-permission-view {
  padding: 24px;

  .search-section {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;

    .search-input {
      max-width: 300px;
    }

    .search-button {
      @include gradient-button;
    }
  }

  .search-result {
    margin-bottom: 24px;

    :deep(.el-table__row) {
      cursor: pointer;
    }
  }

  .user-detail {
    .info-card, .roles-card {
      @include glass-card;
      padding: 20px;
      margin-bottom: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .card-title {
        @include gradient-text;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .remove-btn {
      color: var(--el-color-danger);
    }
  }
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/components/permission/UserPermissionView.vue`
Expected: 文件存在

---

### Task 4: 创建PermissionList页面

**Files:**
- Create: `ui/src/views/PermissionList.vue`

- [ ] **Step 1: 创建 PermissionList.vue**

```vue
<template>
  <Navbar />
  <div class="permission-list-container">
    <div class="content-card">
      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" class="permission-tabs">
        <el-tab-pane label="角色权限配置" name="role">
          <RolePermissionPanel />
        </el-tab-pane>
        <el-tab-pane label="用户权限查看" name="user">
          <UserPermissionView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'
import RolePermissionPanel from '../components/permission/RolePermissionPanel.vue'
import UserPermissionView from '../components/permission/UserPermissionView.vue'

const activeTab = ref('role')
</script>

<style lang="scss" scoped>
.permission-list-container {
  padding-top: 60px;
  min-height: 100vh;
  background: var(--color-bg-base);

  .content-card {
    @include glass-card;
    margin: 24px;
    padding: 24px;
  }

  .permission-tabs {
    :deep(.el-tabs__item) {
      color: var(--color-text-secondary);
      font-size: 16px;

      &.is-active {
        color: var(--color-primary);
      }
    }

    :deep(.el-tabs__content) {
      padding-top: 16px;
    }
  }
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/views/PermissionList.vue`
Expected: 文件存在

---

### Task 5: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/components/permission/ src/views/PermissionList.vue
git commit -m "feat(permission): implement permission management page with role config and user view"
```

---

## 验收标准

- [ ] PermissionConfigDialog弹窗显示警告提示，配置权限后刷新permissionStore
- [ ] RolePermissionPanel左侧角色树，右侧资源表格，点击配置打开弹窗
- [ ] UserPermissionView支持用户搜索，显示关联角色，支持添加/移除角色
- [ ] PermissionList页面Tab切换正确
- [ ] 按钮级权限控制生效
- [ ] 代码已提交

---

## 下一步

完成此计划后，执行 `06-router-update.md` 更新路由配置。