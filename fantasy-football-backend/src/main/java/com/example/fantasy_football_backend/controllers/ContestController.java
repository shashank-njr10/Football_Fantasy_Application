package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.ContestDTO;
import com.example.fantasy_football_backend.models.Contest;
import com.example.fantasy_football_backend.services.AnalyticsService;
import com.example.fantasy_football_backend.services.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contests")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ContestController {
    private final ContestService contestService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<List<ContestDTO>> getAllContests() {
        return ResponseEntity.ok(contestService.getAllContests());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ContestDTO>> getActiveContests() {
        return ResponseEntity.ok(contestService.getActiveContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDTO> getContestById(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContestById(id));
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<List<ContestDTO>> getContestsByAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(contestService.getContestsByAdmin(adminId));
    }

    @PostMapping("/{adminId}")
    public ResponseEntity<ContestDTO> createContest(@PathVariable Long adminId, @RequestBody Contest contest) {
        return new ResponseEntity<>(contestService.createContest(contest, adminId), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContestDTO> updateContest(@PathVariable Long id, @RequestBody Contest contest) {
        return ResponseEntity.ok(contestService.updateContest(id, contest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContest(@PathVariable Long id) {
        contestService.deleteContest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<AnalyticsService.ContestStats> getContestStats(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getContestStats(id));
    }
}