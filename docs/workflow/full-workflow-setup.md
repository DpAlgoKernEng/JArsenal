# 全生命周期软件设计开发流程配置方案

## Context

**用户需求**：个人开发者，需要完整的全生命周期软件开发流程，支持多平台集成（CLI/IDE、消息平台、CI/CD），多语言支持（Java、TypeScript、Python），且需要同时兼顾效率、质量规范和学习进化能力。

**设计目标**：构建一套轻量但完整、高效且可扩展的专业开发流程，让一个人能像二十人团队一样高效工作。

---

## 一、推荐架构：三层叠加模型

```
┌─────────────────────────────────────────────────────────────┐
│                   学习进化层 (Learning)                       │
│   ECC continuous-learning + Hermes 闭环学习 + Memory 系统    │
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

## 三、工作流层配置（按需选择）

### 3.1 推荐组合：GStack + OpenSpec + Superpowers

**工作流程图**：

```
需求阶段          规划阶段          实现阶段          验证阶段          发布阶段          反思阶段
┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐    ┌────────┐
│OpenSpec │───►│GStack  │───►│Claude  │───►│Super-  │───►│GStack  │───►│ECC     │
│/opsx:   │    │office- │    │Code    │    │powers  │    │ship    │    │learn   │
│propose  │    │hours   │    │实现    │    │TDD/QA  │    │        │    │        │
└────────┘    └────────┘    └────────┘    └────────┘    └────────┘    └────────┘
     │              │              │              │              │              │
     ▼              ▼              ▼              ▼              ▼              ▼
 proposal.md   design.md      代码实现       测试验证       PR/部署       模式提取
 specs/        tasks.md                      code-review    canary        skills进化
```

### 3.2 GStack 安装（效率优先）

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

### 3.3 Superpowers 安装（质量规范）

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

### 3.4 工作流选择矩阵

| 场景 | 推荐路径 | 时间 |
|------|----------|------|
| **快速功能** | `/opsx:propose` → `/ship` | 15-30min |
| **探索性需求** | `/office-hours` → `/opsx:propose` → `/qa` → `/ship` | 1-2h |
| **复杂重构** | `/brainstorm` → `/write-plan` → `/execute-plan` → `/review` | 2-4h |
| **Bug 修复** | `/investigate` (GStack) → TDD fix → `/qa` | 30min-1h |

---

## 四、语言层配置（多语言支持）

### 4.1 ECC 多语言安装

```bash
# 安装多语言 skills
./install.sh java typescript python

# 安装对应 rules
cp -r rules/java ~/.claude/rules/
cp -r rules/typescript ~/.claude/rules/
cp -r rules/python ~/.claude/rules/
```

### 4.2 语言专属 Skills

| 语言 | ECC Skills | 核心能力 |
|------|------------|----------|
| **Java/Spring Boot** | `springboot-patterns`, `springboot-security`, `springboot-tdd` | JPA 模式、安全配置、测试 |
| **TypeScript/Vue** | `typescript-patterns`, `frontend-patterns` | React/Vue 模式、API 设计 |
| **Python** | `python-patterns`, `django-patterns`, `django-security` | Django/FastAPI 模式 |

### 4.3 语言专属 Agents

```bash
# Java 代码审查
/java-review

# Python 代码审查
/python-review

# TypeScript 代码审查
/typescript-review
```

---

## 五、平台集成层配置

### 5.1 Hermes Gateway（消息平台）

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

### 5.2 GitHub MCP 集成

**已在核心层配置**，用途：
- `/land-and-deploy` 自动合并 PR
- `/opsx:archive` 自动更新 specs
- Issue 自动追踪

### 5.3 CI/CD Hooks

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

## 六、学习进化层配置

### 6.1 ECC 持续学习系统

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

### 6.2 Memory 系统

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

### 6.3 Hermes 闭环学习

**配置**：`~/.hermes/memories/MEMORY.md`
```markdown
# Project Learnings

