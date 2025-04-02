package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.services.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<AnalyticsService.UserPerformanceStats> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserPerformanceStats(userId));
    }

    @GetMapping("/contest/{contestId}")
    public ResponseEntity<AnalyticsService.ContestStats> getContestStats(@PathVariable Long contestId) {
        return ResponseEntity.ok(analyticsService.getContestStats(contestId));
    }

    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(analyticsService.getSystemStats());
    }
}