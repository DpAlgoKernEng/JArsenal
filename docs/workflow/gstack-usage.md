# GStack 使用文档

> 创建日期：2026-04-14
> 版本：GStack 1.1.0
> 作者：Garry Tan (Y Combinator CEO)

## 一、概述

**GStack** 是由 Y Combinator CEO Garry Tan 开发的 Claude Code Skills 集合，将 Claude Code 转化为一个虚拟工程团队。该项目代表了 AI 辅助编程的最新范式：一个人可以像二十人的团队一样高效地开发软件。

### 作者背景

Garry Tan 是 Y Combinator 的 President & CEO，曾投资 Coinbase、Instacart、Rippling 等成功创业公司。在 YC 之前，他是 Palantir 最早的工程师/PM/设计师之一，创立了 Posterous（被 Twitter 收购），并构建了 YC 的内部社交网络 Bookface。

### 项目亮点

- **60 天内：600,000+ 行生产代码**（35% 测试）
- **每日 10,000-20,000 行代码**，兼职开发，同时全职运营 YC
- **一周 retro：140,751 行新增，362 commits，~115k net LOC**
- **23 个专业角色 + 8 个强力工具**
- MIT License，完全免费

### 核心理念

| 原则 | 说明 |
|------|------|
| **Boil the Lake** | 完整性成本接近零，永远做完整的事情 |
| **Search Before Building** | 先搜索已有方案，再构建正确的完整版本 |
| **User Sovereignty** | AI 推荐，用户决策，用户始终拥有最终决定权 |

---

## 二、安装配置

### 2.1 系统要求

| 要求 | 说明 |
|------|------|
| Claude Code | Anthropic Claude Code CLI |
| Git | 版本控制系统 |
| Bun | v1.0+（运行时） |
| Node.js | 仅 Windows 需要 |

### 2.2 快速安装（30秒）

在 Claude Code 中粘贴以下命令：

```bash
# 全局安装
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack && cd ~/.claude/skills/gstack && ./setup
```

然后在项目的 `CLAUDE.md` 中添加 gstack 配置部分。

### 2.3 团队模式安装（推荐）

自动更新，团队成员无需手动升级：

```bash
# 1. 每个开发者全局安装
cd ~/.claude/skills/gstack && ./setup --team

# 2. 在项目根目录初始化团队模式
cd <your-repo>
~/.claude/skills/gstack/bin/gstack-team-init required  # 或 optional

# 3. 提交配置
git add .claude/ CLAUDE.md && git commit -m "require gstack for AI-assisted work"
```

**模式说明**：

| 模式 | 说明 |
|------|------|
| `required` | 强制要求安装，未安装时阻止工作 |
| `optional` | 温和建议安装，一次性提醒 |

### 2.4 完整克隆（贡献者）

如需完整 git 历史：

```bash
git clone https://github.com/garrytan/gstack.git ~/.claude/skills/gstack
cd ~/.claude/skills/gstack && ./setup
```

### 2.5 其他 AI Agent 支持

GStack 支持 8 种 AI 编程 Agent：

| Agent | 安装路径 | Flag |
|-------|----------|------|
| OpenAI Codex CLI | `~/.codex/skills/gstack-*/` | `--host codex` |
| OpenCode | `~/.config/opencode/skills/gstack-*/` | `--host opencode` |
| Cursor | `~/.cursor/skills/gstack-*/` | `--host cursor` |
| Factory Droid | `~/.factory/skills/gstack-*/` | `--host factory` |
| Slate | `~/.slate/skills/gstack-*/` | `--host slate` |
| Kiro | `~/.kiro/skills/gstack-*/` | `--host kiro` |

```bash
# 多 Agent 安装
git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/gstack
cd ~/gstack && ./setup --host codex  # 或其他 agent
```

### 2.6 OpenClaw 集成

OpenClaw 通过 ACP 启动 Claude Code sessions，自动使用 GStack skills：

```
# 在 OpenClaw agent 中粘贴：
Install gstack: run `git clone --single-branch --depth 1 https://github.com/garrytan/gstack.git ~/.claude/skills/gstack && cd ~/.claude/skills/gstack && ./setup`

