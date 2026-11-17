#!/bin/zsh
set -euo pipefail

. ./.secret

if [ -z "$ANIMALDB_PASSWORD" ]; then
    echo "ANIMALDB_PASSWORD is not set."
    exit 1
fi

PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  -h localhost \
  -U dbadmin \
  -d animaldb \
  -Fc \
  --exclude-table=public.flyway_schema_history \
  -f animaldb_full.dump
