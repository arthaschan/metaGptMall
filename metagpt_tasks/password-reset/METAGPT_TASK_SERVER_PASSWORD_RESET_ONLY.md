# MetaGPT Task (impl mode, server password reset only)

Only generate backend code for the password reset feature under server/. Do not generate any web/ files.

Strict output format:
- Every file must use exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use other fenced block types.

Feature scope:
- Implement the local runnable password reset feature.
- Do not implement email delivery, SMS, captcha, or token-based reset flow.
- Backend local baseline must stay H2 + MyBatis, not MySQL + JPA.

API contract:
- Public endpoint: `POST /api/auth/password/reset`
- Request fields:
  - `email`
  - `newPassword`
  - `confirmPassword`
- Success response must include at least:
  - `message`
  - `email`

Validation rules:
- email must be valid
- newPassword length >= 8
- confirmPassword must equal newPassword
- password must be stored as hash, not plain text

Package/layout rules:
- application class remains `com.ecommerce.EcommerceApplication`
- auth package should use:
  - `com.ecommerce.auth.controller`
  - `com.ecommerce.auth.service`
  - `com.ecommerce.auth.dto`
- repository package stays `com.ecommerce.repository`
- repository interface name must be `UserRepository`
- prefer annotation-based MyBatis mapper methods
- do not require XML mapper files
- config style stays under `com.ecommerce.common.config`
- prefer updating existing `WebSecurityConfig` instead of inventing parallel config classes
- preserve the existing `CorsConfig` + `WebSecurityConfig` style
- if a password encoder is needed, inject `PasswordEncoder` into the service; do not instantiate `new BCryptPasswordEncoder()` directly inside the service

Schema/data rules:
- Update `server/src/main/resources/schema.sql` to add `users` table if needed
- Update `server/src/main/resources/data.sql` to seed at least one demo user if needed
- users table fields:
  - `id`
  - `email`
  - `password_hash`
  - `role`
  - `created_at`

Security rules:
- Keep `/api/health` and product public access intact
- Allow anonymous access to `POST /api/auth/password/reset`
- Do not introduce login/JWT platform work unless strictly required by this feature
- Keep existing CORS handling intact
- Avoid rewriting unrelated security behavior such as session policy, Swagger exposure, or H2 console access unless required by compilation

Required files to generate or update as needed:
- server/src/main/resources/schema.sql
- server/src/main/resources/data.sql
- server/src/main/java/com/ecommerce/repository/UserRepository.java
- server/src/main/java/com/ecommerce/auth/dto/PasswordResetRequest.java
- server/src/main/java/com/ecommerce/auth/dto/PasswordResetResponse.java
- server/src/main/java/com/ecommerce/auth/service/PasswordResetService.java
- server/src/main/java/com/ecommerce/auth/controller/AuthController.java
- server/src/main/java/com/ecommerce/common/config/WebSecurityConfig.java

Tests are required:
- server/src/test/java/com/ecommerce/auth/controller/AuthControllerTest.java
- server/src/test/java/com/ecommerce/auth/service/PasswordResetServiceTest.java
- server/src/test/java/com/ecommerce/repository/UserRepositoryTest.java
- tests must stay aligned with MyBatis mapper assumptions, not JPA

Acceptance:
- Output only server/ file blocks
- Do not generate web/ files
- Do not rewrite unrelated product code unless necessary for compilation
