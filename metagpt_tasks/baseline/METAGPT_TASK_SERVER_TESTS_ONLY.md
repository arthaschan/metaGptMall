# MetaGPT Task (impl mode, server tests only)

Only generate backend test code under server/src/test/java/. Do not generate any implementation files under server/src/main/java. Do not generate any web/ files.

Strict output format:
- Every file must use exactly:
  ```file path=server/<relative-path>
  <full file content>
  ```
- Do not use other code fence types.

Testing scope:
- Assume the backend already contains these runtime endpoints/components:
  1. GET /api/health
  2. GET /api/products
  3. GET /api/products/{id}
  4. ProductRepository
  5. ProductService
- Generate only minimal runnable JUnit5 tests for these.
- Match the current implementation contracts exactly:
  - Backend local startup target is H2 + MyBatis, not MySQL + JPA.
  - `GET /api/health` returns JSON, not plain text.
  - Health JSON must contain at least `status`, `service`, `version`, and `timestamp`.
  - `GET /api/products` returns a JSON array, not a paged object.
  - DTO class name is `ProductResponse`.
  - Product JSON fields are `title` and `priceCents`, not `name` and `price`.
  - Repository package is `com.ecommerce.repository.ProductRepository`.
  - ProductRepository is a MyBatis mapper interface.
  - ProductRepository uses annotation-based MyBatis methods, not XML mappers.
  - Repository method names should match the current local contract `findAll()` and `findById(...)`.
  - Service package is `com.ecommerce.product.service.ProductService`.
  - Controller package is `com.ecommerce.product.controller.ProductController`.
  - Health controller package is `com.ecommerce.common.controller.HealthController`.
  - Entity package is `com.ecommerce.entity.Product`.
  - ProductRepository currently exposes `findAll()` and `findById(...)`; do not assume JPA inheritance.
  - Product and ProductResponse should be instantiated with no-arg constructors plus setters unless the exact matching constructor already exists.
  - `ProductService#getProductById` currently throws on missing data; do not invent a 404 contract unless main code already implements it.
  - If the existing pom.xml lacks test dependencies, also output a replacement `server/pom.xml` that adds the minimal test-scoped dependencies required by generated tests.

Required files:
- server/pom.xml
- server/src/test/java/com/ecommerce/common/controller/HealthControllerTest.java
- server/src/test/java/com/ecommerce/product/controller/ProductControllerTest.java
- server/src/test/java/com/ecommerce/product/service/ProductServiceTest.java

Rules:
- Output only server/src/test/java file blocks.
- Do not output main implementation classes.
- Use MockMvc for controller tests.
- Service test may use Mockito.
- Keep tests focused and short.
- Do not invent pagination APIs, Redis tests, or RocketMQ tests if the task does not mention them.