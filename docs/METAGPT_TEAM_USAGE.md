# MetaGPT 多角色（Team）使用说明（本项目）

本项目目标：用 **MetaGPT（FoundationAgents/MetaGPT）SDK** 以“团队分工”方式产出可 review 的文档与开发计划，然后你再手工挑选回写到仓库。

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

### 2.2 使用国产模型（DeepSeek，推荐：deepseek-reasoner）

DeepSeek 提供 OpenAI-compatible 接口，因此仍使用 `api_type: "openai"`，仅替换 `base_url / model / api_key`。

```yaml
llm:
  api_type: "openai"
  model: "deepseek-reasoner"   # 若报错可降级为 deepseek-chat
  base_url: "https://api.deepseek.com/v1"
  api_key: "<YOUR_DEEPSEEK_API_KEY>"
```

如果出现以下报错：
- `not a chat model` / `v1/chat/completions endpoint`：说明你填了 instruct/completions-only 模型，请改为 chat 模型（如 `deepseek-chat` / `gpt-4o-mini`）。
- `model_not_found`：模型名不可用或无权限。
- `insufficient_quota`：额度不足（建议换更便宜模型如 `gpt-4o-mini`，或检查计费）。

## 3. 安装本仓库 runner 依赖

```bash
pip install -r metagpt_team/requirements.txt
```

## 4. 运行（多角色）

建议用 `-m` 方式运行，避免 `ModuleNotFoundError: metagpt_team`：

```bash
python3 -m metagpt_team.run_team "为 metaGptMall 设计下单流程的 PRD、架构、测试计划与开发任务拆分"
```

也可以用任务文件：

```bash
python3 -m metagpt_team.run_team --task-file METAGPT_TASK.md
```

> 说明：仓库里目前的脚本文件路径是 `metagpt_team/metagpt_team_run.sh`（不是 `scripts/metagpt_team_run.sh`）。

## 5. 输出

默认输出目录：

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

## 6. latest 快照（可提交到仓库）

运行结束后，runner 会把关键产物同步覆盖到：
- `metagpt_artifacts/latest/`

该目录用于在仓库中保留“最新一版可 review 的文档快照”。

## 7. 常见问题

- 如果报 `ImportError` / `AttributeError`：99% 是 MetaGPT 版本不一致。请确认 checkout 的 commit 与本文一致。
- 如果模型/Key 不生效：优先检查 `MetaGPT/config/config2.yaml` 是否仍是 `YOUR_API_KEY`、以及 `model/base_url` 是否正确。