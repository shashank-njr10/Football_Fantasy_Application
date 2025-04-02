package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_performances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private int goals;

    private int assists;

    private int minutesPlayed;

    private boolean cleanSheet;

    private boolean yellowCard;

    private boolean redCard;

    private int saves;

    private int totalPoints;
}