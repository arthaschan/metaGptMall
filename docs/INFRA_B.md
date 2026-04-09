# Infrastructure (DEV)

> IMPORTANT: ports/passwords/secrets are DEV defaults and MUST be manually modified for production.

## MySQL
- Host: `localhost`
- Port: `3306`
- Root password: `root`
- App user/password: `test` / `123456`
- Database: `ecommerce`

Where to change:
- `docker-compose.yml`
- `sql/00_create_db_mysql.sql`

## Redis
- Host: `localhost`
- Port: `6379`

## RocketMQ 4.9.x
- NameServer (host access): `localhost:9876`
- NameServer (in-docker): `rocketmq-namesrv:9876`

Broker config file (docker): `rocketmq/broker.v2.conf`