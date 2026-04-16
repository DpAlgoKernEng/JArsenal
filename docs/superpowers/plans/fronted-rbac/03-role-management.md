# P3: 角色管理页面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现角色管理页面，左侧角色树 + 右侧详情面板布局

**Architecture:** RoleList.vue组合RoleTree.vue和RoleDetail.vue，RoleForm.vue为弹窗编辑表单

**Tech Stack:** Vue 3 + Element Plus + Pinia

**前置依赖:** 
- P1 API封装层 (`ui/src/api/role.js`)
- P2 角色Store (`ui/src/stores/role.js`)

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/components/role/RoleTree.vue` | 创建 | 角色树组件 |
| `ui/src/components/role/RoleForm.vue` | 创建 | 角色编辑表单弹窗 |
| `ui/src/components/role/RoleDetail.vue` | 创建 | 角色详情面板 |
| `ui/src/views/RoleList.vue` | 创建 | 角色管理页面 |

---

### Task 1: 创建RoleTree组件

**Files:**
- Create: `ui/src/components/role/RoleTree.vue`

- [ ] **Step 1: 创建组件目录**

```bash
mkdir -p ui/src/components/role
```

- [ ] **Step 2: 创建 RoleTree.vue**

```vue
<template>
  <div class="role-tree-container">
    <!-- 搜索框 -->
    <el-input
      v-model="filterText"
      placeholder="搜索角色"
      clearable
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button
        v-if="canCreate.value"
        class="create-button"
        @click="handleCreateRoot"
      >
        <el-icon><Plus /></el-icon>
        新增根角色
      </el-button>
      <el-button class="refresh-button" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <!-- 角色树 -->
    <el-tree
      ref="treeRef"
      :data="roleStore.roles"
      :props="treeProps"
      node-key="id"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      :highlight-current="true"
      class="role-tree"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <span class="node-label">{{ data.name }}</span>
          <span class="node-code">{{ data.code }}</span>
          <div class="node-actions">
            <el-button
              v-if="canCreate.value"
              link
              size="small"
              @click.stop="handleCreateChild(data)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button
              v-if="canDelete.value && !data.builtin"
              link
              size="small"
              class="delete-btn"
              @click.stop="handleDelete(data)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh, Delete } from '@element-plus/icons-vue'
import { useRoleStore } from '../../stores/role'
import { usePermission } from '../../composables/usePermission'
import { roleApi } from '../../api'

const roleStore = useRoleStore()
const { hasPermission } = usePermission()

const canCreate = hasPermission('ROLE_MANAGE', 'CREATE')
const canDelete = hasPermission('ROLE_MANAGE', 'DELETE')

const treeRef = ref()
const filterText = ref('')
const emit = defineEmits(['select', 'create', 'delete'])

const treeProps = {
  children: 'children',
  label: 'name'
}

// 过滤节点
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value) || data.code.includes(value)
}

// 监听搜索文本变化
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 点击节点
const handleNodeClick = (data) => {
  roleStore.selectRole(data)
  emit('select', data)
}

// 新增根角色
const handleCreateRoot = () => {
  emit('create', { parentId: null })
}

// 新增子角色
const handleCreateChild = (data) => {
  emit('create', { parentId: data.id, parentName: data.name })
}

// 删除角色
const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(
      `确定删除角色 "${data.name}"？删除后不可恢复。`,
      '提示',
      { type: 'warning' }
    )
    await roleApi.delete(data.id)
    ElMessage.success('删除成功')
    await roleStore.refreshRoles()
    emit('delete', data)
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}

// 刷新
const handleRefresh = async () => {
  await roleStore.loadRoles()
}

onMounted(() => {
  if (!roleStore.loaded) {
    roleStore.loadRoles()
  }
})
</script>

