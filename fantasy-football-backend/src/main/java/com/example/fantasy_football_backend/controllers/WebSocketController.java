package com.example.fantasy_football_backend.controllers;

import com.example.fantasy_football_backend.dto.MatchDTO;
import com.example.fantasy_football_backend.dto.PlayerPerformanceDTO;
import com.example.fantasy_football_backend.services.LiveUpdateService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class WebSocketController {

    private final LiveUpdateService liveUpdateService;

    public WebSocketController(LiveUpdateService liveUpdateService) {
        this.liveUpdateService = liveUpdateService;
    }

    @MessageMapping("/matches")
    @SendTo("/topic/matches")
    public MatchDTO broadcastMatchUpdate(MatchDTO match) {
        return match;
    }

    @MessageMapping("/player-performances")
    @SendTo("/topic/player-performances")
    public PlayerPerformanceDTO broadcastPlayerPerformanceUpdate(PlayerPerformanceDTO performance) {
        return performance;
    }

    @MessageMapping("/teams/{teamId}")
    @SendTo("/topic/teams/{teamId}")
    public Map<String, Object> sendTeamUpdate(@DestinationVariable Long teamId, Map<String, Object> update) {
        return update;
    }

    @MessageMapping("/contests/{contestId}/leaderboard")
    @SendTo("/topic/contests/{contestId}/leaderboard")
    public List<Map<String, Object>> broadcastContestLeaderboard(@DestinationVariable Long contestId, List<Map<String, Object>> leaderboard) {
        return leaderboard;
    }

    @MessageMapping("/notifications/{userId}")
    @SendTo("/topic/notifications/{userId}")
    public Map<String, Object> sendUserNotification(@DestinationVariable Long userId, Map<String, Object> notification) {
        return notification;
    }
}