# MetaGPT 多角色（Team）使用说明（本项目）

本项目目标：用 **MetaGPT（FoundationAgents/MetaGPT）SDK** 以“团队分工”方式产出可 review 的文档与开发计划，然后你再手工挑选回写到仓库。

第一次部署环境或第一次跑脚本，先看：

- [NEWBIE_QUICKSTART.md](NEWBIE_QUICKSTART.md)
- [METAGPT_ALIGNMENT_STATUS.md](METAGPT_ALIGNMENT_STATUS.md)

本仓库不直接提交大段自动生成业务代码（避免污染）；只提交可复用的 runner、模板与文档。

## 0. 固定版本

本仓库的 SDK runner 以以下 MetaGPT commit 为准（你已确认）：
- Repo: `FoundationAgents/MetaGPT`
- Commit: `11cdf466d042aece04fc6cfd13b28e1a70341b1f`

如果你更换 MetaGPT 版本，SDK API 可能变化，需要同步更新本 runner。

## 1. 源码安装 MetaGPT（方式 2）

```bash
git clone https://github.com/FoundationAgents/MetaGPT.git
cd MetaGPT
git checkout 11cdf466d042aece04fc6cfd13b28e1a70341b1f

# 建议虚拟环境
python -m venv .venv
source .venv/bin/activate

pip install -U pip
pip install -e .
```

如果本机已有单独环境，例如 conda 环境 `metagpt`，优先在该环境中运行本仓库 runner。
已验证 Python 3.13 作为 base 环境时，MetaGPT 依赖可能安装失败。

## 2. 配置 LLM（推荐改 MetaGPT 的 config2.yaml）

> 不要把任何真实 key 写进 **metaGptMall** 仓库。

MetaGPT 这一版会读取 `MetaGPT/config/config2.yaml`（你的本机 MetaGPT clone 里）。常见位置示例：
- macOS: `/Users/<you>/git/MetaGPT/config/config2.yaml`

### 2.1 使用 OpenAI

在 `config2.yaml` 中配置（示例）：

```yaml
llm:
  api_type: "openai"
  model: "gpt-4o-mini"
  base_url: "https://api.openai.com/v1"
  api_key: "<YOUR_OPENAI_API_KEY>"
```

### 2.2 使用国产模型（DeepSeek）

DeepSeek 提供 OpenAI-compatible 接口，因此仍使用 `api_type: "openai"`，仅替换 `base_url / model / api_key`。

建议区分用途：
- `plan` 模式：可优先尝试 `deepseek-reasoner`
- `impl` 模式：优先使用 `deepseek-chat`

原因：`impl` 模式需要稳定输出大量闭合的 ` ```file path=... ` 代码块。
本仓库已验证一次：在 `deepseek-reasoner` 下跑大型 impl 任务时，模型可能中途截断输出，导致最后一个 file block 未闭合，`impl_writer` 会因此写入 0 个文件。

```yaml
llm:
  api_type: "openai"
  model: "deepseek-chat"
  base_url: "https://api.deepseek.com/v1"
  api_key: "<YOUR_DEEPSEEK_API_KEY>"
```

如果出现以下报错：
- `not a chat model` / `v1/chat/completions endpoint`：说明你填了 instruct/completions-only 模型，请改为 chat 模型（如 `deepseek-chat` / `gpt-4o-mini`）。
- `model_not_found`：模型名不可用或无权限。
- `insufficient_quota`：额度不足（建议换更便宜模型如 `gpt-4o-mini`，或检查计费）。
- `IMPL_RAW.md` 中只有少量文件块，且最后一个文件块未闭合：通常不是 writer 出错，而是模型输出被截断。优先改用 `deepseek-chat`，或把任务拆小后重试。

## 3. 安装本仓库 runner 依赖

```bash
pip install -r metagpt_team/requirements.txt
```

## 4. 运行（多角色）

建议用 `-m` 方式运行，避免 `ModuleNotFoundError: metagpt_team`：

```bash
conda activate metagpt
python3 -m metagpt_team.run_team "为 metaGptMall 设计下单流程的 PRD、架构、测试计划与开发任务拆分"
```

也可以用任务文件：

```bash
conda activate metagpt
python3 -m metagpt_team.run_team --task-file metagpt_tasks/baseline/METAGPT_TASK.md
```

> 说明：当前最推荐的入口是 `scripts/metagpt_run.sh`，它会转发到 `metagpt_team/metagpt_team_run.sh`。

需求新增或变更时，推荐固定流程：

1. 先改任务文件和上下文文档。
2. 先跑 `--dry-run`。
3. 检查 `metagpt_outputs/<timestamp>/SUMMARY.md` 与 `IMPL_RAW.md`。
4. 确认后再跑 `--overwrite`。

## 4b. 运行 impl 模式（生成可运行代码）

使用 `--mode impl` 标志，runner 将调用 LLM 生成完整、可运行的实现代码（Spring Boot 3.x 后端 + Vue 3 前端），并自动将文件写入仓库的 `server/` 和 `web/` 目录。

当前仓库的本地 runnable baseline 已验证为：
- backend: H2 + MyBatis
- frontend: Vue 3 + Vite
- local startup should not depend on MySQL / Redis / RocketMQ

如果使用 DeepSeek，建议先把任务拆成更小单元，例如：
- 只生成后端 core（H2 配置、健康检查、安全配置）
- 只生成后端 products（MyBatis mapper、service、controller、schema/data）
- 只生成 web/
- 只生成 tests

不要一开始就让模型同时重写完整后端、前端和测试，尤其是在 `deepseek-reasoner` 下。

```bash
# 通过命令行任务字符串
python3 -m metagpt_team.run_team --mode impl "实现 metaGptMall 的下单流程：后端 REST API + 前端 Vue3 页面"

