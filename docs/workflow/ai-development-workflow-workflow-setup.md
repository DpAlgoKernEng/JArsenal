# 全生命周期软件设计开发流程配置方案

## Context

**用户需求**：个人开发者，需要完整的全生命周期软件开发流程，支持多平台集成（CLI/IDE、消息平台、CI/CD），多语言支持（Java、TypeScript、Python），且需要同时兼顾效率、质量规范和学习进化能力。

**设计目标**：构建一套轻量但完整、高效且可扩展的专业开发流程，让一个人能像二十人团队一样高效工作。

---

## 一、推荐架构：四层叠加模型

```
┌─────────────────────────────────────────────────────────────┐
│                   学习进化层 (Learning)                       │
│   ECC continuous-learning + Hermes 闭环学习 + Memory 系统    │
├─────────────────────────────────────────────────────────────┤
│                   自主执行层 (Autonomous)                     │
│   GSD Fresh Context Agents + Ralph PRD 循环                  │
├─────────────────────────────────────────────────────────────┤
│                   工作流层 (Workflow)                         │
│   GStack Sprint + Superpowers TDD + OpenSpec 规格           │
├─────────────────────────────────────────────────────────────┤
│                   平台层 (Platform)                           │
│   Claude Code 核心 + Hermes Gateway + GitHub MCP            │
├─────────────────────────────────────────────────────────────┤
│                   语言层 (Language)                           │
│   ECC Java/TypeScript/Python Skills + Rules                 │
└─────────────────────────────────────────────────────────────┘
```

### 架构层说明

| 层级 | 核心组件 | 主要功能 |
|------|----------|----------|
| **学习进化层** | ECC learn + Memory | 模式提取、跨会话记忆、持续进化 |
| **自主执行层** | GSD + Ralph | Fresh Context 代理、PRD 自主循环、大型任务分解 |
| **工作流层** | GStack + OpenSpec + Superpowers | Sprint 管理、规格驱动、TDD 质量保障 |
| **平台层** | Claude Code + Hermes + MCP | CLI 核心、消息集成、外部工具连接 |
| **语言层** | ECC Skills + Rules | 语言专属编码规范、审查代理 |

---

## 二、核心层配置（必装）

### 2.1 Claude Code 基础配置

**位置**：`~/.claude/settings.json`

```json
{
  "model": "claude-sonnet-4-6",
  "defaultMode": "acceptEdits",
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
      "Edit(.env)",
      "Write(.env)"
    ]
  },
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": { "GITHUB_TOKEN": "${GITHUB_TOKEN}" }
    }
  }
}
```

### 2.2 OpenSpec 轻量规格框架（流体工作流）

**安装**：
```bash
npm install -g @fission-ai/openspec@latest
openspec init --tools claude
```

**核心命令**（适合个人开发者）：
```
/opsx:propose → /opsx:apply → /opsx:archive
```

**优势**：无刚性阶段门槛，随时更新任何 artifact。

### 2.3 ECC Common Rules（基础编码规范）

**安装**：
```bash
git clone https://github.com/affaan-m/everything-claude-code.git
cp -r everything-claude-code/rules/common ~/.claude/rules/
```

**包含**：
- `coding-style.md` — 不可变性、文件组织
- `git-workflow.md` — Commit 格式、PR 流程
- `testing.md` — TDD 基础要求
- `security.md` — 强制安全检查

---

## 三、自主执行层配置（大型任务）

### 3.1 GSD (Get Shit Done) 安装

**GSD** 是完整的上下文工程 + 规范驱动开发系统，支持 Fresh Context 代理循环。

```bash
# 克隆仓库
git clone git@github.com:gsd-build/get-shit-done.git ~/.claude/skills/gsd

# 安装 Skills
cp -r ~/.claude/skills/gsd/skills/* ~/.claude/skills/

# 初始化项目
cd your-project
/gsd-new-project
```

**核心 Skills 使用**（69 命令、24 代理）：

