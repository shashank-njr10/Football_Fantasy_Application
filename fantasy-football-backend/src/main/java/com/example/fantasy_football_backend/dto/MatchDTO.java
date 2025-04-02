package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDTO {
    private Long id;
    private String homeTeam;
    private String awayTeam;
    private String competition;
    private LocalDateTime kickoffTime;
    private String status;
    private int homeScore;
    private int awayScore;
}