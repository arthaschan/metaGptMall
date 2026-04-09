# 项目背景

- **目标**: 提供一个电商平台的基础框架
- **技术栈**: Spring Boot, React, Redis, RocketMQ
- **代码结构**: 主要分为后端和前端模块
- **运行步骤**:
  - 环境准备: Docker 和 Docker Compose
  - 启动后端: `docker-compose up backend`
  - 启动前端: `cd frontend && npm start`

- **核心流程**:
  - 用户认证
  - 产品浏览
  - 购物车管理

- **Redis键约定**:
  - `ecom:product:{id}`

- **RocketMQ主题约定**:
  - `order.created`

- **API列表**:
  - `/auth/login`
  - `/products`
  - `/cart`
  - `/orders`

- **JWT声明**:
  - `sub`: 用户ID
  - `exp`: 过期时间

- **TODO路线图**:
  - [ ] 完善文档
  - [ ] 增加支付功能
  - [ ] 编写单元测试

**注意**: 端口/密码/密钥需手动修改，配置文件位置：
- `docker-compose.yml`
- `backend/src/main/resources/application.yml`
- `.env` files