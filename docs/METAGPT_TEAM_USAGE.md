# MetaGPT 多角色（Team）使用说明（本项目）

本项目目标：用 **MetaGPT（FoundationAgents/MetaGPT）SDK** 以“团队分工”方式产出可 review 的文档与开发计划，然后你再手工挑选回写到仓库。

本仓库不直接提交大段自动生成业务代码（避免污染）；只提交可复用的 runner、模板与文档。

## 0. 固定版本

本仓库的 SDK runner 以以下 MetaGPT commit 为准（你已确认）：
- Repo: `FoundationAgents/MetaGPT`
- Commit: `11cdf466d042aece04fc6cfd13b28e1a70341b1f`

如果你更换 MetaGPT 版本，SDK API 可��变化，需要同步更新本 runner。

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

## 2. 配置环境变量（必须手动修改）

> 不要把任何真实 key 写进仓库。

以 OpenAI 风格为例：

```bash
export OPENAI_API_KEY="your-api-key-here"
export OPENAI_MODEL="gpt-4o-mini"  # 可选
```

你也可以使用 `.env`（确保已在 `.gitignore` 中）：

```bash
OPENAI_API_KEY=your-api-key-here
OPENAI_MODEL=gpt-4o-mini
```

## 3. 安装本仓库 runner 依赖

```bash
pip install -r metagpt_team/requirements.txt
```

## 4. 运行（多角色）

### 4.1 直接给任务文本

```bash
bash scripts/metagpt_team_run.sh "为 metaGptMall 设计下单流程的 PRD、架构、测试计划与开发任务拆分"
```

### 4.2 使用任务文件

```bash
bash scripts/metagpt_team_run.sh --task-file METAGPT_TASK.md
```

## 5. 输出

默认输出目录：

```
metagpt_outputs/<YYYYMMDD_HHMMSS>/
  PM_PRD.md
  ARCHITECTURE.md
  QA_TESTPLAN.md
  DEV_PLAN.md
  SUMMARY.md
  prompt_context.md
```

其中 `prompt_context.md` 是本次运行使用的“上下文拼接结果”，便于追溯。

## 6. 产物回写仓库（推荐流程）

1) 先在输出目录 review 文档
2) 只挑选“确定要保留”的文档/代码片段回写到仓库
3) 保持一次 commit 做一件事

## 7. 常见问题

- 如果报 `ImportError` / `AttributeError`：99% 是 MetaGPT 版本不一致。请确认 checkout 的 commit 与本文一致。
- 如果模型/Key 不生效：检查你的环境变量是否被 MetaGPT 读取（不同 provider 环境变量名不同）。
