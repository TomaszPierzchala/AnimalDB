PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  --schema-only \
  --no-owner \
  --no-privileges \
  -U dbadmin \
  -d animaldb \
  > animaldb_schema.sql
