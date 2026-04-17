# 系统设置

## 页面信息

| 属性 | 值 |
|------|-----|
| **路由** | `/system/settings` |
| **功能** | 系统参数配置，包括基本设置、安全设置、通知设置等 |
| **权限** | `system:settings` (仅管理员) |

---

## 页面布局

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ [Logo] 企业权限管理平台        🔍 搜索...        🔔(3)  [头像▼] Minghui    │
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                               │
│  ⚙️ 系统管理 │   馀页 > 系统管理 > 系统设置                                   │
│   └ 菜单管理 │   ─────────────────────────────                                │
│  ●└ 系统设置 │                                                               │
│              │   ┌────────────────────────────────────────────────────────┐│
│              │   │                                                        ││
│              │   │  [基本设置] [安全设置] [通知设置] [高级设置]            ││
│              │   │                                                        ││
│              │   │  ─── 基本设置 ──────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  系统名称                                              ││
│              │   │  ┌──────────────────────────────────────────────────┐ ││
│              │   │  │ 企业权限管理平台                                  │ ││
│              │   │  └──────────────────────────────────────────────────┘ ││
│              │   │                                                        ││
│              │   │  系统Logo                                              ││
│              │   │  ┌──────────┐  [上传新Logo]                            ││
│              │   │  │ [当前Logo]│                                         ││
│              │   │  └──────────┘                                          ││
│              │   │                                                        ││
│              │   │  默认主题                                              ││
│              │   │  [▼ 浅色模式]                                          ││
│              │   │                                                        ││
│              │   │  语言设置                                              ││
│              │   │  [▼ 简体中文]                                          ││
│              │   │                                                        ││
│              │   │  ─── 安全设置 ──────────────────────────────────────── ││
│              │   │                                                        ││
│              │   │  密码策略                                              ││
│              │   │  ☑ 最少8位字符                                         ││
│              │   │  ☑ 必须包含大小写字母                                   ││
│              │   │  ☑ 必须包含数字                                        ││
│              │   │  ☐ 必须包含特殊字符                                     ││
│              │   │                                                        ││
│              │   │  登录策略                                              ││
│              │   │  失败锁定次数: [5] 次                                   ││
│              │   │  锁定时长:     [30] 分钟                                ││
│              │   │  ☑ 启用验证码                                          ││
│              │   │  ☑ 首次登录修改密码                                    ││
│              │   │                                                        ││
│              │   │  会话设置                                              ││
│              │   │  Token有效期: [24] 小时                                 ││
│              │   │  ☑ 同账号单点登录 (踢出其他会话)                        ││
│              │   │                                                        ││
│              │   │  ┌────────────┐                                        │ │
│              │   │  │    取消    │       [保存设置]                       │ │
│              │   │  └────────────┘                                        │ │
│              │   │                                                        ││
│              │   └────────────────────────────────────────────────────────┘│
│              │                                                               │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## Tab 切换

| Tab | 内容 |
|------|-----|
| **基本设置** | 系统名称、Logo、主题、语言等 |
| **安全设置** | 密码策略、登录策略、会话设置 |
| **通知设置** | 邮件通知、短信通知、站内通知 |
| **高级设置** | 日志保留、存储配置、性能参数 |

---

## 基本设置

| 字段 | 类型 | 说明 |
|------|-----|-----|
| **系统名称** | 输入框 | 显示在顶部导航和登录页 |
| **系统Logo** | 图片上传 | Logo 图片，建议 120x40px |
| **默认主题** | 下拉选择 | 浅色模式/深色模式 |
| **语言设置** | 下拉选择 | 简体中文/English 等 |
| **版权信息** | 输入框 | 底部版权文字 |

---

## 安全设置

### 密码策略

```
密码策略
☑ 最少8位字符
☑ 必须包含大小写字母
☑ 必须包含数字
☐ 必须包含特殊字符

说明:
- 这些规则将在用户创建和密码修改时校验
- 勾选的规则必须在密码中满足
```

### 登录策略

