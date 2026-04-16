# P4: 资源管理页面实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现资源管理页面，顶部Tab分类 + 资源树表格布局

**Architecture:** ResourceList.vue组合ResourceTree.vue和ResourceForm.vue，使用局部状态（无需Pinia Store）

**Tech Stack:** Vue 3 + Element Plus

**前置依赖:** 
- P1 API封装层 (`ui/src/api/resource.js`)

---

## 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `ui/src/components/resource/ResourceTree.vue` | 创建 | 资源树表格组件 |
| `ui/src/components/resource/ResourceForm.vue` | 创建 | 资源编辑表单弹窗 |
| `ui/src/views/ResourceList.vue` | 创建 | 资源管理页面 |

---

### Task 1: 创建ResourceTree组件

**Files:**
- Create: `ui/src/components/resource/ResourceTree.vue`

- [ ] **Step 1: 创建组件目录**

```bash
mkdir -p ui/src/components/resource
```

- [ ] **Step 2: 创建 ResourceTree.vue**

```vue
<template>
  <div class="resource-tree-container">
    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button
        v-if="canCreate.value"
        class="create-button"
        @click="handleCreateRoot"
      >
        <el-icon><Plus /></el-icon>
        新增根资源
      </el-button>
      <el-button class="refresh-button" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <!-- 资源树表格 -->
    <el-table
      :data="resources"
      row-key="id"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      default-expand-all
      v-loading="loading"
      class="resource-table"
    >
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="code" label="编码" width="150" />
      <el-table-column prop="path" label="路径" min-width="180">
        <template #default="{ row }">
          <span v-if="row.path">{{ row.path }}</span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status ? 'success' : 'danger'" effect="plain" size="small">
            {{ row.status ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button
            v-if="canCreate.value"
            link
            size="small"
            @click="handleCreateChild(row)"
          >
            新增子资源
          </el-button>
          <el-button
            v-if="canUpdate.value"
            link
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="canDelete.value"
            link
            size="small"
            class="delete-btn"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
          <el-button
            v-if="canUpdate.value"
            link
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { usePermission } from '../../composables/usePermission'
import { resourceApi } from '../../api'

const props = defineProps({
  type: { type: String, default: '' } // MENU, OPERATION, API
})

const emit = defineEmits(['create', 'edit', 'refresh'])

const { hasPermission } = usePermission()
const canCreate = hasPermission('RESOURCE_MANAGE', 'CREATE')
const canUpdate = hasPermission('RESOURCE_MANAGE', 'UPDATE')
const canDelete = hasPermission('RESOURCE_MANAGE', 'DELETE')

const resources = ref([])
const loading = ref(false)

// 加载资源树
const loadResources = async () => {
  loading.value = true
  try {
    const tree = props.type
      ? await resourceApi.tree()
      : await resourceApi.tree()
    // 根据类型过滤
    if (props.type) {
      resources.value = filterByType(tree, props.type)
    } else {
      resources.value = tree
    }
  } finally {
    loading.value = false
  }
}

// 递归过滤资源类型
const filterByType = (tree, type) => {
  return tree
    .filter(node => node.type === type)
    .map(node => ({
      ...node,
      children: node.children ? filterByType(node.children, type) : []
    }))
}

// 新增根资源
const handleCreateRoot = () => {
  emit('create', { parentId: null, type: props.type })
}

// 新增子资源
const handleCreateChild = (row) => {
  emit('create', { parentId: row.id, parentName: row.name, type: props.type })
}

// 编辑
const handleEdit = (row) => {
  emit('edit', row)
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除资源 "${row.name}"？删除后不可恢复。`,
      '提示',
      { type: 'warning' }
    )
    await resourceApi.delete(row.id)
    ElMessage.success('删除成功')
    await loadResources()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}

// 启用/禁用
const handleToggleStatus = async (row) => {
  try {
    if (row.status) {
      await resourceApi.disable(row.id)
      ElMessage.success('已禁用')
    } else {
      await resourceApi.enable(row.id)
      ElMessage.success('已启用')
    }
    await loadResources()
  } catch (error) {
    // error handled by interceptor
  }
}

// 刷新
const handleRefresh = async () => {
  await loadResources()
}

onMounted(() => {
  loadResources()
})

// 暴露刷新方法
defineExpose({ loadResources })
</script>

