# 本地实现基线（高优先级）

本文件用于约束 MetaGPT 在当前仓库中的默认本地实现方向。
如果本文件与其它架构/基础设施文档冲突，以本文件为准。

## 目标

- 当前默认目标是“本地可直接启动的 MVP 实现”。
- 优先保证 server/ 与 web/ 在开发机上可直接运行。
- 不要求本地必须部署 MySQL、Redis、RocketMQ。

## 后端基线

- 技术栈：Spring Boot 3.x + H2 + MyBatis。
- 默认数据源：H2 内存库。
- 本地启动时应自动执行 `schema.sql` 与 `data.sql`。
- 当前必须可用的接口：
  - `GET /api/health`
  - `GET /api/products`
  - `GET /api/products/{id}`
- `GET /api/health` 的本地默认 JSON 响应至少包含：
  - `status`
  - `service`
  - `version`
  - `timestamp`
- 如果当前需求是“用户重置密码”，本地最小实现允许新增：
  - `POST /api/auth/password/reset`
- 该接口在当前本地基线中不要求邮件、短信、验证码或重置 token。
- 当前本地重置密码最小契约应基于：`email + newPassword + confirmPassword`。
- 包结构应保持与当前仓库一致：
  - 健康接口：`com.ecommerce.common.controller.HealthController`
  - 商品 controller：`com.ecommerce.product.controller`
  - 商品 service：`com.ecommerce.product.service`
  - 商品 dto：`com.ecommerce.product.dto`
  - repository：`com.ecommerce.repository`
- 若新增认证/密码能力，建议使用：
  - `com.ecommerce.auth.controller`
  - `com.ecommerce.auth.service`
  - `com.ecommerce.auth.dto`
  - `com.ecommerce.repository.UserRepository`
- 不要把这些类扁平化生成为 `com.ecommerce.controller`、`com.ecommerce.service`、`com.ecommerce.dto`。
- 产品查询应使用 MyBatis mapper，包路径为 `com.ecommerce.repository`。
- repository 接口命名优先沿用当前仓库中的 `ProductRepository`，不要再平行生成 `ProductMapper`。
- 用户仓储接口命名优先使用 `UserRepository`。
- 当前本地默认风格是“注解式 mapper 接口”。
- 不要把 XML mapper 文件与 `mybatis.mapper-locations=classpath:mapper/*.xml` 作为默认前提。
- 本地 impl 不应依赖 JPA repository、Hibernate 专有注解、Redis cache、RocketMQ producer/consumer。
- 应用启动类保留在 `com.ecommerce` 根包下，并优先沿用当前 `EcommerceApplication` 类名。
- 配置类优先复用 `com.ecommerce.common.config` 下已有风格，不要平行生成新的 `com.ecommerce.config.WebConfig` 来重复处理 CORS 或 Web 配置。

## 前端基线

- 技术栈：Vue 3 + Vite。
- 前端通过 `/api` 代理访问后端。
- 生成代码必须满足：
  - `npm install`
  - `npm run build`
- 不要引入未在 `web/package.json` 中声明的依赖。
- 不要在源码中使用以 `.ts` 结尾的 import 路径。

## 测试基线

- 测试只需要覆盖当前本地可运行链路。
- 不生成 Redis 或 RocketMQ 测试。
- 不生成基于 JPA 的测试假设。
- 如果需要测试依赖，必须同步更新 `server/pom.xml`。
- 如果当前需求是重置密码，至少覆盖：
  - controller 测试
  - service 测试
  - repository 测试


## 关于其它文档

- `docs/ARCHITECTURE.md`、`docs/INFRA.md`、`docs/REDIS_KEYS.md`、`docs/ROCKETMQ_TOPICS.md` 可以视为“后续集成阶段设计文档”。
- 它们不应覆盖当前默认本地 impl 的 H2 + MyBatis 基线。