package com.example.cau_likelion_spring.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CAU Likelion Spring API is running");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}