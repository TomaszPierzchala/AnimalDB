# AnimalDB

AnimalDB is a laboratory animal management system for tracking mice, breeding, genealogy, and experimental data.

This project is a Spring Boot backend application that uses PostgreSQL for data storage.  
It provides a REST API for managing laboratory animal data, which will be consumed by a React frontend.

Tech stack: Java 21, Spring Boot, PostgreSQL, Flyway, React.

## Getting Started

Before running the application for the first time, create the PostgreSQL database and application user as described in the **Local Database Setup** please follow the [README](dev.database/README.md) [](section below - comments).

Then start the application:

```bash
./mvnw spring-boot:run
```

## TESTS
### JPA entities 
It uses testcontainers-junit-jupiter, so needs a local **Docker Destop** to be installed.