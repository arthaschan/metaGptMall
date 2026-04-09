# API 说明（MVP）

> Swagger UI：`http://localhost:8080/swagger-ui/index.html`

## 1. Auth
### 1.1 注册
- POST `/api/auth/register`

Request:
```json
{ "email": "user@example.com", "password": "password123" }
```
Response:
```json
{ "accessToken": "..." }
```

### 1.2 登录
- POST `/api/auth/login`

Request:
```json
{ "email": "user@example.com", "password": "password123" }
```
Response:
```json
{ "accessToken": "..." }
```

## 2. Products
### 2.1 商品列表
- GET `/api/products?page=0&size=10`

Response: ProductDto[]

### 2.2 商品详情（带缓存）
- GET `/api/products/{id}`

## 3. Cart（需要 Authorization）
Header:
- `Authorization: Bearer <token>`

### 3.1 获取购物车
- GET `/api/cart`

### 3.2 加购
- POST `/api/cart/items`

Request:
```json
{ "productId": 1, "quantity": 1 }
```

## 4. Orders（需要 Authorization）
### 4.1 下单
- POST `/api/orders`

行为：下单即扣库存；成功后发 MQ 事件。

Response: OrderEntity（含 orderNo）