#!/usr/bin/env bash
set -eo pipefail
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
 --set=auth_password="$AUTH_DB_PASSWORD" --set=customer_password="$CUSTOMER_DB_PASSWORD" --set=order_password="$ORDER_DB_PASSWORD" <<'SQL'
CREATE ROLE auth_app LOGIN PASSWORD :'auth_password';
CREATE ROLE customer_app LOGIN PASSWORD :'customer_password';
CREATE ROLE order_app LOGIN PASSWORD :'order_password';
CREATE DATABASE auth_db OWNER auth_app;
CREATE DATABASE customer_db OWNER customer_app;
CREATE DATABASE order_db OWNER order_app;
REVOKE ALL ON DATABASE auth_db FROM PUBLIC;
REVOKE ALL ON DATABASE customer_db FROM PUBLIC;
REVOKE ALL ON DATABASE order_db FROM PUBLIC;
SQL
