# Claude Code 使用文档

> 创建日期：2026-04-14
> 版本：Claude Code 2.1.92
> 来源：Anthropic 官方 CLI

## 一、概述

**Claude Code** 是 Anthropic 官方的 Claude CLI 工具，将 Claude Opus 4.6 模型带入终端、桌面应用、网页和 IDE 扩展中。这是一个革命性的 AI 编程助手，能够理解代码、执行命令、编写代码并进行智能交互。

### 项目亮点

- **官方出品**：Anthropic 官方开发的 CLI 工具
- **最强模型**：搭载 Claude Opus 4.6 / Sonnet 4.6 模型
- **多平台支持**：终端、桌面（Mac/Windows）、Web（claude.ai/code）、IDE 扩展（VS Code、JetBrains）
- **完整工具链**：50+ 斜杠命令、20+ 内置工具、MCP 服务器集成
- **智能代理系统**：多智能体协作、子智能体专业化
- **记忆系统**：跨会话持久化记忆
- **安全可控**：6 种权限模式、Hooks 自动化、详细配置

### 核心能力

| 能力 | 说明 |
|------|------|
| **代码理解** | 阅读项目、理解架构、分析依赖 |
| **代码编写** | 创建文件、编辑代码、重构优化 |
| **命令执行** | 运行测试、构建项目、Git 操作 |
| **智能搜索** | 网络搜索、网页抓取、文档获取 |
| **团队协作** | 多智能体团队、任务分配、消息传递 |
| **持续学习** | 记忆系统、模式提取、跨会话知识 |

---

## 二、安装配置

### 2.1 安装方式

```bash
# npm 全局安装（推荐）
npm install -g @anthropic/claude-code

# Bun 全局安装
bun install -g @anthropic/claude-code

# 一次性运行（无需安装）
npx @anthropic/claude-code
bunx @anthropic/claude-code
```

### 2.2 系统要求

| 要求 | 说明 |
|------|------|
| **运行时** | Node.js 18+ 或 Bun 1.0+ |
| **Shell** | Bash、Zsh、Fish |
| **Windows** | WSL 1/2 或 Git Bash |
| **认证** | Anthropic 账户（需登录） |

### 2.3 平台支持

| 平台 | 支持方式 |
|------|----------|
| macOS | 原生支持 |
| Windows 11 | 原生或 WSL |
| Linux | 所有主流发行版 |
| VS Code | IDE 扩展 |
| JetBrains | IDE 扩展 |

### 2.4 首次运行

```bash
# 启动 Claude Code
claude

# 首次启动流程
1. 检测安装环境
2. 引导 Anthropic 账户认证
3. 自动检查并安装后台更新
4. 进入交互式 REPL
```

### 2.5 命令行参数

```bash
# 基本启动
claude                          # 在当前目录启动

# 指定目录
claude --cwd /path/to/project   # 在指定目录启动

# 权限模式
claude --permission-mode acceptEdits  # 自动批准编辑
claude --permission-mode auto         # 智能自动模式

# 模型选择
claude --model claude-opus-4-6        # Opus 模型
claude --model claude-sonnet-4-6      # Sonnet 模型

# 其他参数
claude --verbose                     # 详细输出
claude --no-update-check             # 跳过更新检查
claude --print                       # 打印模式（非交互）
claude --input-file prompts.txt      # 从文件读取输入
```

---

## 三、斜杠命令列表（50+）

### 3.1 核心命令

| 命令 | 说明 |
|------|------|
| `/help` | 显示帮助信息和可用命令 |
| `/clear` | 清除当前对话上下文 |
| `/init` | 在项目中创建 CLAUDE.md 初始化文档 |
| `/status` | 显示当前会话状态和项目信息 |
| `/exit` | 退出 Claude Code 会话 |
| `/resume` | 恢复之前的会话状态 |

### 3.2 代码操作命令

