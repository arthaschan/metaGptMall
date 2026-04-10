# API 说明

> 当前文档需要区分“本地默认可运行接口”与“后续集成接口规划”。
> 对 MetaGPT 默认 impl 任务，应优先遵循本地默认可运行接口，而不是后续扩展接口。

> Swagger UI：`http://localhost:8080/swagger-ui/index.html`

## 1. 本地默认可运行接口

### 1.1 健康检查
- GET `/api/health`

Response:
```json
{
	"status": "UP",
	"service": "ecommerce-server",
	"version": "1.0.0",
	"timestamp": "2026-04-10T09:00:00"
}
```

说明：
- 当前本地健康检查返回 JSON 对象。
- 必须至少包含 `status`、`service`、`version`、`timestamp`。

### 1.2 商品列表
- GET `/api/products`

说明：
- 当前本地基线不要求分页参数。
- 当前本地基线不要求登录态。
- 当前本地基线不要求 Redis 缓存命中逻辑。

Response: `ProductResponse[]`

示例：
```json
[
	{
		"id": 1,
		"title": "Wireless Mouse",
		"description": "Ergonomic wireless mouse",
		"priceCents": 2999,
		"currency": "CNY",
		"stock": 100,
		"imageUrl": "/images/mouse.jpg",
		"active": true,
		"createdAt": "2026-04-10T09:00:00",
		"updatedAt": "2026-04-10T09:00:00"
	}
]
```

### 1.3 商品详情
- GET `/api/products/{id}`

说明：
- 当前返回 DTO 名称应为 `ProductResponse`，不要改成 `ProductDto`。
- 字段命名应与当前本地代码保持一致：`title`、`priceCents`、`currency`、`stock`、`imageUrl`、`active`。

Response: `ProductResponse`

### 1.4 ProductResponse 字段契约

字段列表：
- `id`: Long
- `title`: String
- `description`: String
- `priceCents`: Integer
- `currency`: String
- `stock`: Integer
- `imageUrl`: String
- `active`: Boolean
- `createdAt`: LocalDateTime
- `updatedAt`: LocalDateTime

### 1.5 用户重置密码
- POST `/api/auth/password/reset`

说明：
- 这是当前仓库可落地的本地最小版本，不依赖邮件、短信、验证码或重置 token。
- 本地默认按 `email + newPassword + confirmPassword` 直接执行密码重置。
- 后续如果要接入邮件验证码或 token 流程，属于集成阶段扩展，不覆盖当前最小实现。

Request:
```json
{
	"email": "user@example.com",
	"newPassword": "new-password-123",
	"confirmPassword": "new-password-123"
}
```

Response:
```json
{
	"message": "Password reset successful",
	"email": "user@example.com"
}
```

约束：
- `newPassword` 与 `confirmPassword` 必须一致。
- 新密码长度至少 8 位。
- 本地实现应更新 `users.password_hash`。
- 密码应以哈希形式保存，不要明文落库。
- 若用户不存在，可返回 404 或明确业务错误；但不要伪造邮件发送链路。

## 2. 本地默认实现约束

- 后端本地默认实现应基于 H2 + MyBatis。
- `products` 表字段应围绕当前 `ProductResponse` 契约设计，不要回退到 `name`、`price`、`stock_quantity` 这套旧命名。
- 若启用重置密码功能，本地默认实现应补充 `users` 表以及对应 H2 初始化数据。
- 本地默认实现不要求 MySQL、Redis、RocketMQ。
- 本地默认实现不要求购物车、订单、登录注册先完成，才能提供 `/api/health` 和 `/api/products`。

## 3. 后续集成接口规划（非当前默认 impl）

以下内容属于后续扩展方向，可以保留为设计目标，但不应覆盖当前默认生成目标。

### 3.1 Auth
- POST `/api/auth/register`
- POST `/api/auth/login`

### 3.2 Cart
- GET `/api/cart`
- POST `/api/cart/items`

### 3.3 Orders
- POST `/api/orders`

扩展说明：
- 订单提交后可对接消息队列事件。
- 商品详情可在后续集成 Redis 缓存。
- 分页、鉴权、购物车、订单流转属于后续阶段，不是本地最小可运行 impl 的前置条件。