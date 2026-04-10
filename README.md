# metaGptMall

本仓库是一个“电商 MVP + MetaGPT 生成工作流”项目。

当前默认本地基线不是 Docker + MySQL 起步，而是：

- 后端：Spring Boot 3.x + H2 + MyBatis
- 前端：Vue 3 + Vite
- MetaGPT runner：`scripts/metagpt_run.sh`

如果你的目标是本机先跑通前后端、修改需求、再用 MetaGPT 重新生成代码，请优先按本地基线操作，不要先走 MySQL/Redis/RocketMQ 集成流程。

## 快速入口

- 新手环境部署与脚本使用：见 [docs/NEWBIE_QUICKSTART.md](docs/NEWBIE_QUICKSTART.md)
- MetaGPT 总入口说明：见 [README_METAGPT.md](README_METAGPT.md)
- 当前文档与本地代码对齐状态：见 [docs/METAGPT_ALIGNMENT_STATUS.md](docs/METAGPT_ALIGNMENT_STATUS.md)

## 本地运行

### 启动后端

```bash
mvn -f /Users/arthas/git/metaGptMall/server/pom.xml -Dmaven.test.skip=true spring-boot:run
```

可访问：

- `http://localhost:8080/api/health`
- `http://localhost:8080/api/products`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/h2-console`

### 启动前端

```bash
cd /Users/arthas/git/metaGptMall/web
npm install
npm run dev
```

默认地址：

- `http://localhost:5173`

## MetaGPT 常用命令

### 先做 dry-run

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --dry-run
```

### 确认后再 overwrite

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode impl --overwrite --auto-fix --max-fix-rounds 2
```

### 只生成规划文档

```bash
cd /Users/arthas/git/metaGptMall
PYTHON=/opt/homebrew/anaconda3/envs/metagpt/bin/python \
bash scripts/metagpt_run.sh --task-file metagpt_tasks/baseline/METAGPT_TASK.md --mode plan

任务文件已经按主题归档：

- 通用基线任务：`metagpt_tasks/baseline/`
- 密码重置需求任务：`metagpt_tasks/password-reset/`
- 需求文档：`docs/requirements/`
```

## 何时再看集成基础设施

当你准备切到 Docker / MySQL / Redis / RocketMQ 集成阶段时，再看这些文档：

- [docs/INFRA.md](docs/INFRA.md)
- [docs/PORTS.md](docs/PORTS.md)
- [docs/REDIS_KEYS.md](docs/REDIS_KEYS.md)
- [docs/ROCKETMQ_TOPICS.md](docs/ROCKETMQ_TOPICS.md)