| 命令 | 说明 |
|------|------|
| `/commit` | 智能生成提交信息并提交代码 |
| `/review` | 代码审查（质量、安全、性能） |
| `/diff` | 显示文件变更差异 |
| `/undo` | 撤销上次操作 |
| `/compact` | 压缩对话上下文 |
| `/simplify` | 代码简化审查 |

### 3.3 开发流程命令

| 命令 | 说明 |
|------|------|
| `/plan` | 进入规划模式，设计实现方案 |
| `/test` | 运行测试并分析结果 |
| `/debug` | 启动调试流程 |
| `/build-fix` | 修复构建错误 |
| `/refactor` | 代码重构指导 |

### 3.4 配置命令

| 命令 | 说明 |
|------|------|
| `/config` | 打开设置界面（标签页形式） |
| `/permissions` | 查看和管理工具权限 |
| `/models` | 切换 Claude 模型 |
| `/hooks` | 查看已注册的 hooks |
| `/mcp` | MCP 服务器管理 |
| `/statusline` | 设置状态行 UI |

### 3.5 认证和账户命令

| 命令 | 说明 |
|------|------|
| `/auth` | 认证管理 |
| `/logout` | 登出账户 |
| `/login` | 登录账户 |

### 3.6 2026 新增命令

| 命令 | 说明 |
|------|------|
| `/effort` | 控制 token 用量/努力级别 |
| `/voice` | 语音输入、审查和提交代码 |
| `/teleport` | 恢复到之前的会话状态 |
| `/remote-env` | 连接到远程环境 |
| `/security-review` | 安全审查 |
| `/batch` | 并行执行大规模代码变更 |
| `/loop` | 按间隔定时执行命令 |
| `/claude-api` | 构建 Claude API 应用 |
| `/insights` | 生成会话分析报告 |
| `/memory` | 记忆管理 |
| `/subagent` | 子智能体管理 |
| `/extension` | 管理扩展 |

### 3.7 其他命令

| 命令 | 说明 |
|------|------|
| `/editor` | 打开外部编辑器 |
| `/copilot` | Copilot 模式 |
| `/stats` | 显示使用统计 |
| `/tree` | 显示项目树结构 |
| `/bug` | 报告 bug |
| `/feedback` | 提交功能请求或反馈 |
| `/buddy` | 与旁边的 Gravy 小伙伴互动 |

---

## 四、内置工具列表（20+）

### 4.1 文件操作工具

| 工具 | 说明 | 权限 |
|------|------|------|
| `Read` | 读取文件内容，支持图片、PDF、笔记本 | 无需 |
| `Write` | 创建或完全重写文件 | 需批准 |
| `Edit` | 字符串替换编辑文件 | 需批准 |
| `Glob` | 文件模式匹配搜索 | 无需 |
| `Grep` | 文本内容搜索（ripgrep） | 无需 |
| `NotebookEdit` | 编辑 Jupyter 笔记本单元格 | 需批准 |

### 4.2 Shell/执行工具

| 工具 | 说明 | 权限 |
|------|------|------|
| `Bash` | 执行 Shell 命令 | 需批准 |
| `Skill` | 执行用户可调用的技能 | 无需 |

### 4.3 网络工具

| 工具 | 说明 | 权限 |
|------|------|------|
| `WebFetch` | 获取网页内容并处理 | 需批准 |
| `WebSearch` | 网络搜索（仅美国可用） | 需批准 |

### 4.4 代理和任务工具

| 工具 | 说明 |
|------|------|
| `Agent` | 启动专业子智能体执行任务 |
| `SendMessage` | 发送消息给团队成员 |
| `TeamCreate` | 创建多智能体团队 |
| `TeamDelete` | 删除团队 |
| `TaskCreate` | 创建结构化任务 |
| `TaskUpdate` | 更新任务状态 |
| `TaskGet` | 获取任务详情 |
| `TaskList` | 列出所有任务 |
| `TaskOutput` | 获取后台任务输出 |
| `TaskStop` | 停止后台任务 |

### 4.5 交互和控制工具

