# Everything Claude Code (ECC) 使用文档

> 创建日期：2026-04-14
> 版本：ECC 1.10.0
> 作者：Affaan Mustafa

## 一、概述

**Everything Claude Code (ECC)** 是由 Affaan Mustafa 开发的 Claude Code 插件，是一个经过实战检验的 AI 编程助手性能优化系统。该项目是 Anthropic Hackathon 获奖项目，提供生产级别的 Agents、Skills、Hooks、Rules 和 MCP 配置。

### 项目亮点

- **140K+ stars** | **21K+ forks** | **170+ contributors** | **12+ 语言生态系统**
- 支持 Claude Code、Codex、Cursor、OpenCode、Gemini 等多种 AI Agent 平台
- 48 个专业 Agents | 183 个 Skills | 79 个斜杠命令
- 跨平台支持：Windows、macOS、Linux

### 核心价值

| 模块 | 说明 |
|------|------|
| **Agents** | 专业子代理，可委托特定任务 |
| **Skills** | 工作流定义和领域知识 |
| **Commands** | 用户调用的斜杠命令入口 |
| **Hooks** | 基于触发器的自动化（会话开始/结束、工具使用前后） |
| **Rules** | 始终遵循的指导原则（安全、编码风格、测试要求） |
| **MCP Configs** | MCP 服务器配置，用于外部集成 |

---

## 二、安装配置

### 2.1 通过 Marketplace 安装

```bash
# 添加 marketplace
/plugin marketplace add https://github.com/affaan-m/everything-claude-code

# 安装插件（命名空间形式）
/plugin install ecc@ecc
```

### 2.2 手动安装（推荐）

```bash
# 克隆仓库
git clone https://github.com/affaan-m/everything-claude-code.git
cd everything-claude-code

# 安装依赖（任选其一）
npm install        # 或 pnpm install | yarn install | bun install
```

#### macOS/Linux 安装

```bash
# 推荐：完整安装
./install.sh --profile full

# 按语言安装
./install.sh typescript          # TypeScript/JavaScript
./install.sh python              # Python
./install.sh golang              # Go
./install.sh swift               # Swift
./install.sh php                 # PHP
./install.sh java                # Java
./install.sh kotlin              # Kotlin
./install.sh cpp                 # C++
./install.sh rust                # Rust

# 多语言安装
./install.sh typescript python golang

# 指定目标平台
./install.sh --target cursor typescript      # Cursor IDE
./install.sh --target gemini --profile full  # Gemini CLI
./install.sh --target antigravity typescript # Antigravity IDE
```

#### Windows PowerShell 安装

```powershell
# 完整安装
.\install.ps1 --profile full

# 按语言安装
.\install.ps1 typescript
.\install.ps1 python golang

# npm 入口（跨平台）
npx ecc-install typescript
```

### 2.3 Rules 手动安装

> ⚠️ **重要提示**：Claude Code 插件无法自动分发 Rules，需手动安装。

```bash
# Rules 目录结构
rules/
├── common/        # 语言无关原则（必装）
├── typescript/    # TypeScript/JavaScript
├── python/        # Python
├── golang/        # Go
├── swift/         # Swift
├── php/           # PHP
├── java/          # Java
├── kotlin/        # Kotlin
├── cpp/           # C++
├── rust/          # Rust
├── web/           # Web 通用
└── zh/            # 中文版本

# 安装方式：复制整个语言目录到 ~/.claude/rules/
cp -r rules/common ~/.claude/rules/
cp -r rules/typescript ~/.claude/rules/
```

### 2.4 配置文件位置

| 路径 | 说明 |
|------|------|
| `~/.claude/plugins/marketplaces/everything-claude-code/` | Marketplace 源码 |
| `~/.claude/rules/` | Rules 安装位置 |
| `~/.claude/agents/` | Agents 安装位置（可选） |
| `~/.claude/skills/` | Skills 安装位置（可选） |

