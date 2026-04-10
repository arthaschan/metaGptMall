# 需求变更实操示例

本文档用一个真实、很小的需求变更，演示在本仓库里应该如何操作。

目标：让新手知道“改需求后，不是直接跑 overwrite，而是先改约束文档，再 dry-run，再检查结果”。

## 1. 本次示例需求

需求内容：

- 收紧健康检查接口 `GET /api/health` 的默认响应契约。
- 要求响应至少包含：
  - `status`
  - `service`
  - `version`
  - `timestamp`

这不是凭空设想，而是对齐当前本地实际后端响应。

## 2. 第一步：先改文档约束

这一步不要直接生成代码。

先把需求写进约束文档：

- [docs/API.md](docs/API.md)
- [docs/LOCAL_IMPL_BASELINE.md](docs/LOCAL_IMPL_BASELINE.md)
- [metagpt_tasks/baseline/METAGPT_TASK.md](../metagpt_tasks/baseline/METAGPT_TASK.md)
- [metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md](../metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md)
- [metagpt_tasks/baseline/METAGPT_TASK_SERVER_TESTS_ONLY.md](../metagpt_tasks/baseline/METAGPT_TASK_SERVER_TESTS_ONLY.md)

为什么要先改这些文件：

- `docs/API.md` 负责定义接口契约
- `docs/LOCAL_IMPL_BASELINE.md` 负责定义当前本地默认实现基线
- `metagpt_tasks/` 下的任务文件负责直接约束模型输出

如果你跳过这一步，模型仍可能按旧理解生成代码。

## 3. 第二步：执行 dry-run

本次示例使用 server core 拆分任务，因为健康检查属于 core 范围。

执行命令：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md --mode impl --dry-run
```

说明：

- 这里使用 `--dry-run`，不会覆盖仓库文件。
- 目标是先检查模型输出是否已经理解新约束。

## 4. 第三步：查看输出目录

本次实际运行生成到了：

- [metagpt_outputs/20260410_193925](metagpt_outputs/20260410_193925)

重点检查：

- [metagpt_outputs/20260410_193925/IMPL_RAW.md](metagpt_outputs/20260410_193925/IMPL_RAW.md)
- [metagpt_outputs/20260410_193925/SUMMARY.md](metagpt_outputs/20260410_193925/SUMMARY.md)

你真正要看的不是“命令 exit code 是否为 0”，而是：

- 是否生成了正确路径下的文件块
- HealthController 是否仍在 `com.ecommerce.common.controller`
- 健康检查响应是否已包含 `status`、`service`、`version`、`timestamp`

## 5. 第四步：本次示例中观察到的结果

本次 dry-run 生成的 HealthController 路径是正确的：

- `server/src/main/java/com/ecommerce/common/controller/HealthController.java`

并且响应中已经包含：

- `status`
- `service`
- `version`
- `timestamp`

说明这次需求约束已经成功影响了模型输出。

虽然模型还额外生成了 `environment`、`uptime` 等字段，但这已经属于“附加字段”，不影响我们要求的核心契约收敛。

## 6. 第五步：什么时候才执行 overwrite

只有在你确认以下几点之后，才执行 `--overwrite`：

- 路径正确
- 包结构正确
- DTO/接口契约正确
- 没有漂回 MySQL/JPA/扁平包结构
- 这次输出不会破坏现有本地可运行基线

执行覆盖示例：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md --mode impl --overwrite --auto-fix
```

## 7. 第六步：覆盖后做本地验证

覆盖后至少做这些验证：

```bash
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml test
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml -Dmaven.test.skip=true spring-boot:run
curl -sf http://localhost:8080/api/health
curl -sf http://localhost:8080/api/products
```

如果你修改的是前端，再补：

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run build
```

## 8. 新手实操模板

以后你每次有新需求，都可以复用这套模板：

1. 明确需求变化。
2. 先修改 `docs/` 与 `metagpt_tasks/` 下的相关任务文件。
3. 只跑最小相关任务的 `--dry-run`。
4. 查看最新 `metagpt_outputs/<timestamp>/IMPL_RAW.md` 和 `SUMMARY.md`。
5. 确认输出契约正确后，再执行 `--overwrite`。
6. 最后做本地编译、启动、接口验证。

## 9. 建议搭配阅读

- [docs/NEWBIE_QUICKSTART.md](docs/NEWBIE_QUICKSTART.md)
- [docs/METAGPT_ALIGNMENT_STATUS.md](docs/METAGPT_ALIGNMENT_STATUS.md)
- [README_METAGPT.md](../README_METAGPT.md)