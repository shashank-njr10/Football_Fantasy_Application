package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.services.FootballApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/test")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApiTestController {

    private final FootballApiService footballApiService;

    @PostMapping("/update-matches/{competitionCode}")
    public ResponseEntity<Map<String, String>> testUpdateMatches(@PathVariable String competitionCode) {
        footballApiService.updateMatches(competitionCode);
        return ResponseEntity.ok(Map.of("status", "Successfully updated matches for competition: " + competitionCode));
    }

    @PostMapping("/update-team-players/{teamCode}")
    public ResponseEntity<Map<String, String>> testUpdateTeamPlayers(@PathVariable String teamCode) {
        footballApiService.updateTeamPlayers(teamCode);
        return ResponseEntity.ok(Map.of("status", "Successfully updated players for team: " + teamCode));
    }

    @PostMapping("/update-match-details/{matchId}")
    public ResponseEntity<Map<String, String>> testUpdateMatchDetails(@PathVariable Long matchId) {
        footballApiService.updateMatchDetails(matchId);
        return ResponseEntity.ok(Map.of("status", "Successfully updated match details for match: " + matchId));
    }
}