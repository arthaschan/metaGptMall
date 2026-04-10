# 架构设计

## 1. 当前本地实现基线

- 前端：Vue 3 + Vite，通过 `/api` 代理访问后端。
- 后端：Spring Boot 3。
- 本地默认数据层：H2 + MyBatis。
- 本地默认目标：无需 MySQL、Redis、RocketMQ，也能直接启动并联调。
- 当前优先保证的接口：
	- `GET /api/health`
	- `GET /api/products`
	- `GET /api/products/{id}`

## 2. 当前后端模块划分

- common：安全配置、异常处理、健康检查。
- product：商品查询接口、DTO、service、MyBatis mapper。
- entity：当前以 POJO 为主，供 MyBatis 映射和后续扩展复用。

## 3. 当前本地业务流

### 3.1 健康检查

1. 前端调用 `GET /api/health`
2. 后端返回 JSON 状态

### 3.2 商品列表/详情

1. 前端调用 `GET /api/products` 或 `GET /api/products/{id}`
2. 后端通过 MyBatis mapper 直接查询 H2 内存库
3. H2 在启动时通过 `schema.sql` 与 `data.sql` 自动初始化

## 4. 后续集成架构（非当前本地默认）

以下内容属于后续扩展方向，不应覆盖当前默认本地实现：

- MySQL：持久化用户、商品、购物车、订单数据。
- Redis：商品详情缓存、会话、幂等、限流等扩展。
- RocketMQ：订单创建事件 `order.created` 的异步处理。
- auth：注册/登录、JWT。
- cart：加购/查询购物车。
- order：下单、库存扣减、异步事件。

## 5. 配置与环境

- 本地启动基线：见 `docs/LOCAL_IMPL_BASELINE.md`
- 端口说明：见 `docs/PORTS.md`
- 集成期基础设施：见 `docs/INFRA.md`
- 本地主要配置文件：`server/src/main/resources/application.yml`、`web/vite.config.ts`