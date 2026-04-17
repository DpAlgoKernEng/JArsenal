# 操作日志

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/log/operation` |
| **功能** | 查询用户操作记录，审计行为 |
| **权限** | `log:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  📋 审计日志 │   馀页 > 审计日志 > 操作日志                                   │
│   └ 登录日志 │   ─────────────────────────────                                │
│  ●└ 操作日志 │                                                               │
│              │   操作日志                                      共 15,678 条  │
│              │   ────────────────────────────────────────────────────────── │
│              │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ 🔍 搜索用户    │ 操作类型▼ │ 模块▼ │ 时间范围         │ │
│              │   │ [导出]                     [重置筛选]                │ │
│              │   └───────────────────────────────────────────────────────┘ │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 时间 │ 操作人 │ 模块 │ 操作类型 │ 对象 │ 详情 │ IP ││
│              │   ├────────────────────────────────────────────────────────┤│
│              │   │10:23 │admin │用户 │创建 │用户#1256│新增用户zhaoliu│10.0││
│              │   │09:45 │admin │角色 │编辑 │角色#3 │修改权限配置 │10.0││
│              │   │09:12 │admin │权限 │分配 │用户#120│分配财务角色 │10.0││
│              │   │08:30 │system│菜单 │创建 │菜单#15│新增审批菜单 │系统││
│              │   │07:55 │admin │用户 │删除 │用户#89 │删除测试账号 │10.0││
│              │   │...   │...   │...  │...  │...    │...          │... ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   点击详情查看:                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 操作详情                                               ││
│              │   │ ─────────────────────────────────────────────────────  ││
│              │   │ 操作时间: 2024-03-20 10:23:15                          ││
│              │   │ 操作人:   admin (admin@corp.com)                       ││
│              │   │ 操作类型: 创建用户                                     ││
│              │   │ 操作对象: 用户 #1256 (zhaoliu)                         ││
│              │   │ ─────────────────────────────────────────────────────  ││
│              │   │ 变更内容:                                              ││
│              │   │ 新增字段:                                              ││
│              │   │   - 用户名: zhaoliu                                    ││
│              │   │   - 邮箱: zl@corp.com                                  ││
│              │   │   - 角色: 普通用户                                     ││
│              │   │   - 状态: 启用                                         ││
│              │   │ ─────────────────────────────────────────────────────  ││
│              │   │ 操作IP:   10.0.1.5                                     ││
│              │   │ ─────────────────────────────────────────────────────  ││
│              │   │ [关闭]                                                 ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 表格列定义

| 列 | 宽度 | 内容 | 排序 | 说明 |
|------|-----|-----|-----|-----|
| **时间** | 140px | 操作时间 | ✓ | HH:mm:ss |
| **操作人** | 100px | 操作者用户名 | - | 可点击 |
| **模块** | 80px | 操作模块 | ✓ | 用户/角色/权限等 |
| **操作类型** | 100px | 创建/编辑/删除等 | ✓ | 标签 |
| **对象** | 120px | 操作对象标识 | - | 如用户#1256 |
| **详情** | 200px | 操作详情描述 | - | 简要描述 |
| **IP** | 120px | 操作者IP | - | 点击查看详情弹窗 |

---

## 操作类型标签

| 操作类型 | 显示 | 颜色 |
|---------|-----|-----|
| **创建** | 创建 | 绿色 `--color-accent` |
| **编辑** | 编辑 | 蓝色 `--color-info` |
| **删除** | 删除 | 红色 `--color-destructive` |
| **分配** | 分配 | 紫色 `--color-purple` |
| **登录** | 登录 | 灰色 `--color-muted-foreground` |
| **导出** | 导出 | 黄色 `--color-warning` |

---

## 模块类型

| 模块 | 说明 |
|------|-----|
| **用户** | 用户管理操作 |
| **角色** | 角色管理操作 |
| **权限** | 权限配置操作 |
| **菜单** | 菜单管理操作 |
| **系统** | 系统设置操作 |
| **登录** | 登录相关操作 |

