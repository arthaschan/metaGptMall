# MetaGPT Task (impl mode, password reset only)

Only generate the minimum runnable implementation for the user password reset feature.

Strict output format:
- Every file must be emitted as:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Or:
  ```file path=web/<relative-path>
  <full file content>
  ```
- Do not use other fenced block types.

Feature scope:
- Implement a local runnable password reset feature without email/token infrastructure.
- Backend local baseline must stay H2 + MyBatis, not MySQL + JPA.
- Frontend local baseline must stay Vue 3 + Vite.

Backend contract:
- Public endpoint: `POST /api/auth/password/reset`
- Request body fields:
  - `email`
  - `newPassword`
  - `confirmPassword`
- Validation rules:
  - email must be valid
  - newPassword length >= 8
  - confirmPassword must equal newPassword
- Success response JSON should include at least:
  - `message`
  - `email`
- Password must be stored as hash, never plain text.

Backend package/layout rules:
- application class: `com.ecommerce.EcommerceApplication`
- config package stays under `com.ecommerce.common.config`
- auth packages should use:
  - `com.ecommerce.auth.controller`
  - `com.ecommerce.auth.service`
  - `com.ecommerce.auth.dto`
- repository package: `com.ecommerce.repository`
- repository interface name should be `UserRepository`
- Prefer annotation-based MyBatis mapper methods on the interface itself.
- Do not require XML mapper files.
- Do not invent `com.ecommerce.controller` or `com.ecommerce.service` flat packages.

Schema/data rules:
- Update local H2 initialization files if needed:
  - `server/src/main/resources/schema.sql`
  - `server/src/main/resources/data.sql`
- Add a `users` table compatible with:
  - `id`
  - `email`
  - `password_hash`
  - `role`
  - `created_at`
- Seed at least one demo user.

Security rules:
- Keep existing `/api/health` and product public rules intact.
- Also allow anonymous access to `POST /api/auth/password/reset`.
- Do not introduce JWT refresh/login platform work unless strictly required by this feature.

Frontend rules:
- Add a simple reset-password page to the existing Vue app.
- Page should include:
  - email input
  - new password input
  - confirm password input
  - submit button
- Submit to `/api/auth/password/reset` through the existing HTTP layer or a small auth API wrapper.
- Show success and error feedback in the UI.
- Do not introduce undeclared dependencies.
- Do not use `.ts` suffix in imports.

Required backend files to generate or update as needed:
- server/src/main/resources/schema.sql
- server/src/main/resources/data.sql
- server/src/main/java/com/ecommerce/repository/UserRepository.java
- server/src/main/java/com/ecommerce/auth/dto/PasswordResetRequest.java
- server/src/main/java/com/ecommerce/auth/dto/PasswordResetResponse.java
- server/src/main/java/com/ecommerce/auth/service/PasswordResetService.java
- server/src/main/java/com/ecommerce/auth/controller/AuthController.java
- server/src/main/java/com/ecommerce/common/config/WebSecurityConfig.java

Required frontend files to generate or update as needed:
- web/src/router/index.ts
- web/src/App.vue
- web/src/views/ResetPasswordView.vue
- web/src/api/http.ts or a small auth API file under web/src/api/

Tests are required:
- At least 3 runnable test files under `server/src/test/java/...`
- Must include:
  1. controller MockMvc test for password reset
  2. service unit test for password reset
  3. repository/DAO test for `UserRepository`
- Do not generate Redis or RocketMQ tests.
- Do not assume JPA repositories.

Acceptance:
- Output only the file blocks relevant to this feature.
- Keep the current local runnable baseline intact.
- Do not rewrite unrelated product files unless necessary for compilation or routing integration.