# 然后在 AGENTS.md 添加 Coding Tasks section
```

### 2.7 安装位置

| 路径 | 说明 |
|------|------|
| `~/.claude/skills/gstack/` | Claude Code skills 位置 |
| `~/.gstack/` | 全局状态、配置、分析 |
| `.claude/CLAUDE.md` | 项目级配置 |

---

## 三、Sprint 工作流

GStack 是一个流程，不是工具集合。Skills 按照 Sprint 顺序运行：

```
Think → Plan → Build → Review → Test → Ship → Reflect
```

每个 Skill 的输出自动传递给下一个 Skill。

### 3.1 工作流程图

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ office-hours│───►│ plan-ceo    │───►│ plan-eng    │
│  (思考)     │    │  review     │    │  review     │
└─────────────┘    └─────────────┘    └─────────────┘
                          │                  │
                          ▼                  ▼
                   ┌─────────────┐    ┌─────────────┐
                   │plan-design  │    │ plan-devex  │
                   │  review     │    │  review     │
                   └─────────────┘    └─────────────┘
                          │                  │
                          ▼                  ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   ship      │◄───│   review    │◄───│ implement   │
│  (发布)     │    │  (审查)     │    │  (构建)     │
└─────────────┘    └─────────────┘    └─────────────┘
       │                  │
       ▼                  ▼
┌─────────────┐    ┌─────────────┐
│land-and-    │    │     qa      │
│  deploy     │    │  (测试)     │
└─────────────┘    └─────────────┘
       │                  │
       ▼                  ▼
┌─────────────┐    ┌─────────────┐
│   canary    │    │    retro    │
│  (监控)     │    │  (反思)     │
└─────────────┘    └─────────────┘
```

---

## 四、Skills 列表（23 个专业角色）

### 4.1 Think 阶段（产品思考）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/office-hours` | **YC Office Hours** | 六个强制性问题重新定义产品，挑战假设，生成实施方案，输出设计文档 |

### 4.2 Plan 阶段（规划审查）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/plan-ceo-review` | **CEO / Founder** | 重新思考问题，四种模式：Expansion、Selective Expansion、Hold Scope、Reduction |
| `/plan-eng-review` | **Eng Manager** | 锁定架构、数据流、ASCII 图、边缘情况、测试矩阵 |
| `/plan-design-review` | **Senior Designer** | 每个设计维度评分 0-10，AI Slop 检测，交互式审查 |
| `/plan-devex-review` | **Developer Experience Lead** | DX 审查：开发者画像、TTHW 基准、魔法时刻、摩擦点追踪 |
| `/autoplan` | **Review Pipeline** | 一命令完成所有审查：CEO → design → eng → DX，自动检测适用项 |

### 4.3 Design 阶段（设计）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/design-consultation` | **Design Partner** | 从零构建完整设计系统，研究现有方案，提出创意风险，生成 mockups |
| `/design-shotgun` | **Design Explorer** | "Show me options" — 生成 4-6 个 AI mockup 变体，浏览器对比板，口味记忆学习 |
| `/design-html` | **Design Engineer** | 将 mockup 转为生产 HTML/CSS，Pretext 计算布局，自动检测框架 |

### 4.4 Review 阶段（审查）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/review` | **Staff Engineer** | 找出通过 CI 但在生产中爆炸的 bug，自动修复明显问题 |
| `/investigate` | **Debugger** | 系统根因调试，铁律：无调查不修复，追踪数据流，测试假设 |
| `/design-review` | **Designer Who Codes** | 与 /plan-design-review 相同审查，然后修复发现的问题 |
| `/devex-review` | **DX Tester** | 实际测试开发者体验：导航文档、尝试入门流程、计时 TTHW |
| `/cso` | **Chief Security Officer** | OWASP Top 10 + STRIDE 威胁模型，零噪音输出，每个发现包含具体攻击场景 |

### 4.5 Test 阶段（测试）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/qa` | **QA Lead** | 测试应用，发现 bug，原子 commit 修复，重新验证，自动生成回归测试 |
| `/qa-only` | **QA Reporter** | 与 /qa 相同方法论，仅报告，不做代码修改 |
| `/benchmark` | **Performance Engineer** | 页面加载时间、Core Web Vitals、资源大小基准，PR 前后对比 |

