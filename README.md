# metaGptMall (MVP)

## Quick start

### Prerequisites
- Docker + Docker Compose

### Ports
See `docs/PORTS.md`.

### Default credentials (DEV ONLY)
> IMPORTANT: passwords/secrets/ports must be manually changed before any real deployment.

- MySQL root password: `root` (dev only)
- MySQL app user: `test`
- MySQL app password: `123456`
- Database: `ecommerce`

Where to change:
- `docker-compose.yml` (MYSQL_* env vars, ports)
- `sql/00_create_db_mysql.sql` (CREATE USER/GRANT)
- future backend config: `backend/src/main/resources/application.yml`

### Start infra
```bash
docker compose up -d
```

### Init DB schema & seed
```bash
bash scripts/db_init.sh
```

### RocketMQ
Broker config: `rocketmq/broker.v2.conf` (dev).
