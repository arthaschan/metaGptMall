# 架构设计（MVP）

## 1. 总览
- 前端（Vue3 + Element Plus）通过 REST API 调用后端。
- 后端（Spring Boot 3）提供 Auth/Catalog/Cart/Order 等模块。
- MySQL 持久化用户、商品、购物车、订单数据。
- Redis 做商品详情缓存（可扩展为会话/幂等/限流）。
- RocketMQ 发送订单创建事件 `order.created`，消费者用于异步处理扩展。

## 2. 模块划分（后端）
- auth：注册/登录、JWT
- catalog：商品列表/商品详情（详情带缓存）
- cart：加购/查询购物车
- order：下单（事务 + 扣库存）+ 发 MQ
- common：统一配置、常量、序列化
- mq：RocketMQ producer/consumer

## 3. 关键业务流
### 3.1 注册/登录
1) `POST /api/auth/register` 创建用户（BCrypt hash）
2) `POST /api/auth/login` 校验密码并签发 JWT

JWT Claims 约定：
- `sub`：userId
- `role`：user/admin

### 3.2 商品列表/详情
- 列表直接读 MySQL（MVP）
- 详情先读 Redis（key `ecom:product:{id}`），未命中读 MySQL 并回填缓存

### 3.3 加购
- 购物车与条目落 MySQL
- 加购会读取商品当前 price 作为 `unit_price_cents` 写入 cart_items

### 3.4 下单（库存策略 A：下单即扣库存）
事务内：
1) 读取 cart_items
2) 逐条执行 `UPDATE products SET stock=stock-qty WHERE stock>=qty` 扣库存
3) 创建 orders + order_items（价格与标题快照）
4) 清空 cart_items
5) 发送 RocketMQ `order.created:v1`

失败回滚：库存不足/DB 错误都会回滚库存与订单写入。

## 4. 配置与环境
- 端口：`docs/PORTS.md`
- 基础设施：`docs/INFRA.md`
- 需要手动修改：`docker-compose.yml`、`backend/src/main/resources/application.yml`