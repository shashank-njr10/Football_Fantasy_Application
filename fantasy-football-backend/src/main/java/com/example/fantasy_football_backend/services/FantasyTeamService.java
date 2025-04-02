package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.FantasyTeamDTO;
import com.example.fantasy_football_backend.dto.PlayerDTO;
import com.example.fantasy_football_backend.exceptions.BadRequestException;
import com.example.fantasy_football_backend.exceptions.ResourceNotFoundException;
import com.example.fantasy_football_backend.models.*;
import com.example.fantasy_football_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FantasyTeamService {
    private final FantasyTeamRepository fantasyTeamRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ContestRepository contestRepository;
    private final ContestEntryRepository contestEntryRepository;

    private static final int MAX_TEAM_BUDGET = 100;
    private static final int MAX_PLAYERS_PER_REAL_TEAM = 4;

    public List<FantasyTeamDTO> getUserTeams(Long userId) {
        return fantasyTeamRepository.findByOwnerId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FantasyTeamDTO getTeamById(Long teamId) {
        FantasyTeam team = fantasyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found with id: " + teamId));
        return mapToDTO(team);
    }

    @Transactional
    public FantasyTeamDTO createTeam(String teamName, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        FantasyTeam team = new FantasyTeam();
        team.setName(teamName);
        team.setOwner(user);
        team.setTotalPoints(0);
        team.setTotalScore(0);

        return mapToDTO(fantasyTeamRepository.save(team));
    }

    @Transactional
    public FantasyTeamDTO addPlayerToTeam(Long teamId, Long playerId) {
        FantasyTeam team = fantasyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found with id: " + teamId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        // Check team budget constraint
        int currentTeamCost = team.getPlayers().stream().mapToInt(Player::getCost).sum();
        if (currentTeamCost + player.getCost() > MAX_TEAM_BUDGET) {
            throw new BadRequestException("Adding this player would exceed the team budget of 100 points");
        }

        // Check players per real team constraint
        String playerTeam = player.getTeam();
        int playersFromSameTeam = fantasyTeamRepository.countPlayersByTeam(playerTeam, teamId);
        if (playersFromSameTeam >= MAX_PLAYERS_PER_REAL_TEAM) {
            throw new BadRequestException("Maximum of 4 players from the same team allowed");
        }

        team.getPlayers().add(player);
        return mapToDTO(fantasyTeamRepository.save(team));
    }

    @Transactional
    public FantasyTeamDTO removePlayerFromTeam(Long teamId, Long playerId) {
        FantasyTeam team = fantasyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found with id: " + teamId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with id: " + playerId));

        team.getPlayers().remove(player);
        return mapToDTO(fantasyTeamRepository.save(team));
    }

    @Transactional
    public FantasyTeamDTO joinContest(Long teamId, Long contestId) {
        FantasyTeam team = fantasyTeamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Fantasy team not found with id: " + teamId));

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        // Check if team has 11 players
        if (team.getPlayers().size() != 11) {
            throw new BadRequestException("Team must have exactly 11 players to join a contest");
        }

        // Check if user already has a team in this contest
        Optional<ContestEntry> existingEntry = contestEntryRepository.findByContestIdAndTeamId(contestId, teamId);
        if (existingEntry.isPresent()) {
            throw new BadRequestException("This team is already part of the contest");
        }

        // Check if contest has reached max teams
        int currentEntries = contestEntryRepository.countByContestId(contestId);
        if (contest.getMaxTeams() > 0 && currentEntries >= contest.getMaxTeams()) {
            throw new BadRequestException("Contest has reached maximum number of teams");
        }

        // Create new contest entry
        ContestEntry entry = new ContestEntry();
        entry.setContest(contest);
        entry.setTeam(team);
        entry.setScore(0);
        entry.setRank(0);
        entry.setJoinedAt(LocalDateTime.now());

        contestEntryRepository.save(entry);

        return mapToDTO(team);
    }

    @Transactional
    public void leaveContest(Long teamId, Long contestId) {
        ContestEntry entry = contestEntryRepository.findByContestIdAndTeamId(contestId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team is not part of this contest"));

        contestEntryRepository.delete(entry);
    }

    @Transactional
    public void updateAllTeamScores() {
        List<FantasyTeam> teams = fantasyTeamRepository.findAll();

        for (FantasyTeam team : teams) {
            int totalScore = calculateTeamScore(team);
            team.setTotalScore(totalScore);
            fantasyTeamRepository.save(team);

            // Update all contest entries for this team
            for (ContestEntry entry : team.getContestEntries()) {
                entry.setScore(totalScore);
                contestEntryRepository.save(entry);
            }
        }

        // Update rankings in each contest
        List<Contest> contests = contestRepository.findAll();
        for (Contest contest : contests) {
            List<ContestEntry> entries = contestEntryRepository.findByContestIdOrderByScoreDesc(contest.getId());

            for (int i = 0; i < entries.size(); i++) {
                ContestEntry entry = entries.get(i);
                entry.setRank(i + 1);
                contestEntryRepository.save(entry);
            }
        }
    }

    private int calculateTeamScore(FantasyTeam team) {
        return team.getPlayers().stream()
                .mapToInt(Player::getTotalPoints)
                .sum();
    }

    private FantasyTeamDTO mapToDTO(FantasyTeam team) {
        FantasyTeamDTO dto = new FantasyTeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setTotalPoints(team.getTotalPoints());
        dto.setTotalScore(team.getTotalScore());
        dto.setOwnerId(team.getOwner().getId());
        dto.setOwnerName(team.getOwner().getUsername());

        // Map players
        List<PlayerDTO> playerDTOs = team.getPlayers().stream()
                .map(player -> {
                    PlayerDTO playerDTO = new PlayerDTO();
                    playerDTO.setId(player.getId());
                    playerDTO.setName(player.getName());
                    playerDTO.setPosition(player.getPosition());
                    playerDTO.setTeam(player.getTeam());
                    playerDTO.setCost(player.getCost());
                    playerDTO.setTotalPoints(player.getTotalPoints());
                    return playerDTO;
                })
                .collect(Collectors.toList());

        dto.setPlayers(playerDTOs);

        // Calculate used and remaining points
        int usedPoints = team.getPlayers().stream().mapToInt(Player::getCost).sum();
        dto.setUsedPoints(usedPoints);
        dto.setRemainingPoints(MAX_TEAM_BUDGET - usedPoints);

        return dto;
    }
}