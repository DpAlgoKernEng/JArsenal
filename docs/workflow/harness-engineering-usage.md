# Harness Engineering 使用文档

> 创建日期：2026-04-15
> 版本：Claude Code 2.1.92
> 来源：Anthropic 官方 + Superpowers Marketplace

## 一、概述

**Harness Engineering** 是 Claude Code 的配置和自动化工程体系，涵盖 Hooks、Skills、Agents、MCP Servers、Settings 等核心组件。通过 Harness Engineering，开发者可以定制 Claude Code 的行为、自动化工作流、集成外部工具、构建专业化智能体。

### 核心价值

| 价值 | 说明 |
|------|------|
| **自动化工作流** | Hooks 实现操作前后的自动处理 |
| **专业化能力** | Skills 提供领域特定的工作流程 |
| **智能体定制** | Agents 支持创建专用子智能体 |
| **外部集成** | MCP Servers 连接数据库、API、工具 |
| **安全可控** | Settings 提供精细权限控制 |

### 关键组件概览

```
┌─────────────────────────────────────────────────────────────────┐
│                     Claude Code Harness                          │
├─────────────┬─────────────┬─────────────┬─────────────┬─────────┤
│   Hooks     │   Skills    │   Agents    │ MCP Servers │ Settings│
│  (自动化)    │  (工作流)    │  (专业化)    │  (集成)      │  (配置)  │
├─────────────┼─────────────┼─────────────┼─────────────┼─────────┤
│ PreToolUse  │ /brainstorm │ Explore     │ GitHub      │ 权限    │
│ PostToolUse │ /simplify   │ Plan        │ Filesystem  │ Hooks   │
│ Stop        │ /loop       │ Test-Runner │ Postgres    │ MCP     │
│ SessionStart│ 自定义      │ 自定义      │ 自定义      │ Agents  │
└─────────────┴─────────────┴─────────────┴─────────────┴─────────┘
```

### 与相关文档的关系

| 文档 | 内容 | 关系 |
|------|------|------|
| [claude-code-usage.md](claude-code-usage.md) | Claude Code 整体使用指南 | Harness 是其配置工程部分 |
| [superpowers-marketplace-usage.md](superpowers-marketplace-usage.md) | Superpowers Skills 使用 | Skills 是 Harness 的组件之一 |

---

## 二、核心组件详解

### 2.1 Hooks（钩子）

**Hooks** 是在特定事件发生时自动执行的脚本，用于实现自动化检查、日志记录、权限控制等。

| Hook 类型 | 触发时机 | 用途 |
|-----------|----------|------|
| `PreToolUse` | 工具调用前 | 权限自动化、安全检查 |
| `PostToolUse` | 工具调用后 | 日志记录、触发事件 |
| `Stop` | 会话结束 | 清理、总结 |
| `SessionStart` | 会话开始 | 初始化、上下文注入 |
| `PreCompact` | 紧凑前 | 选择性保留 |

**典型应用场景**：
- 阻止危险命令（`rm -rf`、`DROP TABLE`）
- 自动批准安全操作
- 记录操作日志
- 会话开始时注入项目信息

### 2.2 Skills（技能）

**Skills** 是可复用的工作流程定义，通过 `Skill` 工具或斜杠命令调用，提供特定领域的专业能力。

| Skill 来源 | 说明 |
|------------|------|
| **内置 Skills** | Claude Code 自带（simplify、loop、claude-api） |
| **插件 Skills** | 如 Superpowers Marketplace 提供的 skills |
| **自定义 Skills** | 项目或用户自定义的 skills |

**调用方式**：
```bash
/simplify           # 斜杠命令
/brainstorm         # Superpowers skill
Skill(skill="loop", args="5m /test")  # 工具调用
```

### 2.3 Agents（智能体）

**Agents** 是专业化的子智能体，拥有独立的上下文和工具集，用于并行处理特定任务。

| Agent 类型 | 说明 | 工具权限 |
|------------|------|----------|
| `general-purpose` | 通用代理 | 全部工具 |
| `Explore` | 快速探索代码库 | 只读工具 |
| `Plan` | 架构规划 | 只读工具 |
| `Test-Runner` | 运行测试 | Read + Bash(test) |
| `自定义` | 用户定义 | 按需配置 |

**调用方式**：
```python
Agent(subagent_type="Explore", prompt="查找所有 API 端点")
Agent(subagent_type="Plan", prompt="设计认证系统方案")
```

