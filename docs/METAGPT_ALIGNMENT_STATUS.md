# MetaGPT 对齐状态

本文档用于记录“当前本地代码”与“MetaGPT 默认生成约束”之间的对齐状态。

## 1. 当前已对齐的内容

- 本地默认后端栈：Spring Boot 3.x + H2 + MyBatis。
- 本地默认前端栈：Vue 3 + Vite。
- 本地默认可运行链路：
  - `GET /api/health`
  - `GET /api/products`
  - `GET /api/products/{id}`
- 当前默认生成不再要求本地必须依赖 MySQL、Redis、RocketMQ。
- 商品 DTO 契约已统一为 `ProductResponse`。
- 健康检查默认响应契约已统一为至少包含：
  - `status`
  - `service`
  - `version`
  - `timestamp`
- 商品字段契约已统一为：
  - `title`
  - `description`
  - `priceCents`
  - `currency`
  - `stock`
  - `imageUrl`
  - `active`
  - `createdAt`
  - `updatedAt`
- 包结构约束已统一为：
  - `com.ecommerce.common.controller`
  - `com.ecommerce.common.config`
  - `com.ecommerce.product.controller`
  - `com.ecommerce.product.service`
  - `com.ecommerce.product.dto`
  - `com.ecommerce.repository`
- repository 默认命名已统一为 `ProductRepository`。
- MyBatis 默认风格已统一为“注解式 mapper 接口”，而不是 XML mapper。
- 应用启动类默认命名已统一为 `EcommerceApplication`。

## 2. 已同步的关键文档

- `metagpt_tasks/baseline/METAGPT_TASK.md`
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md`
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_PRODUCT_ONLY.md`
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_ONLY.md`
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_IMPL_ONLY.md`
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_TESTS_ONLY.md`
- `PROJECT_CONTEXT.md`
- `README_METAGPT.md`
- `docs/LOCAL_IMPL_BASELINE.md`
- `docs/API.md`
- `docs/ARCHITECTURE.md`
- `docs/INFRA.md`
- `docs/PORTS.md`
- `docs/METAGPT_USAGE.md`
- `docs/METAGPT_TEAM_USAGE.md`
- `metagpt_tools/context_files.txt`

## 3. 当前 dry-run 观察结论

- MetaGPT 已基本收敛到当前本地代码结构。
- 最近几轮 dry-run 已稳定生成：
  - H2 + MyBatis 方向
  - `ProductResponse`
  - `EcommerceApplication`
  - `com.ecommerce.common.controller.HealthController`
  - `com.ecommerce.product.*`
  - `com.ecommerce.repository.ProductRepository`
- 拆分任务 `metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md` 与 `metagpt_tasks/baseline/METAGPT_TASK_SERVER_PRODUCT_ONLY.md` 也已复核到同一方向。
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_PRODUCT_ONLY.md` 最新 dry-run 已回到 `findAll()` 与 `findById(...)` 方法契约。
- `metagpt_tasks/baseline/METAGPT_TASK_SERVER_CORE_ONLY.md` 在新增健康检查响应约束后，dry-run 已生成包含 `status`、`service`、`version`、`timestamp` 的健康检查响应。
- 当前剩余偏差已从“技术栈偏差”收敛为“局部配置复用偏差”。

## 4. 当前仍可能出现的残余偏差

- 偶发额外生成 `com.ecommerce.common.config.WebConfig`。
- 偶发不完全复用现有 `CorsConfig` / `WebSecurityConfig` 组合。

这些偏差已经属于次要级别，不再影响主链路技术栈、包路径或 DTO 契约方向。

## 5. 使用建议

- 如果目标是“继续完善本地最小可运行实现”，优先使用 `metagpt_tasks/baseline/METAGPT_TASK.md` 或已同步的 server 拆分任务。
- 如果目标切换到 MySQL、Redis、RocketMQ 集成，再回看 `docs/INFRA.md`、`docs/REDIS_KEYS.md`、`docs/ROCKETMQ_TOPICS.md`。
- 每次调整任务文档后，建议至少执行一次 dry-run 再决定是否 overwrite。