| 工具 | 说明 |
|------|------|
| `AskUserQuestion` | 向用户提问以获取输入 |
| `EnterPlanMode` | 进入规划模式 |
| `ExitPlanMode` | 退出规划模式（需用户批准） |
| `EnterWorktree` | 创建 Git 工作树 |
| `ExitWorktree` | 退出工作树 |

### 4.6 定时和记忆工具

| 工具 | 说明 |
|------|------|
| `CronCreate` | 创建定时任务 |
| `CronDelete` | 删除定时任务 |
| `CronList` | 列出定时任务 |

### 4.7 MCP 工具（由 MCP 服务器提供）

| 工具前缀 | 说明 |
|----------|------|
| `mcp__pencil__*` | Pencil 设计工具（.pen 文件编辑） |
| `mcp__github__*` | GitHub MCP 服务器工具 |
| `mcp__*` | 其他 MCP 服务器工具 |

---

## 五、Agent 系统（子智能体）

### 5.1 内置 Agent 类型

| Agent 类型 | 说明 | 工具 |
|------------|------|------|
| `general-purpose` | 通用代理，研究复杂问题 | 全部工具 |
| `statusline-setup` | 配置状态行设置 | Read, Edit |
| `Explore` | 快速探索代码库 | 除 Agent/Edit/Write 外 |
| `Plan` | 软件架构规划 | 除 Agent/Edit/Write 外 |
| `claude-code-guide` | Claude Code 使用指南 | Glob, Grep, Read, WebFetch, WebSearch |

### 5.2 自定义 Agent

创建文件 `.claude/agents/my-agent.md`：

```markdown
---
name: my-agent
description: 我的自定义代理
model: claude-sonnet-4-6
tools:
  - Read
  - Write
  - Bash(npm test)
---

你是一个专注于测试的代理。
...
```

### 5.3 Agent 配置选项

```json
{
  "agents": {
    "test-runner": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Write", "Bash(npm test)"],
      "memory": {
        "enabled": true,
        "path": "./.claude/memory/test-runner"
      }
    }
  }
}
```

### 5.4 调用 Agent

```python
# 通过 Agent 工具调用
Agent(
    subagent_type="Explore",
    description="探索代码库结构",
    prompt="查找项目中的所有 API 端点"
)

Agent(
    subagent_type="Plan",
    description="规划功能实现",
    prompt="为用户认证功能设计实现方案"
)

# 后台运行
Agent(
    subagent_type="general-purpose",
    prompt="分析所有依赖并生成报告",
    run_in_background=true
)
```

### 5.5 Agent Teams（团队）

```bash
# 创建团队
TeamCreate(
    team_name="dev-team",
    description="开发团队"
)

# 创建任务
TaskCreate(subject="实现 API", description="详细描述")

# 分配任务
TaskUpdate(taskId="1", owner="agent-name")

# 发送消息
SendMessage(to="agent-name", message="开始工作")

# 关闭团队
TeamDelete()
```

---

## 六、Memory 系统

### 6.1 记忆类型

| 类型 | 说明 | 使用场景 |
|------|------|----------|
| `user` | 用户信息、角色、偏好 | 调整响应风格 |
| `feedback` | 用户指导（正确/错误方法） | 避免重复错误 |
| `project` | 项目状态、目标、进展 | 理解项目上下文 |
| `reference` | 外部资源指针 | 访问外部系统 |

### 6.2 记忆文件结构

```
.claude/projects/<project-hash>/memory/
├── MEMORY.md           # 主索引文件
├── user_role.md        # 用户信息
├── feedback_testing.md # 反馈记忆
├── project_status.md   # 项目状态
└── reference_links.md  # 外部引用
```

### 6.3 记忆文件格式

```markdown
---
name: feedback-testing
description: 测试反馈记忆
type: feedback
---

测试必须运行真实数据库，不使用 mock。
**Why:** 上季度 mocked 测试通过但生产迁移失败。
**How to apply:** 所有集成测试配置真实数据库连接。
```

