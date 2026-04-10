# MetaGPT 使用说明（本项目）

本文档说明如何在 `metaGptMall` 仓库中使用 MetaGPT 来生成/迭代代码与文档，并把产物回写到仓库。

如果你是第一次接触本仓库，先读：

- [NEWBIE_QUICKSTART.md](NEWBIE_QUICKSTART.md)
- [METAGPT_ALIGNMENT_STATUS.md](METAGPT_ALIGNMENT_STATUS.md)

> 当前推荐入口是 `metagpt_team` 这套 runner。
> `metagpt_tools/run.py` 属于旧版 CLI 包装层，保留用于兼容，不再作为首选。

> **安全要求**：不要把任何真实 API Key 写进仓库。只通过环境变量提供。

---

## 1. 前置条件

### 1.1 系统要求

- macOS / Linux（Windows 建议使用 WSL2）
- Python 3.10 或 3.11（建议 3.11）
- 已安装 `git`、`bash`

> 当前仓库已验证：base 环境若使用 Python 3.13，MetaGPT 依赖可能无法安装；建议使用单独的 `metagpt` 虚拟环境或 conda 环境。

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
> 本仓库当前默认通过 `metagpt_team/run_team.py` 使用 MetaGPT SDK；旧版 `metagpt_tools/run.py` 只用于兼容老流程。

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

### 2.A 使用场景与边界（非常重要）

MetaGPT 在程序员群体里的最佳定位，不是“全自动交付系统”，而是“研发加速器 + 文档与样板代码生产器”。

更适合的场景：

- 快速做 PoC / Demo
- 新项目脚手架和样板代码生成
- 需求到设计文档初稿产出（PRD、架构草图、任务拆分、测试计划）
- CRUD 型、规则清晰的中低复杂模块
- 测试骨架、接口契约、Mock 数据生成
- 旧系统重构前的方案探索与多版本对比
- 团队规范落地（通过任务模板约束目录结构、技术栈、编码风格）

不适合直接依赖的场景：

- 高复杂领域逻辑（交易一致性、计费、风控）
- 强性能/高并发/低延迟核心链路
- 高安全与强合规系统
- 跨系统复杂状态机、分布式事务、灰度回滚
- 期望“一次生成即可上线”

推荐落地方法：

1. 把任务拆小，按模块/按层/按接口分批生成。
2. 先生成文档和测试，再生成实现代码。
3. 在任务中写清楚“必须遵守项”和“禁止项”。
4. 固定流程：生成 -> 自动测试 -> 人工 review -> 修复循环。
5. 把 MetaGPT 输出视为 80 分初稿，由工程师完成最后 20 分工程化。

简要结论：

- 你的判断是对的：MetaGPT 非常适合快速研发 demo。
- 进一步说，它也适合中小功能的第一版骨架与文档资产生产。

### 2.0 新增需求 / 修改需求时的推荐顺序

1. 先修改任务文件和上下文文档。
2. 先执行 `--dry-run`，不要直接 `--overwrite`。
3. 检查最新 `metagpt_outputs/<timestamp>/SUMMARY.md`。
4. 再检查 `IMPL_RAW.md`，确认没有漂回旧的 MySQL/JPA/扁平包结构。
5. 确认没问题后，再执行 `--overwrite`。
6. 最后做本地构建和启动验证。

### 2.1 仓库上下文文件

MetaGPT 调用时，本仓库会自动把以下上下文文件拼接成 prompt 喂给模型，以减少"胡编"和返工：

| 文件 | 说明 |
|------|------|
| `PROJECT_CONTEXT.md` | 项目目标、阶段计划 |
| `docs/LOCAL_IMPL_BASELINE.md` | 当前本地默认实现基线，高优先级 |
| `docs/ARCHITECTURE.md` | 架构与模块设计 |
| `docs/API.md` | 接口草案 |
| `docs/PORTS.md` | 端口约定 |
| `docs/ROCKETMQ_TOPICS.md` | MQ topic/tag 约定（集成阶段参考，不是本地启动前置） |
| `docs/REDIS_KEYS.md` | Redis key/TTL 约定（集成阶段参考，不是本地启动前置） |
| `docs/INFRA.md` | 基础设施说明 |

上下文文件列表维护在 `metagpt_tools/context_files.txt`，可按需增删。

当前默认本地方向已统一为：H2 + MyBatis + Vue 3/Vite。
对齐状态可查看 [METAGPT_ALIGNMENT_STATUS.md](METAGPT_ALIGNMENT_STATUS.md)。

### 2.2 推荐工作流：需求 → 生成 → 验证 → 回写

```
需求文本
    │
    ▼
整理 prompt（上下文 + 任务）
    │
    ▼
metagpt_team/run_team.py  ──→  metagpt_outputs/<timestamp>/
    │                               ├── prompt.txt    （完整 prompt 存档）
    │                               ├── IMPL_RAW.md / 文档产物
    │                               └── SUMMARY.md    （结果摘要）
    │
    ▼
人工 review 产物
    │
    ├── plan 模式：review 文档，再决定是否进入 impl
    │
    └── impl 模式：review 已写入的 server/ 与 web/ 文件
```

### 2.3 任务拆分建议

把需求拆成"可一次提交"的最小单元，例如：

