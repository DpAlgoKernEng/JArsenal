# 首页/仪表盘

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/home` 或 `/` |
| **功能** | 系统概览、数据统计、快捷入口 |
| **权限** | 需登录，所有角色可见 |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  🏠 首页     │   首页                                                        │
│              │   ─────                                                       │
│  👥 用户管理 │                                                               │
│   └ 用户列表 │   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐│
│   └ 角色管理 │   │ 用户总数    │ │ 角色总数    │ │ 今日登录    │ │ 待处理  ││
│   └ 权限配置 │   │   1,256    │ │    12      │ │    89       │ │   5    ││
│              │   │ ↑ +12 本月 │ │ ↑ +2 本季度│ │ ↑ +15%     │ │ 审批请求││
│  📋 审计日志 │   └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘│
│   └ 登录日志 │                                                               │
│   └ 操作日志 │   ─── 快捷入口 ───────────────────────────────────────────── │
│              │                                                               │
│  ────────    │   [创建用户]  [角色配置]  [权限分配]  [查看日志]            │
│              │                                                               │
│  ⚙️ 系统管理 │   ─── 近期活动 ─────────────────────────────────────────────  │
│   └ 菜单管理 │                                                               │
│   └ 系统设置 │   ┌──────────────────────────────────────────────────────┐  │
│              │   │ 时间        │ 用户      │ 操作类型    │ 详情           │  │
│              │   ├──────────────────────────────────────────────────────┤  │
│              │   │ 10:23:15   │ admin    │ 创建用户    │ 新增用户 #1256│  │
│              │   │ 09:45:32   │ zhangsan│ 编辑角色    │ 修改「财务」 │  │
│              │   │ 09:12:08   │ lisi     │ 登录系统    │ IP: 10.0.1.5 │  │
│              │   │ 08:30:00   │ system   │ 权限同步    │ 12个角色更新 │  │
│              │   └──────────────────────────────────────────────────────┘  │
│              │   [查看全部活动 →]                                             │
│              │                                                               │
│              │   ─── 系统状态 ─────────────────────────────────────────────  │
│              │                                                               │
│              │   ┌─────────────────────┐ ┌─────────────────────┐            │
│              │   │ 🟢 服务状态: 正常    │ │ 🟢 数据库: 正常     │            │
│              │   │ CPU: 23%  MEM: 45%  │ │ Redis: 已连接       │            │
│              │   └─────────────────────┘ └─────────────────────┘            │
│              │                                                               │
├──────────────┴──────────────────────────────────────────────────────────────┤
│                                                    版本 v2.1.0 · 帮助文档    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 区域划分

| 区域 | 功能 | 高度 |
|------|-----|-----|
| **顶部导航** | Logo、搜索、通知、用户菜单 | 64px (固定) |
| **侧边栏** | 导航菜单 | 240px (固定) |
| **统计卡片区** | KPI 概览 | 120px |
| **快捷入口** | 常用功能入口 | 80px |
| **近期活动** | 操作日志表格 | auto (min 200px) |
| **系统状态** | 服务健康监控 | 100px |
| **底部版权** | 版本信息 | 48px (固定) |

---

## 组件详解

### 统计卡片

```
┌─────────────────────┐
│  👥 用户总数        │  ← Icon + 标题
│                     │
│    1,256           │  ← 数值，字号 32px，字重 700
│                     │
│  ↑ +12 本月        │  ← 趋势，绿色向上箭头 + 增长说明
└─────────────────────┘

CSS 规范:
.stat-card {
  width: calc(25% - 16px);
  height: 120px;
  padding: 20px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  display: flex;
  flex-direction: column;
}

.stat-icon {
  width: 24px;
  height: 24px;
  color: var(--color-primary);
}

.stat-title {
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-foreground);
}

.stat-trend {
  font-size: var(--font-size-xs);
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-trend.positive {
  color: var(--color-accent);
}

.stat-trend.negative {
  color: var(--color-destructive);
}
```

**卡片内容配置**:

| 卡片 | Icon | 数据来源 | 刷新频率 |
|------|-----|---------|---------|
| 用户总数 | `users` | `/api/users/count` | 页面加载 |
| 角色总数 | `shield` | `/api/roles/count` | 页面加载 |
| 今日登录 | `log-in` | `/api/logs/login/today` | 实时 (可定时刷新) |
| 待处理 | `clock` | `/api/tasks/pending` | 实时 |

### 快捷入口

```
─── 快捷入口 ─────────────────────────────────────────────

[创建用户]  [角色配置]  [权限分配]  [查看日志]

CSS 规范:
.quick-actions {
  display: flex;
  gap: 16px;
  padding: 16px 0;
}

.quick-action-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  cursor: pointer;
  transition: all var(--duration-fast);
}

