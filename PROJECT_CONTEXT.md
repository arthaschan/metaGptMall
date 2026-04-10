# 项目背景

- **目标**: 提供一个电商平台的基础框架
- **技术栈**: Spring Boot, Vue 3, H2（本地默认）, MyBatis
- **代码结构**: 后端代码位于 `server/`，前端代码位于 `web/`
- **运行步骤**:
  - 环境准备: Docker 和 Docker Compose
  - 本地默认启动不依赖 MySQL、Redis、RocketMQ
  - 启动后端: `cd server && mvn spring-boot:run`
  - 启动前端: `cd web && npm install && npm run dev`

- **核心流程**:
  - 用户认证
  - 产品浏览
  - 购物车管理

- **当前本地后端约定**:
  - 数据源默认使用 H2 内存库
  - 产品查询使用 MyBatis mapper
  - H2 控制台路径: `/h2-console`

- **API列表**:
  - `/api/auth/login`
  - `/api/products`
  - `/api/cart`
  - `/api/orders`
  - `/api/health`

- **JWT声明**:
  - `sub`: 用户ID
  - `exp`: 过期时间

- **TODO路线图**:
  - [ ] 完善文档
  - [ ] 增加支付功能
  - [ ] 编写单元测试

**注意**: 端口/密码/密钥需手动修改，配置文件位置：
- `docker-compose.yml`
- `server/src/main/resources/application.yml`
- `.env` files