### 2.4 MCP Servers（模型上下文协议服务器）

**MCP（Model Context Protocol）** 是连接 Claude Code 与外部工具和服务的标准协议。

| MCP 服务器 | 用途 |
|------------|------|
| `GitHub` | 仓库操作、PR/Issue 管理 |
| `Filesystem` | 文件系统操作 |
| `Postgres` | PostgreSQL 数据库 |
| `Pencil` | 设计文件（.pen）编辑 |
| `Memory` | 持久化记忆 |
| `自定义` | 自定义工具集成 |

**工具命名**：`mcp__<server>__<tool>`
- `mcp__pencil__batch_design`
- `mcp__github__create_issue`

### 2.5 Settings（配置）

**Settings** 是 Claude Code 的核心配置文件，控制模型、权限、Hooks、MCP、Agents 等所有行为。

**配置层级**：

| 层级 | 位置 | 优先级 |
|------|------|--------|
| 项目级 | `.claude/settings.json` | 最高 |
| 用户级 | `~/.claude/settings.json` | 中 |
| 企业级 | 系统策略 | 基础 |

---

## 三、安装配置

### 3.1 环境准备

**系统要求**：
- Node.js 18+ 或 Bun 1.0+
- Bash/Zsh/Fish Shell
- Anthropic 账户认证

**安装 Claude Code**：
```bash
npm install -g @anthropic/claude-code
# 或
bun install -g @anthropic/claude-code
```

### 3.2 目录结构

**项目级目录**：
```
your-project/
├── .claude/
│   ├── settings.json           # 项目配置
│   ├── CLAUDE.md               # 项目指导
│   ├── commands/               # 斜杠命令
│   │   └── my-cmd.md
│   ├── skills/                 # 自定义 Skills
│   │   └── my-skill/
│   │       └── SKILL.md
│   ├── agents/                 # 自定义 Agents
│   │   └── my-agent.md
│   ├── hooks/                  # Hook 脚本
│   │   ├── pre-tool-use.sh
│   │   └── post-tool-use.sh
│   ├── rules/                  # Rules 文件
│   └── worktrees/              # Git 工作树
└── docs/
    └── workflow/
        ├── claude-code-usage.md
        ├── superpowers-marketplace-usage.md
        └── harness-engineering-usage.md
```

**全局目录**：
```
~/.claude/
├── settings.json               # 用户配置
├── plugins/                    # 插件目录
│   └── marketplaces/
│   └── cache/
├── skills/                     # 全局 Skills
├── agents/                     # 全局 Agents
├── rules/                      # 全局 Rules
├── bin/                        # 工具脚本
└── projects/                   # 项目记忆
    └── <hash>/memory/
```

### 3.3 基础配置

**创建 settings.json**：
```json
{
  "model": "claude-sonnet-4-6",
  "permissions": {
    "allow": ["Edit(src/**)", "Bash(git:*)"],
    "deny": ["Bash(rm -rf /*)"]
  },
  "defaultMode": "acceptEdits"
}
```

**创建 CLAUDE.md**：
```bash
# 自动生成
/init

# 手动创建
# 描述项目结构、构建命令、重要模式
```

---

## 四、Hooks 工程

### 4.1 Hook 配置语法

在 `settings.json` 中配置 Hooks：

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/pre-bash.sh"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/post-edit.sh"
          }
        ]
      }
    ],
    "Stop": [
      {
        "type": "command",
        "command": "./.claude/hooks/session-end.sh"
      }
    ]
  }
}
```

**Matcher 语法**：
- `Bash` - 匹配 Bash 工具
- `Edit|Write` - 匹配 Edit 或 Write
- `.*` - 匹配所有工具

### 4.2 Hook 脚本格式

Hook 脚本接收 JSON 输入，输出 JSON 决策：

**输入结构**：
```json
{
  "tool": "Bash",
  "input": {
    "command": "rm -rf /tmp/cache"
  },
  "context": {
    "session_id": "...",
    "working_directory": "/path/to/project"
  }
}
```

**输出结构**：
```json
{
  "decision": "approve",          // 或 "deny"
  "reason": "安全操作",            // 拒绝时的原因
  "modified_input": {             // 可选：修改后的输入
    "command": "rm -rf /tmp/cache --dry-run"
  },
  "message": "已批准删除缓存"      // 可选：显示消息
}
```

### 4.3 PreToolUse Hook 示例

**安全检查 Hook**：

```bash
#!/bin/bash
# .claude/hooks/pre-bash.sh

