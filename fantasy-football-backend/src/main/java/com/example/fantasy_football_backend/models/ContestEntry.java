package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_entries",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contest_id", "team_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"contest", "team"})
public class ContestEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private FantasyTeam team;

    @Column(name = "`rank`")
    private int rank;

    private int score;

    private LocalDateTime joinedAt;

    // Convenience method to check if this entry belongs to a specific user
    public boolean isOwnedByUser(Long userId) {
        return team != null && team.getOwner() != null && team.getOwner().getId().equals(userId);
    }
}