<style lang="scss" scoped>
.role-tree-container {
  padding: 16px;

  .search-input {
    margin-bottom: 12px;
  }

  .action-bar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    .create-button {
      @include gradient-button;
      flex: 1;
    }

    .refresh-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      transition: all 0.3s ease;

      &:hover {
        background: var(--color-border);
      }
    }
  }

  .role-tree {
    background: transparent;

    :deep(.el-tree-node__content) {
      height: 36px;
      border-radius: 6px;
      transition: all 0.2s ease;

      &:hover {
        background: var(--color-bg-glass);
      }
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: var(--color-primary-light);
      color: var(--color-primary);
    }

    .tree-node {
      display: flex;
      align-items: center;
      flex: 1;
      padding-right: 8px;

      .node-label {
        flex: 1;
        font-size: 14px;
      }

      .node-code {
        font-size: 12px;
        color: var(--color-text-muted);
        margin-right: 8px;
      }

      .node-actions {
        display: flex;
        gap: 4px;
        opacity: 0;
        transition: opacity 0.2s ease;

        .delete-btn {
          color: var(--el-color-danger);
        }
      }

      &:hover .node-actions {
        opacity: 1;
      }
    }
  }
}
</style>
```

- [ ] **Step 3: 验证创建成功**

Run: `ls -la ui/src/components/role/RoleTree.vue`
Expected: 文件存在

---

### Task 2: 创建RoleForm组件

**Files:**
- Create: `ui/src/components/role/RoleForm.vue`

- [ ] **Step 1: 创建 RoleForm.vue**

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑角色' : (parentId ? '新增子角色' : '新增根角色')"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="角色编码" prop="code" v-if="!isEdit">
        <el-input
          v-model="form.code"
          placeholder="大写字母开头，如 ADMIN_USER"
        />
      </el-form-item>

      <el-form-item label="角色名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入角色名称" />
      </el-form-item>

      <el-form-item label="父角色" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ children: 'children', label: 'label', value: 'value' }"
          check-strictly
          clearable
          placeholder="选择父角色（可选）"
          class="full-width"
        />
      </el-form-item>

      <el-form-item label="继承模式" prop="inheritMode">
        <el-select v-model="form.inheritMode" placeholder="请选择继承模式">
          <el-option label="继承全部" value="EXTEND" />
          <el-option label="不继承" value="NONE" />
        </el-select>
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button class="save-button" @click="handleSave" :loading="submitLoading">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoleStore } from '../../stores/role'
import { roleApi } from '../../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  roleId: { type: Number, default: null },
  parentId: { type: Number, default: null },
  parentName: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'success'])

const roleStore = useRoleStore()
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => props.roleId !== null)
const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)

const form = reactive({
  code: '',
  name: '',
  parentId: null,
  inheritMode: 'EXTEND',
  sort: 0
})

const rules = {
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]{1,49}$/, message: '编码格式：大写字母开头，2-50字符，仅含字母数字下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50字符', trigger: 'blur' }
  ],
  sort: [
    { type: 'number', min: 0, message: '排序值必须大于等于0', trigger: 'blur' }
  ]
}

// 父角色选项
const parentOptions = computed(() => {
  const options = roleStore.buildTreeOptions()
  // 编辑时排除当前角色（不能设自己为父角色）
  if (isEdit.value) {
    return filterCurrentRole(options, props.roleId)
  }
  return options
})

// 递归排除当前角色
const filterCurrentRole = (tree, excludeId) => {
  return tree.filter(node => node.value !== excludeId).map(node => ({
    ...node,
    children: node.children ? filterCurrentRole(node.children, excludeId) : undefined
  }))
}

// 监听打开，加载数据
watch(visible, async (val) => {
  if (val) {
    formRef.value?.resetFields()
    if (isEdit.value) {
      loading.value = true
      try {
        const role = await roleApi.get(props.roleId)
        form.code = role.code
        form.name = role.name
        form.parentId = role.parentId
        form.inheritMode = role.inheritMode || 'EXTEND'
        form.sort = role.sort || 0
      } finally {
        loading.value = false
      }
    } else {
      form.code = ''
      form.name = ''
      form.parentId = props.parentId || null
      form.inheritMode = 'EXTEND'
      form.sort = 0
    }
  }
})

// 保存
const handleSave = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    const data = {
      name: form.name,
      parentId: form.parentId || 0,
      inheritMode: form.inheritMode,
      sort: form.sort
    }

    if (isEdit.value) {
      await roleApi.update(props.roleId, data)
      ElMessage.success('更新成功')
    } else {
      data.code = form.code
      await roleApi.create(data)
      ElMessage.success('创建成功')
    }

    await roleStore.refreshRoles()
    emit('success')
    visible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      // validation error or API error handled
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭
const handleClose = () => {
  formRef.value?.resetFields()
}
</script>

<style lang="scss" scoped>
.full-width {
  width: 100%;
}

.save-button {
  @include gradient-button;
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/components/role/RoleForm.vue`
Expected: 文件存在

