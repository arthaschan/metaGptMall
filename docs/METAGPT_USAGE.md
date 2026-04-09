# MetaGPT 使用说明（本项目）

本文档说明如何在 `metaGptMall` 仓库中使用 MetaGPT 来生成/迭代代码与文档，并把产物回写到仓库。

> **安全要求**：不要把任何真实 API Key 写进仓库。只通过环境变量提供。

---

## 1. 前置条件

### 1.1 系统要求

- macOS / Linux（Windows 建议使用 WSL2）
- Python 3.10+（建议 3.11）
- 已安装 `git`、`bash`

### 1.2 安装 MetaGPT

MetaGPT 可通过 pip 安装（具体命令可能因版本不同而略有差异；以官方文档为准）：

```bash
pip install metagpt
```

或从源码安装：

```bash
git clone https://github.com/geekan/MetaGPT.git
cd MetaGPT
pip install -e .
```

> **注意**：不同版本的 MetaGPT CLI/SDK 调用方式可能不同，请以你安装的版本的官方文档为准。  
> 本仓库脚本默认使用 CLI 方式调用（`metagpt` 命令），如需适配 SDK 请修改 `metagpt_tools/run.py`。

### 1.3 安装本工具依赖

```bash
cd metagpt_tools
pip install -r requirements.txt
```

### 1.4 配置环境变量（**必须手动修改**）

根据你使用的 LLM 提供商不同，环境变量可能不同。以下以 OpenAI 风格为例（**仅示例，不要写入代码或提交到 git**）：

```bash
# 必填：你的 API Key（不要提交到 git）
export OPENAI_API_KEY="your-api-key-here"

# 可选：自定义模型名（按需修改）
export OPENAI_MODEL="gpt-4o-mini"

# 可选：指定 MetaGPT CLI 可执行文件路径（默认 metagpt）
export METAGPT_CMD="metagpt"
```

建议把这些变量放在 `.env` 文件中（确保 `.env` 已加入 `.gitignore`）：

```bash
# .env（不要提交！）
OPENAI_API_KEY=your-api-key-here
OPENAI_MODEL=gpt-4o-mini
METAGPT_CMD=metagpt
```

然后 `source .env` 或使用 `python-dotenv` 自动加载。

---

## 2. 本仓库推荐工作流

### 2.1 仓库上下文文件

MetaGPT 调用时，本仓库会自动把以下上下文文件拼接成 prompt 喂给模型，以减少"胡编"和返工：

| 文件 | 说明 |
|------|------|
| `PROJECT_CONTEXT.md` | 项目目标、阶段计划 |
| `docs/ARCHITECTURE.md` | 架构与模块设计 |
| `docs/API.md` | 接口草案 |
| `docs/PORTS.md` | 端口约定 |
| `docs/ROCKETMQ_TOPICS.md` | MQ topic/tag 约定 |
| `docs/REDIS_KEYS.md` | Redis key/TTL 约定 |
| `docs/INFRA.md` / `docs/INFRA_B.md` | 基础设施说明 |

上下文文件列表维护在 `metagpt_tools/context_files.txt`，可按需增删。

### 2.2 推荐工作流：需求 → 生成 → 验证 → 回写

```
需求文本
    │
    ▼
整理 prompt（上下文 + 任务）
    │
    ▼
metagpt_tools/run.py  ─────→  metagpt_outputs/<timestamp>/
    │                               ├── prompt.txt    （完整 prompt 存档）
    │                               ├── stdout.txt    （MetaGPT 输出）
    │                               └── stderr.txt    （错误日志）
    │
    ▼
人工 review 产物
    │
    ▼
挑选需要的文件 → 回写仓库 → git commit → git push
```

### 2.3 任务拆分建议

把需求拆成"可一次提交"的最小单元，例如：

- 仅新增一个 API + mapper + 表结构变更
- 仅补齐一个文档（并同步 README 引用）
- 仅增加一个脚本（并在 `docs/` 中说明用法）