### 2.5 验证安装

```bash
# 查看已安装插件
/plugin list ecc@ecc

# 启动使用
/ecc:plan "Add user authentication"
```

---

## 三、Agents 列表（48 个）

### 3.1 核心开发 Agents

| Agent | 说明 |
|-------|------|
| `planner` | 复杂功能规划和重构规划专家，自动激活 |
| `architect` | 系统架构设计决策 |
| `code-reviewer` | 代码质量和安全审查，所有代码修改后必用 |
| `tdd-guide` | TDD 专家，强制测试优先，确保 80%+ 覆盖率 |
| `build-error-resolver` | 构建错误解决专家 |
| `refactor-cleaner` | 死代码清理 |
| `e2e-runner` | Playwright E2E 测试专家 |
| `doc-updater` | 文档同步更新 |
| `docs-lookup` | 文档/API 查询助手 |
| `performance-optimizer` | 性能优化专家 |

### 3.2 语言专属 Reviewers

| Agent | 语言 |
|-------|------|
| `typescript-reviewer` | TypeScript/JavaScript |
| `python-reviewer` | Python |
| `go-reviewer` | Go |
| `java-reviewer` | Java/Spring Boot |
| `kotlin-reviewer` | Kotlin/Android/KMP |
| `rust-reviewer` | Rust |
| `cpp-reviewer` | C++ |
| `csharp-reviewer` | C#/.NET |
| `dart-build-resolver` | Dart |
| `flutter-reviewer` | Flutter |
| `php-reviewer` | PHP（隐含） |

### 3.3 语言专属 Build Resolvers

| Agent | 语言 |
|-------|------|
| `go-build-resolver` | Go 构建错误 |
| `java-build-resolver` | Java/Maven/Gradle |
| `kotlin-build-resolver` | Kotlin/Gradle |
| `rust-build-resolver` | Rust/Cargo |
| `cpp-build-resolver` | C++/CMake |
| `dart-build-resolver` | Dart |
| `pytorch-build-resolver` | PyTorch/CUDA |

### 3.4 其他专业 Agents

| Agent | 说明 |
|-------|------|
| `security-reviewer` | 安全漏洞分析 |
| `database-reviewer` | 数据库/Supabase 审查 |
| `healthcare-reviewer` | 医疗健康领域代码审查 |
| `loop-operator` | 自动循环执行器 |
| `harness-optimizer` | Harness 配置调优 |
| `chief-of-staff` | 通信分流和草稿 |
| `code-explorer` | 代码探索助手 |
| `code-simplifier` | 代码简化 |
| `comment-analyzer` | 注释分析 |
| `conversation-analyzer` | 对话分析 |
| `opensource-forker` | 开源项目 Fork |
| `opensource-packager` | 开源项目打包 |
| `opensource-sanitizer` | 开源项目清理 |
| `pr-test-analyzer` | PR 测试分析 |
| `gan-planner/generator/evaluator` | GAN 模型相关 |
| `a11y-architect` | 无障碍架构设计 |
| `code-architect` | 代码架构设计 |

### 3.5 调用 Agents

通过 Agent 工具调用：

```python
# 调用 planner
Agent(subagent_type="ecc:planner", prompt="规划用户认证功能")

# 调用 code-reviewer
Agent(subagent_type="ecc:code-reviewer", prompt="审查最近的代码变更")

# 调用 tdd-guide
Agent(subagent_type="ecc:tdd-guide", prompt="为新功能编写测试")
```

---

## 四、Skills 列表（183 个）

### 4.1 核心开发 Skills

| Skill | 说明 |
|-------|------|
| `coding-standards` | 语言最佳实践 |
| `tdd-workflow` | TDD 方法论 |
| `backend-patterns` | API、数据库、缓存模式 |
| `frontend-patterns` | React、Next.js 模式 |
| `api-design` | REST API 设计、分页、错误响应 |
| `deployment-patterns` | CI/CD、Docker、健康检查、回滚 |
| `docker-patterns` | Docker Compose、网络、卷、安全 |
| `database-migrations` | Prisma、Drizzle、Django、Go 迁移模式 |

