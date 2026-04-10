# 代码骨架建议 (Code Skeleton Suggestions)

基于提供的项目上下文和架构设计，以下是实现该项目 MVP 所需的代码骨架建议。

## 1. 目录树结构 (Proposed Directory Tree)

```
metaGptMall/
├── backend/ (Spring Boot 后端)
│   ├── src/main/java/com/metagptmall/
│   │   ├── common/ (公共模块)
│   │   │   ├── config/ (配置类，如 RedisConfig, RocketMQConfig, WebConfig)
│   │   │   ├── constants/ (常量，如 RedisKeyConstants, MQTopicConstants)
│   │   │   ├── exception/ (全局异常处理器，如 GlobalExceptionHandler)
│   │   │   ├── security/ (JWT 过滤器、安全配置)
│   │   │   └── utils/ (工具类，如 JwtUtil, RedisUtil)
│   │   ├── auth/ (认证模块)
│   │   ├── catalog/ (商品模块)
│   │   ├── cart/ (购物车模块)
│   │   ├── order/ (订单模块)
│   │   └── mq/ (消息队列模块)
│   │       ├── producer/
│   │       └── consumer/
│   ├── src/main/resources/
│   │   ├── application.yml (主配置文件)
│   │   └── db/migration/ (可选，Flyway脚本)
│   └── src/test/ (测试)
├── frontend/ (React 前端)
│   ├── public/
│   ├── src/
│   │   ├── api/ (API 调用封装)
│   │   ├── components/ (通用组件)
│   │   ├── contexts/ (React Context，如 AuthContext)
│   │   ├── hooks/ (自定义Hooks)
│   │   ├── pages/ (页面组件)
│   │   │   ├── Auth/ (登录/注册页)
│   │   │   ├── Products/ (商品列表/详情页)
│   │   │   ├── Cart/ (购物车页)
│   │   │   └── Orders/ (订单页)
│   │   ├── router/ (路由配置)
│   │   └── utils/ (工具函数)
│   ├── .env.development (开发环境变量)
│   ├── vite.config.ts
│   └── package.json
├── sql/ (数据库脚本)
│   ├── 00_create_db_mysql.sql
│   └── 01_seed_data.sql
├── rocketmq/ (RocketMQ 配置文件)
│   └── broker.v2.conf
├── docker-compose.yml
├── scripts/
│   └── db_init.sh
└── docs/ (已有文档)
```

## 2. 关键文件列表与接口/类签名 (Key Files & Signatures)

### 2.1 后端 (Backend)

#### 公共模块 (common)
- **`common/config/RedisConfig.java`**: 配置 RedisTemplate，设置序列化方式。
- **`common/constants/RedisKeyConstants.java`**:
    ```java
    public class RedisKeyConstants {
        public static final String PRODUCT_CACHE_KEY_PREFIX = "ecom:product:";
        public static String getProductCacheKey(Long productId) { ... }
    }
    ```
- **`common/security/JwtAuthFilter.java`**: 拦截请求，验证 JWT，设置 SecurityContext。
- **`common/security/SecurityConfig.java`**: 配置 Spring Security，放行登录/注册等公开 API。

#### 认证模块 (auth)
- **`auth/controller/AuthController.java`**:
    ```java
    @RestController @RequestMapping("/api/auth")
    public class AuthController {
        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) { ... }
        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) { ... }
    }
    ```
- **`auth/service/AuthService.java`**:
    ```java
    public interface AuthService {
        AuthResponse register(RegisterRequest request);
        AuthResponse login(LoginRequest request);
    }
    ```
- **`auth/dto/`**:
    - `RegisterRequest.java` (email, password)
    - `LoginRequest.java` (email, password)
    - `AuthResponse.java` (accessToken)

#### 商品模块 (catalog)
- **`catalog/controller/ProductController.java`**:
    ```java
    @RestController @RequestMapping("/api/products")
    public class ProductController {
        @GetMapping
        public Page<ProductDto> getProducts(@RequestParam int page, @RequestParam int size) { ... }
        @GetMapping("/{id}")
        public ProductDto getProductById(@PathVariable Long id) { ... } // 带缓存逻辑
    }
    ```
- **`catalog/service/ProductService.java`**:
    ```java
    public interface ProductService {
        Page<ProductDto> getProducts(int page, int size);
        ProductDto getProductById(Long id);
    }
    ```
- **`catalog/dto/ProductDto.java`**: 包含 id, title, description, priceCents, currency, stock, imageUrl, active 等字段。

#### 购物车模块 (cart)
- **`cart/controller/CartController.java`** (需 `@PreAuthorize("hasRole('USER')")`):
    ```java
    @RestController @RequestMapping("/api/cart")
    public class CartController {
        @GetMapping
        public CartDto getCart() { ... } // 从 SecurityContext 获取 userId
        @PostMapping("/items")
        public CartDto addItem(@RequestBody @Valid AddItemRequest request) { ... }
    }
    ```
- **`cart/dto/AddItemRequest.java`**: (productId, quantity)

#### 订单模块 (order)
- **`order/controller/OrderController.java`** (需 `@PreAuthorize("hasRole('USER')")`):
    ```java
    @RestController @RequestMapping("/api/orders")
    public class OrderController {
        @PostMapping
        @Transactional // 关键事务注解
        public OrderDto createOrder() { ... } // 从购物车下单
    }
    ```
- **`order/service/OrderService.java`**:
    ```java
    public interface OrderService {
        OrderDto createOrder(Long userId); // 伪代码：下单、扣库存、清购物车、发MQ
    }
    ```

