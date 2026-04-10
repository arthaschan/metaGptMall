# Infrastructure

> 当前需要区分“本地默认实现”与“后续集成基础设施”。

## 1. 本地默认实现

- 本地默认后端不依赖 MySQL、Redis、RocketMQ。
- 默认数据源为 H2 内存库。
- H2 控制台：`/h2-console`
- 启动后会自动执行：
	- `server/src/main/resources/schema.sql`
	- `server/src/main/resources/data.sql`

## 2. 后续集成基础设施

以下基础设施属于后续集成阶段，不是当前本地 impl 的默认前置：

### MySQL

- Host: `localhost`
- Port: `3306`
- Root password: `root`
- App user/password: `test` / `123456`
- Database: `ecommerce`

Where to change:
- `docker-compose.yml`
- `sql/00_create_db_mysql.sql`

### Redis

- Host: `localhost`
- Port: `6379`

### RocketMQ 4.9.x

- NameServer (host access): `localhost:9876`
- NameServer (in-docker): `rocketmq-namesrv:9876`
- Broker config file (docker): `rocketmq/broker.conf`

## 3. 使用原则

- 如果目标是“本地先跑通前后端”，优先按 H2 本地基线实现。
- 如果目标是“进入集成环境或 Docker 环境”，再引入 MySQL、Redis、RocketMQ。