这样更容易 review 与回滚。

### 2.4 输出目录约定

MetaGPT 输出统一放在：

```
metagpt_outputs/<YYYYMMDD_HHMMSS>/
```

目录已加入 `.gitignore`，不会随意提交到仓库。确认产物后再手动复制到对应目录提交。

---

## 3. 最小可运行示例

### 3.1 一行命令（任务作为参数传入）

```bash
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh "为后端新增订单取消接口，并补齐 API 文档与测试建议"
```

### 3.2 任务文件方式（适合长需求）

把需求写在文件里（例如 `METAGPT_TASK.md`），然后：

```bash
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh --task-file METAGPT_TASK.md
```

### 3.3 直接调用 Python（更灵活）

```bash
export OPENAI_API_KEY="your-api-key-here"
python metagpt_tools/run.py "为后端新增订单取消接口"
# 或
python metagpt_tools/run.py --task-file METAGPT_TASK.md --out-dir /tmp/my_output
```

---

## 4. 脚本与文件说明

| 文件 | 作用 |
|------|------|
| `scripts/metagpt_run.sh` | 一键运行脚本，读取环境变量，喂上下文给 MetaGPT，输出到指定目录 |
| `metagpt_tools/run.py` | Python 入口，拼接上下文 + 调用 CLI + 保存输出 |
| `metagpt_tools/context_files.txt` | 上下文文件列表（每行一个相对于仓库根目录的路径） |
| `metagpt_tools/requirements.txt` | Python 依赖（仅包装层依赖，MetaGPT 本身需单独安装） |

---

## 5. 常见问题

### 5.1 为什么跑不起来？

- 检查 Python 版本与依赖：`python --version`（需 3.10+），`pip install -r metagpt_tools/requirements.txt`
- 检查 MetaGPT 是否已安装：`metagpt --help`（或 `python -m metagpt --help`）
- 检查 API Key 是否正确设置：`echo $OPENAI_API_KEY`
- 如果 MetaGPT CLI 命令名不同：设置 `METAGPT_CMD` 环境变量

### 5.2 如何适配不同版本的 MetaGPT？

- 本仓库脚本通过 `METAGPT_CMD` 变量支持任意命令，可能需要按你的 MetaGPT 版本调整。
- 如果你的版本没有 CLI，可修改 `metagpt_tools/run.py` 中的 `main()` 函数，将 `subprocess.run(...)` 替换为 MetaGPT SDK 调用。
- 参考官方文档：https://github.com/geekan/MetaGPT

### 5.3 如何保证生成内容符合本仓库约定？

- 把关键约定写进 `docs/` 并加入 `metagpt_tools/context_files.txt`
- 对生成结果做 review：尤其是端口、topic/tag、Redis key、DB 账号密码等
- 生产环境配置不要写死：一定使用环境变量或配置文件

### 5.4 输出在哪里？

默认输出到 `metagpt_outputs/<YYYYMMDD_HHMMSS>/`，该目录在 `.gitignore` 中，不会自动提交。

---

## 6. 与 GitHub Copilot 协作建议

- 每个阶段尽量"一个 commit / 一个 PR"
- 若要修改已存在文件，记录对应文件 SHA（或本地改完再 push）
- 不确定时先生成到 `metagpt_outputs/`，确认后再回写仓库
- 不要把任何真实 API Key 写进仓库；只写环境变量名示例

---

## 7. 相关文档

- [项目架构](ARCHITECTURE.md)
- [API 文档](API.md)
- [端口说明](PORTS.md)
- [RocketMQ Topics](ROCKETMQ_TOPICS.md)
- [Redis Keys](REDIS_KEYS.md)
- [基础设施说明](INFRA_B.md)
- [README（项目主页）](../README.md)
- [MetaGPT 官方文档](https://github.com/geekan/MetaGPT)