| 阶段 | 命令 | 说明 |
|------|------|------|
| **启动** | `/gsd-new-project` | 创建 .planning/ 目录、初始化 GSD 配置 |
| **讨论** | `/gsd-discuss-phase` | 产品假设讨论、用户故事生成 |
| **规划** | `/gsd-plan-phase` | 技术方案设计、任务分解 |
| **执行** | `/gsd-execute-phase` | Fresh Context 代理循环执行 |
| **验证** | `/gsd-verify-work` | Typecheck + lint + tests 验证 |
| **发布** | `/gsd-ship` | Atomic commit + PR |

**GSD 工作流程**：

```
┌─────────────────────────────────────────────────────────────────┐
│                        GSD WORKFLOW                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  /gsd-new-project  →  .planning/ 目录初始化                      │
│         ↓                                                        │
│  /gsd-discuss-phase →  用户故事、假设挑战                         │
│         ↓                                                        │
│  /gsd-plan-phase    →  research → research-findings →           │
│                       tasks → task-queue → design →              │
│                       design-review                              │
│         ↓                                                        │
│  /gsd-execute-phase →  Fresh Context 代理循环                    │
│                       ├── Agent: 研究任务                        │
│                       ├── Agent: 实现任务                        │
│                       ├── Agent: 审查任务                        │
│                       └── 循环直到所有任务完成                    │
│         ↓                                                        │
│  /gsd-verify-work   →  Typecheck + Lint + Tests                 │
│         ↓                                                        │
│  /gsd-ship         →  Atomic Git Commit + PR                    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Ralph 安装（PRD 自主循环）

**Ralph** 是简单的 PRD → 自动执行循环，适合明确的用户故事列表。

```bash
# 克隆仓库
git clone git@github.com:snarktank/ralph.git ~/.claude/skills/ralph

# 复制脚本到项目
mkdir -p scripts/ralph
cp ~/.claude/skills/ralph/ralph.sh scripts/ralph/
cp ~/.claude/skills/ralph/CLAUDE.md scripts/ralph/

chmod +x scripts/ralph/ralph.sh
```

**Ralph Skills 使用**：

| Skill | 说明 |
|-------|------|
| `/prd` | 生成 Product Requirements Document |
| `/ralph` | 将 PRD 转换为 prd.json 格式 |

**Ralph 工作流程**：

```
┌─────────────────────────────────────────────────────────────┐
│                    RALPH LOOP                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. /prd → 生成 tasks/prd-[feature].md                      │
│         ↓                                                   │
│  2. /ralph → 转换为 prd.json                                │
│         ↓                                                   │
│  3. ./ralph.sh → 自动循环执行                               │
│     ├── Fresh Context Agent 执行 US-001                     │
│     ├── Commit + Update prd.json                            │
│     ├── Fresh Context Agent 执行 US-002                     │
│     ├── 循环直到所有 passes: true                           │
│     └── 输出 <promise>COMPLETE</promise>                    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**PRD JSON 格式示例**：

```json
{
  "project": "MyApp",
  "branchName": "ralph/task-priority",
  "description": "Task Priority System",
  "userStories": [
    {
      "id": "US-001",
      "title": "Add priority field to database",
      "acceptanceCriteria": [
        "Add priority column: 'high' | 'medium' | 'low'",
        "Typecheck passes"
      ],
      "priority": 1,
      "passes": false,
      "notes": ""
    }
  ]
}
```

### 3.3 GSD vs Ralph 选择

| 场景 | 推荐 | 原因 |
|------|------|------|
| **复杂功能** | GSD | 支持研究阶段、设计评审、多代理协作 |
| **明确用户故事** | Ralph | 简单 PRD → 自动执行，无需人工干预 |
| **需要讨论假设** | GSD | `/gsd-discuss-phase` 挑战产品假设 |
| **快速迭代** | Ralph | 小任务直接执行，无需完整流程 |

---

## 四、工作流层配置（按需选择）

### 4.1 推荐组合：GSD + GStack + OpenSpec + Superpowers

**工作流程图**：

```
需求阶段          规划阶段          执行阶段          验证阶段          发布阶段          反思阶段
┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐
│OpenSpec │───►│GStack  │───►│GSD/Ralph│───►│Super-  │───►│GStack  │───►│ECC     │
│/opsx:   │    │office- │    │Fresh   │    │powers  │    │ship    │    │learn   │
│propose  │    │hours   │    │Context │    │TDD/QA  │    │        │    │        │
└────────┘    └────────┘    └────────┘    └────────┘    └────────┘    └────────┘
     │              │              │              │              │              │
     ▼              ▼              ▼              ▼              ▼              ▼
 proposal.md   design.md      代理循环        测试验证       PR/部署       模式提取
 specs/        tasks.md      prd.json       code-review    canary        skills进化
                             progress.txt
```

