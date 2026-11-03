#!/bin/zsh
set -euo pipefail

PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  -h localhost \
  -U dbadmin \
  -d animaldb \
  -Fc \
  --exclude-table=public.flyway_schema_history \
  -f animaldb_full.dump