input=$(cat)
tool=$(echo "$input" | jq -r '.tool')
command=$(echo "$input" | jq -r '.input.command')

# 检查危险命令
if [[ "$command" =~ "rm -rf /" ]] || [[ "$command" =~ "DROP TABLE" ]]; then
    echo '{"decision": "deny", "reason": "危险命令被阻止"}'
    exit 0
fi

# 检查生产环境操作
if [[ "$command" =~ "git push origin main" ]]; then
    echo '{"decision": "deny", "reason": "禁止直接推送到 main 分支"}'
    exit 0
fi

# 自动批准安全命令
if [[ "$command" =~ "git status" ]] || [[ "$ "$command" =~ "npm test" ]]; then
    echo '{"decision": "approve"}'
    exit 0
fi

# 其他需要确认
echo '{"decision": "approve"}'
```

**权限自动化 Hook**：

```bash
#!/bin/bash
# .claude/hooks/pre-edit.sh

input=$(cat)
file_path=$(echo "$input" | jq -r '.input.file_path')

# 禁止编辑敏感文件
if [[ "$file_path" == ".env" ]] || [[ "$file_path" =~ "credentials" ]]; then
    echo '{"decision": "deny", "reason": "禁止编辑敏感配置文件"}'
    exit 0
fi

# 自动批准源代码目录
if [[ "$file_path" =~ "^src/" ]]; then
    echo '{"decision": "approve"}'
    exit 0
fi

echo '{"decision": "approve"}'
```

### 4.4 PostToolUse Hook 示例

**日志记录 Hook**：

```bash
#!/bin/bash
# .claude/hooks/post-bash.sh

input=$(cat)
tool=$(echo "$input" | jq -r '.tool')
command=$(echo "$input" | jq -r '.input.command')
timestamp=$(date '+%Y-%m-%d %H:%M:%S')

# 写入操作日志
echo "[$timestamp] $tool: $command" >> .claude/logs/operations.log

echo '{"decision": "approve"}'
```

**触发 CI/CD Hook**：

```bash
#!/bin/bash
# .claude/hooks/post-edit.sh

input=$(cat)
file_path=$(echo "$input" | jq -r '.input.file_path')

# 如果修改了测试文件，自动运行测试
if [[ "$file_path" =~ "test" ]]; then
    npm test &
fi

echo '{"decision": "approve"}'
```

### 4.5 SessionStart Hook 示例

**上下文注入 Hook**：

```bash
#!/bin/bash
# .claude/hooks/session-start.sh

# 注入项目信息
cat << EOF
项目：JArsenal - Full-stack Spring Boot + Vue 3 应用
当前分支：$(git branch --show-current)
最近提交：$(git log -1 --oneline)
技术栈：Spring Boot 3.2 + MyBatis + Vue 3 + Element Plus
EOF
```

### 4.6 Hook 最佳实践

| 实践 | 说明 |
|------|------|
| **明确 matcher** | 精确匹配工具，避免过度拦截 |
| **提供拒绝原因** | 用户需要知道为什么被阻止 |
| **日志记录** | PostToolUse 记录所有关键操作 |
| **分层配置** | 项目级覆盖用户级配置 |
| **异步操作** | PostToolUse 可用后台任务 |

---

## 五、Skills 工程

### 5.1 Skill 结构

每个 Skill 由 `SKILL.md` 文件定义：

```
.claude/skills/my-skill/
├── SKILL.md            # Skill 定义文件
└── scripts/            # 辅助脚本（可选）
    └── helper.sh
```

### 5.2 SKILL.md 格式

```markdown
---
name: my-skill
description: 我的自定义技能
userInvoke: true           # 是否可通过斜杠命令调用
---

## 功能说明

这个技能用于执行特定任务...

## 使用方式

用户通过 `/my-skill` 调用...

## 执行流程

1. 第一步：xxx
2. 第二步：xxx
3. 第三步：xxx

## 注意事项

- 注意点 1
- 注意点 2
```

### 5.3 创建自定义 Skill

**示例：代码审查 Skill**

```markdown
---
name: code-review
description: 项目代码审查工作流
userInvoke: true
---

## 功能说明

执行项目代码审查，检查代码质量、安全问题和性能问题。

## 使用方式

```bash
/code-review
/code-review src/auth/    # 审查指定目录
```

## 执行流程

1. **代码质量检查**：检查命名规范、代码重复、复杂度
2. **安全审查**：检查 SQL 注入、XSS、敏感数据暴露
3. **性能审查**：检查 N+1 查询、内存泄漏风险
4. **生成报告**：输出审查结果和建议

## 输出格式

```markdown
## 代码审查报告

### 质量问题
- [文件:行号] 问题描述

### 安全问题
- [文件:行号] 问题描述

### 性能问题
- [文件:行号] 问题描述

### 建议
- 建议内容
```
```

### 5.4 Skill 调用方式

**斜杠命令调用**：
```bash
/my-skill              # 直接调用
/my-skill arg1 arg2    # 带参数调用
```

**工具调用**：
```python
Skill(skill="my-skill")
Skill(skill="my-skill", args="--verbose")
```

**Skill 间调用**：
Skill 可以在执行过程中调用其他 Skills。

### 5.5 插件 Skills

**Superpowers Marketplace Skills**：

| Skill | 说明 |
|-------|------|
| `brainstorming` | 设计阶段工作流 |
| `writing-plans` | 实施计划编写 |
| `test-driven-development` | TDD 工作流 |
| `systematic-debugging` | 系统化调试 |
| `requesting-code-review` | 代码审查请求 |

参见 [superpowers-marketplace-usage.md](superpowers-marketplace-usage.md) 了解完整列表。

### 5.6 Skills 最佳实践

| 实践 | 说明 |
|------|------|
| **单一职责** | 每个 Skill 只做一件事 |
| **明确流程** | 清晰描述执行步骤 |
| **提供示例** | 包含使用示例和输出示例 |
| **复用优先** | 优先使用现有 Skills，避免重复 |
| **文档完善** | 详细说明用途和限制 |

---

## 六、Agents 工程

### 6.1 Agent 配置格式

**配置文件定义**：

```json
{
  "agents": {
    "test-runner": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Bash(npm test)", "Bash(mvn test)"],
      "memory": {
        "enabled": true,
        "path": "./.claude/memory/test-runner"
      }
    },
    "db-analyst": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Grep", "mcp__postgres__*"]
    }
  }
}
```

**Agent 文件定义**：

创建 `.claude/agents/my-agent.md`：

```markdown
---
name: my-agent
description: 我的自定义代理
model: claude-sonnet-4-6
tools:
  - Read
  - Write
  - Bash(npm test)