---

### Task 3: 创建RoleDetail组件

**Files:**
- Create: `ui/src/components/role/RoleDetail.vue`

- [ ] **Step 1: 创建 RoleDetail.vue**

```vue
<template>
  <div class="role-detail-container" v-if="roleStore.currentRole">
    <!-- 基本信息卡片 -->
    <div class="info-card">
      <div class="card-header">
        <h3 class="card-title">基本信息</h3>
        <div class="card-actions">
          <el-button
            v-if="canUpdate.value"
            class="edit-button"
            @click="handleEdit"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button
            v-if="canDelete.value && !role.builtin"
            class="delete-button"
            @click="handleDelete"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>

      <el-descriptions :column="2" border class="info-descriptions">
        <el-descriptions-item label="ID">{{ role.id }}</el-descriptions-item>
        <el-descriptions-item label="编码">{{ role.code }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ role.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="role.status === 'ENABLED' ? 'success' : 'danger'" effect="plain">
            {{ role.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="继承模式">{{ role.inheritMode || 'EXTEND' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ role.sort }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ role.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ role.updateTime }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-card">
      <h3 class="card-title">统计信息</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <span class="stat-label">子角色数</span>
          <span class="stat-value">{{ childCount }}</span>
        </div>
      </div>
    </div>

    <!-- 权限配置入口 -->
    <div class="permission-card">
      <h3 class="card-title">权限配置</h3>
      <el-button
        v-if="canConfigPermission.value"
        class="config-button"
        @click="handleConfigPermission"
      >
        <el-icon><Setting /></el-icon>
        配置权限
      </el-button>
    </div>
  </div>

  <!-- 未选中提示 -->
  <div class="empty-container" v-else>
    <el-empty description="请从左侧选择一个角色" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Setting } from '@element-plus/icons-vue'
import { useRoleStore } from '../../stores/role'
import { usePermission } from '../../composables/usePermission'
import { roleApi } from '../../api'

const router = useRouter()
const roleStore = useRoleStore()
const { hasPermission } = usePermission()

const canUpdate = hasPermission('ROLE_MANAGE', 'UPDATE')
const canDelete = hasPermission('ROLE_MANAGE', 'DELETE')
const canConfigPermission = hasPermission('PERMISSION', 'UPDATE')

const role = computed(() => roleStore.currentRole)

// 计算子角色数
const childCount = computed(() => {
  if (!role.value) return 0
  return role.value.children?.length || 0
})

const emit = defineEmits(['edit', 'delete'])

// 编辑
const handleEdit = () => {
  emit('edit', role.value.id)
}

// 删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定删除角色 "${role.value.name}"？删除后不可恢复。`,
      '提示',
      { type: 'warning' }
    )
    await roleApi.delete(role.value.id)
    ElMessage.success('删除成功')
    roleStore.selectRole(null)
    await roleStore.refreshRoles()
    emit('delete', role.value)
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}

// 配置权限
const handleConfigPermission = () => {
  router.push({
    path: '/system/permissions',
    query: { roleId: role.value.id }
  })
}
</script>

