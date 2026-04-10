# MetaGPT Task (impl mode, web password reset only)

Only generate frontend code for the password reset feature under web/. Do not generate any server/ files.

Strict output format:
- Every file must use exactly:
  ```file path=web/<relative-path>
  <full file content>
  ```
- Do not use other fenced block types.

Feature scope:
- Implement a simple reset-password page in the existing Vue 3 + Vite app.
- The page should call backend `POST /api/auth/password/reset`.

Frontend rules:
- Keep the existing Vue 3 + Vite baseline.
- Do not introduce undeclared dependencies.
- Do not use TypeScript import paths ending in `.ts`.
- Reuse the existing HTTP layer if practical.
- Keep the app structure aligned with the current repository.
- Preserve the current existing view structure and routes already in the repo.
- The current repo already has these views and should not replace them with invented alternatives:
  - `web/src/views/HealthView.vue`
  - `web/src/views/ProductListView.vue`
  - `web/src/views/ProductDetailView.vue`
- Do not invent `HomeView.vue` or `ProductsView.vue`.
- Update the existing router by adding the reset-password route, rather than replacing the app with a different route map.
- Preserve the existing App shell/menu style unless a very small navigation addition is needed.

Page requirements:
- Add `web/src/views/ResetPasswordView.vue`
- Add route `/reset-password`
- Add a navigation entry if needed in `web/src/App.vue`
- Form fields:
  - email
  - new password
  - confirm password
- Submit button
- Show success feedback on success
- Show backend error message on failure

Required files to generate or update as needed:
- web/src/router/index.ts
- web/src/App.vue
- web/src/views/ResetPasswordView.vue
- web/src/api/http.ts or a small auth API file under web/src/api/

Acceptance:
- Output only web/ file blocks
- Do not generate server/ files
- Keep existing health/product pages intact unless routing/nav integration requires small edits