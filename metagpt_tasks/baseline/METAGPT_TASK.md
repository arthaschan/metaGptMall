# MetaGPT Task (impl mode)

Generate full runnable implementation code for this repository.

Requirements:
- Backend: Spring Boot 3.x under server/
  - Use REST API style.
  - Local runnable target must be H2 + MyBatis, not MySQL + JPA.
  - For the current runnable baseline, prioritize `/api/health`, `/api/products`, `/api/products/{id}`.
  - Local impl must not require Redis or RocketMQ to start.
  - Include pom.xml, application.yml, main application class, controllers, services, repositories/DAO, DTOs, validation, global exception handler, and minimal tests.
- Frontend: Vue 3 under web/
  - Use Vue 3 + Vite only. Do not generate React code. Do not create frontend/ or backend/ directories.
  - Include package.json, Vite config if needed, src/main.ts, router, API client, and at least one page that calls backend.
- Output format MUST be multiple fenced blocks:
  - ```file path=server/<path>
    <content>
    ```
  - ```file path=web/<path>
    <content>
    ```
- Only write files under server/ and web/.
- If a file already exists, output the full replacement content for that same path.
- Ensure the generated code can run locally with minimal setup; include a README in server/ and/or web/ with commands.

Notes:
- Follow repository constraints and existing docs.
- Repository directories are already fixed: backend code goes in server/, frontend code goes in web/.
- Prefer plain Java POJOs over Lombok unless pom.xml explicitly includes correct Lombok setup.
- Health endpoint is expected at GET /api/health and frontend should call it through /api proxy.
- Health endpoint response should stay aligned with the current local contract and include at least:
  - `status`
  - `service`
  - `version`
  - `timestamp`
- Health controller should stay under `com.ecommerce.common.controller`.
- Product read path should stay in current package layout:
  - controller: `com.ecommerce.product.controller`
  - service: `com.ecommerce.product.service`
  - dto: `com.ecommerce.product.dto`
  - repository: `com.ecommerce.repository`
- Do not flatten these classes into `com.ecommerce.controller`, `com.ecommerce.service`, or `com.ecommerce.dto`.
- Product read path should use MyBatis mapper interfaces under `com.ecommerce.repository`.
- Repository interface name should prefer the existing contract name `ProductRepository`, not invent a parallel `ProductMapper` abstraction.
- Prefer annotation-based MyBatis mappers on the interface itself.
- Do not require XML mapper files or `mapper/*.xml` as the primary implementation style for the current local baseline.
- Local backend should auto-init H2 via `schema.sql` and `data.sql` when needed.
- Do not introduce undeclared frontend dependencies such as `pinia` unless they are also added to `web/package.json` and actually used.
- Do not use TypeScript import paths ending in `.ts` inside Vue app source.
- Main application class should remain under `com.ecommerce` and should prefer the current class name `EcommerceApplication`.
- CORS/config classes should prefer existing `com.ecommerce.common.config` package.
- Do not invent a parallel `com.ecommerce.config.WebConfig` when existing CORS/security configuration classes already satisfy the local baseline.
## 强制验收（不满足就继续生成，直到满足）
你输出的文件块必须同时满足以下条件，否则视为失败，请在同一次输出中补齐缺失文件：

A) 后端测试（必须）
- 至少生成 3 个可运行的 JUnit5 测试文件，路径必须在：
  - `server/src/test/java/...`
- 必须覆盖：
  1) Controller 层 MockMvc 测试（至少 1 个）
  2) Service 层单元测试（至少 1 个）
  3) Repository/DAO 层测试（至少 1 个，基于当前 MyBatis mapper 契约，不要要求 JPA/Testcontainers）
- 必须提供 `server/pom.xml` 中对应 test 依赖（spring-boot-starter-test）
- 测试不要覆盖 Redis 或 RocketMQ。

B) 前端（必须）
- 必须生成一个可运行的 Vue3 工程到 `web/`，至少包含：
  - `web/package.json`
  - `web/vite.config.*`（如使用 Vite）
  - `web/src/main.ts`
  - `web/src/router/index.ts`
  - `web/src/api/http.ts`（axios/fetch 封装）
  - `web/src/views/HealthView.vue`（或类似页面，调用后端 /api/health 并展示结果）
- 前端必须能 `npm install && npm run dev` 启动

C) 联调（必须）
- 必须给出一种可运行跨域方案（二选一）：
  1) `web/vite.config.*` 里配置 proxy 到后端端口；或
  2) 后端 Spring Boot 配置全局 CORS
