# deprecated because of FK constrains (mouse table)
PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  -h localhost \
  --data-only \
  --column-inserts \
  --no-owner \
  --no-privileges \
  -U dbadmin \
  -d animaldb \
  --exclude-table=public.flyway_schema_history
#  > animaldb_data.sql