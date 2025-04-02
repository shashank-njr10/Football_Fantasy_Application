package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String position;

    private String team;

    private int cost;

    private int totalPoints;

    @ManyToMany(mappedBy = "players")
    private Set<FantasyTeam> fantasyTeams = new HashSet<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private Set<PlayerPerformance> performances = new HashSet<>();
}