package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.PlayerDTO;
import com.example.fantasy_football_backend.dto.PlayerPerformanceDTO;
import com.example.fantasy_football_backend.models.Player;
import com.example.fantasy_football_backend.models.PlayerPerformance;
import com.example.fantasy_football_backend.services.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayerById(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    @GetMapping("/team/{team}")
    public ResponseEntity<List<PlayerDTO>> getPlayersByTeam(@PathVariable String team) {
        return ResponseEntity.ok(playerService.getPlayersByTeam(team));
    }

    @GetMapping("/position/{position}")
    public ResponseEntity<List<PlayerDTO>> getPlayersByPosition(@PathVariable String position) {
        return ResponseEntity.ok(playerService.getPlayersByPosition(position));
    }

    @GetMapping("/top-performers")
    public ResponseEntity<List<PlayerDTO>> getTopPerformers() {
        return ResponseEntity.ok(playerService.getTopPerformers());
    }

    @PostMapping
    public ResponseEntity<PlayerDTO> createPlayer(@RequestBody Player player) {
        return new ResponseEntity<>(playerService.createPlayer(player), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerDTO> updatePlayer(@PathVariable Long id, @RequestBody Player player) {
        return ResponseEntity.ok(playerService.updatePlayer(id, player));
    }

    @PostMapping("/{playerId}/matches/{matchId}/performance")
    public ResponseEntity<PlayerPerformanceDTO> recordPerformance(
            @PathVariable Long playerId,
            @PathVariable Long matchId,
            @RequestBody PlayerPerformance performance) {
        return ResponseEntity.ok(playerService.recordPerformance(playerId, matchId, performance));
    }

    @PostMapping("/update-points")
    public ResponseEntity<Void> updateAllPlayerPoints() {
        playerService.updateAllPlayerPoints();
        return ResponseEntity.ok().build();
    }
}