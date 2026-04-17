# 菜单管理

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/system/menu` |
| **功能** | 动态菜单配置，管理侧边栏导航结构 |
| **权限** | `menu:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  ⚙️ 系统管理 │   馀页 > 系统管理 > 菜单管理                                   │
│  ●           │   ─────────────────────────────                                │
│   └ 菜单管理 │                                                               │
│   └ 系统设置 │   菜单管理                                                     │
│              │   ────────────────────────────────────────────────────────── │
│              │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ [新建菜单] [刷新]                      搜索: [      ] │ │
│              │   └───────────────────────────────────────────────────────┘ │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 排序 │ 名称     │ 类型 │ 路由        │图标│状态│操作 ││
│              │   ├──────┼──────────┼──────┼─────────────┼────┼────┼─────┤│
│              │   │  1  │ 首页     │ 目录 │ /home       │ 🏠 │启用│ ⋮  ││
│              │   │  2  │ 用户管理 │ 目录 │ /user       │ 👥 │启用│ ⋮  ││
│              │   │      │ ├─用户列表│菜单 │ /user/list │    │启用│ ⋮  ││
│              │   │      │ ├─角色管理│菜单 │ /user/role │    │启用│ ⋮  ││
│              │   │      │ └─权限配置│菜单 │ /user/perm │    │启用│ ⋮  ││
│              │   │  3  │ 审计日志 │ 目录 │ /log       │ 📋 │启用│ ⋮  ││
│              │   │      │ ├─登录日志│菜单 │ /log/login │    │启用│ ⋮  ││
│              │   │      │ └─操作日志│菜单 │ /log/op    │    │启用│ ⋮  ││
│              │   │  4  │ 系统管理 │ 目录 │ /system    │ ⚙️ │启用│ ⋮  ││
│              │   │      │ ├─菜单管理│菜单 │ /sys/menu  │    │启用│ ⋮  ││
│              │   │      │ └─系统设置│菜单 │ /sys/setting│   │启用│ ⋮  ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   ─── 菜单树预览 ───                                           │
│              │                                                               │
│              │   🏠 首页                                                      │
│              │   👥 用户管理                                                  │
│              │      ├─ 用户列表                                               │
│              │      ├─ 角色管理                                               │
│              │      └─ 权限配置                                               │
│              │   📋 审计日志                                                  │
│              │   ⚙️ 系统管理                                                  │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 菜单类型

| 类型 | 说明 | 特性 |
|------|-----|-----|
| **目录** | 顶部层级菜单节点 | 可含子菜单，无实际页面 |
| **菜单** | 实际页面菜单项 | 跳转到具体路由页面 |
| **按钮** | 页面内操作按钮权限 | 不显示在导航，仅用于权限控制 |

---

## 表格列定义

| 列 | 宽度 | 内容 | 说明 |
|------|-----|-----|-----|
| **排序** | 60px | 排序号 | 用于同级菜单排序 |
| **名称** | 200px | 菜单名称 | 树形层级显示 |
| **类型** | 80px | 目录/菜单/按钮 | 标签显示 |
| **路由** | 200px | 路由路径 | 如 `/user/list` |
| **图标** | 60px | 菜单图标 | Icon 显示 |
| **状态** | 80px | 启用/禁用 | 标签显示 |
| **操作** | 80px | ⋮ 菜单 | |

---

## 操作菜单

```
┌──────────────────┐
│ ➕ 新增子菜单     │  ← 在当前菜单下新增子菜单
│ ✏ 编辑菜单       │  ← 编辑当前菜单信息
│ ──────────────── │
│ ⛔ 禁用菜单       │  ← 切换启用/禁用
│ 🗑 删除菜单       │  ← 有子菜单时禁止删除
└──────────────────┘
```

### 删除限制

```
有子菜单时:
- 显示「删除菜单」按钮但禁用
- Tooltip 提示「请先删除子菜单」

无子菜单时:
- 正常删除
- 弹出确认弹窗
```

---

## 新建/编辑菜单弹窗

