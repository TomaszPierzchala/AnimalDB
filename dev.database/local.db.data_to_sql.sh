pg_restore \
   --data-only \
   --no-owner \
   --no-privileges \
   -f animaldb_data-only.sql \
   animaldb_full.dump
