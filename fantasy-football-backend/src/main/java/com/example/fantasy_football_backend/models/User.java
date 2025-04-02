package com.example.fantasy_football_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String profilePicture;

    private boolean isAdmin;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<FantasyTeam> fantasyTeams = new HashSet<>();
}