```
登录策略
失败锁定次数: [5] 次    ← 连续失败N次后锁定账号
锁定时长:     [30] 分钟 ← 锁定持续时间
☑ 启用验证码           ← 失败N次后显示验证码
☑ 首次登录修改密码      ← 新用户首次登录强制修改密码
```

### 会话设置

```
会话设置
Token有效期: [24] 小时   ← JWT Token 有效期
☑ 同账号单点登录        ← 同账号只能有一个活跃会话
```

---

## 通知设置

### 邮件通知

```
─── 雷件通知 ────────────────────────────────────

SMTP服务器: [smtp.corp.com]
SMTP端口:   [587]
发件人邮箱: [admin@corp.com]
发件人名称: [系统管理员]

认证设置:
☑ 启用SMTP认证
SMTP用户名: [admin@corp.com]
SMTP密码:   [        ]

通知事件:
☑ 用户创建通知
☑ 密码重置通知
☐ 系统警告通知

[发送测试邮件]
```

### 短信通知

```
─── 短信通知 ────────────────────────────────────

短信服务商: [▼ 阿里云短信]
API Key:    [                    ]
API Secret: [                    ]
短信签名:   [企业权限系统]

通知事件:
☐ 登录验证码
☐ 密码重置验证码
☐ 异常登录警报

[发送测试短信]
```

---

## 高级设置

### 日志配置

```
─── 日志配置 ────────────────────────────────────

日志保留天数: [90] 天    ← 超过天数的日志自动清理

日志级别:    [▼ INFO]   ← DEBUG/INFO/WARN/ERROR

☑ 记录登录日志
☑ 记录操作日志
☐ 记录访问日志 (数据量较大)
```

### 存储配置

```
─── 存储配置 ────────────────────────────────────

文件存储方式: [▼ 本地存储]
              ○ 本地存储
              ○ 阿里云OSS
              ○ 腾讯云COS

本地存储路径: [/data/uploads]
最大文件大小: [10] MB
允许的文件类型: [jpg,png,gif,pdf,doc,xls]
```

### 性能参数

```
─── 性能参数 ────────────────────────────────────

页面缓存:    ☑ 启用 Redis 缓存
缓存时长:    [300] 秒

并发限制:    [100] 请求/秒
超时时间:    [30] 秒
```

---

## 保存流程

```
1. 用户修改设置项
2. 点击「保存设置」按钮
3. 表单验证:
   - 必填字段检查
   - 格式验证 (如邮箱格式)
   - SMTP 连接测试 (可选)
4. 验证通过:
   - 按钮 show loading
   - 发送 API 请求
5. 成功:
   - Toast 提示「设置已保存」
   - 某些设置需要重启服务生效，提示用户
6. 失败:
   - Toast 提示错误信息
```

---

## Vue 组件示例

