# 角色权限配置

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/user/role/permission/:id` |
| **功能** | 配置角色的模块-功能-操作权限 |
| **权限** | `permission:config` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│              │   首页 > 用户管理 > 角色管理 > 权限配置                       │
│              │   ─────────────────────────────────────────                   │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │                                                        ││
│              │   │  角色: 管理员                                          ││
│              │   │  描述: 系统管理员，拥有系统全部权限                      ││
│              │   │  类型: 系统角色                                        ││
│              │   │                                                        ││
│              │   │  ─── 权限配置 ──────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  ┌────────────────────────────────────────────────────┐││
│              │   │  │ [展开全部] [折叠全部] [全选] [取消全选]              │││
│              │   │  │                                                    │││
│              │   │  │ ☑ 用户管理 ──────────────────────────── [▼]        │││
│              │   │  │   ├─ ☑ 用户列表                                     │││
│              │   │  │   │   ├─ ☑ 查看                                     │││
│              │   │  │   │   ├─ ☑ 新建                                     │││
│              │   │  │   │   ├─ ☑ 编辑                                     │││
│              │   │  │   │   └─ ☑ 删除                                     │││
│              │   │  │   ├─ ☑ 角色管理                                     │││
│              │   │  │   │   ├─ ☑ 查看                                     │││
│              │   │  │   │   ├─ ☑ 新建                                     │││
│              │   │  │   │   ├─ ☑ 编辑                                     │││
│              │   │  │   │   └─ ☑ 删除                                     │││
│              │   │  │   └─ ☑ 权限配置                                     │││
│              │   │  │       └─ ☑ 配置权限                                 │││
│              │   │  │                                                    │││
│              │   │  │ ☐ 财务管理 ──────────────────────────── [▶]        │││
│              │   │  │                                                    │││
│              │   │  │ ☑ 订单管理 ──────────────────────────── [▼]        │││
│              │   │  │   ├─ ☑ 订单列表                                     │││
│              │   │  │   │   ├─ ☑ 查看                                     │││
│              │   │  │   │   ├─ ☐ 新建                                     │││
│              │   │  │   │   ├─ ☐ 编辑                                     │││
│              │   │  │   │   └─ ☑ 删除                                     │││
│              │   │  │   └─ ☐ 订单审批                                     │││
│              │   │  │       ├─ ☐ 查看                                     │││
│              │   │  │       └─ ☐ 审批                                     │││
│              │   │  │                                                    │││
│              │   │  │ ☐ 系统设置 ──────────────────────────── [▶]        │││
│              │   │  │                                                    │││
│              │   │  │ ☑ 审计日志 ──────────────────────────── [▼]        │││
│              │   │  │   ├─ ☑ 登录日志                                     │││
│              │   │  │   │   └─ ☑ 查看                                     │││
│              │   │  │   └─ ☑ 操作日志                                     │││
│              │   │  │       └─ ☑ 查看                                     │││
│              │   │  │                                                    │││
│              │   │  └────────────────────────────────────────────────────┘││
│              │   │                                                        ││
│              │   │  ┌────────────────┐                                    │ │
│              │   │  │    取消        │  [保存权限配置]                    │ │
│              │   │  └────────────────┘                                    │ │
│              │   │                                                        ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 权限树结构

权限采用三级树形结构：

```
模块 (Level 1)
├─ 功能 (Level 2)
│   ├─ 操作权限 (Level 3)
│   ├─ 操作权限
│   └─ 操作权限
└─ 功能
    └─ 操作权限
```

### 权限树示例

| 模块 | 功能 | 操作权限 |
|------|-----|---------|
| 用户管理 | 用户列表 | 查看、新建、编辑、删除 |
| 用户管理 | 角色管理 | 查看、新建、编辑、删除 |
| 用户管理 | 权限配置 | 配置权限 |
| 订单管理 | 订单列表 | 查看、新建、编辑、删除 |
| 订单管理 | 订单审批 | 查看、审批 |
| 审计日志 | 登录日志 | 查看 |
| 审计日志 | 操作日志 | 查看 |
| 系统设置 | 菜单管理 | 查看、新建、编辑、删除 |
| 系统设置 | 系统配置 | 查看、编辑 |

---

## 权限树交互逻辑

### 级联勾选

```
勾选父节点 → 自动勾选所有子节点

