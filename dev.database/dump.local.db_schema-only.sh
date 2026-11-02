PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  -h localhost \
  --schema-only \
  --no-owner \
  --no-privileges \
  -U dbadmin \
  -d animaldb \
  --exclude-table=public.flyway_schema_history
# > animaldb_schema.sql