---

## 筛选字段

| 字段 | 类型 | 说明 |
|------|-----|-----|
| **搜索用户** | 输入框 | 搜索操作人用户名 |
| **操作类型** | 下拉选择 | 创建/编辑/删除/分配等 |
| **模块** | 下拉选择 | 用户/角色/权限/菜单等 |
| **时间范围** | 日期范围选择 | 开始 ~ 结束 |

---

## 操作详情弹窗

点击行或详情列显示详情弹窗：

```
┌────────────────────────────────────────────────────────────────────┐
│ 操作详情                                                            │
│ ───────────────────────────────────────────────────────────────────│
│ 操作时间:   2024-03-20 10:23:15                                     │
│ 操作人:     admin (admin@corp.com)                                  │
│ 操作类型:   创建用户                                                 │
│ 操作对象:   用户 #1256 (zhaoliu)                                     │
│ ───────────────────────────────────────────────────────────────────│
│ 变更内容:                                                           │
│                                                                     │
│ 新增字段:                                                           │
│   • 用户名: zhaoliu                                                 │
│   • 邮箱: zl@corp.com                                               │
│   • 角色: 普通用户                                                   │
│   • 状态: 启用                                                       │
│                                                                     │
│ ───────────────────────────────────────────────────────────────────│
│ 操作IP:     10.0.1.5                                                │
│ 操作设备:   Web                                                     │
│ ───────────────────────────────────────────────────────────────────│
│                                                                    │
│ [关闭]                                                              │
└────────────────────────────────────────────────────────────────────┘
```

### 变更内容格式

| 操作类型 | 变更内容格式 |
|---------|-------------|
| **创建** | 新增字段列表 |
| **编辑** | 修改前后对比 |
| **删除** | 删除的字段值 |
| **分配** | 分配的内容 |

**编辑操作变更示例**:
```
变更内容:

修改字段:
  • 状态: 启用 → 禁用
  • 角色: 普通用户 → 财务
```

---

## Vue 组件示例

```vue
<template>
  <div class="operation-log-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SearchInput
        v-model="filters.operator"
        placeholder="搜索用户"
        @search="loadLogs"
      />
      <FilterSelect
        v-model="filters.actionType"
        label="操作类型"
        :options="actionTypeOptions"
      />
      <FilterSelect
        v-model="filters.module"
        label="模块"
        :options="moduleOptions"
      />
      <DateRangePicker
        v-model="filters.dateRange"
        label="时间范围"
      />
      <Button icon="download" @click="handleExport">导出</Button>
      <Button type="ghost" @click="resetFilters">重置筛选</Button>
    </div>

    <!-- 数据表格 -->
    <DataTable
      :columns="columns"
      :data="logs"
      :loading="loading"
      @sort="handleSort"
      @row-click="showDetail"
    >
      <template #operator="{ row }">
        <a class="operator-link" @click.stop="goToUserDetail(row.operatorId)">
          {{ row.operator }}
        </a>
      </template>
      <template #actionType="{ row }">
        <ActionTypeTag :type="row.actionType" />
      </template>
      <template #detail="{ row }">
        <span class="detail-text">{{ row.detailSummary }}</span>
        <Button size="small" type="ghost" @click.stop="showDetail(row)">
          详情
        </Button>
      </template>
      <template #ip="{ row }">
        <a class="ip-link" @click.stop="showIpDetail(row.ip)">
          {{ row.ip }}
        </a>
      </template>
    </DataTable>

    <!-- 分页 -->
    <Pagination
      :current="pagination.current"
      :total="pagination.total"
      :page-size="pagination.pageSize"
      @change="handlePageChange"
    />

    <!-- 操作详情弹窗 -->
    <OperationDetailDialog
      v-model:visible="detailDialogVisible"
      :log="selectedLog"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '审计日志', route: '/log' },
  { label: '操作日志' }
]

const filters = ref({
  operator: '',
  actionType: '',
  module: '',
  dateRange: { start: '', end: '' }
})

const logs = ref([])
const loading = ref(false)
const pagination = ref({ current: 1, total: 0, pageSize: 10 })
const detailDialogVisible = ref(false)
const selectedLog = ref(null)

const columns = [
  { key: 'timestamp', title: '时间', width: 140, sortable: true },
  { key: 'operator', title: '操作人', width: 100 },
  { key: 'module', title: '模块', width: 80, sortable: true },
  { key: 'actionType', title: '操作类型', width: 100, sortable: true },
  { key: 'object', title: '对象', width: 120 },
  { key: 'detail', title: '详情', width: 200 },
  { key: 'ip', title: 'IP', width: 120 }
]

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await api.getOperationLogs({
      ...filters.value,
      page: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    logs.value = res.data.list
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const showDetail = (log) => {
  selectedLog.value = log
  detailDialogVisible.value = true
}

const goToUserDetail = (userId) => {
  router.push(`/user/detail/${userId}`)
}

const showIpDetail = (ip) => {
  // 显示 IP 详情弹窗
}

const handleExport = async () => {
  const res = await api.exportOperationLogs(filters.value)
  downloadFile(res.data, 'operation_logs.xlsx')
}

onMounted(() => {
  loadLogs()
})
</script>
```

