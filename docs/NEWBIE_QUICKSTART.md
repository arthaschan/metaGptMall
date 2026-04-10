# 新手快速上手

本文档面向第一次接触本仓库的人，目标是两件事：

1. 把本地环境跑起来。
2. 知道在“新增需求”或“修改需求”后，应该如何启动 MetaGPT 脚本。

## 1. 先理解当前默认基线

当前仓库默认分成两层：

- 本地默认可运行基线：
  - 后端：Spring Boot 3.x + H2 + MyBatis
  - 前端：Vue 3 + Vite
  - 默认本地不要求 MySQL、Redis、RocketMQ
- 后续集成阶段：
  - MySQL
  - Redis
  - RocketMQ

如果你只是想在本机跑通、调试、改需求、重新生成代码，请优先按“本地默认基线”执行。

## 2. 一次性环境准备

### 2.1 安装基础工具

需要这些工具：

- Git
- Java 17
- Maven 3.9+
- Node.js 18+
- npm
- Conda 或 Python 3.11 虚拟环境

本仓库已验证：

- Python 3.11 可用
- Python 3.13 作为 base 环境时，MetaGPT 依赖容易出问题

### 2.2 准备 MetaGPT 环境

推荐做法：使用单独的 `metagpt` 环境。

示例：

```bash
conda create -n metagpt python=3.11 -y
conda activate metagpt
```

### 2.3 安装固定版本 MetaGPT

```bash
git clone https://github.com/FoundationAgents/MetaGPT.git
cd MetaGPT
git checkout 11cdf466d042aece04fc6cfd13b28e1a70341b1f
pip install -U pip
pip install -e .
```

### 2.4 安装本仓库 runner 依赖

在本仓库根目录执行：

```bash
cd /Users/arthas/git/metaGptMall
pip install -r metagpt_team/requirements.txt
```

### 2.5 配置模型

不要把真实 key 写进本仓库。

编辑你本机 MetaGPT 仓库中的配置文件：

`MetaGPT/config/config2.yaml`

DeepSeek 示例：

```yaml
llm:
  api_type: "openai"
  model: "deepseek-chat"
  base_url: "https://api.deepseek.com/v1"
  api_key: "<YOUR_DEEPSEEK_API_KEY>"
```

说明：

- `impl` 模式更推荐 `deepseek-chat`
- `deepseek-reasoner` 更容易在大任务下输出截断

### 2.6 可选：本仓库根目录放 `.env`

如果你希望脚本自动加载环境变量，可以在仓库根目录创建 `.env`：

```bash
OPENAI_API_KEY=your-key
```

这个文件不会提交到仓库。

### 2.7 关于 `OPENAI_API_KEY` 警告

运行脚本时，你可能看到这条警告：

```bash
WARN: OPENAI_API_KEY is not set in the shell; continuing and relying on MetaGPT config.
```

这不一定表示运行失败。

如果你已经在本机 MetaGPT 仓库的 `config/config2.yaml` 中正确配置了 `api_key`、`model`、`base_url`，脚本仍然可以继续工作。

只有当你既没有在 shell 中设置环境变量，也没有在 MetaGPT 配置文件中设置 key 时，才会真正无法调用模型。

## 3. 先验证本地项目能跑

### 3.1 启动后端

```bash
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml -Dmaven.test.skip=true spring-boot:run
```

成功后可验证：

- `http://localhost:8080/api/health`
- `http://localhost:8080/api/products`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/h2-console`

停止后端：

- 在当前终端按 `Ctrl+C`

### 3.2 启动前端

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run dev
```

成功后访问：

- `http://localhost:5173`

停止前端：

- 在当前终端按 `Ctrl+C`

## 4. 新增需求或修改需求后，怎么启动脚本

建议按下面顺序做。

### 4.1 先改需求文档和约束文档

最常改的文件有：