- [2026-04-14] Spring Boot JWT 认证最佳实践
- [2026-04-14] Vue 3 Composition API 模式
```

---

## 七、完整开发流程示例

### 7.1 新功能开发流程

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

### 7.2 Bug 修复流程

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

### 7.3 探索性研究流程

```bash
# 1. 探索问题
/opsx:explore
# → 无结构探索，澄清需求

# 2. 转为具体变更
/opsx:propose optimize-performance

# 3. 后续同新功能流程
```

---

## 八、配置文件总览

### 8.1 文件位置汇总

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
│   └── openspec-*/            # OpenSpec Skills（自动生成）
├── commands/
│   ├── opsx/                  # OpenSpec 命令
│   └── ecc/                   # ECC 命令（可选）
├── agents/
│   └ ecc-*/                   # ECC Agents（可选）
└── hooks/
    ├── post-push.sh           # Push 后通知
    └── session-end-learn.sh   # 会话结束学习

~/.hermes/
├── config.yaml                # Hermes 配置
├── .env                       # API 密钥和平台 Token
├── skills/                    # Hermes Skills
└── memories/                  # Hermes 记忆

项目根目录/
├── openspec/
│   ├── specs/                 # 系统规格
│   ├── changes/               # 变更目录
│   └── config.yaml            # 项目配置
├── CLAUDE.md                  # 项目指导文件
└── .claude/
    └── settings.local.json    # 项目级配置
```

### 8.2 环境变量汇总

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

## 九、安装脚本（一键配置）

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

# 4. ECC Rules
git clone https://github.com/affaan-m/everything-claude-code.git /tmp/ecc
cp -r /tmp/ecc/rules/common ~/.claude/rules/
cp -r /tmp/ecc/rules/java ~/.claude/rules/
cp -r /tmp/ecc/rules/typescript ~/.claude/rules/
cp -r /tmp/ecc/rules/python ~/.claude/rules/

# 5. Hermes（可选，需要消息平台）
# curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh | bash

# 6. Superpowers（可选，需要插件系统）
# claude
# /plugin marketplace add obra/superpowers-marketplace
# /plugin install superpowers@claude-plugins-official

echo "=== 安装完成 ==="
echo "下一步："
echo "1. 编辑 ~/.claude/settings.json 配置权限和 MCP"
echo "2. 在项目运行 openspec init"
echo "3. 添加 CLAUDE.md 项目指导"
```

---

## 十、验证方案

### 10.1 验证安装成功

```bash
# 检查 Claude Code
claude --version

# 检查 OpenSpec
openspec --version

# 检查 GStack
ls ~/.claude/skills/gstack/

# 检查 ECC Rules
ls ~/.claude/rules/common/

# 检查 Hermes（如安装）
hermes --version
```

### 10.2 验证工作流

```bash
# 在测试项目运行完整流程
mkdir test-project && cd test-project
openspec init

# 运行最小流程
claude
> /opsx:propose test-feature
> /review
> /ship
> /opsx:archive
```

---

## 十一、维护和升级

### 11.1 定期升级

```bash
# Claude Code
npm update -g @anthropic/claude-code

# OpenSpec
npm update -g @fission-ai/openspec
openspec update

# GStack
/gstack-upgrade

# ECC
cd /tmp/ecc && git pull
cp -r rules/* ~/.claude/rules/

# Hermes
hermes update
```

### 11.2 定期反思和学习

```bash
# 每周五运行
/retro
/learn
/evolve

# 每月清理过期 instincts
/prune
```

---

## 十二、注意事项

1. **避免过度配置** — 个人开发者应选择轻量路径，OpenSpec + GStack + ECC Rules 为最小必装
2. **按需添加** — Superpowers 和 Hermes 可在需要更严格 TDD 或平台集成时再添加
3. **Memory 持久化** — 定期备份 `~/.claude/projects/*/memory/` 和 `~/.hermes/memories/`
4. **API 成本** — 多平台集成会增加 API 调用，监控成本使用 `/stats` 和 Hermes `/usage`