### 6.4 MEMORY.md 索引格式

```markdown
# Memory Index

- [User Role](user_role.md) — 用户角色和偏好
- [Testing Feedback](feedback_testing.md) — 测试反馈指导
- [Project Status](project_status.md) — 项目当前状态
```

### 6.5 记忆使用规则

**何时保存**：
- 用户明确要求记住/忘记
- 学习用户角色、偏好
- 用户纠正或确认非明显方法
- 了解项目状态、目标、Bug

**不保存的内容**：
- 代码模式、架构（可从代码推导）
- Git 历史（git log 是权威来源）
- 调试方案（修复在代码中）
- CLAUDE.md 中已有的内容
- 临时任务细节

---

## 七、Hooks 配置

### 7.1 Hook 类型

| Hook 类型 | 触发时机 | 用途 |
|-----------|----------|------|
| `PreToolUse` | 工具调用前 | 权限自动化、安全检查 |
| `PostToolUse` | 工具调用后 | 日志记录、触发事件 |
| `Stop` | 会话结束 | 清理、总结 |
| `SessionStart` | 会话开始 | 初始化、上下文注入 |
| `PreCompact` | 紧凑前 | 选择性保留 |

### 7.2 配置方式

**settings.json**：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/check-dangerous.sh"
          }
        ]
      },
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/file-warning.sh"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./hooks/log-command.sh"
          }
        ]
      }
    ],
    "Stop": [
      {
        "type": "command",
        "command": "./hooks/session-end.sh"
      }
    ]
  }
}
```

### 7.3 Hook 脚本示例

```bash
#!/bin/bash
# pre-tool-use.sh - 安全检查

# 读取 stdin JSON
input=$(cat)
tool=$(echo "$input" | jq -r '.tool')
command=$(echo "$input" | jq -r '.input.command')

# 检查危险命令
if [[ "$command" =~ "rm -rf" ]]; then
    echo '{"decision": "deny", "reason": "危险命令被阻止"}'
    exit 0
fi

# 默认允许
echo '{"decision": "approve"}'
```

### 7.4 Hook 用途

| 用途 | 示例 |
|------|------|
| **安全检查** | 阻止 `rm -rf`、`DROP TABLE`、`force-push` |
| **权限自动化** | 根据规则自动批准特定操作 |
| **工作流集成** | 触发 CI/CD、发送通知 |
| **上下文注入** | 会话开始时添加项目信息 |
| **持续学习** | 会话结束时提取模式 |

---

## 八、权限模式

### 8.1 六种权限模式

| 模式 | 文件编辑 | Shell | 网络 | 说明 |
|------|----------|-------|------|------|
| `default` | 提示 | 提示 | 提示 | 最安全 |
| `acceptEdits` | 自动批准 | 提示 | 提示 | 适合重构 |
| `auto` | 自动批准 | AI判断 | AI判断 | 智能模式 |
| `plan` | 提示 | 提示 | 提示 | 仅规划 |
| `dontAsk` | 自动批准 | 自动批准 | 自动批准 | 跳过所有 |
| `bypassPermissions` | 完全绕过 | 完全绕过 | 完全绕过 | 无限制 |

### 8.2 权限类型

| 权限模式 | 说明 |
|----------|------|
| `Edit(*)` | 编辑任意文件 |
| `Edit(path/to/file)` | 编辑指定文件 |
| `Write(*)` | 写入任意文件 |
| `Write(path/to/file)` | 写入指定文件 |
| `Bash(*)` | 执行任意命令 |
| `Bash(git:*)` | 执行 git 命令 |
| `Bash(npm:*)` | 执行 npm 命令 |
| `WebFetch` | 获取网页 |
| `WebSearch` | 网络搜索 |
| `mcp__github` | 使用 GitHub MCP |

### 8.3 权限配置

```json
{
  "permissions": {
    "allow": [
      "Edit(src/**)",
      "Write(src/**)",
      "Bash(git:*)",
      "Bash(npm:*)",
      "Bash(mvn:*)",
      "Bash(python:*)",
      "WebFetch(domain:github.com)"
    ],
    "deny": [
      "Bash(rm -rf /*)",
      "Bash(sudo:*)",
      "Edit(.env)",
      "Write(.env)"
    ]
  },
  "defaultMode": "acceptEdits"
}
```

### 8.4 设置权限模式

```bash
# 启动时指定
claude --permission-mode acceptEdits

