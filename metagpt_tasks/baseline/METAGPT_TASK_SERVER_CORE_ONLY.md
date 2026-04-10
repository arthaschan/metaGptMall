# MetaGPT Task (impl mode, server core only)

Only generate backend core infrastructure code under server/. Do not generate any web/ files. Do not generate any tests. Do not generate product domain files.

Strict output format:
- Every file must use exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use other fenced block types.

Scope:
- Implement only these backend core files and nothing else:
  - server/pom.xml
  - server/src/main/resources/application.yml
  - server/src/main/java/com/ecommerce/EcommerceApplication.java
  - server/src/main/java/com/ecommerce/common/controller/HealthController.java
  - server/src/main/java/com/ecommerce/common/config/WebSecurityConfig.java
- GET /api/health must be publicly accessible.
- GET /api/health must return a JSON object containing at least `status`, `service`, `version`, and `timestamp`.
- Use Spring Boot 3.x, Java 17.
- Keep package root under com.ecommerce.
- Keep the application class name as `EcommerceApplication`.
- Keep health controller under `com.ecommerce.common.controller`.
- Keep config style aligned with existing `com.ecommerce.common.config` package.
- Do not invent parallel config classes such as `com.ecommerce.config.WebConfig`.
- Local runnable baseline is H2 + MyBatis, not MySQL + JPA.
- Do not generate entity, repository, product service, product controller, DTO, or test files.
- Keep server port 8080 and health path /api/health.
- Keep Swagger/OpenAPI path aligned with repo docs.

Rules:
- Output only the 5 server file blocks above.
- If a file already exists, output the full replacement content for that same path.
