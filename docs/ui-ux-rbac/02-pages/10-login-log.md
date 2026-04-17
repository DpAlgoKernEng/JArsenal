# 登录日志

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/log/login` |
| **功能** | 查询用户登录记录，审计安全 |
| **权限** | `log:view` |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  📋 审计日志 │   馀页 > 审计日志 > 登录日志                                   │
│  ●└ 登录日志 │   ─────────────────────────────                                │
│   └ 操作日志 │                                                               │
│              │   登录日志                                      共 2,345 条   │
│              │   ────────────────────────────────────────────────────────── │
│              │                                                               │
│              │   ┌───────────────────────────────────────────────────────┐ │
│              │   │ 🔍 搜索用户    │ 状态 ▼ │ 时间范围: [开始] ~ [结束]    │ │
│              │   │ [导出]                     [重置筛选]                │ │
│              │   └───────────────────────────────────────────────────────┘ │
│              │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │ 时间        │ 用户     │ IP地址    │ 状态 │ 设备 │备注 ││
│              │   ├────────────┼─────────┼──────────┼──────┼──────┼─────┤│
│              │   │ 2024-03-20 │ admin   │ 10.0.1.5 │ 成功 │ Web │     ││
│              │   │ 09:12:08   │         │          │      │      │     ││
│              │   │ 2024-03-20 │ zhangsan│ 192.168. │ 成功 │ App │     ││
│              │   │ 08:45:32   │         │ 1.100    │      │      │     ││
│              │   │ 2024-03-20 │ hacker  │ 10.0.2.5 │ 失败 │ Web │密码 ││
│              │   │ 03:12:15   │         │          │      │      │错误 ││
│              │   │ 2024-03-19 │ lisi    │ 10.0.1.8 │ 成功 │ Web │     ││
│              │   │ 17:30:00   │         │          │      │      │     ││
│              │   │ 2024-03-19 │ unknown │ 外部IP   │ 失败 │ API │账号 ││
│              │   │ 15:20:30   │         │          │      │      │锁定 ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
│              │   < 1 2 3 ... 235 >                每页显示: [10▼] 条        │
│              │                                                               │
│              │   ─── 统计概览 ───                                              │
│              │                                                               │
│              │   今日登录: 89 次    成功率: 95.5%    失败: 4 次             │
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 表格列定义

| 列 | 宽度 | 内容 | 排序 | 说明 |
|------|-----|-----|-----|-----|
| **时间** | 180px | 登录时间 | ✓ | 格式: YYYY-MM-DD HH:mm:ss |
| **用户** | 120px | 用户名 | - | 可点击跳转用户详情 |
| **IP地址** | 150px | 登录IP | - | 可点击查看IP详情 |
| **状态** | 80px | 成功/失败 | ✓ | 标签显示 |
| **设备** | 80px | Web/App/API | - | 登录设备类型 |
| **备注** | 150px | 失败原因 | - | 仅失败时显示 |

---

## 状态标签

| 状态 | 显示 | 颜色 |
|------|-----|-----|
| **成功** | ✓ 成功 | 绿色 `--color-accent` |
| **失败** | ✗ 失败 | 红色 `--color-destructive` |

---

## 失败备注类型

| 备注内容 | 说明 |
|---------|-----|
| **密码错误** | 用户密码输入错误 |
| **账号锁定** | 连续失败锁定 |
| **账号不存在** | 用户名不存在 |
| **验证码错误** | 验证码校验失败 |
| **权限拒绝** | 无登录权限 |

---

## 筛选字段

| 字段 | 类型 | 说明 |
|------|-----|-----|
| **搜索用户** | 输入框 | 搜索用户名，模糊匹配 |
| **状态** | 下拉选择 | 成功/失败/全部 |
| **时间范围** | 日期范围选择 | 开始日期 ~ 结束日期 |

---

## 统计概览

```
─── 统计概览 ───

今日登录: 89 次    成功率: 95.5%    失败: 4 次

CSS 规范:
.stats-summary {
  display: flex;
  gap: 32px;
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  margin-top: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.stat-value {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-foreground);
}

.stat-value.success {
  color: var(--color-accent);
}

