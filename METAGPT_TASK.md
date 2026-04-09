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