memory:
  enabled: true
---

你是一个专注于特定任务的代理。

## 职责

你的主要职责是...

## 工作方式

1. 首先阅读相关代码
2. 分析问题
3. 提出解决方案
4. 执行并验证

## 注意事项

- 只做你被分配的任务
- 有问题及时反馈
- 完成后报告结果
```

### 6.2 Agent 工具限制

| 工具类别 | 说明 |
|----------|------|
| `Read` | 读取文件 |
| `Write` | 创建文件 |
| `Edit` | 编辑文件 |
| `Glob` | 文件搜索 |
| `Grep` | 内容搜索 |
| `Bash` | Shell 命令（可限制） |
| `WebFetch` | 网页获取 |
| `WebSearch` | 网络搜索 |
| `mcp__*` | MCP 工具（可限制） |

**工具限制语法**：
```json
"tools": ["Read", "Bash(npm test)", "Bash(git status)"]
// 限制 Bash 只能执行 npm test 和 git status
```

### 6.3 Agent 调用方式

**基本调用**：
```python
Agent(
    subagent_type="test-runner",
    prompt="运行所有测试并报告结果"
)
```

**后台运行**：
```python
Agent(
    subagent_type="general-purpose",
    prompt="分析依赖关系",
    run_in_background=true
)
```

**命名 Agent**：
```python
Agent(
    subagent_type="Explore",
    name="api-explorer",
    prompt="查找所有 API 端点"
)
# 后续可通过 SendMessage(to="api-explorer", message="...")
```

### 6.4 Agent Teams（团队）

**创建团队**：
```python
TeamCreate(
    team_name="dev-team",
    description="开发团队",
    agent_type="test-runner"  # Team lead 类型
)
```

**团队协作**：
```python
# 创建任务
TaskCreate(subject="实现 API", description="详细描述")
TaskCreate(subject="编写测试", description="测试描述")

# 启动团队成员
Agent(subagent_type="general-purpose", name="api-dev", team_name="dev-team")
Agent(subagent_type="test-runner", name="test-dev", team_name="dev-team")

# 分配任务
TaskUpdate(taskId="1", owner="api-dev")
TaskUpdate(taskId="2", owner="test-dev")

