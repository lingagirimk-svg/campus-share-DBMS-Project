# Submission Notes

## Project title
CampusShare — Smart Campus Resource Management & Booking System

## Problem statement
Colleges have shared resources such as projectors, laptops, lab kits, cameras and sports equipment. Manual tracking can cause double booking, forgotten returns and poor visibility of equipment condition. CampusShare centralizes these records in a relational database.

## Objectives
- Maintain a single database for campus resources.
- Allow students to check resources.
- Prevent overlapping bookings.
- Track borrowed and returned resources.
- Record maintenance problems.
- Generate useful database reports.

## ER relationship summary
Users 1 ---- N Bookings
Resources 1 ---- N Bookings
Users 1 ---- N Borrow_Return
Resources 1 ---- N Borrow_Return
Resources 1 ---- N Maintenance

## Tables
users, resources, bookings, borrow_return, maintenance

## Strong DBMS points for presentation
- Primary key in every main table.
- Foreign keys connect related entities.
- ENUM/NOT NULL/UNIQUE/CHECK constraints improve data integrity.
- JOIN queries combine information from multiple tables.
- GROUP BY and COUNT create analytics.
- Booking conflict query checks time overlap.
- Borrowing updates resource availability.
- Returning a damaged item moves it to maintenance status.

## Demo flow
1. Show dashboard counts.
2. Search for a laptop/projector.
3. Create a booking.
4. Try the same time slot again to demonstrate conflict prevention.
5. Borrow an available resource.
6. Show database tables in MySQL Workbench.
7. Run a JOIN query.
8. Run an overdue-resource query.
9. Show category/department analytics.

## Important
Do not present the system as an AI project. Its core strength is relational database design and SQL-based resource management.