- `metagpt_tasks/baseline/METAGPT_TASK.md`
- `metagpt_tasks/password-reset/METAGPT_TASK_PASSWORD_RESET_ONLY.md`（如果需求是重置密码）
- `PROJECT_CONTEXT.md`
- `docs/LOCAL_IMPL_BASELINE.md`
- `docs/API.md`
- 需要时再改拆分任务文件：
  - `metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md`
  - `metagpt_tasks/baseline/METAGPT_TASK_SERVER_PRODUCT_ONLY.md`
  - `metagpt_tasks/baseline/METAGPT_TASK_SERVER_TESTS_ONLY.md`
  - `metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md`

原则：

- 先把约束写清楚，再启动脚本
- 如果当前目标只是本地可运行，不要把 MySQL、Redis、RocketMQ 重新写成默认前置

### 4.2 先跑 dry-run，不要直接 overwrite

完整任务：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --dry-run
```

只跑后端 core：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md --mode impl --dry-run
```

只跑商品域：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_SERVER_PRODUCT_ONLY.md --mode impl --dry-run
```

只跑前端：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --dry-run
```

### 4.3 看输出，不要只看命令是否成功

每次运行后，检查最新目录：

```bash
ls -1 metagpt_outputs | tail -n 1
```

重点看两个文件：

- `metagpt_outputs/<timestamp>/SUMMARY.md`
- `metagpt_outputs/<timestamp>/IMPL_RAW.md`

检查点：

- 是否生成了你预期的文件路径
- 是否仍然是 H2 + MyBatis
- 是否仍然是 `ProductResponse`、`ProductRepository`、`EcommerceApplication`
- `GET /api/health` 对应生成结果是否仍包含 `status`、`service`、`version`、`timestamp`
- 是否没有重新漂回 MySQL/JPA/扁平包结构

### 4.4 确认没问题后，再 overwrite

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

### 4.5 覆盖后做本地验证

后端验证：

```bash
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml test
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml -Dmaven.test.skip=true spring-boot:run
```

前端验证：

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run build
```

## 5. 常用命令速查

### 5.1 生成规划文档

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode plan
```

### 5.2 生成完整实现

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --dry-run
```

### 5.3 只生成前端

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_WEB_ONLY.md --mode impl --dry-run
```

### 5.4 只生成后端测试

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK_SERVER_TESTS_ONLY.md --mode impl --dry-run
```

### 5.5 只演练“重置密码”功能

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_PASSWORD_RESET_ONLY.md --mode impl --dry-run
```

更稳的做法是拆开执行：

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_SERVER_PASSWORD_RESET_ONLY.md --mode impl --dry-run

PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/password-reset/METAGPT_TASK_WEB_PASSWORD_RESET_ONLY.md --mode impl --dry-run
```

## 6. 推荐你继续看的文档

- `README.md`
- `README_METAGPT.md`
- `docs/METAGPT_USAGE.md`
- `docs/METAGPT_ALIGNMENT_STATUS.md`
- `docs/LOCAL_IMPL_BASELINE.md`
- `docs/CHANGE_REQUEST_EXAMPLE.md`
- `docs/requirements/password-reset/PASSWORD_RESET_REQUIREMENT.md`
- `docs/requirements/password-reset/PASSWORD_RESET_RUNBOOK.md`

## 7. 一份可照抄的真实演练

如果你希望先看一遍“真实需求变更是怎么操作的”，直接看：

- [CHANGE_REQUEST_EXAMPLE.md](CHANGE_REQUEST_EXAMPLE.md)

这份文档展示了一个实际例子：

- 先修改接口契约文档
- 再执行拆分任务 `--dry-run`
- 然后检查 `metagpt_outputs/` 里的生成结果
- 最后再决定是否执行 `--overwrite`

如果真实需求就是“用户重置密码”，直接看：

- [requirements/password-reset/PASSWORD_RESET_RUNBOOK.md](requirements/password-reset/PASSWORD_RESET_RUNBOOK.md)