### 4.6 Ship 阶段（发布）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/ship` | **Release Engineer** | 同步 main、运行测试、审计覆盖率、push、创建 PR，无测试框架时自动初始化 |
| `/land-and-deploy` | **Release Engineer** | 合并 PR、等待 CI 和部署、验证生产健康 |
| `/document-release` | **Technical Writer** | 更新所有项目文档匹配已发布内容，自动检测过时 README |

### 4.7 Reflect 阶段（反思）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/retro` | **Eng Manager** | 团队周 retro：每人 breakdown、发布 streaks、测试健康趋势 |
| `/learn` | **Memory** | 管理跨会话学习：审查、搜索、修剪、导出项目特定模式 |

### 4.8 Post-Deploy 阶段（监控）

| Skill | 角色 | 功能 |
|-------|------|------|
| `/canary` | **SRE** | 发布后监控循环：console 错误、性能回归、页面失败 |

### 4.9 Power Tools（强力工具）

| Skill | 功能 |
|-------|------|
| `/codex` | **Second Opinion** — OpenAI Codex CLI 独立代码审查，三种模式：review、adversarial、consultation |
| `/careful` | **Safety Guardrails** — 警告危险命令（rm -rf、DROP TABLE、force-push） |
| `/freeze` | **Edit Lock** — 限制文件编辑到一个目录，防止调试时意外修改 |
| `/guard` | **Full Safety** — /careful + /freeze 组合 |
| `/unfreeze` | **Unlock** — 解除 /freeze 边界 |
| `/browse` | **QA Engineer** — 真实 Chromium 浏览器，真实点击，真实截图，~100ms/命令 |
| `/open-gstack-browser` | **GStack Browser** — 启动带侧边栏的 AI 浏览器，anti-bot stealth，自动模型路由 |
| `/setup-browser-cookies` | **Session Manager** — 从真实浏览器导入 cookies 到无头会话 |
| `/setup-deploy` | **Deploy Configurator** — /land-and-deploy 一次性配置 |
| `/gstack-upgrade` | **Self-Updater** — 升级 gstack 到最新版本 |
| `/pair-agent` | **Multi-Agent Coordinator** — 与其他 AI agent 共享浏览器，跨代理协调 |

---

## 五、审查选择指南

| 构建目标 | Plan 阶段（代码前） | Live 审查（发布后） |
|----------|---------------------|---------------------|
| **End users**（UI、Web、Mobile） | `/plan-design-review` | `/design-review` |
| **Developers**（API、CLI、SDK、Docs） | `/plan-devex-review` | `/devex-review` |
| **Architecture**（Data Flow、Perf、Tests） | `/plan-eng-review` | `/review` |
| **所有方面** | `/autoplan`（自动运行 CEO → design → eng → DX） | — |

---

## 六、详细 Skill 说明

### 6.1 `/office-hours` — YC Office Hours

**触发场景**：开始构建任何新功能之前

**流程**：
1. 六个强制性问题重新定义产品
2. 挑战你的假设和框架
3. 生成 3 种实施方案带工作量估算
4. 输出设计文档，自动传递给下游 skills

**示例对话**：
```
You: I want to build a daily briefing app for my calendar.
Claude: [asks about the pain — specific examples, not hypotheticals]
You: Multiple Google calendars, events with stale info, wrong locations...
Claude: I'm going to push back on the framing. You said "daily briefing app."
       But what you actually described is a personal chief of staff AI.
       [extracts 5 capabilities you didn't realize you were describing]
       [challenges 4 premises — you agree, disagree, or adjust]
       [generates 3 implementation approaches with effort estimates]
       RECOMMENDATION: Ship the narrowest wedge tomorrow.
```

### 6.2 `/plan-ceo-review` — CEO Review

**四种模式**：

| 模式 | 说明 |
|------|------|
| Expansion | 扩展范围，发现隐藏的 10-star 产品 |
| Selective Expansion | 选择性扩展部分维度 |
| Hold Scope | 保持现有范围 |
| Reduction | 缩减范围，聚焦核心 |

### 6.3 `/plan-eng-review` — Engineering Review