# 配置文件设置
{
  "defaultMode": "acceptEdits"
}

# 切换模式命令
/permissions mode acceptEdits
```

---

## 九、settings.json 配置详解

### 9.1 配置层级

| 层级 | 位置 | 优先级 |
|------|------|--------|
| 项目级 | `.claude/settings.json` | 最高 |
| 用户级 | `~/.claude/settings.json` | 中 |
| 企业级 | 系统策略 | 基础 |

### 9.2 完整配置示例

```json
{
  // 模型配置
  "model": "claude-sonnet-4-6",
  "modelTemperature": 0.7,
  "maxTokens": 8192,
  "reasoningEffort": "medium",

  // 环境变量
  "env": {
    "ANTHROPIC_API_KEY": "your-key",
    "CUSTOM_VAR": "value"
  },

  // MCP 服务器
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "your-token"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path"]
    }
  },

  // 权限配置
  "permissions": {
    "allow": ["Edit(*)", "Write(*)", "Bash(git:*)"],
    "deny": ["Bash(rm -rf /*)"]
  },
  "defaultMode": "acceptEdits",

  // Hooks 配置
  "hooks": {
    "PreToolUse": [...],
    "PostToolUse": [...]
  },

  // 子智能体配置
  "agents": {
    "test-runner": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Bash(npm test)"]
    }
  },

  // 行为配置
  "autoAccept": false,
  "verbose": true,
  "includeCoAuthoredBy": true,

  // 状态行配置
  "statusLine": {
    "type": "command",
    "command": "bun ~/.claude/plugins/status-line.ts"
  },

  // 插件配置
  "enabledPlugins": {
    "superpowers@claude-plugins-official": true
  }
}
```

### 9.3 配置项详解

| 配置项 | 类型 | 说明 |
|--------|------|------|
| `model` | string | 默认模型（sonnet/opus） |
| `modelTemperature` | number | 模型温度（0-1） |
| `maxTokens` | number | 最大输出 token |
| `reasoningEffort` | string | 思考深度（low/medium/high） |
| `env` | object | 环境变量 |
| `mcpServers` | object | MCP 服务器配置 |
| `permissions` | object | 权限规则 |
| `defaultMode` | string | 默认权限模式 |
| `hooks` | object | Hooks 配置 |
| `agents` | object | 子智能体配置 |
| `autoAccept` | boolean | 自动接受提示 |
| `verbose` | boolean | 详细输出 |
| `includeCoAuthoredBy` | boolean | 提交包含 co-authored-by |

---

## 十、MCP 服务器集成

### 10.1 MCP 协议说明

MCP（Model Context Protocol）是连接 Claude Code 与外部工具和服务的协议，允许 Claude 使用各种外部工具。

### 10.2 流行 MCP 服务器

| 服务器 | 用途 | 安装 |
|--------|------|------|
| GitHub | 仓库操作、PR/Issue | `@modelcontextprotocol/server-github` |
| Filesystem | 文件系统操作 | `@modelcontextprotocol/server-filesystem` |
| Postgres | PostgreSQL 数据库 | `@modelcontextprotocol/server-postgres` |
| Memory | 持久化记忆 | `@modelcontextprotocol/server-memory` |
| Pencil | 设计文件（.pen） | 内置 Pencil MCP |
| Context7 | 实时库文档 | `context7` |

### 10.3 配置 MCP 服务器

```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_xxx"
      }
    },
    "pencil": {
      "command": "bun",
      "args": ["run", "/path/to/pencil-server"]
    }
  },
  "enableAllProjectMcpServers": true
}
```

### 10.4 MCP 工具命名

MCP 工具使用前缀命名：`mcp__<server>__<tool>`

例如：
- `mcp__pencil__batch_design` - Pencil 设计操作
- `mcp__pencil__batch_get` - Pencil 内容读取
- `mcp__github__create_issue` - GitHub 创建 Issue

### 10.5 MCP 命令

```bash
# 查看已连接的 MCP 服务器
/mcp list

