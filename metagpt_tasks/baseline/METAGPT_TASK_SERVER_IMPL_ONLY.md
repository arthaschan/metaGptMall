# MetaGPT Task (impl mode, server implementation only)

Only generate backend Spring Boot 3.x implementation code under server/. Do not generate any web/ files. Do not generate any test files.

Strict output format:
- Every file must use exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use other code fence types.
- Do not output prose between file blocks except very short separators if necessary.

Scope:
- Implement only these backend capabilities:
  1. GET /api/health
  2. GET /api/products
  3. GET /api/products/{id}
- Use Spring Boot 3.x, Java 17, REST style.
- Keep package root under com.ecommerce.
- Keep GET endpoints publicly accessible.
- Local runnable baseline is H2 + MyBatis, not MySQL + JPA.
- Align field names with the current local product contract:
  - `title`
  - `description`
  - `priceCents`
  - `currency`
  - `stock`
  - `imageUrl`
  - `active`
  - `createdAt`
  - `updatedAt`
- Prefer plain Java POJOs over Lombok.
- `/api/products` returns a JSON array, not a paged response.
- Use DTO name `ProductResponse`, not `ProductDto`.
- Use repository package `com.ecommerce.repository`.
- Repository interface name must be `ProductRepository`, not `ProductMapper`.
- Keep current package layout:
  - `com.ecommerce.common.controller.HealthController`
  - `com.ecommerce.product.controller`
  - `com.ecommerce.product.service`
  - `com.ecommerce.product.dto`
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

Rules:
- Output only server/ file blocks.
- Do not output any server/src/test/java files.
- If a file already exists, output the full replacement content for that same path.