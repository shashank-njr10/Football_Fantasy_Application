package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private FantasyTeam team;

    @Column(name = "`rank`")
    private int rank;

    private int score;

    private LocalDateTime joinedAt;
}