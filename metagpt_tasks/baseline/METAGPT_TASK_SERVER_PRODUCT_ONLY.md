# MetaGPT Task (impl mode, server product only)

Only generate backend product domain code under server/. Do not generate any web/ files. Do not generate tests. Do not generate pom.xml, application.yml, HealthController, or WebSecurityConfig.

Strict output format:
- Every file must use exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use other fenced block types.

Assume these already exist and should NOT be regenerated:
- server/pom.xml
- server/src/main/resources/application.yml
- server/src/main/java/com/ecommerce/EcommerceApplication.java
- server/src/main/java/com/ecommerce/common/controller/HealthController.java
- server/src/main/java/com/ecommerce/common/config/WebSecurityConfig.java

Scope:
- Implement only these product files:
  - server/src/main/java/com/ecommerce/entity/Product.java
  - server/src/main/java/com/ecommerce/repository/ProductRepository.java
  - server/src/main/java/com/ecommerce/product/dto/ProductResponse.java
  - server/src/main/java/com/ecommerce/product/service/ProductService.java
  - server/src/main/java/com/ecommerce/product/controller/ProductController.java
- Use plain Java POJOs, not Lombok.
- Align Product fields with the current local runnable contract:
  - `title`
  - `description`
  - `priceCents`
  - `currency`
  - `stock`
  - `imageUrl`
  - `active`
  - `createdAt`
  - `updatedAt`
- Local runnable baseline is H2 + MyBatis, not MySQL + JPA.
- GET /api/products returns a JSON array.
- GET /api/products/{id} returns one ProductResponse.
- Use DTO name ProductResponse.
- Use repository package com.ecommerce.repository.
- Repository interface name must be `ProductRepository`, not `ProductMapper`.
- Repository method names should prefer the current local contract:
  - `findAll()`
  - `findById(...)`
- Do not rename the list method to `findAllActive()` unless the existing repository contract has already changed.
- Product code must stay in current package layout:
  - `com.ecommerce.product.controller`
  - `com.ecommerce.product.service`
  - `com.ecommerce.product.dto`
- Repository should use annotation-based MyBatis mapper methods on the interface itself.
- Do not require XML mapper files.
- Keep package root under com.ecommerce.

Rules:
- Output only the 5 product-related server file blocks above.
- Do not output any core config file, test file, or web file.