# 发送消息
SendMessage(to="api-dev", message="开始实现 API")

# 完成后关闭
TeamDelete()
```

### 6.5 Agent 最佳实践

| 实践 | 说明 |
|------|------|
| **工具最小化** | 只授予必需的工具 |
| **明确职责** | 在描述中清晰定义职责 |
| **隔离上下文** | 每个 Agent 独立上下文，避免干扰 |
| **结果验证** | 检查 Agent 的输出是否符合预期 |
| **错误处理** | Agent 可能失败，需要重试或人工介入 |

---

## 七、MCP 集成

### 7.1 MCP 协议说明

MCP（Model Context Protocol）是 Anthropic 开发的标准协议，用于连接 AI 模型与外部工具和服务。Claude Code 通过 MCP Servers 扩展其能力。

### 7.2 配置 MCP Servers

**settings.json 配置**：

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
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/path/to/dir"]
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "postgresql://..."
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

### 7.3 常用 MCP Servers

| 服务器 | 用途 | 安装包 |
|--------|------|--------|
| GitHub | PR/Issue/仓库操作 | `@modelcontextprotocol/server-github` |
| Filesystem | 文件系统扩展操作 | `@modelcontextprotocol/server-filesystem` |
| Postgres | PostgreSQL 数据库 | `@modelcontextprotocol/server-postgres` |
| Memory | 持久化记忆 | `@modelcontextprotocol/server-memory` |
| Slack | Slack 集成 | `@modelcontextprotocol/server-slack` |
| Puppeteer | 浏览器自动化 | `@modelcontextprotocol/server-puppeteer` |

### 7.4 MCP 工具命名

MCP 工具使用前缀命名：`mcp__<server>__<tool>`

**示例**：
- `mcp__github__create_issue` - 创建 GitHub Issue
- `mcp__github__create_pull_request` - 创建 PR
- `mcp__postgres__query` - 执行 SQL 查询
- `mcp__pencil__batch_design` - Pencil 设计操作

### 7.5 MCP 命令管理

```bash
# 查看已连接的 MCP 服务器
/mcp list

# 添加服务器
/mcp add github

# 移除服务器
/mcp remove github
```

### 7.6 MCP 最佳实践

| 实践 | 说明 |
|------|------|
| **环境变量** | Token 等敏感信息使用 env 配置 |
| **权限隔离** | 不同项目使用不同的 MCP 配置 |
| **错误处理** | MCP 工具可能失败，需要 fallback |
| **审计日志** | 记录 MCP 工具调用 |

---

## 八、Settings 配置

### 8.1 配置层级

| 层级 | 位置 | 作用域 | 优先级 |
|------|------|--------|--------|
| 项目级 | `.claude/settings.json` | 当前项目 | 最高 |
| 用户级 | `~/.claude/settings.json` | 所有项目 | 中 |
| CLI 参数 | 启动参数 | 当前会话 | 最高 |

### 8.2 权限模式

| 模式 | 文件编辑 | Shell | 网络 | 说明 |
|------|----------|-------|------|------|
| `default` | 提示 | 提示 | 提示 | 最安全 |
| `acceptEdits` | 自动批准 | 提示 | 提示 | 适合重构 |
| `auto` | 自动批准 | AI判断 | AI判断 | 智能模式 |
| `plan` | 提示 | 提示 | 提示 | 仅规划 |
| `dontAsk` | 自动批准 | 自动批准 | 自动批准 | 跳过所有 |
| `bypassPermissions` | 完全绕过 | 完全绕过 | 完全绕过 | 无限制 |

### 8.3 权限规则

**Allow 规则**：
```json
"permissions": {
  "allow": [
    "Edit(*)",                    // 编辑任意文件
    "Edit(src/**)",               // 只编辑 src 目录
    "Write(src/**)",              // 只写入 src 目录
    "Bash(git:*)",                // git 命令
    "Bash(npm:*)",                // npm 命令
    "Bash(mvn:*)",                // mvn 命令
    "WebFetch(domain:github.com)" // 只获取 github.com
  ]
}
```

**Deny 规则**：
```json
"permissions": {
  "deny": [
    "Bash(rm -rf /*)",            // 禁止危险删除
    "Bash(sudo:*)",               // 禁止 sudo
    "Edit(.env)",                 // 禁止编辑环境文件
    "Write(.env)",                // 禁止写入环境文件
    "Bash(git push --force)"      // 禁止强制推送
  ]
}
```

### 8.4 完整配置示例

```json
{
  // 模型配置
  "model": "claude-sonnet-4-6",
  "modelTemperature": 0.7,
  "maxTokens": 8192,
  "reasoningEffort": "medium",

  // 环境变量
  "env": {
    "ANTHROPIC_API_KEY": "${ANTHROPIC_API_KEY}",
    "GITHUB_TOKEN": "${GITHUB_TOKEN}"
  },

  // MCP 服务器
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "${PWD}"]
    }
  },

  // 权限配置
  "permissions": {
    "allow": [
      "Edit(src/**)",
      "Write(src/**)",
      "Write(ui/src/**)",
      "Edit(ui/src/**)",
      "Bash(git:*)",
      "Bash(npm:*)",
      "Bash(mvn:*)",
      "Bash(mysql:*)",
      "WebFetch(domain:github.com)"
    ],
    "deny": [
      "Bash(rm -rf /*)",
      "Bash(sudo:*)",
      "Edit(.env)",
      "Write(.env)",
      "Edit(.claude/settings.json)"
    ]
  },
  "defaultMode": "acceptEdits",

  // Hooks 配置
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/pre-bash.sh"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/post-edit.sh"
          }
        ]
      }
    ]
  },

  // Agent 配置
  "agents": {
    "test-runner": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Bash(mvn test)", "Bash(npm test)"]
    },
    "code-reviewer": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Glob", "Grep"]
    }
  },

  // 插件配置
  "enabledPlugins": {
    "superpowers@claude-plugins-official": true
  },

  // 行为配置
  "autoAccept": false,
  "verbose": true,
  "includeCoAuthoredBy": true,

  // 状态行配置
  "statusLine": {
    "type": "command",
    "command": "bun ~/.claude/plugins/status-line.ts"
  }
}
```

### 8.5 CLI 参数覆盖

```bash
# 启动时指定权限模式
claude --permission-mode acceptEdits

