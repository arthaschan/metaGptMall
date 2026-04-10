# Ports Reference (Default)

> **Important**: distinguish local default ports from optional integration ports.

## Local default ports

| Component | Service | Default port(s) | Where to change |
|---|---|---:|---|
| Backend API | spring-boot | 8080 | `server/src/main/resources/application.yml` (server.port) |
| Swagger UI | springdoc | 8080 | same as backend; UI at `/swagger-ui/index.html` |
| H2 Console | h2-console | 8080 | `server/src/main/resources/application.yml` (`spring.h2.console.path`) |
| Frontend | vite dev server | 5173 | `web/vite.config.ts` or `web/package.json` |

## Optional integration ports

| Component | Service | Default port(s) | Where to change |
|---|---|---:|---|
| MySQL | mysql | 3306 | `docker-compose.yml`, `sql/00_create_db_mysql.sql` |
| Redis | redis | 6379 | `docker-compose.yml` |
| RocketMQ | namesrv | 9876 | `docker-compose.yml`, `rocketmq/broker.conf` |
| RocketMQ | broker | 10911 (listen), 10909 (HA) | `docker-compose.yml`, `rocketmq/broker.conf` |

## Notes

- Local default impl does not require MySQL, Redis, or RocketMQ.
- If you later switch to integrated infrastructure, update both the Docker files and backend config together.