<style lang="scss" scoped>
.resource-tree-container {
  .action-bar {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;

    .create-button {
      @include gradient-button;
    }

    .refresh-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
    }
  }

  .resource-table {
    border-radius: 8px;

    :deep(.el-table__header-wrapper th) {
      background: var(--color-bg-elevated);
      color: var(--color-text-primary);
    }

    :deep(.el-table__row:hover > td) {
      background: var(--color-bg-glass) !important;
    }

    .text-muted {
      color: var(--color-text-muted);
    }

    .delete-btn {
      color: var(--el-color-danger);
    }
  }
}
</style>
```

- [ ] **Step 3: 验证创建成功**

Run: `ls -la ui/src/components/resource/ResourceTree.vue`
Expected: 文件存在

---

### Task 2: 创建ResourceForm组件

**Files:**
- Create: `ui/src/components/resource/ResourceForm.vue`

- [ ] **Step 1: 创建 ResourceForm.vue**

```vue
<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑资源' : (parentId ? '新增子资源' : '新增根资源')"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
      v-loading="loading"
    >
      <el-form-item label="资源编码" prop="code" v-if="!isEdit">
        <el-input v-model="form.code" placeholder="如 USER_MANAGE" />
      </el-form-item>

      <el-form-item label="资源名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入资源名称" />
      </el-form-item>

      <el-form-item label="父资源" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          check-strictly
          clearable
          placeholder="选择父资源（可选）"
          class="full-width"
        />
      </el-form-item>

      <el-form-item label="资源类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择类型" :disabled="isEdit">
          <el-option label="菜单" value="MENU" />
          <el-option label="操作" value="OPERATION" />
          <el-option label="API" value="API" />
        </el-select>
      </el-form-item>

      <el-form-item label="路径" prop="path" v-if="showPath">
        <el-input v-model="form.path" placeholder="如 /system/users" />
      </el-form-item>

      <el-form-item label="路径模式" prop="pathPattern" v-if="form.type === 'API'">
        <el-input v-model="form.pathPattern" placeholder="如 /api/users/**" />
      </el-form-item>

      <el-form-item label="请求方法" prop="method" v-if="form.type === 'API'">
        <el-select v-model="form.method" placeholder="请选择方法">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>

      <el-form-item label="图标" prop="icon" v-if="form.type === 'MENU'">
        <el-input v-model="form.icon" placeholder="如 User" />
      </el-form-item>

      <el-form-item label="组件" prop="component" v-if="form.type === 'MENU'">
        <el-input v-model="form.component" placeholder="如 views/UserList" />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="form.sort" :min="0" :max="999" />
      </el-form-item>

      <el-form-item label="状态" prop="status" v-if="isEdit">
        <el-radio-group v-model="form.status">
          <el-radio :value="true">启用</el-radio>
          <el-radio :value="false">禁用</el-radio>
        </el-radio-group>
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
import { resourceApi } from '../../api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  resourceId: { type: Number, default: null },
  parentId: { type: Number, default: null },
  parentName: { type: String, default: '' },
  type: { type: String, default: '' },
  allResources: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => props.resourceId !== null)
const showPath = computed(() => form.type === 'MENU' || form.type === 'API')

const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)

const form = reactive({
  code: '',
  name: '',
  parentId: null,
  type: 'MENU',
  path: '',
  pathPattern: '',
  method: '',
  icon: '',
  component: '',
  sort: 0,
  status: true
})