# 指定模型
claude --model claude-opus-4-6

# 加载额外配置
claude --settings /path/to/custom.json

# 只使用指定 MCP 配置
claude --mcp-config /path/to/mcp.json --strict-mcp-config
```

---

## 九、实战示例

### 9.1 工作流自动化

**目标**：实现代码修改后自动运行测试、生成报告。

**配置**：

```json
// .claude/settings.json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/auto-test.sh"
          }
        ]
      }
    ]
  }
}
```

**Hook 脚本**：

```bash
#!/bin/bash
# .claude/hooks/auto-test.sh

input=$(cat)
file_path=$(echo "$input" | jq -r '.input.file_path')

# 如果是 Java 文件，运行 mvn test
if [[ "$file_path" =~ ".java$" ]]; then
    mvn test --quiet &
fi

# 如果是 Vue 文件，运行 npm test
if [[ "$file_path" =~ ".vue$" ]] || [[ "$file_path" =~ ".ts$" ]]; then
    npm test --quiet &
fi

echo '{"decision": "approve"}'
```

### 9.2 安全加固

**目标**：阻止危险操作，记录所有关键操作。

**配置**：

```json
{
  "permissions": {
    "deny": [
      "Bash(rm -rf /)",
      "Bash(rm -rf /*)",
      "Bash(sudo:*)",
      "Bash DROP",
      "Bash(TRUNCATE)",
      "Bash(git push --force)",
      "Edit(.env)",
      "Edit(credentials)"
    ]
  },
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/security-check.sh"
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": ".*",
        "hooks": [
          {
            "type": "command",
            "command": "./.claude/hooks/log-all.sh"
          }
        ]
      }
    ]
  }
}
```

**安全检查脚本**：

```bash
#!/bin/bash
# .claude/hooks/security-check.sh

input=$(cat)
command=$(echo "$input" | jq -r '.input.command')

# 危险命令列表
dangerous_commands=(
    "rm -rf /"
    "rm -rf /*"
    "DROP TABLE"
    "TRUNCATE TABLE"
    "git push --force"
    "sudo"
)

for pattern in "${dangerous_commands[@]}"; do
    if [[ "$command" =~ "$pattern" ]]; then
        echo '{"decision": "deny", "reason": "危险操作被阻止: '$pattern'"}'
        exit 0
    fi
done