# 添加服务器
/mcp add <server-name>

# 移除服务器
/mcp remove <server-name>
```

---

## 十一、Skills（技能）系统

### 11.1 内置 Skills

| Skill | 触发场景 | 说明 |
|-------|----------|------|
| `update-config` | 配置 harness | 配置 Claude Code 设置 |
| `simplify` | 代码审查 | 审查代码质量、复用、效率 |
| `loop` | 定时任务 | 按间隔执行命令 |
| `claude-api` | API 构建 | 构建 Claude API 应用 |

### 11.2 调用 Skills

```bash
# 通过斜杠命令
/commit              # 提交代码
/simplify            # 代码简化审查
/loop 5m /test       # 每5分钟执行测试

# 或通过 Skill 工具
Skill(skill="simplify")
Skill(skill="loop", args="5m /test")
```

### 11.3 自定义 Skills

创建文件 `.claude/skills/my-skill/SKILL.md`：

```markdown
---
name: my-skill
description: 我的自定义技能
userInvoke: true
---

## 功能说明

这个技能用于执行特定任务...

## 使用方式

用户通过 `/my-skill` 调用...
```

---

## 十二、CLAUDE.md 项目指导

### 12.1 文件作用

CLAUDE.md 是项目级指导文件，告诉 Claude Code 如何理解和工作于当前项目。

### 12.2 创建 CLAUDE.md

```bash
# 自动创建
/init

# 或手动创建
```

### 12.3 CLAUDE.md 内容示例

```markdown
# CLAUDE.md

This file provides guidance to Claude Code when working with this project.

## Build Commands

```bash
npm run build      # 构建
npm run test       # 测试
npm run dev        # 开发服务器
```

## Architecture

- 前端：React + TypeScript
- 后端：Node.js + Express
- 数据库：PostgreSQL

## Important Patterns

- 使用 TypeScript 严格模式
- 所有 API 需要 JWT 认证
- 测试覆盖率需 >80%
```

### 12.4 内容建议

| 内容 | 说明 |
|------|------|
| Build Commands | 构建、测试、开发命令 |
| Architecture | 项目架构和技术栈 |
| Important Patterns | 重要模式和约定 |
| Directory Structure | 目录结构说明 |
| Configuration | 配置文件说明 |

---

## 十三、.claude 目录结构

### 13.1 目录结构

```
.claude/
├── settings.json           # 项目级配置
├── CLAUDE.md               # 项目指导文件
├── commands/               # 自定义斜杠命令
│   ├── my-command.md
│   └── ...
├── skills/                 # 自定义 Skills
│   ├── my-skill/
│   │   ├── SKILL.md
│   │   └── scripts/
│   └── ...
├── agents/                 # 自定义 Agents
│   ├── my-agent.md
│   └── ...
├── hooks/                  # Hook 脚本
│   ├── pre-tool-use.sh
│   └── post-tool-use.sh
├── memory/                 # 记忆目录（可选）
│   └── MEMORY.md
├── teams/                  # 团队配置（可选）
│   └── my-team/
│   │   └── config.json
├── tasks/                  # 任务目录（可选）
│   └── my-team/
├── plugins/                # 插件目录
├── bin/                    # 工具脚本
└── worktrees/              # Git 工作树（可选）
```

### 13.2 全局目录

```
~/.claude/
├── settings.json           # 用户级配置
├── plugins/                # 全局插件
│   └── marketplaces/
├── rules/                  # 全局 Rules
├── skills/                 # 全局 Skills
├── agents/                 # 全局 Agents
├── memory/                 # 全局记忆
├── bin/                    # 工具脚本
├── .ccg/                   # ccg 配置
└── projects/               # 项目记忆
    └── <project-hash>/memory/