### 4.2 GStack 安装（效率优先）

```bash
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup
```

**核心 Skills 使用**：
- `/office-hours` — 开始任何新功能前，YC 式问题追问
- `/autoplan` — 一键完成 CEO → design → eng → DX 审查
- `/review` — 代码审查，自动修复明显问题
- `/qa` — 真实浏览器测试，发现并修复 bug
- `/ship` — 同步 main、运行测试、push、创建 PR

### 4.3 Superpowers 安装（质量规范）

```bash
/plugin marketplace add obra/superpowers-marketplace
/plugin install superpowers@claude-plugins-official
```

**核心 Skills 使用**：
- `/brainstorm` — 苏格拉底式设计细化，先理解需求再写代码
- `/write-plan` — 详细实施计划，每个任务 2-5 分钟
- `/execute-plan` — 执行计划，两阶段审查

**TDD 铁律**：
```
没有失败的测试，就没有生产代码
RED → GREEN → REFACTOR → COMMIT
```

### 4.4 工作流选择矩阵

| 场景 | 推荐路径 | 时间 | 说明 |
|------|----------|------|------|
| **快速功能** | `/opsx:propose` → `/ship` | 15-30min | OpenSpec 轻量流程 |
| **探索性需求** | `/office-hours` → `/opsx:propose` → `/qa` → `/ship` | 1-2h | GStack YC 六问 |
| **复杂重构** | `/brainstorm` → `/write-plan` → `/execute-plan` → `/review` | 2-4h | Superpowers TDD |
| **Bug 修复** | `/investigate` → TDD fix → `/qa` | 30min-1h | GStack 调试 |
| **大型功能** | `/gsd-new-project` → `/gsd-discuss` → `/gsd-plan` → `/gsd-execute` | 4-8h | GSD Fresh Context |
| **明确用户故事** | `/prd` → `/ralph` → `./ralph.sh` | 自动 | Ralph 自主循环 |
| **多任务并行** | `/gsd-execute-phase` | 自动 | GSD 多代理协作 |

---

## 五、语言层配置（多语言支持）

### 5.1 ECC 多语言安装

```bash
# 安装多语言 skills
./install.sh java typescript python

# 安装对应 rules
cp -r rules/java ~/.claude/rules/
cp -r rules/typescript ~/.claude/rules/
cp -r rules/python ~/.claude/rules/
```

### 5.2 语言专属 Skills

| 语言 | ECC Skills | 核心能力 |
|------|------------|----------|
| **Java/Spring Boot** | `springboot-patterns`, `springboot-security`, `springboot-tdd` | JPA 模式、安全配置、测试 |
| **TypeScript/Vue** | `typescript-patterns`, `frontend-patterns` | React/Vue 模式、API 设计 |
| **Python** | `python-patterns`, `django-patterns`, `django-security` | Django/FastAPI 模式 |

### 5.3 语言专属 Agents

```bash
# Java 代码审查
/java-review

# Python 代码审查
/python-review

# TypeScript 代码审查
/typescript-review
```

---

## 六、平台集成层配置

### 6.1 Hermes Gateway（消息平台）

**安装**：
```bash
curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh | bash
source ~/.bashrc
hermes gateway setup
```

**配置平台**（选择需要的）：
```bash
hermes gateway setup  # 交互式选择
# 支持: Telegram, Discord, Slack, WeChat, 飞书, 钉钉, Email, SMS
```

**配置文件**：`~/.hermes/.env`
```bash
# 添加对应平台的 Token
TELEGRAM_BOT_TOKEN=xxx
SLACK_BOT_TOKEN=xxx
DISCORD_BOT_TOKEN=xxx
```

**用途**：
- 开发进度通知（PR 创建、测试完成、部署成功）
- 移动端接收 AI 分析结果
- 定时提醒（日报、备份）

### 6.2 GitHub MCP 集成

