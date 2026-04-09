# MetaGPT 使用指南

本项目已集成 MetaGPT 工具链，可用于生成/迭代代码与文档。

## 快速入口

```bash
export OPENAI_API_KEY="your-api-key-here"
bash scripts/metagpt_run.sh "你的需求文本"
```

## 完整文档

➡️ **[docs/METAGPT_USAGE.md](docs/METAGPT_USAGE.md)**

涵盖：
- 前置条件（Python 版本、MetaGPT 安装方式、API Key 环境变量）
- 推荐工作流（需求 → prompts → 生成 → 回写仓库 → 验证）
- 与仓库文档文件的对应关系
- 最小可运行示例
- 常见问题排查

## 文件结构

| 文件 | 说明 |
|------|------|
| `scripts/metagpt_run.sh` | 一键运行脚本 |
| `metagpt_tools/run.py` | Python 入口 |
| `metagpt_tools/context_files.txt` | 上下文文件列表 |
| `metagpt_tools/requirements.txt` | Python 依赖 |
| `docs/METAGPT_USAGE.md` | 完整使用文档 |

> 输出结果默认保存在 `metagpt_outputs/<timestamp>/`（已加入 `.gitignore`）。