const rules = {
  code: [
    { required: true, message: '请输入资源编码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入资源名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度2-50字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  path: [
    { required: true, message: '请输入路径', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择请求方法', trigger: 'change' }
  ]
}

// 父资源选项（同类型）
const parentOptions = computed(() => {
  const filtered = filterByType(props.allResources, props.type || form.type)
  // 编辑时排除当前资源
  if (isEdit.value) {
    return filterCurrentResource(filtered, props.resourceId)
  }
  return filtered
})

const filterByType = (tree, type) => {
  return tree.filter(node => node.type === type).map(node => ({
    value: node.id,
    label: node.name,
    children: node.children ? filterByType(node.children, type) : undefined
  }))
}

const filterCurrentResource = (tree, excludeId) => {
  return tree.filter(node => node.value !== excludeId).map(node => ({
    ...node,
    children: node.children ? filterCurrentResource(node.children, excludeId) : undefined
  }))
}

// 监听打开
watch(visible, async (val) => {
  if (val) {
    formRef.value?.resetFields()
    if (isEdit.value) {
      loading.value = true
      try {
        const res = await resourceApi.get(props.resourceId)
        form.code = res.code
        form.name = res.name
        form.parentId = res.parentId || null
        form.type = res.type
        form.path = res.path || ''
        form.pathPattern = res.pathPattern || ''
        form.method = res.method || ''
        form.icon = res.icon || ''
        form.component = res.component || ''
        form.sort = res.sort || 0
        form.status = res.status
      } finally {
        loading.value = false
      }
    } else {
      form.code = ''
      form.name = ''
      form.parentId = props.parentId || null
      form.type = props.type || 'MENU'
      form.path = ''
      form.pathPattern = ''
      form.method = ''
      form.icon = ''
      form.component = ''
      form.sort = 0
      form.status = true
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
      type: form.type,
      path: form.path,
      pathPattern: form.pathPattern,
      method: form.method,
      icon: form.icon,
      component: form.component,
      sort: form.sort,
      status: form.status ? 'ENABLED' : 'DISABLED'
    }

    if (isEdit.value) {
      await resourceApi.update(props.resourceId, data)
      ElMessage.success('更新成功')
    } else {
      data.code = form.code
      await resourceApi.create(data)
      ElMessage.success('创建成功')
    }

    emit('success')
    visible.value = false
  } catch (error) {
    // validation or API error
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

Run: `ls -la ui/src/components/resource/ResourceForm.vue`
Expected: 文件存在

---

### Task 3: 创建ResourceList页面

**Files:**
- Create: `ui/src/views/ResourceList.vue`

- [ ] **Step 1: 创建 ResourceList.vue**

```vue
<template>
  <Navbar />
  <div class="resource-list-container">
    <div class="content-card">
      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" class="resource-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="菜单资源" name="MENU" />
        <el-tab-pane label="操作资源" name="OPERATION" />
        <el-tab-pane label="API资源" name="API" />
      </el-tabs>

      <!-- 资源树表格 -->
      <ResourceTree
        ref="treeRef"
        :type="activeTab"
        @create="handleCreate"
        @edit="handleEdit"
        @refresh="handleRefresh"
      />
    </div>

    <!-- 编辑弹窗 -->
    <ResourceForm
      v-model="formVisible"
      :resource-id="editResourceId"
      :parent-id="createParentId"
      :parent-name="createParentName"
      :type="activeTab"
      :all-resources="allResources"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Navbar from '../components/Navbar.vue'
import ResourceTree from '../components/resource/ResourceTree.vue'
import ResourceForm from '../components/resource/ResourceForm.vue'
import { resourceApi } from '../api'

const activeTab = ref('MENU')
const treeRef = ref()
const allResources = ref([])

const formVisible = ref(false)
const editResourceId = ref(null)
const createParentId = ref(null)
const createParentName = ref('')

// 加载全部资源（用于父资源选择）
const loadAllResources = async () => {
  allResources.value = await resourceApi.tree()
}

// Tab切换
const handleTabChange = () => {
  treeRef.value?.loadResources()
}

// 新增资源
const handleCreate = ({ parentId, parentName, type }) => {
  editResourceId.value = null
  createParentId.value = parentId
  createParentName.value = parentName || ''
  formVisible.value = true
}

// 编辑资源
const handleEdit = (row) => {
  editResourceId.value = row.id
  createParentId.value = null
  createParentName.value = ''
  formVisible.value = true
}

// 刷新
const handleRefresh = async () => {
  await loadAllResources()
}

// 表单保存成功
const handleFormSuccess = async () => {
  await loadAllResources()
  await treeRef.value?.loadResources()
}

onMounted(() => {
  loadAllResources()
})
</script>

<style lang="scss" scoped>
.resource-list-container {
  padding: 84px 24px 24px 24px;
  min-height: 100vh;
  background: var(--color-bg-base);

  .content-card {
    max-width: 1200px;
    margin: 0 auto;
    @include glass-card;
    padding: 24px;
  }

  .resource-tabs {
    margin-bottom: 16px;

    :deep(.el-tabs__item) {
      color: var(--color-text-secondary);
      font-size: 16px;

      &.is-active {
        color: var(--color-primary);
      }
    }
  }
}
</style>
```

- [ ] **Step 2: 验证创建成功**

Run: `ls -la ui/src/views/ResourceList.vue`
Expected: 文件存在

---

### Task 4: 提交代码

- [ ] **Step 1: Git提交**

```bash
cd ui
git add src/components/resource/ src/views/ResourceList.vue
git commit -m "feat(resource): implement resource management page with type tabs and tree table"
```

---

## 验收标准

- [ ] ResourceTree组件显示资源树表格，按类型过滤
- [ ] ResourceForm弹窗支持创建和编辑资源，表单校验正确
- [ ] ResourceList页面Tab切换正确（菜单/操作/API）
- [ ] 按钮级权限控制生效
- [ ] 代码已提交

---

## 下一步

完成此计划后，执行 `05-permission-management.md` 创建权限管理页面。