**已在核心层配置**，用途：
- `/land-and-deploy` 自动合并 PR
- `/opsx:archive` 自动更新 specs
- Issue 自动追踪

### 6.3 CI/CD Hooks

**配置**：`~/.claude/settings.json`
```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Bash(git:push)",
        "hooks": [{ "type": "command", "command": "./hooks/post-push.sh" }]
      }
    ]
  }
}
```

**post-push.sh 示例**：
```bash
#!/bin/bash
# 发送通知到 Hermes
curl -X POST "http://localhost:8080/send" \
  -d '{"message": "Code pushed to main", "platform": "telegram"}'
```

---

## 七、学习进化层配置

### 7.1 ECC 持续学习系统

**核心命令**：
```bash
/learn          # 从当前会话提取模式
/instinct-status # 查看已学习的 instincts
/evolve         # 将 instincts 进化为 skills
/instinct-export # 导出分享
```

**配置文件**：`~/.claude/settings.json`
```json
{
  "hooks": {
    "Stop": [
      {
        "type": "command",
        "command": "./hooks/session-end-learn.sh",
        "async": true
      }
    ]
  }
}
```

### 7.2 Memory 系统

**位置**：`~/.claude/projects/<project-hash>/memory/`

**创建记忆**：
```markdown
---
name: java-testing
type: feedback
---

Spring Boot 测试必须使用 @SpringBootTest。
**Why:** 集成测试需要完整上下文。
**How to apply:** 所有涉及数据库的测试使用完整上下文加载。
```

### 7.3 Hermes 闭环学习

**配置**：`~/.hermes/memories/MEMORY.md`
```markdown
# Project Learnings

- [2026-04-14] Spring Boot JWT 认证最佳实践
- [2026-04-14] Vue 3 Composition API 模式
```

---

## 八、完整开发流程示例

### 8.1 新功能开发流程（标准）

```bash
# 1. 需求分析（OpenSpec + GStack）
/opsx:propose add-user-export
# → 生成 proposal.md, specs/, design.md, tasks.md

# 2. 产品思考（可选，复杂功能）
/office-hours
# → YC 六问，挑战假设

# 3. 技术规划审查
/autoplan
# → CEO → design → eng → DX 一键审查

# 4. 实现（Superpowers TDD）
/execute-plan
# → RED-GREEN-REFACTOR 循环

# 5. 代码审查
/java-review  # 或 /typescript-review / /python-review
# → 自动修复明显问题

# 6. QA 测试
/qa http://localhost:3000
# → 真实浏览器测试

# 7. 发布
/ship
# → Sync → Test → Push → PR

# 8. 合并部署
/land-and-deploy
# → Merge → CI → Deploy → Verify

# 9. 监控
/canary
# → Console 错误、性能回归

# 10. 归档
/opsx:archive
# → Merge delta specs → Archive

# 11. 学习进化
/learn
# → 提取模式 → 进化 skills

# 12. 反思（每周）
/retro
# → 团队 retro（单人也可用）
```

### 8.2 大型功能开发流程（GSD）

```bash
# 1. 初始化 GSD 项目
/gsd-new-project
# → 创建 .planning/ 目录结构

# 2. 产品讨论
/gsd-discuss-phase
# → 用户故事生成、假设挑战

# 3. 技术规划
/gsd-plan-phase
# → research → research-findings → tasks → design → design-review

# 4. 自动执行（Fresh Context 代理循环）
/gsd-execute-phase
# → 代理自动执行所有任务
# → 每个 Fresh Context 代理执行一个任务
# → 循环直到所有任务完成

# 5. 验证
/gsd-verify-work
# → Typecheck + lint + tests

# 6. 发布
/gsd-ship
# → Atomic commit + PR
```

### 8.3 明确用户故事流程（Ralph）

```bash
# 1. 创建 PRD
/prd
# → 回答澄清问题
# → 生成 tasks/prd-[feature].md

# 2. 转换为 prd.json
/ralph
# → 转换 markdown PRD 为 JSON 格式
# → 生成 prd.json（用户故事列表）

# 3. 运行 Ralph 循环
./scripts/ralph/ralph.sh --tool claude 10
# → 自动循环执行（最多 10 次迭代）
# → Fresh Context Agent 执行每个用户故事
# → Commit + Update prd.json + progress.txt
# → 循环直到所有 passes: true
# → 输出 <promise>COMPLETE</promise>

# 4. 查看进度
cat progress.txt
cat prd.json | jq '.userStories[] | {id, title, passes}'
```

