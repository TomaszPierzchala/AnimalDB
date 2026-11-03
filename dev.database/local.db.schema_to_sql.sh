pg_restore \
   --schema-only \
   --no-owner \
   --no-privileges \
   -f animaldb_schema.sql \
   animaldb_full.dump