### 4.2 语言专属 Skills

| 语言 | Skills |
|------|--------|
| **TypeScript/JS** | `typescript-patterns`, `bun-runtime`, `nextjs-turbopack` |
| **Python** | `python-patterns`, `python-testing`, `django-patterns`, `django-security`, `django-tdd` |
| **Go** | `golang-patterns`, `golang-testing` |
| **Java** | `springboot-patterns`, `springboot-security`, `springboot-tdd`, `springboot-verification`, `java-coding-standards`, `jpa-patterns` |
| **Swift** | `swift-actor-persistence`, `swift-protocol-di-testing`, `swift-concurrency-6-2`, `foundation-models-on-device`, `liquid-glass-design` |
| **PHP** | `laravel-patterns`, `laravel-security`, `laravel-tdd` |
| **C++** | `cpp-coding-standards`, `cpp-testing` |
| **Rust** | `rust-patterns`, `rust-testing` |
| **Kotlin/Android** | `android-clean-architecture`, `compose-multiplatform-patterns` |
| **Perl** | `perl-patterns`, `perl-security`, `perl-testing` |
| **Dart/Flutter** | `dart-flutter-patterns` |

### 4.3 测试 Skills

| Skill | 说明 |
|-------|------|
| `tdd-workflow` | TDD 工作流 |
| `e2e-testing` | Playwright E2E 模式、Page Object Model |
| `verification-loop` | 连续验证 |
| `eval-harness` | 验证循环评估 |
| `ai-regression-testing` | AI 回归测试 |

### 4.4 安全 Skills

| Skill | 说明 |
|-------|------|
| `security-review` | 安全审查清单 |
| `security-scan` | AgentShield 安全审计集成 |
| `defi-amm-security` | DeFi/AMM 安全 |

### 4.5 持续学习 Skills

| Skill | 说明 |
|-------|------|
| `continuous-learning` | Legacy v1 Stop-hook 模式提取 |
| `continuous-learning-v2` | 基于 Instinct 的学习，带置信度评分 |
| `iterative-retrieval` | 子代理渐进式上下文细化 |
| `strategic-compact` | 手动紧凑建议 |
| `search-first` | 研究优先编码工作流 |

### 4.6 数据库/数据 Skills

| Skill | 说明 |
|-------|------|
| `postgres-patterns` | PostgreSQL 优化模式 |
| `clickhouse-io` | ClickHouse 分析、查询、数据工程 |
| `content-hash-cache-pattern` | SHA-256 内容哈希缓存 |
| `cost-aware-llm-pipeline` | LLM 成本优化、模型路由、预算追踪 |

### 4.7 内容/业务 Skills

| Skill | 说明 |
|-------|------|
| `article-writing` | 长文写作，避免 AI 通用风格 |
| `content-engine` | 多平台社交内容和再利用 |
| `market-research` | 来源标注的市场、竞争者、投资者研究 |
| `investor-materials` | Pitch Deck、One-pager、Memo、财务模型 |
| `investor-outreach` | 个性化融资外联和跟进 |
| `brand-voice` | 品牌声音 |
| `frontend-slides` | HTML 演示文稿和 PPTX 转 Web |
| `videodb` | 视频/音频：摄取、搜索、编辑、生成、流媒体 |

### 4.8 运维 Skills

| Skill | 说明 |
|-------|------|
| `automation-audit-ops` | 自动化审计运维 |
| `customer-billing-ops` | 客户计费运维 |
| `connections-optimizer` | 社交连接优化 |
| `google-workspace-ops` | Google Workspace 运维 |
| `project-flow-ops` | 项目流运维 |

### 4.9 架构/设计 Skills