- 仅新增一个 API + MyBatis mapper + H2 初始化脚本变更
- 仅补齐一个文档（并同步 README 引用）
- 仅增加一个脚本（并在 `docs/` 中说明用法）

这样更容易 review 与回滚。

### 2.4 两种模式

- `plan`：生成 PRD / Architecture / QA / Dev Plan / Skeleton 文档，不改源码。
- `impl`：要求模型按 ` ```file path=... ` 格式输出，并自动写入 `server/` 与 `web/`。

建议流程：先 `plan`，确认方向后再 `impl`。

### 2.5 输出目录约定

MetaGPT 输出统一放在：

```
metagpt_outputs/<YYYYMMDD_HHMMSS>/
```

目录已加入 `.gitignore`，不会随意提交到仓库。确认产物后再手动复制到对应目录提交。

---

## 3. 最小可运行示例

### 3.1 直接生成实现代码（推荐）

```bash
conda activate metagpt
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite
```

建议在 overwrite 阶段开启自动校验修复：

```bash
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

若不切换环境，也可以指定解释器：

```bash
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

### 3.2 先只生成规划文档

```bash
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode plan
```

### 3.3 前端单独生成

```bash
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --overwrite
```

### 3.4 直接调用 Python（更灵活）

```bash
export OPENAI_API_KEY="your-api-key-here"
python3 -m metagpt_team.run_team --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

若只想预览将要写入哪些文件：

```bash
export OPENAI_API_KEY="your-api-key-here"
python3 -m metagpt_team.run_team --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --dry-run
```

---

## 4. 脚本与文件说明

| 文件 | 作用 |
|------|------|
| `scripts/metagpt_run.sh` | 推荐入口，转发到 `metagpt_team/metagpt_team_run.sh` |
| `metagpt_team/metagpt_team_run.sh` | Shell runner，支持 `plan` / `impl` / `--dry-run` / `--overwrite` |
| `metagpt_team/run_team.py` | Python 入口，拼接上下文、调用 MetaGPT SDK、写入产物或源码 |
| `metagpt_tools/run.py` | 旧版 CLI 包装器，保留兼容 |
| `metagpt_tools/context_files.txt` | 上下文文件列表（每行一个相对于仓库根目录的路径） |
| `metagpt_tools/requirements.txt` | Python 依赖（仅包装层依赖，MetaGPT 本身需单独安装） |

---

## 5. 常见问题

### 5.1 为什么跑不起来？

- 检查 Python 版本与依赖：`python --version`（需 3.10+），`pip install -r metagpt_tools/requirements.txt`
- 检查 MetaGPT 是否已安装，并确认 Python 环境能导入 `metagpt`
- 检查 API Key 是否正确设置：`echo $OPENAI_API_KEY`
- 如果 SDK 跑不通，检查 MetaGPT 配置文件中的模型名、api_key、base_url

### 5.2 应该跑哪个命令？

- 需要生成或重写 `server/`、`web/` 源码：跑 `scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2`
- 需要先出 PRD / 架构 / 测试计划：跑 `scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode plan`
- 只生成前端：跑 `scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --overwrite`

当前仓库已验证的本地实现基线：
- 后端默认使用 H2 + MyBatis
- `/api/health` 与 `/api/products` 必须可在无 MySQL 场景下启动并返回数据
- Redis 与 RocketMQ 文档仍保留，但不应作为本地 impl 的必选前置

### 5.3 如何适配不同版本的 MetaGPT？

- 当前推荐 runner 是 `metagpt_team/run_team.py`，它走 SDK 路径。
- 如果你的 MetaGPT 版本 API 变化，优先调整 `metagpt_team/roles.py` 中的 LLM 调用适配层。
- `metagpt_tools/run.py` 的 CLI 方式仅保留兼容，不建议作为主流程。
- 参考官方文档：https://github.com/geekan/MetaGPT

### 5.4 如何保证生成内容符合本仓库约定？

- 把关键约定写进 `docs/` 并加入 `metagpt_tools/context_files.txt`
- 对生成结果做 review：尤其是本地启动基线是否仍为 H2 + MyBatis，以及端口、topic/tag、Redis key、DB 账号密码等
- 生产环境配置不要写死：一定使用环境变量或配置文件

### 5.5 输出在哪里？

默认输出到 `metagpt_outputs/<YYYYMMDD_HHMMSS>/`，该目录在 `.gitignore` 中，不会自动提交。

补充说明：
- 若 `impl` 模式运行结束后，`SUMMARY.md` 显示 `Generated 0 source file(s)`，先检查 `IMPL_RAW.md`。
- 如果 `IMPL_RAW.md` 中最后一个 ` ```file path=... ` 块没有闭合，说明模型输出被截断，不是 writer 逻辑错误。
- 如果 `IMPL_RAW.md` 已经完整，但目标文件原本就存在，而你没有加 `--overwrite`，dry-run 也会跳过这些文件，`SUMMARY.md` 仍可能显示 0。
- 遇到这种情况，优先切换为 `deepseek-chat`，或把任务拆小，比如先用 `metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md` 单独生成前端。

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
- [基础设施说明](INFRA.md)
- [MetaGPT Team 用法](METAGPT_TEAM_USAGE.md)
- [README（项目主页）](../README.md)
- [MetaGPT 官方文档](https://github.com/geekan/MetaGPT)
