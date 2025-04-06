package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "fantasy_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"owner", "players", "contestEntries"})
public class FantasyTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private int totalPoints;

    private int totalScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "fantasy_team_players",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    private Set<Player> players = new HashSet<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ContestEntry> contestEntries = new HashSet<>();

    // Helper methods for team validation

    public int calculateTeamCost() {
        return players.stream().mapToInt(Player::getCost).sum();
    }

    public boolean isWithinBudget() {
        return calculateTeamCost() <= 100;
    }

    public boolean hasFullSquad() {
        return players.size() == 11;
    }

    public Map<String, Integer> getPlayersPerTeam() {
        Map<String, Integer> teamCounts = new HashMap<>();

        for (Player player : players) {
            String teamName = player.getTeam();
            teamCounts.put(teamName, teamCounts.getOrDefault(teamName, 0) + 1);
        }

        return teamCounts;
    }

    public boolean isValidTeamDistribution() {
        // Max 4 players from any real team
        return getPlayersPerTeam().values().stream().noneMatch(count -> count > 4);
    }

    public boolean isInContest(Long contestId) {
        return contestEntries.stream()
                .anyMatch(entry -> entry.getContest().getId().equals(contestId));
    }

    public Set<String> getPositionCounts() {
        return players.stream()
                .collect(Collectors.groupingBy(
                        Player::getPosition,
                        Collectors.counting()))
                .entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.toSet());
    }
}