echo '{"decision": "approve"}'
```

### 9.3 团队协作配置

**目标**：为团队成员创建专业化 Agent，实现并行开发。

**配置**：

```json
{
  "agents": {
    "backend-dev": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Edit", "Write", "Bash(mvn:*)", "Bash(git:*)"],
      "memory": {
        "enabled": true,
        "path": "./.claude/memory/backend"
      }
    },
    "frontend-dev": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Edit", "Write", "Bash(npm:*)", "Bash(git:*)"],
      "memory": {
        "enabled": true,
        "path": "./.claude/memory/frontend"
      }
    },
    "test-runner": {
      "model": "claude-sonnet-4-6",
      "tools": ["Read", "Bash(mvn test)", "Bash(npm test)"]
    }
  }
}
```

**使用示例**：

```python
# 创建团队
TeamCreate(team_name="feature-team", description="功能开发团队")

# 创建任务
TaskCreate(subject="后端 API 实现", description="实现用户管理 API")
TaskCreate(subject="前端界面开发", description="开发用户管理界面")
TaskCreate(subject="测试编写", description="编写单元测试和集成测试")

# 启动 Agent
Agent(subagent_type="backend-dev", name="backend", team_name="feature-team")
Agent(subagent_type="frontend-dev", name="frontend", team_name="feature-team")
Agent(subagent_type="test-runner", name="tester", team_name="feature-team")

# 分配任务
TaskUpdate(taskId="1", owner="backend")
TaskUpdate(taskId="2", owner="frontend")
TaskUpdate(taskId="3", owner="tester")

# 协作消息
SendMessage(to="backend", message="API 设计已完成，开始实现")
SendMessage(to="frontend", message="等待 API 完成后对接")
SendMessage(to="tester", message="准备测试框架")
```

### 9.4 Superpowers 工作流集成

**目标**：集成 Superpowers Skills 实现完整开发流程。

**参见**：[superpowers-marketplace-usage.md](superpowers-marketplace-usage.md)

**完整流程**：

```
用户请求 → /brainstorm → brainstorming skill → 设计文档
    → /write-plan → writing-plans skill → 实施计划
    → /execute-plan → subagent-driven-development → 分任务执行
    → test-driven-development → TDD 实现
    → requesting-code-review → 代码审查
    → finishing-a-development-branch → 合并/PR
```

**配置示例**：

```json
{
  "enabledPlugins": {
    "superpowers@claude-plugins-official": true
  },
  "hooks": {
    "SessionStart": [
      {
        "matcher": "startup|clear|compact",
        "hooks": [
          {
            "type": "command",
            "command": "superpowers-hooks-session-start"
          }
        ]
      }
    ]
  }
}
```

---

## 十、最佳实践

### 10.1 安全实践

| 实践 | 说明 |
|------|------|
| **禁止危险命令** | Deny 规则阻止 `rm -rf`、`DROP`、`sudo` |
| **保护敏感文件** | 禁止编辑 `.env`、`credentials`、`keys` |
| **Hooks 检查** | PreToolUse Hook 实现安全检查 |
| **审计日志** | PostToolUse 记录所有关键操作 |
| **权限最小化** | 只授予必需的工具和操作 |

### 10.2 性能实践

| 实践 | 说明 |
|------|------|
| **使用 compact** | 定期压缩上下文 `/compact` |
| **并行 Agent** | 多任务使用并行 Agent |
| **后台运行** | 长任务使用 `run_in_background` |
| **精准搜索** | 使用 Glob/Grep 而非 Bash |
| **限制读取** | Read 文件时限制行数 |

### 10.3 可维护性实践

| 实践 | 说明 |
|------|------|
| **配置分离** | 项目级配置覆盖用户级 |
| **Hook 脚本独立** | Hook 脚本单独存放，便于维护 |
| **Skill 文档化** | 每个 Skill 有完整使用说明 |
| **Agent 职责明确** | 每个 Agent 只做一类任务 |
| **版本控制** | `.claude/` 目录纳入 git（排除敏感信息） |

### 10.4 团队协作实践

| 实践 | 说明 |
|------|------|
| **共享配置** | 项目级 settings.json 团队共享 |
| **统一 Hooks** | 团队统一安全和工作流 Hooks |
| **专业化 Agent** | 每个成员/任务使用专门 Agent |
| **任务追踪** | 使用 TaskCreate/TaskList 追踪进度 |
| **消息通知** | 使用 SendMessage 协作沟通 |

---

## 十一、常见问题

### Q1: Hooks 为什么没有生效？

**检查清单**：
1. `settings.json` 是否正确配置 hooks
2. Hook 脚本路径是否正确
3. Hook 脚本是否有执行权限（`chmod +x`）
4. Matcher 是否匹配正确的工具

### Q2: 如何调试 Hook？

```bash
# 使用 --debug 模式
claude --debug hooks