```
┌─────────────────────────────────────────────────────┐
│                    新建菜单                         │
│  ─────────────────────────────────────────────────  │
│                                                     │
│  上级菜单    [▼ 无（顶级菜单）]                     │
│                                                     │
│  菜单类型    [▼ 目录] (目录/菜单/按钮)              │
│                                                     │
│  菜单名称 *  [                              ]       │
│                                                     │
│  菜单图标    [▼ 选择图标]  [🏠]                     │
│              ┌──────────────────────────┐           │
│              │ 🏠 👥 📋 ⚙️ 📊 🔐 💰 ... │           │
│              └──────────────────────────┘           │
│                                                     │
│  路由路径 *  [                              ]       │
│              如: /user/list                         │
│                                                     │
│  排序号      [  1  ]                                │
│                                                     │
│  是否外链    ☐ 是 (点击跳转外部链接)               │
│                                                     │
│  显示状态    ☑ 显示 (在侧边栏显示)                  │
│                                                     │
│  启用状态    ☑ 启用                                 │
│                                                     │
│  ┌────────────┐                                    │
│  │    取消    │       [保存]                       │
│  └────────────┘                                    │
└─────────────────────────────────────────────────────┘
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|-----|-----|-----|
| **上级菜单** | 下拉选择 | | 选择父级菜单，顶级菜单选「无」 |
| **菜单类型** | 下拉选择 | ✓ | 目录/菜单/按钮 |
| **菜单名称** | 输入框 | ✓ | 显示名称 |
| **菜单图标** | 图标选择器 | | 目录和菜单必填 |
| **路由路径** | 输入框 | ✓(菜单类型) | 页面路由，如 `/user/list` |
| **排序号** | 数字输入 | ✓ | 同级菜单排序，默认 1 |
| **是否外链** | 开关 | | 外链菜单跳转外部 URL |
| **外链地址** | 输入框 | ✓(外链时) | 外部链接地址 |
| **显示状态** | 开关 | | 是否在侧边栏显示 |
| **启用状态** | 开关 | | 是否启用 |

---

## 图标选择器

```
┌──────────────────────────────────────────────┐
│ 选择图标                                      │
│ ─────────────────────────────────────────────│
│                                              │
│ 搜索: [         ]                            │
│                                              │
│ ┌──────────────────────────────────────────┐│
│ │ 🏠 👥 📋 ⚙️ 📊 🔐 💰 📁 📄 ✏ 🗑 📥 📤 ││
│ │ ⭐ ❤ 💡 🔔 ⏰ 🔍 🔄 ↗ ✅ ❌ ⚠  🔺 🔻 ││
│ │ ... 更多图标 ...                          ││
│ └──────────────────────────────────────────┘│
│                                              │
│ 当前选择: [🏠] home                           │
│                                              │
│ [取消]                    [确定]             │
└──────────────────────────────────────────────┘

交互:
- 搜索图标名称过滤
- 点击图标选中
- 显示当前选中图标和名称
```

---

## 菜单树预览

```
─── 菜单树预览 ───

🏠 首页
👥 用户管理
   ├─ 用户列表
   ├─ 角色管理
   └─ 权限配置
📋 审计日志
⚙️ 系统管理

CSS 规范:
.menu-preview {
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 24px;
}

.preview-tree {
  font-size: var(--font-size-base);
}

.preview-node {
  padding: 4px 0;
}

.preview-node.child {
  padding-left: 20px;
}
```

---

## Vue 组件示例

```vue
<template>
  <div class="menu-management-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 工具栏 -->
    <div class="toolbar">
      <Button
        v-if="hasPermission('menu:create')"
        type="primary"
        icon="plus"
        @click="openCreateDialog"
      >
        新建菜单
      </Button>
      <Button icon="refresh-cw" @click="loadMenus">
        刷新
      </Button>
      <SearchInput
        v-model="search"
        placeholder="搜索菜单名称"
        @search="loadMenus"
      />
    </div>

    <!-- 菜单表格 -->
    <table class="menu-table">
      <thead>
        <tr>
          <th>排序</th>
          <th>名称</th>
          <th>类型</th>
          <th>路由</th>
          <th>图标</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="menu in menuTree" :key="menu.id">
          <MenuRow
            :menu="menu"
            :level="0"
            @action="handleAction"
          />
        </template>
      </tbody>
    </table>

    <!-- 菜单树预览 -->
    <div class="menu-preview">
      <h4>菜单树预览</h4>
      <MenuPreviewTree :menus="menuTree" />
    </div>

    <!-- 新建/编辑弹窗 -->
    <MenuFormDialog
      v-model:visible="formDialogVisible"
      :menu="editMenu"
      :parent-options="menuOptions"
      @success="loadMenus"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '系统管理', route: '/system' },
  { label: '菜单管理' }
]

