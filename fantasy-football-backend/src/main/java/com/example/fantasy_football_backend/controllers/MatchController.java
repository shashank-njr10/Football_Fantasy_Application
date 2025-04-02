package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.MatchDTO;
import com.example.fantasy_football_backend.models.Match;
import com.example.fantasy_football_backend.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @GetMapping("/live")
    public ResponseEntity<List<MatchDTO>> getLiveMatches() {
        return ResponseEntity.ok(matchService.getLiveMatches());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MatchDTO>> getUpcomingMatches() {
        return ResponseEntity.ok(matchService.getUpcomingMatches());
    }

    @PostMapping
    public ResponseEntity<MatchDTO> createMatch(@RequestBody Match match) {
        return new ResponseEntity<>(matchService.createMatch(match), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MatchDTO> updateMatchStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String status = (String) payload.get("status");
        int homeScore = (int) payload.get("homeScore");
        int awayScore = (int) payload.get("awayScore");
        return ResponseEntity.ok(matchService.updateMatchStatus(id, status, homeScore, awayScore));
    }

    @PostMapping("/update-scheduled")
    public ResponseEntity<Void> updateScheduledMatchesToLive() {
        matchService.updateScheduledMatchesToLive();
        return ResponseEntity.ok().build();
    }
}