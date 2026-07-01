#!/bin/bash

set -e

if [ -z "$ANIMALDB_PASSWORD" ]; then
    echo "ANIMALDB_PASSWORD is not set."
    exit 1
fi

echo "Dropping database..."
psql -d postgres -f cancel-connection-and-drop-db.sql

echo "Creating database..."
psql -d postgres \
     -v password="$ANIMALDB_PASSWORD" \
     -f create-db.sql

echo "Done."
