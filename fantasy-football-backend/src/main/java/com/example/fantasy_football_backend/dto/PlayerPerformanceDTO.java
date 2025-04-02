package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPerformanceDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private String position;
    private String team;
    private Long matchId;
    private int goals;
    private int assists;
    private int minutesPlayed;
    private boolean cleanSheet;
    private boolean yellowCard;
    private boolean redCard;
    private int saves;
    private int totalPoints;
}