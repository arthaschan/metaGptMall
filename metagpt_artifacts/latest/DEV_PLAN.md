# 开发计划 (Dev Plan)

## 目标与原则
- **目标**：基于现有架构文档，实现一个可运行的电商平台 MVP (Minimum Viable Product)。
- **原则**：
    1.  增量开发：将开发分解为小而独立的 PR/Commit。
    2.  优先实现核心业务流程。
    3.  遵循项目约定的目录结构和命名规范。
    4.  每个阶段完成后都有明确的检查点进行验证。

---

## 阶段 1：项目初始化与基础框架 (Week 1)
**目标**：搭建项目骨架，完成基础依赖和配置，确保环境可运行。
### 任务分解
1.  **PR-1：初始化后端 Spring Boot 项目**
    *   **步骤**：
        1.  使用 Spring Initializr 创建项目，包含 `Spring Web`, `Spring Data JPA`, `MySQL Driver`, `Spring Data Redis`, `Lombok`, `SpringDoc OpenAPI` 等依赖。
        2.  按照 `docs/ARCHITECTURE.md` 的建议，在 `src/main/java/com/metagptmall` 下创建模块包：`common`, `auth`, `catalog`, `cart`, `order`, `mq`。
        3.  创建 `src/main/resources/application.yml`，根据 `docs/PORTS.md` 和 `docs/INFRA.md` 配置 MySQL、Redis、RocketMQ 连接（先使用占位符）。
        4.  在 `common` 模块下创建统一的响应包装类 `ApiResponse`、异常处理器 `GlobalExceptionHandler` 和常量文件。
    *   **文件路径**：
        *   `backend/pom.xml`
        *   `backend/src/main/java/com/metagptmall/common/...`
        *   `backend/src/main/resources/application.yml`
    *   **检查点**：项目能成功启动，无编译错误。访问 `http://localhost:8080/swagger-ui/index.html` 能看到基础的 Swagger UI。

2.  **PR-2：数据库初始化脚本**
    *   **步骤**：
        1.  在 `sql/` 目录下创建 `01_schema.sql`，定义 `users`, `products`, `cart`, `cart_items`, `orders`, `order_items` 表结构。
        2.  创建 `02_seed_data.sql`，插入测试用的商品和用户数据。
        3.  更新 `scripts/db_init.sh` 脚本，按顺序执行 SQL 文件。
    *   **文件路径**：
        *   `sql/01_schema.sql`
        *   `sql/02_seed_data.sql`
        *   `scripts/db_init.sh`
    *   **检查点**：执行 `bash scripts/db_init.sh` 后，MySQL 中 `ecommerce` 数据库的表结构和测试数据被正确创建。

3.  **PR-3：初始化前端 React 项目**
    *   **步骤**：
        1.  使用 Vite + React 模板创建项目。
        2.  安装基础依赖：`react-router-dom`, `axios`, `zustand` (状态管理), `antd` 或 `MUI` (UI 组件库)。
        3.  配置开发服务器代理，将 `/api` 请求转发到后端 `localhost:8080`。
        4.  创建基础路由结构：登录页、商品列表页、商品详情页、购物车页、个人中心页。
        5.  创建 `src/services/api.js` 封装 `axios` 实例，统一处理请求拦截（添加 JWT）、响应拦截和错误。
    *   **文件路径**：
        *   `frontend/vite.config.js`
        *   `frontend/src/router/index.jsx`
        *   `frontend/src/services/api.js`
    *   **检查点**：前端能独立启动 (`npm run dev`)，访问 `http://localhost:5173` 看到页面框架，且无 JS 错误。

---