| Skill | 说明 |
|-------|------|
| `architecture-decision-records` | ADR 架构决策记录 |
| `design-system` | 设计系统 |
| `dashboard-builder` | Dashboard 构建器 |
| `blueprint` | Blueprint |
| `autonomous-loops` | 自动循环：顺序管道、PR 循环、DAG 编排 |

### 4.10 Agent/Harness Skills

| Skill | 说明 |
|-------|------|
| `agent-eval` | Agent 评估 |
| `agent-harness-construction` | Agent Harness 构建 |
| `agent-introspection-debugging` | Agent 内省调试 |
| `agent-payment-x402` | Agent 支付 X402 |
| `agent-sort` | Agent 分类 |
| `agentic-engineering` | Agent 工程 |
| `ai-first-engineering` | AI 优先工程 |
| `autonomous-agent-harness` | 自主 Agent Harness |
| `continuous-agent-loop` | 连续 Agent 循环 |
| `claude-api` | Claude API 构建 |
| `claude-devfleet` | Claude DevFleet |

### 4.11 其他 Skills

| Skill | 说明 |
|-------|------|
| `context-budget` | 上下文预算管理 |
| `benchmark` | 基准测试 |
| `code-tour` | Code Tour |
| `codebase-onboarding` | 代码库入门 |
| `configure-ecc` | ECC 交互式安装向导 |
| `crosspost` | 跨平台发布 |
| `deep-research` | 深度研究 |
| `mcp-server-patterns` | MCP 服务器模式 |
| `nutrient-document-processing` | Nutrient API 文档处理 |

---

## 五、Commands 列表（79 个）

### 5.1 核心开发命令

| 命令 | 说明 |
|------|------|
| `/tdd` | 测试驱动开发工作流 |
| `/plan` | 实施规划 |
| `/e2e` | 生成并运行 E2E 测试 |
| `/code-review` | 代码质量审查 |
| `/build-fix` | 修复构建错误 |
| `/refactor-clean` | 死代码移除 |
| `/update-docs` | 更新文档 |
| `/test-coverage` | 测试覆盖率分析 |
| `/eval` | 按标准评估 |

### 5.2 语言专属命令

| 命令 | 说明 |
|------|------|
| `/go-review` | Go 代码审查 |
| `/go-test` | Go TDD 工作流 |
| `/go-build` | Go 构建错误修复 |
| `/python-review` | Python 代码审查 |
| `/cpp-review` | C++ 代码审查 |
| `/cpp-test` | C++ 测试 |
| `/cpp-build` | C++ 构建修复 |
| `/java-review` | Java 代码审查 |
| `/kotlin-review` | Kotlin 代码审查 |
| `/kotlin-test` | Kotlin 测试 |
| `/kotlin-build` | Kotlin 构建修复 |
| `/flutter-review` | Flutter 代码审查 |
| `/flutter-test` | Flutter 测试 |
| `/flutter-build` | Flutter 构建修复 |
| `/gradle-build` | Gradle 构建 |

### 5.3 学习/进化命令

| 命令 | 说明 |
|------|------|
| `/learn` | 从会话中提取模式 |
| `/learn-eval` | 提取、评估、保存模式 |
| `/evolve` | 将 instincts 聚合为 skills |
| `/prune` | 删除过期的待处理 instincts |
| `/instinct-status` | 查看已学习的 instincts |
| `/instinct-import` | 导入 instincts |
| `/instinct-export` | 导出 instincts |
| `/skill-create` | 从 git 历史生成 skills |

### 5.4 验证/检查命令

| 命令 | 说明 |
|------|------|
| `/checkpoint` | 保存验证状态 |
| `/verify` | 运行验证循环 |
| `/harness-audit` | Harness 审计 |

### 5.5 多代理命令（需 ccg-workflow）