```

---

## 十四、实战示例

### 14.1 基本使用流程

```bash
# 1. 启动 Claude Code
claude

# 2. 描述任务
"I want to add a login feature"

# 3. Claude 自动：
#    - 阅读项目结构
#    - 理解现有代码
#    - 提出实现方案
#    - 编写代码
#    - 运行测试

# 4. 提交代码
/commit
```

### 14.2 使用规划模式

```bash
# 进入规划模式
/plan

# 或由 Claude 自动进入（复杂任务）

# 规划流程：
# 1. 探索代码库
# 2. 设计实现方案
# 3. 写入计划文件
# 4. 用户批准
# 5. 开始实现
```

### 14.3 使用子智能体

```bash
# 探索代码库
"I need to understand the API structure"
# Claude 自动调用 Explore agent

# 规划复杂功能
"I want to refactor the auth system"
# Claude 自动调用 Plan agent

# 后台运行任务
"Analyze all dependencies and generate a report"
# Claude 使用后台 agent
```

### 14.4 代码审查流程

```bash
# 完成代码修改后
/review

# 流程：
# 1. 检查代码质量
# 2. 检查安全问题
# 3. 检查性能问题
# 4. 自动修复明显问题
# 5. 标记需要确认的问题
```

### 14.5 Git 工作流

```bash
# 查看状态
/status

# 查看变更
/diff

# 提交（智能生成消息）
/commit

# Claude 自动：
# 1. 运行 git status
# 2. 运行 git diff
# 3. 分析变更
# 4. 生成提交消息
# 5. 执行 git commit
```

### 14.6 多智能体协作

```bash
# 创建团队
TeamCreate(team_name="dev-team", description="开发团队")

# 创建任务
TaskCreate(subject="实现 API", description="详细描述")
TaskCreate(subject="编写测试", description="测试描述")

# 启动成员
Agent(subagent_type="general-purpose", name="api-dev", team_name="dev-team")
Agent(subagent_type="general-purpose", name="test-dev", team_name="dev-team")

# 分配任务
TaskUpdate(taskId="1", owner="api-dev")
TaskUpdate(taskId="2", owner="test-dev")

# 发送消息
SendMessage(to="api-dev", message="开始实现 API")

# 完成后关闭
TeamDelete()
```

### 14.7 定时任务

```bash
# 创建定时任务（提醒）
CronCreate(
    cron="30 14 14 4 *",    # 4月14日14:30
    prompt="提醒检查部署",
    recurring=false
)

# 创建周期任务
CronCreate(
    cron="*/5 * * * *",      # 每5分钟
    prompt="运行健康检查",
    recurring=true
)

# 查看任务
CronList()

# 删除任务
CronDelete(id="task-id")
```

---

## 十五、最佳实践

### 15.1 提示技巧

| 技巧 | 说明 |
|------|------|
| **明确目标** | 清楚描述要做什么，不模糊 |
| **提供上下文** | 说明为什么、背景信息 |
| **分步描述** | 大任务分解为小步骤 |
| **指定文件** | 提供具体文件路径 |
| **使用斜杠命令** | 利用内置命令提高效率 |

### 15.2 安全实践

| 实践 | 说明 |
|------|------|
| **使用 hooks** | 阻止危险命令 |
| **配置权限** | 精细控制允许的操作 |
| **审查变更** | 代码修改后使用 /review |
| **检查提交** | 提交前确认变更内容 |
| **保护敏感文件** | 禁止编辑 .env、credentials |

### 15.3 性能优化

| 优化 | 说明 |
|------|------|
| **使用 compact** | 定期压缩上下文 |
| **并行 agent** | 多任务并行执行 |
| **后台运行** | 长任务使用后台模式 |
| **精准搜索** | 使用 Glob/Grep 而非 Bash |
| **限制深度** | 读取文件时限制行数 |

---

## 十六、常见问题解答

### Q1: 如何查看当前版本？

```bash
claude --version
```

### Q2: 如何切换模型？

```bash
# 命令行
claude --model claude-opus-4-6