### 8.4 Bug 修复流程

```bash
# 1. 根因调试
/investigate
# → 系统调试，追踪数据流

# 2. TDD 修复
/tdd
# → 先写失败测试 → 修复 → 确认通过

# 3. QA 验证
/qa-only  # 只报告，不改代码

# 4. 发布
/ship
```

### 8.5 探索性研究流程

```bash
# 1. 探索问题
/opsx:explore
# → 无结构探索，澄清需求

# 2. 转为具体变更
/opsx:propose optimize-performance

# 3. 后续同新功能流程
```

---

## 九、配置文件总览

### 9.1 文件位置汇总

```
~/.claude/
├── settings.json              # Claude Code 主配置
├── rules/
│   ├── common/                # ECC 基础规范（必装）
│   ├── java/                  # Java 规范
│   ├── typescript/            # TypeScript 规范
│   └── python/                # Python 规范
├── skills/
│   ├── gstack/                # GStack Skills
│   ├── gsd/                   # GSD Skills（自主执行）
│   ├── ralph/                 # Ralph Skills（PRD 循环）
│   └── openspec-*/            # OpenSpec Skills（自动生成）
├── commands/
│   ├── opsx/                  # OpenSpec 命令
│   ├── gsd/                   # GSD 命令
│   └── ecc/                   # ECC 命令（可选）
├── agents/
│   └── ecc-*/                 # ECC Agents（可选）
└── hooks/
    ├── post-push.sh           # Push 后通知
    └── session-end-learn.sh   # 会话结束学习

~/.hermes/
├── config.yaml                # Hermes 配置
├── .env                       # API 密钥和平台 Token
├── skills/                    # Hermes Skills
└── memories/                  # Hermes 记忆

项目根目录/
├── .planning/                 # GSD 规划目录
│   ├── config.json            # GSD 配置
│   ├── research.md            # 研究笔记
│   ├── tasks.md               # 任务列表
│   ├── design.md              # 设计文档
│   └── progress/              # 进度记录
├── scripts/ralph/             # Ralph 脚本目录
│   ├── ralph.sh               # Ralph 循环脚本
│   ├── CLAUDE.md              # Ralph 提示模板
│   ├── prd.json               # PRD JSON（用户故事）
│   ├── progress.txt           # 进度日志
│   └── archive/               # 归档目录
├── openspec/
│   ├── specs/                 # 系统规格
│   ├── changes/               # 变更目录
│   └── config.yaml            # 项目配置
├── CLAUDE.md                  # 项目指导文件
└── .claude/
    └── settings.local.json    # 项目级配置
```

### 9.2 环境变量汇总

```bash
# Claude Code
export ANTHROPIC_API_KEY=xxx

# Hermes 平台 Token
export TELEGRAM_BOT_TOKEN=xxx
export SLACK_BOT_TOKEN=xxx
export DISCORD_BOT_TOKEN=xxx

# GitHub MCP
export GITHUB_TOKEN=xxx

# Hermes LLM（可选切换）
export OPENROUTER_API_KEY=xxx  # 支持 200+ 模型
```

---

## 十、安装脚本（一键配置）

