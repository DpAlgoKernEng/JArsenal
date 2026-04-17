# 设计系统规范

## 风格定位

**Data-Dense Dashboard（数据密集型后台）**

面向企业内部管理员，强调信息密度、操作效率和功能完整性。采用专业、清晰的视觉风格，减少装饰性元素，突出数据呈现和操作效率。

### 设计关键词

- 专业 (Professional)
- 清晰 (Clear)
- 高效 (Efficient)
- 数据密集 (Data-Dense)
- 深色/浅色双主题 (Dark/Light Dual Theme)

---

## 色彩系统

### 深色主题（推荐）

| 颜色角色 | 色值 | CSS 变量 | 用途 |
|---------|------|---------|-----|
| **Primary** | `#0F172A` | `--color-primary` | 主按钮、导航选中、主操作 |
| **On Primary** | `#FFFFFF` | `--color-on-primary` | 主按钮文字 |
| **Secondary** | `#1E293B` | `--color-secondary` | 次级按钮、卡片背景 |
| **Accent/CTA** | `#22C55E` | `--color-accent` | 成功状态、启用状态、正向指标 |
| **Background** | `#020617` | `--color-background` | 页面背景 |
| **Foreground** | `#F8FAFC` | `--color-foreground` | 主要文字 |
| **Muted** | `#1A1E2F` | `--color-muted` | 禁用状态、次要背景 |
| **Border** | `#334155` | `--color-border` | 边框、分隔线 |
| **Destructive** | `#EF4444` | `--color-destructive` | 删除、禁用、危险操作 |
| **Warning** | `#F59E0B` | `--color-warning` | 警告提示 |
| **Info** | `#3B82F6` | `--color-info` | 信息提示 |

### 浅色主题

| 颜色角色 | 色值 | CSS 变量 | 用途 |
|---------|------|---------|-----|
| **Primary** | `#1E40AF` | `--color-primary` | 主按钮、导航选中 |
| **On Primary** | `#FFFFFF` | `--color-on-primary` | 主按钮文字 |
| **Background** | `#F8FAFC` | `--color-background` | 页面背景 |
| **Foreground** | `#0F172A` | `--color-foreground` | 主要文字 |
| **Muted** | `#F1F5F9` | `--color-muted` | 卡片背景、禁用状态 |
| **Border** | `#E2E8F0` | `--color-border` | 边框、分隔线 |
| **Surface** | `#FFFFFF` | `--color-surface` | 卡片、表格背景 |

### 状态色

| 状态 | 深色模式 | 浅色模式 | 用途 |
|------|---------|---------|-----|
| **成功/启用** | `#22C55E` | `#16A34A` | 成功提示、启用状态 |
| **失败/禁用** | `#EF4444` | `#DC2626` | 错误提示、禁用状态 |
| **警告** | `#F59E0B` | `#D97706` | 警告提示 |
| **信息** | `#3B82F6` | `#2563EB` | 信息提示 |

---

## 字体系统

### 字体家族

**推荐字体组合**: Fira Sans + Fira Code

```
主字体: Fira Sans (界面文字)
代码字体: Fira Code (ID、路径、代码片段)
```

**Google Fonts 引入**:
```css
@import url('https://fonts.googleapis.com/css2?family=Fira+Sans:wght@300;400;500;600;700&family=Fira+Code:wght@400;500;600;700&display=swap');
```

**系统字体回退**:
```css
font-family: 'Fira Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
font-family: 'Fira Code', 'SF Mono', Monaco, 'Cascadia Code', monospace;
```

### 字体层级

| 层级 | 字号 | 字重 | 行高 | 用途 |
|------|-----|------|-----|-----|
| **H1** | 32px | 600 | 1.25 | 页面标题 |
| **H2** | 24px | 600 | 1.33 | 模块标题 |
| **H3** | 18px | 500 | 1.5 | 卡片标题、表单分组标题 |
| **Body** | 14px | 400 | 1.5 | 正文、表格内容 |
| **Body Large** | 16px | 400 | 1.5 | 重要正文 |
| **Small** | 12px | 400 | 1.5 | 辅助信息、标签、时间戳 |
| **Tiny** | 10px | 400 | 1.5 | 极小标签（慎用） |

