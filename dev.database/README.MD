# Local Database Setup

## 1. Installation of PostgreSQL
#### at Mac 
```bash
brew install postgresql@17
```
#### start it as a service
```bash
brew services start postgresql@17
```

#### test default connection to the local PostgreSQL
```bash
psql postgres
```
## 2. Create local animaldb
### set the secret as an environment variable
```bash
$ export ANIMALDB_PASSWORD='ExampleOfMyVerySecretPassword'
```
### (or at Windows)
```powershell
$env:ANIMALDB_PASSWORD="<your-password>"
```

### run SQL script to create PostgreSQL user and DB
```bash
$ psql -h localhost -U tomek -d postgres -v password="$ANIMALDB_PASSWORD" -f create-db.sql
```

### test connection to newly created DB
```bash
$ psql -h localhost -U animal_app -d animaldb
```
### then run SQL
```SQL
SELECT current_database(), current_user;
```
### you should see
```
current_database | current_user
------------------+--------------
 animaldb         | animal_app
```

## 2B. Eventually drop animaldb
```bash
$ psql -d postgres -f cancel-connection-and-drop-db.sql
```
### or recrete an empty animaldb
```bash
$ ./recreate-db.sh
```

## 3. Configure Spring Boot

The application expects the following configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/animaldb
    username: animal_app
    password: ${ANIMALDB_PASSWORD}
```

> **Note:** Never commit passwords or other secrets to Git.