## 阶段 2：用户认证模块 (Week 2)
**目标**：实现用户注册、登录、JWT 签发与校验。
### 任务分解
1.  **PR-4：后端 Auth 模块实体与 Repository**
    *   **步骤**：
        1.  在 `auth` 模块下创建 `UserEntity` (对应 `users` 表)。
        2.  创建 `UserRepository` (JPA)。
        3.  创建 `LoginRequest`, `RegisterRequest`, `AuthResponse` 等 DTO。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/auth/`
    *   **检查点**：通过 Repository 能成功查询到 `02_seed_data.sql` 中插入的测试用户。

2.  **PR-5：后端 Auth 服务与控制器**
    *   **步骤**：
        1.  创建 `AuthService`，实现 `register` (BCrypt 加密密码) 和 `login` (校验密码) 逻辑。
        2.  创建 `JwtService`，用于生成和解析 JWT，声明应包含 `sub` (userId) 和 `role`。
        3.  创建 `AuthController`，实现 `POST /api/auth/register` 和 `POST /api/auth/login` 接口，返回格式符合 `docs/API.md`。
        4.  在 `application.yml` 中配置 JWT 密钥和过期时间。
    *   **文件路径**：
        *   `backend/src/main/java/com/metagptmall/auth/{AuthService, JwtService, AuthController}`
        *   `backend/src/main/resources/application.yml` (JWT配置)
    *   **检查点**：使用 Swagger UI 或 Postman 调用注册和登录接口成功，登录接口返回有效的 JWT Token。

3.  **PR-6：后端 JWT 请求过滤器**
    *   **步骤**：
        1.  创建 `JwtAuthenticationFilter`，拦截请求，从 `Authorization` Header 解析 JWT，验证有效性后设置安全上下文。
        2.  配置 Spring Security 或使用 `@Component` + `FilterRegistrationBean` 将该过滤器应用到 `/api/cart/**` 和 `/api/orders/**` 路径。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/auth/filter/JwtAuthenticationFilter.java`
    *   **检查点**：不带 Token 访问 `/api/cart` 返回 401；使用有效 Token 访问能通过过滤器。

4.  **PR-7：前端登录/注册页面**
    *   **步骤**：
        1.  创建 `src/pages/Login.jsx` 和 `src/pages/Register.jsx` 页面组件。
        2.  在页面中调用 `AuthService` (封装 `api.js` 中的登录/注册请求)。
        3.  登录成功后，将 Token 存储到 `localStorage` 或 `zustand` 状态中，并跳转到首页。
    *   **文件路径**：
        *   `frontend/src/pages/Login.jsx`
        *   `frontend/src/pages/Register.jsx`
        *   `frontend/src/services/AuthService.js`
    *   **检查点**：在前端页面能完成注册和登录流程，登录后 Token 被保存。

---

## 阶段 3：商品与购物车模块 (Week 3)
**目标**：实现商品浏览（带缓存）和购物车管理。
### 任务分解
1.  **PR-8：后端 Catalog 模块**
    *   **步骤**：
        1.  创建 `ProductEntity` 和 `ProductRepository`。
        2.  创建 `ProductService` 和 `ProductController`。
        3.  在 `ProductService` 中实现 `getProductById`：先查 Redis (`ecom:product:{id}`)，未命中则查 DB 并回填缓存，TTL 参考 `docs/REDIS_KEYS.md`。
        4.  实现 `GET /api/products` (分页列表) 和 `GET /api/products/{id}` 接口。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/catalog/`
    *   **检查点**：调用商品详情接口，第一次请求后，Redis 中能查到对应的缓存 Key 和 Value。

2.  **PR-9：后端 Cart 模块实体与 Repository**
    *   **步骤**：
        1.  创建 `CartEntity`, `CartItemEntity` 及对应的 Repository。
        2.  创建 `AddToCartRequest`, `CartDto` 等 DTO。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/cart/`
    *   **检查点**：JPA 实体映射正确，能通过 `userId` 查询到购物车。

3.  **PR-10：后端 Cart 服务与控制器**
    *   **步骤**：
        1.  创建 `CartService` 和 `CartController`。
        2.  实现 `GET /api/cart`：获取当前用户购物车详情。
        3.  实现 `POST /api/cart/items`：将商品加入购物车。需从 `ProductRepository` 读取当前价格作为 `unit_price_cents` 写入 `cart_items`。
        4.  确保这两个接口都需要 JWT 认证（已在阶段 2 配置）。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/cart/{CartService, CartController}`
    *   **检查点**：登录后，能成功调用加购和查询购物车接口，数据正确落库。

4.  **PR-11：前端商品列表与详情页**
    *   **步骤**：
        1.  创建 `src/pages/ProductList.jsx`，调用 `GET /api/products` 接口展示商品列表（图片、标题、价格）。
        2.  创建 `src/pages/ProductDetail.jsx`，展示商品详细信息，并包含“加入购物车”按钮。
        3.  实现“加入购物车”按钮点击事件，调用 `CartService.addItem`。
    *   **文件路径**：
        *   `frontend/src/pages/ProductList.jsx`
        *   `frontend/src/pages/ProductDetail.jsx`
        *   `frontend/src/services/ProductService.js`
        *   `frontend/src/services/CartService.js`
    *   **检查点**：在前端能浏览商品，点击加入购物车后，通过 API 或数据库验证购物车数据已更新。

5.  **PR-12：前端购物车页面**
    *   **步骤**：
        1.  创建 `src/pages/Cart.jsx` 页面。
        2.  调用 `GET /api/cart` 接口，展示购物车中的商品条目、单价、数量、总价。
        3.  实现数量修改、删除商品的功能（对应新的后端接口，本阶段可先实现前端交互）。
    *   **文件路径**：`frontend/src/pages/Cart.jsx`
    *   **检查点**：购物车页面能正确显示已添加的商品信息。

---

## 阶段 4：订单与消息队列模块 (Week 4)
**目标**：实现下单流程（扣库存）并集成 RocketMQ 发送事件。
### 任务分解
1.  **PR-13：后端 Order 模块实体与 Repository**
    *   **步骤**：
        1.  创建 `OrderEntity`, `OrderItemEntity` 及对应 Repository。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/order/`
    *   **检查点**：实体定义正确。

2.  **PR-14：后端 Order 服务（事务与扣库存）**
    *   **步骤**：
        1.  创建 `OrderService` 和 `OrderController`。
        2.  在 `OrderService.createOrder` 方法上添加 `@Transactional`。
        3.  实现事务内逻辑（参考 `docs/ARCHITECTURE.md 3.4`）：
            a. 获取当前用户购物车。
            b. 遍历商品，执行扣库存 SQL (`UPDATE products SET stock=stock-? WHERE id=? AND stock>=?`)。
            c. 任一商品库存不足则抛异常，事务回滚。
            d. 库存充足则创建订单、订单快照条目，清空购物车。
    *   **文件路径**：`backend/src/main/java/com/metagptmall/order/{OrderService, OrderController}`
    *   **检查点**：调用下单接口，库存正确扣减，订单和订单项生成，购物车清空。库存不足时，订单不会创建，库存数据回滚。

3.  **PR-15：后端 MQ 模块集成**
    *   **步骤**：
        1.  在 `mq` 模块下创建 `RocketMQProducer`，用于发送消息。
        2.  在 `OrderService.createOrder` 事务提交**成功后**，调用 `RocketMQProducer` 发送 `order.created:v1` 消息，消息体格式遵循 `docs/ROCKETMQ_TOPICS.md`。
        3.  创建 `RocketMQConsumer` 作为示例消费者，监听 `order.created` 主题，打印日志即可（为后续扩展如发邮件、更新统计数据留口子）。
        4.  在 `application.yml` 中配置 RocketMQ NameServer 地址。
    *   **文件路径**：
        *   `backend/src/main/java/com/metagptmall/mq/`
        *   `backend/src/main/resources/application.yml` (RocketMQ配置)
    *   **检查点**：成功下单后，能在 RocketMQ 控制台或消费者日志中看到对应消息被发送和接收。

4.  **PR-16：前端订单创建与个人中心**
    *   **步骤**：
        1.  在购物车页面 (`Cart.jsx`) 增加“结算”或“下单”按钮。
        2.  按钮点击后调用 `OrderService.createOrder` 接口。
        3.  创建 `src/pages/Profile/OrderList.jsx` 页面，展示用户的历史订单列表（调用新的后端接口 `GET /api/orders`）。
    *   **文件路径**：
        *   `frontend/src/pages/Cart.jsx` (增加下单逻辑)
        *   `frontend/src/pages/Profile/OrderList.jsx`
        *   `frontend/src/services/OrderService.js`
    *   **检查点**：在前端能完成从购物车到创建订单的全流程，并能在个人中心看到订单记录。

---

## 阶段 5：测试、文档与收尾 (Week 5)
**目标**：完善测试，补充文档，进行集成验证。
### 任务分解
1.  **PR-17：关键单元测试与集成测试**
    *   **步骤**：
        1.  为 `AuthService.login/register` 编写单元测试。
        2.  为 `OrderService.createOrder`（库存充足/不足场景）编写集成测试 (`@SpringBootTest`)，使用 `@Transactional` 保持测试数据隔离。
        3.  为 `ProductService.getProductById` (缓存逻辑) 编写集成测试。
    *   **文件路径**：`backend/src/test/` 下对应模块的测试类。
    *   **检查点**：所有测试通过 (`mvn test`)。

2.  **PR-18：完善项目文档**
    *   **步骤**：
        1.  根据实际实现，更新 `README.md` 中的快速启动步骤。
        2.  补充 `docs/API.md` 中可能遗漏的接口细节。
        3.  创建 `docs/DEV_GUIDE.md`，说明本地开发环境设置、编码规范、分支策略等。
    *   **文件路径**：`README.md`, `docs/`
    *   **检查点**：文档清晰，新成员能根据 `README.md` 在本地成功启动整个项目并走通核心流程。

3.  **PR-19：端到端核心流程验证与 Bug 修复**
    *   **步骤**：
        1.  按照“注册 -> 登录 -> 浏览商品 -> 加入购物车 -> 查看购物车 -> 下单 -> 查看订单”的完整路径进行手动测试。
        2.  修复在此过程中发现的任何前端或后端 Bug。
        3.  确保所有配置文件中的端口、密码与 `docker-compose.yml` 中的定义一致。
    *   **检查点**：核心业务流程完全畅通，无阻塞性错误。MVP 版本达到可用状态。