```bash
#!/bin/bash
# full-workflow-setup.sh

echo "=== 安装全生命周期开发流程 ==="

# 1. Claude Code 核心（已安装则跳过）
# npm install -g @anthropic/claude-code

# 2. OpenSpec
npm install -g @fission-ai/openspec@latest
cd ~/code && openspec init --tools claude

# 3. GStack
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup

# 4. GSD（自主执行层）
git clone git@github.com:gsd-build/get-shit-done.git ~/.claude/skills/gsd
cp -r ~/.claude/skills/gsd/skills/* ~/.claude/skills/

# 5. Ralph（PRD 循环）
git clone git@github.com:snarktank/ralph.git ~/.claude/skills/ralph
cp -r ~/.claude/skills/ralph/skills/* ~/.claude/skills/

# 6. ECC Rules
git clone https://github.com/affaan-m/everything-claude-code.git /tmp/ecc
cp -r /tmp/ecc/rules/common ~/.claude/rules/
cp -r /tmp/ecc/rules/java ~/.claude/rules/
cp -r /tmp/ecc/rules/typescript ~/.claude/rules/
cp -r /tmp/ecc/rules/python ~/.claude/rules/

# 7. Hermes（可选，需要消息平台）
# curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh | bash

# 8. Superpowers（可选，需要插件系统）
# claude
# /plugin marketplace add obra/superpowers-marketplace
# /plugin install superpowers@claude-plugins-official

echo "=== 安装完成 ==="
echo "下一步："
echo "1. 编辑 ~/.claude/settings.json 配置权限和 MCP"
echo "2. 在项目运行 openspec init"
echo "3. 添加 CLAUDE.md 项目指导"
echo "4. 对于大型功能，运行 /gsd-new-project"
echo "5. 对于明确用户故事，运行 /prd → /ralph → ./ralph.sh"
```
# /plugin marketplace add obra/superpowers-marketplace
# /plugin install superpowers@claude-plugins-official

echo "=== 安装完成 ==="
echo "下一步："
echo "1. 编辑 ~/.claude/settings.json 配置权限和 MCP"
echo "2. 在项目运行 openspec init"
echo "3. 添加 CLAUDE.md 项目指导"
```

---

## 十一、验证方案

### 11.1 验证安装成功

```bash
# 检查 Claude Code
claude --version

# 检查 OpenSpec
openspec --version

# 检查 GStack
ls ~/.claude/skills/gstack/

# 检查 GSD
ls ~/.claude/skills/gsd/

# 检查 Ralph
ls ~/.claude/skills/ralph/

# 检查 ECC Rules
ls ~/.claude/rules/common/

# 检查 Hermes（如安装）
hermes --version
```

### 11.2 验证工作流

```bash
# 在测试项目运行完整流程
mkdir test-project && cd test-project
openspec init

# 运行最小流程（OpenSpec）
claude
> /opsx:propose test-feature
> /review
> /ship
> /opsx:archive

# 运行 GSD 流程
> /gsd-new-project
> /gsd-discuss-phase
> /gsd-plan-phase
> /gsd-execute-phase
> /gsd-ship

# 运行 Ralph 流程
> /prd test-feature
> /ralph
> ./scripts/ralph/ralph.sh --tool claude 5
```

---

## 十二、维护和升级

### 12.1 定期升级

```bash
# Claude Code
npm update -g @anthropic/claude-code

# OpenSpec
npm update -g @fission-ai/openspec
openspec update

# GStack
/gstack-upgrade

# GSD
cd ~/.claude/skills/gsd && git pull

# Ralph
cd ~/.claude/skills/ralph && git pull

# ECC
cd /tmp/ecc && git pull
cp -r rules/* ~/.claude/rules/

# Hermes
hermes update
```

### 12.2 定期反思和学习

```bash
# 每周五运行
/retro
/learn
/evolve

# 每月清理过期 instincts
/prune
```

---

## 十三、注意事项

1. **避免过度配置** — 个人开发者应选择轻量路径，OpenSpec + GStack + ECC Rules 为最小必装
2. **自主执行层** — GSD 和 Ralph 适合大型/明确任务，小型任务可直接用 OpenSpec + GStack
3. **Fresh Context 原理** — GSD/Ralph 每次代理获得干净 200K 上下文，避免累积垃圾
4. **按需添加** — Superpowers 和 Hermes 可在需要更严格 TDD 或平台集成时再添加
5. **Memory 持久化** — 定期备份 `~/.claude/projects/*/memory/` 和 `~/.hermes/memories/`
6. **progress.txt** — Ralph 的学习日志，供未来迭代参考
7. **.planning/ 目录** — GSD 的规划文件，包含研究、任务、设计文档
8. **API 成本** — 多平台集成会增加 API 调用，监控成本使用 `/stats` 和 Hermes `/usage`
9. **任务大小** — Ralph 每个用户故事必须能在一次迭代内完成（小任务原则）
10. **依赖顺序** — GSD/Ralph 任务需按依赖顺序排列（先 schema，后 backend，再 UI）