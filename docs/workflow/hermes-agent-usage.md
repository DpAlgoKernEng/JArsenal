# Hermes Agent 使用指南

## 项目概述

**Hermes Agent** 是由 [Nous Research](https://nousresearch.com) 开发的自我改进型 AI 代理。它是唯一具有内置学习循环的代理——从经验中创建技能、在使用过程中改进技能、主动保存知识、搜索历史对话、并在跨会话中建立深化的用户模型。

### 核心特点

| 特性 | 描述 |
|------|------|
| **真正的终端界面** | 完整 TUI，多行编辑、斜杠命令自动补全、对话历史、中断重定向、流式工具输出 |
| **全平台覆盖** | Telegram、Discord、Slack、WhatsApp、Signal、微信、飞书、钉钉、企业微信、Email、SMS、Matrix、iMessage、Webhook 等 16+ 平台 |
| **闭环学习** | 代理管理的记忆、周期性提醒、复杂任务后自动创建技能、使用时技能自我改进、FTS5 会话搜索、Honcho 用户建模 |
| **定时自动化** | 内置 cron 调度器，可向任意平台交付。日报、夜间备份、每周审计——全用自然语言描述 |
| **委托与并行** | 可派生隔离的子代理进行并行工作流。编写通过 RPC 调用工具的 Python 脚本，将多步管道压缩为零上下文成本的回合 |
| **随处运行** | 6 种终端后端：local、Docker、SSH、Daytona、Singularity、Modal。Daytona 和 Modal 提供无服务器持久化——空闲时休眠、按需唤醒 |
| **研究就绪** | 批量轨迹生成、Atropos RL 环境、轨迹压缩，用于训练下一代工具调用模型 |

---

## 快速安装

### 一键安装

```bash
curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh | bash
```

支持 Linux、macOS、WSL2 和 Android（通过 Termux）。

**安装后配置**:

```bash
source ~/.bashrc    # 重载 shell（或: source ~/.zshrc）
hermes              # 开始对话！
```

### 系统要求

| 平台 | 说明 |
|------|------|
| **Linux/macOS** | 完全支持 |
| **Windows** | 不支持原生 Windows，需安装 WSL2 |
| **Android/Termux** | 通过 Termux 支持，安装 `.termux]` 额外依赖 |

---

## 快速开始

### 基本命令

```bash
hermes              # 交互式 CLI — 开始对话
hermes model        # 选择 LLM 提供商和模型
hermes tools        # 配置启用的工具
hermes config set   # 设置单个配置值
hermes gateway      # 启动消息网关（Telegram、Discord 等）
hermes setup        # 运行完整设置向导（一次性配置所有）
hermes claw migrate # 从 OpenClaw 迁移
hermes update       # 更新到最新版本
hermes doctor       # 诊断问题
hermes backup       # 备份配置和数据
hermes import       # 导入备份
```

---

## 支持的 LLM 提供商

Hermes 支持多种 LLM 提供商，可通过 `hermes model` 或 `/model` 命令切换：

| 提供商 | 说明 | 端点 |
|--------|------|------|
| **Nous Portal** | Nous Research 官方 | portal.nousresearch.com |
| **OpenRouter** | 200+ 模型聚合 | openrouter.ai |
| **OpenAI** | GPT 系列 | platform.openai.com |
| **Anthropic** | Claude 系列 | anthropropic.com |
| **z.ai/GLM** | 智谱 GLM 模型 | z.ai / open.bigmodel.cn |
| **Kimi/Moonshot** | Moonshot 编码模型 | platform.kimi.ai |
| **MiniMax** | MiniMax 模型 | minimax.io |
| **xAI/Grok** | Grok 模型 | x.ai |
| **Xiaomi MiMo** | 小米 MiMo 模型 | xiaomimimo.com |
| **Qwen OAuth** | 通义千问（OAuth 登录） | portal.qwen.ai |
| **Google Gemini** | Gemini 模型 | aistudio.google.com |
| **Hugging Face** | 20+ 开源模型 | huggingface.co |
| **Arcee AI** | Trinity 模型 | chat.arcee.ai |
| **OpenCode Zen/Go** | 精选模型 | opencode.ai |

### Fast Mode

```bash
/fast               # 切换到优先处理队列（更低延迟）
```

支持 OpenAI Priority Processing 和 Anthropic fast tier。

---

## CLI vs 消息平台对照

| 操作 | CLI | 消息平台 |
|------|-----|----------|
| 开始对话 | `hermes` | 运行 `hermes gateway start`，发送消息 |
| 新对话 | `/new` 或 `/reset` | `/new` 或 `/reset` |
| 切换模型 | `/model [provider:model]` | `/model [provider:model]` |
| 设置人格 | `/personality [name]` | `/personality [name]` |
| 重试/撤销 | `/retry`, `/undo` | `/retry`, `/undo` |
| 压缩上下文/查看用量 | `/compress`, `/usage`, `/insights` | `/compress`, `/usage`, `/insights` |
| 浏览技能 | `/skills` 或 `/<skill-name>` | `/skills` 或 `/<skill-name>` |
| 中断当前工作 | `Ctrl+C` 或发送新消息 | `/stop` 或发送新消息 |

---

## 消息平台支持（16+）

### 完整平台列表

| 平台 | 文件 | 特性 |
|------|------|------|
| Telegram | `telegram.py` | Webhook/长轮询、语音转录 |
| Discord | `discord.py` | 论坛频道、允许频道白名单 |
| Slack | `slack.py` | Socket Mode、助理线程 |
| WhatsApp | `whatsapp.py` | Baileys 桥接 |
| Signal | `signal.py` | 端到端加密 |
| Matrix | `matrix.py` | E2EE 支持 |
| Email | `email.py` | IMAP/SMTP |
| SMS | `sms.py` | Twilio |
| iMessage | `bluebubbles.py` | BlueBubbles 桥接 |
| WeChat/微信 | `weixin.py` | iLink Bot API |
| WeCom/企业微信 | `wecom.py` | 回调模式 |
| Feishu/飞书 | `feishu.py` | QR 登录 |
| DingTalk/钉钉 | `dingtalk.py` | 企业集成 |
| Mattermost | `mattermost.py` | 开源团队聊天 |
| Home Assistant | `homeassistant.py` | 智能家居控制 |
| Webhook | `webhook.py` | 自定义集成 |

### 网关配置

```bash
hermes gateway setup   # 交互式设置向导
hermes gateway start   # 启动网关
hermes gateway status  # 查看状态
```

### 代理支持

Hermes 支持统一代理配置：
- SOCKS 代理
- `DISCORD_PROXY` 专用代理
- macOS 系统代理自动检测

---

## 工具系统

### 核心工具（40+）

| 工具类别 | 工具名称 | 描述 |
|----------|----------|------|
| **Web** | `web_search`, `web_extract` | 网络搜索和内容提取 |
| **Terminal** | `terminal`, `process` | 命令执行和进程管理 |
| **File** | `read_file`, `write_file`, `patch`, `search_files` | 文件操作 |
| **Vision** | `vision_analyze` | 图像分析 |
| **Image** | `image_generate` | 图像生成 |
| **Browser** | `browser_navigate`, `browser_click`, `browser_type` 等 | 浏览器自动化（Browserbase） |
| **Skills** | `skills_list`, `skill_view`, `skill_manage` | 技能管理 |
| **TTS** | `text_to_speech` | 文本转语音 |
| **Memory** | `todo`, `memory` | 待办和记忆 |
| **Search** | `session_search` | 会话历史搜索 |
| **Code** | `execute_code` | Python 代码执行沙箱 |
| **Delegate** | `delegate_task` | 子代理委托 |
| **Cron** | `cronjob` | 定时任务管理 |
| **Message** | `send_message` | 跨平台消息发送 |
| **Home Assistant** | `ha_list_entities`, `ha_call_service` 等 | 智能家居控制 |
| **MCP** | MCP 工具 | MCP 服务器集成 |

### 工具集（Toolsets）

工具可以按工具集分组启用/禁用：

| 工具集 | 描述 | 包含工具 |
|--------|------|----------|
| `web` | 网络研究 | `web_search`, `web_extract` |
| `terminal` | 命令执行 | `terminal`, `process` |
| `vision` | 图像分析 | `vision_analyze` |
| `browser` | 浏览器自动化 | 全套 browser 工具 |
| `file` | 文件操作 | 全套文件工具 |
| `skills` | 技能系统 | 技能管理工具 |
| `moa` | Mixture of Agents | 多代理协作 |

### 终端后端

| 后端 | 描述 | 适用场景 |
|------|------|----------|
| `local` | 本地执行 | 个人开发机 |
| `docker` | Docker 容器 | 隔离环境 |
| `ssh` | SSH 远程 | 远程服务器 |
| `modal` | Modal 无服务器 | 云端无服务器 |
| `daytona` | Daytona | 开发环境管理 |
| `singularity` | Singularity 容器 | HPC 环境 |

配置方式：

```bash
hermes config set terminal.backend docker
```

---

## 技能系统

### 技能概述

技能是 Hermes 的程序性记忆——可复用的工作流程指令。技能存储在 `~/.hermes/skills/` 目录。

### 内置技能分类

| 类别 | 技能示例 |
|------|----------|
| **Apple** | `apple-notes`, `apple-reminders`, `imessage`, `findmy` |
| **Autonomous AI Agents** | `claude-code`, `codex`, `opencode` |
| **Creative** | `ascii-art`, `excalidraw`, `manim-video`, `p5js` |
| **Data Science** | `jupyter-live-kernel` |
| **DevOps** | `webhook-subscriptions` |
| **Email** | `himalaya` |
| **Gaming** | `minecraft-modpack-server`, `pokemon-player` |
| **GitHub** | `github-auth`, `github-pr-workflow`, `github-issues`, `codebase-inspection` |
| **Media** | `gif-search`, `youtube-content` |
| **MLOps** | `vllm`, `llama-cpp`, `unsloth`, `axolotl`, `trl-fine-tuning`, `peft` |
| **Productivity** | `notion`, `linear`, `google-workspace`, `ocr-and-documents` |
| **Research** | `arxiv`, `blogwatcher`, `research-paper-writing` |
| **Smart Home** | `openhue` |
| **Software Development** | `test-driven-development`, `systematic-debugging`, `writing-plans` |
| **Social Media** | `xitter` |

### 技能管理命令

```bash
/skills              # 列出可用技能
/skills search <q>   # 搜索技能
/skills install <n>  # 安装技能
/<skill-name>        # 加载技能
```

### Skills Hub

Hermes 集成 [agentskills.io](https://agentskills.io) 开放标准，支持技能发现、安装和发布。

```bash
hermes skills browse   # 浏览 Skills Hub
hermes skills install <skill-name>  # 安装社区技能
```

### SKILL.md 格式

```yaml
---
name: my-skill
description: 技能简介
version: 1.0.0
author: Your Name
license: MIT
platforms: [macos, linux]          # 可选：平台限制
required_environment_variables:    # 可选：环境变量需求
  - name: MY_API_KEY
    prompt: API key
    help: Where to get it
metadata:
  hermes:
    tags: [Category, Keywords]
    related_skills: [other-skill]
    fallback_for_toolsets: [web]   # 可选：备用显示条件
---

# Skill Title

技能描述...

## When to Use
触发条件...

## Procedure
步骤说明...
```

---

## 记忆系统

### 记忆类型

| 记忆类型 | 文件 | 描述 |
|----------|------|------|
| **MEMORY.md** | `~/.hermes/memories/MEMORY.md` | 通用持久记忆 |
| **USER.md** | `~/.hermes/memories/USER.md` | 用户画像记忆 |

### 记忆管理

```bash
/memory              # 查看记忆状态
/memory add <text>   # 添加记忆
/memory search <q>   # 搜索记忆
```

### Honcho 用户建模

Hermes 集成 Honcho 进行跨会话 AI-native 用户建模，建立持久的用户理解。

---

## 定时任务（Cron）

### 功能

- 内置 cron 调度器
- 自然语言描述任务
- 向任意平台交付

### 管理

```bash
/cron                # 查看定时任务
/cron add            # 添加任务
/cron remove <id>    # 移除任务
```

### 示例

```
每天早上 9 点发送日报到 Telegram
每周一晚上备份数据库
每 5 分钟检查服务状态
```

---

## 上下文文件

### 功能

项目上下文文件可影响每个对话：
- `AGENTS.md` - 项目级指令
- `SOUL.md` - 代理人格定义

### 使用

放置在项目目录，Hermes 会自动加载并注入系统提示。

---

## Profiles（多实例）

### 功能

Hermes 支持完全隔离的多实例配置：
- 每个 profile 有独立的 `HERMES_HOME` 目录
- 独立的配置、API 密钥、记忆、技能、会话、网关

### 管理

```bash
hermes profile list         # 列出所有 profiles
hermes profile create <n>   # 创建新 profile
hermes -p <name>            # 使用指定 profile 运行
```

---

## 皮肤/主题系统

### 内置皮肤

| 皮肤名 | 风格 |
|--------|------|
| `default` | 经典 Hermes 金色/kawaii |
| `ares` | 红铜战神主题 |
| `mono` | 灰度单色 |
| `slate` | 冷蓝开发者主题 |

### 自定义皮肤

创建 `~/.hermes/skins/<name>.yaml`：

```yaml
name: cyberpunk
description: Neon-soaked terminal theme

colors:
  banner_border: "#FF00FF"
  banner_title: "#00FFFF"

spinner:
  thinking_verbs: ["jacking in", "decrypting"]

branding:
  agent_name: "Cyber Agent"
```

### 使用

```bash
/skin <name>         # 切换皮肤
hermes config set display.skin cyberpunk
```

---

## 安全机制

### 安全层级

| 层级 | 实现 |
|------|------|
| **Sudo 密码管道** | `shlex.quote()` 防止 shell 注入 |
| **危险命令检测** | `approval.py` 正则模式 + 用户审批流程 |
| **Cron 提示注入** | 扫描器阻止指令覆盖模式 |
| **写入黑名单** | 保护路径（`~/.ssh/authorized_keys` 等） |
| **Skills Guard** | Hub 安装技能的安全扫描器 |
| **代码执行沙箱** | `execute_code` 子进程剥离 API 密钥 |
| **容器加固** | Docker：所有能力丢弃、无权限提升、PID 限制 |

### 命令审批

危险命令需要用户批准：
- `rm -rf`
- `sudo`
- 文件系统写入
- 网络操作

---

## 配置系统

### 配置文件位置

| 文件 | 位置 | 用途 |
|------|------|------|
| `config.yaml` | `~/.hermes/config.yaml` | 设置 |
| `.env` | `~/.hermes/.env` | API 密钥 |
| `auth.json` | `~/.hermes/auth.json` | OAuth 凭据 |
| `state.db` | `~/.hermes/state.db` | SQLite 会话数据库 |

### 配置命令

```bash
hermes config set <key> <value>   # 设置值
hermes config get <key>           # 获取值
hermes config list                # 列出所有
```

### 关键配置项

```yaml
model:
  default: anthropic/claude-opus-4.6

terminal:
  backend: local
  cwd: .
  timeout: 60

compression:
  enabled: true
  threshold: 0.85
  summary_model: google/gemini-3-flash-preview

display:
  skin: default
  tool_progress: true

stt:
  provider: local  # local | groq | openai
```

---

## 环境变量

### LLM 提供商

| 变量 | 描述 |
|------|------|
| `OPENROUTER_API_KEY` | OpenRouter API 密钥 |
| `OPENAI_API_KEY` | OpenAI API 密钥 |
| `ANTHROPIC_API_KEY` | Anthropic API 密钥 |
| `GOOGLE_API_KEY` | Google/Gemini API 密钥 |
| `GLM_API_KEY` | z.ai/GLM API 密钥 |
| `KIMI_API_KEY` | Kimi/Moonshot API 密钥 |
| `MINIMAX_API_KEY` | MiniMax API 密钥 |
| `XAI_API_KEY` | xAI/Grok API 密钥 |
| `XIAOMI_API_KEY` | Xiaomi MiMo API 密钥 |

### 工具 API 密钥

| 变量 | 描述 |
|------|------|
| `EXA_API_KEY` | Exa 网络搜索 |
| `PARALLEL_API_KEY` | Parallel 网络搜索 |
| `FIRECRAWL_API_KEY` | Firecrawl 网络抓取 |
| `BROWSERBASE_API_KEY` | Browserbase 浏览器自动化 |
| `FAL_KEY` | FAL.ai 图像生成 |
| `HONCHO_API_KEY` | Honcho 用户建模 |

### 消息平台

| 变量 | 描述 |
|------|------|
| `TELEGRAM_BOT_TOKEN` | Telegram Bot Token |
| `SLACK_BOT_TOKEN` | Slack Bot Token |
| `SLACK_APP_TOKEN` | Slack App Token |
| `DISCORD_BOT_TOKEN` | Discord Bot Token |
| `EMAIL_ADDRESS` | Email 地址 |
| `EMAIL_PASSWORD` | Email 密码 |

---

## 本地 Web Dashboard

v0.9.0 新增浏览器仪表盘：
- 配置设置
- 监控会话
- 浏览技能
- 管理网关

```bash
hermes dashboard      # 启动仪表盘
```

---

## MCP 集成

### 功能

连接任意 MCP 服务器扩展能力：
- Claude Code MCP 工具
- 自定义 MCP 服务器
- OAuth 认证支持

### 配置

```bash
hermes mcp add <server-config>   # 添加 MCP 服务器
hermes mcp list                  # 列出 MCP 服务器
```

---

## OpenClaw 迁移

### 功能

从 OpenClaw 自动导入：
- SOUL.md 人格文件
- 记忆（MEMORY.md、USER.md）
- 技能 → `~/.hermes/skills/openclaw-imports/`
- 命令白名单
- 消息设置
- API 密钥

### 命令

```bash
hermes claw migrate              # 交互式迁移
hermes claw migrate --dry-run    # 预览迁移内容
hermes claw migrate --preset user-data  # 仅用户数据
hermes claw migrate --overwrite  # 覆盖冲突
```

---

## RL 训练支持

### 功能

用于训练工具调用模型：
- 批量轨迹生成
- Atropos RL 环境
- 范围压缩
- Tinker API 集成

### 配置

```bash
# 安装 RL 子模块
git submodule update --init tinker-atropos
uv pip install -e "./tinker-atropos"

# 配置
TINKER_API_KEY=xxx
WANDB_API_KEY=xxx
```

---

## 项目架构

### 核心模块

| 模块 | 文件 | 描述 |
|------|------|------|
| Agent Loop | `run_agent.py` | AIAgent 类、对话循环、工具分发 |
| CLI | `cli.py` | HermesCLI 类、TUI、prompt_toolkit |
| Tool Orchestration | `model_tools.py` | 工具协调层 |
| Toolsets | `toolsets.py` | 工具集定义 |
| Session Store | `hermes_state.py` | SQLite + FTS5 |
| Batch Runner | `batch_runner.py` | 并行批处理 |

### 目录结构

```
hermes-agent/
├── run_agent.py              # Agent 核心
├── cli.py                    # CLI TUI
├── model_tools.py            # 工具协调
├── toolsets.py               # 工具集定义
├── hermes_state.py           # 会话存储
├── agent/                    # Agent 内部模块
│   ├── prompt_builder.py     # 系统提示组装
│   ├── context_compressor.py # 上下文压缩
│   ├── display.py            # 显示组件
│   └── trajectory.py         # 轨迹保存
├── hermes_cli/               # CLI 命令实现
│   ├── main.py               # 入口点
│   ├── config.py             # 配置管理
│   ├── setup.py              # 设置向导
│   ├── commands.py           # 斜杠命令注册表
│   ├── skin_engine.py        # 皮肤引擎
│   └── skills_hub.py         # Skills Hub
├── tools/                    # 工具实现（自注册）
│   ├── registry.py           # 工具注册表
│   ├── terminal_tool.py      # 终端工具
│   ├── file_operations.py    # 文件操作
│   ├── web_tools.py          # 网络工具
│   ├── browser_tool.py       # 浏览器工具
│   ├── mcp_tool.py           # MCP 工具
│   ├── delegate_tool.py      # 委托工具
│   └── environments/         # 终端后端
├── gateway/                  # 消息网关
│   ├── run.py                # 网关主循环
│   ├── session.py            # 会话存储
│   └── platforms/            # 平台适配器
├── skills/                   # 内置技能
├── optional-skills/          # 可选技能
├── tests/                    # 测试套件
└── website/                  # 文档网站
```

---

## 开发者指南

### 开发环境设置

```bash
git clone --recurse-submodules https://github.com/NousResearch/hermes-agent.git
cd hermes-agent

uv venv venv --python 3.11
source venv/bin/activate
uv pip install -e ".[all,dev]"

mkdir -p ~/.hermes/{cron,sessions,logs,memories,skills}
cp cli-config.yaml.example ~/.hermes/config.yaml
touch ~/.hermes/.env
```

### 运行测试

```bash
pytest tests/ -v              # 完整套件
pytest tests/test_model_tools.py -q  # 工具集测试
pytest tests/gateway/ -q      # 网关测试
```

### 添加工具

1. 创建 `tools/your_tool.py`
2. 使用 `registry.register()` 注册
3. 在 `model_tools.py` 添加导入
4. 在 `toolsets.py` 添加到工具集

### 添加技能

在 `skills/<category>/<name>/` 创建：
- `SKILL.md` - 主指令
- `scripts/` - 辅助脚本
- `references/` - 参考资料

---

## 常见问题

### Q: 如何切换不同的功能规范？

A: Hermes 通过 SQLite 会话管理支持多会话，使用 `/new` 开始新会话。

### Q: 技能文件存储在哪里？

A: `~/.hermes/skills/<category>/<name>/`

### Q: 支持哪些终端后端？

A: local、docker、ssh、modal、daytona、singularity

### Q: 如何在企业隔离环境使用？

A: 配置代理或使用 SSH 后端在远程服务器执行。

### Q: 如何从 OpenClaw 迁移？

A: 运行 `hermes claw migrate` 自动导入。

---

## 相关资源

- **官方网站**: https://hermes-agent.nousresearch.com
- **文档**: https://hermes-agent.nousresearch.com/docs/
- **GitHub**: https://github.com/NousResearch/hermes-agent
- **Discord**: https://discord.gg/NousResearch
- **Skills Hub**: https://agentskills.io

---

## 总结

Hermes Agent 是一个功能丰富的自我改进型 AI 代理，核心特点：

| 特性 | 价值 |
|------|------|
| **闭环学习** | 技能自动创建、改进、持久化 |
| **全平台覆盖** | 16+ 消息平台，随时随地使用 |
| **随处运行** | 6 种终端后端，无服务器支持 |
| **强大工具集** | 40+ 工具，浏览器自动化、代码执行、委托 |
| **丰富技能库** | 70+ 内置技能，涵盖研究、开发、创意等 |
| **安全加固** | 多层安全机制，命令审批、容器隔离 |
| **研究就绪** | RL 训练支持，轨迹生成 |

适用场景：
- 个人 AI 助手（Telegram/微信/邮件）
- 开发辅助（终端执行、代码审查）
- 研究助手（arXiv 搜索、论文写作）
- 团队协作（Slack/Discord/飞书集成）
- 智能家居控制（Home Assistant）
- MLOps（模型训练、推理服务）