.stat-value.failure {
  color: var(--color-destructive);
}
```

---

## 行点击交互

### 点击用户名

跳转到用户详情页 `/user/detail/:userId`

### 点击 IP 地址

```
弹出 IP 详情弹窗:
┌──────────────────────────────────────┐
│ IP 详情                               │
│ ─────────────────────────────────────│
│ IP 地址: 10.0.1.5                    │
│ 所属网段: 内网                        │
│ 地理位置: 北京                        │
│ 最近登录用户: admin                   │
│ 登录次数: 156                         │
│                                      │
│ [查看该IP所有登录记录]                │
│                                      │
│ [关闭]                                │
└──────────────────────────────────────┘
```

---

## 导出功能

```
点击「导出」:
- 导出当前筛选结果
- 格式: Excel (.xlsx)
- 字段: 时间、用户、IP、状态、设备、备注
- 文件名: login_logs_YYYYMMDD.xlsx
```

---

## Vue 组件示例

```vue
<template>
  <div class="login-log-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <SearchInput
        v-model="filters.username"
        placeholder="搜索用户"
        @search="loadLogs"
      />
      <FilterSelect
        v-model="filters.status"
        label="状态"
        :options="statusOptions"
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
    >
      <template #username="{ row }">
        <a class="username-link" @click="goToUserDetail(row.userId)">
          {{ row.username }}
        </a>
      </template>
      <template #ip="{ row }">
        <a class="ip-link" @click="showIpDetail(row.ip)">
          {{ row.ip }}
        </a>
      </template>
      <template #status="{ row }">
        <StatusTag
          :status="row.status"
          :icon="row.status === 'success' ? 'check' : 'x'"
        />
      </template>
      <template #note="{ row }">
        <span v-if="row.note" class="failure-note">
          {{ row.note }}
        </span>
      </template>
    </DataTable>

    <!-- 分页 -->
    <Pagination
      :current="pagination.current"
      :total="pagination.total"
      :page-size="pagination.pageSize"
      @change="handlePageChange"
    />

    <!-- 统计概览 -->
    <div class="stats-summary">
      <StatItem label="今日登录" :value="stats.todayCount" />
      <StatItem label="成功率" :value="stats.successRate + '%'" type="success" />
      <StatItem label="失败次数" :value="stats.failureCount" type="failure" />
    </div>

    <!-- IP 详情弹窗 -->
    <IpDetailDialog
      v-model:visible="ipDialogVisible"
      :ip="selectedIp"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '审计日志', route: '/log' },
  { label: '登录日志' }
]

const filters = ref({
  username: '',
  status: '',
  dateRange: { start: '', end: '' }
})

const logs = ref([])
const loading = ref(false)
const pagination = ref({ current: 1, total: 0, pageSize: 10 })
const stats = ref({ todayCount: 0, successRate: 0, failureCount: 0 })

const ipDialogVisible = ref(false)
const selectedIp = ref('')

const columns = [
  { key: 'timestamp', title: '时间', width: 180, sortable: true },
  { key: 'username', title: '用户', width: 120 },
  { key: 'ip', title: 'IP地址', width: 150 },
  { key: 'status', title: '状态', width: 80, sortable: true },
  { key: 'device', title: '设备', width: 80 },
  { key: 'note', title: '备注', width: 150 }
]

const loadLogs = async () => {
  loading.value = true
  try {
    const res = await api.getLoginLogs({
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

const loadStats = async () => {
  const res = await api.getLoginStats()
  stats.value = res.data
}

const goToUserDetail = (userId) => {
  router.push(`/user/detail/${userId}`)
}

const showIpDetail = (ip) => {
  selectedIp.value = ip
  ipDialogVisible.value = true
}

const handleExport = async () => {
  const res = await api.exportLoginLogs(filters.value)
  downloadFile(res.data, 'login_logs.xlsx')
}

const resetFilters = () => {
  filters.value = { username: '', status: '', dateRange: { start: '', end: '' } }
  loadLogs()
}

let statsTimer = null

onMounted(() => {
  loadLogs()
  loadStats()
  statsTimer = setInterval(loadStats, 60000) // 每分钟更新统计
})

onUnmounted(() => {
  if (statsTimer) clearInterval(statsTimer)
})
</script>
```