#### 消息队列模块 (mq)
- **`mq/producer/OrderEventProducer.java`**:
    ```java
    @Component
    public class OrderEventProducer {
        public void sendOrderCreatedEvent(OrderCreatedEvent event) { ... } // 发送到 topic `order.created`, tag `v1`
    }
    ```
- **`mq/consumer/OrderCreatedConsumer.java`**:
    ```java
    @Component @RocketMQMessageListener(...) // 指定 consumerGroup, topic, tag
    public class OrderCreatedConsumer implements RocketMQListener<OrderCreatedEvent> {
        @Override
        public void onMessage(OrderCreatedEvent event) { ... } // 处理订单创建事件，如发送邮件/短信（幂等处理）
    }
    ```
- **`mq/dto/OrderCreatedEvent.java`**: 对应 `docs/ROCKETMQ_TOPICS.md` 中的 JSON 结构。

### 2.2 前端 (Frontend)

#### API 层 (api/)
- **`api/axiosClient.js`**: 创建 axios 实例，配置 baseURL，请求拦截器（添加 JWT token）。
- **`api/auth.js`**:
    ```javascript
    export const login = (credentials) => axiosClient.post('/auth/login', credentials);
    export const register = (userData) => axiosClient.post('/auth/register', userData);
    ```
- **`api/products.js`**, **`api/cart.js`**, **`api/orders.js`**: 类似结构。

#### 状态与路由 (contexts/, router/)
- **`contexts/AuthContext.jsx`**: 提供 `user`, `token`, `login`, `logout` 的全局状态。
- **`router/AppRouter.jsx`**: 使用 React Router，配置路由守卫（如未登录跳转至登录页）。

#### 页面组件 (pages/)
- **`pages/Auth/LoginPage.jsx`**: 表单，调用 `api.auth.login`，成功后更新 `AuthContext` 并跳转。
- **`pages/Products/ProductListPage.jsx`**: 调用 `api.products.getProducts` 展示列表，点击进入详情页。
- **`pages/Cart/CartPage.jsx`**: 调用 `api.cart.getCart` 展示购物车，提供结算按钮（跳转下单）。
- **`pages/Orders/OrderCreatePage.jsx`**: 调用 `api.orders.createOrder` 提交订单。

## 3. 核心逻辑伪代码 (Pseudocode for Key Logic)

### 3.1 商品详情缓存 (Product Detail with Cache)
```java
// 在 ProductService 实现类中
public ProductDto getProductById(Long id) {
    String cacheKey = RedisKeyConstants.getProductCacheKey(id);
    // 1. 尝试从 Redis 获取
    ProductDto cached = redisTemplate.opsForValue().get(cacheKey);
    if (cached != null) {
        return cached;
    }
    // 2. 缓存未命中，查询数据库
    Product product = productRepository.findById(id).orElseThrow(...);
    ProductDto dto = convertToDto(product);
    // 3. 写入缓存，设置 TTL
    redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofSeconds(productCacheTtl));
    return dto;
}
```

### 3.2 下单事务 (Create Order Transaction)
```java
@Transactional
public OrderDto createOrder(Long userId) {
    // 1. 获取用户购物车及条目
    Cart cart = cartRepository.findByUserIdWithItems(userId);
    // 2. 遍历条目，逐个尝试扣减库存 (乐观锁)
    for (CartItem item : cart.getItems()) {
        int rowsUpdated = productRepository.deductStock(item.getProductId(), item.getQuantity());
        if (rowsUpdated == 0) {
            throw new InsufficientStockException(...);
        }
    }
    // 3. 创建订单及订单项（价格快照）
    Order order = new Order();
    // ... 填充订单信息
    order = orderRepository.save(order);
    // 4. 清空购物车
    cartItemRepository.deleteAll(cart.getItems());
    // 5. 发送 MQ 事件 (在事务提交后发送，避免消息先于数据提交)
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                orderEventProducer.sendOrderCreatedEvent(convertToEvent(order));
            }
        }
    );
    return convertToDto(order);
}
```

## 4. 配置与环境变量说明 (Configuration Notes)

1.  **关键配置文件**:
    *   `backend/src/main/resources/application.yml`: 必须配置 `spring.datasource.url` (MySQL), `spring.data.redis.*`, `app.rocketmq.namesrv-addr`, `server.port` (8080)。
    *   `frontend/.env.development`: 应设置 `VITE_API_BASE_URL=http://localhost:8080/api`。
    *   `docker-compose.yml`: 定义 MySQL, Redis, RocketMQ 服务及端口映射。

2.  **环境变量与安全**:
    *   **JWT 密钥**、**数据库密码**等敏感信息**不应**硬编码在 `application.yml` 中。建议使用环境变量或 Spring Cloud Config（生产环境）。
    *   在 `application.yml` 中使用占位符: `jwt.secret: ${JWT_SECRET:defaultDevSecret}`。
    *   `docker-compose.yml` 中通过 `environment:` 区块传递环境变量给后端容器。

3.  **跨域 (CORS)**:
    *   在 `backend` 的 `WebConfig` 或 `SecurityConfig` 中配置 CORS，允许前端开发服务器 (`http://localhost:5173`) 的请求。

**重要提醒**: 此代码骨架是**起点建议**。开发时需遵循 `docs/` 下的详细 API、Redis、MQ 约定，并补充完整的错误处理、输入验证、日志和单元测试。