PGPASSWORD="$ANIMALDB_PASSWORD" pg_dump \
  --data-only \
  --column-inserts \
  --no-owner \
  --no-privileges \
  -U dbadmin \
  -d animaldb \
  > animaldb_data.sql