package com.example.fantasy_football_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
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
    private List<PlayerDTO> players = new ArrayList<>();
    private int usedPoints;
    private int remainingPoints;
    private List<ContestEntryDTO> contestEntries = new ArrayList<>();

    // Helper method for tests
    public boolean isInContest(Long contestId) {
        return contestEntries.stream()
                .anyMatch(entry -> entry.getContestId().equals(contestId));
    }
}