```vue
<template>
  <div class="system-settings-page">
    <!-- 面包屑 -->
    <Breadcrumb :items="breadcrumbItems" />

    <!-- Tab 切换 -->
    <div class="settings-tabs">
      <TabNav
        :tabs="tabs"
        :active="activeTab"
        @change="activeTab = $event"
      />
    </div>

    <!-- 设置表单 -->
    <div class="settings-form">
      <!-- 基本设置 -->
      <template v-if="activeTab === 'basic'">
        <SettingsSection title="基本设置">
          <FormGroup label="系统名称" required>
            <input v-model="settings.systemName" />
          </FormGroup>

          <FormGroup label="系统Logo">
            <LogoUpload
              :src="settings.logo"
              @change="handleLogoChange"
            />
          </FormGroup>

          <FormGroup label="默认主题">
            <Select v-model="settings.theme" :options="themeOptions" />
          </FormGroup>

          <FormGroup label="语言设置">
            <Select v-model="settings.language" :options="languageOptions" />
          </FormGroup>

          <FormGroup label="版权信息">
            <input v-model="settings.copyright" />
          </FormGroup>
        </SettingsSection>
      </template>

      <!-- 安全设置 -->
      <template v-if="activeTab === 'security'">
        <SettingsSection title="密码策略">
          <CheckboxGroup
            v-model="settings.passwordPolicy"
            :options="passwordPolicyOptions"
          />
        </SettingsSection>

        <SettingsSection title="登录策略">
          <FormGroup label="失败锁定次数">
            <NumberInput v-model="settings.lockoutThreshold" :min="1" :max="10" />
            <span class="unit">次</span>
          </FormGroup>

          <FormGroup label="锁定时长">
            <NumberInput v-model="settings.lockoutDuration" :min="1" :max="60" />
            <span class="unit">分钟</span>
          </FormGroup>

          <CheckboxGroup
            v-model="settings.loginPolicy"
            :options="loginPolicyOptions"
          />
        </SettingsSection>

        <SettingsSection title="会话设置">
          <FormGroup label="Token有效期">
            <NumberInput v-model="settings.tokenExpiry" :min="1" :max="168" />
            <span class="unit">小时</span>
          </FormGroup>

          <ToggleSwitch
            v-model="settings.singleSession"
            label="同账号单点登录"
          />
        </SettingsSection>
      </template>

      <!-- 通知设置 -->
      <template v-if="activeTab === 'notification'">
        <SettingsSection title="邮件通知">
          <FormGroup label="SMTP服务器" required>
            <input v-model="settings.smtpServer" />
          </FormGroup>
          <FormGroup label="SMTP端口" required>
            <NumberInput v-model="settings.smtpPort" :min="1" :max="65535" />
          </FormGroup>
          <FormGroup label="发件人邮箱" required>
            <input v-model="settings.senderEmail" type="email" />
          </FormGroup>

          <div class="test-button">
            <Button @click="sendTestEmail">发送测试邮件</Button>
          </div>
        </SettingsSection>

        <SettingsSection title="短信通知">
          <!-- ... -->
        </SettingsSection>
      </template>

      <!-- 高级设置 -->
      <template v-if="activeTab === 'advanced'">
        <SettingsSection title="日志配置">
          <FormGroup label="日志保留天数">
            <NumberInput v-model="settings.logRetentionDays" :min="30" :max="365" />
          </FormGroup>
          <FormGroup label="日志级别">
            <Select v-model="settings.logLevel" :options="logLevelOptions" />
          </FormGroup>
        </SettingsSection>

        <SettingsSection title="存储配置">
          <!-- ... -->
        </SettingsSection>
      </template>
    </div>

    <!-- 操作按钮 -->
    <div class="form-actions">
      <Button type="secondary" @click="loadSettings">取消</Button>
      <Button type="primary" :loading="saving" @click="handleSave">
        保存设置
      </Button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const breadcrumbItems = [
  { label: '首页', route: '/home' },
  { label: '系统管理', route: '/system' },
  { label: '系统设置' }
]

const tabs = [
  { key: 'basic', label: '基本设置' },
  { key: 'security', label: '安全设置' },
  { key: 'notification', label: '通知设置' },
  { key: 'advanced', label: '高级设置' }
]

const activeTab = ref('basic')
const settings = ref({})
const saving = ref(false)

const loadSettings = async () => {
  const res = await api.getSystemSettings()
  settings.value = res.data
}

const handleSave = async () => {
  saving.value = true
  try {
    await api.updateSystemSettings(settings.value)
    showToast('设置已保存')

    // 检查是否需要重启服务
    if (needsRestart(settings.value)) {
      showToast('部分设置需要重启服务才能生效', 'warning')
    }
  } catch (err) {
    showToast(err.message, 'error')
  } finally {
    saving.value = false
  }
}

const handleLogoChange = async (file) => {
  const res = await api.uploadLogo(file)
  settings.value.logo = res.data.url
}

const sendTestEmail = async () => {
  try {
    await api.sendTestEmail()
    showToast('测试邮件已发送')
  } catch (err) {
    showToast('邮件发送失败: ' + err.message, 'error')
  }
}

onMounted(() => {
  loadSettings()
})
</script>
```