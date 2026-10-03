CREATE DATABASE IF NOT EXISTS campusshare;
USE campusshare;

DROP TABLE IF EXISTS maintenance;
DROP TABLE IF EXISTS borrow_return;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(15),
    department VARCHAR(80) NOT NULL,
    year_level INT NOT NULL CHECK (year_level BETWEEN 1 AND 4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    resource_name VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    location VARCHAR(120) NOT NULL,
    condition_status ENUM('GOOD','FAIR','DAMAGED') DEFAULT 'GOOD',
    availability_status ENUM('AVAILABLE','BORROWED','MAINTENANCE') DEFAULT 'AVAILABLE'
);

CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    booking_status ENUM('CONFIRMED','CANCELLED','COMPLETED') DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_booking_user FOREIGN KEY(user_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_resource FOREIGN KEY(resource_id) REFERENCES resources(resource_id),
    CONSTRAINT chk_booking_time CHECK (end_time > start_time)
);

CREATE TABLE borrow_return (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    issued_date DATE NOT NULL,
    due_date DATE NOT NULL,
    returned_date DATE NULL,
    return_condition ENUM('GOOD','FAIR','DAMAGED') NULL,
    CONSTRAINT fk_borrow_user FOREIGN KEY(user_id) REFERENCES users(user_id),
    CONSTRAINT fk_borrow_resource FOREIGN KEY(resource_id) REFERENCES resources(resource_id),
    CONSTRAINT chk_due_date CHECK (due_date >= issued_date)
);

CREATE TABLE maintenance (
    maintenance_id INT PRIMARY KEY AUTO_INCREMENT,
    resource_id INT NOT NULL,
    problem VARCHAR(255) NOT NULL,
    reported_date DATE NOT NULL,
    repair_status ENUM('REPORTED','IN_PROGRESS','FIXED') DEFAULT 'REPORTED',
    CONSTRAINT fk_maintenance_resource FOREIGN KEY(resource_id) REFERENCES resources(resource_id)
);

INSERT INTO users(name,email,phone,department,year_level) VALUES
('Mani','mani@campus.edu','9000000001','CSE-AI&DS',2),
('Rahul','rahul@campus.edu','9000000002','CSE',2),
('Sneha','sneha@campus.edu','9000000003','ECE',3),
('Arjun','arjun@campus.edu','9000000004','EEE',2),
('Priya','priya@campus.edu','9000000005','CSE-AI&DS',3),
('Kiran','kiran@campus.edu','9000000006','MECH',4);

INSERT INTO resources(resource_name,category,location,condition_status,availability_status) VALUES
('Epson Projector','Projector','Seminar Hall A','GOOD','AVAILABLE'),
('Dell Laptop 01','Laptop','CSE Lab 1','GOOD','AVAILABLE'),
('Dell Laptop 02','Laptop','CSE Lab 1','GOOD','BORROWED'),
('Canon Camera','Camera','Media Room','GOOD','AVAILABLE'),
('Arduino Starter Kit','Lab Equipment','IoT Lab','GOOD','AVAILABLE'),
('Raspberry Pi Kit','Lab Equipment','IoT Lab','FAIR','AVAILABLE'),
('Football Kit','Sports','Sports Room','GOOD','AVAILABLE'),
('Volleyball Kit','Sports','Sports Room','FAIR','AVAILABLE'),
('Wireless Microphone','Audio','Seminar Hall A','GOOD','MAINTENANCE'),
('Extension Board','Electrical','Store Room','GOOD','AVAILABLE'),
('HDMI Adapter','Accessory','AV Store','GOOD','AVAILABLE'),
('Drawing Tablet','Design','Design Lab','GOOD','AVAILABLE');

INSERT INTO bookings(user_id,resource_id,booking_date,start_time,end_time,booking_status) VALUES
(1,1,DATE_ADD(CURDATE(),INTERVAL 1 DAY),'10:00:00','11:00:00','CONFIRMED'),
(2,4,DATE_ADD(CURDATE(),INTERVAL 1 DAY),'14:00:00','15:30:00','CONFIRMED'),
(3,7,DATE_ADD(CURDATE(),INTERVAL 2 DAY),'16:00:00','18:00:00','CONFIRMED'),
(5,5,DATE_ADD(CURDATE(),INTERVAL 3 DAY),'11:00:00','12:00:00','CONFIRMED'),
(1,1,DATE_SUB(CURDATE(),INTERVAL 2 DAY),'09:00:00','10:00:00','COMPLETED');

INSERT INTO borrow_return(user_id,resource_id,issued_date,due_date,returned_date,return_condition) VALUES
(2,3,DATE_SUB(CURDATE(),INTERVAL 2 DAY),DATE_ADD(CURDATE(),INTERVAL 3 DAY),NULL,NULL),
(4,6,DATE_SUB(CURDATE(),INTERVAL 8 DAY),DATE_SUB(CURDATE(),INTERVAL 2 DAY),NULL,NULL),
(1,10,DATE_SUB(CURDATE(),INTERVAL 10 DAY),DATE_SUB(CURDATE(),INTERVAL 7 DAY),DATE_SUB(CURDATE(),INTERVAL 6 DAY),'GOOD');

INSERT INTO maintenance(resource_id,problem,reported_date,repair_status) VALUES
(9,'Microphone battery and audio jack need replacement',CURDATE(),'IN_PROGRESS'),
(6,'Raspberry Pi power adapter is loose',DATE_SUB(CURDATE(),INTERVAL 5 DAY),'REPORTED');

-- DBMS demonstration queries
-- 1. JOIN: current bookings with student and resource
SELECT b.booking_id,u.name,r.resource_name,b.booking_date,b.start_time,b.end_time
FROM bookings b JOIN users u ON b.user_id=u.user_id
JOIN resources r ON b.resource_id=r.resource_id;

-- 2. GROUP BY: resources by category
SELECT category, COUNT(*) AS total_resources
FROM resources GROUP BY category ORDER BY total_resources DESC;

-- 3. Overdue resources
SELECT br.transaction_id,u.name,r.resource_name,br.due_date
FROM borrow_return br
JOIN users u ON br.user_id=u.user_id
JOIN resources r ON br.resource_id=r.resource_id
WHERE br.returned_date IS NULL AND br.due_date < CURDATE();

-- 4. Most-booked resources
SELECT r.resource_name,COUNT(b.booking_id) AS times_booked
FROM resources r LEFT JOIN bookings b ON r.resource_id=b.resource_id
GROUP BY r.resource_id,r.resource_name
ORDER BY times_booked DESC;

-- 5. Department usage
SELECT u.department,COUNT(b.booking_id) AS booking_count
FROM users u LEFT JOIN bookings b ON u.user_id=b.user_id
GROUP BY u.department ORDER BY booking_count DESC;