# 查看 Hook 输出
claude --debug-file /tmp/claude-debug.log
```

### Q3: Skills 和 Agents 有什么区别？

| 特性 | Skills | Agents |
|------|--------|--------|
| 执行方式 | 同步执行 | 可并行/后台 |
| 上下文 | 共享主会话 | 独立上下文 |
| 工具权限 | 继承主会话 | 可限制 |
| 适用场景 | 工作流程 | 专业任务 |

### Q4: MCP Server 连接失败怎么办？

**常见原因**：
1. 环境变量未设置（如 `GITHUB_TOKEN`）
2. MCP 包未安装或版本不兼容
3. 权限不足

**解决方法**：
```bash
# 检查 MCP 状态
/mcp list

# 查看 debug 日志
claude --debug mcp
```

### Q5: 如何共享团队配置？

```bash
# 项目级配置纳入 git
git add .claude/settings.json
git add .claude/hooks/
git add .claude/skills/
git add .claude/agents/

# 注意：不要提交敏感信息
# 使用环境变量代替硬编码 token
```

### Q6: 如何创建项目特定的 Skill？

```bash
# 创建目录
mkdir -p .claude/skills/my-skill

# 创建 SKILL.md
cat > .claude/skills/my-skill/SKILL.md << 'EOF'
---
name: my-skill
description: 项目特定技能
userInvoke: true
---

## 功能说明
...
EOF
```

### Q7: 如何限制 Agent 的文件访问？

```json
{
  "agents": {
    "readonly-agent": {
      "tools": ["Read", "Glob", "Grep"]
      // 不包含 Edit/Write
    }
  }
}
```

### Q8: Hooks 能修改工具输入吗？

是的，Hook 可以返回 `modified_input`：

```bash
#!/bin/bash
input=$(cat)
command=$(echo "$input" | jq -r '.input.command')

# 添加 --dry-run 参数
if [[ "$command" =~ "rm" ]]; then
    echo '{"decision": "approve", "modified_input": {"command": "'$command' --dry-run"}}'
fi
```

### Q9: 如何查看当前配置？

```bash
# 在 Claude Code 中
/status
/permissions
/mcp list
/hooks
```

### Q10: 如何临时禁用某个 Hook？

```bash
# 使用 --bare 模式跳过所有 Hooks
claude --bare

# 或修改 settings.json 临时移除
```

---

## 十二、参考资源

### 官方文档

| 资源 | 链接 |
|------|------|
| Settings 文档 | https://code.claude.com/docs/en/settings |
| Permissions 文档 | https://code.claude.com/docs/en/permissions |
| Hooks 文档 | https://code.claude.com/docs/en/hooks |
| MCP 协议 | https://modelcontextprotocol.io |
| Skills 文档 | https://code.claude.com/docs/en/skills |

### 项目相关文档

| 文档 | 说明 |
|------|------|
| [claude-code-usage.md](claude-code-usage.md) | Claude Code 整体使用指南 |
| [superpowers-marketplace-usage.md](superpowers-marketplace-usage.md) | Superpowers Skills 使用 |

### 社区资源

| 资源 | 链接 |
|------|------|
| Superpowers Marketplace | https://github.com/obra/superpowers-marketplace |
| MCP Servers List | https://github.com/modelcontextprotocol/servers |
| Claude Code Settings | https://github.com/feiskyer/claude-code-settings |
| Visual Settings Config | https://claude-settings.nl/ |

---

## 十三、总结

Harness Engineering 是 Claude Code 的配置和自动化工程体系，涵盖五大核心组件：

| 组件 | 作用 | 核心能力 |
|------|------|----------|
| **Hooks** | 自动化 | PreToolUse/PostToolUse 事件处理 |
| **Skills** | 工作流 | 领域专业流程自动化 |
| **Agents** | 专业化 | 独立上下文的子智能体 |
| **MCP Servers** | 集成 | 外部工具和服务连接 |
| **Settings** | 配置 | 权限、模型、行为控制 |

通过 Harness Engineering，团队可以：
- 自动化安全检查和工作流
- 构建专业化开发智能体
- 集成数据库、GitHub 等外部服务
- 实现精细化权限控制
- 提升开发效率和代码质量

---

*文档由 Claude Code 根据官方文档和 Superpowers Marketplace 内容生成*