.quick-action-button:hover {
  background: var(--color-primary);
  color: var(--color-on-primary);
  border-color: var(--color-primary);
}
```

**快捷入口配置**:

| 入口 | Icon | 路由 | 权限要求 |
|------|-----|------|---------|
| 创建用户 | `user-plus` | `/user/create` | `user:create` |
| 角色配置 | `shield` | `/user/role` | `role:view` |
| 权限分配 | `key` | `/user/role/permission` | `permission:config` |
| 查看日志 | `file-text` | `/log/operation` | `log:view` |

**权限控制**:
- 无权限的入口按钮显示禁用状态
- 点击禁用按钮显示 Tooltip "需要权限: xxx"

### 近期活动表格

```
─── 近期活动 ─────────────────────────────────────────────

┌──────────────────────────────────────────────────────┐
│ 时间        │ 用户      │ 操作类型    │ 详情         │
├──────────────────────────────────────────────────────┤
│ 10:23:15   │ admin    │ 创建用户    │ 新增用户 #1256│
│ 09:45:32   │ zhangsan│ 编辑角色    │ 修改「财务」 │
│ 09:12:08   │ lisi     │ 登录系统    │ IP: 10.0.1.5 │
│ 08:30:00   │ system   │ 权限同步    │ 12个角色更新 │
└──────────────────────────────────────────────────────┘

[查看全部活动 →]

CSS 规范:
.activity-table {
  width: 100%;
  border-collapse: collapse;
}

.activity-table th {
  padding: 12px 16px;
  background: var(--color-muted);
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-muted-foreground);
  text-align: left;
}

.activity-table td {
  padding: 12px 16px;
  font-size: var(--font-size-base);
  border-bottom: 1px solid var(--color-border);
}

.activity-table tr:last-child td {
  border-bottom: none;
}

.view-all-link {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  cursor: pointer;
}
```

**数据来源**:
- `/api/logs/operation/recent?limit=5`
- 按时间倒序，最近 5 条

**操作类型映射**:

| 操作类型 | 显示颜色 | Icon |
|---------|---------|-----|
| 创建 | `--color-accent` | `plus` |
| 编辑 | `--color-info` | `edit` |
| 删除 | `--color-destructive` | `trash-2` |
| 登录 | `--color-muted-foreground` | `log-in` |
| 权限分配 | `--color-primary` | `key` |

### 系统状态卡片

```
─── 系统状态 ─────────────────────────────────────────────

┌─────────────────────┐ ┌─────────────────────┐
│ 🟢 服务状态: 正常    │ │ 🟢 数据库: 正常     │
│ CPU: 23%  MEM: 45%  │ │ Redis: 已连接       │
└─────────────────────┘ └─────────────────────┘

CSS 规范:
.status-cards {
  display: flex;
  gap: 16px;
}

.status-card {
  flex: 1;
  padding: 16px;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-base);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.normal {
  background: var(--color-accent);
}

.status-dot.warning {
  background: var(--color-warning);
}

.status-dot.error {
  background: var(--color-destructive);
}