### 字体 CSS Token

```css
:root {
  --font-size-xs: 10px;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 18px;
  --font-size-2xl: 24px;
  --font-size-3xl: 32px;

  --font-weight-light: 300;
  --font-weight-normal: 400;
  --font-weight-medium: 500;
  --font-weight-semibold: 600;
  --font-weight-bold: 700;

  --line-height-tight: 1.25;
  --line-height-normal: 1.5;
  --line-height-relaxed: 1.75;
}
```

---

## 图标系统

### 图标库推荐

**Lucide Icons** (推荐) - 清晰、现代、一致的图标风格

- 官网: https://lucide.dev/
- Vue 组件: `lucide-vue-next`
- React 组件: `lucide-react`

### 核心图标集

| 功能 | 图标名称 | 用途 |
|------|---------|-----|
| **导航** | `home`, `users`, `shield`, `file-text`, `settings`, `menu` | 侧边栏导航 |
| **用户** | `user`, `user-plus`, `user-minus`, `user-x`, `users` | 用户管理 |
| **角色** | `shield`, `shield-check`, `shield-off`, `key` | 角色管理 |
| **权限** | `lock`, `unlock`, `key`, `check`, `x` | 权限配置 |
| **操作** | `plus`, `edit`, `trash-2`, `copy`, `download`, `upload` | CRUD 操作 |
| **状态** | `check-circle`, `x-circle`, `alert-circle`, `info` | 状态指示 |
| **交互** | `search`, `filter`, `sort-asc`, `sort-desc`, `more-vertical` | 筛选排序 |
| **反馈** | `save`, `refresh-cw`, `loading` (自定义动画) | 操作反馈 |
| **导航** | `chevron-left`, `chevron-right`, `chevron-down`, `chevron-up` | 展开/折叠 |
| **文件** | `file`, `file-plus`, `folder`, `folder-open` | 文件/菜单树 |

### 图标规范

| 属性 | 规格 |
|------|-----|
| **标准尺寸** | 20px (小图标), 24px (标准图标), 32px (大图标) |
| **描边宽度** | 1.5px 或 2px (保持一致) |
| **颜色** | 继承父级文字颜色或使用语义色 |
| **交互态** | 悬停时颜色加深或透明度变化 |
| **点击区域** | 最小 32x32px (icon 比实际尺寸小时增加 padding) |

### 禁止事项

- ❌ **禁止使用 Emoji 作为图标** (🚀 🎨 ⚙️ 等在不同平台渲染不一致)
- ❌ **禁止混合使用不同图标库** (如 Lucide + Font Awesome 混用)
- ❌ **禁止随意修改图标描边宽度** (保持视觉一致性)

---

## 间距系统

### 间距 Token (8px 基础单位)

```css
:root {
  --spacing-0: 0;
  --spacing-1: 4px;    /* 0.5 单位 */
  --spacing-2: 8px;    /* 1 单位 */
  --spacing-3: 12px;   /* 1.5 单位 */
  --spacing-4: 16px;   /* 2 单位 */
  --spacing-5: 20px;   /* 2.5 单位 */
  --spacing-6: 24px;   /* 3 单位 */
  --spacing-8: 32px;   /* 4 单位 */
  --spacing-10: 40px;  /* 5 单位 */
  --spacing-12: 48px;  /* 6 单位 */
  --spacing-16: 64px;  /* 8 单位 */
}
```

### 间距应用规则