示例:
☑ 用户管理 (勾选)
  → ☑ 用户列表 (自动勾选)
      → ☑ 查看 (自动勾选)
      → ☑ 新建 (自动勾选)
      → ☑ 编辑 (自动勾选)
      → ☑ 删除 (自动勾选)
  → ☑ 角色管理 (自动勾选)
      → ☑ 查看 (自动勾选)
      → ...
```

### 级联取消

```
取消父节点 → 自动取消所有子节点

示例:
☐ 用户管理 (取消)
  → ☐ 用户列表 (自动取消)
      → ☐ 查看 (自动取消)
      → ☐ 新建 (自动取消)
      → ...
```

### 向上联动

```
子节点全部勾选 → 父节点自动勾选

子节点部分勾选 → 父节点显示半选状态 (indeterminate)

示例:
☑ 用户列表
    ├─ ☑ 查看
    ├─ ☑ 新建
    ├─ ☐ 编辑  ← 部分勾选
    └─ ☐ 删除
  → 用户列表显示半选状态 (可视化为一个横线)
```

### 展开/折叠

```
点击 [▼]/[▶] 或双击节点名:
- [▼] 展开，显示子节点
- [▶] 折叠，隐藏子节点

默认状态:
- 有勾选子节点的模块默认展开
- 无勾选子节点的模块默认折叠
```

### 快捷操作

| 按钮 | 行为 |
|------|-----|
| **展开全部** | 展开所有模块节点 |
| **折叠全部** | 折叠所有模块节点 |
| **全选** | 勾选所有权限节点 |
| **取消全选** | 取消所有权限节点 |

---

## 权限树 CSS 规范

```css
.permission-tree {
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
}

.tree-node {
  display: flex;
  align-items: center;
  padding: 8px 0;
  font-size: var(--font-size-base);
}

.tree-node.module {
  font-weight: 600;
}

.tree-node.function {
  padding-left: 24px;
  font-weight: 500;
}

.tree-node.action {
  padding-left: 48px;
}

.tree-checkbox {
  width: 18px;
  height: 18px;
  margin-right: 8px;
}

.tree-checkbox.indeterminate {
  /* 半选状态样式 */
}

.tree-expand-icon {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  cursor: pointer;
}

.tree-label {
  flex: 1;
}
```

---

## 权限数据结构

```javascript
// 权限树数据结构
const permissionTree = [
  {
    id: 'user-management',
    name: '用户管理',
    level: 'module',
    checked: true,          // 是否勾选
    indeterminate: false,   // 是否半选
    expanded: true,         // 是否展开
    children: [
      {
        id: 'user-list',
        name: '用户列表',
        level: 'function',
        checked: true,
        indeterminate: false,
        children: [
          { id: 'user-view', name: '查看', level: 'action', checked: true },
          { id: 'user-create', name: '新建', level: 'action', checked: true },
          { id: 'user-edit', name: '编辑', level: 'action', checked: true },
          { id: 'user-delete', name: '删除', level: 'action', checked: true }
        ]
      },
      {
        id: 'role-management',
        name: '角色管理',
        level: 'function',
        checked: true,
        children: [
          { id: 'role-view', name: '查看', level: 'action', checked: true },
          { id: 'role-create', name: '新建', level: 'action', checked: true },
          { id: 'role-edit', name: '编辑', level: 'action', checked: true },
          { id: 'role-delete', name: '删除', level: 'action', checked: true }
        ]
      }
    ]
  },
  // ... 更多模块
]
```

---

## 保存流程

```
1. 用户勾选/取消权限节点
2. 点击「保存权限配置」按钮
3. 按钮显示 loading
4. 发送 API 请求:
   POST /api/roles/:id/permissions
   Body: { permissions: ['user-view', 'user-create', ...] }
5. 成功:
   - Toast 提示「权限配置已保存」
   - 返回角色列表 或 停留在当前页
6. 失败:
   - Toast 提示错误信息
   - 恢复按钮状态
