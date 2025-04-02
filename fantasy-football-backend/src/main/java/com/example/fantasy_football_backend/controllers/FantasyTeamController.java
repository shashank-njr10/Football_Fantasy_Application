package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.FantasyTeamDTO;
import com.example.fantasy_football_backend.services.FantasyTeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fantasy-teams")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FantasyTeamController {
    private final FantasyTeamService fantasyTeamService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FantasyTeamDTO>> getUserTeams(@PathVariable Long userId) {
        return ResponseEntity.ok(fantasyTeamService.getUserTeams(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FantasyTeamDTO> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(fantasyTeamService.getTeamById(id));
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<FantasyTeamDTO> createTeam(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String teamName = payload.get("name");
        return new ResponseEntity<>(
                fantasyTeamService.createTeam(teamName, userId),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<FantasyTeamDTO> addPlayerToTeam(
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(fantasyTeamService.addPlayerToTeam(teamId, playerId));
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    public ResponseEntity<FantasyTeamDTO> removePlayerFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long playerId) {
        return ResponseEntity.ok(fantasyTeamService.removePlayerFromTeam(teamId, playerId));
    }

    @PostMapping("/{teamId}/contests/{contestId}")
    public ResponseEntity<FantasyTeamDTO> joinContest(
            @PathVariable Long teamId,
            @PathVariable Long contestId) {
        return ResponseEntity.ok(fantasyTeamService.joinContest(teamId, contestId));
    }

    @DeleteMapping("/{teamId}/contests/{contestId}")
    public ResponseEntity<Void> leaveContest(
            @PathVariable Long teamId,
            @PathVariable Long contestId) {
        fantasyTeamService.leaveContest(teamId, contestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-scores")
    public ResponseEntity<Void> updateAllTeamScores() {
        fantasyTeamService.updateAllTeamScores();
        return ResponseEntity.ok().build();
    }
}