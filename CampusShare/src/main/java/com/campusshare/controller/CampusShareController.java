package com.campusshare.controller;

import com.campusshare.service.CampusShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CampusShareController {

    private final CampusShareService service;

    public CampusShareController(CampusShareService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(service.dashboard());
    }

    @GetMapping("/resources")
    public ResponseEntity<?> resources(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.resources(search));
    }

    @GetMapping("/resources/{id}")
    public ResponseEntity<?> resource(@PathVariable int id) {
        return ResponseEntity.ok(service.resource(id));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> bookings() {
        return ResponseEntity.ok(service.bookings());
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.createBooking(body));
    }

    @GetMapping("/borrow")
    public ResponseEntity<?> borrowTransactions() {
        return ResponseEntity.ok(service.borrowTransactions());
    }

    @PostMapping("/borrow")
    public ResponseEntity<?> borrow(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.borrow(body));
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<?> returnResource(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.returnResource(id, body));
    }

    @GetMapping("/maintenance")
    public ResponseEntity<?> maintenance() {
        return ResponseEntity.ok(service.maintenance());
    }

    @PostMapping("/maintenance")
    public ResponseEntity<?> reportMaintenance(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.reportMaintenance(body));
    }

    @GetMapping("/analytics/category")
    public ResponseEntity<?> categoryAnalytics() {
        return ResponseEntity.ok(service.categoryAnalytics());
    }

    @GetMapping("/analytics/departments")
    public ResponseEntity<?> departmentAnalytics() {
        return ResponseEntity.ok(service.departmentAnalytics());
    }
}
