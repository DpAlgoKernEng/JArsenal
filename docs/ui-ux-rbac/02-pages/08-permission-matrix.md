# 权限矩阵视图

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/permission/matrix` |
| **功能** | 批量对比各角色的权限配置 |
| **权限** | `permission:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│              │   首页 > 用户管理 > 权限矩阵                                   │
│              │   ─────────────────────────────                                │
│              │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ [树形视图] [矩阵视图]                     角色筛选:    │ │
│              │   │                                          [▼ 全部角色] │ │
│              │   └───────────────────────────────────────────────────────┘ │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │              │管理员│财务│普通用户│销售│HR │审批员│    ││
│              │   │──────────────┼──────┼────┼────────┼────┼───┼──────┤    ││
│              │   │ 用户管理     │      │    │        │    │   │      │    ││
│              │   │ ├─查看       │  ✓  │ ✓ │   ✓   │ ✓ │ ✓ │  ✓  │    ││
│              │   │ ├─新建       │  ✓  │ ✗ │   ✗   │ ✗ │ ✓ │  ✗  │    ││
│              │   │ ├─编辑       │  ✓  │ ✓ │   ✗   │ ✗ │ ✓ │  ✗  │    ││
│              │   │ └─删除       │  ✓  │ ✗ │   ✗   │ ✗ │ ✗ │  ✗  │    ││
│              │   │──────────────┼──────┼────┼────────┼────┼───┼──────┤    ││
│              │   │ 角色管理     │      │    │        │    │   │      │    ││
│              │   │ ├─查看       │  ✓  │ ✓ │   ✓   │ ✗ │ ✓ │  ✓  │    ││
│              │   │ ├─新建       │  ✓  │ ✗ │   ✗   │ ✗ │ ✗ │  ✗  │    ││
│              │   │ ├─编辑       │  ✓  │ ✗ │   ✗   │ ✗ │ ✗ │  ✗  │    ││
│              │   │ └─删除       │  ✓  │ ✗ │   ✗   │ ✗ │ ✗ │  ✗  │    ││
│              │   │──────────────┼──────┼────┼────────┼────┼───┼──────┤    ││
│              │   │ 订单管理     │      │    │        │    │   │      │    ││
│              │   │ ├─查看       │  ✓  │ ✓ │   ✓   │ ✓ │ ✗ │  ✓  │    ││
│              │   │ ├─新建       │  ✓  │ ✗ │   ✗   │ ✓ │ ✗ │  ✗  │    ││
│              │   │ └─审批       │  ✓  │ ✓ │   ✗   │ ✗ │ ✗ │  ✓  │    ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   [导出矩阵] [导入权限配置]                                   │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 视图切换

| 视图 | 说明 |
|------|-----|
| **树形视图** | 单角色权限树，适合单个角色配置 |
| **矩阵视图** | 多角色权限对比，适合批量查看 |

---

## 矩阵表格结构

```
            │ 角色1 │ 角色2 │ 角色3 │ ...
───────────┼───────┼───────┼───────┼────
模块/功能   │       │       │       │
├─ 操作1   │  ✓   │  ✗   │  ✓   │
├─ 操作2   │  ✓   │  ✓   │  ✗   │
└─ 操作3   │  ✗   │  ✗   │  ✓   │
```

### 行层级

| 层级 | 样式 | 说明 |
|------|-----|-----|
| **模块行** | 粗体、背景浅灰 | 模块名称，无勾选标记 |
| **功能行** | 中等粗体 | 功能名称，可能含勾选 |
| **操作行** | 正常粗体 | 操作权限，含勾选 ✓/✗ |

---

## 单元格内容

### 权限状态

| 状态 | 显示 | 颜色 |
|------|-----|-----|
| **有权限** | ✓ 绿色勾 | `--color-accent` |
| **无权限** | ✗ 灰色叉 | `--color-muted-foreground` |

### 单元格点击交互

```
点击单元格弹出详情:
┌────────────────────────┐
│ 当前: ✓               │
│ 点击切换为 ✗          │
│ 或弹出详情:           │
│ ─────────────────────  │
│ 用户管理-查看         │
│ 授权角色: 管理员      │
│ 授权时间: 2024-01-15  │
│ 授权人: system        │
└────────────────────────┘

权限详情弹窗:
┌──────────────────────────────────┐
│ 权限详情                          │
│ ─────────────────────────────────│
│ 权限名称: 用户管理-用户列表-查看 │
│ 角色: 管理员                      │
│ 授权时间: 2024-01-15 10:23:15     │
│ 授权人: system                    │
│ ─────────────────────────────────│
│ [关闭]                            │
└──────────────────────────────────┘
```

---

## 角色筛选

```
角色筛选: [▼ 全部角色]

选项:
- 全部角色 (显示所有角色列)
- 系统角色 (仅显示管理员、普通用户)
- 业务角色 (仅显示财务、销售、HR等)
- 自定义选择 (勾选特定角色)
```

---

## 批量操作

### 导出矩阵

```
点击「导出矩阵」:
- 生成 Excel 文件
- 内容: 权限矩阵表格
- 格式: 模块 | 功能 | 操作 | 角色1 | 角色2 | ...
- 文件名: permission_matrix_YYYYMMDD.xlsx
```

### 导入权限配置

```
点击「导入权限配置」:
1. 打开文件选择弹窗
2. 选择 Excel/CSV 文件
3. 验证文件格式
4. 预览导入内容
5. 确认导入
6. 更新角色权限
```

---

## CSS 规范

