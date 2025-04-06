package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.PlayerDTO;
import com.example.fantasy_football_backend.dto.PlayerPerformanceDTO;
import com.example.fantasy_football_backend.exceptions.ResourceNotFoundException;
import com.example.fantasy_football_backend.models.Match;
import com.example.fantasy_football_backend.models.Player;
import com.example.fantasy_football_backend.models.PlayerPerformance;
import com.example.fantasy_football_backend.repositories.MatchRepository;
import com.example.fantasy_football_backend.repositories.PlayerPerformanceRepository;
import com.example.fantasy_football_backend.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PlayerPerformanceRepository performanceRepository;
    private final MatchRepository matchRepository;

    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PlayerDTO getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));
        return mapToDTO(player);
    }

    public List<PlayerDTO> getPlayersByTeam(String team) {
        return playerRepository.findByTeam(team).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersByPosition(String position) {
        return playerRepository.findByPosition(position).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getTopPerformers() {
        return playerRepository.findTopPerformers().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PlayerDTO createPlayer(Player player) {
        return mapToDTO(playerRepository.save(player));
    }

    @Transactional
    public PlayerDTO updatePlayer(Long id, Player playerDetails) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + id));

        player.setName(playerDetails.getName());
        player.setPosition(playerDetails.getPosition());
        player.setTeam(playerDetails.getTeam());
        player.setCost(playerDetails.getCost());

        return mapToDTO(playerRepository.save(player));
    }

    @Transactional
    public PlayerPerformanceDTO recordPerformance(Long playerId, Long matchId, PlayerPerformance performance) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        // Check if performance already exists
        PlayerPerformance existingPerformance = performanceRepository
                .findByMatchIdAndPlayerId(matchId, playerId);

        if (existingPerformance != null) {
            // Update existing performance
            existingPerformance.setGoals(performance.getGoals());
            existingPerformance.setAssists(performance.getAssists());
            existingPerformance.setMinutesPlayed(performance.getMinutesPlayed());
            existingPerformance.setCleanSheet(performance.isCleanSheet());
            existingPerformance.setYellowCard(performance.isYellowCard());
            existingPerformance.setRedCard(performance.isRedCard());
            existingPerformance.setSaves(performance.getSaves());

            // Calculate points based on performance
            int points = calculatePoints(existingPerformance);
            existingPerformance.setTotalPoints(points);

            performance = performanceRepository.save(existingPerformance);
        } else {
            // Create new performance
            performance.setPlayer(player);
            performance.setMatch(match);

            // Calculate points based on performance
            int points = calculatePoints(performance);
            performance.setTotalPoints(points);

            performance = performanceRepository.save(performance);
        }

        // Update player's total points
        updatePlayerTotalPoints(player);

        return mapToPerformanceDTO(performance);
    }

    @Transactional
    public void updatePlayerTotalPoints(Player player) {
        Integer totalPoints = performanceRepository.getTotalPointsByPlayerId(player.getId());
        if (totalPoints == null) {
            totalPoints = 0;
        }

        player.setTotalPoints(totalPoints);
        playerRepository.save(player);
    }

    @Transactional
    public void updateAllPlayerPoints() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            updatePlayerTotalPoints(player);
        }
    }
    @Transactional
    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    private int calculatePoints(PlayerPerformance performance) {
        int points = 0;
        String position = performance.getPlayer().getPosition();

        // Points for minutes played
        if (performance.getMinutesPlayed() >= 60) {
            points += 2;
        } else if (performance.getMinutesPlayed() > 0) {
            points += 1;
        }

        // Points for goals
        if (position.equals("GK") || position.equals("DEF")) {
            points += performance.getGoals() * 6;
        } else if (position.equals("MID")) {
            points += performance.getGoals() * 5;
        } else {  // Forward
            points += performance.getGoals() * 4;
        }

        // Points for assists
        points += performance.getAssists() * 3;

        // Points for clean sheet
        if (performance.isCleanSheet() && (position.equals("GK") || position.equals("DEF"))) {
            points += 4;
        } else if (performance.isCleanSheet() && position.equals("MID")) {
            points += 1;
        }

        // Points for saves (only for GK)
        if (position.equals("GK")) {
            points += (performance.getSaves() / 3);
        }

        // Deductions for cards
        if (performance.isYellowCard()) {
            points -= 1;
        }
        if (performance.isRedCard()) {
            points -= 3;
        }

        return points;
    }

    private PlayerDTO mapToDTO(Player player) {
        PlayerDTO dto = new PlayerDTO();
        dto.setId(player.getId());
        dto.setName(player.getName());
        dto.setPosition(player.getPosition());
        dto.setTeam(player.getTeam());
        dto.setCost(player.getCost());
        dto.setTotalPoints(player.getTotalPoints());
        return dto;
    }

    private PlayerPerformanceDTO mapToPerformanceDTO(PlayerPerformance performance) {
        PlayerPerformanceDTO dto = new PlayerPerformanceDTO();
        dto.setId(performance.getId());
        dto.setPlayerId(performance.getPlayer().getId());
        dto.setPlayerName(performance.getPlayer().getName());
        dto.setPosition(performance.getPlayer().getPosition());
        dto.setTeam(performance.getPlayer().getTeam());
        dto.setMatchId(performance.getMatch().getId());
        dto.setGoals(performance.getGoals());
        dto.setAssists(performance.getAssists());
        dto.setMinutesPlayed(performance.getMinutesPlayed());
        dto.setCleanSheet(performance.isCleanSheet());
        dto.setYellowCard(performance.isYellowCard());
        dto.setRedCard(performance.isRedCard());
        dto.setSaves(performance.getSaves());
        dto.setTotalPoints(performance.getTotalPoints());
        return dto;
    }
}