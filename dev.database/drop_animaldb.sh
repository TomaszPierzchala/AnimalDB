#!/bin/bash
. ./.secret

set -e

if [ -z "$ANIMALDB_PASSWORD" ]; then
    echo "ANIMALDB_PASSWORD is not set."
    exit 1
fi

echo "Dropping database..."
psql -d postgres -f cancel-connection-and-drop-db.sql
