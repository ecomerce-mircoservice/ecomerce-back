#!/bin/sh
set -e

# Create databases only if they do not exist
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
  -- Auth DB
  SELECT 'CREATE DATABASE $DB_NAME_AUTH'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME_AUTH')\gexec

  -- Product DB
  SELECT 'CREATE DATABASE $DB_NAME_PRODUCT'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME_PRODUCT')\gexec

  -- Order DB
  SELECT 'CREATE DATABASE $DB_NAME_ORDER'
  WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB_NAME_ORDER')\gexec
EOSQL