| 场景 | 间距 | 说明 |
|------|-----|-----|
| **组件内间距** | 8px - 16px | 按钮 padding、卡片内间距 |
| **组件间间距** | 16px - 24px | 同级元素间距 |
| **模块间间距** | 24px - 32px | 卡片之间、区块之间 |
| **页面边距** | 24px - 48px | 内容区与边缘的间距 |
| **表格单元格** | 12px - 16px | 表格内 padding |

---

## 圆角系统

### 圆角 Token

```css
:root {
  --radius-none: 0;
  --radius-sm: 4px;     /* 小元素：标签、徽章 */
  --radius-md: 8px;     /* 中等元素：按钮、输入框 */
  --radius-lg: 12px;    /* 大元素：卡片、弹窗 */
  --radius-xl: 16px;    /* 特大元素：侧边栏抽屉 */
  --radius-full: 9999px; /* 圆形：头像、圆形按钮 */
}
```

### 圆角应用

| 元素 | 圆角 |
|------|-----|
| 按钮 | 8px |
| 输入框 | 8px |
| 卡片 | 12px |
| 弹窗 | 12px |
| 侧边抽屉 | 0 (右侧直边) 或 16px (左侧圆角) |
| 标签/徽章 | 4px |
| 头像 | 9999px (圆形) |
| Toast | 8px |

---

## 阴影系统

### 阴影层级

```css
:root {
  /* Elevation 1: 卡片、下拉菜单 */
  --shadow-1: 0 1px 2px rgba(0, 0, 0, 0.05);

  /* Elevation 2: 悬浮卡片 */
  --shadow-2: 0 4px 6px rgba(0, 0, 0, 0.1);

  /* Elevation 3: 弹窗 */
  --shadow-3: 0 10px 15px rgba(0, 0, 0, 0.1);

  /* Elevation 4: 模态对话框 */
  --shadow-4: 0 20px 25px rgba(0, 0, 0, 0.15);
}
```

### 深色模式阴影

深色模式下阴影效果较弱，建议使用边框或背景色差异来区分层级：

```css
.dark {
  --shadow-1: 0 1px 2px rgba(0, 0, 0, 0.3);
  --shadow-2: 0 4px 6px rgba(0, 0, 0, 0.4);
  --shadow-3: 0 10px 15px rgba(0, 0, 0, 0.5);
  --shadow-4: 0 20px 25px rgba(0, 0, 0, 0.6);
}
```

---

## 布局系统

### 页面布局框架

```
┌────────────────────────────────────────────────────────────┐
│ 顶部导航栏 (height: 64px)                                  │
├────────────┬───────────────────────────────────────────────┤
│ 侧边栏     │ 主内容区                                      │
│ (width:    │ ┌─────────────────────────────────────────┐   │
│  240px)    │ │ 面包屑                                   │   │
│            │ ├─────────────────────────────────────────┤   │
│            │ │ 页面标题 + 操作按钮                      │   │
│            │ ├─────────────────────────────────────────┤   │
│            │ │ 内容区域                                 │   │
│            │ │ (min-height: calc(100vh - 64px - 48px)) │   │
│            │ └─────────────────────────────────────────┘   │
├────────────┴───────────────────────────────────────────────┤
│ 底部版权区 (可选, height: 48px)                            │
└────────────────────────────────────────────────────────────┘
```

### 响应式断点

| 断点名称 | 范围 | 侧边栏行为 |
|---------|------|----------|
| **Mobile** | < 768px | 隐藏，点击菜单按钮展开 |
| **Tablet** | 768px - 1024px | 折叠为图标模式 (64px) |
| **Desktop** | > 1024px | 展开完整模式 (240px) |

```css
:root {
  --breakpoint-sm: 640px;
  --breakpoint-md: 768px;
  --breakpoint-lg: 1024px;
  --breakpoint-xl: 1280px;
  --breakpoint-2xl: 1536px;
}
```

### 内容区最大宽度

```css
:root {
  --content-max-width: 1280px;  /* 内容区最大宽度 */
  --sidebar-width: 240px;       /* 侧边栏展开宽度 */
  --sidebar-collapsed-width: 64px; /* 侧边栏折叠宽度 */
  --header-height: 64px;        /* 顶部导航高度 */
  --footer-height: 48px;        /* 底部版权高度 */
}
```

