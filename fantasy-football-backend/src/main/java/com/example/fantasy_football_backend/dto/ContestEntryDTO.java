package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestEntryDTO {
    private Long id;
    private Long contestId;
    private String contestName;
    private Long teamId;
    private String teamName;
    private int rank;
    private int score;
    private LocalDateTime joinedAt;

    // Fields for nested objects in case needed
    private ContestDTO contest;
}