---

## OperationDetailDialog 组件

```vue
<template>
  <Dialog v-model:visible="visible" title="操作详情" width="600px">
    <div class="detail-content">
      <!-- 基本信息 -->
      <InfoRow label="操作时间" :value="formatDateTime(log.timestamp)" />
      <InfoRow label="操作人" :value="`${log.operator} (${log.operatorEmail})`" />
      <InfoRow label="操作类型" :value="log.actionTypeLabel" />
      <InfoRow label="操作对象" :value="`${log.objectType} #${log.objectId} (${log.objectName})`" />

      <!-- 变更内容 -->
      <div class="change-content">
        <h4>变更内容</h4>
        <div class="changes">
          <!-- 根据操作类型显示不同格式 -->
          <template v-if="log.actionType === 'create'">
            <div class="change-section">
              <span class="section-label">新增字段:</span>
              <ul class="field-list">
                <li v-for="field in log.newValues" :key="field.key">
                  • {{ field.key }}: {{ field.value }}
                </li>
              </ul>
            </div>
          </template>

          <template v-else-if="log.actionType === 'edit'">
            <div class="change-section">
              <span class="section-label">修改字段:</span>
              <ul class="field-list">
                <li v-for="field in log.changedFields" :key="field.key">
                  • {{ field.key }}: {{ field.oldValue }} → {{ field.newValue }}
                </li>
              </ul>
            </div>
          </template>

          <template v-else-if="log.actionType === 'delete'">
            <div class="change-section">
              <span class="section-label">删除的字段值:</span>
              <ul class="field-list">
                <li v-for="field in log.deletedValues" :key="field.key">
                  • {{ field.key }}: {{ field.value }}
                </li>
              </ul>
            </div>
          </template>
        </div>
      </div>

      <!-- IP 和设备 -->
      <InfoRow label="操作IP" :value="log.ip" />
      <InfoRow label="操作设备" :value="log.device" />
    </div>

    <template #footer>
      <Button type="primary" @click="visible = false">关闭</Button>
    </template>
  </Dialog>
</template>

<script setup>
defineProps({
  log: Object
})

const visible = defineModel('visible')

const formatDateTime = (date) => new Date(date).toLocaleString('zh-CN')
</script>

<style scoped>
.change-content {
  margin: 16px 0;
  padding: 16px;
  background: var(--color-muted);
  border-radius: var(--radius-md);
}

.change-section {
  margin-top: 8px;
}

.section-label {
  font-weight: 500;
  color: var(--color-foreground);
}

.field-list {
  margin-top: 8px;
  padding-left: 16px;
  list-style: none;
}

.field-list li {
  padding: 4px 0;
  font-size: var(--font-size-sm);
}
</style>
```