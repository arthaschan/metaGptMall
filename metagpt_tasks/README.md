# MetaGPT Tasks

任务文件已经按主题归档：

- `baseline/`：通用基线任务，覆盖完整 impl、server 拆分、web 拆分、tests 拆分。
- `password-reset/`：用户重置密码需求的完整任务与前后端拆分任务。

使用建议：

- 通用本地基线生成：优先使用 `baseline/METAGPT_TASK.md`
- 真实密码重置需求：优先使用 `password-reset/METAGPT_TASK_SERVER_PASSWORD_RESET_ONLY.md` 和 `password-reset/METAGPT_TASK_WEB_PASSWORD_RESET_ONLY.md`

兼容说明：

- `metagpt_team/run_team.py` 现在支持按文件名自动解析 `--task-file`，但文档和脚本示例统一建议写全新路径。
