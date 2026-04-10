# MetaGPT 使用指南

本项目已集成 MetaGPT 工具链，可用于生成/迭代代码与文档。

第一次接触本仓库，建议先看：

➡️ **[docs/NEWBIE_QUICKSTART.md](docs/NEWBIE_QUICKSTART.md)**

## 快速入口

```bash
conda activate metagpt
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

如果你不想切换 conda 环境，也可以显式指定解释器：

```bash
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

默认推荐通过 impl 模式直接生成并写入 server/ 和 web/。
当前本地可运行基线是：后端使用 H2 + MyBatis，前端使用 Vue 3 + Vite。
如果任务文件仍然要求 MySQL/JPA/Redis/RocketMQ，生成结果会与当前本地代码偏离。
新增需求或修改需求后，推荐流程是：先改任务/上下文文档，再跑 `--dry-run`，检查 `metagpt_outputs/<timestamp>/SUMMARY.md` 与 `IMPL_RAW.md`，确认无偏移后再执行 `--overwrite`。
如果只想先生成规划文档，不写代码，改用：

```bash
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode plan
```

现在任务与需求文档已按目录整理：

- `metagpt_tasks/baseline/`：通用基线任务
- `metagpt_tasks/password-reset/`：密码重置需求任务
- `docs/requirements/password-reset/`：密码重置需求说明与 runbook

## 适用场景与边界

MetaGPT 在程序员群体里的最佳定位，不是“全自动交付系统”，而是“研发加速器 + 文档与样板代码生产器”。

更适合的场景：

- 快速做 PoC / Demo
- 新项目脚手架和样板代码生成
- 需求到设计文档初稿（PRD、架构草图、任务拆分、测试计划）
- CRUD 型、规则清晰的中低复杂模块
- 测试骨架、接口契约、Mock 数据生成

不适合直接依赖的场景：

- 高复杂领域逻辑（交易一致性、计费、风控）
- 强性能/高并发/低延迟核心链路
- 高安全与强合规系统
- 跨系统复杂状态机、分布式事务、灰度回滚
- 期望“一次生成即可上线”

推荐用法：

1. 把任务拆小，按模块/按层/按接口分批生成。
2. 先生成文档和测试，再生成实现代码。
3. 固定流程：生成 -> 自动测试 -> 人工 review -> 修复循环。

结论：

- 适合快速研发 demo。
- 也适合中小功能第一版骨架与文档资产生产。

## 完整文档

➡️ **[docs/METAGPT_USAGE.md](docs/METAGPT_USAGE.md)**

涵盖：
- 前置条件（Python 版本、MetaGPT 安装方式、API Key 环境变量）
- 推荐工作流（需求 → prompts → 生成 → 回写仓库 → 验证）
- 与仓库文档文件的对应关系
- 最小可运行示例
- 常见问题排查

当前文档与本地代码的对齐状态见：

➡️ **[docs/METAGPT_ALIGNMENT_STATUS.md](docs/METAGPT_ALIGNMENT_STATUS.md)**

## 文件结构

| 文件 | 说明 |
|------|------|
| `scripts/metagpt_run.sh` | 推荐入口，兼容包装脚本，内部转发到 metagpt_team runner |
| `metagpt_team/metagpt_team_run.sh` | 推荐的团队 runner，支持 `plan` / `impl` / `--overwrite` |
| `metagpt_team/run_team.py` | Python 入口，负责拼接上下文、调用 MetaGPT SDK、写入产物 |
| `metagpt_tools/context_files.txt` | 上下文文件列表 |
| `metagpt_tools/run.py` | 旧版 CLI 包装器，仅作兼容保留 |
| `metagpt_tools/requirements.txt` | Python 依赖 |
| `docs/METAGPT_USAGE.md` | 完整使用文档 |

> 输出结果默认保存在 `metagpt_outputs/<timestamp>/`（已加入 `.gitignore`）。
