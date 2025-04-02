package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FantasyTeamDTO {
    private Long id;
    private String name;
    private int totalPoints;
    private int totalScore;
    private Long ownerId;
    private String ownerName;
    private List<PlayerDTO> players;
    private int usedPoints;
    private int remainingPoints;
}