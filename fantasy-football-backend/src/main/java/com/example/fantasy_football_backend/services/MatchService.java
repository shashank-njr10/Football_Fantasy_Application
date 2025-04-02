package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.MatchDTO;
import com.example.fantasy_football_backend.exceptions.ResourceNotFoundException;
import com.example.fantasy_football_backend.models.Match;
import com.example.fantasy_football_backend.repositories.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;
    private final FantasyTeamService fantasyTeamService;
    private final PlayerService playerService;
    private final LiveUpdateService liveUpdateService;

    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public MatchDTO getMatchById(Long id) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));
        return mapToDTO(match);
    }

    public List<MatchDTO> getLiveMatches() {
        return matchRepository.findByStatus("LIVE").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getUpcomingMatches() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        return matchRepository.findByKickoffTimeBetween(now, weekLater).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public MatchDTO createMatch(Match match) {
        match.setStatus("SCHEDULED");
        return mapToDTO(matchRepository.save(match));
    }

    @Transactional
    public MatchDTO updateMatchStatus(Long id, String status, int homeScore, int awayScore) {
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + id));

        boolean wasFinished = "FINISHED".equals(match.getStatus());

        match.setStatus(status);
        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);

        Match updatedMatch = matchRepository.save(match);

        // If match just finished, update player points and team scores
        if (!wasFinished && "FINISHED".equals(status)) {
            playerService.updateAllPlayerPoints();
            fantasyTeamService.updateAllTeamScores();
        }

        // Send update via WebSocket
        liveUpdateService.broadcastMatchUpdate(mapToDTO(updatedMatch));

        return mapToDTO(updatedMatch);
    }

    @Transactional
    public void updateScheduledMatchesToLive() {
        LocalDateTime now = LocalDateTime.now();
        List<Match> scheduledMatches = matchRepository.findByStatusAndKickoffTimeBefore("SCHEDULED", now);

        for (Match match : scheduledMatches) {
            match.setStatus("LIVE");
            matchRepository.save(match);
        }
    }

    private MatchDTO mapToDTO(Match match) {
        MatchDTO dto = new MatchDTO();
        dto.setId(match.getId());
        dto.setHomeTeam(match.getHomeTeam());
        dto.setAwayTeam(match.getAwayTeam());
        dto.setCompetition(match.getCompetition());
        dto.setKickoffTime(match.getKickoffTime());
        dto.setStatus(match.getStatus());
        dto.setHomeScore(match.getHomeScore());
        dto.setAwayScore(match.getAwayScore());
        return dto;
    }
}