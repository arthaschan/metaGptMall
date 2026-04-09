# MetaGPT Task (impl mode)

Generate full runnable implementation code for this repository.

Requirements:
- Backend: Spring Boot 3.x under server/
  - Use REST API style.
  - Include pom.xml, application.yml, main application class, controllers, services, repositories/DAO, DTOs, validation, global exception handler, and minimal tests.
- Frontend: Vue 3 under web/
  - Include package.json, Vite config if needed, src/main.ts, router, API client, and at least one page that calls backend.
- Output format MUST be multiple fenced blocks:
  - ```file path=server/<path>
    <content>
    ```
  - ```file path=web/<path>
    <content>
    ```
- Only write files under server/ and web/.
- Ensure the generated code can run locally with minimal setup; include a README in server/ and/or web/ with commands.

Notes:
- Follow repository constraints and existing docs.
## 强制验收（不满足就继续生成，直到满足）
你输出的文件块必须同时满足以下条件，否则视为失败，请在同一次输出中补齐缺失文件：

A) 后端测试（必须）
- 至少生成 3 个可运行的 JUnit5 测试文件，路径必须在：
  - `server/src/test/java/...`
- 必须覆盖：
  1) Controller 层 MockMvc 测试（至少 1 个）
  2) Service 层单元测试（至少 1 个）
  3) Repository/DAO 层测试（至少 1 个，或用 @DataJpaTest / Testcontainers 均可）
- 必须提供 `server/pom.xml` 中对应 test 依赖（spring-boot-starter-test）

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
