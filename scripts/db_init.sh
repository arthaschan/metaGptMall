#!/usr/bin/env bash
set -euo pipefail

echo "[1/3] Start MySQL"
docker compose up -d mysql

MYSQL_CID="
$(docker ps -qf name=mysql)"
if [ -z "${MYSQL_CID}" ]; then
  echo "MySQL container not found."
  exit 1
fi

echo "[2/3] Wait for MySQL ready..."
for i in {1..60}; do
  if docker exec "${MYSQL_CID}" mysqladmin ping -uroot -proot --silent; then
    break
  fi
  sleep 2
done

echo "[3/3] Apply SQL (as root)"
docker exec -i "${MYSQL_CID}" mysql -uroot -proot < sql/00_create_db_mysql.sql
docker exec -i "${MYSQL_CID}" mysql -uroot -proot < sql/01_schema_mysql.sql
docker exec -i "${MYSQL_CID}" mysql -uroot -proot < sql/02_seed_mysql.sql
echo "Done. Default MySQL app user: test / 123456 (DEV ONLY; change for production)."