**输出内容**：
- ASCII 数据流图
- 状态机图
- 错误路径分析
- 边缘情况列表
- 测试矩阵
- 失败模式
- 安全关注点

### 6.4 `/plan-design-review` — Design Review

**评分维度**（0-10）：
- 视觉层级
- 交互反馈
- 响应式设计
- 可访问性
- 一致性
- 等等...

**AI Slop 检测**：识别 AI 生成的通用、无味设计

### 6.5 `/qa` — QA Testing

**流程**：
1. 打开真实浏览器
2. 点击测试用户流程
3. 发现 bug → 原子 commit 修复
4. 重新验证修复
5. 自动生成回归测试

**关键能力**：Agent 有眼睛了

### 6.6 `/review` — Code Review

**审查内容**：
- CI 通过但生产会爆炸的 bug
- 完整性缺口
- 自动修复明显问题
- 标记需要用户确认的问题

### 6.7 `/ship` — Release Engineer

**流程**：
1. Sync main
2. Run tests（无框架时自动初始化）
3. Audit coverage
4. Push
5. Open PR
6. 自动调用 `/document-release`

### 6.8 `/cso` — Security Audit

**内容**：
- OWASP Top 10
- STRIDE 威胁模型
- 17 个误报排除规则
- 8/10+ 置信度门槛
- 独立发现验证
- 每个发现包含具体攻击场景

### 6.9 `/browse` — Browser Control

**能力**：
- 真实 Chromium 浏览器
- 真实点击和交互
- 截图和注解
- 表单填写
- 文件上传
- 对话框处理
- ~100ms/命令

### 6.10 `/design-shotgun` — Visual Exploration

**流程**：
1. 描述你想要的
2. 生成 4-6 个 GPT Image mockup 变体
3. 浏览器对比板展示所有变体
4. 选择喜欢的，提供反馈
5. 生成新一轮
6. 口味记忆学习你的偏好
7. 重复直到满意
8. 传递给 `/design-html`

### 6.11 `/design-html` — Production HTML

**特点**：
- Pretext 计算布局：文本自动重排，高度自动调整
- 30KB overhead，零依赖
- 自动检测框架（React、Svelte、Vue）
- 智能路由：landing page vs dashboard vs form vs card

### 6.12 `/retro` — Retrospective

**输出**：
- 每人 breakdown
- 发布 streaks
- 测试健康趋势
- 成长机会

**`/retro global`**：跨所有项目和 AI 工具运行（Claude Code、Codex、Gemini）

### 6.13 `/pair-agent` — Multi-Agent Coordination

**功能**：
- 与其他 AI agent 共享浏览器
- 每个 agent 独立 tab
- scoped tokens
- tab 隔离
- rate limiting
- domain 限制
- activity attribution
- 自动启动 ngrok tunnel（远程 agent）

---

## 七、Voice Input 触发短语

GStack skills 支持语音友好触发：

| 语音命令 | 触发 Skill |
|----------|------------|
| "run a security check" | `/cso` |
| "test the website" | `/qa` |
| "do an engineering review" | `/plan-eng-review` |
| "review my code" | `/review` |
| "ship this feature" | `/ship` |
| "plan this feature" | `/autoplan` |

---

## 八、压缩比率表

| 任务类型 | 人工团队 | AI 辅助 | 压缩比 |
|----------|----------|---------|--------|
| Boilerplate / scaffolding | 2 days | 15 min | ~100x |
| Test writing | 1 day | 15 min | ~50x |
| Feature implementation | 1 week | 30 min | ~30x |
| Bug fix + regression test | 4 hours | 15 min | ~20x |
| Architecture / design | 2 days | 4 hours | ~5x |
| Research / exploration | 1 day | 3 hours | ~3x |

---

## 九、并行 Sprint

GStack 支持与 Conductor 配合运行 10-15 个并行 Sprint：

```
Session 1: /office-hours on new idea
Session 2: /review on PR
Session 3: implementing feature
Session 4: /qa on staging
Sessions 5-15: other branches
```

**关键**：流程结构化，每个 agent 知道做什么和何时停止。

---

## 十、浏览器功能

### 10.1 GStack Browser

`/open-gstack-browser` 启动功能：