```

---

## Vue 组件示例

```vue
<template>
  <div class="permission-config-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 角色信息卡片 -->
    <div class="role-info-card">
      <div class="role-info">
        <div class="role-name">角色: {{ role.name }}</div>
        <div class="role-description">描述: {{ role.description }}</div>
        <div class="role-type">类型: {{ role.type === 'system' ? '系统角色' : '业务角色' }}</div>
      </div>
    </div>

    <!-- 权限配置 -->
    <div class="permission-section">
      <h3 class="section-title">权限配置</h3>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <Button size="small" @click="expandAll">展开全部</Button>
        <Button size="small" @click="collapseAll">折叠全部</Button>
        <Button size="small" @click="selectAll">全选</Button>
        <Button size="small" @click="unselectAll">取消全选</Button>
      </div>

      <!-- 权限树 -->
      <div class="permission-tree">
        <TreeNode
          v-for="node in permissionTree"
          :key="node.id"
          :node="node"
          :level="0"
          @check="handleCheck"
          @expand="handleExpand"
        />
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <Button type="secondary" @click="handleCancel">取消</Button>
      <Button
        type="primary"
        :loading="saving"
        @click="handleSave"
      >
        保存权限配置
      </Button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const roleId = computed(() => route.params.id)

const breadcrumbItems = computed(() => [
  { label: '首页', route: '/home' },
  { label: '用户管理', route: '/user' },
  { label: '角色管理', route: '/user/role' },
  { label: '权限配置' }
])

// 角色信息
const role = ref({ name: '', description: '', type: '' })

// 权限树数据
const permissionTree = ref([])
const saving = ref(false)

// 加载角色和权限数据
const loadData = async () => {
  const [roleRes, permRes] = await Promise.all([
    api.getRole(roleId.value),
    api.getRolePermissions(roleId.value)
  ])
  role.value = roleRes.data
  permissionTree.value = buildPermissionTree(permRes.data.allPermissions, permRes.data.rolePermissions)
}

// 构建权限树
const buildPermissionTree = (allPermissions, rolePermissions) => {
  // 根据 allPermissions 结构构建树
  // 根据 rolePermissions 设置 checked 状态
  // ...
}

// 处理勾选
const handleCheck = (node, checked) => {
  // 更新 node.checked
  // 级联更新子节点
  // 向上联动更新父节点
  updateNodeCheck(node, checked)
  cascadeToChildren(node, checked)
  cascadeToParent(node)
}

// 处理展开/折叠
const handleExpand = (node) => {
  node.expanded = !node.expanded
}

// 快捷操作
const expandAll = () => {
  setAllExpanded(permissionTree.value, true)
}

const collapseAll = () => {
  setAllExpanded(permissionTree.value, false)
}

const selectAll = () => {
  setAllChecked(permissionTree.value, true)
}

const unselectAll = () => {
  setAllChecked(permissionTree.value, false)
}

// 保存
const handleSave = async () => {
  const permissions = collectCheckedPermissions(permissionTree.value)
  saving.value = true
  try {
    await api.updateRolePermissions(roleId.value, permissions)
    showToast('权限配置已保存')
    router.push('/user/role')
  } catch (err) {
    showToast(err.message, 'error')
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/user/role')
}

onMounted(() => {
  loadData()
})
</script>
```

---

## TreeNode 组件

```vue
<template>
  <div class="tree-node" :class="nodeClass">
    <!-- 展开/折叠按钮 -->
    <span
      v-if="hasChildren"
      class="tree-expand-icon"
      @click="$emit('expand', node)"
    >
      <ChevronDown v-if="node.expanded" />
      <ChevronRight v-else />
    </span>

    <!-- 复选框 -->
    <input
      type="checkbox"
      class="tree-checkbox"
      :checked="node.checked"
      :indeterminate.prop="node.indeterminate"
      @change="$emit('check', node, $event.target.checked)"
    />

    <!-- 节点名称 -->
    <span class="tree-label">{{ node.name }}</span>

    <!-- 子节点 -->
    <div v-if="hasChildren && node.expanded" class="tree-children">
      <TreeNode
        v-for="child in node.children"
        :key="child.id"
        :node="child"
        :level="level + 1"
        @check="(n, c) => $emit('check', n, c)"
        @expand="(n) => $emit('expand', n)"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  node: Object,
  level: Number
})

const emit = defineEmits(['check', 'expand'])

const hasChildren = computed(() => props.node.children?.length > 0)

const nodeClass = computed(() => {
  const classes = [`level-${props.node.level}`]
  if (props.node.checked) classes.push('checked')
  if (props.node.indeterminate) classes.push('indeterminate')
  return classes
})
</script>

<style scoped>
.tree-node {
  padding-left: calc(var(--level) * 24px);
}

.tree-node.level-module {
  font-weight: 600;
}

.tree-node.level-function {
  font-weight: 500;
}

.tree-node.level-action {
  font-weight: 400;
}
</style>
```