<style lang="scss" scoped>
.role-detail-container {
  padding: 24px;

  .info-card, .stats-card, .permission-card {
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

    .card-actions {
      display: flex;
      gap: 8px;

      .edit-button {
        background: var(--color-bg-glass);
        border: 1px solid var(--color-border);
        color: var(--color-text-primary);
        transition: all 0.3s ease;

        &:hover {
          background: var(--color-primary-light);
          color: var(--color-primary);
        }
      }

      .delete-button {
        background: transparent;
        border: 1px solid var(--el-color-danger);
        color: var(--el-color-danger);

        &:hover {
          background: var(--el-color-danger-light);
        }
      }
    }
  }

  .info-descriptions {
    :deep(.el-descriptions__label) {
      background: var(--color-bg-elevated);
      color: var(--color-text-secondary);
    }

    :deep(.el-descriptions__content) {
      color: var(--color-text-primary);
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .stat-item {
      display: flex;
      justify-content: space-between;
      padding: 12px;
      background: var(--color-bg-glass);
      border-radius: 8px;

      .stat-label {
        color: var(--color-text-secondary);
      }

      .stat-value {
        font-weight: 600;
        color: var(--color-primary);
      }
    }
  }

  .config-button {
    @include gradient-button;
    width: 100%;
  }
}

.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/components/role/RoleDetail.vue`
Expected: 文件存在

---

### Task 4: 创建RoleList页面

**Files:**
- Create: `ui/src/views/RoleList.vue`

- [ ] **Step 1: 创建 RoleList.vue**

```vue
<template>
  <Navbar />
  <div class="role-list-container">
    <div class="layout-wrapper">
      <!-- 左侧角色树 -->
      <div class="left-panel">
        <RoleTree
          @select="handleSelect"
          @create="handleCreate"
          @delete="handleDeleteComplete"
        />
      </div>

      <!-- 右侧详情面板 -->
      <div class="right-panel">
        <RoleDetail
          ref="detailRef"
          @edit="handleEdit"
          @delete="handleDeleteComplete"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <RoleForm
      v-model="formVisible"
      :role-id="editRoleId"
      :parent-id="createParentId"
      :parent-name="createParentName"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'
import RoleTree from '../components/role/RoleTree.vue'
import RoleDetail from '../components/role/RoleDetail.vue'
import RoleForm from '../components/role/RoleForm.vue'
import { useRoleStore } from '../stores/role'

const roleStore = useRoleStore()

const detailRef = ref()
const formVisible = ref(false)
const editRoleId = ref(null)
const createParentId = ref(null)
const createParentName = ref('')

// 选择角色
const handleSelect = (role) => {
  // RoleDetail 通过 roleStore.currentRole 自动更新
}

// 新增角色
const handleCreate = ({ parentId, parentName }) => {
  editRoleId.value = null
  createParentId.value = parentId
  createParentName.value = parentName || ''
  formVisible.value = true
}

// 编辑角色
const handleEdit = (roleId) => {
  editRoleId.value = roleId
  createParentId.value = null
  createParentName.value = ''
  formVisible.value = true
}

// 删除完成
const handleDeleteComplete = () => {
  // RoleTree 和 RoleDetail 已在各自组件中处理刷新
}

// 表单保存成功
const handleFormSuccess = () => {
  // RoleForm 已调用 roleStore.refreshRoles()
}
</script>

<style lang="scss" scoped>
.role-list-container {
  padding-top: 60px; // Navbar高度
  min-height: 100vh;
  background: var(--color-bg-base);

  .layout-wrapper {
    display: flex;
    height: calc(100vh - 60px);
  }

  .left-panel {
    width: 300px;
    border-right: 1px solid var(--color-border);
    background: var(--color-bg-elevated);
    overflow-y: auto;
  }

  .right-panel {
    flex: 1;
    overflow-y: auto;
  }
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/views/RoleList.vue`
Expected: 文件存在

---

### Task 5: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/components/role/ src/views/RoleList.vue
git commit -m "feat(role): implement role management page with tree and detail panel"
```

---

## 验收标准

- [ ] RoleTree组件显示角色树，支持搜索、新增、删除
- [ ] RoleForm弹窗支持创建和编辑角色，表单校验正确
- [ ] RoleDetail显示角色详情，支持编辑、删除、配置权限
- [ ] RoleList页面布局正确（左树右详情）
- [ ] 按钮级权限控制生效
- [ ] 代码已提交

---

## 下一步

完成此计划后，执行 `04-resource-management.md` 创建资源管理页面。