| 命令 | 说明 |
|------|------|
| `/pm2` | PM2 服务生命周期管理 |
| `/multi-plan` | 多代理任务分解 |
| `/multi-execute` | 协调多代理工作流 |
| `/multi-backend` | 后端多服务编排 |
| `/multi-frontend` | 前端多服务编排 |
| `/multi-workflow` | 通用多服务工作流 |
| `/orchestrate` | 多代理协调 |

### 5.6 循环/状态命令

| 命令 | 说明 |
|------|------|
| `/loop-start` | 启动自主循环 |
| `/loop-status` | 循环状态 |
| `/sessions` | 会话历史管理 |
| `/feature-dev` | 功能开发 |
| `/devfleet` | DevFleet |

### 5.7 配置命令

| 命令 | 说明 |
|------|------|
| `/setup-pm` | 配置包管理器 |
| `/hookify` | Hookify 命令 |
| `/context-budget` | 上下文预算 |

### 5.8 其他命令

| 命令 | 说明 |
|------|------|
| `/docs` | 文档相关 |
| `/aside` | 旁注 |
| `/claw` | Claw |
| `/jira` | Jira 集成 |
| `/gan-design` | GAN 设计 |
| `/gan-build` | GAN 构建 |

---

## 六、Hooks 配置

### 6.1 Hook 类型

| Hook 类型 | 触发时机 |
|-----------|----------|
| `PreToolUse` | 工具使用前 |
| `PostToolUse` | 工具使用后 |
| `Stop` | 会话结束 |
| `SessionStart` | 会话开始 |
| `PreCompact` | 紧凑前 |

### 6.2 已配置 Hooks

| Hook ID | 类型 | 说明 |
|---------|------|------|
| `pre:bash:block-no-verify` | PreToolUse | 阻止 git --no-verify 跳过钩子 |
| `pre:bash:auto-tmux-dev` | PreToolUse | 自动在 tmux 启动开发服务器 |
| `pre:bash:tmux-reminder` | PreToolUse | 提醒对长命令使用 tmux |
| `pre:bash:git-push-reminder` | PreToolUse | git push 前提醒审查变更 |
| `pre:bash:commit-quality` | PreToolUse | 提交前质量检查：lint、消息格式、console.log |
| `pre:write:doc-file-warning` | PreToolUse | 非标准文档文件警告 |
| `pre:edit-write:suggest-compact` | PreToolUse | 建议手动紧凑 |
| `pre:observe:continuous-learning` | PreToolUse | 持续学习观察（异步） |
| `pre:governance-capture` | PreToolUse | 治理捕获 |

### 6.3 Hook 运行时控制

```bash
# Hook 严格度 profile（默认：standard）
export ECC_HOOK_PROFILE=standard  # minimal | standard | strict

# 临时禁用特定 hooks
export ECC_DISABLED_HOOKS="pre:bash:tmux-reminder,post:edit:typecheck"
```

---

## 七、Rules 目录结构

### 7.1 Rules 安装

Rules 需手动安装，复制到 `~/.claude/rules/`：

```
rules/
├── common/           # 语言无关原则（必装）
│   ├── coding-style.md     # 不可变性、文件组织
│   ├── git-workflow.md     # Commit 格式、PR 流程
│   ├── testing.md          # TDD、80% 覆盖率要求
│   ├── performance.md      # 模型选择、上下文管理
│   ├── patterns.md         # 设计模式、骨架项目
│   ├── hooks.md            # Hook 架构、TodoWrite
│   ├── agents.md           #何时委托子代理
│   ├── security.md         # 强制安全检查
│
├── typescript/       # TypeScript/JavaScript
├── python/           # Python
├── golang/           # Go
├── swift/            # Swift
├── php/              # PHP
├── java/             # Java
├── kotlin/           # Kotlin
├── cpp/              # C++
├── rust/             # Rust
├── web/              # Web 通用
├── zh/               # 中文版本
```

### 7.2 安装命令