.status-details {
  margin-top: 8px;
  font-size: var(--font-size-sm);
  color: var(--color-muted-foreground);
}
```

**状态指标配置**:

| 指标 | 数据来源 | 正常阈值 | 警告阈值 |
|------|---------|---------|---------|
| 服务状态 | `/actuator/health` | status=UP | status=DOWN |
| CPU | `/actuator/metrics/system.cpu.usage` | <70% | 70-90% |
| 内存 | `/actuator/metrics/jvm.memory.used` | <80% | 80-95% |
| 数据库 | `/actuator/health/db` | status=UP | status=DOWN |
| Redis | `/actuator/health/redis` | status=UP | status=DOWN |

---

## 数据刷新策略

| 数据 | 刷新方式 | 刷新频率 |
|------|---------|---------|
| 统计卡片 | 页面加载 + 手动刷新 | 页面加载 |
| 近期活动 | 页面加载 | 页面加载 |
| 系统状态 | 定时轮询 | 每 30 秒 |
| 待处理数 | 定时轮询 | 每 10 秒 |

**手动刷新**:
- 顶部提供刷新按钮
- 点击刷新按钮刷新所有数据

---

## Vue 组件示例

```vue
<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <StatCard
        v-for="stat in stats"
        :key="stat.key"
        :icon="stat.icon"
        :title="stat.title"
        :value="stat.value"
        :trend="stat.trend"
        :trendDirection="stat.trendDirection"
      />
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <h3 class="section-title">快捷入口</h3>
      <div class="action-buttons">
        <QuickActionButton
          v-for="action in quickActions"
          :key="action.route"
          :icon="action.icon"
          :label="action.label"
          :route="action.route"
          :disabled="!hasPermission(action.permission)"
        />
      </div>
    </div>

    <!-- 近期活动 -->
    <div class="recent-activity">
      <h3 class="section-title">近期活动</h3>
      <table class="activity-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>用户</th>
            <th>操作类型</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in recentLogs" :key="log.id">
            <td>{{ formatTime(log.timestamp) }}</td>
            <td>{{ log.username }}</td>
            <td>
              <span class="operation-type" :class="log.type">
                {{ log.typeLabel }}
              </span>
            </td>
            <td>{{ log.detail }}</td>
          </tr>
        </tbody>
      </table>
      <a class="view-all" @click="goToLogs">查看全部活动 →</a>
    </div>

    <!-- 系统状态 -->
    <div class="system-status">
      <h3 class="section-title">系统状态</h3>
      <div class="status-cards">
        <StatusCard
          title="服务状态"
          :status="systemHealth.status"
          :details="`CPU: ${systemHealth.cpu}%  MEM: ${systemHealth.memory}%`"
        />
        <StatusCard
          title="数据库"
          :status="systemHealth.db"
          :details="systemHealth.db === 'UP' ? '正常' : '异常'"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { StatCard, QuickActionButton, StatusCard } from '@/components/dashboard'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref([
  { key: 'users', icon: 'users', title: '用户总数', value: 0, trend: '+0', trendDirection: 'neutral' },
  { key: 'roles', icon: 'shield', title: '角色总数', value: 0, trend: '+0', trendDirection: 'neutral' },
  { key: 'logins', icon: 'log-in', title: '今日登录', value: 0, trend: '+0%', trendDirection: 'neutral' },
  { key: 'pending', icon: 'clock', title: '待处理', value: 0, trend: '审批请求', trendDirection: 'neutral' }
])

// 快捷入口配置
const quickActions = [
  { label: '创建用户', icon: 'user-plus', route: '/user/create', permission: 'user:create' },
  { label: '角色配置', icon: 'shield', route: '/user/role', permission: 'role:view' },
  { label: '权限分配', icon: 'key', route: '/user/role/permission', permission: 'permission:config' },
  { label: '查看日志', icon: 'file-text', route: '/log/operation', permission: 'log:view' }
]

// 近期活动日志
const recentLogs = ref([])

// 系统健康状态
const systemHealth = ref({
  status: 'UP',
  cpu: 0,
  memory: 0,
  db: 'UP'
})

// 定时刷新
let healthTimer = null

// 权限检查
const hasPermission = (perm) => {
  return userStore.hasPermission(perm)
}

// 加载统计数据
const loadStats = async () => {
  // API 调用...
}

// 加载近期活动
const loadRecentLogs = async () => {
  // API 调用...
}

// 加载系统状态
const loadSystemHealth = async () => {
  // API 调用...
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

// 跳转到日志页
const goToLogs = () => {
  router.push('/log/operation')
}

onMounted(() => {
  loadStats()
  loadRecentLogs()
  loadSystemHealth()

  // 定时刷新系统状态
  healthTimer = setInterval(loadSystemHealth, 30000)
})

onUnmounted(() => {
  if (healthTimer) {
    clearInterval(healthTimer)
  }
})
</script>
```

---

## 响应式适配

### 平板 (768px - 1024px)

```
统计卡片: 2列 (每行 2 个)
快捷入口: 按钮变小，间距缩小
系统状态: 1列
```

### 手机 (< 768px)

```
统计卡片: 1列 (每行 1 个)
快捷入口: 纵向排列或 2 列网格
近期活动: 简化显示，隐藏详情列
系统状态: 1列
```

---

## 权限控制

根据当前用户权限，动态显示/隐藏或禁用相关内容：

| 内容 | 无权限时行为 |
|------|------------|
| 快捷入口按钮 | 显示禁用状态，Tooltip 提示所需权限 |
| 统计卡片数值 | 正常显示 (概览数据) |
| 近期活动 | 正常显示 (审计日志权限独立) |
| 系统状态 | 仅管理员可见 |