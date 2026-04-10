# MetaGPT Task (impl mode, server only)

Only generate backend Spring Boot 3.x code under server/. Do not generate any web/ files.

Strict output format:
- Every file must be emitted as a fenced block using exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use ```java or other fenced block types.
- Do not output partial snippets. Output full file contents only.

Scope constraints:
- Implement only the minimum runnable backend for these capabilities:
  1. GET /api/health
  2. GET /api/products
  3. GET /api/products/{id}
- Use Spring Boot 3.x, Java 17, REST style.
- Local runnable baseline is H2 + MyBatis, not MySQL + JPA.
- Align entity/DTO fields with the current local product contract:
  - `title`
  - `description`
  - `priceCents`
  - `currency`
  - `stock`
  - `imageUrl`
  - `active`
  - `createdAt`
  - `updatedAt`
- Do not use Lombok unless pom.xml fully configures it; plain Java POJOs are preferred.
- Keep package root under com.ecommerce.
- Keep current package layout:
  - `com.ecommerce.common.controller.HealthController`
  - `com.ecommerce.product.controller`
  - `com.ecommerce.product.service`
  - `com.ecommerce.product.dto`
  - `com.ecommerce.repository.ProductRepository`
- Do not flatten classes into `com.ecommerce.controller`, `com.ecommerce.service`, or `com.ecommerce.dto`.
- Repository name must be `ProductRepository`, not `ProductMapper`.
- Prefer annotation-based MyBatis mapper methods; do not require XML mappers.
- Keep application class name as `EcommerceApplication`.
- Prefer existing config style under `com.ecommerce.common.config`; do not invent a parallel `com.ecommerce.config.WebConfig`.

Required files:
- server/pom.xml
- server/src/main/resources/application.yml
- server/src/main/java/com/ecommerce/EcommerceApplication.java
- server/src/main/java/com/ecommerce/common/controller/HealthController.java
- server/src/main/java/com/ecommerce/common/config/WebSecurityConfig.java
- server/src/main/java/com/ecommerce/entity/Product.java
- server/src/main/java/com/ecommerce/repository/ProductRepository.java
- server/src/main/java/com/ecommerce/product/dto/ProductResponse.java
- server/src/main/java/com/ecommerce/product/service/ProductService.java
- server/src/main/java/com/ecommerce/product/controller/ProductController.java

Testing requirements:
- Also generate 2 minimal runnable tests under server/src/test/java/...
- Include:
  1. one MockMvc controller test for GET /api/health or GET /api/products
  2. one repository or service test

Acceptance rules:
- Output only server/ file blocks.
- Ensure GET endpoints are publicly accessible.
- Keep Swagger/OpenAPI path aligned with current repo docs.
- If a file already exists, output the full replacement content for that same path.