```css
.permission-matrix {
  overflow-x: auto;
}

.matrix-table {
  width: 100%;
  border-collapse: collapse;
}

.matrix-table th {
  padding: 12px;
  background: var(--color-muted);
  font-weight: 600;
  border: 1px solid var(--color-border);
}

.matrix-table td {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  text-align: center;
}

.matrix-table td.module {
  font-weight: 600;
  background: var(--color-muted);
}

.matrix-table td.function {
  font-weight: 500;
  padding-left: 24px;
}

.matrix-table td.action {
  padding-left: 48px;
}

.matrix-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.matrix-cell.has-permission {
  color: var(--color-accent);
}

.matrix-cell.no-permission {
  color: var(--color-muted-foreground);
}

.matrix-cell:hover {
  background: var(--color-muted);
}
```

---

## Vue 组件示例

```vue
<template>
  <div class="permission-matrix-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 视图切换和筛选 -->
    <div class="toolbar">
      <div class="view-toggle">
        <Button
          :type="viewMode === 'tree' ? 'primary' : 'secondary'"
          @click="viewMode = 'tree'"
        >
          树形视图
        </Button>
        <Button
          :type="viewMode === 'matrix' ? 'primary' : 'secondary'"
          @click="viewMode = 'matrix'"
        >
          矩阵视图
        </Button>
      </div>
      <FilterSelect
        v-model="selectedRoles"
        label="角色筛选"
        :options="roleOptions"
        multiple
      />
    </div>

    <!-- 矩阵表格 -->
    <div class="permission-matrix">
      <table class="matrix-table">
        <thead>
          <tr>
            <th class="permission-col">权限</th>
            <th v-for="role in filteredRoles" :key="role.id">
              {{ role.name }}
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-for="module in permissionModules" :key="module.id">
            <!-- 模块行 -->
            <tr class="module-row">
              <td class="module">{{ module.name }}</td>
              <td v-for="role in filteredRoles" :key="role.id"></td>
            </tr>
            <!-- 功能和操作行 -->
            <template v-for="func in module.functions" :key="func.id">
              <tr class="function-row" v-if="func.actions.length === 0">
                <td class="function">{{ func.name }}</td>
                <td v-for="role in filteredRoles" :key="role.id">
                  <MatrixCell
                    :has-permission="hasPermission(role.id, func.id)"
                    @click="showPermissionDetail(role.id, func.id)"
                  />
                </td>
              </tr>
              <template v-else>
                <tr class="function-row">
                  <td class="function">{{ func.name }}</td>
                  <td v-for="role in filteredRoles" :key="role.id"></td>
                </tr>
                <tr v-for="action in func.actions" :key="action.id" class="action-row">
                  <td class="action">{{ action.name }}</td>
                  <td v-for="role in filteredRoles" :key="role.id">
                    <MatrixCell
                      :has-permission="hasPermission(role.id, action.id)"
                      @click="showPermissionDetail(role.id, action.id)"
                    />
                  </td>
                </tr>
              </template>
            </template>
          </template>
        </tbody>
      </table>
    </div>

    <!-- 操作按钮 -->
    <div class="actions">
      <Button icon="download" @click="handleExport">
        导出矩阵
      </Button>
      <Button icon="upload" @click="openImportDialog">
        导入权限配置
      </Button>
    </div>

    <!-- 权限详情弹窗 -->
    <PermissionDetailDialog
      v-model:visible="detailDialogVisible"
      :permission="selectedPermission"
    />

    <!-- 导入弹窗 -->
    <PermissionImportDialog
      v-model:visible="importDialogVisible"
      @success="loadData"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const viewMode = ref('matrix')
const selectedRoles = ref([])
const permissionModules = ref([])
const rolePermissions = ref({}) // { roleId: [permissionIds] }
const detailDialogVisible = ref(false)
const importDialogVisible = ref(false)
const selectedPermission = ref(null)

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '权限矩阵' }
]

const filteredRoles = computed(() => {
  if (!selectedRoles.value.length) return roleOptions.value
  return roleOptions.value.filter(r => selectedRoles.value.includes(r.id))
})

const hasPermission = (roleId, permissionId) => {
  return rolePermissions.value[roleId]?.includes(permissionId)
}

const showPermissionDetail = (roleId, permissionId) => {
  selectedPermission.value = {
    roleId,
    permissionId,
    roleName: roleOptions.value.find(r => r.id === roleId)?.name,
    permissionName: findPermissionName(permissionId)
  }
  detailDialogVisible.value = true
}

const handleExport = async () => {
  const res = await api.exportPermissionMatrix()
  downloadFile(res.data, 'permission_matrix.xlsx')
}

const openImportDialog = () => {
  importDialogVisible.value = true
}

const loadData = async () => {
  const [modulesRes, permissionsRes, rolesRes] = await Promise.all([
    api.getPermissionModules(),
    api.getAllRolePermissions(),
    api.getRoles()
  ])
  permissionModules.value = modulesRes.data
  rolePermissions.value = permissionsRes.data
  roleOptions.value = rolesRes.data
}

onMounted(() => {
  loadData()
})
</script>
```

---

## MatrixCell 组件

```vue
<template>
  <span
    class="matrix-cell"
    :class="hasPermission ? 'has' : 'no'"
    @click="$emit('click')"
  >
    {{ hasPermission ? '✓' : '✗' }}
  </span>
</template>

<script setup>
defineProps({
  hasPermission: Boolean
})

defineEmits(['click'])
</script>

<style scoped>
.matrix-cell {
  display: inline-flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-size-lg);
}

.matrix-cell.has {
  color: var(--color-accent);
}

.matrix-cell.no {
  color: var(--color-muted-foreground);
}

.matrix-cell:hover {
  background: var(--color-muted);
}
</style>
```