```bash
# 安装 common rules（必装）
cp -r rules/common ~/.claude/rules/

# 安装特定语言 rules
cp -r rules/typescript ~/.claude/rules/
cp -r rules/python ~/.claude/rules/
cp -r rules/golang ~/.claude/rules/

# 或使用安装脚本
./install.sh --profile full  # 自动安装所有
```

---

## 八、Dashboard GUI

### 8.1 启动 Dashboard

```bash
# 方式一：npm
npm run dashboard

# 方式二：Python
python3 ./ecc_dashboard.py
```

### 8.2 Dashboard 功能

- **Tabbed 界面**：Agents、Skills、Commands、Rules、Settings
- **主题切换**：深色/浅色模式
- **字体定制**：字体家族和大小
- **项目 Logo**：Header 和任务栏显示
- **搜索过滤**：跨所有组件搜索

---

## 九、包管理器检测

ECC 自动检测项目包管理器，优先级：

1. **环境变量**：`CLAUDE_PACKAGE_MANAGER`
2. **项目配置**：`.claude/package-manager.json`
3. **package.json**：`packageManager` 字段
4. **锁文件**：package-lock.json、yarn.lock、pnpm-lock.yaml、bun.lockb
5. **全局配置**：`~/.claude/package-manager.json`
6. **回退**：第一个可用的包管理器

### 配置包管理器

```bash
# 环境变量
export CLAUDE_PACKAGE_MANAGER=pnpm

# 全局配置
node scripts/setup-package-manager.js --global pnpm

# 项目配置
node scripts/setup-package-manager.js --project bun

# 检测当前设置
node scripts/setup-package-manager.js --detect

# 或使用命令
/setup-pm
```

---

## 十、多模型命令配置

> ⚠️ `multi-*` 命令需要额外安装 `ccg-workflow` 运行时。

### 10.1 安装 ccg-workflow

```bash
npx ccg-workflow
```

### 10.2 提供的依赖

- `~/.claude/bin/codeagent-wrapper`
- `~/.claude/.ccg/prompts/*`

---

## 十一、实战示例

### 11.1 使用 planner Agent 规划功能

```bash
# 通过斜杠命令
/plan "Add user authentication with JWT"

# 或调用 Agent
# AI 自动调用 ecc:planner
```

### 11.2 使用 code-reviewer 审查代码

```bash
# 代码修改后
/code-review

# 或自动触发
# AI 在代码修改后自动调用 ecc:code-reviewer
```

### 11.3 使用 TDD 工作流

```bash
/tdd

# 流程：
# 1. 编写失败测试
# 2. 运行确认失败
# 3. 编写最小实现
# 4. 运行确认通过
# 5. 重构
# 6. 确保覆盖率 80%+
```

### 11.4 语言专属审查

```bash
# Go 代码审查
/go-review

# Python 代码审查
/python-review

# Java 代码审查
/java-review
```

### 11.5 学习和进化

```bash
# 从会话学习模式
/learn

# 查看 learned instincts
/instinct-status

# 进化为 skill
/evolve

# 导出分享
/instinct-export
```

---

## 十二、跨平台支持

### 12.1 支持的平台

| 平台 | 状态 |
|------|------|
| Claude Code | ✅ 完整支持 |
| Cursor | ✅ 完整支持 |
| OpenCode | ✅ 完整支持 |
| Codex (CLI) | ✅ 完整支持 |
| Codex (App) | ✅ 完整支持 |
| Gemini CLI | ✅ 完整支持 |
| Antigravity IDE | ✅ 完整支持 |

### 12.2 安装命令差异

```bash
# Claude Code
./install.sh typescript

# Cursor
./install.sh --target cursor typescript

# Gemini CLI
./install.sh --target gemini --profile full

# Antigravity IDE
./install.sh --target antigravity typescript
```

---

## 十三、文件结构

### 13.1 ECC 仓库结构