---

## 动效规范

### 动效时长

| 类型 | 时长 | 用途 |
|------|-----|-----|
| **微交互** | 150ms | 悬停、点击反馈 |
| **标准过渡** | 200ms | 下拉展开、开关切换 |
| **弹窗进入** | 250ms | 弹窗、抽屉打开 |
| **弹窗退出** | 150ms | 弹窗、抽屉关闭 |
| **页面切换** | 300ms | 路由切换动画 |

### 缓动函数

```css
:root {
  --ease-in: cubic-bezier(0.4, 0, 1, 1);        /* 进入 */
  --ease-out: cubic-bezier(0, 0, 0.2, 1);       /* 退出 */
  --ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);  /* 过渡 */
  --ease-spring: cubic-bezier(0.175, 0.885, 0.32, 1.275); /* 弹性效果 */
}
```

### 动效类型

| 动效 | CSS 属性 | 示例 |
|------|---------|-----|
| **淡入淡出** | `opacity` | 弹窗显示/隐藏 |
| **滑动** | `transform: translateY/X` | 抽屉、下拉菜单 |
| **缩放** | `transform: scale` | 弹窗进入效果 |
| **展开** | `height`, `max-height` | 树节点展开 |

### Reduced Motion 支持

```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 无障碍规范

### 对比度要求

| 类型 | 最小对比度 | WCAG 标准 |
|------|----------|----------|
| **正文文字** | 4.5:1 | AA |
| **大号文字 (>18px)** | 3:1 | AA |
| **图标/图形元素** | 3:1 | AA |

### Focus 状态

```css
:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

/* 不要移除 focus 样式 */
:focus {
  outline: none; /* 仅在使用 :focus-visible 替代时 */
}
```

### 语义化要求

- 所有交互元素必须可键盘访问
- 图标按钮必须有 `aria-label`
- 表格必须有正确的 `<thead>`, `<tbody>` 结构
- 表单字段必须有 `<label>` 关联
- 弹窗必须有 `role="dialog"` 和 `aria-modal="true"`

---

## 设计 Token 导出

### CSS 变量完整定义

```css
:root {
  /* Colors */
  --color-primary: #1E40AF;
  --color-on-primary: #FFFFFF;
  --color-secondary: #1E293B;
  --color-accent: #22C55E;
  --color-destructive: #EF4444;
  --color-warning: #F59E0B;
  --color-info: #3B82F6;
  --color-background: #F8FAFC;
  --color-foreground: #0F172A;
  --color-muted: #F1F5F9;
  --color-border: #E2E8F0;

  /* Typography */
  --font-family: 'Fira Sans', sans-serif;
  --font-family-mono: 'Fira Code', monospace;
  --font-size-xs: 10px;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 18px;
  --font-size-2xl: 24px;
  --font-size-3xl: 32px;

  /* Spacing */
  --spacing-1: 4px;
  --spacing-2: 8px;
  --spacing-3: 12px;
  --spacing-4: 16px;
  --spacing-6: 24px;
  --spacing-8: 32px;

  /* Radius */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-full: 9999px;

  /* Shadows */
  --shadow-1: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-2: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-3: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-4: 0 20px 25px rgba(0, 0, 0, 0.15);

  /* Layout */
  --sidebar-width: 240px;
  --sidebar-collapsed-width: 64px;
  --header-height: 64px;
  --content-max-width: 1280px;

  /* Animation */
  --duration-fast: 150ms;
  --duration-normal: 200ms;
  --duration-slow: 300ms;
  --ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
}

/* Dark Mode */
.dark {
  --color-primary: #0F172A;
  --color-background: #020617;
  --color-foreground: #F8FAFC;
  --color-muted: #1A1E2F;
  --color-border: #334155;
}
```