- 侧边栏 AI agent 集成
- Anti-bot stealth（Google、NYTimes 无 captcha）
- 自动模型路由：Sonnet for actions，Opus for analysis
- 一键 cookie 导入
- 自定义 branding（"GStack Browser"）

### 10.2 Sidebar Agent

侧边栏自然语言输入：
- "Navigate to the settings page and screenshot it"
- "Fill out this form with test data"
- "Go through every item and extract the prices"

### 10.3 Handoff Mode

`$B handoff` — CAPTCHA/auth/MFA 时切换到可见 Chrome
`$B resume` — 解决问题后继续

---

## 十一、配置工具

### 11.1 CLI 工具（bin/）

| 工具 | 功能 |
|------|------|
| `gstack-config` | 配置管理 |
| `gstack-team-init` | 团队模式初始化 |
| `gstack-update-check` | 更新检查 |
| `gstack-uninstall` | 卸载 |
| `gstack-slug` | 项目标识符 |
| `gstack-repo-mode` | 仓库模式检测 |
| `gstack-learnings-log` | 学习日志 |
| `gstack-learnings-search` | 学习搜索 |
| `gstack-telemetry-log` | 遥测日志 |
| `gstack-timeline-log` | 时间线日志 |
| `gstack-review-log` | 审查日志 |
| `gstack-analytics` | 分析数据 |

### 11.2 配置命令

```bash
# 遥测设置
gstack-config set telemetry community  # 社区模式
gstack-config set telemetry anonymous   #匿名模式
gstack-config set telemetry off         # 完全关闭

# 主动模式
gstack-config set proactive false       # 关闭主动建议

# Skill 前缀
gstack-config set skill_prefix true     # 使用 /gstack- 前缀
```

---

## 十二、项目结构

```
gstack/
├── office-hours/         # YC Office Hours skill
├── plan-ceo-review/      # CEO review skill
├── plan-eng-review/      # Engineering review skill
├── plan-design-review/   # Design review skill
├── plan-devex-review/    # DX review skill
├── autoplan/             # Auto-review pipeline
├── review/               # PR review skill
├── investigate/          # Debugging skill
├── design-consultation/  # Design system skill
├── design-shotgun/       # Visual exploration skill
├── design-html/          # Production HTML skill
├── design-review/        # Design audit + fix
├── devex-review/         # DX live audit
├── qa/                   # QA testing skill
├── qa-only/              # QA report only
├── benchmark/            # Performance benchmark
├── ship/                 # Release skill
├── land-and-deploy/      # Merge + deploy skill
├── document-release/     # Doc update skill
├── canary/               # Post-deploy monitoring
├── retro/                # Retrospective skill
├── learn/                # Memory management
├── cso/                  # Security audit
├── codex/                # Multi-AI review
├── careful/              # Safety warnings
├── freeze/               # Edit lock
├── guard/                # Full safety
├── unfreeze/             # Unlock
├── browse/               # Browser CLI
├── open-gstack-browser/  # GStack Browser
├── setup-browser-cookies/# Cookie import
├── setup-deploy/         # Deploy config
├── gstack-upgrade/       # Self-updater
├── pair-agent/           # Multi-agent coord
├── bin/                  # CLI utilities
├── hosts/                # Multi-agent configs
├── scripts/              # Build tools
├── docs/                 # Documentation
├── extension/            # Chrome extension
├── lib/                  # Shared libraries
├── test/                 # Test suite
├── setup                 # Setup script
├── SKILL.md              # Skill definition
├── SKILL.md.tmpl         # Skill template
├── ETHOS.md              # Builder philosophy
├── CLAUDE.md             # Claude Code instructions
└── README.md             # README
```

---

## 十三、实战示例

### 13.1 完整 Sprint 流程

```bash
# 1. 产品思考
/office-hours
# 输出设计文档

# 2. 规划审查（或一键 autoplan）
/autoplan
# 或分别运行：
/plan-ceo-review
/plan-eng-review
/plan-design-review

# 3. 实现（Claude 自动根据计划编写代码）

# 4. 审查
/review
# AUTO-FIXED 2 issues, ASK 1 race condition

# 5. QA 测试
/qa https://staging.myapp.com
# opens browser, clicks through, finds and fixes bug

# 6. 发布
/ship
# Tests: 42 → 51 (+9 new). PR created.

# 7. 合并部署
/land-and-deploy
# Merge → CI → Deploy → Verify

# 8. 监控
/canary
# Watch for console errors, performance regression

# 9. 反思
/retro
# Weekly team retro
```