```
everything-claude-code/
├── .claude-plugin/
│   ├── plugin.json           # 插件元数据
│   └── marketplace.json      # Marketplace 目录
│
├── agents/                   # 48 个专业 Agents
│   ├── planner.md
│   ├── code-reviewer.md
│   ├── tdd-guide.md
│   └── ...
│
├── skills/                   # 183 个 Skills
│   ├── coding-standards/
│   ├── tdd-workflow/
│   ├── backend-patterns/
│   ├── frontend-patterns/
│   └── ...
│
├── commands/                 # 79 个斜杠命令
│   ├── tdd.md
│   ├── plan.md
│   ├── code-review.md
│   └── ...
│
├── hooks/
│   ├── hooks.json            # Hook 配置
│   └── README.md             # Hook 文档
│
├── rules/                    # Rules 目录
│   ├── common/
│   ├── typescript/
│   ├── python/
│   └── ...
│
├── scripts/                  # Node.js 脚本
│   ├── lib/
│   │   ├── utils.js
│   │   └── package-manager.js
│   └── hooks/
│       ├── session-start.js
│       ├── session-end.js
│       └── ...
│
├── tests/                    # 测试套件
│
├── docs/                     # 文档
│
├── mcp-configs/              # MCP 配置
│
├── ecc_dashboard.py          # Dashboard GUI
│
├── install.sh                # macOS/Linux 安装脚本
├── install.ps1               # Windows PowerShell 安装脚本
│
├── README.md                 # README（英文）
├── README.zh-CN.md           # README（中文）
└── CLAUDE.md                 # Claude Code 指导文件
```

---

## 十四、指南文档

ECC 提供三份核心指南（外部链接）：

| 指南 | 内容 |
|------|------|
| **Shorthand Guide** | 设置、基础、哲学 - **先读这个** |
| **Longform Guide** | Token 优化、内存持久化、Evals、并行化 |
| **Security Guide** | 攻击向量、沙箱、清理、CVE、AgentShield |

---

## 十五、常见问题

### Q1: 如何查看已安装内容？

```bash
# 查看插件
/plugin list ecc@ecc

# 或查看本地
ls ~/.claude/rules/
ls ~/.claude/plugins/marketplaces/everything-claude-code/
```

### Q2: Rules 没生效？

```bash
# 确保已复制到正确位置
ls ~/.claude/rules/common/

# 必须复制整个目录，不能只复制文件
cp -r rules/common ~/.claude/rules/
```

### Q3: 如何只安装需要的语言？

```bash
./install.sh typescript python golang
```

### Q4: multi-* 命令不工作？

```bash
# 需要安装 ccg-workflow
npx ccg-workflow
```

### Q5: 如何禁用特定 Hook？

```bash
export ECC_DISABLED_HOOKS="pre:bash:tmux-reminder,pre:bash:git-push-reminder"
```

---

## 十六、更新

### 16.1 更新 Marketplace

```bash
/plugin update ecc@ecc
```

### 16.2 手动更新

```bash
cd everything-claude-code
git pull
./install.sh --profile full
```

---

## 十七、参考链接

| 资源 | 链接 |
|------|------|
| GitHub仓库 | https://github.com/affaan-m/everything-claude-code |
| ECC Tools 网站 | https://ecc.tools |
| GitHub Marketplace | https://github.com/marketplace/ecc-tools |
| 作者 Twitter | https://x.com/affaanmustafa |
| npm ecc-universal | https://www.npmjs.com/package/ecc-universal |
| npm ecc-agentshield | https://www.npmjs.com/package/ecc-agentshield |

---

## 十八、License

MIT License

---

## 十九、统计数据

| 指标 | 数值 |
|------|------|
| GitHub Stars | 140K+ |
| Forks | 21K+ |
| Contributors | 170+ |
| Agents | 48 |
| Skills | 183 |
| Commands | 79 |
| Languages | 12+ |
| Tests | 997+ |

---

*文档由 Claude Code 根据实际安装的插件内容生成*