#!/bin/zsh
. ./.secret

set -e

if [ -z "$ANIMALDB_PASSWORD" ]; then
    echo "ANIMALDB_PASSWORD is not set."
    exit 1
fi

flyway -target=$1 \
  -locations=filesystem:../src/main/resources/db/migrations-without-baseline \
  migrate