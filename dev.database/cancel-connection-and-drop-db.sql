SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'animaldb'
  AND pid <> pg_backend_pid();

DROP DATABASE IF EXISTS animaldb;

DROP ROLE IF EXISTS animal_app;