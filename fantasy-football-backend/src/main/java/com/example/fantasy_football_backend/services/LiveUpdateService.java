package com.example.fantasy_football_backend.services;

import com.example.fantasy_football_backend.dto.FantasyTeamDTO;
import com.example.fantasy_football_backend.dto.MatchDTO;
import com.example.fantasy_football_backend.dto.PlayerPerformanceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveUpdateService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push live match updates to all connected clients
     */
    public void broadcastMatchUpdate(MatchDTO match) {
        messagingTemplate.convertAndSend("/topic/matches", match);
    }

    /**
     * Push player performance updates to all connected clients
     */
    public void broadcastPlayerPerformanceUpdate(PlayerPerformanceDTO performance) {
        messagingTemplate.convertAndSend("/topic/player-performances", performance);
    }

    /**
     * Push fantasy team score updates to specific team owners
     */
    public void sendTeamScoreUpdate(FantasyTeamDTO team) {
        messagingTemplate.convertAndSend("/topic/teams/" + team.getId(), team);
    }

    /**
     * Push contest leaderboard updates
     */
    public void broadcastContestLeaderboard(Long contestId, List<Map<String, Object>> leaderboard) {
        messagingTemplate.convertAndSend("/topic/contests/" + contestId + "/leaderboard", leaderboard);
    }

    /**
     * Send notification to specific user
     */
    public void sendUserNotification(Long userId, String message) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, Map.of(
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
    }
}