# 通过任务文件
python3 -m metagpt_team.run_team --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl
```

也可以用 Shell 脚本：

```bash
bash scripts/metagpt_run.sh --mode impl "实现下单流程"
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

### LLM 输出格式

impl 模式要求 LLM 以以下格式输出每个文件：

````
```file path=server/src/main/java/com/example/mall/order/OrderController.java
// Java source
```

```file path=web/src/views/OrderView.vue
<!-- Vue SFC -->
```
````

- 路径必须以 `server/` 或 `web/` 开头
- runner 会解析所有 ```` ```file path=... ```` 块，并将文件写入仓库对应路径
- 原始 LLM 输出保存在 `metagpt_outputs/<timestamp>/IMPL_RAW.md` 以备审阅

## 4c. 只生成前端（web/）的可靠方式（推荐）

当你发现 impl 模式没有生成 `web/`（即 `IMPL_RAW.md` 中没有 ` ```file path=web/... ` 块）时，推荐使用“前端专用任务文件”来强制 LLM 输出可解析的 web 文件块。

### 4c.1 准备任务文件：metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md

任务文件必须包含**闭合**的 file block 示例，并明确禁止输出 ```vue / ```ts 等其它代码块，否则 writer 无法落盘。

最小模板（示例，按需改任务内容即可）：

```text
只生成前端 Vue 3 项目代码，写入 web/ 目录；不要生成任何 server/ 文件。

强制输出格式（非常重要）：你必须对每个文件使用且只使用如下 fenced block 格式输出，否则程序不会写入磁盘：

```file path=web/<relative-path>
<full file content>
```

禁止输出 ```vue / ```ts / ```js 等其它类型代码块；禁止用 Markdown 标题包裹文件内容；禁止省略任何必须文件。

必须生成的文件清单（每个都要输出一个 ` ```file path=... ` 块）：
- web/package.json
- web/vite.config.ts
- web/index.html
- web/src/main.ts
- web/src/App.vue
- web/src/router/index.ts
- web/src/api/http.ts
- web/src/views/HealthView.vue
- web/README.md

功能要求：
- Vue 3（Composition API）+ Vite
- HealthView.vue 调用 GET /api/health 并展示结果
- 本地联调：vite proxy 或 README 说明 CORS（二选一）
```

> 提示：在终端创建包含 ``` 的任务文件，建议用 heredoc，避免引号/转义问题：
>
> ```bash
> mkdir -p metagpt_tasks/baseline
> cat > metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md <<'EOF'
> ...（内容里可以安全包含 ```file path=... ```）...
> EOF
> ```

### 4c.2 运行生成前端（写入 web/）

```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode impl --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --overwrite

# 或使用推荐包装脚本
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --overwrite
```

### 4c.3 先 dry-run 预览（不落盘）

```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode impl --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --dry-run

# 或使用推荐包装脚本
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --dry-run
```

注意：`--dry-run` 仍然会检查目标文件是否已存在。
如果对应路径在仓库里已经存在，而你又没有加 `--overwrite`，日志和 `SUMMARY.md` 可能显示 `Generated 0 source file(s)`。
这不一定表示模型没生成内容；要同时检查 `IMPL_RAW.md` 中是否已经出现完整闭合的 ` ```file path=... ` 块。

### 4c.4 排查：为什么没有生成 web/

第一入口：检查原始输出是否包含 `web/` 的 file blocks：

```bash
ls -1 metagpt_outputs | tail -n 1
# 假设最新目录是 metagpt_outputs/20260410_153000（自行替换）
grep -n "```file path=web/" metagpt_outputs/20260410_153000/IMPL_RAW.md | head -n 50
```

- 如果为空：说明 LLM 没按要求输出 `web/` file blocks（常见原因是它输出成了 ```vue / ```ts 代码块）；请强化任务文件里的“禁止其它代码块 + 必须输出文件清���”。
- 如果 `IMPL_RAW.md` 在某个文件中途直接结束，且没有闭合的 ```：说明响应被模型或上游接口截断。此时即使日志显示 dry-run 成功，`SUMMARY.md` 仍可能是 `Generated 0 source file(s)`。
- 如果不为空但仍未写入：请查看运行日志中的 `[impl_writer] SKIP ...` 提示，通常是路径不合法（不是以 `web/` 开头）或文件已存在且未开启 `--overwrite`。
## 5. 输出

