# metaGptMall Backend

Spring Boot 3 + MyBatis + JWT + Redis + RocketMQ e-commerce MVP.

## Tech Stack

| Layer | Library |
|-------|---------|
| Framework | Spring Boot 3.2.x |
| Persistence | MyBatis 3 (XML mappers) |
| Database | MySQL 8.4 |
| Cache | Redis 7 (`ecom:product:{id}`, TTL=60 s) |
| Messaging | RocketMQ 4.9.x — topic `order.created`, tag `v1` |
| Auth | JWT (jjwt 0.11.x), BCrypt |
| API Docs | Swagger UI / springdoc-openapi |
| Build | Maven 3.9 / Java 17 |

## Prerequisites

- Java 17+
- Maven 3.9+
- Docker + Docker Compose (MySQL, Redis, RocketMQ)

## Quick Start

### 1. Start infrastructure

```bash
# From repository root
docker compose up -d
```

### 2. Init database

```bash
bash scripts/db_init.sh
```

Or manually:

```bash
mysql -h 127.0.0.1 -uroot -proot ecommerce < sql/01_schema_mysql.sql
mysql -h 127.0.0.1 -uroot -proot ecommerce < sql/02_seed_mysql.sql
```

### 3. Review configuration

Open `src/main/resources/application.yml`.  
Items you **must** change before any real deployment:

| Key | Default | What to change |
|-----|---------|----------------|
| `spring.datasource.password` | `123456` | Strong password |
| `app.jwt.secret` | `CHANGE_ME_IN_PRODUCTION_...` | Random string ≥ 32 chars |
| `spring.data.redis.host` | `localhost` | Redis host in production |
| `rocketmq.name-server` | `localhost:9876` | RocketMQ NameServer |

### 4. Run the backend

```bash
mvn -f backend/pom.xml spring-boot:run
```

Or with dev profile:

```bash
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

## API Overview

### Auth (public)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register with email + password |
| POST | `/api/auth/login` | Login, returns JWT |

### Products (public)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/products?page=1&size=10` | Paginated product list |
| GET | `/api/products/{id}` | Product detail (Redis cached, TTL 60 s) |

### Cart (JWT required)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/cart` | Get current user's cart |
| POST | `/api/cart/items` | Add `{productId, quantity}` to cart |

### Orders (JWT required)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Create order from cart (deducts stock, publishes MQ event) |

## Build & Test

```bash
# Compile
mvn -f backend/pom.xml compile

# Run tests
mvn -f backend/pom.xml test

# Package
mvn -f backend/pom.xml package -DskipTests
```

## RocketMQ Message

On order creation a JSON message is published to topic `order.created` with tag `v1`:

```json
{
  "orderId": 1,
  "orderNo": "20260409123456ABCD1234",
  "userId": 1,
  "totalCents": 9998,
  "currency": "USD",
  "createdAt": "2026-04-09T12:34:56Z"
}
```

## Security Notes

> **IMPORTANT**: Default credentials are for local development only.
> Before any production or staging deployment you **MUST** change:
> - MySQL password (`docker-compose.yml` + `application.yml`)
> - JWT secret (`app.jwt.secret`)
> - Redis auth (if enabled)
