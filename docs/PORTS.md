# Ports Reference (Default)

> **Important**: These are the project’s **recommended default ports**. If you change any port, you must update the corresponding configuration files noted below.

## Default ports

| Component | Service | Default port(s) | Where to change |
|---|---|---:|---|
| MySQL | mysql | 3306 | `docker-compose.yml` (ports), `backend/src/main/resources/application.yml` (JDBC URL) |
| Redis | redis | 6379 | `docker-compose.yml` (ports), `backend/src/main/resources/application.yml` (spring.data.redis.*) |
| RocketMQ | namesrv | 9876 | `docker-compose.yml` (ports), `backend/src/main/resources/application.yml` (app.rocketmq.namesrvAddr) |
| RocketMQ | broker | 10911 (listen), 10909 (HA) | `docker-compose.yml` (ports), `rocketmq/broker.conf` |
| Backend API | spring-boot | 8080 | `backend/src/main/resources/application.yml` (server.port) |
| Swagger UI | springdoc | 8080 | same as backend; UI at `/swagger-ui/index.html` |
| Frontend | vite dev server | 5173 | `frontend/vite.config.*` or `package.json` scripts; also any reverse proxy config |

## Notes

- If you change MySQL credentials (user/password/database), update both `docker-compose.yml` and `backend/src/main/resources/application.yml`.
- If you run Docker on a remote host, replace `localhost` in backend configs with the host/IP.