### plan 模式（默认）

```
metagpt_outputs/<YYYYMMDD_HHMMSS>/
  PM_PRD.md
  ARCHITECTURE.md
  QA_TESTPLAN.md
  DEV_PLAN.md
  CODE_SKELETON.md
  SUMMARY.md
  prompt_context.md
```

其中 `prompt_context.md` 是本次运行使用的“上下文拼接结果”，便于追溯。

### impl 模式

```
metagpt_outputs/<YYYYMMDD_HHMMSS>/
  IMPL_RAW.md       ← 原始 LLM 输出（含所有 ```file ... ``` 块）
  SUMMARY.md        ← 写入文件列表摘要
  prompt_context.md

server/             ← 后端 Spring Boot 3.x 源码（由 LLM 生成并写入）
web/                ← 前端 Vue 3 源码（由 LLM 生成并写入）
```

## 6. latest 快照（可提交到仓库）

运行结束后，runner 会把关键产物同步覆盖到：
- `metagpt_artifacts/latest/`

该目录用于在仓库中保留“最新一版可 review 的文档快照”。

## 7. 常见问题

- 如果报 `ImportError` / `AttributeError`：99% 是 MetaGPT 版本不一致。请确认 checkout 的 commit 与本文一致。
- 如果模型/Key 不生效：优先检查 `MetaGPT/config/config2.yaml` 是否仍是 `YOUR_API_KEY`、以及 `model/base_url` 是否正确。
## 8. 命令速查（从生成文档到生成代码）

> 目标：你能快速记住“生成说明文档（plan）/生成后端+前端代码（impl）/如何运行验证”的命令。

### 8.1 一次性准备（只做一次）

#### A) 安装 MetaGPT（固定 commit）
```bash
git clone https://github.com/FoundationAgents/MetaGPT.git
cd MetaGPT
git checkout 11cdf466d042aece04fc6cfd13b28e1a70341b1f

python -m venv .venv
source .venv/bin/activate
pip install -U pip
pip install -e .
```

#### B) 配置 DeepSeek（OpenAI-compatible）
编辑（在 MetaGPT 仓库里��：
`MetaGPT/config/config2.yaml`

示例：
```yaml
llm:
  api_type: "openai"
  model: "deepseek-reasoner"
  base_url: "https://api.deepseek.com/v1"
  api_key: "<YOUR_DEEPSEEK_API_KEY>"
```

> 注意：不要把任何真实 key 提交到 `metaGptMall` 仓库。

#### C) 安装本仓库 runner 依赖
在 `metaGptMall` 仓库根目录：
```bash
cd /Users/arthas/git/metaGptMall
pip install -r metagpt_team/requirements.txt
```

---

### 8.2 生成“说明文档/计划”（plan 模式：默认）

#### A) 直接给任务文本
```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode plan "为 metaGptMall 设计下单流程：输出 PRD/架构/测试计划/开发计划/代码骨架"
```

#### B) 使用任务文件
```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode plan --task-file metagpt_tasks/baseline/METAGPT_TASK.md
```

输出目录：
- 原始输出：`metagpt_outputs/<timestamp>/`
- 同步快照：`metagpt_artifacts/latest/`

---

### 8.3 生成“可运行后端+前端代码”（impl 模式：写入 server/ + web/）

#### A) 使用任务文件（推荐）
```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode impl --task-file metagpt_tasks/baseline/METAGPT_TASK.md
```

#### B) 只预览会写哪些文件（不落盘）
```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode impl --task-file metagpt_tasks/baseline/METAGPT_TASK.md --dry-run
```

#### C) 允许覆盖已有文件（第二次生成/修复时常用）
```bash
cd /Users/arthas/git/metaGptMall
python3 -m metagpt_team.run_team --mode impl --task-file metagpt_tasks/baseline/METAGPT_TASK.md --overwrite --auto-fix --max-fix-rounds 2
```

排查“为什么没生成前端/没生成测试”的第一入口：
- 看原始输出是否有 `file path=web/...` 和 `file path=server/src/test/...`
```bash
ls -1 metagpt_outputs | tail -n 3
sed -n '1,200p' metagpt_outputs/<最新时间戳>/IMPL_RAW.md
grep -n "```file path=" metagpt_outputs/<最新时间戳>/IMPL_RAW.md | head
```

---

### 8.4 运行后端（Spring Boot 3）

```bash
cd /Users/arthas/git/metaGptMall/server
mvn test
mvn spring-boot:run
```

---

### 8.5 运行前端（Vue 3）

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run dev
```

> 以 `web/README.md`（生成后会有）为准，若使用 pnpm/yarn 请按 README 替换命令。
