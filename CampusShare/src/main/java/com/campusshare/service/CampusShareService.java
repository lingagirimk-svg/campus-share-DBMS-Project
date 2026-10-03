package com.campusshare.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CampusShareService {

    private final JdbcTemplate db;

    public CampusShareService(JdbcTemplate db) {
        this.db = db;
    }

    public Map<String, Object> dashboard() {
        return Map.of(
            "totalResources", scalar("SELECT COUNT(*) FROM resources"),
            "availableResources", scalar("SELECT COUNT(*) FROM resources WHERE availability_status='AVAILABLE'"),
            "borrowedResources", scalar("SELECT COUNT(*) FROM resources WHERE availability_status='BORROWED'"),
            "maintenanceResources", scalar("SELECT COUNT(*) FROM resources WHERE availability_status='MAINTENANCE'"),
            "totalBookings", scalar("SELECT COUNT(*) FROM bookings"),
            "overdueItems", scalar("""
                SELECT COUNT(*) FROM borrow_return
                WHERE returned_date IS NULL AND due_date < CURDATE()
            """)
        );
    }

    public List<Map<String, Object>> resources(String search) {
        if (search == null || search.isBlank()) {
            return db.queryForList("""
                SELECT r.resource_id, r.resource_name, r.category, r.location,
                       r.condition_status, r.availability_status,
                       COUNT(b.booking_id) AS booking_count
                FROM resources r
                LEFT JOIN bookings b ON r.resource_id=b.resource_id
                GROUP BY r.resource_id, r.resource_name, r.category, r.location,
                         r.condition_status, r.availability_status
                ORDER BY r.resource_id
            """);
        }
        String q = "%" + search.trim() + "%";
        return db.queryForList("""
            SELECT r.resource_id, r.resource_name, r.category, r.location,
                   r.condition_status, r.availability_status,
                   COUNT(b.booking_id) AS booking_count
            FROM resources r
            LEFT JOIN bookings b ON r.resource_id=b.resource_id
            WHERE r.resource_name LIKE ? OR r.category LIKE ? OR r.location LIKE ?
            GROUP BY r.resource_id, r.resource_name, r.category, r.location,
                     r.condition_status, r.availability_status
            ORDER BY r.resource_id
        """, q, q, q);
    }

    public Map<String, Object> resource(int id) {
        return db.queryForMap("SELECT * FROM resources WHERE resource_id=?", id);
    }

    public List<Map<String, Object>> bookings() {
        return db.queryForList("""
            SELECT b.booking_id, u.name AS student, r.resource_name,
                   b.booking_date, b.start_time, b.end_time, b.booking_status
            FROM bookings b
            JOIN users u ON b.user_id=u.user_id
            JOIN resources r ON b.resource_id=r.resource_id
            ORDER BY b.booking_date DESC, b.start_time DESC
        """);
    }

    @Transactional
    public Map<String, Object> createBooking(Map<String, Object> body) {
        int userId = Integer.parseInt(body.get("userId").toString());
        int resourceId = Integer.parseInt(body.get("resourceId").toString());
        String date = body.get("bookingDate").toString();
        String start = body.get("startTime").toString();
        String end = body.get("endTime").toString();

        Integer conflicts = db.queryForObject("""
            SELECT COUNT(*) FROM bookings
            WHERE resource_id=? AND booking_date=? AND booking_status='CONFIRMED'
              AND start_time < ? AND end_time > ?
        """, Integer.class, resourceId, date, end, start);

        if (conflicts != null && conflicts > 0) {
            throw new IllegalArgumentException("This resource is already booked for the selected time.");
        }

        db.update("""
            INSERT INTO bookings(user_id, resource_id, booking_date, start_time, end_time, booking_status)
            VALUES (?, ?, ?, ?, ?, 'CONFIRMED')
        """, userId, resourceId, date, start, end);

        return Map.of("message", "Booking created successfully.");
    }

    public List<Map<String, Object>> borrowTransactions() {
        return db.queryForList("""
            SELECT br.transaction_id, u.name AS student, r.resource_name,
                   br.issued_date, br.due_date, br.returned_date, br.return_condition
            FROM borrow_return br
            JOIN users u ON br.user_id=u.user_id
            JOIN resources r ON br.resource_id=r.resource_id
            ORDER BY br.transaction_id DESC
        """);
    }

    @Transactional
    public Map<String, Object> borrow(Map<String, Object> body) {
        int userId = Integer.parseInt(body.get("userId").toString());
        int resourceId = Integer.parseInt(body.get("resourceId").toString());
        String dueDate = body.get("dueDate").toString();

        Integer available = db.queryForObject(
            "SELECT COUNT(*) FROM resources WHERE resource_id=? AND availability_status='AVAILABLE'",
            Integer.class, resourceId
        );

        if (available == null || available == 0) {
            throw new IllegalArgumentException("Resource is not currently available.");
        }

        db.update("""
            INSERT INTO borrow_return(user_id, resource_id, issued_date, due_date)
            VALUES (?, ?, CURDATE(), ?)
        """, userId, resourceId, dueDate);

        db.update("UPDATE resources SET availability_status='BORROWED' WHERE resource_id=?", resourceId);

        return Map.of("message", "Resource issued successfully.");
    }

    @Transactional
    public Map<String, Object> returnResource(int transactionId, Map<String, Object> body) {
        String condition = body.getOrDefault("condition", "GOOD").toString();

        Map<String, Object> tx = db.queryForMap(
            "SELECT resource_id FROM borrow_return WHERE transaction_id=? AND returned_date IS NULL",
            transactionId
        );
        int resourceId = ((Number) tx.get("resource_id")).intValue();

        db.update("""
            UPDATE borrow_return
            SET returned_date=CURDATE(), return_condition=?
            WHERE transaction_id=? AND returned_date IS NULL
        """, condition, transactionId);

        db.update("""
            UPDATE resources
            SET availability_status=CASE
                WHEN ?='DAMAGED' THEN 'MAINTENANCE'
                ELSE 'AVAILABLE'
            END
            WHERE resource_id=?
        """, condition, resourceId);

        return Map.of("message", "Resource returned successfully.");
    }

    public List<Map<String, Object>> maintenance() {
        return db.queryForList("""
            SELECT m.maintenance_id, r.resource_name, m.problem,
                   m.reported_date, m.repair_status
            FROM maintenance m
            JOIN resources r ON m.resource_id=r.resource_id
            ORDER BY m.maintenance_id DESC
        """);
    }

    @Transactional
    public Map<String, Object> reportMaintenance(Map<String, Object> body) {
        int resourceId = Integer.parseInt(body.get("resourceId").toString());
        String problem = body.get("problem").toString();

        db.update("""
            INSERT INTO maintenance(resource_id, problem, reported_date, repair_status)
            VALUES (?, ?, CURDATE(), 'REPORTED')
        """, resourceId, problem);

        db.update("UPDATE resources SET availability_status='MAINTENANCE' WHERE resource_id=?", resourceId);

        return Map.of("message", "Maintenance report created.");
    }

    public List<Map<String, Object>> categoryAnalytics() {
        return db.queryForList("""
            SELECT category, COUNT(*) AS resource_count,
                   SUM(availability_status='AVAILABLE') AS available_count,
                   SUM(availability_status='BORROWED') AS borrowed_count
            FROM resources
            GROUP BY category
            ORDER BY resource_count DESC
        """);
    }

    public List<Map<String, Object>> departmentAnalytics() {
        return db.queryForList("""
            SELECT u.department, COUNT(b.booking_id) AS booking_count
            FROM users u
            LEFT JOIN bookings b ON u.user_id=b.user_id
            GROUP BY u.department
            ORDER BY booking_count DESC
        """);
    }

    private Object scalar(String sql) {
        return db.queryForObject(sql, Object.class);
    }
}