# 或使用命令
/models
```

### Q3: 如何清除上下文？

```bash
/clear
```

### Q4: 如何查看权限设置？

```bash
/permissions
```

### Q5: 如何配置 MCP 服务器？

```bash
# 编辑 settings.json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"]
    }
  }
}

# 或使用命令
/mcp add github
```

### Q6: 如何创建自定义 Agent？

```bash
# 创建文件
.claude/agents/my-agent.md

# 内容格式
---
name: my-agent
description: 代理描述
model: claude-sonnet-4-6
tools: [Read, Write, Bash]
---

代理提示内容...
```

### Q7: 如何使用记忆系统？

```bash
# 用户明确要求记住
"记住这个项目使用 TDD"

# Claude 自动保存到 memory 目录

# 查看记忆
/memory
```

### Q8: 如何跳过权限提示？

```bash
# 启动时指定模式
claude --permission-mode acceptEdits

# 或配置
{
  "defaultMode": "acceptEdits"
}
```

### Q9: 如何撤销操作？

```bash
/undo
```

### Q10: 如何报告问题？

```bash
/bug      # 报告 bug
/feedback  # 提交反馈
```

---

## 十七、参考资源

### 官方资源

| 资源 | 链接 |
|------|------|
| GitHub 仓库 | https://github.com/anthropics/claude-code |
| 官方网站 | https://claude.ai/code |
| 设置文档 | https://code.claude.com/docs/en/settings |
| 权限文档 | https://code.claude.com/docs/en/permissions |
| Hooks 文档 | https://code.claude.com/docs/en/hooks |
| 命令列表 | https://code.claude.com/docs/en/commands |
| CLI 参考 | https://code.claude.com/docs/en/cli-reference |

### 社区资源

| 资源 | 链接 |
|------|------|
| Visual 配置器 | https://claude-settings.nl/ |
| GitHub 配置示例 | https://github.com/feiskyer/claude-code-settings |
| Awesome Subagents | https://github.com/VoltAgent/awesome-claude-code-subagents |

---

## 十八、更新日志

### Claude Code 2.x 新功能

- **多智能体团队**：TeamCreate/TeamDelete 支持
- **任务系统**：TaskCreate/TaskUpdate/TaskList
- **定时任务**：CronCreate/CronDelete
- **工作树**：EnterWorktree/ExitWorktree 隔离工作
- **消息传递**：SendMessage 跨智能体通信
- **Fast 模式**：/fast 切换快速输出
- **语音输入**：/voice 语音交互
- **远程环境**：/remote-env 连接远程

---

## 十九、统计数据

| 指标 | 数值 |
|------|------|
| 版本 | 2.1.92 |
| 斜杠命令 | 50+ |
| 内置工具 | 20+ |
| Agent 类型 | 5 |
| 权限模式 | 6 |
| MCP 服务器 | 100+ |
| 支持平台 | 6 |

---

## 二十、总结

Claude Code 是 Anthropic 官方的革命性 AI 编程助手，将最强 Claude 模型带入开发工作流：

1. **多平台支持** — 终端、桌面、Web、IDE
2. **完整工具链** — 50+ 命令、20+ 工具、MCP 集成
3. **智能代理** — 多智能体协作、专业化任务
4. **持续学习** — 跨会话记忆、模式提取
5. **安全可控** — 6 种权限模式、Hooks 自动化

通过 Claude Code，开发者可以：
- 快速理解代码库
- 智能编写和重构代码
- 自动化测试和部署
- 多智能体并行工作
- 持续学习和优化

---

*文档由 Claude Code 根据官方信息和系统提示生成*