const search = ref('')
const menuTree = ref([])
const menuOptions = ref([])
const formDialogVisible = ref(false)
const editMenu = ref(null)

const loadMenus = async () => {
  const res = await api.getMenus({ search: search.value })
  menuTree.value = res.data.tree
  menuOptions.value = res.data.options
}

const openCreateDialog = () => {
  editMenu.value = null
  formDialogVisible.value = true
}

const handleAction = (action, menu) => {
  switch (action) {
    case 'add-child':
      editMenu.value = { parentId: menu.id }
      formDialogVisible.value = true
      break
    case 'edit':
      editMenu.value = menu
      formDialogVisible.value = true
      break
    case 'disable':
      toggleMenuStatus(menu)
      break
    case 'delete':
      if (menu.children?.length) {
        showToast('请先删除子菜单', 'error')
      } else {
        confirmDelete(menu)
      }
      break
  }
}

const toggleMenuStatus = async (menu) => {
  await api.toggleMenuStatus(menu.id)
  showToast('菜单状态已更新')
  loadMenus()
}

const confirmDelete = async (menu) => {
  if (confirm(`确认删除菜单「${menu.name}」？`)) {
    await api.deleteMenu(menu.id)
    showToast('菜单已删除')
    loadMenus()
  }
}

onMounted(() => {
  loadMenus()
})
</script>
```

---

## MenuRow 组件

```vue
<template>
  <tr class="menu-row" :class="{ disabled: !menu.enabled }">
    <td>{{ menu.sort }}</td>
    <td class="menu-name" :style="{ paddingLeft: level * 20 + 'px' }">
      {{ menu.name }}
    </td>
    <td>
      <span class="type-tag" :class="menu.type">
        {{ typeLabel }}
      </span>
    </td>
    <td>{{ menu.path || '-' }}</td>
    <td>
      <Icon v-if="menu.icon" :name="menu.icon" />
    </td>
    <td>
      <StatusTag :status="menu.enabled ? 'enabled' : 'disabled'" />
    </td>
    <td>
      <ActionMenu
        :items="getActionItems()"
        @action="$emit('action', $event, menu)"
      />
    </td>
  </tr>

  <!-- 子菜单 -->
  <template v-if="menu.children?.length">
    <MenuRow
      v-for="child in menu.children"
      :key="child.id"
      :menu="child"
      :level="level + 1"
      @action="(a, m) => $emit('action', a, m)"
    />
  </template>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  menu: Object,
  level: Number
})

const emit = defineEmits(['action'])

const typeLabel = computed(() => {
  const labels = { directory: '目录', menu: '菜单', button: '按钮' }
  return labels[props.menu.type]
})

const getActionItems = () => {
  const hasChildren = props.menu.children?.length > 0
  return [
    { key: 'add-child', label: '新增子菜单', icon: 'plus', permission: 'menu:create', disabled: props.menu.type === 'button' },
    { key: 'edit', label: '编辑菜单', icon: 'edit', permission: 'menu:edit' },
    { key: 'divider', type: 'divider' },
    { key: 'disable', label: props.menu.enabled ? '禁用菜单' : '启用菜单', icon: 'ban', permission: 'menu:edit' },
    { key: 'delete', label: '删除菜单', icon: 'trash-2', permission: 'menu:delete', danger: true, disabled: hasChildren }
  ]
}
</script>

<style scoped>
.menu-name {
  position: relative;
}

.menu-row.disabled {
  opacity: 0.5;
}
</style>
```