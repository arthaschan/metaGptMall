好的，作为QA工程师，我将根据提供的项目上下文，制定一份MVP（最小可行产品）阶段的测试计划。

---

# QA 测试计划 - metaGptMall (MVP)

## 1. 概述
本测试计划旨在覆盖电商平台MVP版本的核心功能与质量保障。重点验证核心用户旅程、API的正确性、边界情况以及数据一致性。由于是MVP，测试将聚焦于**功能正确性**和**核心稳定性**。

## 2. 测试策略与范围

**测试重点：**
*   **后端REST API**：Auth、Catalog、Cart、Order模块。
*   **核心业务流**：用户注册/登录 -> 浏览商品 -> 加入购物车 -> 创建订单。
*   **数据一致性**：下单时的库存扣减、购物车清空。
*   **边界与异常**：无效输入、资源不存在、权限不足、库存不足。

**暂缓/排除范围（MVP）：**
*   前端UI/UX详细测试（可进行基础冒烟）。
*   支付模块（根据路线图尚未实现）。
*   性能、压力与安全渗透测试。
*   RocketMQ消费者端的业务逻辑（假设其存在且正确）。

## 3. 冒烟测试 (Smoke Tests)
目标：验证系统最基本、最核心的功能流程是否畅通，确保后续详细测试的基线可用。
**执行频率**：每次部署后。

| 序号 | 测试场景 | 预期结果 |
| :--- | :--- | :--- |
| SMK-01 | 基础设施健康检查 | 能成功连接MySQL、Redis、RocketMQ NameServer。 |
| SMK-02 | 用户注册新账户 | 使用新邮箱注册成功，返回有效的`accessToken`。 |
| SMK-03 | 用户登录 | 使用已注册账户登录成功，返回有效的`accessToken`。 |
| SMK-04 | 获取商品列表 (未授权) | GET `/api/products` 返回200 OK及商品列表。 |
| SMK-05 | 获取商品详情 (未授权) | GET `/api/products/{已知id}` 返回200 OK及商品详情。 |
| SMK-06 | 添加商品到购物车 (需授权) | 使用有效Token，POST `/api/cart/items` 成功，返回200/201。 |
| SMK-07 | 查询购物车 (需授权) | GET `/api/cart` 返回200 OK，包含刚才添加的商品。 |
| SMK-08 | 创建订单 (需授权) | POST `/api/orders` 成功，返回200 OK及订单号，购物车被清空。 |
| SMK-09 | Swagger UI 可访问 | `http://localhost:8080/swagger-ui/index.html` 可正常加载。 |

## 4. API 测试 (API Tests)

### 4.1 认证模块 (Auth)
**Happy Path:**
*   **AUTH-HP-01**: 注册有效新用户 -> 201 Created，返回Token。
*   **AUTH-HP-02**: 使用正确凭据登录 -> 200 OK，返回Token。

**Negative Cases:**
*   **AUTH-NG-01**: 注册已存在的邮箱 -> 应返回 `409 Conflict` 或 `400 Bad Request`。
*   **AUTH-NG-02**: 注册请求体格式错误（如缺少邮箱字段）-> `400 Bad Request`。
*   **AUTH-NG-03**: 使用错误密码登录 -> `401 Unauthorized`。
*   **AUTH-NG-04**: 使用不存在的邮箱登录 -> `401 Unauthorized`。
*   **AUTH-NG-05**: 登录请求体格式错误 -> `400 Bad Request`。

### 4.2 商品模块 (Catalog)
**Happy Path:**
*   **CAT-HP-01**: 获取商品列表（带分页）-> 200 OK，返回ProductDto数组。
*   **CAT-HP-02**: 获取存在的商品详情 -> 200 OK，返回完整商品信息。

**Negative Cases:**
*   **CAT-NG-01**: 获取不存在的商品详情 (id=999999) -> `404 Not Found`。
*   **CAT-NG-02**: 商品详情缓存验证 -> 首次请求后，短时间内再次请求，验证响应头或逻辑表明来自缓存（可选，通过日志或监控判断）。
*   **CAT-NG-03**: 分页参数无效 (page=-1, size=1000) -> 应处理，返回`400`或应用默认值。

### 4.3 购物车模块 (Cart) - 所有操作需授权
**Happy Path:**
*   **CART-HP-01**: 向购物车添加有效商品 -> 200/201 Created，购物车中包含该商品。
*   **CART-HP-02**: 获取当前用户购物车 -> 200 OK，返回商品列表及数量、单价快照。
*   **CART-HP-03**: 重复添加同一商品 -> 应增加该商品的数量或更新数量（根据业务逻辑）。

