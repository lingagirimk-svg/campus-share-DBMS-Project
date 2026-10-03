# CampusShare — Smart Campus Resource Management & Booking System

## Technology
- Java 17
- Spring Boot 3.3.5
- Maven
- MySQL
- MySQL Workbench
- HTML, CSS, JavaScript
- VS Code / Eclipse

## Main DBMS concepts
Primary keys, foreign keys, constraints, joins, aggregate functions, GROUP BY, ORDER BY, filtering, transactions, and relational database design.

## Setup

### 1. Create the database
Open MySQL Workbench and run:
`database/campusshare.sql`

This creates the `campusshare` database, tables, relationships, and sample data.

### 2. Configure MySQL password
Open:
`src/main/resources/application.properties`

Change:
`spring.datasource.password=CHANGE_ME`

to your MySQL root password.

### 3. Run
From the project folder:

`mvn spring-boot:run`

or run `CampusShareApplication.java` from Eclipse/VS Code.

### 4. Open
`http://localhost:8080`

## Main modules
1. Dashboard
2. Resource search
3. Resource booking
4. Borrow / return tracking
5. Maintenance tracking
6. Database analytics

## Suggested viva explanation
The database is the core of CampusShare. Users are connected to bookings and borrow/return transactions through foreign keys. Resources are reused by several modules. SQL joins produce meaningful reports, while constraints protect data integrity.