### 13.2 安全审计

```bash
/cso
# OWASP Top 10 + STRIDE audit
# Zero-noise output
# Each finding includes exploit scenario
```

### 13.3 设计流程

```bash
# 1. 探索选项
/design-shotgun
# 生成 4-6 mockups，浏览器对比，迭代

# 2. 转为生产 HTML
/design-html
# Pretext layout，framework detection
```

### 13.4 调试流程

```bash
/investigate
# 铁律：无调查不修复
# Traces data flow
# Tests hypotheses
# Stops after 3 failed fixes
```

### 13.5 安全模式

```bash
/guard
# /careful + /freeze 组合
# 警告危险命令 + 锁定编辑目录

/unfreeze
# 解除锁定
```

---

## 十四、遥测配置

GStack 默认询问遥测设置：

| 模式 | 说明 |
|------|------|
| `community` | 分享使用数据（skills 使用、时长、crash 信息），带稳定设备 ID |
| `anonymous` | 仅计数器，无唯一 ID |
| `off` | 完全关闭 |

```bash
gstack-config set telemetry community
gstack-config set telemetry anonymous
gstack-config set telemetry off
```

---

## 十五、卸载

### 15.1 使用卸载脚本

```bash
~/.claude/skills/gstack/bin/gstack-uninstall

# 保留状态
~/.claude/skills/gstack/bin/gstack-uninstall --keep-state

# 强制卸载
~/.claude/skills/gstack/bin/gstack-uninstall --force
```

### 15.2 手动卸载

```bash
# 1. 停止 browse daemons
pkill -f "gstack.*browse"

# 2. 移除 symlinks
find ~/.claude/skills -maxdepth 1 -type l | while read link; do
  case "$(readlink "$link")" in gstack/*) rm -f "$link" ;; esac
done

# 3. 移除 gstack
rm -rf ~/.claude/skills/gstack

# 4. 移除全局状态
rm -rf ~/.gstack

# 5. 移除其他集成
rm -rf ~/.codex/skills/gstack*
rm -rf ~/.factory/skills/gstack*
rm -rf ~/.kiro/skills/gstack*

# 6. 清理项目配置
# 在每个项目根目录运行
rm -rf .gstack .gstack-worktrees .claude/skills/gstack
```

---

## 十六、常见问题

### Q1: 如何查看当前版本？

```bash
cat ~/.claude/skills/gstack/VERSION
```

### Q2: 如何升级？

```bash
/gstack-upgrade
# 或
~/.claude/skills/gstack/bin/gstack-update-check
```

### Q3: 如何关闭主动建议？

```bash
gstack-config set proactive false
```

### Q4: 如何使用 skill 前缀？

```bash
gstack-config set skill_prefix true
# 然后使用 /gstack-qa 而非 /qa
```

### Q5: 浏览器 CAPTCHA 问题？

```bash
$B handoff  # 切换到可见 Chrome
# 解决问题后
$B resume   # 继续
```

---

## 十七、参考链接

| 资源 | 链接 |
|------|------|
| GitHub 仓库 | https://github.com/garrytan/gstack |
| 作者 Twitter | https://x.com/garrytan |
| Y Combinator | https://www.ycombinator.com/ |
| Boil the Ocean 文章 | https://garryslist.org/posts/boil-the-ocean |
| OpenClaw | https://github.com/openclaw/openclaw |
| Conductor | https://conductor.build |
| YC招聘 | https://ycombinator.com/software |

---

## 十八、License

MIT License — 完全免费，无高级版，无等待名单。

---

## 十九、统计数据

| 指标 | 数值 |
|------|------|
| Skills | 23 专业角色 |
| Power Tools | 8 |
| 作者 LOC（60天） | 600,000+ |
| 每日 LOC | 10,000-20,000 |
| 2026 Contributions | 1,237+ |
| 支持的 AI Agents | 8 |

---

*文档由 Claude Code 根据实际仓库内容生成*