**Negative Cases:**
*   **CART-NG-01**: 添加不存在的商品 (productId=999999) -> `404 Not Found` 或 `400 Bad Request`。
*   **CART-NG-02**: 添加数量为0或负数的商品 -> `400 Bad Request`。
*   **CART-NG-03**: **未提供/提供无效的Authorization头** -> `401 Unauthorized`。
*   **CART-NG-04**: 添加已下架 (`active=false`) 的商品 -> `400 Bad Request` 或 `409 Conflict`。

### 4.4 订单模块 (Order) - 所有操作需授权
**Happy Path:**
*   **ORD-HP-01**: 购物车有商品时下单 -> `200 OK`，返回订单详情。
    *   **数据一致性校验**：验证1) 订单总金额正确；2) 对应商品库存减少；3) 用户购物车被清空。
*   **ORD-HP-02**: **MQ事件验证**：下单成功后，检查RocketMQ `order.created:v1` 主题中是否有一条对应的消息，且消息体格式正确。

**Negative Cases:**
*   **ORD-NG-01**: **库存不足**：尝试购买数量超过库存的商品 -> `409 Conflict` 或 `400 Bad Request`，且库存、购物车均无变化。
*   **ORD-NG-02**: 对空购物车下单 -> `400 Bad Request` (购物车为空)。
*   **ORD-NG-03**: **未授权请求** -> `401 Unauthorized`。
*   **ORD-NG-04**: 下单过程中商品被删除或下架 -> `400 Bad Request` 或 `409 Conflict`。

## 5. 边缘与特殊情况 (Edge Cases)
*   **EDG-01**: **并发下单抢库存**：模拟两个用户同时购买最后一件商品。预期结果：仅一个订单成功，另一个因库存不足失败。**（关键测试点）**
*   **EDG-02**: **JWT令牌过期**：获取Token后，等待其过期，再用其调用需要授权的API -> `401 Unauthorized`。
*   **EDG-03**: **Redis缓存失效**：等待商品详情缓存TTL过期，再次请求，验证是否回源数据库并重新填充缓存。
*   **EDG-04**: **RocketMQ不可用**：停掉RocketMQ服务，然后执行下单。预期：订单创建可能成功（如果Producer是异步且容忍失败），但事件发送失败应有日志记录。**（测试系统健壮性）**
*   **EDG-05**: **大数据分页**：当商品数量巨大时，测试分页的最后一页和超出范围的情况。

## 6. 最小化测试数据建议
建议在 `scripts/db_init.sh` 或单独的测试数据脚本中准备以下数据：

```sql
-- 1. 基础商品数据 (至少3-4个)
INSERT INTO products (id, title, price_cents, stock, active) VALUES
(1, '测试商品A', 2999, 10, true),
(2, '测试商品B', 4999, 5, true),
(3, '测试商品C（库存为1）', 1999, 1, true),
(4, '已下架商品', 999, 100, false); -- 用于测试不可购买场景

-- 2. 预置测试用户 (可选，也可完全通过注册API创建)
-- 密码为 `password123` 的 BCrypt Hash
INSERT INTO users (id, email, password_hash, role) VALUES
(99, 'test@example.com', '$2a$10$...', 'USER'); -- 注意替换为真实的hash
```

**测试账户**：`test@example.com` / `password123` (使用与代码中相同的BCrypt强度)。

## 7. 后续自动化建议
1.  **自动化优先级**：
    *   **P0 (立即/高收益)**：将**冒烟测试(SMK-01至SMK-08)**和**核心API的Happy Path**自动化，集成到CI/CD流水线中，作为门禁。
    *   **P1 (中期)**：自动化**关键的Negative Cases**（如 AUTH-NG-01/03, CART-NG-01, ORD-NG-01）和**数据一致性校验**（ORD-HP-01的验证点）。
    *   **P2 (远期)**：考虑自动化**边缘案例**（如EDG-01并发测试需要工具支持）和更复杂的场景。

2.  **工具选型建议**：
    *   **API自动化**：`Postman` (Collections + Newman) 或 `Pytest`/`JUnit` + `RestAssured`/`Requests`。
    *   **集成CI/CD**：使用 `GitHub Actions` / `Jenkins` 运行自动化测试集。
    *   **数据一致性校验**：在自动化测试中连接测试数据库进行断言。

3.  **测试环境**：建议建立独立的`test`环境，使用单独的数据库和缓存实例，便于数据隔离和清理（使用`@Transactional`或每次测试前后清理数据）。

4.  **监控与可观测性**：在测试过程中，验证应用日志、Redis命中率、MQ消息堆积等指标是否正常，为未来非功能测试打基础。